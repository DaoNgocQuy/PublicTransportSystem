import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { MapContainer, TileLayer, Polyline, Marker, Popup, useMap, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './map.css';
import { FaLandmark } from 'react-icons/fa';
import { divIcon } from 'leaflet';
import { renderToString } from 'react-dom/server';

// Solve default icon issues
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
    iconUrl: require("leaflet/dist/images/marker-icon.png"),
    shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
});

// Component to fly map to user location
const SetViewToUserLocation = ({ position }) => {
    const map = useMap();
    useEffect(() => {
        if (position) {
            map.flyTo(position, map.getZoom());
        }
    }, [map, position]);
    return null;
};

// Component to fly map to selected stop
const SetViewToSelectedStop = ({ selectedStop }) => {
    const map = useMap();
    useEffect(() => {
        if (selectedStop?.latitude && selectedStop?.longitude) {
            map.flyTo([selectedStop.latitude, selectedStop.longitude], 17);
        }
    }, [map, selectedStop]);
    return null;
};

// Component to fly map to first/last stop when route is selected
const SetViewToFirstStop = ({ busStops, selectedRoute, tripDirection, focusFirstStop }) => {
    const map = useMap();
    const previousDirectionRef = useRef(tripDirection);

    useEffect(() => {
        if (selectedRoute && busStops?.length > 0 && focusFirstStop) {
            if (tripDirection !== previousDirectionRef.current || busStops.length > 0) {
                previousDirectionRef.current = tripDirection;

                const stopToFocus = tripDirection === 'outbound'
                    ? busStops[0]
                    : busStops[busStops.length - 1];

                if (stopToFocus?.latitude && stopToFocus?.longitude) {
                    map.flyTo([stopToFocus.latitude, stopToFocus.longitude], 16, {
                        duration: 1.5
                    });
                }
            }
        }
    }, [map, busStops, selectedRoute, tripDirection, focusFirstStop]);

    return null;
};

const RoutePath = React.memo(({ busStops, tripDirection }) => {
    const [routeSegments, setRouteSegments] = useState([]);
    const map = useMap();
    const routeCache = useRef({});
    const isLoadingRef = useRef(false);
    const routeKeyRef = useRef('');

    // Tạo key duy nhất cho tuyến đường hiện tại
    const routeKey = useMemo(() => {
        if (!busStops || busStops.length < 2) return '';
        return busStops.map(stop => stop.id).join('-') + '-' + tripDirection;
    }, [busStops, tripDirection]);

    // Xử lý việc fetch dữ liệu đường đi
    useEffect(() => {
        // Nếu route key không thay đổi hoặc đang loading, không thực hiện lại
        if (routeKey === routeKeyRef.current || isLoadingRef.current || !busStops || busStops.length < 2) return;

        // Cập nhật route key hiện tại
        routeKeyRef.current = routeKey;
        isLoadingRef.current = true;

        const stopsToUse = tripDirection === 'outbound'
            ? busStops
            : [...busStops].reverse();

        const fetchRouteSegment = async (start, end) => {
            const cacheKey = `${start.id}-${end.id}`;

            if (routeCache.current[cacheKey]) {
                return routeCache.current[cacheKey];
            }

            try {
                const startPoint = `${start.longitude},${start.latitude}`;
                const endPoint = `${end.longitude},${end.latitude}`;

                const response = await fetch(
                    `https://router.project-osrm.org/route/v1/driving/${startPoint};${endPoint}?overview=full&geometries=geojson`
                );

                const data = await response.json();

                let coordinates;
                if (data.routes && data.routes.length > 0) {
                    coordinates = data.routes[0].geometry.coordinates.map(coord => [coord[1], coord[0]]);
                } else {
                    coordinates = [
                        [start.latitude, start.longitude],
                        [end.latitude, end.longitude]
                    ];
                }

                routeCache.current[cacheKey] = coordinates;
                return coordinates;
            } catch (error) {
                const fallback = [
                    [start.latitude, start.longitude],
                    [end.latitude, end.longitude]
                ];
                routeCache.current[cacheKey] = fallback;
                return fallback;
            }
        };

        // Hàm fetch tất cả các đoạn đường
        const fetchAllSegments = async () => {
            const segments = [];
            const fetchPromises = [];

            for (let i = 0; i < stopsToUse.length - 1; i++) {
                const start = stopsToUse[i];
                const end = stopsToUse[i + 1];

                if (start?.latitude && start?.longitude && end?.latitude && end?.longitude) {
                    const promise = fetchRouteSegment(start, end).then(coordinates => {
                        segments.push({
                            id: `${start.id}-${end.id}`,
                            positions: coordinates
                        });
                    });
                    fetchPromises.push(promise);
                }
            }

            await Promise.all(fetchPromises);

            // Lưu kết quả và cập nhật state
            setRouteSegments(segments);
            isLoadingRef.current = false;

            // Điều chỉnh view bản đồ chỉ khi cần thiết
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
    }, [routeKey, busStops, tripDirection, map]);

    // Không bao giờ trả về null để đảm bảo component không bị unmount/remount
    const pathColor = tripDirection === 'outbound' ? '#4CAF50' : '#1976D2';

    if (routeSegments.length === 0) {
        // Trả về một element trống thay vì null để giữ component trong DOM
        return <div style={{ display: 'none' }} />;
    }

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
}, (prevProps, nextProps) => {
    // Custom comparison function để tránh re-render không cần thiết
    if (!prevProps.busStops || !nextProps.busStops) return false;

    if (prevProps.tripDirection !== nextProps.tripDirection) return false;

    if (prevProps.busStops.length !== nextProps.busStops.length) return false;

    // Chỉ so sánh ID của các trạm để đơn giản hóa
    const prevIds = prevProps.busStops.map(stop => stop.id).join('-');
    const nextIds = nextProps.busStops.map(stop => stop.id).join('-');

    return prevIds === nextIds;
});
// Optimized Map events handler
const MapEventsHandler = ({ onBoundsChange, onZoomChange }) => {
    const map = useMapEvents({
        moveend: () => onBoundsChange?.(map.getBounds()),
        zoomend: () => onZoomChange?.(map.getZoom())
    });

    useEffect(() => {
        onBoundsChange?.(map.getBounds());
        onZoomChange?.(map.getZoom());
    }, [map, onBoundsChange, onZoomChange]);

    return null;
};

// Custom locate control
const LocateControl = ({ userLocation }) => {
    const map = useMap();

    useEffect(() => {
        const locateControl = L.control({ position: 'bottomright' });

        locateControl.onAdd = function () {
            const div = L.DomUtil.create('div', 'leaflet-control-locate');
            const button = L.DomUtil.create('button', 'leaflet-control-locate-button', div);

            button.innerHTML = `
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"></polygon>
                </svg>
            `;

            L.DomEvent.on(button, 'click', function () {
                if (userLocation) {
                    button.classList.add('active');
                    map.flyTo(userLocation, 17);
                    setTimeout(() => button.classList.remove('active'), 500);
                }
            });

            L.DomEvent.disableClickPropagation(div);
            return div;
        };

        locateControl.addTo(map);
        return () => locateControl.remove();
    }, [map, userLocation]);

    return null;
};

// Default coordinates for Ho Chi Minh City
const DEFAULT_LOCATION = [10.7769, 106.7009];
const MIN_ZOOM_SHOW_STOPS = 17;

const MapLeaflet = ({ busStops = [], allStops = [], selectedRoute, tripDirection, focusedStopId, landmarks = [], selectionMode = 'destination', onLocationSelect }) => {
    const [userLocation, setUserLocation] = useState(null);
    const [map, setMap] = useState(null);
    const [accuracy, setAccuracy] = useState(0);
    const [selectedStop, setSelectedStop] = useState(null);
    const [mapBounds, setMapBounds] = useState(null);
    const [zoomLevel, setZoomLevel] = useState(15);
    const [visibleStops, setVisibleStops] = useState([]);
    const [focusFirstStop, setFocusFirstStop] = useState(true);
    const prevDirectionRef = useRef(tripDirection);
    const markersRef = useRef([]);
    const [selectedMapLocation, setSelectedMapLocation] = useState(null);
    const [originLocation, setOriginLocation] = useState(null);
    const [destinationLocation, setDestinationLocation] = useState(null);

    const originMarkerIcon = useMemo(() => L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
    }), []);

    const destinationMarkerIcon = useMemo(() => L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
    }), []);

    const DoubleClickHandler = () => {
        useMapEvents({
            dblclick: (e) => {
                const { lat, lng } = e.latlng;
                console.log(`Double clicked at: ${lat}, ${lng} in mode: ${selectionMode}`);

                // Tạo marker mới với màu phù hợp
                const markerIcon = selectionMode === 'origin' ? originMarkerIcon : destinationMarkerIcon;

                // Lưu vị trí click theo loại điểm
                if (selectionMode === 'origin') {
                    setOriginLocation({
                        coords: [lat, lng],
                        type: 'origin'
                    });
                } else {
                    setDestinationLocation({
                        coords: [lat, lng],
                        type: 'destination'
                    });
                }

                // Gửi thông tin địa điểm đã chọn về component cha
                const locationData = {
                    latitude: lat,
                    longitude: lng,
                    stop_name: 'Vị trí đã chọn',
                    address: `[${lat.toFixed(6)}, ${lng.toFixed(6)}]`,
                    locationType: selectionMode
                };

                // Thực hiện reverse geocoding nếu cần
                fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`)
                    .then(response => response.json())
                    .then(data => {
                        const betterLocationData = {
                            ...locationData,
                            stop_name: data.display_name.split(',')[0] || 'Vị trí đã chọn',
                            address: data.display_name
                        };
                        onLocationSelect(betterLocationData);
                    })
                    .catch(err => {
                        console.error("Lỗi reverse geocoding:", err);
                        onLocationSelect(locationData);
                    });
            }
        });
        return null;
    };


    // Move the icon creation inside component
    const busStopIcon = useMemo(() => L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
    }), []);

    // Set selected stop when focusedStopId changes
    useEffect(() => {
        if (focusedStopId && busStops?.length > 0) {
            const stopToFocus = busStops.find(stop => stop.id === focusedStopId);
            if (stopToFocus) {
                setSelectedStop(stopToFocus);
                setFocusFirstStop(false);
            }
        }
    }, [focusedStopId, busStops]);

    // Handle route or direction changes
    useEffect(() => {
        if (selectedRoute) {
            if (prevDirectionRef.current !== tripDirection) {
                prevDirectionRef.current = tripDirection;
                setSelectedStop(null);
            }
            setFocusFirstStop(true);
        }
    }, [selectedRoute, tripDirection]);

    // Get user location
    useEffect(() => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const { latitude, longitude, accuracy } = position.coords;
                    setUserLocation([latitude, longitude]);
                    setAccuracy(accuracy);
                },
                () => setUserLocation(DEFAULT_LOCATION),
                { enableHighAccuracy: true }
            );
        } else {
            setUserLocation(DEFAULT_LOCATION);
        }
    }, []);

    // Memoize stopsToUse
    const stopsToUse = useMemo(() => {
        if (selectedRoute) {
            return busStops?.length > 0 ? busStops : [];
        }
        return allStops?.length > 0 ? allStops : [];
    }, [busStops, allStops, selectedRoute]);

    // Map event handlers
    const handleBoundsChange = useCallback((bounds) => {
        setMapBounds(bounds);
    }, []);

    const handleZoomChange = useCallback((zoom) => {
        setZoomLevel(zoom);
    }, []);

    // Filter visible stops based on map bounds and zoom
    useEffect(() => {
        if (!mapBounds || !stopsToUse?.length) return;

        // Show all route stops when a route is selected
        if (selectedRoute) {
            setVisibleStops(stopsToUse);
            return;
        }

        // Show all stops at high zoom levels
        if (zoomLevel >= 18) {
            setVisibleStops(stopsToUse);
            return;
        }

        const shouldShowAllStops = zoomLevel >= MIN_ZOOM_SHOW_STOPS;
        const filtered = stopsToUse.filter(stop => {
            if (!stop?.latitude || !stop?.longitude) return false;

            // Always show selected stop
            if (selectedStop?.id === stop.id) return true;

            // Filter by map bounds and zoom
            if (mapBounds.contains([stop.latitude, stop.longitude])) {
                if (shouldShowAllStops) return true;
                if (stop.isMainStop) return true;
            }

            return false;
        });

        setVisibleStops(filtered);
    }, [mapBounds, zoomLevel, selectedRoute, selectedStop, stopsToUse]);
    useEffect(() => {
        // Đảm bảo map đã được khởi tạo trước khi thêm markers
        if (!map || !landmarks || landmarks.length === 0) return;

        // Xóa markers hiện tại
        markersRef.current.forEach(marker => {
            if (marker._map) { // Kiểm tra marker có tồn tại trên map không
                marker.remove();
            }
        });

        // Mảng để lưu các markers mới
        const newMarkers = [];

        landmarks.forEach(landmark => {
            if (landmark.latitude && landmark.longitude) {
                const landmarkIcon = divIcon({
                    html: renderToString(
                        <div style={{ color: '#E91E63' }}>
                            <FaLandmark size={22} />
                        </div>
                    ),
                    className: 'landmark-icon',
                    iconSize: [24, 24],
                    iconAnchor: [12, 24]
                });

                try {
                    // Tạo marker và thêm vào map nếu map đã được khởi tạo
                    const marker = L.marker([landmark.latitude, landmark.longitude], {
                        icon: landmarkIcon
                    });

                    marker.addTo(map);

                    // Popup cho landmark
                    marker.bindPopup(`
                <div class="landmark-popup">
                    <h3>${landmark.name}</h3>
                    <p>${landmark.address || ''}</p>
                    ${landmark.description ? `<p class="description">${landmark.description}</p>` : ''}
                </div>
            `);

                    newMarkers.push(marker);
                } catch (error) {
                    console.error("Lỗi khi thêm landmark marker:", error);
                }
            }
        });

        // Cập nhật ref thay vì state
        markersRef.current = newMarkers;

        // Cleanup function
        return () => {
            newMarkers.forEach(marker => {
                if (marker._map) { // Kiểm tra marker có tồn tại trên map không
                    marker.remove();
                }
            });
        };
    }, [map, landmarks]);
    // Create user location marker icon
    const userLocationIcon = useMemo(() => L.divIcon({
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
    }), []);

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
            <div className="map-instructions">
                Nhấp đúp để chọn {selectionMode === 'origin' ? 'điểm đi' : 'điểm đến'} trên bản đồ
            </div>

            <DoubleClickHandler />

            {originLocation && (
                <Marker
                    position={originLocation.coords}
                    icon={originMarkerIcon}
                >
                    <Popup>
                        <div>
                            <strong>Điểm đi đã chọn</strong>
                            <p>Nhấn đúp để chọn vị trí khác</p>
                        </div>
                    </Popup>
                </Marker>
            )}

            {/* Hiển thị marker cho điểm đến nếu có */}
            {destinationLocation && (
                <Marker
                    position={destinationLocation.coords}
                    icon={destinationMarkerIcon}
                >
                    <Popup>
                        <div>
                            <strong>Điểm đến đã chọn</strong>
                            <p>Nhấn đúp để chọn vị trí khác</p>
                        </div>
                    </Popup>
                </Marker>
            )}
            {selectedRoute && busStops.length > 1 && (
                <RoutePath
                    busStops={busStops}
                    tripDirection={tripDirection}
                />
            )}

            <Marker position={userLocation} icon={userLocationIcon}>
                <Popup>
                    <div>
                        <strong>Vị trí của bạn</strong>
                        <br />
                        Độ chính xác: {Math.round(accuracy)} mét
                    </div>
                </Popup>
            </Marker>

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

            <SetViewToUserLocation position={userLocation} />
            <SetViewToSelectedStop selectedStop={selectedStop} />
            <SetViewToFirstStop
                busStops={busStops}
                selectedRoute={selectedRoute}
                tripDirection={tripDirection}
                focusFirstStop={focusFirstStop}

            />
            <MapEventsHandler
                onBoundsChange={handleBoundsChange}
                onZoomChange={handleZoomChange}
            />
            <LocateControl userLocation={userLocation} />
        </MapContainer>
    );
};

export default MapLeaflet;