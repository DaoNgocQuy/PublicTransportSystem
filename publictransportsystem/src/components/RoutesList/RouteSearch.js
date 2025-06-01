import React, { useState, useEffect } from 'react';
import { authApi } from '../../configs/Apis';
import { FaMapMarkerAlt, FaExchangeAlt, FaDotCircle, FaFlag, FaLandmark, FaWalking, FaBus, FaClock, FaRoute } from 'react-icons/fa';
import './RouteSearch.css';
import { findRoutes, getRouteDetails, getRouteLegsDetails } from '../../services/routeService';
import RouteItinerary from './RouteItinerary';
import { useNavigate } from 'react-router-dom';
import RouteOption from './RouteOption';
const RouteSearch = ({ onRouteFound, selectedMapLocation, onMapSelectionChange }) => {
    const [origin, setOrigin] = useState('');
    const [destination, setDestination] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [originSuggestions, setOriginSuggestions] = useState([]);
    const [destSuggestions, setDestSuggestions] = useState([]);
    const [searchResults, setSearchResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showOriginSuggestions, setShowOriginSuggestions] = useState(false);
    const [showDestSuggestions, setShowDestSuggestions] = useState(false);
    const [activeRouteCount, setActiveRouteCount] = useState(1); // Tuyến hoạt động (mặc định là 1)
    const [landmark, setLandmarks] = useState([]);
    const [mapSelectionActive, setMapSelectionActive] = useState(false);
    const [mapSelectionType, setMapSelectionType] = useState(null);

    const [routeOptions, setRouteOptions] = useState([]);
    const [selectedRouteOption, setSelectedRouteOption] = useState(null);
    const [routePreferences, setRoutePreferences] = useState({
        maxWalkingDistance: 1000,
        maxTransfers: 2,
        routePriority: 'LEAST_TIME'
    });
    const [loadingDetails, setLoadingDetails] = useState(false);
    const [routeDetails, setRouteDetails] = useState(null);

    const navigate = useNavigate();
    const handleBusRouteClick = (routeId) => {
        if (!routeId) return;
        console.log('Bus route clicked with ID:', routeId);
        onRouteFound && onRouteFound({ id: routeId });
    };
    // Thêm hàm xử lý lỗi đăng nhập
    const handleAuthError = () => {
        // Lưu trạng thái tìm kiếm hiện tại
        sessionStorage.setItem('lastSearch', JSON.stringify({
            origin,
            destination
        }));

        // Hiển thị thông báo
        alert('Vui lòng đăng nhập để sử dụng tính năng tìm kiếm tuyến');

        // Sử dụng navigate thay vì history.push
        navigate('/login');
    };
    // Lấy danh sách tất cả các trạm và địa điểm nổi tiếng
    const handleRouteOptionSelect = async (option) => {
        if (selectedRouteOption !== option || !routeDetails) {
            setSelectedRouteOption(option);
            setLoadingDetails(true);
            setRouteDetails(null);

            try {
                const detailedOption = await getRouteLegsDetails(option);
                setRouteDetails(detailedOption);

                // Check if this is a walking-only route
                if (option.walkingOnly) {
                    // Cải thiện cấu trúc đối tượng walking route
                    const walkingRoute = {
                        walkingOnly: true,
                        id: "walking-route",
                        name: "Đi bộ",
                        routeName: "Đường đi bộ",
                        description: `Đi bộ ${(option.totalDistance / 1000).toFixed(1)}km`,
                        legs: option.legs, // Giữ nguyên legs để hiển thị trong RouteItinerary
                        journeySegment: {
                            origin: option.legs[0]?.from,
                            destination: option.legs[0]?.to,
                            // Thêm thông tin khoảng cách và thời gian
                            walkingDistance: option.totalDistance || option.legs[0]?.distance,
                            walkingDuration: option.totalTime || option.legs[0]?.duration
                        },
                        // Thêm phần thông tin để hiển thị tổng quan
                        totalDistance: option.totalDistance,
                        totalTime: option.totalTime
                    };

                    console.log("Created walking route object:", walkingRoute);
                    onRouteFound && onRouteFound(walkingRoute);
                    setLoadingDetails(false);
                    return;
                } else {
                    // Xác định trạm lên và trạm xuống
                    const busLegs = option.legs?.filter(leg => leg.type === 'BUS') || [];

                    if (busLegs.length > 0) {
                        // Lấy thông tin tuyến từ API
                        const routeId = busLegs[0].routeId;
                        const routeDetail = await getRouteDetails(routeId);

                        // Xác định chiều di chuyển dựa vào thứ tự của trạm
                        const boardStopOrder = busLegs[0].boardStopOrder;
                        const alightStopOrder = busLegs[0].alightStopOrder;

                        // Xác định chiều đi (1: chiều đi, 2: chiều về)
                        let direction = 1; // Mặc định là chiều đi

                        // Nếu có thông tin định hướng trong option
                        if (option.direction !== undefined) {
                            direction = option.direction;
                        }
                        // Nếu không, thử xác định từ thứ tự trạm
                        else if (boardStopOrder !== undefined && alightStopOrder !== undefined) {
                            // Nếu trạm lên có thứ tự > trạm xuống, có thể là chiều về
                            if (boardStopOrder > alightStopOrder) {
                                direction = 2;
                            }
                        }

                        // Đóng gói thông tin để truyền lên component cha
                        const enhancedRoute = {
                            ...routeDetail,
                            direction: direction,
                            journeySegment: {
                                routeId: busLegs[0].routeId,
                                boardStop: busLegs[0].boardStop || busLegs[0].from,
                                alightStop: busLegs[0].alightStop || busLegs[0].to,
                                boardStopOrder: busLegs[0].boardStopOrder,
                                alightStopOrder: busLegs[0].alightStopOrder
                            }
                        };

                        onRouteFound(enhancedRoute);
                    }
                }
            } catch (err) {
                console.error('Error loading route details:', err);
                setError('Không thể tải thông tin chi tiết tuyến đường');
            } finally {
                setLoadingDetails(false);
            }
        }
    };



    useEffect(() => {
        const fetchStopsAndLandmarks = async () => {
            try {
                setLoading(true);
                setError(null);
                // Gọi cả API stops và landmarks
                const [stopsResponse] = await Promise.all([
                    authApi.get('/api/stops'),
                ]);

                console.log('API stops response:', stopsResponse);

                // Xử lý kết quả từ API stops
                let allSuggestions = [];

                if (Array.isArray(stopsResponse.data)) {
                    console.log('Got stop suggestions:', stopsResponse.data.length, 'items');

                    // Định dạng các trạm dừng với thẻ "BUS_STOP" - sửa lỗi stop_name
                    const formattedStops = stopsResponse.data.map(stop => {
                        // Đảm bảo các trường cần thiết đều có giá trị
                        return {
                            ...stop,
                            stop_name: stop.name || "Unnamed Stop",
                            address: stop.address || "",
                            latitude: stop.latitude || 0,
                            longitude: stop.longitude || 0,
                            suggestionType: 'BUS_STOP',
                            displayIcon: <FaMapMarkerAlt />
                        };
                    });

                    allSuggestions = [...formattedStops];
                } else {
                    console.error('API stops response is not an array:', stopsResponse.data);
                }
                // Cập nhật state với tất cả các gợi ý
                setSuggestions(allSuggestions);

            } catch (err) {
                console.error('Error fetching stops and landmarks:', err);
                if (err.response) {
                    console.error('Server returned:', err.response.status, err.response.data);
                }
                throw err;
            } finally {
                setLoading(false);
            }
        };

        fetchStopsAndLandmarks();
    }, []);
    useEffect(() => {
        if (selectedMapLocation) {
            const locationData = {
                ...selectedMapLocation,
                displayName: selectedMapLocation.stop_name
            };

            // Make sure we're checking the locationType property correctly
            if (selectedMapLocation.locationType === 'origin') {
                console.log("Setting origin from map selection:", locationData);
                setOrigin(locationData);
                setShowOriginSuggestions(false);
            } else if (selectedMapLocation.locationType === 'destination') {
                console.log("Setting destination from map selection:", locationData);
                setDestination(locationData);
                setShowDestSuggestions(false);
            } else {
                console.warn("Unknown location type:", selectedMapLocation.locationType);
            }
        }
    }, [selectedMapLocation]);
    // Lọc gợi ý khi người dùng gõ
    const handleOriginChange = (e) => {
        const value = e.target.value;
        console.log('Origin input value:', value);
        // Chỉ lưu trữ chuỗi văn bản, không phải đối tượng
        setOrigin(value);

        if (value.length > 1) {
            console.log('Filtering suggestions with:', value);
            const filtered = suggestions.filter(item => {
                if (!item || typeof item !== 'object') return false;

                // Sửa: Kiểm tra cả name và stop_name
                const nameMatch = (item.stop_name && item.stop_name.toLowerCase().includes(value.toLowerCase())) ||
                    (item.name && item.name.toLowerCase().includes(value.toLowerCase()));
                const addressMatch = item.address && item.address.toLowerCase().includes(value.toLowerCase());

                // Các xử lý khác giữ nguyên
                const tagsMatch = item.suggestionType === 'LANDMARK' &&
                    item.landmarkData &&
                    item.landmarkData.tags &&
                    item.landmarkData.tags.toLowerCase().includes(value.toLowerCase());

                return nameMatch || addressMatch || tagsMatch;
            });

            console.log('Filtered suggestions:', filtered);
            setOriginSuggestions(filtered);
            setShowOriginSuggestions(true);
        } else {
            setOriginSuggestions([]);
            setShowOriginSuggestions(false);
        }
    };

    const handleDestinationChange = (e) => {
        const value = e.target.value;
        setDestination(value);

        if (value.length > 1) {
            const filtered = suggestions.filter(item => {
                if (!item || typeof item !== 'object') return false;

                const nameMatch = item.stop_name && item.stop_name.toLowerCase().includes(value.toLowerCase());
                const addressMatch = item.address && item.address.toLowerCase().includes(value.toLowerCase());
                // Nếu là landmark, tìm cả trong tags
                const tagsMatch = item.suggestionType === 'LANDMARK' &&
                    item.landmarkData &&
                    item.landmarkData.tags &&
                    item.landmarkData.tags.toLowerCase().includes(value.toLowerCase());

                return nameMatch || addressMatch || tagsMatch;
            });

            setDestSuggestions(filtered);
            setShowDestSuggestions(true);
        } else {
            setDestSuggestions([]);
            setShowDestSuggestions(false);
        }
    };

    // Xử lý chọn trạm từ gợi ý
    const selectOrigin = (item) => {
        console.log('Selected origin item:', item);

        // Sửa: Sử dụng name nếu stop_name không tồn tại
        setOrigin({
            ...item,
            displayName: item.stop_name || item.name,
            // Ensure these critical properties are always set
            latitude: item.latitude,
            longitude: item.longitude,
            suggestionType: item.suggestionType ||
                (item.landmark_id ? 'LANDMARK' : 'BUS_STOP')
        });
        setShowOriginSuggestions(false);
    };

    const selectDestination = (item) => {
        console.log('Selected destination item:', item);

        // Make sure we preserve all properties and explicitly set the display name
        setDestination({
            ...item,
            displayName: item.stop_name,
            // Ensure these critical properties are always set
            latitude: item.latitude,
            longitude: item.longitude,
            suggestionType: item.suggestionType ||
                (item.landmark_id ? 'LANDMARK' : 'BUS_STOP')
        });
        setShowDestSuggestions(false);
    };

    // Hoán đổi điểm đi và điểm đến
    const swapLocations = () => {
        const tempOrigin = origin;
        setOrigin(destination);
        setDestination(tempOrigin);
    };

    // Tìm kiếm tuyến
    const handleSearch = async () => {
        if (!origin || !destination) {
            setError('Vui lòng nhập điểm đi và điểm đến');
            return;
        }

        const userStr = sessionStorage.getItem('user');
        if (!userStr) {
            handleAuthError();
            return;
        }

        setLoading(true);
        setError(null);
        setRouteOptions([]);
        setSelectedRouteOption(null);
        if (onRouteFound) {
            // Pass null to clear the current route
            onRouteFound(null);
        }
        try {
            const cleanOrigin = formatLocationForSearch(origin);
            const cleanDestination = formatLocationForSearch(destination);

            if (!cleanOrigin.latitude || !cleanOrigin.longitude ||
                !cleanDestination.latitude || !cleanDestination.longitude) {
                setError('Địa điểm không có tọa độ hợp lệ');
                setLoading(false);
                return;
            }

            // Gọi API tìm đường
            const response = await authApi.get('/api/routes/find-journey', {
                params: {
                    fromLat: cleanOrigin.latitude,
                    fromLng: cleanOrigin.longitude,
                    toLat: cleanDestination.latitude,
                    toLng: cleanDestination.longitude,
                    maxWalkDistance: 500,
                    priority: 'LEAST_TIME'
                }
            });

            console.log('Server response:', response.data);

            if (response.data.error) {
                setError(response.data.error);
                setLoading(false);
                return;
            }

            // Xử lý kết quả
            const options = response.data.options;
            if (options && options.length > 0) {
                setRouteOptions(options);

            } else {
                setError('Không tìm thấy tuyến đường phù hợp');
            }
        } catch (err) {
            console.error('Search error:', err);
            setError('Đã xảy ra lỗi khi tìm kiếm');
        } finally {
            setLoading(false);
        }
    };
    const formatLocationForSearch = (location) => {
        console.log('Formatting location data:', location);

        if (!location) return null;

        // Make sure to handle both direct properties and nested properties
        return {
            id: location.id || null,
            stop_name: location.stop_name || location.displayName || 'Vị trí đã chọn',
            latitude: location.latitude ? parseFloat(location.latitude) : null,
            longitude: location.longitude ? parseFloat(location.longitude) : null,
            suggestionType: location.suggestionType ||
                (location.displayIcon && location.displayIcon.type && location.displayIcon.type.name === 'FaLandmark' ? 'LANDMARK' : 'BUS_STOP'),
            landmark_id: location.landmark_id ||
                (location.landmarkData && location.landmarkData.id ? location.landmarkData.id : null)
        };
    };

    return (
        <div className="route-search">
            <div className="search-container">
                <div className="search-header">
                    <div className="location-inputs busmap-style">
                        <div className="locations-wrapper">
                            <div className="locations-container">
                                <div className="search-input-container origin-input">
                                    <div className="input-icon">
                                        <FaDotCircle className="origin-icon" style={{ color: '#4CAF50' }} />
                                    </div>
                                    <input
                                        type="text"
                                        placeholder="Điểm đi..."
                                        value={typeof origin === 'object' ? origin.displayName || origin.stop_name : origin}
                                        onChange={handleOriginChange}
                                        onFocus={() => {
                                            setShowOriginSuggestions(true);
                                            // Signal to parent component that map selection should be for origin
                                            setMapSelectionType('origin');
                                            setMapSelectionActive(true);
                                            // Thêm dòng này để thông báo cho component cha
                                            onMapSelectionChange && onMapSelectionChange('origin');
                                        }}
                                        className="search-input"
                                    />
                                    {showOriginSuggestions && originSuggestions.length > 0 && (
                                        <ul className="suggestion-list origin-suggestions">
                                            {originSuggestions.map((item, index) => (
                                                <li key={index} onClick={() => selectOrigin(item)}>
                                                    <span className="suggestion-icon">
                                                        {item.displayIcon || (item.suggestionType === 'BUS_STOP' ?
                                                            <FaMapMarkerAlt style={{ color: '#4CAF50' }} /> :
                                                            <FaLandmark style={{ color: '#E91E63' }} />)}
                                                    </span>
                                                    <div className="suggestion-info">
                                                        <div className="suggestion-name">{item.stop_name}</div>
                                                        <div className="suggestion-address">{item.address}</div>
                                                    </div>
                                                </li>
                                            ))}
                                        </ul>
                                    )}
                                </div>

                                <div className="journey-line"></div>

                                <div className="search-input-container dest-input">
                                    <div className="input-icon">
                                        <FaFlag className="dest-icon" style={{ color: '#E91E63' }} />
                                    </div>
                                    <input
                                        type="text"
                                        placeholder="Điểm đến..."
                                        value={typeof destination === 'object' ? destination.displayName || destination.stop_name : destination}
                                        onChange={handleDestinationChange}
                                        onFocus={() => {
                                            setShowDestSuggestions(true);
                                            setMapSelectionType('destination');
                                            setMapSelectionActive(true);
                                            onMapSelectionChange && onMapSelectionChange('destination');
                                        }}
                                        className="search-input"
                                    />
                                    {showDestSuggestions && destSuggestions.length > 0 && (
                                        <ul className="suggestion-list dest-suggestions">
                                            {destSuggestions.map((item, index) => (
                                                <li key={index} onClick={() => selectDestination(item)}>
                                                    <span className="suggestion-icon">
                                                        {item.displayIcon || (item.suggestionType === 'BUS_STOP' ?
                                                            <FaMapMarkerAlt style={{ color: '#4CAF50' }} /> :
                                                            <FaLandmark style={{ color: '#E91E63' }} />)}
                                                    </span>
                                                    <div className="suggestion-info">
                                                        <div className="suggestion-name">{item.stop_name}</div>
                                                        <div className="suggestion-address">{item.address}</div>
                                                    </div>
                                                </li>
                                            ))}
                                        </ul>
                                    )}
                                </div>
                            </div>

                            <div className="action-buttons">
                                <button className="swap-button" onClick={swapLocations} title="Hoán đổi điểm đi và điểm đến">
                                    <FaExchangeAlt className="swap-icon-vertical" />
                                </button>
                            </div>
                        </div>

                        <button
                            className="search-button busmap-style"
                            onClick={handleSearch}
                            disabled={loading}
                        >
                            {loading ? (
                                <>
                                    <div className="spinner"></div>
                                    <span>Đang tìm...</span>
                                </>
                            ) : 'Tìm kiếm'}
                        </button>
                    </div>
                </div>

                {error && <div className="error-message">{error}</div>}
                {routeOptions.map((option, index) => (
                    <RouteOption
                        key={index}
                        option={option}
                        isSelected={selectedRouteOption === option}
                        onClick={() => handleRouteOptionSelect(option)}
                    />
                ))}
                {searchResults.length > 0 && (
                    <div className="route-options">
                        <div className="route-count-header">
                            <span>SỐ TUYẾN ĐI TỐI ĐA</span>
                            <div className="route-counter">
                                <span className={`route-option ${activeRouteCount === 1 ? 'active' : ''}`}>1 Tuyến</span>
                                <span className={`route-option ${activeRouteCount === 2 ? 'active' : ''}`}>2 Tuyến</span>
                                <span className={`route-option ${activeRouteCount === 3 ? 'active' : ''}`}>3 Tuyến</span>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            <div className="search-results">
                {searchResults.length > 0 ? (
                    <ul className="routes">
                        {searchResults.map((route) => (
                            <li
                                key={route.id}
                                className="route-item"
                                onClick={() => onRouteFound && onRouteFound(route)}
                            >
                                <div className="route-icon">
                                    {(route.icon && <img src={route.icon} alt="icon" />) ||
                                        <div className="route-number" style={{ backgroundColor: route.route_color || '#4CAF50' }}>
                                            {route.id}
                                        </div>}
                                </div>
                                <div className="route-info">
                                    <h3 className="route-name" style={{ color: route.route_color || '#4CAF50' }}>
                                        {route.name}
                                    </h3>
                                    <p className="route-path">{route.start_location} - {route.end_location}</p>
                                    <div className="route-details">
                                        <span className="route-time">
                                            <i className="far fa-clock"></i> {route.operation_start_time?.substring(0, 5)} - {route.operation_end_time?.substring(0, 5)}
                                        </span>
                                        <span className="route-frequency">
                                            <i className="fas fa-sync-alt"></i> {route.frequency_minutes || '15'} phút/chuyến
                                        </span>
                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                ) : (
                    !loading && origin && destination && routeOptions.length === 0 && (
                        <p className="no-data">Không tìm thấy tuyến đường phù hợp.</p>
                    )
                )}
            </div>
        </div>
    );
};

export default RouteSearch;