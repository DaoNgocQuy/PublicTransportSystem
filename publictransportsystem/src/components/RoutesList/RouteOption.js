import React from 'react';
import { FaWalking, FaArrowRight, FaBus } from 'react-icons/fa';
import './RouteOption.css';

const RouteOption = ({ option, onClick, isSelected = false }) => {
    if (!option) return null;

    // Kiểm tra xem đây có phải là tuyến đi bộ không
    const isWalkingOnly = option.walkingOnly === true;

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

    return (
        <div
            className={`route-option-busmap ${isSelected ? 'selected' : ''} ${isWalkingOnly ? 'walking-only' : ''}`}
            onClick={() => onClick && onClick(option)}
        >
            {/* Header với số tuyến và thời gian */}
            <div className="route-option-header">
                {isWalkingOnly ? (
                    // Hiển thị icon đi bộ cho tuyến đi bộ
                    <div className="route-number-badge walking-badge">
                        <FaWalking />
                    </div>
                ) : (
                    // Hiển thị số tuyến cho xe buýt
                    <div
                        className="route-number-badge"
                        style={{ backgroundColor: firstBusLeg?.routeColor || '#e53935' }}
                    >
                        {firstBusLeg?.routeNumber || option.routes?.[0]?.number || '?'}
                    </div>
                )}
                <div className="route-time">{formatTime(option.totalTime)}</div>
            </div>

            {/* Thông tin khoảng cách */}
            {isWalkingOnly ? (
                // Hiển thị chỉ khoảng cách đi bộ cho tuyến đi bộ
                <div className="route-distances walking-only">
                    <div className="walk-distance full-width">
                        <FaWalking className="distance-icon" />
                        <span>Đi bộ {formatDistance(option.totalDistance || 0)}</span>
                    </div>
                </div>
            ) : (
                // Hiển thị khoảng cách đi bộ và xe buýt cho tuyến xe buýt
                <div className="route-distances">
                    <div className="walk-distance">
                        <FaWalking className="distance-icon" />
                        {formatDistance(option.walkingDistance || 0)}
                    </div>
                    <FaArrowRight className="arrow-icon" />
                    <div className="bus-distance">
                        <FaBus className="distance-icon" />
                        {formatDistance((option.totalDistance - option.walkingDistance) || 0)}
                    </div>
                </div>
            )}

            {/* Thông tin trạm đón xe - chỉ hiển thị cho tuyến xe buýt */}
            {!isWalkingOnly && (
                <div className="boarding-info">
                    Đón xe tại trạm: <strong>
                        {firstBusLeg?.from?.name || 'Trạm không xác định'}
                    </strong>
                </div>
            )}

            {/* Thông tin điểm đi bộ - chỉ hiển thị cho tuyến đi bộ */}
            {isWalkingOnly && option.legs && option.legs[0] && (
                <div className="walking-info">
                    <div>
                        <strong>Từ:</strong> {option.legs[0].from?.name || 'Điểm xuất phát'}
                    </div>
                    <div>
                        <strong>Đến:</strong> {option.legs[0].to?.name || 'Điểm đến'}
                    </div>
                </div>
            )}
        </div>
    );
};

export default RouteOption;