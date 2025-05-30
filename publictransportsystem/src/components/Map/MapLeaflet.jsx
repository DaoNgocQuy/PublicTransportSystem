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
const MapClickHandler = ({ selectionMode, onLocationSelect, activeTab }) => {
    const map = useMapEvents({
        dblclick: (e) => {
            // Chỉ xử lý khi đang ở tab tìm đường
            if (activeTab !== 'search') return;

            const { lat, lng } = e.latlng;
            console.log(`Double clicked at: ${lat}, ${lng} in mode: ${selectionMode}`);

            // Tạo object thông tin vị trí
            const locationData = {
                latitude: lat,
                longitude: lng,
                stop_name: 'Vị trí đã chọn',
                address: `[${lat.toFixed(6)}, ${lng.toFixed(6)}]`,
                locationType: selectionMode || 'origin'
            };

            // Thực hiện reverse geocoding để lấy địa chỉ thực
            fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`)
                .then(response => response.json())
                .then(data => {
                    if (data && data.display_name) {
                        const betterLocationData = {
                            ...locationData,
                            stop_name: data.display_name.split(',')[0] || 'Vị trí đã chọn',
                            address: data.display_name || locationData.address
                        };
                        onLocationSelect(betterLocationData);
                    } else {
                        onLocationSelect(locationData);
                    }
                })
                .catch(err => {
                    console.error("Lỗi reverse geocoding:", err);
                    onLocationSelect(locationData);
                });
        }
    });

    return null;
};

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
const JourneySegment = React.memo(({ busStops, selectedRoute, direction, journeySegment }) => {
    const [routeCoordinates, setRouteCoordinates] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const map = useMap();

    // Xác định màu sắc tuyến đường
    const lineColor = useMemo(() => {
        const routeColor = selectedRoute?.color;
        const routeTypeColor = selectedRoute?.routeType?.colorCode;
        const DEFAULT_COLOR = "#4CAF50";
        return routeColor || routeTypeColor || DEFAULT_COLOR;
    }, [selectedRoute]);

    // Xác định loại tuyến (metro hoặc bus)
    const isMetroLine = useMemo(() => {
        const byId = selectedRoute?.routeTypeId === 2;
        const byName = selectedRoute?.name?.toLowerCase().includes('metro') ||
            selectedRoute?.routeName?.toLowerCase().includes('metro');
        return byId || byName;
    }, [selectedRoute]);

    useEffect(() => {
        // Nếu không có thông tin hành trình, không vẽ gì cả
        if (!journeySegment || !busStops || busStops.length < 2) {
            setRouteCoordinates([]);
            return;
        }

        console.log("Journey Segment:", journeySegment);
        console.log("Bus Stops:", busStops);

        let boardIndex = -1;
        let alightIndex = -1;

        // Phương pháp 1: Tìm dựa trên stopOrder
        if (journeySegment.boardStopOrder !== undefined &&
            journeySegment.alightStopOrder !== undefined) {

            boardIndex = busStops.findIndex(stop =>
                stop.stopOrder === journeySegment.boardStopOrder);

            alightIndex = busStops.findIndex(stop =>
                stop.stopOrder === journeySegment.alightStopOrder);

            if (boardIndex !== -1 && alightIndex !== -1) {
                console.log("Tìm thấy trạm theo stopOrder:", { boardIndex, alightIndex });
            }
        }

        // Phương pháp 2: Tìm dựa trên ID
        if (boardIndex === -1 && journeySegment.boardStop?.id) {
            boardIndex = busStops.findIndex(stop =>
                stop.id === journeySegment.boardStop.id ||
                stop.stopId === journeySegment.boardStop.id);
        }

        if (alightIndex === -1 && journeySegment.alightStop?.id) {
            alightIndex = busStops.findIndex(stop =>
                stop.id === journeySegment.alightStop.id ||
                stop.stopId === journeySegment.alightStop.id);
        }

        // Phương pháp 3: Tìm dựa trên tọa độ
        if (boardIndex === -1 && journeySegment.boardStop?.lat) {
            boardIndex = busStops.findIndex(stop =>
                Math.abs(stop.latitude - journeySegment.boardStop.lat) < 0.0001 &&
                Math.abs(stop.longitude - journeySegment.boardStop.lng) < 0.0001);
        }

        if (alightIndex === -1 && journeySegment.alightStop?.lat) {
            alightIndex = busStops.findIndex(stop =>
                Math.abs(stop.latitude - journeySegment.alightStop.lat) < 0.0001 &&
                Math.abs(stop.longitude - journeySegment.alightStop.lng) < 0.0001);
        }

        // Phương pháp 4: Tìm dựa trên tên
        if (boardIndex === -1 && journeySegment.boardStop?.name) {
            boardIndex = busStops.findIndex(stop =>
                (stop.name && stop.name.includes(journeySegment.boardStop.name)) ||
                (stop.stopName && stop.stopName.includes(journeySegment.boardStop.name)));
        }

        if (alightIndex === -1 && journeySegment.alightStop?.name) {
            alightIndex = busStops.findIndex(stop =>
                (stop.name && stop.name.includes(journeySegment.alightStop.name)) ||
                (stop.stopName && stop.stopName.includes(journeySegment.alightStop.name)));
        }

        // Thêm log chi tiết để debug
        console.log(`[boardStopId: ${journeySegment.boardStop?.id}, alightStopId: ${journeySegment.alightStop?.id}, busStopsCount: ${busStops.length}]`);

        // Nếu vẫn không tìm thấy hoặc chỉ tìm thấy một trạm
        if (boardIndex === -1 && alightIndex === -1) {
            console.warn("Không tìm thấy cả hai trạm lên/xuống, sử dụng trạm đầu và cuối");
            boardIndex = 0;
            alightIndex = busStops.length - 1;
        } else if (boardIndex === -1 && alightIndex !== -1) {
            console.warn("Không tìm thấy trạm lên, sử dụng trạm đầu tiên");
            boardIndex = 0;
        } else if (boardIndex !== -1 && alightIndex === -1) {
            console.warn("Không tìm thấy trạm xuống, sử dụng trạm cuối cùng");
            alightIndex = busStops.length - 1;
        }

        // Đảm bảo đi đúng chiều (boardIndex < alightIndex)
        if (boardIndex > alightIndex) {
            console.log("Đảo ngược trạm lên/xuống vì boardIndex > alightIndex");
            [boardIndex, alightIndex] = [alightIndex, boardIndex];
        }

        console.log("Kết quả tìm trạm:", { boardIndex, alightIndex, totalStops: busStops.length });

        // Lấy danh sách các trạm cần đi qua
        const relevantStops = busStops.slice(boardIndex, alightIndex + 1);

        if (relevantStops.length < 2) {
            console.warn("Không đủ trạm để vẽ đường đi", { boardIndex, alightIndex });
            setRouteCoordinates([]);
            return;
        }

        // Vẽ đường thẳng cho metro
        if (isMetroLine) {
            const straightLinePositions = relevantStops.map(
                stop => [stop.latitude, stop.longitude]
            );
            setRouteCoordinates(straightLinePositions);
            return;
        }

        // Vẽ đường thực tế cho bus
        const fetchRouteGeometry = async () => {
            setIsLoading(true);
            try {
                // Xây dựng chuỗi tọa độ
                const coordinates = relevantStops.map(
                    stop => `${stop.longitude},${stop.latitude}`
                ).join(';');

                console.log("Fetching route geometry with coordinates:", coordinates);

                // Sử dụng OSRM API
                const response = await fetch(
                    `https://router.project-osrm.org/route/v1/driving/${coordinates}?overview=full&geometries=geojson`
                );

                const data = await response.json();

                if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
                    const coords = data.routes[0].geometry.coordinates;
                    const latLngs = coords.map(coord => [coord[1], coord[0]]);
                    setRouteCoordinates(latLngs);
                } else {
                    // Fallback sang đường thẳng nếu API thất bại
                    console.log("OSRM API thất bại, sử dụng đường thẳng");
                    const straightLinePositions = relevantStops.map(
                        stop => [stop.latitude, stop.longitude]
                    );
                    setRouteCoordinates(straightLinePositions);
                }
            } catch (error) {
                console.error("Lỗi khi lấy hình dạng đường đi:", error);

                // Fallback sang đường thẳng
                const straightLinePositions = relevantStops.map(
                    stop => [stop.latitude, stop.longitude]
                );
                setRouteCoordinates(straightLinePositions);
            } finally {
                setIsLoading(false);
            }
        };

        // Fetch và vẽ route
        fetchRouteGeometry();

    }, [busStops, journeySegment, isMetroLine]);

    if (routeCoordinates.length === 0) {
        return null;
    }

    // Render đường metro với kiểu đặc biệt
    if (isMetroLine) {
        return (
            <>
                <Polyline
                    positions={routeCoordinates}
                    color={lineColor}
                    weight={6}
                    opacity={0.9}
                    lineJoin="round"
                    lineCap="round"
                />

                {/* Đánh dấu các trạm metro */}
                {routeCoordinates.map((coord, index) => (
                    <CircleMarker
                        key={`metro-station-segment-${index}`}
                        center={coord}
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
const WalkingPath = React.memo(({ origin, destination, type }) => {
    const [walkPath, setWalkPath] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [apiSource, setApiSource] = useState('none'); // Thêm state để theo dõi nguồn API

    const validOrigin = useMemo(() => {
        return Array.isArray(origin) && origin.length === 2 &&
            !isNaN(origin[0]) && !isNaN(origin[1]);
    }, [origin]);

    const validDestination = useMemo(() => {
        return Array.isArray(destination) && destination.length === 2 &&
            !isNaN(destination[0]) && !isNaN(destination[1]);
    }, [destination]);

    // Cache key for storing paths
    const cacheKey = useMemo(() => {
        if (!validOrigin || !validDestination) return null;
        return `walk_${origin[0]},${origin[1]}_${destination[0]},${destination[1]}`;
    }, [origin, destination, validOrigin, validDestination]);

    // Path cache to avoid redundant API calls
    const pathCacheRef = React.useRef({});

    // Path color based on type
    const pathColor = type === 'origin' ? '#4CAF50' :
        type === 'destination' ? '#e91e63' : '#2196F3';

    useEffect(() => {
        if (!validOrigin || !validDestination) {
            console.warn("Invalid coordinates for walking path:", { origin, destination });
            setWalkPath([]);
            return;
        }

        // Use cached path if available
        if (cacheKey && pathCacheRef.current[cacheKey]) {
            console.log(`[WalkingPath] Sử dụng đường đi từ cache cho ${type}`, {
                origin,
                destination,
                pathLength: pathCacheRef.current[cacheKey].length
            });
            setWalkPath(pathCacheRef.current[cacheKey]);
            setApiSource('cache');
            return;
        }

        console.log(`[WalkingPath] Bắt đầu tìm đường đi cho ${type}`, {
            origin,
            destination
        });

        const fetchWalkingPath = async () => {
            setIsLoading(true);
            setError(null);

            try {
                // Sử dụng OSRM API với profile "foot" để lấy đường đi bộ
                const url = `https://router.project-osrm.org/route/v1/foot/${origin[1]},${origin[0]};${destination[1]},${destination[0]}?overview=full&geometries=geojson`;

                console.log(`[WalkingPath] Đang gọi OSRM API cho ${type}...`, url);
                const response = await fetch(url);

                if (!response.ok) {
                    throw new Error(`OSRM API error: ${response.status}`);
                }

                const data = await response.json();

                if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
                    // Lấy tọa độ từ kết quả API và chuyển đổi định dạng
                    const coords = data.routes[0].geometry.coordinates;
                    const latLngs = coords.map(coord => [coord[1], coord[0]]);

                    console.log(`[WalkingPath] OSRM API thành công cho ${type}`, {
                        pointCount: latLngs.length,
                        distance: data.routes[0].distance,
                        duration: data.routes[0].duration
                    });

                    // Lưu vào cache và cập nhật state
                    if (cacheKey) {
                        pathCacheRef.current[cacheKey] = latLngs;
                    }
                    setWalkPath(latLngs);
                    setApiSource('osrm');
                    return;
                } else {
                    // Nếu không lấy được từ OSM, sử dụng backend API
                    console.warn(`[WalkingPath] OSRM API không trả về kết quả hợp lệ cho ${type}, chuyển sang backend API`);
                    await fetchFromBackend();
                }
            } catch (error) {
                console.error(`[WalkingPath] Lỗi khi gọi OSRM API cho ${type}:`, error);
                // Nếu OSRM thất bại, sử dụng backend API
                await fetchFromBackend();
            } finally {
                setIsLoading(false);
            }
        };

        // Phương thức gọi API backend để lấy đường đi bộ
        const fetchFromBackend = async () => {
            try {
                const backendUrl = `/api/routes/walking-path?fromLat=${origin[0]}&fromLng=${origin[1]}&toLat=${destination[0]}&toLng=${destination[1]}`;
                console.log(`[WalkingPath] Đang gọi Backend API cho ${type}...`, backendUrl);

                const response = await fetch(backendUrl);

                if (!response.ok) {
                    throw new Error(`Backend API error: ${response.status}`);
                }

                const data = await response.json();

                if (data && data.path && data.path.length > 0) {
                    // API trả về định dạng [[lat, lng], [lat, lng],...]
                    console.log(`[WalkingPath] Backend API thành công cho ${type}`, {
                        pointCount: data.path.length,
                        firstPoint: data.path[0],
                        lastPoint: data.path[data.path.length - 1]
                    });

                    if (cacheKey) {
                        pathCacheRef.current[cacheKey] = data.path;
                    }
                    setWalkPath(data.path);
                    setApiSource('backend');
                } else {
                    throw new Error("Backend API didn't return valid path");
                }
            } catch (backendError) {
                console.error(`[WalkingPath] Lỗi khi gọi Backend API cho ${type}:`, backendError);
                // Phương án cuối cùng: vẽ đường thẳng đơn giản
                const straightPath = [origin, destination];
                console.log(`[WalkingPath] Sử dụng đường thẳng đơn giản cho ${type}`);

                setWalkPath(straightPath);
                setApiSource('fallback');

                if (cacheKey) {
                    pathCacheRef.current[cacheKey] = straightPath;
                }
                setError("Không thể lấy đường đi chính xác");
            }
        };

        // Thêm delay nhỏ để tránh gọi API quá nhiều lần
        const timeoutId = setTimeout(() => {
            fetchWalkingPath();
        }, 300);

        return () => clearTimeout(timeoutId);
    }, [origin, destination, cacheKey, validOrigin, validDestination, type]);

    // Log khi render để xem đường đi từ nguồn nào
    useEffect(() => {
        if (walkPath.length > 0) {
            console.log(`[WalkingPath] Hiển thị đường đi bộ ${type} từ nguồn: ${apiSource}`, {
                pointCount: walkPath.length
            });
        }
    }, [walkPath, apiSource, type]);

    if (!validOrigin || !validDestination || walkPath.length === 0) {
        return null;
    }

    // Render walking path with dashed line
    return (
        <Polyline
            positions={walkPath}
            color={pathColor}
            weight={4}
            opacity={0.7}
            dashArray="10, 10"
            lineJoin="round"
            lineCap="round"
        >
            {error && <Popup>Không thể tải đường đi chính xác</Popup>}
            <Popup>
                <div>
                    <strong>{type === 'origin' ? 'Đường đi bộ đến trạm' :
                        type === 'destination' ? 'Đường đi bộ từ trạm' : 'Đường đi bộ'}</strong>
                    <p>Nguồn dữ liệu: {apiSource === 'osrm' ? 'OpenStreetMap' :
                        apiSource === 'backend' ? 'Backend API' :
                            apiSource === 'cache' ? 'Cache' : 'Đường thẳng'}</p>
                    <p>Số điểm: {walkPath.length}</p>
                </div>
            </Popup>
        </Polyline>
    );
});

// Tọa độ mặc định cho Hồ Chí Minh
const DEFAULT_LOCATION = [10.7769, 106.7009];
const MIN_ZOOM_SHOW_STOPS = 15;

const MapLeaflet = ({ busStops = [], allStops = [], selectedRoute, direction, activeTab = 'lookup',
    selectionMode = 'origin',
    onLocationSelect,
    onSelectionModeChange,
    ...otherProps }) => {
    const [userLocation, setUserLocation] = useState(DEFAULT_LOCATION);
    const [accuracy, setAccuracy] = useState(0);
    const [selectedStop, setSelectedStop] = useState(null);
    const [mapBounds, setMapBounds] = useState(null);
    const [zoomLevel, setZoomLevel] = useState(15);
    const [visibleStops, setVisibleStops] = useState([]);
    const [showUserLocation, setShowUserLocation] = useState(false);
    const [shouldFocusUserLocation, setShouldFocusUserLocation] = useState(false);
    const [originPoint, setOriginPoint] = useState(null);
    const [destinationPoint, setDestinationPoint] = useState(null);
    const [currentSelectionMode, setCurrentSelectionMode] = useState(selectionMode);
    // Tạo icon cho trạm dừng
    const busStopIcon = useMemo(() => L.icon({
        iconUrl: require('../../assets/icons/busstop.png'),
        iconSize: [36, 36],
        iconAnchor: [18, 36],
        popupAnchor: [0, -36]
    }), []);
    useEffect(() => {
        setCurrentSelectionMode(selectionMode);
    }, [selectionMode]);
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
    const handleLocationSelect = (locationData) => {
        console.log("Selected location:", locationData);

        // Lưu điểm đã chọn theo loại
        if (locationData.locationType === 'origin') {
            setOriginPoint({
                position: [locationData.latitude, locationData.longitude],
                name: locationData.stop_name || "Điểm đi",
                address: locationData.address
            });
        } else {
            setDestinationPoint({
                position: [locationData.latitude, locationData.longitude],
                name: locationData.stop_name || "Điểm đến",
                address: locationData.address
            });
        }

        // Chuyển dữ liệu ra component cha
        if (typeof onLocationSelect === 'function') {
            onLocationSelect(locationData);
        }
    };

    // Xử lý khi người dùng thay đổi chế độ chọn điểm
    const handleSelectionModeChange = (mode) => {
        setCurrentSelectionMode(mode);

        // Thông báo ra bên ngoài
        if (typeof onSelectionModeChange === 'function') {
            onSelectionModeChange(mode);
        }
    };
    const originIcon = useMemo(() => L.icon({
        iconUrl: require('../../assets/icons/blue_pin.png'),    // Sử dụng icon có sẵn trong assets
        iconSize: [36, 36],
        iconAnchor: [18, 36],
        popupAnchor: [0, -36]
    }), []);

    const destinationIcon = useMemo(() => L.icon({
        iconUrl: require('../../assets/icons/red_pin.png'),    // Sử dụng icon có sẵn trong assets
        iconSize: [36, 36],
        iconAnchor: [18, 36],
        popupAnchor: [0, -36]
    }), []);

    useEffect(() => {
        if (activeTab !== 'search') {
            // Reset điểm đi/đến khi chuyển sang tab khác
            setOriginPoint(null);
            setDestinationPoint(null);
        }
    }, [activeTab]);
    // Xác định danh sách trạm hiển thị
    const stopsToUse = useMemo(() => {
        // Trong tab search, không hiển thị trạm nào cho đến khi người dùng chọn một tuyến cụ thể
        if (activeTab === 'search') {
            // Kiểm tra xem người dùng đã chọn một tuyến cụ thể với journeySegment chưa
            if (selectedRoute?.walkingOnly) {
                return [];
            }
            if (!selectedRoute?.journeySegment) {
                return [];
            }
            // Nếu đã chọn tuyến cụ thể, tiếp tục xử lý như trước
            const boardStop = selectedRoute.journeySegment.boardStop;
            const alightStop = selectedRoute.journeySegment.alightStop;

            if (boardStop && alightStop && busStops?.length > 0) {
                // Filter stops between board and alight stops
                return busStops.filter(stop => {
                    // Filter by stopOrder if available
                    if (selectedRoute.journeySegment.boardStopOrder !== undefined &&
                        selectedRoute.journeySegment.alightStopOrder !== undefined &&
                        stop.stopOrder !== undefined) {
                        return stop.stopOrder >= selectedRoute.journeySegment.boardStopOrder &&
                            stop.stopOrder <= selectedRoute.journeySegment.alightStopOrder;
                    }

                    // Filter by ID
                    if ((boardStop.id && stop.id === boardStop.id) ||
                        (alightStop.id && stop.id === alightStop.id)) {
                        return true;
                    }

                    // Filter by coordinates
                    if (Math.abs(stop.latitude - boardStop.lat) < 0.0001 &&
                        Math.abs(stop.longitude - boardStop.lng) < 0.0001) {
                        return true;
                    }

                    if (Math.abs(stop.latitude - alightStop.lat) < 0.0001 &&
                        Math.abs(stop.longitude - alightStop.lng) < 0.0001) {
                        return true;
                    }

                    // Filter by name
                    if ((boardStop.name && stop.name && stop.name.includes(boardStop.name)) ||
                        (alightStop.name && stop.name && stop.name.includes(alightStop.name))) {
                        return true;
                    }

                    return false;
                });
            }

            if (boardStop && alightStop) {
                // Tạo mảng chỉ chứa 2 trạm lên và xuống
                return [
                    {
                        id: 'board-stop',
                        latitude: boardStop.lat,
                        longitude: boardStop.lng,
                        name: boardStop.name,
                        address: boardStop.address || ''
                    },
                    {
                        id: 'alight-stop',
                        latitude: alightStop.lat,
                        longitude: alightStop.lng,
                        name: alightStop.name,
                        address: alightStop.address || ''
                    }
                ];
            }

            return [];
        }

        // Trong tab lookup hoặc những tab khác
        if (selectedRoute) {
            return busStops?.length > 0 ? busStops : [];
        }
        return allStops?.length > 0 ? allStops : [];
    }, [busStops, allStops, selectedRoute, activeTab]);

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

        // Hiển thị tất cả các trạm của tuyến đã chọn khi ở tab search
        if (activeTab === 'search' && selectedRoute?.journeySegment) {
            // Đã lọc trong stopsToUse, nên ở đây chỉ cần kiểm tra nếu trạm nằm trong viewport
            const filtered = stopsToUse.filter(stop => {
                // Luôn hiển thị trạm lên và xuống xe
                const isBoardStop = selectedRoute.journeySegment.boardStop &&
                    (stop.id === 'board-stop' ||
                        Math.abs(stop.latitude - selectedRoute.journeySegment.boardStop.lat) < 0.0001 &&
                        Math.abs(stop.longitude - selectedRoute.journeySegment.boardStop.lng) < 0.0001);

                const isAlightStop = selectedRoute.journeySegment.alightStop &&
                    (stop.id === 'alight-stop' ||
                        Math.abs(stop.latitude - selectedRoute.journeySegment.alightStop.lat) < 0.0001 &&
                        Math.abs(stop.longitude - selectedRoute.journeySegment.alightStop.lng) < 0.0001);

                // Luôn hiển thị trạm lên và xuống, bỏ qua bounds
                if (isBoardStop || isAlightStop) {
                    return true;
                }

                // Các trạm khác chỉ hiển thị nếu nằm trong viewport
                return mapBounds.contains([stop.latitude, stop.longitude]);
            });

            setVisibleStops(filtered);
            return;
        }

        // Hiển thị các trạm trong tuyến đã chọn (tab lookup)
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
    }, [mapBounds, zoomLevel, selectedRoute, selectedStop, stopsToUse, activeTab]);



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
            {/* Xử lý double-click */}
            {activeTab === 'search' && (
                <MapClickHandler
                    selectionMode={currentSelectionMode}
                    onLocationSelect={handleLocationSelect}
                    activeTab={activeTab}
                />
            )}

            {/* Hiển thị điểm đi */}
            {activeTab === 'search' && originPoint && (
                <Marker
                    position={originPoint.position}
                    icon={originIcon}
                >
                    <Popup>
                        <div>
                            <strong>Điểm đi</strong>
                            <p>{originPoint.name}</p>
                            <p style={{ fontSize: '12px' }}>{originPoint.address}</p>
                        </div>
                    </Popup>
                </Marker>
            )}

            {/* Hiển thị điểm đến */}
            {activeTab === 'search' && destinationPoint && (
                <Marker
                    position={destinationPoint.position}
                    icon={destinationIcon}
                >
                    <Popup>
                        <div>
                            <strong>Điểm đến</strong>
                            <p>{destinationPoint.name}</p>
                            <p style={{ fontSize: '12px' }}>{destinationPoint.address}</p>
                        </div>
                    </Popup>
                </Marker>
            )}

            {/* Hiển thị đường đi bộ khi chọn lộ trình đi bộ */}
            {activeTab === 'search' && selectedRoute?.walkingOnly && originPoint && destinationPoint && (
                <WalkingPath
                    origin={originPoint.position}
                    destination={destinationPoint.position}
                    type="direct"
                />
            )}

            {/* Đánh dấu trạm lên xuống xe khi đã chọn phương án đi cụ thể */}
            {activeTab === 'search' && selectedRoute?.journeySegment && !selectedRoute.walkingOnly && (
                <>
                    {/* Trạm lên xe */}
                    {selectedRoute.journeySegment.boardStop && (
                        <CircleMarker
                            center={[
                                selectedRoute.journeySegment.boardStop.lat,
                                selectedRoute.journeySegment.boardStop.lng
                            ]}
                            radius={10}
                            pathOptions={{
                                color: '#4CAF50',
                                fillColor: '#4CAF50',
                                fillOpacity: 0.7,
                                weight: 2
                            }}
                        >
                            <Popup>
                                <div>
                                    <strong>Trạm lên xe</strong>
                                    <p>{selectedRoute.journeySegment.boardStop.name}</p>
                                </div>
                            </Popup>
                        </CircleMarker>
                    )}

                    {/* Trạm xuống xe */}
                    {selectedRoute.journeySegment.alightStop && (
                        <CircleMarker
                            center={[
                                selectedRoute.journeySegment.alightStop.lat,
                                selectedRoute.journeySegment.alightStop.lng
                            ]}
                            radius={10}
                            pathOptions={{
                                color: '#e91e63',
                                fillColor: '#e91e63',
                                fillOpacity: 0.7,
                                weight: 2
                            }}
                        >
                            <Popup>
                                <div>
                                    <strong>Trạm xuống xe</strong>
                                    <p>{selectedRoute.journeySegment.alightStop.name}</p>
                                </div>
                            </Popup>
                        </CircleMarker>
                    )}
                </>
            )}

            {/* Hiển thị đường đi tuyến - Chỉ khi đã chọn phương án di chuyển cụ thể */}
            {(activeTab === 'lookup' && selectedRoute && busStops.length > 1) && (
                <RoutePath
                    busStops={busStops}
                    selectedRoute={selectedRoute}
                    direction={direction}
                />
            )}

            {(activeTab === 'search' && selectedRoute?.journeySegment && busStops.length > 1) && (
                <JourneySegment
                    busStops={busStops}
                    selectedRoute={selectedRoute}
                    direction={direction}
                    journeySegment={selectedRoute.journeySegment}
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

            {/* Hiển thị đường đi bộ khi đã chọn phương án di chuyển cụ thể */}
            {activeTab === 'search' && selectedRoute?.journeySegment && !selectedRoute.walkingOnly && (
                <>
                    {/* Đường đi bộ từ điểm xuất phát đến trạm lên xe */}
                    {originPoint && selectedRoute.journeySegment.boardStop && (
                        <WalkingPath
                            origin={originPoint.position}
                            destination={[
                                selectedRoute.journeySegment.boardStop.lat,
                                selectedRoute.journeySegment.boardStop.lng
                            ]}
                            type="origin"
                        />
                    )}

                    {/* Đường đi bộ từ trạm xuống xe đến điểm đến */}
                    {destinationPoint && selectedRoute.journeySegment.alightStop && (
                        <WalkingPath
                            origin={[
                                selectedRoute.journeySegment.alightStop.lat,
                                selectedRoute.journeySegment.alightStop.lng
                            ]}
                            destination={destinationPoint.position}
                            type="destination"
                        />
                    )}
                </>
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
            ))}

            <SetViewToUserLocation
                position={userLocation}
                shouldFocus={shouldFocusUserLocation}
            />
            <SetViewToSelectedStop selectedStop={selectedStop} />

            {/* Chỉ tự động focus vào trạm đầu tiên trong tab tra cứu */}
            {activeTab === 'lookup' && (
                <SetViewToFirstStop
                    busStops={busStops}
                    selectedRoute={selectedRoute}
                />
            )}

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