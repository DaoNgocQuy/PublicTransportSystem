import React, { useState, useEffect } from 'react';
import axios from 'axios';
import MapLeaflet from './Map/MapLeaflet';
import './Home.css';

const Home = () => {
    const [routes, setRoutes] = useState([]);
    const [selectedRoute, setSelectedRoute] = useState(null);
    const [routeStops, setRouteStops] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchRoutes = async () => {
            setLoading(true);
            try {
                const response = await axios.get('http://localhost:8080/PTS/api/routes');
                console.log('API response:', response); // Debug response
                setRoutes(response.data);
            } catch (err) {
                console.error('Error fetching routes:', err);
                setError(err.message || 'Không thể tải danh sách tuyến');
            } finally {
                setLoading(false);
            }
        };

        fetchRoutes();
    }, []);

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
                            {routes.map((route) => (
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