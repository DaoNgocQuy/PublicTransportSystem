import React, { useState, useEffect } from 'react';
import { getStops } from '../../services/stopService';
import { getRoutes } from '../../services/routeService';
import './css/RouteSearch.css';

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
    const [activeRouteCount, setActiveRouteCount] = useState(1);

    // Lấy danh sách tất cả các trạm
    useEffect(() => {
        const fetchAllStops = async () => {
            try {
                const allStops = await getStops();
                setSuggestions(allStops);
            } catch (err) {
                console.error('Lỗi khi tải danh sách các trạm:', err);
            }
        };
        fetchAllStops();
    }, []);

    // Lọc gợi ý khi người dùng gõ
    const handleOriginChange = (e) => {
        const value = e.target.value;
        setOrigin(value);

        if (value.length > 1) {
            const filtered = suggestions.filter(stop =>
                stop.name.toLowerCase().includes(value.toLowerCase()) ||
                stop.address?.toLowerCase().includes(value.toLowerCase())
            );
            setOriginSuggestions(filtered.slice(0, 5));
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
            const filtered = suggestions.filter(stop =>
                stop.name.toLowerCase().includes(value.toLowerCase()) ||
                stop.address?.toLowerCase().includes(value.toLowerCase())
            );
            setDestSuggestions(filtered.slice(0, 5));
            setShowDestSuggestions(true);
        } else {
            setDestSuggestions([]);
            setShowDestSuggestions(false);
        }
    };

    // Xử lý chọn trạm từ gợi ý
    const selectOrigin = (stop) => {
        setOrigin(stop.name);
        setOriginSuggestions([]);
        setShowOriginSuggestions(false);
    };

    const selectDestination = (stop) => {
        setDestination(stop.name);
        setDestSuggestions([]);
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
            setError('Vui lòng nhập cả điểm đi và điểm đến');
            return;
        }

        setLoading(true);
        setError(null);

        try {
            // Lấy ID của điểm đi và điểm đến
            const originStop = suggestions.find(stop =>
                stop.name.toLowerCase() === origin.toLowerCase() ||
                stop.address?.toLowerCase() === origin.toLowerCase()
            );

            const destStop = suggestions.find(stop =>
                stop.name.toLowerCase() === destination.toLowerCase() ||
                stop.address?.toLowerCase() === destination.toLowerCase()
            );

            if (!originStop || !destStop) {
                setError('Không tìm thấy điểm đi hoặc điểm đến');
                setLoading(false);
                return;
            }

            // Gọi API để tìm các tuyến kết nối 2 điểm
            // Ví dụ: một API endpoint có thể là /routes/find?originId=123&destinationId=456
            const response = await fetch(`http://localhost:8080/PTS/api/routes/find?originId=${originStop.id}&destinationId=${destStop.id}`);

            if (!response.ok) {
                throw new Error('Không thể tìm tuyến đường');
            }

            const data = await response.json();

            if (data.length === 0) {
                setError('Không tìm thấy tuyến đường phù hợp');
            } else {
                setSearchResults(data);
                if (onRouteFound && data.length > 0) {
                    onRouteFound(data[0]); // Tự động chọn tuyến đầu tiên
                }
            }
        } catch (err) {
            console.error('Lỗi khi tìm kiếm tuyến:', err);
            setError('Có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại sau.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="route-search-container">
            <div className="search-inputs">
                <div className="input-group">
                    <div className="input-icon origin-icon">
                        <i className="fas fa-map-marker-alt"></i>
                    </div>
                    <div className="input-wrapper">
                        <input
                            type="text"
                            placeholder="Điểm đi"
                            value={origin}
                            onChange={handleOriginChange}
                            onFocus={() => setShowOriginSuggestions(true)}
                        />
                        {showOriginSuggestions && originSuggestions.length > 0 && (
                            <div className="suggestions-dropdown">
                                {originSuggestions.map(stop => (
                                    <div
                                        key={stop.id}
                                        className="suggestion-item"
                                        onClick={() => selectOrigin(stop)}
                                    >
                                        <div className="suggestion-name">{stop.name}</div>
                                        <div className="suggestion-address">{stop.address}</div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                <button className="swap-button" onClick={swapLocations}>
                    <i className="fas fa-exchange-alt"></i>
                </button>

                <div className="input-group">
                    <div className="input-icon dest-icon">
                        <i className="fas fa-map-marker"></i>
                    </div>
                    <div className="input-wrapper">
                        <input
                            type="text"
                            placeholder="Điểm đến"
                            value={destination}
                            onChange={handleDestinationChange}
                            onFocus={() => setShowDestSuggestions(true)}
                        />
                        {showDestSuggestions && destSuggestions.length > 0 && (
                            <div className="suggestions-dropdown">
                                {destSuggestions.map(stop => (
                                    <div
                                        key={stop.id}
                                        className="suggestion-item"
                                        onClick={() => selectDestination(stop)}
                                    >
                                        <div className="suggestion-name">{stop.name}</div>
                                        <div className="suggestion-address">{stop.address}</div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            <button
                className="search-button"
                onClick={handleSearch}
                disabled={loading}
            >
                {loading ? 'Đang tìm...' : 'Tìm Kiếm'}
            </button>

            {error && <div className="error-message">{error}</div>}

            {searchResults.length > 0 && (
                <div className="search-results">
                    <h3>Kết quả tìm kiếm:</h3>
                    <div className="routes-result-list">
                        {searchResults.map((route, index) => (
                            <div
                                key={route.id}
                                className={`route-result-item ${index === 0 ? 'active' : ''}`}
                                onClick={() => {
                                    onRouteFound(route);
                                    setActiveRouteCount(index + 1);
                                }}
                            >
                                <div className="route-number" style={{ backgroundColor: route.color || '#4CAF50' }}>
                                    {index + 1}
                                </div>
                                <div className="route-result-info">
                                    <div className="route-result-name">{route.name}</div>
                                    <div className="route-result-path">{route.route}</div>
                                    <div className="route-result-details">
                                        <span className="time">
                                            <i className="far fa-clock"></i> {route.operatingHours || "06:00 - 20:00"}
                                        </span>
                                        <span className="stops">
                                            <i className="fas fa-bus"></i> {route.totalStops || "?"} trạm
                                        </span>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default RouteSearch;