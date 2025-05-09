// src/components/RoutesList/RoutesList.jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './css/RoutesList.css';

// Các icons cho loại phương tiện
import BusIcon from '../../assets/icons/bus.svg';
import MetroIcon from '../../assets/icons/metro.svg';
import FerryIcon from '../../assets/icons/ferry.svg';
import TramIcon from '../../assets/icons/tram.svg';

const RoutesList = ({ onSelectRoute }) => {
    const [routes, setRoutes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        const fetchRoutes = async () => {
            try {
                setLoading(true);
                // Gọi API từ backend của bạn
                const response = await axios.get('http://localhost:8080/PTS/api/routes');
                setRoutes(response.data);
                setLoading(false);
            } catch (err) {
                setError('Không thể tải danh sách tuyến');
                setLoading(false);
                console.error(err);
            }
        };

        fetchRoutes();
    }, []);

    // Lọc tuyến theo tên
    const filteredRoutes = routes.filter(route =>
        route.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    // Lấy icon phù hợp cho từng loại phương tiện
    const getTransportIcon = (route) => {
        if (!route.icon) return BusIcon;

        const iconName = route.icon.toLowerCase();
        if (iconName.includes('metro')) return MetroIcon;
        if (iconName.includes('ferry')) return FerryIcon;
        if (iconName.includes('tram')) return TramIcon;
        return BusIcon;
    };

    // Xử lý khi click vào một tuyến
    const handleRouteClick = (route) => {
        if (onSelectRoute) onSelectRoute(route);
    };

    return (
        <div className="routes-list-container">
            <div className="search-input-container">
                <input
                    type="text"
                    className="search-input"
                    placeholder="Tìm tuyến xe"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </div>

            {loading && <div className="loading">Đang tải dữ liệu...</div>}
            {error && <div className="error">{error}</div>}

            <div className="routes-list">
                {filteredRoutes.map(route => (
                    <div
                        key={route.id}
                        className="route-item"
                        onClick={() => handleRouteClick(route)}
                    >
                        <div className="route-icon">
                            <img src={getTransportIcon(route)} alt={route.name} />
                        </div>
                        <div className="route-info">
                            <div className="route-name" style={{ color: route.color || '#4CAF50' }}>
                                {route.name}
                            </div>
                            <div className="route-path">{route.route}</div>
                            <div className="route-details">
                                <div className="operating-hours">
                                    <i className="far fa-clock"></i>
                                    <span>{route.operatingHours}</span>
                                </div>
                                {/* Nếu có thông tin giá vé */}
                                {route.price && (
                                    <div className="price">
                                        <i className="fas fa-ticket-alt"></i>
                                        <span>{route.formattedPrice}</span>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                ))}

                {filteredRoutes.length === 0 && !loading && (
                    <div className="no-routes">
                        Không tìm thấy tuyến nào phù hợp
                    </div>
                )}
            </div>
        </div>
    );
};

export default RoutesList;