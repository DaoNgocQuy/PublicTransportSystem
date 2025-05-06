import axios from "axios";

const BASE_URL = 'http://localhost:8080/PTS/';

export const endpoints = {
    // Auth endpoints
    login: "auth/login",
    register: "auth/register",
    getUserProfile: (id) => `auth/profile/${id}`,
    updateProfile: (id) => `auth/profile/${id}`,
    changePassword: (id) => `auth/change-password/${id}`
}

export const authApi = axios.create({
    baseURL: BASE_URL
});

// Thêm interceptor để gửi kèm token nếu cần
authApi.interceptors.request.use(config => {
    const user = JSON.parse(localStorage.getItem('user'));
    if (user && user.token) {
        config.headers.Authorization = `Bearer ${user.token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

export default axios.create({
    baseURL: BASE_URL
});