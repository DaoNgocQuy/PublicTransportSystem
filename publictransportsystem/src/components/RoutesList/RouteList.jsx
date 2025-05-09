// src/components/RoutesList/RoutesList.jsx
import React, { useState, useEffect } from 'react';
import { getRoutes } from '../../services/routeService';
import './RoutesList.css';

const RoutesList = ({ onRouteSelect }) => {
    const [routes, setRoutes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        const fetchRoutes = async () => {
            try {
                setLoading(true);
                // Delay để giảm số lượng request khi người dùng đang gõ
                const timeoutId = setTimeout(async () => {
                    const data = await getRoutes(searchTerm);
                    setRoutes(data);
                    setError(null);
                    setLoading(false);
                }, 300);

                return () => clearTimeout(timeoutId);
            } catch (err) {
                setError('Không thể tải danh sách tuyến');
                setLoading(false);
            }
        };

        fetchRoutes();
    }, [searchTerm]);

    return (
        <div className="routes-list-container">
            <div className="search-box">
                <input
                    type="text"
                    placeholder="Tìm tuyến xe"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </div>

            {loading && <div className="loading">Đang tải...</div>}
            {error && <div className="error">{error}</div>}

            <ul className="routes-list">
                {routes.map(route => (
                    <li
                        key={route.id}
                        className="route-item"
                        onClick={() => onRouteSelect(route)}
                    >
                        <div className="route-icon">
                            {/* Icon tùy thuộc vào loại tuyến */}
                            {route.icon && <img src={route.icon} alt="route icon" />}
                        </div>
                        <div className="route-info">
                            <h3 className="route-name" style={{ color: route.color || '#4CAF50' }}>
                                {route.name}
                            </h3>
                            <p className="route-path">{route.route}</p>
                            <div className="route-details">
                                <span className="time-info">
                                    <i className="far fa-clock"></i> {route.operatingHours}
                                </span>
                            </div>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default RoutesList;