import React, { useState, useEffect } from 'react';
import { authApi } from '../../configs/Apis';
import { FaMapMarkerAlt, FaExchangeAlt, FaDotCircle, FaFlag, FaLandmark } from 'react-icons/fa';
import './RouteSearch.css';

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
    // Lấy danh sách tất cả các trạm và địa điểm nổi tiếng


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
        // Lưu lại cả đối tượng để có thể truy cập ID khi tìm kiếm
        setOrigin({
            ...item,
            displayName: item.stop_name
        });
        setShowOriginSuggestions(false);
    };

    const selectDestination = (item) => {
        // Lưu lại cả đối tượng để có thể truy cập ID khi tìm kiếm
        setDestination({
            ...item,
            displayName: item.stop_name
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

        if (origin === destination) {
            setError('Điểm đi và điểm đến không được trùng nhau');
            return;
        }

        setLoading(true);
        setError(null);

        try {
            // Xác định loại và ID của điểm đi/đến
            let params = {};

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

            // Gọi API với tham số đúng định dạng
            const response = await authApi.get('/api/routes/search', { params });

            console.log('Search response:', response.data);

            if (Array.isArray(response.data)) {
                setSearchResults(response.data);
                setActiveRouteCount(Math.min(response.data.length, 3)); // Giới hạn tối đa 3 tuyến

                // Nếu có kết quả và có hàm callback
                if (response.data.length > 0 && onRouteFound) {
                    onRouteFound(response.data[0]);
                }
            } else {
                setSearchResults([]);
                setActiveRouteCount(0);
            }
        } catch (err) {
            console.error('Error searching routes:', err);
            // Hiển thị thông báo lỗi chi tiết hơn
            const errorMessage = err.response?.data?.message || 'Không thể tìm kiếm tuyến. Vui lòng thử lại sau.';
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
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
                                            setShowOriginSuggestions(true);  // Sai: đây là cho origin, không phải destination
                                            setMapSelectionType('origin');    // Sai: đây phải là 'destination'
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