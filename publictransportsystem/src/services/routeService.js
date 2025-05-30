// src/services/routeService.js
import apiClient from './apiClient';
import { authApi } from '../configs/Apis';

export const getRoutes = async (keyword = '') => {
    try {
        const response = await apiClient.get('/routes', {
            params: { keyword }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching routes:', error);
        throw error;
    }
};

export const getRouteById = async (id) => {
    try {
        const response = await apiClient.get(`/routes/${id}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching route ${id}:`, error);
        throw error;
    }
};
export const findRoutes = async (originData, destinationData, options = {}) => {
    try {
        const userStr = sessionStorage.getItem('user');
        if (!userStr) {
            throw new Error('AUTH_REQUIRED');
        }

        // Get user token
        const user = JSON.parse(userStr);
        const token = user.token;

        // Verify required data
        if (!originData || !destinationData) {
            return {
                options: [],
                error: 'Thiếu thông tin điểm đi hoặc điểm đến'
            };
        }

        // Ensure we have valid coordinates
        if (!originData.latitude || !originData.longitude ||
            !destinationData.latitude || !destinationData.longitude) {
            return {
                options: [],
                error: 'Địa điểm không có tọa độ hợp lệ'
            };
        }

        try {
            // Convert coordinates to numbers to ensure proper formatting
            const fromLat = Number(originData.latitude);
            const fromLng = Number(originData.longitude);
            const toLat = Number(destinationData.latitude);
            const toLng = Number(destinationData.longitude);

            // Manually construct the query string to avoid encoding issues
            const queryParams = [
                `fromLat=${fromLat.toFixed(6)}`,
                `fromLng=${fromLng.toFixed(6)}`,
                `toLat=${toLat.toFixed(6)}`,
                `toLng=${toLng.toFixed(6)}`
            ];

            // Add optional parameters
            if (options.maxWalkingDistance) queryParams.push(`maxWalkDistance=${options.maxWalkingDistance}`);
            if (options.maxTransfers) queryParams.push(`maxTransfers=${options.maxTransfers}`);
            if (options.routePriority) queryParams.push(`routePriority=${options.routePriority}`);

            const apiUrl = `/api/routes/search?${queryParams.join('&')}`;
            console.log('Making request to:', apiUrl);

            // Make API request with token
            const response = await authApi.get(apiUrl, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            // Process the successful response
            if (response.data) {
                console.log('API response data:', response.data);

                // Process data based on format
                if (Array.isArray(response.data)) {
                    const routeOptions = processRouteData(response.data);
                    return { options: routeOptions };
                } else if (response.data.routes && Array.isArray(response.data.routes)) {
                    const routeOptions = processRouteData(response.data.routes);
                    return { options: routeOptions };
                }
            }

            // Empty response
            return {
                options: [],
                error: 'Không tìm thấy tuyến đường phù hợp'
            };
        } catch (apiError) {
            console.error('API error:', apiError);

            // Log detailed error information
            if (apiError.response) {
                console.error('API error details:', {
                    status: apiError.response.status,
                    statusText: apiError.response.statusText,
                    data: apiError.response.data,
                    headers: apiError.response.headers
                });
            }

            // Return mock data with a useful error message
            console.log('Generating mock data for UI display');
            return {
                options: [],
                error: `Lỗi khi tìm kiếm tuyến: ${apiError.message}`
            };
        }
    } catch (error) {
        console.error('Error in findRoutes:', error);

        // Make sure we always return a proper object structure, even on critical errors
        return {
            options: [],
            error: `Lỗi hệ thống: ${error.message}`
        };
    }
};
export const getRouteLegsDetails = async (routeOption) => {
    if (!routeOption || !routeOption.legs || routeOption.legs.length === 0) {
        return null;
    }

    try {
        // Get details for the bus legs
        const busLegs = routeOption.legs.filter(leg => leg.type === 'BUS');

        if (busLegs.length === 0) {
            return routeOption;
        }

        // Fetch detailed information for each bus leg if possible,
        // but continue even if the API call fails
        const detailedLegs = await Promise.all(
            busLegs.map(async (leg) => {
                if (leg.routeId) {
                    try {
                        console.log(`Fetching route details for route ${leg.routeId}`);

                        // Use the correct endpoint - this endpoint returns complete route details including stops
                        const details = await authApi.get(`api/routes/${leg.routeId}`);

                        // Extract stops from the route details response
                        const stopsData = details.data?.stopsInbound || details.data?.stopsOutbound || [];
                        console.log(`Got ${stopsData.length} stops for route ${leg.routeId}`);

                        return {
                            ...leg,
                            stops: stopsData,
                            detailsFetched: true
                        };
                    } catch (err) {
                        console.log(`Không thể tải trạm cho tuyến ${leg.routeId}, sử dụng dữ liệu có sẵn`, err);
                        console.error("API Error details:", {
                            status: err.response?.status,
                            statusText: err.response?.statusText,
                            data: err.response?.data,
                            url: err.config?.url
                        });

                        // Fall back to trying the stops endpoint via a different controller
                        try {
                            console.log(`Trying to get stops through stop controller for route ${leg.routeId}`);
                            const fallbackResponse = await authApi.get(`api/stops/route/${leg.routeId}`);
                            return {
                                ...leg,
                                stops: fallbackResponse.data,
                                detailsFetched: true
                            };
                        } catch (fallbackErr) {
                            console.error("Fallback also failed:", fallbackErr.message);
                            return leg;
                        }
                    }
                }
                return leg;
            })
        );

        // Return the full route option with any successfully updated legs
        return {
            ...routeOption,
            legs: routeOption.legs.map(leg => {
                if (leg.type !== 'BUS') return leg;
                const detailedLeg = detailedLegs.find(d => d.routeId === leg.routeId);
                return detailedLeg || leg;
            }),
            detailsFetched: true
        };
    } catch (error) {
        console.error('Error fetching route leg details:', error);
        // Return the original data if something went wrong
        return routeOption;
    }
};
function processRouteData(routes) {
    if (!Array.isArray(routes) || routes.length === 0) {
        return [];
    }

    // Xử lý dữ liệu thực từ API
    return routes.map((route, index) => {
        // Sử dụng dữ liệu thực từ API hoặc mặc định nếu không có
        const routeId = route.id || route.routeId || index;
        const routeName = route.name || route.routeName || `Tuyến ${routeId}`;
        const routeColor = route.routeColor || route.route_color || '#4CAF50';

        // Sử dụng totalTime và totalDistance từ API nếu có
        const totalTime = route.totalTime || (route.duration ? parseInt(route.duration) : 30);
        const totalDistance = route.totalDistance || route.distance || 5000; // mét
        const walkingDistance = route.walkingDistance || 500;
        const transfers = route.transfers || 0;

        // Lấy danh sách trạm từ API nếu có
        const stops = route.stops || [];
        const firstStop = stops.length > 0 ? stops[0] : null;
        const lastStop = stops.length > 0 ? stops[stops.length - 1] : null;

        // Tạo cấu trúc dữ liệu chuẩn để hiển thị
        return {
            id: routeId,
            totalTime: totalTime,
            totalDistance: totalDistance,
            walkingDistance: walkingDistance,
            transfers: transfers,
            routes: [
                {
                    number: routeId,
                    name: routeName,
                    color: routeColor
                }
            ],
            // Sử dụng legs trực tiếp từ API nếu có, nếu không tạo từ dữ liệu trạm
            legs: route.legs || createLegsFromStops(route, firstStop, lastStop, totalTime)
        };
    });
}
function createLegsFromStops(route, firstStop, lastStop, totalTime) {
    // Tạo chặng đi bộ đầu tiên
    const firstLeg = {
        type: 'WALK',
        distance: Math.floor(200 + Math.random() * 300), // 200-500m
        duration: 5,
        from: { name: 'Vị trí hiện tại' },
        to: firstStop ? {
            name: firstStop.stopName || firstStop.name,
            lat: firstStop.latitude || firstStop.lat,
            lng: firstStop.longitude || firstStop.lng
        } : { name: 'Điểm đón' }
    };

    // Tạo chặng xe buýt chính
    const busLeg = {
        type: 'BUS',
        routeId: route.id || route.routeId,
        routeNumber: route.routeNumber || route.id || '',
        routeName: route.name || route.routeName || '',
        routeColor: route.routeColor || route.route_color || '#4CAF50',
        duration: totalTime - 10, // trừ đi thời gian đi bộ
        stops: route.stops ? route.stops.length : (route.totalStops || 0),
        boardStop: firstStop ? {
            name: firstStop.stopName || firstStop.name,
            lat: firstStop.latitude || firstStop.lat,
            lng: firstStop.longitude || firstStop.lng
        } : { name: 'Điểm đầu' },
        alightStop: lastStop ? {
            name: lastStop.stopName || lastStop.name,
            lat: lastStop.latitude || lastStop.lat,
            lng: lastStop.longitude || lastStop.lng
        } : { name: 'Điểm cuối' }
    };

    // Tạo chặng đi bộ cuối cùng
    const lastLeg = {
        type: 'WALK',
        distance: Math.floor(200 + Math.random() * 300), // 200-500m
        duration: 5,
        from: lastStop ? {
            name: lastStop.stopName || lastStop.name,
            lat: lastStop.latitude || lastStop.lat,
            lng: lastStop.longitude || lastStop.lng
        } : { name: 'Điểm xuống' },
        to: { name: 'Điểm đến' }
    };

    return [firstLeg, busLeg, lastLeg];
}
// Lấy chi tiết cho một lộ trình cụ thể
export const getRouteDetails = async (routeId) => {
    try {
        const response = await authApi.get(`/api/routes/${routeId}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching details for route ${routeId}:`, error);
        throw error;
    }
};
