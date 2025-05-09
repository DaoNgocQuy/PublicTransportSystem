// src/pages/HomePage.jsx
import React, { useState, useEffect } from 'react';
import RoutesList from '../components/RoutesList/RoutesList';
import MapLeaflet from '../components/Map/MapLeaflet';
import { getStopsByRouteId } from '../services/stopService';
import './HomePage.css';

const HomePage = () => {
    const [selectedRoute, setSelectedRoute] = useState(null);
    const [routeStops, setRouteStops] = useState([]);
    const [loading, setLoading] = useState(false);

    // Khi chọn tuyến, lấy các điểm dừng của tuyến đó
    useEffect(() => {
        const fetchRouteStops = async () => {
            if (!selectedRoute) {
                setRouteStops([]);
                return;
            }

            try {
                setLoading(true);
                const stops = await getStopsByRouteId(selectedRoute.id);
                setRouteStops(stops);
            } catch (error) {
                console.error('Error fetching route stops:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchRouteStops();
    }, [selectedRoute]);

    // Xử lý khi người dùng chọn tuyến
    const handleRouteSelect = (route) => {
        setSelectedRoute(route);
    };

    return (
        <div className="home-page">
            <header className="app-header">
                <h1>
                    <i className="fas fa-bus"></i> Bản đồ xe buýt
                </h1>
                <div className="header-actions">
                    <button>VI | EN</button>
                    <button>TP Hồ Chí Minh</button>
                </div>
            </header>

            <div className="app-container">
                {/* Sidebar bên trái hiển thị danh sách tuyến */}
                <div className="sidebar">
                    <div className="tabs">
                        <button className="tab active">TRA CỨU</button>
                        <button className="tab">TÌM ĐƯỜNG</button>
                    </div>
                    <RoutesList onRouteSelect={handleRouteSelect} />
                </div>

                {/* Phần bản đồ chính */}
                <div className="map-container">
                    <MapLeaflet busStops={routeStops} selectedRoute={selectedRoute} />

                    {loading && (
                        <div className="loading-overlay">
                            <div className="loading-spinner"></div>
                            <p>Đang tải dữ liệu...</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default HomePage;