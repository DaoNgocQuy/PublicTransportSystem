import axios from "axios";

const BASE_URL = 'http://localhost:8080/PTS/';

export const endpoints = {
    // Auth endpoints
    login: "auth/login",
    register: "auth/register",
    getUserProfile: (id) => `auth/profile/${id}`,
    updateProfile: (id) => `auth/profile/${id}`,
    changePassword: (id) => `auth/change-password/${id}`,
    forgotPassword: "auth/forgot-password",
    resetPassword: "auth/reset-password",

    favorites: "api/favorites",
    favoritesById: (id) => `api/favorites/${id}`,
    notifications: "api/notifications",
    notificationsUnread: "api/notifications/unread",
    notificationsMarkRead: (id) => `api/notifications/${id}/read`,
    notificationsMarkAllRead: "api/notifications/read-all",
    notificationSettings: "api/notifications/settings",
    notificationSettingsById: (id) => `api/notifications/settings/${id}`,
    routeSearch: "api/routes/search",
    routeDirections: "api/directions",
    routeDetails: (id) => `api/routes/${id}`
}

export const authApi = axios.create({
    baseURL: BASE_URL,
    withCredentials: true
});

authApi.interceptors.request.use(config => {
    try {
        // Lấy từ sessionStorage thay vì localStorage
        const userStr = sessionStorage.getItem('user');
        if (userStr) {
            const user = JSON.parse(userStr);
            if (user && user.token) {
                console.log("Gửi token trong request");
                config.headers.Authorization = `Bearer ${user.token}`;
            } else {
                console.log("Không tìm thấy token trong user object");
            }
        } else {
            console.log("Không tìm thấy user trong sessionStorage");
        }
    } catch (error) {
        console.error('Lỗi khi xử lý thông tin user:', error);
    }
    return config;
}, error => {
    return Promise.reject(error);
});
authApi.interceptors.response.use(
    response => {
        // Kiểm tra nếu response là HTML thay vì JSON
        const contentType = response.headers['content-type'];
        if (contentType && contentType.includes('text/html') && typeof response.data === 'string') {
            // Phát hiện trang HTML (có thể là trang đăng nhập)
            console.error('Received HTML response instead of JSON');
            // Chuyển hướng đến trang đăng nhập
            sessionStorage.removeItem('user'); // Xóa thông tin user hết hạn
            window.location.href = '/login';
            return Promise.reject(new Error('AUTH_REQUIRED'));
        }
        return response;
    },
    error => {
        // Xử lý lỗi HTTP
        if (error.response && error.response.status === 401) {
            console.error('Authentication error (401)');
            sessionStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);
export default axios.create({
    baseURL: BASE_URL
});
