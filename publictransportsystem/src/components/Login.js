import React, { useState, useEffect, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { UserDispatchContext } from "../configs/MyContexts";
import { authApi } from "../configs/Apis";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // Thay đổi từ userContext sang useContext
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
      console.log('Login response:', response.data);

      // Tạo một token đơn giản từ thông tin người dùng
      const user = response.data;
      user.token = btoa(`${user.id}:${user.username}:${new Date().getTime()}`);

      // Lưu vào sessionStorage thay vì localStorage
      sessionStorage.setItem("user", JSON.stringify(user));
      sessionStorage.setItem("isLoggedIn", "true");

      // Cập nhật context
      dispatch({
        type: "login",
        payload: user
      });

      toast.success("Đăng nhập thành công!", {
        autoClose: 2000, // Đóng sau 2 giây
      });

      // Chuyển hướng sau khi thông báo hiển thị
      setTimeout(() => {
        navigate('/');
      }, 2000);
    } catch (error) {
      // existing error handling code
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container d-flex align-items-center justify-content-center"
      style={{
        minHeight: "100vh",
        background: "linear-gradient(135deg, #6e8efb, #a777e3)"
      }}>
      <div className="card shadow-lg p-4" style={{ width: "400px", maxWidth: "90%" }}>
        <h2 className="text-center mb-4">Đăng nhập</h2>
        <form onSubmit={handleSubmit} autoComplete="off">
          <div className="mb-3">
            <label htmlFor="username" className="form-label">Tên đăng nhập</label>
            <input
              type="text"
              className="form-control"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
              autoComplete="username"
              key="username-field"
            />
          </div>
          <div className="mb-3">
            <label htmlFor="password" className="form-label">Mật khẩu</label>
            <input
              type="password"
              className="form-control"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
              autoComplete="new-password"
              key="password-field"
            />
          </div>
          <div className="d-grid gap-2">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? (
                <span>
                  <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Đang xử lý...
                </span>
              ) : 'Đăng nhập'}
            </button>
          </div>
          <div className="mt-3 text-center">
            <p>Chưa có tài khoản? <a href="/register">Đăng ký</a></p>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Login;