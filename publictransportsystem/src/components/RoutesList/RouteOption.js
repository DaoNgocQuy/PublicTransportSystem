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

    // Calculate total walking and bus distances from all legs
    const calculateDistances = () => {
        if (!option.legs) return { walkingDistance: 0, busDistance: 0 };

        // Get walking distance directly from API response
        const walkingDistance = Math.abs(parseFloat(option.walkingDistance) || 0);

        // Calculate total distance from legs if available
        let totalDistance = 0;
        let calculatedBusDistance = 0;

        if (option.legs && option.legs.length > 0) {
            option.legs.forEach(leg => {
                const legDistance = Math.abs(parseFloat(leg.distance) || 0);
                totalDistance += legDistance;

                if (leg.type === 'BUS') {
                    calculatedBusDistance += legDistance;
                }
            });
        } else {
            // Fallback to option's total distance if available
            totalDistance = Math.abs(parseFloat(option.totalDistance) || 0);
        }

        // If totalDistance is still 0 or less than walkingDistance, use walking distance as base
        if (totalDistance === 0 || totalDistance < walkingDistance) {
            // If this is a walking route, just use walking distance
            if (isWalkingOnly) {
                totalDistance = walkingDistance;
            } else {
                // For bus routes, estimate - assume bus distance is at least equal to walking
                totalDistance = walkingDistance * 2;
            }
        }

        // Calculate bus distance as the difference (if not already calculated from legs)
        let busDistance = calculatedBusDistance;
        if (busDistance === 0 && !isWalkingOnly) {
            busDistance = totalDistance - walkingDistance;
        }

        // Ensure bus distance is never negative
        busDistance = Math.max(0, busDistance);

        console.log("Walking:", walkingDistance, "Bus:", busDistance, "Total:", totalDistance);

        return { walkingDistance, busDistance };
    };

    const { walkingDistance, busDistance } = calculateDistances();

    // Format khoảng cách
    const formatDistance = (meters) => {
        if (!meters && meters !== 0) return "";

        // Ensure meters is a positive number
        meters = Math.abs(parseFloat(meters));

        if (meters < 1000) {
            return `${Math.round(meters)} m`;
        }
        return `${(meters / 1000).toFixed(1)} km`;
    };

    // Format thời gian phút sang giờ:phút nếu cần
    const formatTime = (minutes) => {
        if (!minutes && minutes !== 0) return "? phút";

        // Ensure minutes is a positive number
        minutes = Math.abs(parseInt(minutes));

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
                        <span>Đi bộ {formatDistance(walkingDistance)}</span>
                    </div>
                </div>
            ) : (
                // Hiển thị khoảng cách đi bộ và xe buýt cho tuyến xe buýt
                <div className="route-distances">
                    <div className="walk-distance">
                        <FaWalking className="distance-icon" />
                        {formatDistance(walkingDistance)}
                    </div>
                    <FaArrowRight className="arrow-icon" />
                    <div className="bus-distance">
                        <FaBus className="distance-icon" /> {formatDistance(busDistance)}
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