// c:\PTS\PublicTransportSystem\publictransportsystem\src\pages\TrafficAdminPage.jsx
import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Form, Button, Table, Alert, Badge, Modal } from 'react-bootstrap'; import {
    getTrafficConditions,
    addTrafficCondition,
    deleteTrafficCondition,
    updateTrafficCondition
} from '../services/TrafficService';
import './TrafficAdminPage.css';

const TrafficAdminPage = () => {
    // State cho form thêm mới
    const [formData, setFormData] = useState({
        latitude: '',
        longitude: '',
        type: 'congestion',
        severity: 'medium',
        description: ''
    });

    // State cho danh sách tình trạng giao thông
    const [trafficConditions, setTrafficConditions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [alert, setAlert] = useState({ show: false, variant: '', message: '' });

    // State cho modal xác nhận xóa
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [selectedConditionId, setSelectedConditionId] = useState(null);

    // Tải dữ liệu khi component mount
    useEffect(() => {
        loadTrafficConditions();
    }, []);

    // Hàm tải dữ liệu tình trạng giao thông
    const loadTrafficConditions = async () => {
        try {
            setLoading(true);
            const data = await getTrafficConditions();
            setTrafficConditions(data);
            setLoading(false);
        } catch (error) {
            console.error("Lỗi khi tải dữ liệu:", error);
            setAlert({
                show: true,
                variant: 'danger',
                message: 'Có lỗi xảy ra khi tải dữ liệu tình trạng giao thông.'
            });
            setLoading(false);
        }
    };

    // Xử lý thay đổi giá trị form
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevData => ({
            ...prevData,
            [name]: value
        }));
    };

    // Xử lý submit form thêm mới
    const handleSubmit = async (e) => {
        e.preventDefault();

        // Kiểm tra dữ liệu đầu vào
        if (!formData.latitude || !formData.longitude || !formData.description) {
            setAlert({
                show: true,
                variant: 'warning',
                message: 'Vui lòng điền đầy đủ thông tin bắt buộc.'
            });
            return;
        }

        try {
            // Chuyển đổi latitude và longitude thành số
            const trafficData = {
                ...formData,
                latitude: parseFloat(formData.latitude),
                longitude: parseFloat(formData.longitude)
            };

            await addTrafficCondition(trafficData);
            setAlert({
                show: true,
                variant: 'success',
                message: 'Thêm tình trạng giao thông thành công.'
            });

            // Reset form và tải lại dữ liệu
            setFormData({
                latitude: '',
                longitude: '',
                type: 'congestion',
                severity: 'medium',
                description: ''
            });

            loadTrafficConditions();
        } catch (error) {
            console.error("Lỗi khi thêm tình trạng giao thông:", error);
            setAlert({
                show: true,
                variant: 'danger',
                message: 'Có lỗi xảy ra khi thêm tình trạng giao thông.'
            });
        }
    };

    // Xử lý xóa tình trạng giao thông
    const handleDelete = async (id) => {
        setSelectedConditionId(id);
        setShowDeleteModal(true);
    };

    // Xác nhận xóa
    const confirmDelete = async () => {
        try {
            await deleteTrafficCondition(selectedConditionId);
            setAlert({
                show: true,
                variant: 'success',
                message: 'Xóa tình trạng giao thông thành công.'
            });

            loadTrafficConditions();
            setShowDeleteModal(false);
        } catch (error) {
            console.error("Lỗi khi xóa tình trạng giao thông:", error);
            setAlert({
                show: true,
                variant: 'danger',
                message: 'Có lỗi xảy ra khi xóa tình trạng giao thông.'
            });
            setShowDeleteModal(false);
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

    // Lấy màu tương ứng với mức độ nghiêm trọng
    const getSeverityBadgeVariant = (severity) => {
        switch (severity) {
            case 'high': return 'danger';
            case 'medium': return 'warning';
            case 'low': return 'success';
            default: return 'secondary';
        }
    };

    return (
        <Container className="py-4">
            {alert.show && (
                <Alert
                    variant={alert.variant}
                    onClose={() => setAlert({ ...alert, show: false })}
                    dismissible
                >
                    {alert.message}
                </Alert>
            )}

            <Row>
                <Col>
                    <h1 className="mb-4">Quản lý tình trạng giao thông</h1>
                </Col>
            </Row>

            <Row className="mb-5">
                <Col>
                    <div className="admin-form-container">
                        <h3 className="mb-3">Thêm tình trạng giao thông mới</h3>
                        <Form onSubmit={handleSubmit}>
                            <Row>
                                <Col md={6}>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Vĩ độ (Latitude) *</Form.Label>
                                        <Form.Control
                                            type="number"
                                            step="0.000001"
                                            name="latitude"
                                            value={formData.latitude}
                                            onChange={handleInputChange}
                                            placeholder="Ví dụ: 10.762622"
                                            required
                                        />
                                        <Form.Text className="text-muted">
                                            Nhập tọa độ vĩ độ của vị trí.
                                        </Form.Text>
                                    </Form.Group>
                                </Col>
                                <Col md={6}>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Kinh độ (Longitude) *</Form.Label>
                                        <Form.Control
                                            type="number"
                                            step="0.000001"
                                            name="longitude"
                                            value={formData.longitude}
                                            onChange={handleInputChange}
                                            placeholder="Ví dụ: 106.660172"
                                            required
                                        />
                                        <Form.Text className="text-muted">
                                            Nhập tọa độ kinh độ của vị trí.
                                        </Form.Text>
                                    </Form.Group>
                                </Col>
                            </Row>

                            <Row>
                                <Col md={6}>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Loại sự cố</Form.Label>
                                        <Form.Select
                                            name="type"
                                            value={formData.type}
                                            onChange={handleInputChange}
                                        >
                                            <option value="congestion">Kẹt xe</option>
                                            <option value="accident">Tai nạn</option>
                                            <option value="roadblock">Đường bị chặn</option>
                                            <option value="construction">Công trình đang thi công</option>
                                        </Form.Select>
                                    </Form.Group>
                                </Col>
                                <Col md={6}>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Mức độ nghiêm trọng</Form.Label>
                                        <Form.Select
                                            name="severity"
                                            value={formData.severity}
                                            onChange={handleInputChange}
                                        >
                                            <option value="low">Nhẹ</option>
                                            <option value="medium">Trung bình</option>
                                            <option value="high">Nghiêm trọng</option>
                                        </Form.Select>
                                    </Form.Group>
                                </Col>
                            </Row>

                            <Form.Group className="mb-3">
                                <Form.Label>Mô tả chi tiết *</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    name="description"
                                    value={formData.description}
                                    onChange={handleInputChange}
                                    placeholder="Mô tả chi tiết về tình trạng giao thông này..."
                                    required
                                />
                            </Form.Group>

                            <Button variant="primary" type="submit">
                                Thêm tình trạng giao thông
                            </Button>
                        </Form>
                    </div>
                </Col>
            </Row>

            <Row>
                <Col>
                    <h3 className="mb-3">Danh sách tình trạng giao thông hiện tại</h3>

                    {loading ? (
                        <div className="text-center py-4">
                            <div className="spinner-border text-primary" role="status">
                                <span className="visually-hidden">Loading...</span>
                            </div>
                            <p className="mt-2">Đang tải dữ liệu...</p>
                        </div>
                    ) : trafficConditions.length === 0 ? (
                        <Alert variant="info">
                            Không có dữ liệu tình trạng giao thông hiện tại.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table striped bordered hover>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Loại</th>
                                        <th>Mức độ</th>
                                        <th>Mô tả</th>
                                        <th>Vị trí</th>
                                        <th>Thời gian</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {trafficConditions.map(condition => (
                                        <tr key={condition.id}>
                                            <td>{condition.id}</td>
                                            <td>{getConditionTypeName(condition.type)}</td>
                                            <td>
                                                <Badge bg={getSeverityBadgeVariant(condition.severity)}>
                                                    {getSeverityName(condition.severity)}
                                                </Badge>
                                            </td>
                                            <td>{condition.description}</td>
                                            <td>
                                                {condition.latitude}, {condition.longitude}
                                            </td>
                                            <td>
                                                {condition.timestamp?.toLocaleString() || 'N/A'}
                                            </td>
                                            <td>
                                                <Button
                                                    variant="danger"
                                                    size="sm"
                                                    onClick={() => handleDelete(condition.id)}
                                                >
                                                    Xóa
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    )}
                </Col>
            </Row>

            {/* Modal xác nhận xóa */}
            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Xác nhận xóa</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Bạn có chắc chắn muốn xóa tình trạng giao thông này không? Hành động này không thể hoàn tác.
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
                        Hủy
                    </Button>
                    <Button variant="danger" onClick={confirmDelete}>
                        Xóa
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
};

export default TrafficAdminPage;
