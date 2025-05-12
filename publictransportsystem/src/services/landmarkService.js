import { authApi } from '../configs/Apis';

// Lấy tất cả các landmarks
export const getLandmarks = async () => {
    try {
        const response = await authApi.get('/api/landmarks');
        return response.data;
    } catch (error) {
        console.error('Error fetching landmarks:', error);
        throw error;
    }
};

// Tìm kiếm landmarks theo từ khóa
export const searchLandmarks = async (keyword) => {
    try {
        const response = await authApi.get(`/api/landmarks/search?query=${encodeURIComponent(keyword)}`);
        return response.data;
    } catch (error) {
        console.error('Error searching landmarks:', error);
        throw error;
    }
};

// Lấy landmarks gần vị trí người dùng
export const getNearbyLandmarks = async (latitude, longitude, radius = 1000) => {
    try {
        const response = await authApi.get('/api/landmarks/nearby', {
            params: { lat: latitude, lng: longitude, radius }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching nearby landmarks:', error);
        throw error;
    }
};

// Lấy chi tiết landmark theo ID
export const getLandmarkById = async (id) => {
    try {
        const response = await authApi.get(`/api/landmarks/${id}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching landmark with ID ${id}:`, error);
        throw error;
    }
};