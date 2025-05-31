import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Form, Button, Table, Alert, Badge, Modal } from 'react-bootstrap';
import {
    getTrafficConditions,
    getPendingTrafficConditions,
    addTrafficCondition,
    deleteTrafficCondition,
    updateTrafficCondition,
    approveTrafficCondition,
    rejectTrafficCondition
} from '../services/TrafficService';
import './TrafficAdminPage.css';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const TrafficAdminPage = () => {
    // State cho form thêm mới
    const [formData, setFormData] = useState({
        latitude: '',
        longitude: '',
        type: 'congestion',
        severity: 'medium',
        description: ''
    });

    // State cho danh sách tình trạng giao thông đã duyệt
    const [trafficConditions, setTrafficConditions] = useState([]);
    // State cho danh sách tình trạng giao thông chờ duyệt
    const [pendingConditions, setPendingConditions] = useState([]);
    
    const [loading, setLoading] = useState(true);
    const [pendingLoading, setPendingLoading] = useState(true);
    const [alert, setAlert] = useState({ show: false, variant: '', message: '' });
    const [position, setPosition] = useState(null);

    // State cho modal xác nhận xóa
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [selectedConditionId, setSelectedConditionId] = useState(null);
    const defaultPosition = [10.762622, 106.660172];
    const [showEditModal, setShowEditModal] = useState(false);
    const [editPosition, setEditPosition] = useState(null);
    const [editFormData, setEditFormData] = useState({
        id: '',
        latitude: '',
        longitude: '',
        type: 'congestion',
        severity: 'medium',
        description: ''
    });

    // Thêm hàm xử lý khi nhấn nút Sửa
    const handleEdit = (condition) => {
        setEditFormData({
            id: condition.id,
            latitude: condition.latitude,
            longitude: condition.longitude,
            type: condition.type,
            severity: condition.severity,
            description: condition.description
        });
        setShowEditModal(true);
    };

    // Hàm xử lý thay đổi giá trị trong form sửa
    const handleEditInputChange = (e) => {
        const { name, value } = e.target;
        setEditFormData(prevData => ({
            ...prevData,
            [name]: value
        }));
    };

    // Hàm xử lý submit form sửa
    const handleEditSubmit = async (e) => {
        e.preventDefault();

        try {
            // Chuyển đổi latitude và longitude thành số
            const trafficData = {
                ...editFormData,
                latitude: parseFloat(editFormData.latitude),
                longitude: parseFloat(editFormData.longitude)
            };

            // Loại bỏ id khỏi dữ liệu cập nhật
            const { id, ...dataToUpdate } = trafficData;

            await updateTrafficCondition(id, dataToUpdate);
            setAlert({
                show: true,
                variant: 'success',
                message: 'Cập nhật tình trạng giao thông thành công.'
            });

            // Đóng modal và tải lại dữ liệu
            setShowEditModal(false);
            loadTrafficConditions();
        } catch (error) {
            console.error("Lỗi khi cập nhật tình trạng giao thông:", error);
            setAlert({
                show: true,
                variant: 'danger',
                message: 'Có lỗi xảy ra khi cập nhật tình trạng giao thông.'
            });
        }
    };

    useEffect(() => {
        if (showEditModal && editFormData.latitude && editFormData.longitude) {
            setEditPosition({
                lat: parseFloat(editFormData.latitude),
                lng: parseFloat(editFormData.longitude)
            });
        }
    }, [showEditModal, editFormData.latitude, editFormData.longitude]);

    useEffect(() => {
        if (editPosition) {
            setEditFormData(prevData => ({
                ...prevData,
                latitude: editPosition.lat.toFixed(6),
                longitude: editPosition.lng.toFixed(6)
            }));
        }
    }, [editPosition]);

    const EditLocationMarker = ({ position, setPosition }) => {
        const map = useMapEvents({
            dblclick(e) {
                const { lat, lng } = e.latlng;
                setPosition({ lat, lng });
                map.flyTo(e.latlng, map.getZoom());
            },
        });

        return position ? (
            <Marker
                position={position}
                icon={new L.Icon({
                    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
                    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                    iconSize: [25, 41],
                    iconAnchor: [12, 41],
                    popupAnchor: [1, -34],
                    shadowSize: [41, 41]
                })}
            />
        ) : null;
    };

    // Tải dữ liệu khi component mount
    useEffect(() => {
        loadTrafficConditions();
        loadPendingConditions();
    }, []);
    
    const LocationMarker = ({ position, setPosition }) => {
        const map = useMapEvents({
            dblclick(e) {
                const { lat, lng } = e.latlng;
                setPosition({ lat, lng });
                map.flyTo(e.latlng, map.getZoom());
            },
        });

        return position ? (
            <Marker
                position={position}
                icon={new L.Icon({
                    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
                    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                    iconSize: [25, 41],
                    iconAnchor: [12, 41],
                    popupAnchor: [1, -34],
                    shadowSize: [41, 41]
                })}
            />
        ) : null;
    };
    // cập nhật form khi position thay đổi

    useEffect(() => {
        if (position) {
            setFormData(prevData => ({
                ...prevData,
                latitude: position.lat.toFixed(6),
                longitude: position.lng.toFixed(6)
            }));
        }
    }, [position]);
    
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

    // Hàm tải dữ liệu báo cáo chờ duyệt
    const loadPendingConditions = async () => {
        try {
            setPendingLoading(true);
            const data = await getPendingTrafficConditions();
            setPendingConditions(data);
            setPendingLoading(false);
        } catch (error) {
            console.error("Lỗi khi tải dữ liệu chờ duyệt:", error);
            setAlert({
                show: true,
                variant: 'danger',
                message: 'Có lỗi xảy ra khi tải dữ liệu báo cáo chờ duyệt.'
            });
            setPendingLoading(false);
        }
    };

    // Hàm duyệt báo cáo
    const handleApprove = async (id) => {
        try {
            await approveTrafficCondition(id);
            setAlert({
                show: true,
                variant: 'success',
                message: 'Đã duyệt báo cáo thành công.'
            });
            // Tải lại cả hai danh sách
            loadPendingConditions();
            loadTrafficConditions();
        } catch (error) {
            console.error("Lỗi khi duyệt báo cáo:", error);
            setAlert({
                show: true,
                variant: 'danger',
                message: 'Có lỗi xảy ra khi duyệt báo cáo.'
            });
        }
    };

    // Hàm từ chối báo cáo
    const handleReject = async (id) => {
        try {
            await rejectTrafficCondition(id);
            setAlert({
                show: true,
                variant: 'success',
                message: 'Đã từ chối báo cáo thành công.'
            });
            // Tải lại danh sách báo cáo chờ duyệt
            loadPendingConditions();
        } catch (error) {
            console.error("Lỗi khi từ chối báo cáo:", error);
            setAlert({
                show: true,
                variant: 'danger',
                message: 'Có lỗi xảy ra khi từ chối báo cáo.'
            });
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
            setPosition(null);

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

            {/* Phần báo cáo chờ duyệt */}
            {pendingConditions.length > 0 && (
                <Row className="mb-4">
                    <Col>
                        <div className="pending-reports-container">
                            <h3 className="mb-3">
                                Báo cáo chờ duyệt 
                                <Badge bg="danger" className="ms-2">{pendingConditions.length}</Badge>
                            </h3>
                            
                            {pendingLoading ? (
                                <div className="text-center py-4">
                                    <div className="spinner-border text-primary" role="status">
                                        <span className="visually-hidden">Loading...</span>
                                    </div>
                                    <p className="mt-2">Đang tải dữ liệu...</p>
                                </div>
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
                                                <th>Hình ảnh</th>
                                                <th>Thời gian</th>
                                                <th>Người báo cáo</th>
                                                <th>Thao tác</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {pendingConditions.map(condition => (
                                                <tr key={condition.id}>
                                                    <td>{condition.id.substring(0, 8)}...</td>
                                                    <td>{getConditionTypeName(condition.type)}</td>
                                                    <td>
                                                        <Badge bg={getSeverityBadgeVariant(condition.severity)}>
                                                            {getSeverityName(condition.severity)}
                                                        </Badge>
                                                    </td>
                                                    <td>{condition.description}</td>
                                                    <td>
                                                        {condition.latitude.toFixed(5)}, {condition.longitude.toFixed(5)}
                                                    </td>
                                                    <td>
                                                        {condition.imageUrl ? (
                                                            <a href={condition.imageUrl} target="_blank" rel="noreferrer">
                                                                <img 
                                                                    src={condition.imageUrl} 
                                                                    alt="Hình ảnh báo cáo" 
                                                                    style={{ maxWidth: '100px', maxHeight: '60px' }} 
                                                                />
                                                            </a>
                                                        ) : (
                                                            <span className="text-muted">Không có</span>
                                                        )}
                                                    </td>
                                                    <td>
                                                        {condition.timestamp?.toLocaleString() || 'N/A'}
                                                    </td>
                                                    <td>
                                                        {condition.reportedBy === 'anonymous' ? 
                                                            'Ẩn danh' : 
                                                            condition.reportedBy
                                                        }
                                                    </td>
                                                    <td>
                                                        <div className="d-flex gap-2">
                                                            <Button
                                                                variant="success"
                                                                size="sm"
                                                                onClick={() => handleApprove(condition.id)}
                                                            >
                                                                Duyệt
                                                            </Button>
                                                            <Button
                                                                variant="danger"
                                                                size="sm"
                                                                onClick={() => handleReject(condition.id)}
                                                            >
                                                                Từ chối
                                                            </Button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </Table>
                                </div>
                            )}
                        </div>
                    </Col>
                </Row>
            )}

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
                            <div className="mb-4">
                                <p><strong>Hoặc double-click để chọn vị trí trên bản đồ:</strong></p>
                                <div className="map-container">
                                    <MapContainer
                                        center={defaultPosition}
                                        zoom={13}
                                        style={{ height: "400px", width: "100%" }}
                                    >
                                        <TileLayer
                                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                                        />
                                        <LocationMarker position={position} setPosition={setPosition} />
                                    </MapContainer>
                                </div>

                                <div className="text-muted mt-2">
                                    Double-click vào bản đồ để chọn vị trí. Tọa độ sẽ tự động cập nhật vào form.
                                </div>
                            </div>
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
                                            <td>{condition.id.substring(0, 8)}...</td>
                                            <td>{getConditionTypeName(condition.type)}</td>
                                            <td>
                                                <Badge bg={getSeverityBadgeVariant(condition.severity)}>
                                                    {getSeverityName(condition.severity)}
                                                </Badge>
                                            </td>
                                            <td>{condition.description}</td>
                                            <td>
                                                {condition.latitude.toFixed(5)}, {condition.longitude.toFixed(5)}
                                            </td>
                                            <td>
                                                {condition.timestamp?.toLocaleString() || 'N/A'}
                                            </td>
                                            <td>
                                                <div className="d-flex gap-2">
                                                    <Button
                                                        variant="primary"
                                                        size="sm"
                                                        onClick={() => handleEdit(condition)}
                                                    >
                                                        Sửa
                                                    </Button>
                                                    <Button
                                                        variant="danger"
                                                        size="sm"
                                                        onClick={() => handleDelete(condition.id)}
                                                    >
                                                        Xóa
                                                    </Button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    )}
                </Col>
            </Row>
            {/* Modal chỉnh sửa tình trạng giao thông */}
            <Modal show={showEditModal} onHide={() => setShowEditModal(false)} size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>Sửa tình trạng giao thông</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form onSubmit={handleEditSubmit}>
                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Vĩ độ (Latitude) *</Form.Label>
                                    <Form.Control
                                        type="number"
                                        step="0.000001"
                                        name="latitude"
                                        value={editFormData.latitude}
                                        onChange={handleEditInputChange}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Kinh độ (Longitude) *</Form.Label>
                                    <Form.Control
                                        type="number"
                                        step="0.000001"
                                        name="longitude"
                                        value={editFormData.longitude}
                                        onChange={handleEditInputChange}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                        </Row>
                        <div className="mb-4">
                            <p><strong>Hoặc double-click để chọn vị trí trên bản đồ:</strong></p>
                            <div className="map-container">
                                <MapContainer
                                    center={editPosition ? [editPosition.lat, editPosition.lng] : defaultPosition}
                                    zoom={13}
                                    style={{ height: "300px", width: "100%" }}
                                    key={`edit-map-${showEditModal}`} // Cần key để re-render bản đồ khi modal mở
                                >
                                    <TileLayer
                                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                                    />
                                    <EditLocationMarker position={editPosition} setPosition={setEditPosition} />
                                </MapContainer>
                            </div>
                            <div className="text-muted mt-2">
                                Double-click vào bản đồ để chọn vị trí mới. Tọa độ sẽ tự động cập nhật.
                            </div>
                        </div>
                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Loại sự cố</Form.Label>
                                    <Form.Select
                                        name="type"
                                        value={editFormData.type}
                                        onChange={handleEditInputChange}
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
                                        value={editFormData.severity}
                                        onChange={handleEditInputChange}
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
                                value={editFormData.description}
                                onChange={handleEditInputChange}
                                required
                            />
                        </Form.Group>

                        <div className="d-flex justify-content-end gap-2">
                            <Button variant="secondary" onClick={() => setShowEditModal(false)}>
                                Hủy
                            </Button>
                            <Button variant="primary" type="submit">
                                Lưu thay đổi
                            </Button>
                        </div>
                    </Form>
                </Modal.Body>
            </Modal>
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