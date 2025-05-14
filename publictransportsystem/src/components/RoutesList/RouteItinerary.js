import React from 'react';
import { FaWalking, FaBus, FaArrowRight, FaExchangeAlt, FaClock, FaRoute } from 'react-icons/fa';
import './RouteItinerary.css';

const RouteItinerary = ({ routeOption, onSelectRoute }) => {
    if (!routeOption) return null;

    const formatDuration = (minutes) => {
        if (minutes < 60) {
            return `${minutes} phút`;
        }
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return `${hours} giờ ${mins > 0 ? `${mins} phút` : ''}`;
    };

    const formatDistance = (meters) => {
        if (meters < 1000) {
            return `${meters} mét`;
        }
        return `${(meters / 1000).toFixed(1)} km`;
    };

    return (
        <div className="route-itinerary">
            <div className="itinerary-header">
                <div className="itinerary-summary">
                    <div className="total-time">
                        <FaClock className="summary-icon" />
                        <span>{formatDuration(routeOption.totalTime)}</span>
                    </div>
                    <div className="total-distance">
                        <FaRoute className="summary-icon" />
                        <span>{formatDistance(routeOption.totalDistance)}</span>
                    </div>
                    <div className="walking-distance">
                        <FaWalking className="summary-icon" />
                        <span>{formatDistance(routeOption.walkingDistance)}</span>
                    </div>
                    <div className="transfers-count">
                        <FaExchangeAlt className="summary-icon" />
                        <span>{routeOption.transfers || 0} lần chuyển tuyến</span>
                    </div>
                </div>
            </div>

            <div className="itinerary-steps">
                {routeOption.legs && routeOption.legs.map((leg, index) => (
                    <div key={index} className="itinerary-leg">
                        {leg.type === 'WALK' ? (
                            <div className="walk-leg">
                                <div className="leg-icon walk-icon">
                                    <FaWalking />
                                </div>
                                <div className="leg-details">
                                    <div className="leg-name">Đi bộ</div>
                                    <div className="leg-stats">
                                        <span>{formatDistance(leg.distance)}</span>
                                        <span> • </span>
                                        <span>{formatDuration(leg.duration)}</span>
                                    </div>
                                    {leg.from && leg.to && (
                                        <div className="leg-endpoints">
                                            <div>Từ: {leg.from.name}</div>
                                            <div>Đến: {leg.to.name}</div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ) : leg.type === 'BUS' ? (
                            <div className="bus-leg" onClick={() => onSelectRoute && onSelectRoute(leg.routeId)}>
                                <div className="leg-icon bus-icon" style={{ backgroundColor: leg.routeColor || '#4CAF50' }}>
                                    <FaBus />
                                </div>
                                <div className="leg-details">
                                    <div className="leg-route-number" style={{ color: leg.routeColor || '#4CAF50' }}>
                                        Tuyến {leg.routeNumber}
                                    </div>
                                    <div className="leg-route-name">{leg.routeName}</div>
                                    <div className="leg-stats">
                                        <span>{formatDuration(leg.duration)}</span>
                                        <span> • </span>
                                        <span>{leg.stops} trạm</span>
                                    </div>
                                    <div className="leg-endpoints">
                                        <div>Lên xe tại: {leg.boardStop && leg.boardStop.name}</div>
                                        <div>Xuống xe tại: {leg.alightStop && leg.alightStop.name}</div>
                                    </div>
                                </div>
                            </div>
                        ) : null}

                        {index < routeOption.legs.length - 1 && (
                            <div className="leg-connector"></div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default RouteItinerary;