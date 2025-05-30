// c:\PTS\PublicTransportSystem\publictransportsystem\src\components\Map\TrafficMap.jsx
import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, CircleMarker } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import { subscribeToTrafficConditions } from '../../services/TrafficService';
import './TrafficMap.css';
import { Button, Modal } from 'react-bootstrap';
import TrafficReportForm from '../Traffic/TrafficReportForm';
import { useMapEvents } from 'react-leaflet/hooks';
// Khắc phục vấn đề với icon mặc định
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
    iconUrl: require("leaflet/dist/images/marker-icon.png"),
    shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
});

// Custom icon cho điểm kẹt xe
const trafficCongestionIcon = new L.Icon({
    iconUrl: require('../../assets/icons/red_pin.png'),
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
});

// Custom icon cho tai nạn
const accidentIcon = new L.Icon({
    iconUrl: require('../../assets/icons/blue_pin.png'),
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
});

const TrafficMap = () => {
    const [trafficConditions, setTrafficConditions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showReportForm, setShowReportForm] = useState(false);
    const [userLocation, setUserLocation] = useState(null);
    const [clickedLocation, setClickedLocation] = useState(null);
    useEffect(() => {
        // Đăng ký lắng nghe tình trạng giao thông theo thời gian thực
        const unsubscribe = subscribeToTrafficConditions((conditions) => {
            setTrafficConditions(conditions);
            setLoading(false);
        });

        // Hủy đăng ký khi component unmount
        return () => {
            unsubscribe();
        };
    }, []);
    const MapClickHandler = () => {
        const map = useMapEvents({
            click(e) {
                setClickedLocation(e.latlng);
            },
        });
        return null;
    };
    const handleReportSubmitted = () => {
        setShowReportForm(false);
        // Có thể thêm action refresh dữ liệu ở đây nếu cần
    };
    // Chọn icon phù hợp dựa trên loại sự cố
    const getTrafficIcon = (type) => {
        switch (type) {
            case 'accident':
                return accidentIcon;
            case 'construction':
            case 'roadblock':
            case 'congestion':
            default:
                return trafficCongestionIcon;
        }
    };

    // Chọn màu hiển thị dựa trên mức độ nghiêm trọng
    const getSeverityColor = (severity) => {
        switch (severity) {
            case 'high':
                return '#d32f2f'; // Đỏ đậm
            case 'medium':
                return '#ff9800'; // Cam
            case 'low':
                return '#ffc107'; // Vàng
            default:
                return '#d32f2f'; // Mặc định là đỏ
        }
    };

    // Format tên loại sự cố
    const getConditionTypeName = (type) => {
        switch (type) {
            case 'congestion': return 'Kẹt xe';
            case 'accident': return 'Tai nạn';
            case 'roadblock': return 'Đường bị chặn';
            case 'construction': return 'Công trình đang thi công';
            default: return 'Sự cố giao thông';
        }
    };

    // Format mức độ nghiêm trọng
    const getSeverityName = (severity) => {
        switch (severity) {
            case 'low': return 'Nhẹ';
            case 'medium': return 'Trung bình';
            case 'high': return 'Nghiêm trọng';
            default: return 'Không xác định';
        }
    };

    // Tọa độ mặc định cho TP.HCM
    const DEFAULT_LOCATION = [10.762622, 106.660172];

    return (
        <div className="traffic-map-container">
            <div className="map-legend">
                <div className="legend-item">
                    <div className="legend-color high"></div>
                    <span>Nghiêm trọng</span>
                </div>
                <div className="legend-item">
                    <div className="legend-color medium"></div>
                    <span>Trung bình</span>
                </div>
                <div className="legend-item">
                    <div className="legend-color low"></div>
                    <span>Nhẹ</span>
                </div>
            </div>

            <MapContainer
                center={DEFAULT_LOCATION}
                zoom={13}
                style={{ height: '500px', width: '100%' }}
            >
                <MapClickHandler />
                <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                />

                {/* Hiển thị các điểm kẹt xe và sự cố giao thông */}
                {trafficConditions.map((condition) => (
                    <React.Fragment key={condition.id}>
                        {/* Vòng tròn hiển thị phạm vi ảnh hưởng */}
                        <CircleMarker
                            center={[condition.latitude, condition.longitude]}
                            radius={30 + (condition.severity === 'high' ? 20 : condition.severity === 'medium' ? 10 : 0)}
                            pathOptions={{
                                color: getSeverityColor(condition.severity),
                                fillColor: getSeverityColor(condition.severity),
                                fillOpacity: 0.3,
                                weight: 2
                            }}
                        />

                        {/* Marker chính thể hiện vị trí sự cố */}
                        <Marker
                            position={[condition.latitude, condition.longitude]}
                            icon={getTrafficIcon(condition.type)}
                        >
                            <Popup className="traffic-popup">
                                <div>
                                    <h4>{getConditionTypeName(condition.type)}</h4>
                                    <p><strong>Mức độ:</strong> {getSeverityName(condition.severity)}</p>
                                    <p>{condition.description}</p>
                                    <p className="timestamp">
                                        <small>Cập nhật: {condition.timestamp?.toLocaleString()}</small>
                                    </p>
                                    {condition.imageUrl && (
                                        <img
                                            src={condition.imageUrl}
                                            alt="Tình trạng giao thông"
                                            style={{ maxWidth: '200px', maxHeight: '150px' }}
                                        />
                                    )}
                                </div>
                            </Popup>
                        </Marker>
                    </React.Fragment>
                ))}
            </MapContainer>
            <Button
                className="report-button"
                onClick={() => {
                    setUserLocation(clickedLocation);
                    setShowReportForm(true);
                }}
                variant="primary"
            >
                <i className="bi bi-plus-circle"></i> Báo cáo kẹt xe
            </Button>
            <Modal
                show={showReportForm}
                onHide={() => setShowReportForm(false)}
                size="lg"
                centered
            >
                <Modal.Header closeButton>
                    <Modal.Title>Báo cáo tình trạng giao thông</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <TrafficReportForm
                        userLocation={userLocation}
                        onReportSubmitted={handleReportSubmitted}
                    />
                </Modal.Body>
            </Modal>
            {loading ? (
                <div className="loading-indicator">Đang tải dữ liệu tình trạng giao thông...</div>
            ) : trafficConditions.length === 0 ? (
                <div className="no-data">Không có dữ liệu về tình trạng kẹt xe hoặc sự cố giao thông hiện tại.</div>
            ) : (
                <div className="traffic-data-info">
                    Hiển thị {trafficConditions.length} điểm giao thông hiện tại.
                </div>
            )}
        </div>
    );
};

export default TrafficMap;