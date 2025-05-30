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
    const getSeverityColor = (severity, type) => {
        // Màu sắc dựa trên loại sự cố
        switch (type) {
            case 'congestion':
                return { color: '#ff0000', fillColor: '#ff3333' }; // Đỏ cho kẹt xe
            case 'accident':
                return { color: '#ff9900', fillColor: '#ffcc00' }; // Cam cho tai nạn
            case 'roadblock':
                return { color: '#3366ff', fillColor: '#6699ff' }; // Xanh dương cho đường bị chặn
            case 'construction':
                return { color: '#9933ff', fillColor: '#b366ff' }; // Tím cho công trình
            default:
                // Nếu không xác định loại, dựa vào severity
                switch (severity) {
                    case 'high':
                        return { color: '#ff0000', fillColor: '#ff3333' };
                    case 'medium':
                        return { color: '#ff9900', fillColor: '#ffcc00' };
                    case 'low':
                        return { color: '#33cc33', fillColor: '#66ff66' };
                    default:
                        return { color: '#3388ff', fillColor: '#3388ff' };
                }
        }
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

    // Component để bắt sự kiện click trên bản đồ
    const MapClickHandler = () => {
        const map = useMapEvents({
            click(e) {
                setClickedLocation(e.latlng);
            },
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
                <h6>Chú thích</h6>
                <div className="legend-item">
                    <div className="legend-color congestion"></div>
                    <span>Kẹt xe</span>
                </div>
                <div className="legend-item">
                    <div className="legend-color accident"></div>
                    <span>Tai nạn</span>
                </div>
                <div className="legend-item">
                    <div className="legend-color roadblock"></div>
                    <span>Đường bị chặn</span>
                </div>
                <div className="legend-item">
                    <div className="legend-color construction"></div>
                    <span>Công trình</span>
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
                            radius={radius}
                            pathOptions={{
                                color: colors.color,
                                fillColor: colors.fillColor,
                                fillOpacity: 0.6,
                                weight: 2
                            }}
                        >
                            <Popup>
                                <div className="traffic-popup">
                                    <h5>{getTypeName(condition.type)}</h5>
                                    <p><strong>Mức độ:</strong> {getSeverityName(condition.severity)}</p>
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