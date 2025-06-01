import React, { useState, useEffect, useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { Button, Form, Card, Spinner } from 'react-bootstrap';
import { UserDispatchContext } from "../configs/MyContexts";
import { authApi } from "../configs/Apis";
import { endpoints } from "../configs/Apis";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  // State cho các chế độ giao diện
  const [view, setView] = useState('login'); // 'login', 'forgotPassword', 'resetPassword'

  // State cho quên mật khẩu
  const [forgotEmail, setForgotEmail] = useState("");
  const [forgotLoading, setForgotLoading] = useState(false);

  // State cho đặt lại mật khẩu
  const [resetToken, setResetToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [resetLoading, setResetLoading] = useState(false);

  const navigate = useNavigate();
  const dispatch = useContext(UserDispatchContext);

  useEffect(() => {
    setUsername("");
    setPassword("");

    // Kiểm tra đã đăng nhập chưa từ sessionStorage
    if (sessionStorage.getItem('isLoggedIn') === 'true') {
      navigate('/');
    }
  }, [navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!username || !password) {
      toast.error("Vui lòng nhập tên đăng nhập và mật khẩu!");
      return;
    }

    setLoading(true);

    const formData = new FormData();
    formData.append("username", username);
    formData.append("password", password);

    try {
      const response = await authApi.post("auth/login", formData);

      const user = response.data;

      // Sử dụng token trả về từ server thay vì tạo mới
      if (!user.token) {
        console.warn("Server không trả về token JWT, sẽ dùng token tạm thời");
        user.token = btoa(`${user.id}:${user.username}:${new Date().getTime()}`);
      }

      // Lưu thông tin user vào sessionStorage
      sessionStorage.setItem("user", JSON.stringify(user));
      sessionStorage.setItem("isLoggedIn", "true");

      // Dispatch action login
      dispatch({
        type: "login",
        payload: user
      });   
      toast.success("Đăng nhập thành công! Chào mừng " + user.fullName)

      // Chuyển hướng sau khi hiển thị thông báo
      setTimeout(() => {
        navigate('/');
      }, 1800);
    } catch (error) {
      console.error("Lỗi đăng nhập:", error);

      // Hiển thị thông báo lỗi chi tiết hơn
      if (error.response) {
        // Chọn thông báo dựa vào status code
        if (error.response.status === 401) {
          toast.error("Tài khoản hoặc mật khẩu không chính xác!");
        } else if (error.response.status === 403) {
          toast.error("Tài khoản của bạn không có quyền truy cập!");
        } else if (error.response.status === 500) {
          toast.error("Lỗi hệ thống, vui lòng thử lại sau!");
        } else {
          // Nếu không phải các lỗi trên, hiển thị message từ server hoặc thông báo chung
          const errorMessage = error.response.data?.error || "Đăng nhập thất bại. Vui lòng thử lại.";
          toast.error(errorMessage);
        }
      } else if (error.request) {
        toast.error("Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng!");
      } else {
        toast.error("Đã xảy ra lỗi. Vui lòng thử lại sau!");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();

    if (!forgotEmail) {
      toast.error("Vui lòng nhập địa chỉ email");
      return;
    }

    // Kiểm tra định dạng email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(forgotEmail)) {
      toast.error("Vui lòng nhập địa chỉ email hợp lệ");
      return;
    }

    setForgotLoading(true);

    try {
      const response = await authApi.post(endpoints.forgotPassword, null, {
        params: { email: forgotEmail }
      });

      toast.success("Mã xác nhận đã được gửi đến email của bạn");

      // Chuyển người dùng đến trang Reset
      navigate('/reset-password');

    } catch (error) {
      toast.error(
        error.response?.data?.error ||
        "Không thể xử lý yêu cầu đặt lại mật khẩu. Vui lòng thử lại sau."
      );
    } finally {
      setForgotLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();

    if (!resetToken || !newPassword || !confirmPassword) {
      toast.error("Vui lòng nhập đầy đủ thông tin!");
      return;
    }

    if (newPassword !== confirmPassword) {
      toast.error("Mật khẩu mới và xác nhận mật khẩu không khớp!");
      return;
    }

    setResetLoading(true);

    const formData = new FormData();
    formData.append("token", resetToken);
    formData.append("newPassword", newPassword);

    try {
      const response = await authApi.post(endpoints.resetPassword, formData);

      toast.success("Đặt lại mật khẩu thành công!");

      // Kiểm tra xem response có chứa thông tin user và token không
      if (response.data.token) {
        // Nếu có, lưu thông tin và đăng nhập tự động
        const user = {
          id: response.data.id,
          username: response.data.username,
          email: response.data.email,
          fullName: response.data.fullName,
          phone: response.data.phone,
          avatarUrl: response.data.avatarUrl,
          token: response.data.token
        };

        sessionStorage.setItem("user", JSON.stringify(user));
        sessionStorage.setItem("isLoggedIn", "true");

        dispatch({
          type: "login",
          payload: user
        }); setTimeout(() => {
          navigate('/');
        }, 1800);
      } else {              // Nếu không, chuyển về màn hình đăng nhập
        setTimeout(() => {
          setView('login');
        }, 1800);
      }
    } catch (error) {
      toast.error(
        error.response?.data?.error ||
        "Đặt lại mật khẩu thất bại. Vui lòng thử lại."
      );
    } finally {
      setResetLoading(false);
    }
  };

  const renderLoginView = () => (
    <>
      <h3 className="text-center mb-4">Đăng nhập</h3>
      <Form onSubmit={handleSubmit}>
        <Form.Group className="mb-3">
          <Form.Label>Tên đăng nhập</Form.Label>
          <Form.Control
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={loading}
            required
            style={{ padding: '12px', borderRadius: '4px', fontSize: '0.95rem' }}
          />
        </Form.Group>

        <Form.Group className="mb-4">
          <Form.Label>Mật khẩu</Form.Label>
          <Form.Control
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={loading}
            required
            style={{ padding: '12px', borderRadius: '4px', fontSize: '0.95rem' }}
          />
        </Form.Group>

        <div className="d-grid gap-2">
          <Button
            type="submit"
            disabled={loading}
            style={{
              backgroundColor: '#000000',
              borderColor: '#000000',
              padding: '12px',
              fontWeight: '600',
              borderRadius: '4px',
              transition: 'all 0.2s ease'
            }}
          >
            {loading ? (
              <>
                <Spinner as="span" animation="border" size="sm" className="me-2" />
                Đang đăng nhập...
              </>
            ) : 'Đăng nhập'}
          </Button>
        </div>

        <div className="text-center mt-3">
          <div className="mb-2">
            <span style={{ color: '#6c757d', fontSize: '0.9rem' }}>Chưa có tài khoản? </span>
            <Link to="/register" style={{ color: '#000000', fontWeight: '500', textDecoration: 'none' }}>Đăng ký</Link>
          </div>
          <div>
            <span
              onClick={() => setView('forgotPassword')}
              style={{
                color: '#000000',
                fontWeight: '500',
                cursor: 'pointer',
                textDecoration: 'none'
              }}
            >
              Quên mật khẩu?
            </span>
          </div>
        </div>
      </Form>
    </>
  );

  const renderForgotPasswordView = () => (
    <>
      <div className="mb-3">
        <button
          className="btn btn-sm"
          onClick={() => setView('login')}
          style={{
            color: '#000000',
            borderColor: '#e0e0e0',
            backgroundColor: 'transparent',
            borderRadius: '4px'
          }}
        >
          <i className="fas fa-arrow-left me-1"></i> Quay lại đăng nhập
        </button>
      </div>

      <h3 className="text-center mb-4" style={{ color: '#2b2b2b', fontWeight: '600' }}>Khôi phục mật khẩu</h3>
      <p className="text-center mb-4" style={{ color: '#6c757d', fontSize: '0.9rem' }}>
        Nhập địa chỉ email đã đăng ký. Chúng tôi sẽ gửi cho bạn hướng dẫn
        để đặt lại mật khẩu.
      </p>

      <Form onSubmit={handleForgotPassword}>
        <Form.Group className="mb-4">
          <Form.Label>Địa chỉ Email</Form.Label>
          <Form.Control
            type="email"
            value={forgotEmail}
            onChange={(e) => setForgotEmail(e.target.value)}
            disabled={forgotLoading}
            required
            style={{ padding: '12px', borderRadius: '4px', fontSize: '0.95rem' }}
          />
        </Form.Group>

        <div className="d-grid gap-2">
          <Button
            type="submit"
            disabled={forgotLoading}
            style={{
              backgroundColor: '#000000',
              borderColor: '#000000',
              padding: '12px',
              fontWeight: '600',
              borderRadius: '4px'
            }}
          >
            {forgotLoading ? (
              <>
                <Spinner as="span" animation="border" size="sm" className="me-2" />
                Đang xử lý...
              </>
            ) : 'Gửi yêu cầu'}
          </Button>
        </div>
      </Form>
    </>
  );

  const renderResetPasswordView = () => (
    <>
      <div className="mb-3">
        <button
          className="btn btn-sm"
          onClick={() => setView('forgotPassword')}
          style={{
            color: '#5a7184',
            borderColor: '#d1d9e2',
            backgroundColor: 'transparent',
            borderRadius: '4px'
          }}
        >
          <i className="fas fa-arrow-left me-1"></i> Quay lại
        </button>
      </div>

      <h3 className="text-center mb-4" style={{ color: '#2b2b2b', fontWeight: '600' }}>Đặt lại mật khẩu</h3>

      <Form onSubmit={handleResetPassword}>
        <Form.Group className="mb-3">
          <Form.Label>Mã xác nhận</Form.Label>
          <Form.Control
            type="text"
            value={resetToken}
            onChange={(e) => setResetToken(e.target.value)}
            disabled={resetLoading}
            required
            style={{ padding: '12px', borderRadius: '4px', fontSize: '0.95rem' }}
          />
          <Form.Text style={{ color: '#6c757d', fontSize: '0.85rem' }}>
            Nhập mã xác nhận từ email hoặc từ phản hồi hệ thống.
          </Form.Text>
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Mật khẩu mới</Form.Label>
          <Form.Control
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            disabled={resetLoading}
            required
            style={{ padding: '12px', borderRadius: '4px', fontSize: '0.95rem' }}
          />
        </Form.Group>

        <Form.Group className="mb-4">
          <Form.Label>Xác nhận mật khẩu mới</Form.Label>
          <Form.Control
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            disabled={resetLoading}
            required
            style={{ padding: '12px', borderRadius: '4px', fontSize: '0.95rem' }}
          />
          <Form.Text style={{ color: '#6c757d', fontSize: '0.85rem' }}>
            Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường,
            số và ký tự đặc biệt (@$!%*?&)
          </Form.Text>
        </Form.Group>

        <div className="d-grid gap-2">
          <Button
            type="submit"
            disabled={resetLoading}
            style={{
              backgroundColor: '#000000',
              borderColor: '#000000',
              padding: '12px',
              fontWeight: '600',
              borderRadius: '4px'
            }}
          >
            {resetLoading ? (
              <>
                <Spinner as="span" animation="border" size="sm" className="me-2" />
                Đang xử lý...
              </>
            ) : 'Đặt lại mật khẩu'}
          </Button>
        </div>
      </Form>
    </>
  );

  return (
    <div className="login-container d-flex align-items-center justify-content-center"
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
          {view === 'login' && renderLoginView()}
          {view === 'forgotPassword' && renderForgotPasswordView()}
          {view === 'resetPassword' && renderResetPasswordView()}
        </Card.Body>
      </Card>
    </div>
  );
};

export default Login;