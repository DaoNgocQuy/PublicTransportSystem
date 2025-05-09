import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import MapLeaflet from './Map/MapLeaflet';
import { UserContext } from '../configs/MyContexts';
import cookie from 'react-cookies';
import { authApi } from '../configs/Apis';
import './Home.css';

const Home = () => {
    const [routes, setRoutes] = useState([]);
    const [selectedRoute, setSelectedRoute] = useState(null);
    const [routeStops, setRouteStops] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const user = useContext(UserContext);

    useEffect(() => {
        // Kiểm tra đăng nhập bằng Context hoặc cookie/localStorage
        const token = cookie.load('token');
        const isLoggedIn = user || token || localStorage.getItem('isLoggedIn') === 'true';

        if (!isLoggedIn) {
            navigate('/login');
            return;
        }

        fetchRoutes();
    }, [navigate, user]);

    // Thêm vào component Home

    const fetchRoutes = async () => {
        setLoading(true);
        try {
            // Sử dụng authApi từ configs/Apis.js để tự động gửi token
            const response = await authApi.get('/api/routes');

            console.log('API Response:', response);

            // Xử lý response nếu là JSON hợp lệ
            if (Array.isArray(response.data)) {
                setRoutes(response.data);
            } else {
                console.error('Data is not an array:', response.data);
                setError('Định dạng dữ liệu không hợp lệ');
                setRoutes([]);
            }
        } catch (err) {
            console.error('Error fetching routes:', err);

            // Kiểm tra nếu là lỗi phiên hết hạn
            if (err.isSessionExpired || (err.response && err.response.status === 401)) {
                setError('Phiên làm việc đã hết hạn. Vui lòng làm mới phiên hoặc đăng nhập lại.');

                // Thêm UI để người dùng có thể làm mới phiên hoặc đăng nhập lại
                // Không tự động chuyển hướng
            } else {
                setError(err.message || 'Không thể tải danh sách tuyến');
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!selectedRoute) return;

        const fetchStops = async () => {
            setLoading(true);
            try {
                const response = await axios.get(`http://localhost:8080/PTS/api/stops/route/${selectedRoute.id}`);
                setRouteStops(response.data);
            } catch (err) {
                console.error('Error fetching stops:', err);
                setError(err.message || 'Không thể tải danh sách điểm dừng');
            } finally {
                setLoading(false);
            }
        };

        fetchStops();
    }, [selectedRoute]);

    const handleRouteSelect = (route) => {
        setSelectedRoute(route);
    };

    return (
        <div className="home-container">
            {/* Hiển thị lỗi nếu có */}
            {error && <div className="error-message">{error}</div>}

            <div className="app-container">
                <div className="sidebar">
                    <div className="tabs">
                        <button className="tab active">TRA CỨU</button>
                        <button className="tab">TÌM ĐƯỜNG</button>
                    </div>

                    <div className="routes-list">
                        <div className="search-box">
                            <input type="text" placeholder="Tìm tuyến xe..." />
                        </div>

                        {loading && <p className="loading-text">Đang tải...</p>}

                        <ul className="routes">
                            {Array.isArray(routes) && routes.map((route) => (
                                <li
                                    key={route.id}
                                    className={`route-item ${selectedRoute?.id === route.id ? 'active' : ''}`}
                                    onClick={() => handleRouteSelect(route)}
                                >
                                    <div className="route-icon">
                                        {route.icon && <img src={route.icon} alt="icon" />}
                                    </div>
                                    <div className="route-info">
                                        <h3 className="route-name" style={{ color: route.color || '#4CAF50' }}>
                                            {route.name}
                                        </h3>
                                        <p className="route-path">{route.route}</p>
                                        <div className="route-details">
                                            <span className="route-time">
                                                <i className="far fa-clock"></i> {route.operatingHours}
                                            </span>
                                        </div>
                                    </div>
                                </li>
                            ))}
                        </ul>

                        {!loading && routes.length === 0 && (
                            <p className="no-data">Không có tuyến nào.</p>
                        )}
                    </div>
                </div>

                <div className="map-container">
                    <MapLeaflet busStops={routeStops} selectedRoute={selectedRoute} />

                    {loading && (
                        <div className="loading-overlay">
                            <div className="spinner"></div>
                            <p>Đang tải dữ liệu...</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Home;