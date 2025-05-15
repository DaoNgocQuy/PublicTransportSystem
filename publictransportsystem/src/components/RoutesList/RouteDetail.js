import React, { useState, useEffect } from 'react';
import './RouteDetail.css';
import { authApi } from '../../configs/Apis';
import ScheduleDisplay from './ScheduleDisplay';
const RouteDetail = ({
    route,
    onBack,
    favoriteRoutes,
    notificationEnabled,
    toggleFavorite,
    toggleNotification,
    tripDirection,
    switchDirection,
    focusedStopId,
    handleStopClick
}) => {
    const [routeStops, setRouteStops] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    // Add new state to control which view is active
    const [activeView, setActiveView] = useState('schedule'); // 'schedule' or 'stops'
    const [scheduleData, setScheduleData] = useState([]);
    const [calculatedFrequency, setCalculatedFrequency] = useState(null); // Add this line
    const calculateFrequencyFromSchedule = (schedules) => {
        if (!schedules || schedules.length < 2) return null;

        // Tính khoảng cách trung bình giữa các chuyến xe
        let totalDiff = 0;
        for (let i = 1; i < schedules.length; i++) {
            const curr = new Date(`2000-01-01T${schedules[i].departureTime}`);
            const prev = new Date(`2000-01-01T${schedules[i - 1].departureTime}`);
            const diffMinutes = (curr - prev) / (1000 * 60);
            totalDiff += diffMinutes;
        }

        return Math.round(totalDiff / (schedules.length - 1)) + " phút";
    };
    useEffect(() => {
        if (!route) return;

        const fetchData = async () => {
            setLoading(true);
            try {
                // Fetch stops based on direction
                const direction = tripDirection === 'outbound' ? 'outbound' : 'return';
                const response = await authApi.get(`/api/stops/route/${route.id}?direction=${direction}`);

                if (Array.isArray(response.data)) {
                    setRouteStops(response.data);
                }

                // Fetch schedule data
                const scheduleResponse = await authApi.get(`/api/schedules/route/${route.id}?direction=${direction}`);
                if (Array.isArray(scheduleResponse.data)) {
                    setScheduleData(scheduleResponse.data);
                }
                const frequencyResponse = await authApi.get(`/api/schedules/route/${route.id}/frequency`);

                if (frequencyResponse.data && frequencyResponse.data.formattedFrequency) {
                    setCalculatedFrequency(frequencyResponse.data.formattedFrequency);
                } else {
                    // If no formatted frequency, calculate it locally from schedules
                    if (Array.isArray(scheduleResponse.data) && scheduleResponse.data.length >= 2) {
                        const localFreq = calculateFrequencyFromSchedule(scheduleResponse.data);
                        setCalculatedFrequency(localFreq);
                    } else {
                        setCalculatedFrequency(null);
                    }
                }
            } catch (err) {
                console.error('Error fetching data:', err);
                setError(err.message || 'Không thể tải dữ liệu');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [route, tripDirection]);

    if (!route) return null;

    return (
        <div className="route-detail">
            <div className="route-header">
                <button className="back-button" onClick={onBack}>
                    <i className="fas fa-arrow-left"></i>
                </button>
                <h2 className="selected-route-name" style={{ color: route.color || '#4CAF50' }}>
                    {route.name}
                </h2>
                <div className="route-actions">
                    <button
                        className={`favorite-btn ${favoriteRoutes.includes(route.id) ? 'active' : ''}`}
                        onClick={(e) => toggleFavorite(e, route.id)}
                    >
                        <i className={favoriteRoutes.includes(route.id) ? "fas fa-heart" : "far fa-heart"}></i>
                    </button>
                    <button
                        className={`notification-btn ${notificationEnabled[route.id] ? 'active' : ''}`}
                        onClick={(e) => toggleNotification(e, route.id)}
                    >
                        <i className={notificationEnabled[route.id] ? "fas fa-bell" : "far fa-bell"}></i>
                    </button>
                </div>
            </div>

            <div className="direction-selector">
                <button
                    className={`direction-btn ${tripDirection === 'outbound' ? 'active' : ''}`}
                    onClick={() => switchDirection('outbound')}
                >
                    Xem lượt đi
                </button>
                <button
                    className={`direction-btn ${tripDirection === 'return' ? 'active' : ''}`}
                    onClick={() => switchDirection('return')}
                >
                    Xem lượt về
                </button>
            </div>

            {/* View selector buttons */}
            <div className="view-selector">
                <button
                    className={`view-btn ${activeView === 'schedule' ? 'active' : ''}`}
                    onClick={() => setActiveView('schedule')}
                >
                    Biểu đồ giờ
                </button>
                <button
                    className={`view-btn ${activeView === 'stops' ? 'active' : ''}`}
                    onClick={() => setActiveView('stops')}
                >
                    Trạm dừng
                </button>
                <button
                    className={`view-btn ${activeView === 'info' ? 'active' : ''}`}
                    onClick={() => setActiveView('info')}
                >
                    Thông tin
                </button>
                <button
                    className={`view-btn ${activeView === 'rating' ? 'active' : ''}`}
                    onClick={() => setActiveView('rating')}
                >
                    Đánh giá
                </button>
            </div>

            {loading ? (
                <p className="loading">Đang tải dữ liệu...</p>
            ) : error ? (
                <p className="error">{error}</p>
            ) : (
                <>
                    {/* Conditional rendering based on active view */}
                    {activeView === 'schedule' && (
                        <ScheduleDisplay schedules={scheduleData} />
                    )}

                    {activeView === 'stops' && (
                        <ul className="route-stops">
                            {routeStops.map((stop) => (
                                <li
                                    key={stop.id}
                                    className={`stop-item ${focusedStopId === stop.id ? 'focused' : ''}`}
                                    onClick={() => handleStopClick(stop.id)}
                                >
                                    <div className="stop-marker"></div>
                                    <div className="stop-info">
                                        <h4>{stop.name}</h4>
                                        <p>{stop.address}</p>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}

                    {activeView === 'info' && (
                        <div className="route-info-detail">
                            <h3>Thông tin tuyến</h3>
                            <p><strong>Tên tuyến:</strong> {route.name}</p>
                            <p><strong>Lộ trình:</strong> {route.route}</p>
                            <p><strong>Giờ hoạt động:</strong> {route.operatingHours}</p>
                            <p><strong>Giá vé:</strong> {route.ticketPrice || "Không có thông tin"}</p>

                            {/* Show calculated frequency if available, otherwise fall back to route.frequency */}
                            <p><strong>Giãn cách tuyến:</strong> {calculatedFrequency || route.frequency || "Không có thông tin"}</p>

                            {/* Show note about differences between stated and actual frequency if both are available and different */}
                            {route.frequency && calculatedFrequency && route.frequency !== calculatedFrequency && (
                                <p className="frequency-note">
                                    <i>Lưu ý: Tần suất chạy xe thực tế ({calculatedFrequency}) có thể khác với giãn cách tuyến tiêu chuẩn ({route.frequency}).</i>
                                </p>
                            )}
                        </div>
                    )}

                    {activeView === 'rating' && (
                        <div className="route-rating">
                            <h3>Đánh giá</h3>
                            <p>Chức năng đánh giá đang được phát triển</p>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};

export default RouteDetail;