package com.pts.services.impl;

import com.pts.repositories.NotificationRepository;
import com.pts.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public List<Map<String, Object>> getNotificationsByUserId(Integer userId) {
        return notificationRepository.getNotificationsByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getUnreadNotifications(Integer userId) {
        return notificationRepository.getUnreadNotifications(userId);
    }

    @Override
    public boolean markAsRead(Integer notificationId, Integer userId) {
        return notificationRepository.markAsRead(notificationId, userId);
    }

    @Override
    public void markAllAsRead(Integer userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    public List<Map<String, Object>> getNotificationSettings(Integer userId) {
        return notificationRepository.getNotificationSettings(userId);
    }

    @Override
    public boolean saveNotificationSetting(Integer userId, Integer routeId, Boolean notifyScheduleChanges, Boolean notifyDelays) {
        return notificationRepository.saveNotificationSetting(userId, routeId, notifyScheduleChanges, notifyDelays);
    }

    @Override
    public boolean deleteNotificationSetting(Integer userId, Integer routeId) {
        return notificationRepository.deleteNotificationSetting(userId, routeId);
    }
}