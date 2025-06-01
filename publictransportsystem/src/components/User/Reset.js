import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { toast } from 'react-toastify';
import { Form, Card, Button, Spinner } from 'react-bootstrap';
import { authApi, endpoints } from "../../configs/Apis";

const Reset = () => {
  const [token, setToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  // Lấy token từ query parameter khi component được tải
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const tokenParam = params.get('token');
    if (tokenParam) {
      setToken(tokenParam);
    }
  }, [location]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!token) {
      toast.error("Mã xác nhận không hợp lệ");
      return;
    }
    
    if (!newPassword || !confirmPassword) {
      toast.error("Vui lòng nhập mật khẩu mới và xác nhận mật khẩu");
      return;
    }
    
    if (newPassword !== confirmPassword) {
      toast.error("Mật khẩu mới và xác nhận mật khẩu không khớp");
      return;
    }
    
    // Kiểm tra độ phức tạp của mật khẩu
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRegex.test(newPassword)) {
      toast.error(
        "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&)"
      );
      return;
    }
    
    setLoading(true);
    
    try {
      const formData = new FormData();
      formData.append("token", token);
      formData.append("newPassword", newPassword);
      
      const response = await authApi.post(endpoints.resetPassword, formData);
      
      toast.success(response.data.message || "Đặt lại mật khẩu thành công!");
      
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (error) {
      toast.error(
        error.response?.data?.error || 
        "Không thể đặt lại mật khẩu. Mã xác nhận có thể đã hết hạn hoặc không hợp lệ."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="d-flex align-items-center justify-content-center"
        style={{ 
          minHeight: "100vh", 
          width: "100vw",
          margin: "0",
          padding: "0",
          background: "#121212", /* Nền đen */
          position: "fixed", 
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          overflow: "hidden"
        }}>
      <Card className="shadow-lg" style={{ 
        width: "420px", 
        maxWidth: "90%", 
        borderRadius: "12px",
        border: "none",
        backgroundColor: "#ffffff", /* Card màu trắng */
        boxShadow: "0 10px 25px rgba(0, 0, 0, 0.5)"
      }}>
        <Card.Body style={{ padding: "32px" }}>
          <div className="text-center mb-4">
            <h3 style={{ color: '#212529', fontWeight: 600 }}>Đặt lại mật khẩu</h3>
          </div>

          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label style={{ fontWeight: 500 }}>Mã xác nhận</Form.Label>
              <Form.Control
                type="text"
                value={token}
                onChange={(e) => setToken(e.target.value)}
                required
                style={{ padding: '12px', borderRadius: '4px', fontSize: '0.95rem' }}
              />
              <Form.Text style={{ color: '#6c757d', fontSize: '0.85rem' }}>
                Nhập mã xác nhận từ email của bạn.
              </Form.Text>
            </Form.Group>
            
            <Form.Group className="mb-3">
              <Form.Label style={{ fontWeight: 500 }}>Mật khẩu mới</Form.Label>
              <Form.Control
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
                disabled={loading}
                style={{ padding: '12px', borderRadius: '4px', fontSize: '0.95rem' }}
              />
            </Form.Group>
            
            <Form.Group className="mb-4">
              <Form.Label style={{ fontWeight: 500 }}>Xác nhận mật khẩu mới</Form.Label>
              <Form.Control
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                disabled={loading}
                style={{ padding: '12px', borderRadius: '4px', fontSize: '0.95rem' }}
              />
              <Form.Text style={{ color: '#6c757d', fontSize: '0.85rem' }}>
                Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, 
                số và ký tự đặc biệt (@$!%*?&)
              </Form.Text>
            </Form.Group>
            
            <div className="d-grid gap-2 mt-4">
              <Button
                type="submit"
                disabled={loading}
                style={{ 
                  backgroundColor: '#000000', 
                  borderColor: '#000000',
                  padding: '12px',
                  fontWeight: '600',
                  borderRadius: '4px'
                }}
              >
                {loading ? (
                  <>
                    <Spinner as="span" animation="border" size="sm" className="me-2" />
                    Đang xử lý...
                  </>
                ) : 'Đặt lại mật khẩu'}
              </Button>
            </div>
            
            <div className="text-center mt-3">
              <Button
                variant="link"
                onClick={() => navigate('/login')}
                style={{ 
                  color: '#000000', 
                  fontWeight: '500', 
                  textDecoration: 'none',
                  padding: '0'
                }}
              >
                Quay lại đăng nhập
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default Reset;