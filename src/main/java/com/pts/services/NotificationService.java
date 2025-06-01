package com.pts.services;

import com.pts.pojo.Routes;
import com.pts.pojo.Schedules;
import com.pts.pojo.Notifications;
import java.util.List;
import java.util.Map;

public interface NotificationService {
    // Notifications
    List<Map<String, Object>> getNotificationsByUserId(Integer userId);
    List<Map<String, Object>> getUnreadNotifications(Integer userId);
    boolean markAsRead(Integer notificationId, Integer userId);
    void markAllAsRead(Integer userId);
    
    // Notification settings
    List<Map<String, Object>> getNotificationSettings(Integer userId);
    boolean saveNotificationSetting(Integer userId, Integer routeId, Boolean notifyScheduleChanges, Boolean notifyDelays);
    boolean deleteNotificationSetting(Integer userId, Integer routeId);
    
    // Gửi thông báo
    void sendRouteChangeNotification(Routes oldRoute, Routes newRoute);
    void sendScheduleChangeNotification(List<Schedules> oldSchedules, List<Schedules> newSchedules, Routes route);
    Notifications saveNotificationToDatabase(Integer userId, Integer routeId, String title, String message, String type);
    List<Map<String, Object>> getSubscribersForRoute(Integer routeId);
    String createRouteChangeEmailContent(String fullName, Routes oldRoute, Routes newRoute);
    String createScheduleChangeEmailContent(String fullName, List<Schedules> oldSchedules, List<Schedules> newSchedules, Routes route);
}