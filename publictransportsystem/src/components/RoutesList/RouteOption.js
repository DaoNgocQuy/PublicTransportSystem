import React from 'react';
import { FaWalking, FaArrowRight, FaBus } from 'react-icons/fa';
import './RouteOption.css';

const RouteOption = ({ option, onClick, isSelected = false }) => {
    if (!option) return null;

    // Lấy thông tin từ chặng đi xe buýt
    const busLegs = option.legs ? option.legs.filter(leg => leg.type === 'BUS') : [];
    const firstBusLeg = busLegs.length > 0 ? busLegs[0] : null;

    // Format khoảng cách
    const formatDistance = (meters) => {
        if (!meters && meters !== 0) return "";
        if (meters < 1000) {
            return `${Math.round(meters)} m`;
        }
        return `${(meters / 1000).toFixed(1)} km`;
    };

    // Lấy thông tin trạm đón
    const getBoardingStation = () => {
        if (firstBusLeg && firstBusLeg.from) {
            return firstBusLeg.from.name || 'Trạm không xác định';
        }
        return 'Trạm không xác định';
    };

    // Format thời gian phút sang giờ:phút nếu cần
    const formatTime = (minutes) => {
        if (!minutes && minutes !== 0) return "? phút";

        if (minutes < 60) {
            return `${minutes} phút`;
        }

        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;

        if (mins === 0) {
            return `${hours} giờ`;
        } else {
            return `${hours} giờ ${mins} phút`;
        }
    };

    // Tính khoảng cách xe buýt
    const getBusDistance = () => {
        if (option.busDistance) {
            return option.busDistance;
        }
        return option.totalDistance - option.walkingDistance;
    };

    return (
        <div
            className={`route-option-busmap ${isSelected ? 'selected' : ''}`}
            onClick={() => onClick && onClick(option)}
        >
            {/* Header với số tuyến và thời gian */}
            <div className="route-option-header">
                <div
                    className="route-number-badge"
                    style={{ backgroundColor: firstBusLeg?.routeColor || '#e53935' }}
                >
                    {firstBusLeg?.routeNumber || option.routes?.[0]?.number || '?'}
                </div>
                <div className="route-time">{formatTime(option.totalTime)}</div>
            </div>

            {/* Thông tin khoảng cách đi bộ và xe buýt */}
            <div className="route-distances">
                <div className="walk-distance">
                    <FaWalking className="distance-icon" />
                    {formatDistance(option.walkingDistance || 0)}
                </div>
                <FaArrowRight className="arrow-icon" />
                <div className="bus-distance">
                    <FaBus className="distance-icon" />
                    {formatDistance(getBusDistance() || 0)}
                </div>
            </div>

            {/* Thông tin trạm đón xe */}
            <div className="boarding-info">
                Đón xe tại trạm: <strong>{getBoardingStation()}</strong>
            </div>
        </div>
    );
};

export default RouteOption;