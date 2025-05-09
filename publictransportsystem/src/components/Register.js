import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ToastContainer, toast } from 'react-toastify';
import { authApi, endpoints } from "../configs/Apis";

const Register = () => {
  const [userData, setUserData] = useState({
    username: "",
    password: "",
    confirmPassword: "",
    email: "",
    fullName: "",
    phone: ""
  });
  const [avatar, setAvatar] = useState(null);
  const [avatarPreview, setAvatarPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUserData({ ...userData, [name]: value });
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setAvatar(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setAvatarPreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const validateForm = () => {
    if (!userData.username || !userData.password || !userData.confirmPassword || 
        !userData.email || !userData.fullName) {
      toast.error("Vui lòng điền đầy đủ thông tin bắt buộc!");
      return false;
    }
  
    if (userData.password !== userData.confirmPassword) {
      toast.error("Mật khẩu xác nhận không khớp!");
      return false;
    }
  
    // Thêm validate mật khẩu mạnh
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRegex.test(userData.password)) {
      toast.error("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
      return false;
    }
  
    // Kiểm tra định dạng email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(userData.email)) {
      toast.error("Email không hợp lệ!");
      return false;
    }
  
    // Kiểm tra định dạng số điện thoại nếu có
    if (userData.phone) {
      const phoneRegex = /^[0-9]{10,11}$/;
      if (!phoneRegex.test(userData.phone)) {
        toast.error("Số điện thoại không hợp lệ!");
        return false;
      }
    }
  
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);
    
    const formData = new FormData();
    formData.append("username", userData.username);
    formData.append("password", userData.password);
    formData.append("email", userData.email);
    formData.append("fullName", userData.fullName);
    
    if (userData.phone) {
      formData.append("phone", userData.phone);
    }
    
    if (avatar) {
      formData.append("avatar", avatar);
    }
    
    try {
      console.log("Sending registration request to:", authApi.defaults.baseURL + endpoints.register);
      const response = await authApi.post(endpoints.register, formData);
      console.log("Registration response:", response.data);
      
      toast.success("Đăng ký thành công! Vui lòng đăng nhập.");
      
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (error) {
      console.error("Registration error:", error);
      
      if (error.response && error.response.data) {
        const errorMessage = error.response.data.error || "Đăng ký thất bại, vui lòng thử lại!";
        toast.error(errorMessage);
      } else {
        toast.error("Đăng ký thất bại, vui lòng thử lại!");
      }
      
      setLoading(false);
    }
  };

  return (
    <div className="register-container d-flex align-items-center justify-content-center"
         style={{ 
           minHeight: "100vh", 
           background: "linear-gradient(135deg, #6e8efb, #a777e3)",
           padding: "40px 0" 
         }}>
      <div className="card shadow-lg p-4" style={{ width: "600px", maxWidth: "90%" }}>
        <h2 className="text-center mb-4">Đăng ký tài khoản</h2>
        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-6 mb-3">
              <label htmlFor="username" className="form-label">Tên đăng nhập <span className="text-danger">*</span></label>
              <input 
                type="text" 
                className="form-control" 
                id="username" 
                name="username"
                value={userData.username}
                onChange={handleChange}
                disabled={loading}
              />
            </div>
            <div className="col-md-6 mb-3">
              <label htmlFor="fullName" className="form-label">Họ và tên <span className="text-danger">*</span></label>
              <input 
                type="text" 
                className="form-control" 
                id="fullName" 
                name="fullName"
                value={userData.fullName}
                onChange={handleChange}
                disabled={loading}
              />
            </div>
          </div>

          <div className="row">
          <div className="col-md-6 mb-3">
            <label htmlFor="password" className="form-label">Mật khẩu <span className="text-danger">*</span></label>
            <input 
              type="password" 
              className="form-control" 
              id="password" 
              name="password"
              value={userData.password}
              onChange={handleChange}
              disabled={loading}
            />
            <small className="form-text text-muted">
              Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&)
            </small>
          </div>
            <div className="col-md-6 mb-3">
              <label htmlFor="confirmPassword" className="form-label">Xác nhận mật khẩu <span className="text-danger">*</span></label>
              <input 
                type="password" 
                className="form-control" 
                id="confirmPassword" 
                name="confirmPassword"
                value={userData.confirmPassword}
                onChange={handleChange}
                disabled={loading}
              />
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label htmlFor="email" className="form-label">Email <span className="text-danger">*</span></label>
              <input 
                type="email" 
                className="form-control" 
                id="email" 
                name="email"
                value={userData.email}
                onChange={handleChange}
                disabled={loading}
              />
            </div>
            <div className="col-md-6 mb-3">
              <label htmlFor="phone" className="form-label">Số điện thoại</label>
              <input 
                type="tel" 
                className="form-control" 
                id="phone" 
                name="phone"
                value={userData.phone}
                onChange={handleChange}
                disabled={loading}
              />
            </div>
          </div>

          <div className="mb-3">
            <label htmlFor="avatar" className="form-label">Ảnh đại diện</label>
            <input 
              type="file" 
              className="form-control" 
              id="avatar" 
              accept="image/*"
              onChange={handleAvatarChange}
              disabled={loading}
            />
            {avatarPreview && (
              <div className="mt-2 text-center">
                <img 
                  src={avatarPreview} 
                  alt="Avatar Preview" 
                  style={{ width: '100px', height: '100px', objectFit: 'cover', borderRadius: '50%' }} 
                />
              </div>
            )}
          </div>

          <div className="d-grid gap-2 mt-4">
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
              ) : 'Đăng ký'}
            </button>
          </div>
          <div className="mt-3 text-center">
            <p>Đã có tài khoản? <a href="/login">Đăng nhập</a></p>
          </div>
        </form>
      </div>
      <ToastContainer position="top-right" autoClose={2000} />
    </div>
  );
};

export default Register;