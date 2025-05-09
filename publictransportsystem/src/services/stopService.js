// src/services/stopService.js
import apiClient from './apiClient';

// Lấy tất cả điểm dừng hoặc tìm kiếm theo keyword
export const getStops = async (keyword = '') => {
    try {
        const params = keyword ? { keyword } : {};
        const response = await apiClient.get('/stops', { params });
        return response.data;
    } catch (error) {
        console.error('Error fetching stops:', error);
        throw error;
    }
};

// Lấy thông tin chi tiết của một điểm dừng
export const getStopById = async (id) => {
    try {
        const response = await apiClient.get(`/stops/${id}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching stop with ID ${id}:`, error);
        throw error;
    }
};

// Lấy tất cả điểm dừng của một tuyến
export const getStopsByRouteId = async (routeId) => {
    try {
        const response = await apiClient.get(`/stops/route/${routeId}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching stops for route ${routeId}:`, error);
        throw error;
    }
};

// Tìm điểm dừng gần vị trí người dùng
export const getNearbyStops = async (lat, lng, radius = 1000) => {
    try {
        const response = await apiClient.get(`/stops/nearby`, {
            params: { lat, lng, radius }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching nearby stops:', error);
        throw error;
    }
};