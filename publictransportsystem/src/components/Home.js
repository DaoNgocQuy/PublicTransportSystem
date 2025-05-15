import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import MapLeaflet from './Map/MapLeaflet';
import RouteSearch from './RoutesList/RouteSearch';
import { UserContext } from '../configs/MyContexts';
import cookie from 'react-cookies';
import RouteDetail from './RoutesList/RouteDetail';
import { authApi } from '../configs/Apis';
import { toast } from 'react-toastify';
import busIcon from '../assets/icons/bus.png';
import metroIcon from '../assets/icons/metro.png';
import './Home.css';


const Home = () => {
    const [landmarks, setLandmarks] = useState([]);

    const [routes, setRoutes] = useState([]);
    const [selectedRoute, setSelectedRoute] = useState(null);
    const [routeStops, setRouteStops] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [favoriteRoutes, setFavoriteRoutes] = useState([]);
    const [notificationEnabled, setNotificationEnabled] = useState({});
    const navigate = useNavigate();
    const user = useContext(UserContext);
    const [activeTab, setActiveTab] = useState('lookup');
    const [tripDirection, setTripDirection] = useState('outbound');
    const [focusedStopId, setFocusedStopId] = useState(null);
    const [allBusStops, setAllBusStops] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedMapLocation, setSelectedMapLocation] = useState(null);
    const [mapSelectionType, setMapSelectionType] = useState('destination'); // Thêm dòng này
    const handleStopClick = (stopId) => {
        setFocusedStopId(stopId);
    };
    const handleMapLocationSelect = (locationData) => {
        setSelectedMapLocation(locationData);
        // We'll pass this to RouteSearch, which will handle differently based on locationType
    };
    const handleMapSelectionTypeChange = (type) => {
        console.log("Thay đổi loại chọn trên bản đồ:", type);
        setMapSelectionType(type);  // Đây là state đã khai báo ở Home.js
    };

    useEffect(() => {
        const token = cookie.load('token');
        const isLoggedIn = user || token || sessionStorage.getItem('isLoggedIn') === 'true';
        if (!isLoggedIn) {
            navigate('/login');
            return;
        }

        fetchRoutes();
        fetchFavorites();
        fetchNotificationSettings();
        fetchLandmarks();
    }, [navigate, user]);
    const fetchLandmarks = async () => {
        try {
            const response = await authApi.get('/api/landmarks');
            if (Array.isArray(response.data)) {
                setLandmarks(response.data);
            }
        } catch (err) {
            console.error('Error fetching landmarks:', err);
        }
    };
    useEffect(() => {
        // Fetch all bus stops when the component mounts
        const fetchAllStops = async () => {
            try {
                const response = await authApi.get('/api/stops');
                if (Array.isArray(response.data)) {
                    setAllBusStops(response.data);
                }
            } catch (error) {
                console.error("Error fetching all bus stops:", error);
            }
        };

        fetchAllStops();
    }, []);

    useEffect(() => {
    // Kiểm tra xem có tuyến được chọn từ trang Userinfo không
    const selectedRouteId = sessionStorage.getItem('selectedRouteId');
    
    if (selectedRouteId && selectedRouteId !== 'processed') {
        console.log("Found selectedRouteId:", selectedRouteId);
        
        // Đánh dấu đã xử lý để tránh xử lý lặp lại
        sessionStorage.setItem('selectedRouteId', 'processed');
        
        // Tìm tuyến đã chọn trong danh sách routes
        const routeId = parseInt(selectedRouteId);
        const foundRoute = routes.find(route => route.id === routeId);
        
        if (foundRoute) {
        console.log("Found route in cached data:", foundRoute);
        setSelectedRoute(foundRoute);
        setTripDirection('outbound');
        } else {
        console.log("Route not found in cached data, fetching from API");
        // Lấy thông tin từ API
        const fetchSelectedRoute = async () => {
            try {
            setLoading(true);
            const response = await authApi.get(`/api/routes/${routeId}`);
            console.log("API response for route:", response.data);
            
            if (response.data) {
                setSelectedRoute(response.data);
                setTripDirection('outbound');
                // Sau khi lấy thông tin tuyến, cần lấy thông tin các điểm dừng
                try {
                const stopsResponse = await authApi.get(`/api/stops/route/${routeId}?direction=outbound`);
                if (Array.isArray(stopsResponse.data)) {
                    setRouteStops(stopsResponse.data);
                }
                } catch (error) {
                console.error("Error fetching stops for selected route:", error);
                }
            }
            } catch (error) {
            console.error("Error fetching selected route:", error);
            toast.error("Không thể tải thông tin tuyến");
            // Xóa để có thể thử lại
            sessionStorage.removeItem('selectedRouteId');
            } finally {
            setLoading(false);
            }
        };
        
        // Gọi hàm fetch
        fetchSelectedRoute();
        }
    }
    }, [routes]); // Thêm routes vào dependencies

    useEffect(() => {
        return () => {
            // Khi component unmount, xóa selectedRouteId nếu đã được đánh dấu là processed
            if (sessionStorage.getItem('selectedRouteId') === 'processed') {
            sessionStorage.removeItem('selectedRouteId');
            }
        };
    }, []);

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
                setNotificationEnabled({ ...notificationEnabled, [routeId]: false });
                toast.info('Đã tắt thông báo cho tuyến này');
            } else {
                console.log("Enabling notifications");
                await authApi.post('/api/notifications/settings', {
                    route_id: routeId,
                    notify_schedule_changes: true,
                    notify_delays: true
                });
                setNotificationEnabled({ ...notificationEnabled, [routeId]: true });
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


    // useEffect và handleRouteSelect
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
        setTripDirection('outbound');
        // Reset to outbound when selecting a new route
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
                    <div className="tabs">
                        <button
                            className={`tab ${activeTab === 'lookup' ? 'active' : ''}`}
                            onClick={() => {
                                switchTab('lookup');
                                // Nếu đang ở tab tìm đường và chọn tab tra cứu, reset selectedRoute
                                if (activeTab === 'search') {
                                    setSelectedRoute(null);
                                }
                            }}
                        >
                            TRA CỨU
                        </button>
                        <button
                            className={`tab ${activeTab === 'search' ? 'active' : ''}`}
                            onClick={() => {
                                switchTab('search');
                                // Khi chuyển từ tab tra cứu sang tab tìm đường, reset selectedRoute
                                setSelectedRoute(null);
                            }}
                        >
                            TÌM ĐƯỜNG
                        </button>
                    </div>

                    {activeTab === 'lookup' ? (
                        <div className="routes-list">
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

                            {selectedRoute ? (
                                <RouteDetail
                                    route={selectedRoute}
                                    onBack={() => setSelectedRoute(null)}
                                    favoriteRoutes={favoriteRoutes}
                                    notificationEnabled={notificationEnabled}
                                    toggleFavorite={toggleFavorite}
                                    toggleNotification={toggleNotification}
                                    tripDirection={tripDirection}
                                    switchDirection={switchDirection}
                                    focusedStopId={focusedStopId}
                                    handleStopClick={handleStopClick}
                                />
                            ) : (
                                <>
                                    <ul className="routes">
                                        {Array.isArray(filteredRoutes) && filteredRoutes.map((route) => (
                                            <li
                                                key={route.id}
                                                className="route-item"
                                                onClick={() => handleRouteSelect(route)}
                                            >
                                                <div className="route-icon">
                                                    {route.name && route.name.toLowerCase().includes('metro') ? (
                                                        <img src={metroIcon} alt="metro" />
                                                    ) : (
                                                        <img src={busIcon} alt="bus" />
                                                    )}
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

                                                {/* Fixed favorite button */}
                                                <button
                                                    className={`favorite-btn ${favoriteRoutes.includes(route.id) ? 'favorite' : ''}`}
                                                    onClick={(e) => toggleFavorite(e, route.id)}
                                                    title={favoriteRoutes.includes(route.id) ? "Bỏ yêu thích" : "Thêm vào yêu thích"}
                                                >
                                                    <i className={favoriteRoutes.includes(route.id) ? "fas fa-heart" : "far fa-heart"}></i>
                                                </button>

                                                {/* Fixed notification button */}
                                                <button
                                                    className={`notification-btn ${notificationEnabled[route.id] ? 'enabled' : ''}`}
                                                    onClick={(e) => toggleNotification(e, route.id)}
                                                    title={notificationEnabled[route.id] ? "Tắt thông báo" : "Bật thông báo"}
                                                >
                                                    <i className={notificationEnabled[route.id] ? "fas fa-bell" : "far fa-bell"}></i>
                                                </button>
                                            </li>
                                        ))}
                                    </ul>

                                    {!loading && filteredRoutes.length === 0 && (
                                        <p className="no-data">Không có tuyến nào phù hợp.</p>
                                    )}
                                </>
                            )}
                        </div>
                    ) : (
                        <RouteSearch
                            onRouteFound={handleRouteFound}
                            selectedMapLocation={selectedMapLocation}
                            onMapSelectionChange={handleMapSelectionTypeChange} />
                    )}
                </div>

                <div className="map-container">
                    <MapLeaflet
                        busStops={routeStops}
                        allStops={allBusStops}
                        selectedRoute={selectedRoute}
                        tripDirection={tripDirection}
                        landmarks={landmarks}
                        onLocationSelect={handleMapLocationSelect}
                        selectionMode={mapSelectionType}
                        activeTab={activeTab}
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