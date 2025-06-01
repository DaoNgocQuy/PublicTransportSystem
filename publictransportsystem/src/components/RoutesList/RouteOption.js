import React from 'react';
import { FaWalking, FaArrowRight, FaBus, FaSubway, FaTrain } from 'react-icons/fa';
import './RouteOption.css';

const RouteOption = ({ option, onClick, isSelected = false }) => {
    if (!option) return null;

    // Kiểm tra xem đây có phải là tuyến đi bộ không
    const isWalkingOnly = option.walkingOnly === true;

    // Lấy thông tin từ chặng đi xe buýt
    const busLegs = option.legs ? option.legs.filter(leg => leg.type === 'BUS') : [];
    const firstBusLeg = busLegs.length > 0 ? busLegs[0] : null;

    // Lấy thông tin về loại phương tiện
    // Lấy thông tin về loại phương tiện
    const getTransportType = () => {
        // Add console logging to debug
        console.log("Option data:", option);
        console.log("Walking only?", isWalkingOnly);
        console.log("Bus legs:", busLegs);
        console.log("First bus leg:", firstBusLeg);

        // Also debug origin/destination data for walking routes
        if (isWalkingOnly) {
            console.log("Walking route details:");
            console.log("From options:", {
                fromLegs: option.legs?.[0]?.from,
                fromDirect: option.from,
                origin: option.origin,
                startPoint: option.startPoint
            });
            console.log("To options:", {
                toLegs: option.legs?.[option.legs.length - 1]?.to,
                toDirect: option.to,
                destination: option.destination,
                endPoint: option.endPoint
            });
            return 'WALK';
        }

        // Rest of existing code
        const routeTypeId = firstBusLeg?.routeTypeId ||
            option?.routeTypeId ||
            option?.routes?.[0]?.routeTypeId ||
            option?.route?.routeTypeId ||
            3; // Default to bus (3) if no type found

        console.log("Route type ID found:", routeTypeId);

        // RouteTypeId: 0 = Tramway, 1 = Metro/Subway, 2 = Train, 3 = Bus
        if (routeTypeId === 1) return 'METRO';
        if (routeTypeId === 2) return 'TRAIN';
        return 'BUS'; // Default to bus
    };

    // Get transport icon based on the type
    const getTransportIcon = () => {
        const transportType = getTransportType();

        switch (transportType) {
            case 'WALK':
                return <FaWalking />;
            case 'METRO':
                return <FaSubway />;
            case 'TRAIN':
                return <FaTrain />;
            case 'BUS':
            default:
                return <FaBus />;
        }
    };

    // Get badge class based on transport type
    const getBadgeClass = () => {
        const transportType = getTransportType();
        return `${transportType.toLowerCase()}-badge`;
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

    // Calculate total walking and bus distances from all legs
    function calculateDistances() {
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

        return { walkingDistance, busDistance };
    }

    return (
        <div
            className={`route-option-busmap ${isSelected ? 'selected' : ''} ${isWalkingOnly ? 'walking-only' : ''}`}
            onClick={() => onClick && onClick(option)}
        >
            {/* Header với icon phương tiện và thời gian */}
            <div className="route-option-header">
                <div className={`route-number-badge ${getBadgeClass()}`}>
                    {isWalkingOnly ? (
                        <FaWalking />
                    ) : (
                        <>
                            {getTransportIcon()}
                            {/* Improve route number display */}
                            {firstBusLeg?.routeNumber || firstBusLeg?.routeName ||
                                option?.routeNumber || option?.routeName ||
                                (firstBusLeg?.routeId?.toString()?.match(/\d+/)?.[0]) ||
                                (option?.routeId?.toString()?.match(/\d+/)?.[0]) || '?'}
                        </>
                    )}
                </div>
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
                // Hiển thị khoảng cách đi bộ và xe buýt/metro cho tuyến phương tiện
                <div className="route-distances">
                    <div className="walk-distance">
                        <FaWalking className="distance-icon" />
                        {formatDistance(walkingDistance)}
                    </div>
                    <FaArrowRight className="arrow-icon" />
                    <div className="transport-distance">
                        {getTransportIcon()}
                        <span className="distance-value">{formatDistance(busDistance)}</span>
                    </div>
                </div>
            )}

            {/* Thông tin trạm đón xe - chỉ hiển thị cho tuyến xe không phải đi bộ */}
            {!isWalkingOnly && (
                <div className="boarding-info">
                    Đón xe tại trạm: <strong>
                        {firstBusLeg?.from?.name || 'Trạm không xác định'}
                    </strong>
                </div>
            )}

            {/* Thông tin điểm đi bộ - chỉ hiển thị cho tuyến đi bộ */}
            {isWalkingOnly && (
                <div className="walking-info">
                    <div>
                        <strong>Từ:</strong> {
                            // Follow similar prioritization as in RouteSearch.js
                            (typeof option.origin === 'object' ? option.origin.displayName || option.origin.stop_name : option.origin) ||
                            (typeof option.from === 'object' ? option.from.displayName || option.from.stop_name : option.from) ||
                            (option.legs?.[0]?.from?.name) ||
                            (option.startPoint?.name) ||
                            'Vị trí của bạn'
                        }
                    </div>
                    <div>
                        <strong>Đến:</strong> {
                            // Follow similar prioritization as in RouteSearch.js 
                            (typeof option.destination === 'object' ? option.destination.displayName || option.destination.stop_name : option.destination) ||
                            (typeof option.to === 'object' ? option.to.displayName || option.to.stop_name : option.to) ||
                            (option.legs?.[option.legs.length - 1]?.to?.name) ||
                            (option.endPoint?.name) ||
                            'Điểm đến của bạn'
                        }
                    </div>
                </div>
            )}
        </div>
    );
};

export default RouteOption;