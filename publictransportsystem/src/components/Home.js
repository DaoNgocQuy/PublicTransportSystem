import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';

import axios from 'axios';
import busIcon from '../assets/icons/bus.png';
import metroIcon from '../assets/icons/metro.png';
import MapLeaflet from './Map/MapLeaflet';
import RouteSearch from './RoutesList/RouteSearch'; // Import component tìm kiếm
import { UserContext } from '../configs/MyContexts';
import cookie from 'react-cookies';
import { authApi } from '../configs/Apis';
import { toast } from 'react-toastify';
import './Home.css';

const Home = () => {
    const [routes, setRoutes] = useState([]);
    const [selectedRoute, setSelectedRoute] = useState(null);
    const [routeStops, setRouteStops] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    // Thêm 2 state mới cho yêu thích và thông báo
    const [favoriteRoutes, setFavoriteRoutes] = useState([]);
    const [notificationEnabled, setNotificationEnabled] = useState({});
    const navigate = useNavigate();
    const user = useContext(UserContext);
    const [activeTab, setActiveTab] = useState('lookup'); // 'lookup' hoặc 'search'
    const [tripDirection, setTripDirection] = useState('outbound'); // 'outbound' hoặc 'return'
    const [focusedStopId, setFocusedStopId] = useState(null);
    const handleStopClick = (stopId) => {
        setFocusedStopId(stopId);
    };

    useEffect(() => {
        // Kiểm tra đăng nhập
        const token = cookie.load('token');
        const isLoggedIn = user || token || sessionStorage.getItem('isLoggedIn') === 'true';
        if (!isLoggedIn) {
            navigate('/login');
            return;
        }

        fetchRoutes();
        // Thêm gọi 2 hàm mới
        fetchFavorites();
        fetchNotificationSettings();
    }, [navigate, user]);


    const fetchRoutes = async () => {
        setLoading(true);
        try {
            const response = await authApi.get('/api/routes');
            if (Array.isArray(response.data)) {
                setRoutes(response.data);
            } else {
                console.error('Data is not an array:', response.data);
                setError('Định dạng dữ liệu không hợp lệ');
                setRoutes([]);
            }
        } catch (err) {
            console.error('Error fetching routes:', err);
            if (err.isSessionExpired || (err.response && err.response.status === 401)) {
                setError('Phiên làm việc đã hết hạn. Vui lòng làm mới phiên hoặc đăng nhập lại.');
            } else {
                setError(err.message || 'Không thể tải danh sách tuyến');
            }
        } finally {
            setLoading(false);
        }
    };
    const [allBusStops, setAllBusStops] = useState([]); // Add this

    useEffect(() => {
        // Fetch all bus stops when the component mounts
        const fetchAllStops = async () => {
            try {
                const response = await authApi.get('/api/stops'); // Use authApi for consistency
                if (Array.isArray(response.data)) {
                    setAllBusStops(response.data); // Update the correct state
                }
            } catch (error) {
                console.error("Error fetching all bus stops:", error);
            }
        };


        fetchAllStops();
    }, []);

    // Thêm 2 hàm mới để fetch dữ liệu yêu thích và thông báo
    const fetchFavorites = async () => {
        try {
            const response = await authApi.get('/api/favorites');
            if (Array.isArray(response.data)) {
                const favoriteIds = response.data.map(fav => fav.route_id);
                setFavoriteRoutes(favoriteIds);
            }
        } catch (err) {
            console.error('Error fetching favorites:', err);
        }
    };

    const fetchNotificationSettings = async () => {
        try {
            const response = await authApi.get('/api/notifications/settings');
            if (Array.isArray(response.data)) {
                const settingsObj = {};
                response.data.forEach(setting => {
                    settingsObj[setting.route_id] = true;
                });
                setNotificationEnabled(settingsObj);
            }
        } catch (err) {
            console.error('Error fetching notification settings:', err);
        }
    };

    // Thêm 2 hàm xử lý toggle
    const toggleFavorite = async (event, routeId) => {
        event.stopPropagation();
        
        const userStr = sessionStorage.getItem('user');
        if (!userStr) {
            console.error('User not logged in');
            toast.error('Vui lòng đăng nhập để sử dụng tính năng này');
            navigate('/login');
            return;
        }

        try {
            console.log("Toggling favorite for route:", routeId);
            
            if (favoriteRoutes.includes(routeId)) {
                console.log("Removing from favorites");
                const response = await authApi.delete(`/api/favorites/${routeId}`);
                console.log("Server response:", response.data);
                
                if (response.status === 200) {
                    setFavoriteRoutes(favoriteRoutes.filter(id => id !== routeId));
                    toast.success('Đã xóa khỏi danh sách yêu thích');
                }
            } else {
                console.log("Adding to favorites");
                const response = await authApi.post('/api/favorites', { route_id: routeId });
                console.log("Server response:", response.data);
                
                if (response.status === 201) {
                    setFavoriteRoutes([...favoriteRoutes, routeId]);
                    toast.success('Đã thêm vào danh sách yêu thích');
                }
            }
        } catch (err) {
            console.error('Error toggling favorite:', err);
            if (err.response) {
                console.log("Server response:", err.response.data);
                toast.error(`Lỗi: ${err.response.data.error || 'Không thể cập nhật danh sách yêu thích'}`);
            } else {
                toast.error('Không thể kết nối đến server');
            }
        }
    };

    const toggleNotification = async (event, routeId) => {
        event.stopPropagation();
        
        try {
            console.log("Toggling notification for route:", routeId);
            
            const isCurrentlyEnabled = notificationEnabled[routeId];
            
            if (isCurrentlyEnabled) {
                console.log("Disabling notifications");
                await authApi.delete(`/api/notifications/settings/${routeId}`);
                setNotificationEnabled({...notificationEnabled, [routeId]: false});
                toast.info('Đã tắt thông báo cho tuyến này');
            } else {
                console.log("Enabling notifications");
                await authApi.post('/api/notifications/settings', { 
                    route_id: routeId,
                    notify_schedule_changes: true,
                    notify_delays: true 
                });
                setNotificationEnabled({...notificationEnabled, [routeId]: true});
                toast.success('Đã bật thông báo cho tuyến này');
            }
        } catch (err) {
            console.error('Error toggling notification:', err);
            if (err.response) {
                console.log("Server response:", err.response.data);
            }
            toast.error('Không thể cập nhật cài đặt thông báo');
        }
    };

    // Giữ nguyên useEffect và handleRouteSelect
    useEffect(() => {
        if (!selectedRoute) return;

        const fetchStops = async () => {
            setLoading(true);
            try {
                // Fetch stops based on selected direction
                const direction = tripDirection === 'outbound' ? 'outbound' : 'return';
                const response = await authApi.get(`/api/stops/route/${selectedRoute.id}?direction=${direction}`);

                if (Array.isArray(response.data)) {
                    setRouteStops(response.data);
                    // After loading new stops for the new direction, reset any focused stop
                    setFocusedStopId(null);
                }
            } catch (err) {
                console.error('Error fetching stops:', err);
                setError(err.message || 'Không thể tải danh sách điểm dừng');
                setRouteStops([]); // Clear stops on error
            } finally {
                setLoading(false);
            }
        };

        fetchStops();
    }, [selectedRoute, tripDirection]);

    const handleRouteSelect = (route) => {
        setSelectedRoute(route);
        setTripDirection('outbound'); // Reset to outbound when selecting a new route
    };

    // Switch between outbound and return trips
    const switchDirection = (direction) => {
        setTripDirection(direction);
        setFocusedStopId(null); // Reset focused stop when switching direction
    };

    // Xử lý chuyển tab
    const switchTab = (tab) => {
        setActiveTab(tab);
    };


    // Xử lý tìm tuyến từ component RouteSearch
    const handleRouteFound = (route) => {
        setSelectedRoute(route);
        setTripDirection('outbound'); // Reset to outbound when finding a new route
    };

    // Lọc tuyến khi tìm kiếm trong tab TRA CỨU
    const [searchQuery, setSearchQuery] = useState('');

    const filteredRoutes = routes.filter(route =>
        route.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        route.route?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        route.startPoint?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        route.endPoint?.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="home-container">
            {/* Hiển thị lỗi nếu có */}
            {error &&
                <div className="error-message">
                    <span>{error}</span>
                    <button onClick={fetchRoutes}>Thử lại</button>
                </div>
            }

            <div className="app-container">
                <div className="sidebar">
                    {/* Only show tabs when no route is selected */}
                    {!selectedRoute && (
                        <div className="tabs">
                            <button
                                className={`tab ${activeTab === 'lookup' ? 'active' : ''}`}
                                onClick={() => switchTab('lookup')}
                            >
                                TRA CỨU
                            </button>
                            <button
                                className={`tab ${activeTab === 'search' ? 'active' : ''}`}
                                onClick={() => switchTab('search')}
                            >
                                TÌM ĐƯỜNG
                            </button>
                        </div>
                    )}

                    {activeTab === 'lookup' ? (
                        // Tab TRA CỨU
                        <div className="routes-list">
                            {/* Only show search box when no route is selected */}
                            {!selectedRoute && (
                                <div className="search-box">
                                    <input
                                        type="text"
                                        placeholder="Tìm tuyến xe..."
                                        value={searchQuery}
                                        onChange={(e) => setSearchQuery(e.target.value)}
                                    />
                                </div>
                            )}

                            {loading && !selectedRoute && <p className="loading-text">Đang tải...</p>}

                            {/* Display route details when a route is selected */}
                            {selectedRoute ? (
                                <div className="route-details-panel">
                                    <div className="route-header">
                                        <button
                                            className="back-button"
                                            onClick={() => setSelectedRoute(null)}
                                        >
                                            ← Quay lại
                                        </button>
                                        <h3 className="selected-route-name" style={{ color: selectedRoute.color || '#4CAF50' }}>
                                            {selectedRoute.name}

//                         {loading && <p className="loading-text">Đang tải...</p>}

//                         <ul className="routes">
//                             {Array.isArray(routes) && routes.map((route) => (
//                                 <li
//                                     key={route.id}
//                                     className={`route-item ${selectedRoute?.id === route.id ? 'active' : ''}`}
//                                     onClick={() => handleRouteSelect(route)}
//                                 >
//                                     <div className="route-icon">
//                                         {route.name && route.name.toLowerCase().includes('metro') ? (
//                                             <img src={metroIcon} alt="metro" />
//                                         ) : (
//                                             <img src={busIcon} alt="bus" />
//                                         )}
//                                     </div>
//                                     <div className="route-info">
//                                         <h3 className="route-name" style={{ color: route.color || '#4CAF50' }}>
//                                             {route.name}

                                        </h3>
                                        <p className="selected-route-path">{selectedRoute.route}</p>
                                    </div>

                                    <div className="direction-tabs">
                                        <button
                                            className={`direction-tab ${tripDirection === 'outbound' ? 'active' : ''}`}
                                            onClick={() => switchDirection('outbound')}
                                        >
                                            Lượt đi
                                        </button>
                                        <button
                                            className={`direction-tab ${tripDirection === 'return' ? 'active' : ''}`}
                                            onClick={() => switchDirection('return')}
                                        >
                                            Lượt về
                                        </button>
                                    </div>

                                    <div className="direction-info">
                                        <p className="direction-title">
                                            {tripDirection === 'outbound' ? (
                                                <>Từ <b>{selectedRoute.startPoint}</b> đến <b>{selectedRoute.endPoint}</b></>
                                            ) : (
                                                <>Từ <b>{selectedRoute.endPoint}</b> đến <b>{selectedRoute.startPoint}</b></>
                                            )}
                                        </p>
                                    </div>

                                    <h4 className="stops-title">Các trạm dừng:</h4>

                                    {loading ? (
                                        <p className="loading-text">Đang tải điểm dừng...</p>
                                    ) : (
                                        <ul className="stops-list">
                                            {routeStops.map((stop, index) => (
                                                <li
                                                    key={stop.id}
                                                    className="stop-item"
                                                    onClick={() => handleStopClick(stop.id)}  // Add onClick handler here
                                                >
                                                    <div className="stop-number">{index + 1}</div>
                                                    <div className="stop-info">
                                                        <h4 className="stop-name">{stop.name}</h4>
                                                        <p className="stop-address">{stop.address}</p>
                                                    </div>
                                                </li>
                                            ))}
                                        </ul>
                                    )}

                                    {!loading && routeStops.length === 0 && (
                                        <p className="no-data">Không có thông tin điểm dừng.</p>
                                    )}
                                </div>
                            ) : (
                                <ul className="routes">
                                    {Array.isArray(filteredRoutes) && filteredRoutes.map((route) => (
                                        <li
                                            key={route.id}
                                            className="route-item"
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
                            )}

                            {!loading && !selectedRoute && filteredRoutes.length === 0 && (
                                <p className="no-data">Không có tuyến nào phù hợp.</p>
                            )}
                        </div>
                    ) : (
                        // Tab TÌM ĐƯỜNG
                        <RouteSearch onRouteFound={handleRouteFound} />
                    )}

                                    
                                    {/* Thêm nút yêu thích */}
//                                     <button 
//                                         className={`favorite-btn ${favoriteRoutes.includes(route.id) ? 'favorite' : ''}`}
//                                         onClick={(e) => toggleFavorite(e, route.id)}
//                                         title={favoriteRoutes.includes(route.id) ? "Bỏ yêu thích" : "Thêm vào yêu thích"}
//                                     >
//                                         <i className={favoriteRoutes.includes(route.id) ? "fas fa-heart" : "far fa-heart"}></i>
//                                     </button>
                                    
//                                     {/* Thêm nút thông báo */}
//                                     <button 
//                                         className={`notification-btn ${notificationEnabled[route.id] ? 'enabled' : ''}`}
//                                         onClick={(e) => toggleNotification(e, route.id)}
//                                         title={notificationEnabled[route.id] ? "Tắt thông báo" : "Bật thông báo"}
//                                     >
//                                         <i className={notificationEnabled[route.id] ? "fas fa-bell" : "far fa-bell"}></i>
//                                     </button>
                                </li>
                            ))}
                        </ul>

                        {!loading && routes.length === 0 && (
                            <p className="no-data">Không có tuyến nào.</p>
                        )}
                    </div>
                </div>

                <div className="map-container">
                    <MapLeaflet
                        busStops={routeStops}
                        allStops={allBusStops}
                        selectedRoute={selectedRoute}
                        tripDirection={tripDirection}
                        focusedStopId={focusedStopId}

                    />

                    {loading && selectedRoute && (
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