import React from 'react';
import { FaWalking, FaBus, FaArrowRight, FaLongArrowAltRight } from 'react-icons/fa';
import './RouteItinerary.css';

const RouteItinerary = ({ routeOption, onSelectRoute }) => {
    if (!routeOption) return null;

    const formatDuration = (minutes) => {
        if (!minutes && minutes !== 0) return "? phút";
        if (minutes < 60) {
            return `${minutes} phút`;
        }
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return `${hours} giờ ${mins > 0 ? `${mins} phút` : ''}`;
    };

    const formatDistance = (meters) => {
        if (!meters && meters !== 0) return "";
        if (meters < 1000) {
            return `${Math.round(meters)} m`;
        }
        return `${(meters / 1000).toFixed(1)} km`;
    };

    // Find bus legs
    const busLegs = routeOption.legs ? routeOption.legs.filter(leg => leg.type === 'BUS') : [];
    const firstBusLeg = busLegs.length > 0 ? busLegs[0] : null;

    // Tìm trạm lên và xuống xe thực tế cho mỗi leg xe buýt
    const getBusStopInfo = (leg) => {
        // Trả về thông tin trạm lên và xuống dựa trên hành trình thực tế
        return {
            boardStop: leg.boardStop || leg.from || {},
            alightStop: leg.alightStop || leg.to || {}
        };
    };

    return (
        <div className="route-itinerary busmap-style">
            <div className="route-option-header">
                <div className="route-option-number">
                    {firstBusLeg && (
                        <div
                            className="route-number-badge"
                            style={{ backgroundColor: firstBusLeg.routeColor || '#4CAF50' }}
                        >
                            {firstBusLeg.routeNumber || '?'}
                        </div>
                    )}
                    <div className="route-time-info">
                        {routeOption.totalTime || '?'} phút
                    </div>
                </div>
                <div className="route-travel-info">
                    <div className="walk-distance">
                        <FaWalking className="travel-icon" /> {formatDistance(routeOption.walkingDistance || 0)}
                    </div>
                    <FaLongArrowAltRight className="arrow-icon" />
                    <div className="bus-distance">
                        <FaBus className="travel-icon" /> {formatDistance(routeOption.busDistance ||
                            (routeOption.totalDistance - routeOption.walkingDistance) || 0)}
                    </div>
                </div>
            </div>

            {routeOption.legs && routeOption.legs.map((leg, index) => {
                // Lấy thông tin chi tiết về trạm dừng cho mỗi chặng xe buýt
                const busStopInfo = leg.type === 'BUS' ? getBusStopInfo(leg) : {};

                return (
                    <div key={index} className="route-segment">
                        {leg.type === 'WALK' ? (
                            <div className="walk-segment">
                                <div className="segment-icon-container">
                                    <div className="segment-icon walk">
                                        <FaWalking />
                                    </div>
                                </div>
                                <div className="segment-details">
                                    <div className="segment-distance">{formatDistance(leg.distance)} · {formatDuration(leg.duration)}</div>
                                    <div className="segment-instruction">
                                        Đi bộ {leg.from && leg.to ?
                                            <>từ <strong>{leg.from.name || 'Điểm xuất phát'}</strong> đến <strong>{leg.to.name || 'Điểm đến'}</strong></> :
                                            ''}
                                    </div>
                                </div>
                            </div>
                        ) : leg.type === 'BUS' ? (
                            <div className="bus-segment" onClick={() => onSelectRoute && onSelectRoute(leg.routeId)}>
                                <div className="segment-icon-container">
                                    <div
                                        className="segment-icon bus"
                                        style={{ backgroundColor: leg.routeColor || '#4CAF50' }}
                                    >
                                        {leg.routeNumber}
                                    </div>
                                </div>
                                <div className="segment-details">
                                    <div className="segment-distance">{formatDistance(leg.distance)} · {formatDuration(leg.duration)}</div>
                                    <div className="segment-instruction">
                                        <div><strong>LÊN XE:</strong> {busStopInfo.boardStop?.name || 'Trạm không xác định'}</div>
                                        <div><strong>XUỐNG XE:</strong> {busStopInfo.alightStop?.name || 'Trạm không xác định'}</div>
                                    </div>
                                </div>
                            </div>
                        ) : null}

                        {index < routeOption.legs.length - 1 && <div className="segment-connector"></div>}
                    </div>
                );
            })}
        </div>
    );
};

export default RouteItinerary;