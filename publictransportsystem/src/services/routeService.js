// src/services/routeService.js
import apiClient from './apiClient';

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