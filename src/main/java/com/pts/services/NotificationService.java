package com.pts.services;

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
}