// c:\PTS\PublicTransportSystem\publictransportsystem\src\pages\TrafficMapPage.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col } from 'react-bootstrap';
import TrafficMap from '../components/Map/TrafficMap';
import '../components/Map/TrafficMap.css'; // Sửa đường dẫn
import './TrafficMapPage.css';

const TrafficMapPage = () => {
    return (
        <>
            <Container className="py-4">                <Row className="mb-3">
                <Col md={8}>
                    <h1>Tình trạng giao thông</h1>
                    <p className="lead">
                        Xem thông tin kẹt xe và các sự cố giao thông theo thời gian thực.
                    </p>
                </Col>

            </Row>
                <Row>
                    <Col>
                        <TrafficMap />
                    </Col>
                </Row>
                <Row className="mt-4">
                    <Col md={6}>
                        <div className="info-box">
                            <h3>Thông tin hiển thị</h3>
                            <ul>
                                <li><strong>Kẹt xe:</strong> Hiển thị các khu vực đang xảy ra tình trạng kẹt xe</li>
                                <li><strong>Tai nạn:</strong> Vị trí xảy ra tai nạn giao thông</li>
                                <li><strong>Đường bị chặn:</strong> Các đoạn đường đang bị chặn</li>
                                <li><strong>Công trình:</strong> Các khu vực có công trình đang thi công</li>
                            </ul>
                            <p>Nhấp vào các điểm trên bản đồ để xem thông tin chi tiết</p>
                        </div>
                    </Col>
                    <Col md={6}>
                        <div className="info-box">
                            <h3>Chú thích mức độ</h3>
                            <div className="severity-info">
                                <div className="severity-item high">
                                    <span className="dot"></span>
                                    <span><strong>Nghiêm trọng:</strong> Kẹt xe hoặc sự cố gây ảnh hưởng lớn đến giao thông</span>
                                </div>
                                <div className="severity-item medium">
                                    <span className="dot"></span>
                                    <span><strong>Trung bình:</strong> Giao thông chậm, di chuyển khó khăn</span>
                                </div>
                                <div className="severity-item low">
                                    <span className="dot"></span>
                                    <span><strong>Nhẹ:</strong> Có ảnh hưởng nhỏ đến lưu thông</span>
                                </div>
                            </div>
                        </div>
                    </Col>
                </Row>
            </Container>
        </>
    );
};

export default TrafficMapPage;