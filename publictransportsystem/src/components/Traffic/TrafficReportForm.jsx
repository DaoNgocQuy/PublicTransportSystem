import React, { useState, useEffect } from 'react';
import { Form, Button, Row, Col, Alert, Spinner, Card } from 'react-bootstrap';
import { reportTrafficCondition } from '../../services/TrafficService';
import './TrafficReportForm.css';

const TrafficReportForm = ({ onReportSubmitted, userLocation }) => {
    const [formData, setFormData] = useState({
        latitude: userLocation?.lat || '',
        longitude: userLocation?.lng || '',
        type: 'congestion',
        severity: 'medium',
        description: '',
        userId: JSON.parse(sessionStorage.getItem('user'))?.id || ''
    });

    const [image, setImage] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    // Cập nhật vị trí khi userLocation thay đổi
    useEffect(() => {
        if (userLocation) {
            setFormData(prev => ({
                ...prev,
                latitude: userLocation.lat,
                longitude: userLocation.lng
            }));
        }
    }, [userLocation]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) { // Giới hạn 5MB
                setError('Kích thước file quá lớn. Vui lòng chọn file nhỏ hơn 5MB.');
                return;
            }
            setImage(file);

            // Tạo preview
            const reader = new FileReader();
            reader.onloadend = () => {
                setImagePreview(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess(false);
        setLoading(true);

        try {
            if (!formData.latitude || !formData.longitude) {
                throw new Error('Vui lòng cung cấp vị trí.');
            }

            if (!formData.description) {
                throw new Error('Vui lòng nhập mô tả tình trạng.');
            }

            // Parse coordinates to numbers
            const reportDataToSubmit = {
                ...formData,
                latitude: parseFloat(formData.latitude),
                longitude: parseFloat(formData.longitude)
            };

            await reportTrafficCondition(reportDataToSubmit, image);

            setSuccess(true);
            // Reset form
            setFormData({
                latitude: userLocation?.lat || '',
                longitude: userLocation?.lng || '',
                type: 'congestion',
                severity: 'medium',
                description: '',
                userId: formData.userId
            });
            setImage(null);
            setImagePreview(null);

            // Thông báo cho component cha
            if (onReportSubmitted) {
                onReportSubmitted();
            }
        } catch (err) {
            setError(err.message || 'Có lỗi xảy ra khi gửi báo cáo.');
        } finally {
            setLoading(false);
        }
    };    return (
        <Card className="report-form-card">
            <Card.Body>
                {error && <Alert variant="danger">{error}</Alert>}
                {success && <Alert variant="success">Gửi báo cáo thành công!</Alert>}

                <Form onSubmit={handleSubmit}>
                    <Row>
                        <Col md={6}>
                            <Form.Group className="mb-3">
                                <Form.Label>Vĩ độ (Latitude)</Form.Label>
                                <Form.Control
                                    type="number"
                                    step="0.000001"
                                    name="latitude"
                                    value={formData.latitude}
                                    onChange={handleInputChange}
                                    required
                                />
                            </Form.Group>
                        </Col>
                        <Col md={6}>
                            <Form.Group className="mb-3">
                                <Form.Label>Kinh độ (Longitude)</Form.Label>
                                <Form.Control
                                    type="number"
                                    step="0.000001"
                                    name="longitude"
                                    value={formData.longitude}
                                    onChange={handleInputChange}
                                    required
                                />
                            </Form.Group>
                        </Col>
                    </Row>

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

                    <Form.Group className="mb-3">
                        <Form.Label>Mô tả chi tiết</Form.Label>
                        <Form.Control
                            as="textarea"
                            rows={3}
                            name="description"
                            value={formData.description}
                            onChange={handleInputChange}
                            placeholder="Mô tả tình trạng giao thông tại đây..."
                            required
                        />
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Hình ảnh minh họa (không bắt buộc)</Form.Label>
                        <Form.Control
                            type="file"
                            accept="image/*"
                            onChange={handleImageChange}
                        />
                        <Form.Text className="text-muted">
                            Chọn một hình ảnh minh họa (tối đa 5MB)
                        </Form.Text>
                    </Form.Group>

                    {imagePreview && (
                        <div className="mb-3">
                            <p>Xem trước:</p>
                            <img
                                src={imagePreview}
                                alt="Preview"
                                className="img-preview"
                                style={{ maxWidth: '100%', maxHeight: '200px' }}
                            />
                        </div>
                    )}

                    <Button
                        variant="primary"
                        type="submit"
                        disabled={loading}
                    >
                        {loading ? (
                            <>
                                <Spinner animation="border" size="sm" className="me-2" />
                                Đang gửi...
                            </>
                        ) : 'Gửi báo cáo'}
                    </Button>
                </Form>
            </Card.Body>
        </Card>
    );
};

export default TrafficReportForm;