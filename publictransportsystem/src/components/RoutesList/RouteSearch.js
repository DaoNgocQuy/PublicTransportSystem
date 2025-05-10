import React, { useState, useEffect } from 'react';
import { authApi } from '../../configs/Apis';
import { FaMapMarkerAlt, FaExchangeAlt, FaDotCircle, FaFlag } from 'react-icons/fa';
import './RouteSearch.css';

const RouteSearch = ({ onRouteFound }) => {
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

    // Lấy danh sách tất cả các trạm
    useEffect(() => {
        const fetchStops = async () => {
            try {
                setLoading(true);
                const response = await authApi.get('/api/stops');
                console.log('API stops response:', response);

                if (Array.isArray(response.data)) {
                    console.log('Got suggestions:', response.data.length, 'items');
                    if (response.data.length === 0) {
                        console.warn('API returned empty suggestions array');
                    } else {
                        console.log('First suggestion:', response.data[0]);
                    }
                    setSuggestions(response.data);
                } else {
                    console.error('API response is not an array:', response.data);
                }
            } catch (err) {
                console.error('Error fetching stops:', err);
                setError('Không thể tải danh sách điểm dừng');
            } finally {
                setLoading(false);
            }
        };

        fetchStops();
    }, []);

    // Lọc gợi ý khi người dùng gõ
    const handleOriginChange = (e) => {
        const value = e.target.value;
        console.log('Origin input value:', value);
        // Chỉ lưu trữ chuỗi văn bản, không phải đối tượng
        setOrigin(value);

        if (value.length > 1) {
            console.log('Filtering suggestions with:', value);
            const filtered = suggestions.filter(stop => {
                if (!stop || typeof stop !== 'object') return false;

                const nameMatch = stop.stop_name && stop.stop_name.toLowerCase().includes(value.toLowerCase());
                const addressMatch = stop.address && stop.address.toLowerCase().includes(value.toLowerCase());

                return nameMatch || addressMatch;
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
            const filtered = suggestions.filter(stop => {
                // Kiểm tra stop tồn tại và stop_name tồn tại
                if (!stop || typeof stop !== 'object') return false;

                const nameMatch = stop.stop_name && stop.stop_name.toLowerCase().includes(value.toLowerCase());
                const addressMatch = stop.address && stop.address.toLowerCase().includes(value.toLowerCase());

                return nameMatch || addressMatch;
            });

            setDestSuggestions(filtered);
            setShowDestSuggestions(true);
        } else {
            setDestSuggestions([]);
            setShowDestSuggestions(false);
        }
    };

    // Xử lý chọn trạm từ gợi ý
    const selectOrigin = (stop) => {
        // Lưu lại cả đối tượng stop để có thể truy cập ID khi tìm kiếm
        setOrigin({
            ...stop,
            displayName: stop.stop_name
        });
        setShowOriginSuggestions(false);
    };

    const selectDestination = (stop) => {
        // Lưu lại cả đối tượng stop để có thể truy cập ID khi tìm kiếm
        setDestination({
            ...stop,
            displayName: stop.stop_name
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
            // Kiểm tra xem origin và destination có phải là đối tượng không
            // Nếu là, sử dụng ID; nếu không, sử dụng tên
            const originParam = typeof origin === 'object' ? origin.id : origin;
            const destinationParam = typeof destination === 'object' ? destination.id : destination;

            // Gọi API với tham số đúng định dạng
            const response = await authApi.get('/api/routes/search', {
                params: {
                    from: originParam,
                    to: destinationParam
                }
            });

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
                    <div className="location-inputs">
                        <div className="search-input-container origin-input">
                            <div className="input-icon">
                                <FaDotCircle className="origin-icon" />
                            </div>
                            <input
                                type="text"
                                placeholder="Điểm đi..."
                                value={typeof origin === 'object' ? origin.displayName || origin.stop_name : origin}
                                onChange={handleOriginChange}
                                onFocus={() => setShowOriginSuggestions(true)}
                                className="search-input"
                            />
                            {showOriginSuggestions && originSuggestions.length > 0 && (
                                <ul className="suggestion-list">
                                    {originSuggestions.map((stop, index) => (
                                        <li
                                            key={stop.id || index}
                                            onClick={() => selectOrigin(stop)}
                                            className="suggestion-item"
                                        >
                                            <div className="suggestion-icon">
                                                <FaMapMarkerAlt />
                                            </div>
                                            <div className="suggestion-text">
                                                <span className="suggestion-name">{stop.stop_name || 'Không có tên'}</span>
                                                {stop.address && <span className="suggestion-address">{stop.address}</span>}
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>

                        <button className="swap-button" onClick={swapLocations}>
                            <FaExchangeAlt />
                        </button>

                        {/* Thêm input điểm đến ở đây */}
                        <div className="search-input-container dest-input">
                            <div className="input-icon">
                                <FaFlag className="dest-icon" />
                            </div>
                            <input
                                type="text"
                                placeholder="Điểm đến..."
                                value={typeof destination === 'object' ? destination.displayName || destination.stop_name : destination}
                                onChange={handleDestinationChange}
                                onFocus={() => setShowDestSuggestions(true)}
                                className="search-input"
                            />
                            {showDestSuggestions && destSuggestions.length > 0 && (
                                <ul className="suggestion-list">
                                    {destSuggestions.map((stop, index) => (
                                        <li
                                            key={stop.id || index}
                                            onClick={() => selectDestination(stop)}
                                            className="suggestion-item"
                                        >
                                            <div className="suggestion-icon">
                                                <FaMapMarkerAlt />
                                            </div>
                                            <div className="suggestion-text">
                                                <span className="suggestion-name">{stop.stop_name || 'Không có tên'}</span>
                                                {stop.address && <span className="suggestion-address">{stop.address}</span>}
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>


                    </div>

                    <button
                        className="search-button"
                        onClick={handleSearch}
                        disabled={loading}
                    >
                        {loading ? 'Đang tìm...' : 'Tìm kiếm'}
                    </button>
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
                                    {route.icon && <img src={route.icon} alt="icon" /> ||
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