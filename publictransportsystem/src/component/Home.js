import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const Home = () => {
    const navigate = useNavigate();
    
    // Kiểm tra xem người dùng đã đăng nhập hay chưa
    // (Đây chỉ là ví dụ, bạn cần thêm logic thực tế để kiểm tra trạng thái đăng nhập)
    useEffect(() => {
        const isLoggedIn = localStorage.getItem('isLoggedIn');
        if (!isLoggedIn) {
            navigate('/login');
        }
    }, [navigate]);

    return (
        <div className="container mt-4">
            <h1>Chào mừng đến với Hệ thống Giao thông Công cộng</h1>
            <p>Đây là trang chủ của ứng dụng.</p>
        </div>
    );
};

export default Home;