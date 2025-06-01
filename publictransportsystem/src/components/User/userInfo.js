import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Tab, Tabs, Spinner, Alert } from 'react-bootstrap';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../../configs/Apis';
import { FaEnvelope, FaPhone, FaCalendar, FaClock } from 'react-icons/fa';
import './userInfo.css';

const Userinfo = () => {
  // State cho thông tin người dùng
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  // State cho form chỉnh sửa thông tin
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    avatar: null
  });

  // State cho form đổi mật khẩu
  const [passwordData, setPasswordData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  // State cho hiển thị ảnh xem trước
  const [previewImage, setPreviewImage] = useState(null);

  // State cho tab tuyến yêu thích
  const [favorites, setFavorites] = useState([]);
  const [loadingFavorites, setLoadingFavorites] = useState(false);
  const [favoriteError, setFavoriteError] = useState(null);

  // State cho các thao tác đang xử lý
  const [updating, setUpdating] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);

  // State cho thông báo kết quả
  const [updateMessage, setUpdateMessage] = useState(null);
  const [passwordMessage, setPasswordMessage] = useState(null);

  // Lấy thông tin người dùng khi component mount
  useEffect(() => {
    fetchUserInfo();
    fetchFavorites();
  }, []);

  // Hàm lấy thông tin người dùng từ API
  const fetchUserInfo = async () => {
    try {
      setLoading(true);

      // Lấy thông tin người dùng từ sessionStorage
      const userStr = sessionStorage.getItem('user');
      if (!userStr) {
        throw new Error('Không tìm thấy thông tin người dùng');
      }

      const userData = JSON.parse(userStr);

      // Gọi API để lấy thông tin chi tiết nhất
      const response = await authApi.get(`/auth/profile/${userData.id}`);

      // Cập nhật state
      setUser(response.data);
      setFormData({
        fullName: response.data.fullName || '',
        email: response.data.email || '',
        phone: response.data.phone || ''
      });

      setError(null);
    } catch (err) {
      console.error('Lỗi khi lấy thông tin người dùng:', err);
      setError('Không thể lấy thông tin người dùng. Vui lòng thử lại sau.');
      toast.error('Không thể tải thông tin người dùng');
    } finally {
      setLoading(false);
    }
  };

  // Xử lý thay đổi input
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  // Xử lý thay đổi input mật khẩu
  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData({
      ...passwordData,
      [name]: value
    });
  };

  // Xử lý khi chọn file ảnh
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setFormData({
        ...formData,
        avatar: file
      });

      // Hiển thị ảnh xem trước
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImage(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  // Xử lý cập nhật thông tin
  const handleUpdateProfile = async (e) => {
    e.preventDefault();

    try {
      setUpdating(true);
      setUpdateMessage(null);

      // Tạo form data để gửi lên server
      const updateFormData = new FormData();
      updateFormData.append('fullName', formData.fullName);
      updateFormData.append('email', formData.email);
      updateFormData.append('phone', formData.phone);

      if (formData.avatar) {
        updateFormData.append('avatar', formData.avatar);
      }

      // Gọi API cập nhật thông tin
      const response = await authApi.put(`/auth/profile/${user.id}`, updateFormData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      // Cập nhật user state và sessionStorage
      setUser({
        ...user,
        ...response.data
      });

      // Cập nhật thông tin trong sessionStorage
      const userStr = sessionStorage.getItem('user');
      if (userStr) {
        const userData = JSON.parse(userStr);
        const updatedUserData = {
          ...userData,
          ...response.data
        };
        sessionStorage.setItem('user', JSON.stringify(updatedUserData));
      }

      setUpdateMessage({ type: 'success', text: 'Cập nhật thông tin thành công!' });
      toast.success('Cập nhật thông tin thành công!');

    } catch (err) {
      console.error('Lỗi khi cập nhật thông tin:', err);
      const errorMessage = err.response?.data?.error || 'Không thể cập nhật thông tin. Vui lòng thử lại sau.';
      setUpdateMessage({ type: 'danger', text: errorMessage });
      toast.error(errorMessage);
    } finally {
      setUpdating(false);
    }
  };

  // Xử lý đổi mật khẩu
  const handleChangePassword = async (e) => {
    e.preventDefault();

    // Kiểm tra mật khẩu mới và xác nhận mật khẩu
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setPasswordMessage({ type: 'danger', text: 'Mật khẩu mới và xác nhận mật khẩu không khớp' });
      return;
    }

    // Kiểm tra độ phức tạp của mật khẩu
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRegex.test(passwordData.newPassword)) {
      setPasswordMessage({
        type: 'danger',
        text: 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&)'
      });
      return;
    }

    try {
      setChangingPassword(true);
      setPasswordMessage(null);

      // Gọi API đổi mật khẩu
      const response = await authApi.post(`/auth/change-password/${user.id}`, null, {
        params: {
          oldPassword: passwordData.oldPassword,
          newPassword: passwordData.newPassword
        }
      });

      // Xóa dữ liệu form mật khẩu
      setPasswordData({
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      });

      setPasswordMessage({ type: 'success', text: 'Đổi mật khẩu thành công!' });
      toast.success('Đổi mật khẩu thành công!');

    } catch (err) {
      console.error('Lỗi khi đổi mật khẩu:', err);
      const errorMessage = err.response?.data?.error || 'Không thể đổi mật khẩu. Vui lòng thử lại sau.';
      setPasswordMessage({ type: 'danger', text: errorMessage });
      toast.error(errorMessage);
    } finally {
      setChangingPassword(false);
    }
  };

  // Xử lý lấy danh sách tuyến yêu thích
  const fetchFavorites = async () => {
    try {
      setLoadingFavorites(true);
      setFavoriteError(null);
      
      // Gọi API lấy danh sách tuyến yêu thích
      const response = await authApi.get('/api/favorites');
      setFavorites(response.data);
      
    } catch (err) {
      console.error('Lỗi khi lấy danh sách tuyến yêu thích:', err);
      setFavoriteError('Không thể tải danh sách tuyến yêu thích');
      toast.error('Không thể tải danh sách tuyến yêu thích');
    } finally {
      setLoadingFavorites(false);
    }
  };

  // Xử lý xóa tuyến yêu thích
  const handleRemoveFavorite = async (routeId) => {
    try {
      await authApi.delete(`/api/favorites/${routeId}`);
      
      // Cập nhật lại danh sách tuyến yêu thích
      setFavorites(favorites.filter(fav => fav.route_id !== routeId));
      
      toast.success('Đã xóa khỏi danh sách yêu thích');
    } catch (err) {
      console.error('Lỗi khi xóa khỏi danh sách yêu thích:', err);
      toast.error('Không thể xóa khỏi danh sách yêu thích');
    }
  };

  const handleViewRoute = (routeId) => {
    console.log("Viewing route with ID:", routeId);
    
    // Lưu ID tuyến vào sessionStorage
    sessionStorage.setItem('selectedRouteId', routeId);
    
    // Chuyển về trang chủ
    navigate('/');
  };

  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '70vh' }}>
        <Spinner animation="border" variant="dark" />
        <span className="ms-2">Đang tải thông tin...</span>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="mt-5">
        <Alert variant="danger">
          {error}
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="profile-container">
      <Row>
        <Col lg={4} md={5}>
          <Card className="profile-card text-center mb-4">
            <Card.Body>
              <div className="avatar-container">
                <img
                  src={previewImage || user.avatarUrl || 'https://via.placeholder.com/150/CCCCCC/FFFFFF?text=User'}
                  alt="Avatar"
                  className="avatar-img"
                />
              </div>
              <h4 className="user-name">{user.fullName || user.username}</h4>
              <p className="user-role">{user.role || 'USER'}</p>

              {/* Phần thông tin liên hệ được cập nhật */}
              <div className="contact-info">
                <div className="info-item">
                  <FaEnvelope />
                  <div>
                    <span className="contact-label">Email</span>
                    <div className="contact-value">{user.email || 'Chưa có email'}</div>
                  </div>
                </div>
                {user.phone && (
                  <div className="info-item">
                    <FaPhone />
                    <div>
                      <span className="contact-label">Số điện thoại</span>
                      <div className="contact-value">{user.phone}</div>
                    </div>
                  </div>
                )}
              </div>

              <div className="last-login">
                <div>
                  <FaClock />
                  <span>Đăng nhập cuối: {user.lastLogin ? new Date(user.lastLogin).toLocaleString() : 'N/A'}</span>
                </div>
                {user.createdAt && (
                  <div>
                    <FaCalendar />
                    <span>Tham gia: {new Date(user.createdAt).toLocaleDateString()}</span>
                  </div>
                )}
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={8} md={7}>
          <Card className="info-card">
            <Card.Header>
              <h5 className="header-title">Quản lý tài khoản</h5>
            </Card.Header>
            <Card.Body>
              <Tabs defaultActiveKey="profile" className="profile-tabs mb-4">
                <Tab eventKey="profile" title="Thông tin cá nhân">
                  {updateMessage && (
                    <Alert variant={updateMessage.type} dismissible onClose={() => setUpdateMessage(null)}>
                      {updateMessage.text}
                    </Alert>
                  )}

                  <Form onSubmit={handleUpdateProfile}>
                    <Form.Group as={Row} className="mb-3">
                      <Form.Label column sm={3}>Tên đăng nhập</Form.Label>
                      <Col sm={9}>
                        <Form.Control
                          plaintext
                          readOnly
                          defaultValue={user.username}
                          className="form-control-plaintext"
                        />
                      </Col>
                    </Form.Group>

                    <Form.Group as={Row} className="mb-3">
                      <Form.Label column sm={3}>Họ và tên</Form.Label>
                      <Col sm={9}>
                        <Form.Control
                          type="text"
                          name="fullName"
                          value={formData.fullName}
                          onChange={handleInputChange}
                        />
                      </Col>
                    </Form.Group>

                    <Form.Group as={Row} className="mb-3">
                      <Form.Label column sm={3}>Email</Form.Label>
                      <Col sm={9}>
                        <Form.Control
                          type="email"
                          name="email"
                          value={formData.email}
                          onChange={handleInputChange}
                        />
                      </Col>
                    </Form.Group>

                    <Form.Group as={Row} className="mb-3">
                      <Form.Label column sm={3}>Số điện thoại</Form.Label>
                      <Col sm={9}>
                        <Form.Control
                          type="text"
                          name="phone"
                          value={formData.phone}
                          onChange={handleInputChange}
                        />
                      </Col>
                    </Form.Group>

                    <Form.Group as={Row} className="mb-3">
                      <Form.Label column sm={3}>Ảnh đại diện</Form.Label>
                      <Col sm={9}>
                        <Form.Control
                          type="file"
                          accept="image/*"
                          onChange={handleFileChange}
                        />
                        <Form.Text className="text-muted">
                          Chọn ảnh có kích thước dưới 2MB
                        </Form.Text>
                      </Col>
                    </Form.Group>

                    <div className="d-flex justify-content-end">
                      <Button
                        type="submit"
                        variant="primary"
                        disabled={updating}
                      >
                        {updating ? (
                          <>
                            <Spinner as="span" animation="border" size="sm" className="me-2" />
                            Đang cập nhật...
                          </>
                        ) : 'Cập nhật thông tin'}
                      </Button>
                    </div>
                  </Form>
                </Tab>

                <Tab eventKey="password" title="Đổi mật khẩu">
                  {passwordMessage && (
                    <Alert variant={passwordMessage.type} dismissible onClose={() => setPasswordMessage(null)}>
                      {passwordMessage.text}
                    </Alert>
                  )}

                  <Form onSubmit={handleChangePassword}>
                    <Form.Group as={Row} className="mb-3">
                      <Form.Label column sm={3}>Mật khẩu hiện tại</Form.Label>
                      <Col sm={9}>
                        <Form.Control
                          type="password"
                          name="oldPassword"
                          value={passwordData.oldPassword}
                          onChange={handlePasswordChange}
                          required
                        />
                      </Col>
                    </Form.Group>

                    <Form.Group as={Row} className="mb-3">
                      <Form.Label column sm={3}>Mật khẩu mới</Form.Label>
                      <Col sm={9}>
                        <Form.Control
                          type="password"
                          name="newPassword"
                          value={passwordData.newPassword}
                          onChange={handlePasswordChange}
                          required
                        />
                        <Form.Text className="text-muted">
                          Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&)
                        </Form.Text>
                      </Col>
                    </Form.Group>

                    <Form.Group as={Row} className="mb-3">
                      <Form.Label column sm={3}>Xác nhận mật khẩu</Form.Label>
                      <Col sm={9}>
                        <Form.Control
                          type="password"
                          name="confirmPassword"
                          value={passwordData.confirmPassword}
                          onChange={handlePasswordChange}
                          required
                        />
                      </Col>
                    </Form.Group>

                    <div className="d-flex justify-content-end">
                      <Button
                        type="submit"
                        variant="primary"
                        disabled={changingPassword}
                      >
                        {changingPassword ? (
                          <>
                            <Spinner as="span" animation="border" size="sm" className="me-2" />
                            Đang xử lý...
                          </>
                        ) : 'Đổi mật khẩu'}
                      </Button>
                    </div>
                  </Form>
                </Tab>

                <Tab eventKey="favorites" title="Tuyến yêu thích">
                  {favoriteError && (
                    <Alert variant={favoriteError ? 'danger' : 'success'} dismissible onClose={() => setFavoriteError(null)}>
                      {favoriteError}
                    </Alert>
                  )}

                  {loadingFavorites ? (
                    <div className="text-center py-4">
                      <Spinner animation="border" variant="dark" />
                      <p className="mt-2">Đang tải danh sách tuyến yêu thích...</p>
                    </div>
                  ) : favorites.length === 0 ? (
                    <div className="text-center py-4">
                      <div className="mb-3">
                        <i className="far fa-heart" style={{ fontSize: '48px', color: '#ccc' }}></i>
                      </div>
                      <p className="text-muted">Bạn chưa có tuyến yêu thích nào</p>
                      <p className="small">Thêm tuyến yêu thích bằng cách nhấn vào biểu tượng trái tim trong danh sách tuyến</p>
                    </div>
                  ) : (
                    <div className="favorites-list">
                      {favorites.map((favorite) => (
                        <Card key={favorite.id} className="favorite-item mb-3">
                          <Card.Body>
                            <div className="d-flex justify-content-between align-items-center">
                              <div>
                                <h5 className="route-name" style={{ color: '#212529', fontWeight: '600' }}>
                                  {favorite.route_name}
                                </h5>
                                <p className="route-path text-muted mb-1">
                                  {favorite.start_location} - {favorite.end_location}
                                </p>
                                {/* Removed the operation time display */}
                              </div>
                              <div>
                                <Button 
                                  variant="light" 
                                  size="sm" 
                                  className="me-2"
                                  onClick={() => handleViewRoute(favorite.route_id)}
                                >
                                  <i className="fas fa-map-marker-alt me-1"></i> Xem
                                </Button>
                                <Button 
                                  variant="outline-danger" 
                                  size="sm"
                                  onClick={() => handleRemoveFavorite(favorite.route_id)}
                                >
                                  <i className="far fa-trash-alt"></i>
                                </Button>
                              </div>
                            </div>
                          </Card.Body>
                        </Card>
                      ))}
                    </div>
                  )}
                </Tab>
              </Tabs>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Userinfo;