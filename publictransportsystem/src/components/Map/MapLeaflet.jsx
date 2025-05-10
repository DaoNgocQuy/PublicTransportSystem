import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { MapContainer, TileLayer, Polyline, Marker, Popup, useMap, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './map.css';

// Xóa cài đặt icon mặc định để tránh lỗi
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
    iconUrl: require("leaflet/dist/images/marker-icon.png"),
    shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
});

// Icon cho trạm xe buýt
const busStopIcon = L.icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

// Component để di chuyển map đến vị trí user mỗi khi update
const SetViewToUserLocation = ({ position }) => {
    const map = useMap();
    useEffect(() => {
        if (position) {
            map.flyTo(position, map.getZoom());
        }
    }, [map, position]);
    return null;
};

// Component để di chuyển map đến trạm được chọn
const SetViewToSelectedStop = ({ selectedStop }) => {
    const map = useMap();

    useEffect(() => {
        if (selectedStop && selectedStop.latitude && selectedStop.longitude) {
            map.flyTo([selectedStop.latitude, selectedStop.longitude], 17);
        }
    }, [map, selectedStop]);

    return null;
};

// Component để di chuyển map đến trạm đầu tiên khi chọn route
const SetViewToFirstStop = ({ busStops, selectedRoute, tripDirection, focusFirstStop }) => {
    const map = useMap();
    const previousDirectionRef = useRef(tripDirection);

    useEffect(() => {
        // Focus appropriate stop when route is selected or direction changes
        if (selectedRoute && busStops && busStops.length > 0 && focusFirstStop) {
            // Focus on appropriate stop if direction changed or new route selected
            if (tripDirection !== previousDirectionRef.current || busStops.length > 0) {
                previousDirectionRef.current = tripDirection;

                // Lấy trạm phù hợp để focus (đầu tiên cho lượt đi, cuối cùng cho lượt về)
                const stopToFocus = tripDirection === 'outbound'
                    ? busStops[0]                         // first stop for outbound
                    : busStops[busStops.length - 1];      // last stop for return

                if (stopToFocus && stopToFocus.latitude && stopToFocus.longitude) {
                    // Fly to appropriate stop with animation
                    map.flyTo([stopToFocus.latitude, stopToFocus.longitude], 16, {
                        duration: 1.5 // Smooth animation
                    });
                }
            }
        }
    }, [map, busStops, selectedRoute, tripDirection, focusFirstStop]);

    return null;
};
const RoutePath = ({ busStops, tripDirection }) => {
    const [routeSegments, setRouteSegments] = useState([]);
    const [loading, setLoading] = useState(false);
    const map = useMap();

    // Generate route segments between stops using OSRM
    useEffect(() => {
        if (!busStops || busStops.length < 2) {
            setRouteSegments([]);
            return;
        }

        // Choose order based on direction
        const stopsToUse = tripDirection === 'outbound'
            ? busStops
            : [...busStops].reverse();

        const fetchRouteSegment = async (start, end) => {
            try {
                // Format: lon,lat for OSRM
                const startPoint = `${start.longitude},${start.latitude}`;
                const endPoint = `${end.longitude},${end.latitude}`;

                // Use OSRM routing service
                const response = await fetch(
                    `https://router.project-osrm.org/route/v1/driving/${startPoint};${endPoint}?overview=full&geometries=geojson`
                );

                const data = await response.json();

                if (data.routes && data.routes.length > 0) {
                    // OSRM returns coordinates as [longitude, latitude], but Leaflet needs [latitude, longitude]
                    const coordinates = data.routes[0].geometry.coordinates.map(coord => [coord[1], coord[0]]);
                    return coordinates;
                }

                // Fallback to direct line if routing fails
                return [
                    [start.latitude, start.longitude],
                    [end.latitude, end.longitude]
                ];
            } catch (error) {
                console.error("Error fetching route:", error);
                // Fallback to direct line
                return [
                    [start.latitude, start.longitude],
                    [end.latitude, end.longitude]
                ];
            }
        };

        const fetchAllSegments = async () => {
            setLoading(true);
            const segments = [];

            for (let i = 0; i < stopsToUse.length - 1; i++) {
                const start = stopsToUse[i];
                const end = stopsToUse[i + 1];

                if (start.latitude && start.longitude && end.latitude && end.longitude) {
                    const coordinates = await fetchRouteSegment(start, end);

                    segments.push({
                        id: `${start.id}-${end.id}`,
                        positions: coordinates
                    });
                }
            }

            setRouteSegments(segments);
            setLoading(false);

            // Adjust map bounds to fit the entire route
            if (segments.length > 0 && map) {
                const allCoordinates = segments.flatMap(segment => segment.positions);
                if (allCoordinates.length > 0) {
                    map.fitBounds(allCoordinates, {
                        padding: [50, 50],
                        maxZoom: 15
                    });
                }
            }
        };

        fetchAllSegments();
    }, [busStops, tripDirection, map]);

    // Color based on direction
    const pathColor = tripDirection === 'outbound' ? '#4CAF50' : '#1976D2';

    if (loading) return null;
    if (routeSegments.length === 0) return null;

    return (
        <>
            {routeSegments.map(segment => (
                <Polyline
                    key={segment.id}
                    positions={segment.positions}
                    color={pathColor}
                    weight={5}
                    opacity={0.8}
                    lineJoin="round"
                    lineCap="round"
                />
            ))}


        </>
    );
};
// Component để theo dõi sự kiện map
const MapEventsHandler = ({ onBoundsChange, onZoomChange }) => {
    const map = useMapEvents({
        moveend: () => {
            if (onBoundsChange) {
                onBoundsChange(map.getBounds());
            }
        },
        zoomend: () => {
            if (onZoomChange) {
                onZoomChange(map.getZoom());
            }
        }
    });

    // Kích hoạt sự kiện ngay khi component mount
    useEffect(() => {
        if (onBoundsChange) {
            onBoundsChange(map.getBounds());
        }
        if (onZoomChange) {
            onZoomChange(map.getZoom());
        }
    }, [map, onBoundsChange, onZoomChange]);

    return null;
};

// Custom control cho nút định vị
const LocateControl = ({ userLocation }) => {
    const map = useMap();

    useEffect(() => {
        // Tạo nút định vị và thêm vào góc phải dưới
        const locateControl = L.control({ position: 'bottomright' });

        locateControl.onAdd = function () {
            const div = L.DomUtil.create('div', 'leaflet-control-locate');
            const button = L.DomUtil.create('button', 'leaflet-control-locate-button', div);

            // Thêm biểu tượng định vị
            button.innerHTML = `
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"></polygon>
                </svg>
            `;

            // Thêm sự kiện click để di chuyển đến vị trí người dùng
            L.DomEvent.on(button, 'click', function () {
                if (userLocation) {
                    button.classList.add('active');
                    map.flyTo(userLocation, 17);

                    // Xóa class active sau khi animation hoàn thành
                    setTimeout(() => {
                        button.classList.remove('active');
                    }, 500);
                }
            });

            L.DomEvent.disableClickPropagation(div);
            return div;
        };

        locateControl.addTo(map);

        // Cleanup khi component unmount
        return () => {
            locateControl.remove();
        };
    }, [map, userLocation]);

    return null;
};

// Mẫu dữ liệu trạm bus nếu không có dữ liệu truyền vào


const MapLeaflet = ({ busStops = [], allStops = [], selectedRoute, tripDirection, focusedStopId }) => {
    const [userLocation, setUserLocation] = useState(null);
    const [map, setMap] = useState(null);
    const [accuracy, setAccuracy] = useState(0);
    const [selectedStop, setSelectedStop] = useState(null);
    const [mapBounds, setMapBounds] = useState(null);
    const [zoomLevel, setZoomLevel] = useState(15);
    const [visibleStops, setVisibleStops] = useState([]);
    const [focusFirstStop, setFocusFirstStop] = useState(true);
    const prevDirectionRef = useRef(tripDirection);

    // Khi focusedStopId thay đổi, tìm và cập nhật trạm được focus
    useEffect(() => {
        if (focusedStopId && busStops && busStops.length > 0) {
            const stopToFocus = busStops.find(stop => stop.id === focusedStopId);
            if (stopToFocus) {
                setSelectedStop(stopToFocus);
                // Mark that we've focused on a specific stop
                setFocusFirstStop(false);
            }
        }
    }, [focusedStopId, busStops]);
    useEffect(() => {
        console.log("Bus stops data received:", busStops?.length || 0);
        console.log("All stops data received:", allStops?.length || 0);
    }, [busStops, allStops]);

    // Focus vào trạm đầu tiên khi route hoặc direction thay đổi
    useEffect(() => {
        if (selectedRoute) {
            // Reset if the direction changed
            if (prevDirectionRef.current !== tripDirection) {
                prevDirectionRef.current = tripDirection;
                setSelectedStop(null); // Clear any selected stop
            }
            setFocusFirstStop(true); // Enable auto-focus on first/last stop
        }
    }, [selectedRoute, tripDirection]);
    useEffect(() => {
        console.log("Bus stops data received:", busStops);
    }, [busStops]);
    // Cấu hình hiển thị
    const MIN_ZOOM_SHOW_STOPS = 17;

    // Lấy vị trí người dùng
    useEffect(() => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const { latitude, longitude, accuracy } = position.coords;
                    setUserLocation([latitude, longitude]);
                    setAccuracy(accuracy);
                },
                (error) => {
                    console.error("Error getting user location:", error);
                    // Default to Ho Chi Minh City center if no location
                    setUserLocation([10.7769, 106.7009]);
                },
                { enableHighAccuracy: true }
            );
        } else {
            console.error("Geolocation is not supported by this browser.");
            // Default to Ho Chi Minh City center
            setUserLocation([10.7769, 106.7009]);
        }
    }, []);

    // Memoize stopsToUse để tránh tính toán lại mỗi khi render
    const stopsToUse = useMemo(() => {
        // Use allStops when no route is selected, otherwise use busStops (route-specific stops)
        if (selectedRoute) {
            return busStops && busStops.length > 0 ? busStops : [];
        } else {
            return allStops && allStops.length > 0 ? allStops : [];
        }
    }, [busStops, allStops, selectedRoute]);

    // Xử lý khi bounds map thay đổi
    const handleBoundsChange = useCallback((bounds) => {
        setMapBounds(bounds);
    }, []);

    // Xử lý khi zoom level thay đổi
    const handleZoomChange = useCallback((zoom) => {
        setZoomLevel(zoom);
    }, []);

    // Lọc trạm xe buýt hiển thị dựa vào bounds map và zoom level
    useEffect(() => {
        if (!mapBounds || !stopsToUse || stopsToUse.length === 0) return;

        // For debugging
        console.log("Filtering stops. Available:", stopsToUse.length, "Zoom:", zoomLevel);

        // Always show all route stops when a route is selected
        if (selectedRoute) {
            setVisibleStops(stopsToUse);
            return;
        }

        // For testing purposes, show ALL stops to verify they're loading correctly
        if (zoomLevel >= 18) {
            setVisibleStops(stopsToUse);
            return;
        }

        // Original filtering logic (only runs at very low zoom levels)
        const shouldShowAllStops = zoomLevel >= MIN_ZOOM_SHOW_STOPS;
        const filtered = stopsToUse.filter(stop => {
            if (!stop.latitude || !stop.longitude) return false;

            // Always show the selected stop
            if (selectedStop && selectedStop.id === stop.id) {
                return true;
            }

            // For general map browsing, show stops based on zoom level and map bounds
            if (mapBounds.contains([stop.latitude, stop.longitude])) {
                if (shouldShowAllStops) {
                    return true; // Show all stops in bounds if zoom is sufficient
                }
                // At lower zoom, only show main stops if that property exists
                if (stop.isMainStop) {
                    return true;
                }
            }

            return false;
        });

        // Log for debugging
        console.log("Visible stops:", filtered.length);

        // Use filtered stops at very low zoom, otherwise show all stops
        setVisibleStops(filtered);
    }, [mapBounds, zoomLevel, selectedRoute, selectedStop, stopsToUse, MIN_ZOOM_SHOW_STOPS]);

    // Khi chưa có vị trí, hiển thị loading
    if (!userLocation) {
        return <div className="map-loading">Đang tải bản đồ...</div>;
    }

    return (
        <MapContainer
            center={userLocation}
            zoom={15}
            style={{ height: "100%", width: "100%" }}
            whenCreated={setMap}
        >
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            />
            {selectedRoute && busStops.length > 1 && (
                <RoutePath
                    busStops={busStops}
                    tripDirection={tripDirection}
                />
            )}
            {/* Marker cho vị trí người dùng */}
            <Marker
                position={userLocation}
                icon={L.divIcon({
                    className: 'user-location-marker',
                    html: `
            <div style="
                background-color: #2196F3;
                border: 2px solid white;
                border-radius: 50%;
                width: 16px;
                height: 16px;
                box-shadow: 0 0 10px rgba(33, 150, 243, 0.7);
                animation: pulse 1.5s infinite;
            "></div>
        `,
                    iconSize: [20, 20],
                    iconAnchor: [10, 10],
                    popupAnchor: [0, -10]
                })}
            >
                <Popup>
                    <div>
                        <strong>Vị trí của bạn</strong>
                        <br />
                        Độ chính xác: {Math.round(accuracy)} mét
                    </div>
                </Popup>
            </Marker>

            {/* Markers cho các trạm xe buýt */}
            {visibleStops.map(stop => (
                <Marker
                    key={stop.id}
                    position={[stop.latitude, stop.longitude]}
                    icon={busStopIcon}
                    eventHandlers={{
                        click: () => setSelectedStop(stop)
                    }}
                >
                    <Popup>
                        <div className="bus-stop-popup">
                            <h4>{stop.name}</h4>
                            <p>{stop.address}</p>
                            <button className="directions-btn">Chỉ đường đến đây</button>
                        </div>
                    </Popup>
                </Marker>
            ))}

            {/* Component để cập nhật view khi vị trí user thay đổi */}
            <SetViewToUserLocation position={userLocation} />

            {/* Component để focus vào trạm đã chọn từ danh sách */}
            <SetViewToSelectedStop selectedStop={selectedStop} />

            {/* Component để focus vào trạm đầu tiên khi chọn route */}
            <SetViewToFirstStop
                busStops={busStops}
                selectedRoute={selectedRoute}
                tripDirection={tripDirection}
                focusFirstStop={focusFirstStop}
            />

            {/* Component theo dõi sự kiện map */}
            <MapEventsHandler
                onBoundsChange={handleBoundsChange}
                onZoomChange={handleZoomChange}
            />

            {/* Nút định vị */}
            <LocateControl userLocation={userLocation} />
        </MapContainer>
    );
};

export default MapLeaflet;