import React, { useState, useEffect } from 'react';
import { authApi } from '../../configs/Apis';
import { FaMapMarkerAlt, FaExchangeAlt, FaDotCircle, FaFlag, FaLandmark, FaWalking, FaBus, FaClock, FaRoute } from 'react-icons/fa';
import './RouteSearch.css';
import { findRoutes, getRouteDetails, getRouteLegsDetails } from '../../services/routeService';
import RouteItinerary from './RouteItinerary';
import { useNavigate } from 'react-router-dom';
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
    const [landmarks, setLandmarks] = useState([]);
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
            setRouteDetails(null); // Clear previous details

            try {
                // Get detailed information about the selected route option
                const detailedOption = await getRouteLegsDetails(option);
                console.log("Detailed route option received:", detailedOption);
                if (detailedOption && detailedOption.legs) {
                    console.log("Number of legs:", detailedOption.legs.length);
                    detailedOption.legs.forEach((leg, index) => {
                        console.log(`Leg ${index} details:`, leg);
                    });
                }
                setRouteDetails(detailedOption);

                // If we need to display the route on the map
                const busLegs = option.legs?.filter(leg => leg.type === 'BUS') || [];
                if (busLegs.length > 0 && busLegs[0].routeId && onRouteFound) {
                    try {
                        const routeDetail = await getRouteDetails(busLegs[0].routeId);
                        if (routeDetail) {
                            onRouteFound(routeDetail);
                        }
                    } catch (err) {
                        console.error('Error loading route for map display:', err);
                        // Continue showing details even if map route can't be loaded
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
    const showRouteDirections = (routeDetails) => {
        console.log("Route details for directions:", routeDetails);

        if (!routeDetails || !routeDetails.legs || routeDetails.legs.length === 0) {
            console.log("No legs data available");
            return null;
        }

        return (
            <div className="route-directions-container">
                <h3 className="directions-title">Chi tiết cách di chuyển</h3>

                <div className="journey-steps">
                    {routeDetails.legs.map((leg, index) => {
                        // Làm sạch dữ liệu và thêm giá trị mặc định
                        const legType = leg.type?.toUpperCase() || 'UNKNOWN';
                        const distance = leg.distance || 0;
                        const duration = leg.duration || '?';
                        const fromName = leg.from?.name || leg.boardStop?.name || 'Điểm xuất phát';
                        const toName = leg.to?.name || leg.alightStop?.name || 'Điểm đến';

                        return (
                            <div key={index} className={`journey-step journey-step-${legType.toLowerCase()}`}>
                                <div className="step-icon">
                                    {legType === 'WALK' ?
                                        <FaWalking className="walk-icon" /> :
                                        <FaBus className="bus-icon" />}
                                </div>

                                <div className="step-details">
                                    {legType === 'WALK' ? (
                                        <>
                                            <div className="step-title">
                                                <span className="step-type">Đi bộ</span>
                                                <span className="step-distance">{distance}m</span>
                                                <span className="step-time">{duration} phút</span>
                                            </div>
                                            <div className="step-instruction">
                                                Từ {fromName} đến {toName}
                                            </div>
                                        </>
                                    ) : (
                                        <>
                                            <div className="step-title">
                                                <span className="step-type">
                                                    <span className="bus-number" style={{ backgroundColor: leg.routeColor || '#4CAF50' }}>
                                                        {leg.routeNumber || leg.routeId || ''}
                                                    </span>
                                                    {leg.routeName || ''}
                                                </span>
                                                <span className="step-time">{duration} phút</span>
                                            </div>
                                            <div className="step-instruction">
                                                <div>Lên xe tại: {fromName}</div>
                                                <div>Xuống xe tại: {toName}</div>
                                                {leg.stops && <div className="stop-count">{typeof leg.stops === 'number' ? leg.stops : (Array.isArray(leg.stops) ? leg.stops.length : 0)} điểm dừng</div>}
                                            </div>
                                        </>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
        );
    };
    useEffect(() => {
        const fetchStopsAndLandmarks = async () => {
            try {
                setLoading(true);

                // Gọi cả API stops và landmarks
                const [stopsResponse, landmarksResponse] = await Promise.all([
                    authApi.get('/api/stops'),
                    authApi.get('/api/landmarks')
                ]);

                console.log('API stops response:', stopsResponse);
                console.log('API landmarks response:', landmarksResponse);

                // Xử lý kết quả từ API stops
                let allSuggestions = [];

                if (Array.isArray(stopsResponse.data)) {
                    console.log('Got stop suggestions:', stopsResponse.data.length, 'items');
                    if (stopsResponse.data.length === 0) {
                        console.warn('API returned empty stops array');
                    } else {
                        console.log('First stop suggestion:', stopsResponse.data[0]);

                        // Định dạng các trạm dừng với thẻ "BUS_STOP"
                        const formattedStops = stopsResponse.data.map(stop => ({
                            ...stop,
                            suggestionType: 'BUS_STOP',
                            displayIcon: <FaMapMarkerAlt />
                        }));

                        allSuggestions = [...formattedStops];
                    }
                } else {
                    console.error('API stops response is not an array:', stopsResponse.data);
                }

                // Xử lý kết quả từ API landmarks
                if (Array.isArray(landmarksResponse.data)) {
                    console.log('Got landmark suggestions:', landmarksResponse.data.length, 'items');
                    setLandmarks(landmarksResponse.data);

                    // Định dạng landmarks để phù hợp với cấu trúc suggestion
                    const formattedLandmarks = landmarksResponse.data.map(landmark => ({
                        id: `landmark-${landmark.id}`,
                        stop_name: landmark.name,
                        address: landmark.address,
                        latitude: landmark.latitude,
                        longitude: landmark.longitude,
                        landmark_id: landmark.id,
                        suggestionType: 'LANDMARK',
                        displayIcon: <FaLandmark />,
                        landmarkData: landmark
                    }));

                    // Thêm landmarks vào danh sách gợi ý
                    allSuggestions = [...allSuggestions, ...formattedLandmarks];
                } else {
                    console.error('API landmarks response is not an array:', landmarksResponse.data);
                }

                // Cập nhật state với tất cả các gợi ý
                setSuggestions(allSuggestions);

            } catch (err) {
                console.error('Error fetching stops and landmarks:', err);
                setError('Không thể tải danh sách điểm dừng và địa điểm');
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

                const nameMatch = item.stop_name && item.stop_name.toLowerCase().includes(value.toLowerCase());
                const addressMatch = item.address && item.address.toLowerCase().includes(value.toLowerCase());
                // Nếu là landmark, tìm cả trong tags
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

        // Make sure we preserve all properties and explicitly set the display name
        setOrigin({
            ...item,
            displayName: item.stop_name,
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
        setSearchResults([]);

        try {
            console.log('Searching with origin:', origin);
            console.log('Searching with destination:', destination);

            if (typeof origin !== 'object' || typeof destination !== 'object') {
                setError('Vui lòng chọn địa điểm từ danh sách gợi ý hoặc trên bản đồ');
                setLoading(false);
                return;
            }

            // Format input data to ensure all required properties are present
            console.log('Original origin data:', origin);
            console.log('Original destination data:', destination);

            const cleanOrigin = formatLocationForSearch(origin);
            const cleanDestination = formatLocationForSearch(destination);

            console.log('Clean origin data:', cleanOrigin);
            console.log('Clean destination data:', cleanDestination);

            // Validate coordinates before proceeding
            if (!cleanOrigin.latitude || !cleanOrigin.longitude ||
                !cleanDestination.latitude || !cleanDestination.longitude) {
                setError('Địa điểm không có tọa độ hợp lệ. Vui lòng chọn lại địa điểm.');
                setLoading(false);
                return;
            }
            try {
                // Use the updated service with clean data
                const result = await findRoutes(cleanOrigin, cleanDestination, routePreferences);
                console.log('Route finding result:', result);

                if (result.error) {
                    setError(result.error);
                    return;
                }

                if (result && Array.isArray(result.options) && result.options.length > 0) {
                    setRouteOptions(result.options);
                    setSelectedRouteOption(result.options[0]);

                    // Thêm code để hiển thị tuyến đường trên bản đồ nếu cần
                    const firstRoute = result.options[0];
                    if (firstRoute && firstRoute.legs) {
                        const busLegs = firstRoute.legs.filter(leg => leg.type === 'BUS');
                        if (busLegs.length > 0 && busLegs[0].routeId) {
                            // Gọi API để lấy chi tiết tuyến đường để hiển thị
                            const routeDetail = await getRouteDetails(busLegs[0].routeId);
                            if (routeDetail && onRouteFound) {
                                onRouteFound(routeDetail);
                            }
                        }
                    }
                } else {
                    setError('Không tìm thấy tuyến đường phù hợp giữa hai điểm đã chọn.');
                }
            } catch (err) {
                console.error('Error with route search:', err);

                if (err.message === 'AUTH_REQUIRED') {
                    handleAuthError();
                    return;
                }

                setError('Không thể tìm kiếm tuyến. Vui lòng thử lại sau.');
            }
        } catch (generalErr) {
            console.error('General search error:', generalErr);
            setError('Đã xảy ra lỗi khi tìm kiếm. Vui lòng thử lại sau.');
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
    const buildLegacySearchParams = (origin, destination) => {
        const params = {};

        if (typeof origin === 'object') {
            if (origin.suggestionType === 'LANDMARK') {
                params.fromLandmarkId = origin.landmark_id;
                params.fromLat = origin.latitude;
                params.fromLng = origin.longitude;
            } else {
                params.fromStopId = origin.id;
            }
        } else {
            params.fromQuery = origin;
        }

        if (typeof destination === 'object') {
            if (destination.suggestionType === 'LANDMARK') {
                params.toLandmarkId = destination.landmark_id;
                params.toLat = destination.latitude;
                params.toLng = destination.longitude;
            } else {
                params.toStopId = destination.id;
            }
        } else {
            params.toQuery = destination;
        }

        return params;
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
                {routeOptions.length > 0 && (
                    <>
                        <div className="route-options-container">
                            <h3 className="routes-found-title">
                                Tìm thấy {routeOptions.length} phương án di chuyển
                            </h3>
                            <div className="route-options-list">
                                {routeOptions.map((option, index) => (
                                    <div
                                        key={index}
                                        className={`route-option-item ${selectedRouteOption === option ? 'active' : ''}`}
                                        onClick={() => handleRouteOptionSelect(option)}
                                    >
                                        <div className="option-summary">
                                            <div className="option-time">
                                                <FaClock />
                                                <span>{Math.floor(option.totalTime / 60)}h{option.totalTime % 60}p</span>
                                            </div>
                                            <div className="option-distance">
                                                <FaRoute />
                                                <span>{(option.totalDistance / 1000).toFixed(1)}km</span>
                                            </div>
                                            <div className="option-walking">
                                                <FaWalking />
                                                <span>{(option.walkingDistance).toFixed(0)}m</span>
                                            </div>
                                        </div>

                                        <div className="option-route-numbers">
                                            {option.routes.map((route, idx) => (
                                                <div
                                                    key={idx}
                                                    className="bus-icon-small"
                                                    style={{ backgroundColor: route.color || '#4CAF50' }}
                                                >
                                                    {route.number}
                                                </div>
                                            ))}
                                        </div>

                                        <div className="option-transfers">
                                            {option.transfers === 0
                                                ? 'Không chuyển tuyến'
                                                : `${option.transfers} lần chuyển tuyến`}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Show loading indicator when fetching details */}
                        {loadingDetails && (
                            <div className="loading-details">
                                <div className="spinner"></div>
                                <span>Đang tải chi tiết...</span>
                            </div>
                        )}

                        {/* Show route details only when a route is selected and details are loaded */}
                        {!loadingDetails && selectedRouteOption && routeDetails && (
                            <>
                                <RouteItinerary
                                    routeOption={routeDetails}
                                    onSelectRoute={(routeId) => onRouteFound && onRouteFound({ id: routeId })}
                                />
                                {showRouteDirections(routeDetails)}
                            </>
                        )}

                        {/* Remove the duplicate RouteItinerary that appears regardless of selection */}
                    </>
                )}
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
                    !loading && searchResults.length === 0 && origin && destination && (
                        <p className="no-data">Không tìm thấy tuyến đường phù hợp.</p>
                    )
                )}
            </div>
        </div>
    );
};

export default RouteSearch;