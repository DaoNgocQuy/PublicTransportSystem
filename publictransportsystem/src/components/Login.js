import React, { useState, useEffect, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import cookie from "react-cookies";
import { UserDispatchContext } from "../configs/MyContexts";
import { authApi, endpoints } from "../configs/Apis";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // Thay đổi từ userContext sang useContext
  const dispatch = useContext(UserDispatchContext);

  // Phần code còn lại giữ nguyên
  useEffect(() => {
    // Kiểm tra đã đăng nhập chưa
    if (localStorage.getItem('isLoggedIn') === 'true') {
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
      const response = await authApi.post(endpoints.login, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'Accept': 'application/json'
        }
      });

      console.log('Login response:', response.data);

      // Đảm bảo response.data có định dạng đúng và có token
      const userData = response.data;

      // Lưu token vào cookie (bảo mật hơn localStorage)
      if (userData.token) {
        cookie.save("token", userData.token, { path: "/" });
      }

      // Lưu user vào cookie và localStorage
      cookie.save("user", userData, { path: "/" });
      localStorage.setItem("user", JSON.stringify(userData));
      localStorage.setItem("isLoggedIn", "true");

      // Cập nhật context
      dispatch({
        type: "login",
        payload: userData
      });

      // Thiết lập token cho API calls
      if (userData.token) {
        authApi.defaults.headers.common['Authorization'] = `Bearer ${userData.token}`;
      }

      toast.success("Đăng nhập thành công!");

      // Chờ toast hiển thị xong rồi chuyển trang
      setTimeout(() => {
        navigate('/');
      }, 1500);
    } catch (error) {
      console.error("Login error:", error);
      const errorMessage = error.response?.data?.error || "Đăng nhập thất bại, vui lòng thử lại!";
      toast.error(errorMessage);
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
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="username" className="form-label">Tên đăng nhập</label>
            <input
              type="text"
              className="form-control"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
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
      <ToastContainer position="top-right" autoClose={2000} />
    </div>
  );
};

export default Login;