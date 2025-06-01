package com.pts.repositories;

import com.pts.pojo.NotificationSettings;
import com.pts.pojo.Notifications;
import java.util.List;
import java.util.Map;

public interface NotificationRepository {
    List<Map<String, Object>> getNotificationsByUserId(Integer userId);
    List<Map<String, Object>> getUnreadNotifications(Integer userId);
    boolean markAsRead(Integer notificationId, Integer userId);
    void markAllAsRead(Integer userId);
    List<Map<String, Object>> getNotificationSettings(Integer userId);
    boolean saveNotificationSetting(Integer userId, Integer routeId, Boolean notifyScheduleChanges, Boolean notifyDelays);
    boolean deleteNotificationSetting(Integer userId, Integer routeId);
    NotificationSettings findNotificationSettingByUserAndRoute(Integer userId, Integer routeId);
    NotificationSettings saveNotificationSettingWithPOJO(NotificationSettings setting);
    List<Map<String, Object>> getUsersSubscribedToRoute(Integer routeId);
    Notifications saveNotification(Notifications notification);
}