import React, { useEffect, useState, useCallback, useMemo } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap, CircleMarker, useMapEvents } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import "./map.css";
import L from "leaflet";

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
        if (position && map) {
            map.setView(position, 15, { animate: true });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [map]); // Chỉ phụ thuộc vào map, không phụ thuộc vào position

    return null;
};

// Component để theo dõi sự kiện map
const MapEventsHandler = ({ onBoundsChange, onZoomChange }) => {
    const map = useMapEvents({
        moveend: () => {
            onBoundsChange(map.getBounds());
        },
        zoomend: () => {
            onZoomChange(map.getZoom());
        }
    });

    // Kích hoạt sự kiện ngay khi component mount
    useEffect(() => {
        if (map) {
            onBoundsChange(map.getBounds());
            onZoomChange(map.getZoom());
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [map]); // Chỉ phụ thuộc vào map, không phụ thuộc vào onBoundsChange và onZoomChange

    return null;
};

// Custom control cho nút định vị
const LocateControl = ({ userLocation }) => {
    const map = useMap();

    useEffect(() => {
        if (!map || !userLocation) return;

        // Tạo một control mới
        const CustomControl = L.Control.extend({
            options: {
                position: 'bottomright' // Vị trí ở góc dưới bên phải
            },

            onAdd: function () {
                const container = L.DomUtil.create('div', 'leaflet-control-locate');
                const button = L.DomUtil.create('button', 'leaflet-control-locate-button', container);

                button.innerHTML = `
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                `;
                button.title = "Trở về vị trí của bạn";

                // Ngăn sự kiện click trên nút gây ra sự kiện click trên map
                L.DomEvent.disableClickPropagation(container);
                L.DomEvent.disableScrollPropagation(container);

                // Xử lý sự kiện click
                L.DomEvent.on(button, 'click', function (e) {
                    L.DomEvent.preventDefault(e);
                    map.setView(userLocation, 15, { animate: true });

                    // Thêm hiệu ứng khi nhấn nút
                    button.classList.add('active');
                    setTimeout(() => {
                        button.classList.remove('active');
                    }, 300);
                });

                return container;
            }
        });

        // Thêm control vào map
        const control = new CustomControl();
        map.addControl(control);

        console.log("Nút Locate đã được thêm vào map"); // Debug để xác nhận

        // Cleanup khi unmount
        return () => {
            map.removeControl(control);
        };
    }, [map, userLocation]);

    return null;
};

// Mẫu dữ liệu trạm bus nếu không có dữ liệu truyền vào
const DEFAULT_BUS_STOPS = [
    { id: 1, name: "Bến xe buýt Sài Gòn", latitude: 10.7596, longitude: 106.6580, address: "Quận 1, TP HCM" },
    { id: 2, name: "Bến xe Miền Đông", latitude: 10.7669, longitude: 106.6977, address: "Bình Thạnh, TP HCM" },
    { id: 3, name: "Bến xe Miền Tây", latitude: 10.7400, longitude: 106.6282, address: "Quận 6, TP HCM" },
    { id: 4, name: "Đại học Khoa học Tự nhiên", latitude: 10.7629, longitude: 106.6822, address: "Quận 5, TP HCM" }
];

const MapLeaflet = ({ busStops = [] }) => {
    const [userLocation, setUserLocation] = useState(null);
    const [setMap] = useState(null);
    const [accuracy, setAccuracy] = useState(0);
    const [nearbyStops, setNearbyStops] = useState([]);
    const [selectedStop, setSelectedStop] = useState(null);
    const [mapBounds, setMapBounds] = useState(null);
    const [zoomLevel, setZoomLevel] = useState(15);
    const [visibleStops, setVisibleStops] = useState([]);

    // Cấu hình hiển thị
    const MIN_ZOOM_SHOW_STOPS = 16; // Mức zoom tối thiểu để hiện trạm



    // Lấy vị trí người dùng
    useEffect(() => {
        if (navigator.geolocation) {
            const watcher = navigator.geolocation.watchPosition(
                (pos) => {
                    const { latitude, longitude, accuracy } = pos.coords;
                    setUserLocation([latitude, longitude]);
                    setAccuracy(accuracy);
                },
                (err) => {
                    console.error("Lỗi lấy vị trí:", err);
                    setUserLocation([10.762622, 106.660172]); // Vị trí mặc định (TP.HCM)
                },
                {
                    enableHighAccuracy: true,
                    timeout: 10000,
                    maximumAge: 0,
                }
            );

            return () => navigator.geolocation.clearWatch(watcher);
        }
    }, []);

    // Memoize stopsToUse để tránh tính toán lại mỗi khi render
    const stopsToUse = useMemo(() => {
        return busStops.length > 0 ? busStops : DEFAULT_BUS_STOPS;
    }, [busStops]);

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
        if (!mapBounds || !userLocation || zoomLevel < MIN_ZOOM_SHOW_STOPS) {
            if (visibleStops.length > 0) {
                setVisibleStops([]);
            }
            return;
        }

        // Lọc trạm nằm trong khung nhìn hiện tại
        const inBoundsStops = stopsToUse.filter(stop =>
            mapBounds.contains(L.latLng(stop.latitude, stop.longitude))
        );

        // Tính khoảng cách từ người dùng đến mỗi trạm
        const stopsWithData = inBoundsStops.map(stop => {
            const distance = L.latLng(userLocation).distanceTo(
                L.latLng([stop.latitude, stop.longitude])
            );
            return { ...stop, distance };
        });

        // Sắp xếp theo khoảng cách
        stopsWithData.sort((a, b) => a.distance - b.distance);

        // Chuẩn bị danh sách trạm gần
        const newNearbyStops = stopsWithData
            .filter(stop => stop.distance <= 500) // 500m
            .map(stop => stop.id);

        // So sánh bằng cách kiểm tra trực tiếp thay vì dùng JSON stringify
        let stopsChanged = visibleStops.length !== stopsWithData.length;
        if (!stopsChanged) {
            for (let i = 0; i < stopsWithData.length; i++) {
                if (stopsWithData[i].id !== visibleStops[i]?.id) {
                    stopsChanged = true;
                    break;
                }
            }
        }

        let nearbyChanged = nearbyStops.length !== newNearbyStops.length;
        if (!nearbyChanged) {
            for (let i = 0; i < newNearbyStops.length; i++) {
                if (!nearbyStops.includes(newNearbyStops[i])) {
                    nearbyChanged = true;
                    break;
                }
            }
        }

        // Cập nhật state khi cần
        if (stopsChanged) {
            setVisibleStops(stopsWithData);
        }

        if (nearbyChanged) {
            setNearbyStops(newNearbyStops);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [mapBounds, zoomLevel, userLocation, stopsToUse, MIN_ZOOM_SHOW_STOPS]);

    // Khi chưa có vị trí, hiển thị loading
    if (!userLocation) {
        return <div className="loading-container">
            <div className="spinner"></div>
            <p>Đang xác định vị trí của bạn...</p>
        </div>;
    }

    return (
        <MapContainer
            center={userLocation}
            zoom={15}
            style={{ height: "100vh", width: "100%" }}
            whenCreated={setMap}
        >
            <TileLayer
                attribution='© <a href="https://www.openstreetmap.org/">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            <SetViewToUserLocation position={userLocation} />
            <MapEventsHandler
                onBoundsChange={handleBoundsChange}
                onZoomChange={handleZoomChange}
            />

            {/* Nút định vị dưới dạng Leaflet Control */}
            <LocateControl userLocation={userLocation} />

            {/* Hiển thị vị trí người dùng là chấm xanh */}
            <CircleMarker
                center={userLocation}
                radius={8}
                pathOptions={{
                    fillColor: '#1e88e5',
                    fillOpacity: 0.8,
                    color: 'white',
                    weight: 2
                }}
            >
                <Popup>
                    Vị trí hiện tại của bạn<br />
                    {userLocation[0].toFixed(6)}, {userLocation[1].toFixed(6)}
                </Popup>
            </CircleMarker>

            {/* Hiển thị vòng tròn độ chính xác */}
            <CircleMarker
                center={userLocation}
                radius={Math.min(accuracy / 2, 100)} // Giới hạn kích thước tối đa
                pathOptions={{
                    fillColor: '#1e88e5',
                    fillOpacity: 0.1,
                    color: '#1e88e5',
                    weight: 1
                }}
            />

            {/* Hiển thị các trạm bus trong tầm nhìn và đủ zoom level */}
            {zoomLevel >= MIN_ZOOM_SHOW_STOPS && visibleStops.map(stop => {
                const isNearby = nearbyStops.includes(stop.id);
                const isSelected = selectedStop && selectedStop.id === stop.id;

                return (
                    <React.Fragment key={stop.id}>
                        {/* Hiển thị vòng tròn highlight cho trạm đang chọn */}
                        {isSelected && (
                            <CircleMarker
                                center={[stop.latitude, stop.longitude]}
                                radius={12}
                                pathOptions={{
                                    color: '#FF4500',
                                    weight: 2,
                                    fillOpacity: 0.2
                                }}
                            />
                        )}

                        {/* Marker trạm bus */}
                        <Marker
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
                                    {stop.distance && (
                                        <p className="distance-tag">
                                            <span role="img" aria-label="Distance">📏</span>
                                            {(stop.distance / 1000).toFixed(2)} km từ vị trí của bạn
                                        </p>
                                    )}
                                    {isNearby && (
                                        <p className="nearby-tag">
                                            <span role="img" aria-label="Near">📍</span>
                                            Gần vị trí của bạn
                                        </p>
                                    )}
                                    <button
                                        className="directions-btn"
                                        onClick={() => {
                                            const url = `https://www.google.com/maps/dir/?api=1&origin=${userLocation[0]},${userLocation[1]}&destination=${stop.latitude},${stop.longitude}&travelmode=walking`;
                                            window.open(url, '_blank');
                                        }}
                                    >
                                        Chỉ đường đến đây
                                    </button>
                                </div>
                            </Popup>
                        </Marker>
                    </React.Fragment>
                );
            })}

            {/* Thông báo số lượng trạm và trạng thái zoom */}
            <div className="map-info">
                {zoomLevel < MIN_ZOOM_SHOW_STOPS ? (
                    <span>Phóng to để xem các trạm xe buýt</span>
                ) : (
                    <span>Hiển thị {visibleStops.length} trạm trong khung nhìn</span>
                )}
            </div>
        </MapContainer>
    );
};

export default MapLeaflet;