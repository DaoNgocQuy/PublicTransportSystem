package com.pts.repositories.impl;

import com.pts.pojo.NotificationSettings;
import com.pts.pojo.Notifications;
import com.pts.pojo.Routes;
import com.pts.pojo.Users;
import com.pts.repositories.NotificationRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class NotificationRepositoryImpl implements NotificationRepository {

    @Autowired
    private LocalSessionFactoryBean sessionFactory;

    @Override
    public List<Map<String, Object>> getNotificationsByUserId(Integer userId) {
        Session session = sessionFactory.getObject().getCurrentSession();

        Query q = session.createNativeQuery(
                "SELECT id, user_id, message, notification_type, related_entity_id, is_read, created_at "
                + "FROM notifications WHERE user_id = :userId "
                + "ORDER BY created_at DESC"
        );
        q.setParameter("userId", userId);

        List<Object[]> results = q.getResultList();
        List<Map<String, Object>> notifications = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", row[0]);
            notification.put("user_id", row[1]);
            notification.put("message", row[2]);
            notification.put("notification_type", row[3]);
            notification.put("related_entity_id", row[4]);
            notification.put("is_read", row[5]);
            notification.put("created_at", row[6]);
            notifications.add(notification);
        }

        return notifications;
    }

    @Override
    public List<Map<String, Object>> getUnreadNotifications(Integer userId) {
        Session session = sessionFactory.getObject().getCurrentSession();

        Query q = session.createNativeQuery(
                "SELECT id, user_id, message, notification_type, related_entity_id, is_read, created_at "
                + "FROM notifications WHERE user_id = :userId AND is_read = 0 "
                + "ORDER BY created_at DESC"
        );
        q.setParameter("userId", userId);

        List<Object[]> results = q.getResultList();
        List<Map<String, Object>> notifications = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", row[0]);
            notification.put("user_id", row[1]);
            notification.put("message", row[2]);
            notification.put("notification_type", row[3]);
            notification.put("related_entity_id", row[4]);
            notification.put("is_read", row[5]);
            notification.put("created_at", row[6]);
            notifications.add(notification);
        }

        return notifications;
    }

    @Override
    public boolean markAsRead(Integer notificationId, Integer userId) {
        Session session = sessionFactory.getObject().getCurrentSession();

        Query q = session.createNativeQuery(
                "UPDATE notifications SET is_read = 1 "
                + "WHERE id = :notificationId AND user_id = :userId"
        );
        q.setParameter("notificationId", notificationId);
        q.setParameter("userId", userId);

        int result = q.executeUpdate();
        return result > 0;
    }

    @Override
    public void markAllAsRead(Integer userId) {
        Session session = sessionFactory.getObject().getCurrentSession();

        Query q = session.createNativeQuery(
                "UPDATE notifications SET is_read = 1 "
                + "WHERE user_id = :userId AND is_read = 0"
        );
        q.setParameter("userId", userId);

        q.executeUpdate();
    }

    @Override
    public List<Map<String, Object>> getNotificationSettings(Integer userId) {
        Session session = sessionFactory.getObject().getCurrentSession();

        // Giả sử bạn có bảng notification_settings
        Query q = session.createNativeQuery(
                "SELECT ns.id, ns.user_id, ns.route_id, r.name AS route_name, "
                + "ns.notify_schedule_changes, ns.notify_delays, ns.created_at "
                + "FROM notification_settings ns "
                + "JOIN routes r ON ns.route_id = r.id "
                + "WHERE ns.user_id = :userId"
        );
        q.setParameter("userId", userId);

        List<Object[]> results = q.getResultList();
        List<Map<String, Object>> settings = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> setting = new HashMap<>();
            setting.put("id", row[0]);
            setting.put("user_id", row[1]);
            setting.put("route_id", row[2]);
            setting.put("route_name", row[3]);
            setting.put("notify_schedule_changes", row[4]);
            setting.put("notify_delays", row[5]);
            setting.put("created_at", row[6]);
            settings.add(setting);
        }

        return settings;
    }

    @Override
    public boolean saveNotificationSetting(Integer userId, Integer routeId, Boolean notifyScheduleChanges, Boolean notifyDelays) {
        Session session = sessionFactory.getObject().getCurrentSession();

        try {
            System.out.println("Saving notification setting for user: " + userId + ", route: " + routeId);
            System.out.println("Settings: scheduleChanges=" + notifyScheduleChanges + ", delays=" + notifyDelays);
            
            // Kiểm tra xem setting đã tồn tại chưa
            Query checkQuery = session.createNativeQuery(
                    "SELECT COUNT(*) FROM notification_settings WHERE user_id = :userId AND route_id = :routeId"
            );
            checkQuery.setParameter("userId", userId);
            checkQuery.setParameter("routeId", routeId);

            Long count = (Long) checkQuery.getSingleResult();
            System.out.println("Existing settings count: " + count);

            // Nếu đã tồn tại, cập nhật
            if (count > 0) {
                System.out.println("Updating existing settings");
                Query updateQuery = session.createNativeQuery(
                        "UPDATE notification_settings SET notify_schedule_changes = :scheduleChanges, "
                        + "notify_delays = :delays WHERE user_id = :userId AND route_id = :routeId"
                );
                updateQuery.setParameter("scheduleChanges", notifyScheduleChanges);
                updateQuery.setParameter("delays", notifyDelays);
                updateQuery.setParameter("userId", userId);
                updateQuery.setParameter("routeId", routeId);

                int result = updateQuery.executeUpdate();
                System.out.println("Update result: " + result + " row(s) affected");
                return result > 0;
            } 
            // Nếu chưa tồn tại, thêm mới
            else {
                System.out.println("Creating new settings");
                Query insertQuery = session.createNativeQuery(
                        "INSERT INTO notification_settings (user_id, route_id, notify_schedule_changes, notify_delays, created_at) "
                        + "VALUES (:userId, :routeId, :scheduleChanges, :delays, NOW())"
                );
                insertQuery.setParameter("userId", userId);
                insertQuery.setParameter("routeId", routeId);
                insertQuery.setParameter("scheduleChanges", notifyScheduleChanges);
                insertQuery.setParameter("delays", notifyDelays);

                int result = insertQuery.executeUpdate();
                System.out.println("Insert result: " + result + " row(s) affected");
                return result > 0;
            }
        } catch (Exception e) {
            System.out.println("Error in saveNotificationSetting: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteNotificationSetting(Integer userId, Integer routeId) {
        Session session = sessionFactory.getObject().getCurrentSession();

        try {
            Query q = session.createNativeQuery(
                    "DELETE FROM notification_settings WHERE user_id = :userId AND route_id = :routeId"
            );
            q.setParameter("userId", userId);
            q.setParameter("routeId", routeId);

            int result = q.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Thêm phương thức mới
    @Override
    public NotificationSettings findNotificationSettingByUserAndRoute(Integer userId, Integer routeId) {
        Session session = sessionFactory.getObject().getCurrentSession();

        try {
            Users user = session.get(Users.class, userId);
            Routes route = session.get(Routes.class, routeId);

            if (user == null || route == null) {
                return null;
            }

            Query<NotificationSettings> q = session.createQuery(
                    "FROM NotificationSettings n WHERE n.userId = :user AND n.routeId = :route",
                    NotificationSettings.class
            );
            q.setParameter("user", user);
            q.setParameter("route", route);

            return q.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public NotificationSettings saveNotificationSettingWithPOJO(NotificationSettings setting) {
        Session session = sessionFactory.getObject().getCurrentSession();

        try {
            session.saveOrUpdate(setting);
            return setting;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getUsersSubscribedToRoute(Integer routeId) {
        Session session = sessionFactory.getObject().getCurrentSession();

        String sql = "SELECT ns.user_id, u.email, u.full_name, ns.notify_schedule_changes, ns.notify_delays "
                + "FROM notification_settings ns "
                + "JOIN users u ON ns.user_id = u.id "
                + "WHERE ns.route_id = :routeId";
        
        Query query = session.createNativeQuery(sql);
        query.setParameter("routeId", routeId);
        
        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> subscribers = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> subscriber = new HashMap<>();
            subscriber.put("user_id", row[0]);
            subscriber.put("email", row[1]);
            subscriber.put("full_name", row[2]);
            subscriber.put("notify_schedule_changes", row[3]);
            subscriber.put("notify_delays", row[4]);
            subscribers.add(subscriber);
        }
        
        return subscribers;
    }

    // Thêm method mới
    @Override
    public Notifications saveNotification(Notifications notification) {
        Session session = sessionFactory.getObject().getCurrentSession();
        try {
            session.save(notification);
            return notification;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}