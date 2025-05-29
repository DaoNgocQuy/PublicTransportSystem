import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { MapContainer, TileLayer, Polyline, Marker, Popup, useMap, useMapEvents, CircleMarker } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './map.css';

// Khắc phục vấn đề với icon mặc định
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
    iconUrl: require("leaflet/dist/images/marker-icon.png"),
    shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
});

// Component để di chuyển bản đồ đến vị trí người dùng
const SetViewToUserLocation = ({ position, shouldFocus = false }) => {
    const map = useMap();
    useEffect(() => {
        // Chỉ fly đến vị trí người dùng nếu shouldFocus = true
        if (position && shouldFocus) {
            map.flyTo(position, map.getZoom());
        }
    }, [map, position, shouldFocus]);
    return null;
};

// Component để di chuyển bản đồ đến trạm đã chọn
const SetViewToSelectedStop = ({ selectedStop }) => {
    const map = useMap();
    useEffect(() => {
        if (selectedStop?.latitude && selectedStop?.longitude) {
            map.flyTo([selectedStop.latitude, selectedStop.longitude], 17);
        }
    }, [map, selectedStop]);
    return null;
};

// Component để di chuyển bản đồ đến trạm đầu tiên khi chọn tuyến
const SetViewToFirstStop = ({ busStops, selectedRoute }) => {
    const map = useMap();

    useEffect(() => {
        if (selectedRoute && busStops?.length > 0) {
            const stopToFocus = busStops[0];
            if (stopToFocus?.latitude && stopToFocus?.longitude) {
                // Sử dụng flyTo với duration ngắn hơn để giảm thời gian chờ
                map.flyTo([stopToFocus.latitude, stopToFocus.longitude], 16, {
                    duration: 1.1 // Giảm thời gian animation xuống còn 0.75 giây
                });
            }
        }
    }, [map, busStops, selectedRoute]);

    return null;
};


const RoutePath = React.memo(({ busStops, selectedRoute, direction }) => {
    const [routeCoordinates, setRouteCoordinates] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [cachedRoutes, setCachedRoutes] = useState({
        outbound: [],
        return: []
    });
    const prevBusStopsRef = React.useRef(null);
    const map = useMap();
    const [mapHasMoved, setMapHasMoved] = useState(false);
    const cacheKey = useMemo(() => {
        return direction === 'return' ? 'return' : 'outbound';
    }, [direction]);
    // Xác định loại tuyến và màu sắc
    const routeTypeId = useMemo(() => {
        // Kiểm tra xem selectedRoute chứa thông tin gì
        console.log("Selected Route Data:", selectedRoute);

        // Kiểm tra các cách lấy id khác nhau
        const id = selectedRoute?.routeTypeId;



        console.log("Route Type ID:", id);
        return id;
    }, [selectedRoute]);
    const isMetroLine = useMemo(() => {
        // Kiểm tra theo ID của loại tuyến (2 = Metro)
        const byId = routeTypeId === 2;

        // Kiểm tra theo tên nếu không có ID
        const byName = selectedRoute?.name?.toLowerCase().includes('metro') ||
            selectedRoute?.routeName?.toLowerCase().includes('metro');

        const result = byId || byName;
        console.log("Is Metro Line:", result);
        return result;
    }, [routeTypeId, selectedRoute]);
    const directionNumber = useMemo(() => {
        const parsedDirection = parseInt(direction) || 1;
        console.log("Direction (parsed):", parsedDirection, "Type:", typeof parsedDirection);
        return parsedDirection;
    }, [direction]);


    const lineColor = useMemo(() => {
        // Lấy màu từ database
        const routeColor = selectedRoute?.color;
        const routeTypeColor = selectedRoute?.routeType?.colorCode;

        // Màu mặc định nếu không có trong database
        const DEFAULT_COLOR = "#4CAF50"; // Xanh lá (mặc định)


        return routeColor || routeTypeColor || DEFAULT_COLOR;
    }, [selectedRoute]);

    useEffect(() => {
        const handleMoveEnd = () => {
            setMapHasMoved(true);
        };

        map.on('moveend', handleMoveEnd);

        return () => {
            map.off('moveend', handleMoveEnd);
        };
    }, [map]);
    useEffect(() => {
        setMapHasMoved(false);
    }, [busStops, direction]);
    // Thêm directionNumber vào dependencies để rerrender khi direction thay đổi
    useEffect(() => {
        if (!mapHasMoved && busStops?.length > 0) {
            return;
        }
        // Kiểm tra nếu không có đủ trạm để vẽ đường
        if (!busStops || busStops.length < 2) {
            setRouteCoordinates([]);
            return;
        }

        if (isMetroLine) {
            const straightLinePositions = busStops.map(stop => [stop.latitude, stop.longitude]);
            setRouteCoordinates(straightLinePositions);
            return;
        }
        const busStopsChanged = !prevBusStopsRef.current ||
            busStops.length !== prevBusStopsRef.current.length ||
            JSON.stringify(busStops.map(s => s.id)) !== JSON.stringify(prevBusStopsRef.current.map(s => s.id));

        // Cập nhật ref để lần sau so sánh
        prevBusStopsRef.current = busStops;
        // Nếu không phải metro, lấy đường đi thực tế từ API
        const fetchRouteGeometry = async () => {
            setIsLoading(true);
            try {
                if (cachedRoutes[cacheKey] &&
                    cachedRoutes[cacheKey].length > 0 &&
                    !busStopsChanged) {
                    console.log("Sử dụng dữ liệu đã lưu trong bộ đệm cho:", cacheKey);
                    setRouteCoordinates(cachedRoutes[cacheKey]);

                    return;
                }
                // Xây dựng chuỗi tọa độ cho API đường đi
                const coordinates = busStops.map(stop => `${stop.longitude},${stop.latitude}`).join(';');

                // Sử dụng OSRM API miễn phí để lấy đường đi thực tế
                const response = await fetch(
                    `https://router.project-osrm.org/route/v1/driving/${coordinates}?overview=full&geometries=geojson`
                );

                const data = await response.json();

                if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
                    // Lấy tọa độ từ GeoJSON và biến đổi thành định dạng của Leaflet
                    const coords = data.routes[0].geometry.coordinates;
                    const latLngs = coords.map(coord => [coord[1], coord[0]]);
                    setRouteCoordinates(latLngs);
                    setCachedRoutes(prev => ({
                        ...prev,
                        [cacheKey]: latLngs
                    }));
                }
                else {
                    setRouteCoordinates([]);
                }
            } catch (error) {
                console.error("Lỗi khi lấy hình dạng đường đi:", error);
                setRouteCoordinates([]);
            } finally {
                setIsLoading(false);
            }
        };

        setTimeout(() => {
            fetchRouteGeometry();
        }, 300);
    }, [busStops, isMetroLine, cacheKey, mapHasMoved]); // Thêm directionNumber vào dependencies

    // Nếu không có tọa độ hoặc đang tải, hiển thị đường thẳng đơn giản
    if (routeCoordinates.length === 0) {

        return null;
    }

    // Render đường metro với kiểu đặc biệt
    if (isMetroLine) {
        return (
            <>
                {/* Đường chính */}
                <Polyline
                    positions={routeCoordinates}
                    color={lineColor}
                    weight={6}
                    opacity={0.9}
                    lineJoin="round"
                    lineCap="round"
                />

                {/* Hiển thị điểm tròn tại các trạm metro */}
                {busStops.map((stop, index) => (
                    <CircleMarker
                        key={`metro-station-${stop.id || index}`}
                        center={[stop.latitude, stop.longitude]}
                        radius={6}
                        fillOpacity={1}
                        fillColor="white"
                        color={lineColor}
                        weight={3}
                    />
                ))}
            </>
        );
    }

    // Render đường thực tế cho bus
    return (
        <Polyline
            positions={routeCoordinates}
            color={lineColor}
            weight={5}
            opacity={0.8}
            lineJoin="round"
            lineCap="round"
        />
    );
});

// Xử lý sự kiện bản đồ
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

// Nút định vị
const LocateControl = ({ userLocation, onLocate }) => {
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
                    map.flyTo(userLocation, 17);
                    // Thông báo cho component cha rằng người dùng đã nhấp vào nút định vị
                    onLocate && onLocate();
                }
            });

            L.DomEvent.disableClickPropagation(div);
            return div;
        };

        locateControl.addTo(map);
        return () => locateControl.remove();
    }, [map, userLocation, onLocate]);

    return null;
};

// Tọa độ mặc định cho Hồ Chí Minh
const DEFAULT_LOCATION = [10.7769, 106.7009];
const MIN_ZOOM_SHOW_STOPS = 15;

const MapLeaflet = ({ busStops = [], allStops = [], selectedRoute, direction }) => {
    const [userLocation, setUserLocation] = useState(DEFAULT_LOCATION);
    const [accuracy, setAccuracy] = useState(0);
    const [selectedStop, setSelectedStop] = useState(null);
    const [mapBounds, setMapBounds] = useState(null);
    const [zoomLevel, setZoomLevel] = useState(15);
    const [visibleStops, setVisibleStops] = useState([]);
    const [showUserLocation, setShowUserLocation] = useState(false);
    const [shouldFocusUserLocation, setShouldFocusUserLocation] = useState(false);

    // Tạo icon cho trạm dừng
    const busStopIcon = useMemo(() => L.icon({
        iconUrl: require('../../assets/icons/busstop.png'),
        iconSize: [36, 36],
        iconAnchor: [18, 36],
        popupAnchor: [0, -36]
    }), []);

    // Lấy vị trí người dùng
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
        }
    }, []);

    // Xác định danh sách trạm hiển thị
    const stopsToUse = useMemo(() => {
        if (selectedRoute) {
            return busStops?.length > 0 ? busStops : [];
        }
        return allStops?.length > 0 ? allStops : [];
    }, [busStops, allStops, selectedRoute]);

    // Xử lý sự thay đổi của bounds bản đồ
    const handleBoundsChange = useCallback((bounds) => {
        setMapBounds(bounds);
    }, []);

    // Xử lý sự thay đổi của zoom
    const handleZoomChange = useCallback((zoom) => {
        setZoomLevel(zoom);
    }, []);

    // Lọc các trạm hiển thị dựa trên bounds và zoom
    useEffect(() => {
        if (!mapBounds || !stopsToUse?.length) return;

        // Hiển thị tất cả các trạm của tuyến đã chọn
        if (selectedRoute) {
            setVisibleStops(stopsToUse);
            return;
        }

        // Lọc trạm theo bounds và zoom
        const shouldShowAllStops = zoomLevel >= MIN_ZOOM_SHOW_STOPS;
        const filtered = stopsToUse.filter(stop => {
            if (!stop?.latitude || !stop?.longitude) return false;

            // Luôn hiển thị trạm đã chọn
            if (selectedStop?.id === stop.id) return true;

            // Lọc theo bounds bản đồ và zoom
            if (mapBounds.contains([stop.latitude, stop.longitude])) {
                if (shouldShowAllStops) return true;
                if (stop.isMainStop) return true;
            }

            return false;
        });

        setVisibleStops(filtered);
    }, [mapBounds, zoomLevel, selectedRoute, selectedStop, stopsToUse]);

    // Tạo icon vị trí người dùng
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
            "></div>
        `,
        iconSize: [20, 20],
        iconAnchor: [10, 10],
        popupAnchor: [0, -10]
    }), []);

    return (
        <MapContainer
            center={DEFAULT_LOCATION}
            zoom={13}
            style={{ height: "100%", width: "100%" }}
        >
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            />

            {/* Hiển thị đường đi tuyến - Truyền thêm selectedRoute */}
            {selectedRoute && busStops.length > 1 && (
                <RoutePath
                    busStops={busStops}
                    selectedRoute={selectedRoute}
                    direction={direction} // Mặc định chiều đi = 1
                />
            )}
            {showUserLocation && (
                <Marker position={userLocation} icon={userLocationIcon}>
                    <Popup>
                        <div>
                            <strong>Vị trí của bạn</strong>
                            <br />
                            Độ chính xác: {Math.round(accuracy)} mét
                        </div>
                    </Popup>
                </Marker>
            )}


            {/* Hiển thị các trạm dừng */}
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
                            <h4>{stop.name || stop.stopName}</h4>
                            <p>{stop.address}</p>
                        </div>
                    </Popup>
                </Marker>
            ))}            {/* Phần còn lại giữ nguyên */}
            <SetViewToUserLocation
                position={userLocation}
                shouldFocus={shouldFocusUserLocation}
            />
            <SetViewToSelectedStop selectedStop={selectedStop} />
            <SetViewToFirstStop
                busStops={busStops}
                selectedRoute={selectedRoute}
            />
            <MapEventsHandler
                onBoundsChange={handleBoundsChange}
                onZoomChange={handleZoomChange}
            />
            <LocateControl
                userLocation={userLocation}
                onLocate={() => {
                    setShowUserLocation(true);
                    setShouldFocusUserLocation(true);
                }}
            />
        </MapContainer>
    );
};

export default MapLeaflet;