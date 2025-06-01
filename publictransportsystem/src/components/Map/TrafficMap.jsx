import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Circle, Popup, useMapEvents, useMap } from 'react-leaflet';
import { subscribeToTrafficConditions } from '../../services/TrafficService';
import { Button, Modal } from 'react-bootstrap';
import TrafficReportForm from '../Traffic/TrafficReportForm';
import './TrafficMap.css';

const TrafficMap = () => {
    const [trafficConditions, setTrafficConditions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showReportForm, setShowReportForm] = useState(false);
    const [clickedLocation, setClickedLocation] = useState(null);

    // Mặc định hiển thị khu vực HCM
    const DEFAULT_LOCATION = [10.762622, 106.660172];


    const formatTimestamp = (timestamp) => {
        if (!timestamp) return 'N/A';

        // Kiểm tra nếu timestamp là Firestore Timestamp (có phương thức toDate())
        if (timestamp.toDate && typeof timestamp.toDate === 'function') {
            return timestamp.toDate().toLocaleString();
        }

        // Nếu timestamp đã là Date object
        if (timestamp instanceof Date) {
            return timestamp.toLocaleString();
        }

        // Nếu timestamp là seconds (số)
        if (typeof timestamp === 'number') {
            return new Date(timestamp * 1000).toLocaleString();
        }

        // Nếu timestamp là object với seconds và nanoseconds (định dạng Firestore)
        if (timestamp.seconds) {
            return new Date(timestamp.seconds * 1000).toLocaleString();
        }

        // Trường hợp khác, chuyển về string
        return String(timestamp);
    };
    // Lấy màu dựa trên severity
    const getSeverityColor = (severity) => {
        // Màu sắc dựa chỉ trên mức độ nghiêm trọng
        switch (severity) {
            case 'high':
                return {
                    color: '#dc3545',         // Màu đỏ cho nghiêm trọng
                    fillColor: '#dc3545',
                    fillOpacity: 0.8,
                    weight: 3
                };
            case 'medium':
                return {
                    color: '#fd7e14',         // Màu cam cho trung bình
                    fillColor: '#fd7e14',
                    fillOpacity: 0.6,
                    weight: 2
                };
            case 'low':
                return {
                    color: '#28a745',         // Màu xanh lá cho nhẹ
                    fillColor: '#28a745',
                    fillOpacity: 0.4,
                    weight: 1.5
                };
            default:
                return {
                    color: '#6c757d',         // Màu xám cho không xác định
                    fillColor: '#6c757d',
                    fillOpacity: 0.5,
                    weight: 2
                };
        }
    };
    const adjustColorLightness = (hex, percent) => {
        // Convert hex to RGB
        let r = parseInt(hex.slice(1, 3), 16);
        let g = parseInt(hex.slice(3, 5), 16);
        let b = parseInt(hex.slice(5, 7), 16);

        // Make lighter by increasing RGB values
        r = Math.min(255, r + Math.floor(percent * 2.55));
        g = Math.min(255, g + Math.floor(percent * 2.55));
        b = Math.min(255, b + Math.floor(percent * 2.55));

        // Convert back to hex
        return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
    };
    // Lấy kích thước dựa trên loại và severity
    const getCircleSize = (type, severity) => {

        switch (severity) {
            case 'high':
                return 80;
            case 'medium':
                return 60;
            case 'low':
                return 30;
            default:
                return 40;
        }
    };

    const MapClickHandler = () => {
        const map = useMapEvents({
            dblclick: (e) => {
                setClickedLocation({
                    lat: e.latlng.lat,
                    lng: e.latlng.lng
                });
                setShowReportForm(true);
            }
        });
        return null;
    };

    useEffect(() => {
        // Đăng ký theo dõi dữ liệu tình trạng giao thông
        const unsubscribe = subscribeToTrafficConditions((data) => {
            setTrafficConditions(data);
            setLoading(false);
        });

        // Hủy đăng ký khi component unmount
        return () => {
            unsubscribe();
        };
    }, []);

    // Xử lý khi người dùng gửi báo cáo thành công
    const handleReportSubmitted = () => {
        setShowReportForm(false);
        // Dữ liệu sẽ tự động được cập nhật nhờ subscribeToTrafficConditions
    };

    // Lấy tên loại sự cố để hiển thị
    const getTypeName = (type) => {
        switch (type) {
            case 'congestion':
                return 'Kẹt xe';
            case 'accident':
                return 'Tai nạn';
            case 'roadblock':
                return 'Đường bị chặn';
            case 'construction':
                return 'Công trình';
            default:
                return 'Sự cố';
        }
    };

    // Lấy tên mức độ nghiêm trọng để hiển thị
    const getSeverityName = (severity) => {
        switch (severity) {
            case 'high':
                return 'Nghiêm trọng';
            case 'medium':
                return 'Trung bình';
            case 'low':
                return 'Nhẹ';
            default:
                return 'Không xác định';
        }
    };

    return (
        <div className="traffic-map-container">
            <div className="map-legend">
                <div className="severity-levels">
                    <h6>Mức độ</h6>
                    <div className="legend-item">
                        <span className="severity-dot high-severity"></span>
                        <span>Nghiêm trọng</span>
                    </div>
                    <div className="legend-item">
                        <span className="severity-dot medium-severity"></span>
                        <span>Trung bình</span>
                    </div>
                    <div className="legend-item">
                        <span className="severity-dot low-severity"></span>
                        <span>Nhẹ</span>
                    </div>
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

                {/* Hiển thị tình trạng giao thông bằng CircleMarker */}
                {trafficConditions.map((condition) => {
                    const colors = getSeverityColor(condition.severity, condition.type);
                    const radius = getCircleSize(condition.type, condition.severity);

                    return (
                        <Circle
                            key={condition.id}
                            center={[condition.latitude, condition.longitude]}
                            radius={getCircleSize(condition.type, condition.severity)}
                            pathOptions={{
                                color: colors.color,
                                fillColor: colors.fillColor,
                                fillOpacity: colors.fillOpacity,
                                weight: colors.weight
                            }}
                        >
                            <Popup>
                                <div className="traffic-popup">
                                    <h5>{getTypeName(condition.type)}</h5>
                                    <p>
                                        <strong>Mức độ:</strong>{' '}
                                        <span className={`severity-badge ${condition.severity}`}>
                                            {getSeverityName(condition.severity)}
                                        </span>
                                    </p>
                                    <p><strong>Mô tả:</strong> {condition.description}</p>
                                    {condition.imageUrl && (
                                        <div className="traffic-image">
                                            <img src={condition.imageUrl} alt="Traffic condition" />
                                        </div>
                                    )}
                                    <p><small>Cập nhật: {formatTimestamp(condition.timestamp)}</small></p>
                                </div>
                            </Popup>
                        </Circle>
                    );
                })}
            </MapContainer>

            {/* Nút báo cáo */}
            <Button
                className="report-button"
                onClick={() => setShowReportForm(true)}
                variant="primary"
            >
                <i className="bi bi-plus-circle"></i> Báo cáo tình trạng
            </Button>

            {/* Modal form báo cáo */}
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
                        userLocation={clickedLocation}
                        onReportSubmitted={handleReportSubmitted}
                    />
                </Modal.Body>
            </Modal>

            {loading && (
                <div className="loading-overlay">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Đang tải...</span>
                    </div>
                </div>
            )}

            {!loading && trafficConditions.length === 0 && (
                <div className="no-data-message">
                    <p>Không có thông tin tình trạng giao thông nào.</p>
                </div>
            )}
        </div>
    );
};

export default TrafficMap;