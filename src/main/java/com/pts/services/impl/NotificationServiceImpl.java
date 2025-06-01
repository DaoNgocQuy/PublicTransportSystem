package com.pts.services.impl;

import com.pts.repositories.NotificationRepository;
import com.pts.services.NotificationService;
import com.pts.services.EmailService;
import com.pts.pojo.Routes;
import com.pts.pojo.Schedules;
import com.pts.pojo.Notifications;
import com.pts.pojo.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.Date;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

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

    // Implement các method mới
    @Override
    public void sendRouteChangeNotification(Routes oldRoute, Routes newRoute) {
        try {
            List<Map<String, Object>> subscribers = getSubscribersForRoute(newRoute.getId());
            
            for (Map<String, Object> subscriber : subscribers) {
                Boolean notifyScheduleChanges = (Boolean) subscriber.get("notify_schedule_changes");
                
                if (notifyScheduleChanges != null && notifyScheduleChanges) {
                    Integer userId = (Integer) subscriber.get("user_id");
                    String email = (String) subscriber.get("email");
                    String fullName = (String) subscriber.get("full_name");
                    
                    // Tạo nội dung email
                    String emailContent = createRouteChangeEmailContent(fullName, oldRoute, newRoute);
                    
                    // Gửi email
                    emailService.sendEmail(email, "Thông báo thay đổi tuyến " + newRoute.getRouteName(), emailContent);
                    
                    // Lưu thông báo vào database
                    String message = "Tuyến " + newRoute.getRouteName() + " đã có thay đổi thông tin. Vui lòng kiểm tra lại.";
                    saveNotificationToDatabase(userId, newRoute.getId(), "Thay đổi tuyến", message, "ROUTE_CHANGE");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi gửi thông báo thay đổi tuyến: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendScheduleChangeNotification(List<Schedules> oldSchedules, List<Schedules> newSchedules, Routes route) {
        try {
            List<Map<String, Object>> subscribers = getSubscribersForRoute(route.getId());
            
            for (Map<String, Object> subscriber : subscribers) {
                Boolean notifyScheduleChanges = (Boolean) subscriber.get("notify_schedule_changes");
                
                if (notifyScheduleChanges != null && notifyScheduleChanges) {
                    Integer userId = (Integer) subscriber.get("user_id");
                    String email = (String) subscriber.get("email");
                    String fullName = (String) subscriber.get("full_name");
                    
                    // Tạo nội dung email
                    String emailContent = createScheduleChangeEmailContent(fullName, oldSchedules, newSchedules, route);
                    
                    // Gửi email
                    emailService.sendEmail(email, "Thông báo thay đổi lịch trình tuyến " + route.getRouteName(), emailContent);
                    
                    // Lưu thông báo vào database
                    String message = "Lịch trình tuyến " + route.getRouteName() + " đã có thay đổi. Vui lòng kiểm tra lại.";
                    saveNotificationToDatabase(userId, route.getId(), "Thay đổi lịch trình", message, "SCHEDULE_CHANGE");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi gửi thông báo thay đổi lịch trình: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Notifications saveNotificationToDatabase(Integer userId, Integer routeId, String title, String message, String type) {
        try {
            Notifications notification = new Notifications();
            
            // Set user
            Users user = new Users();
            user.setId(userId);
            notification.setUserId(user);
            
            notification.setMessage(message);
            notification.setNotificationType(type);
            notification.setRelatedEntityId(routeId);
            notification.setIsRead(false);
            notification.setCreatedAt(new Date());
            
            return notificationRepository.saveNotification(notification);
        } catch (Exception e) {
            System.err.println("Lỗi lưu thông báo vào database: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getSubscribersForRoute(Integer routeId) {
        return notificationRepository.getUsersSubscribedToRoute(routeId);
    }

    @Override
    public String createScheduleChangeEmailContent(String fullName, List<Schedules> oldSchedules, List<Schedules> newSchedules, Routes route) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>");
        content.append("<html><head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<style>");
        content.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f4f4f4; }");
        content.append(".container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        content.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; border-radius: 8px; margin-bottom: 30px; }");
        content.append(".schedule-box { background: #f8f9ff; border-left: 4px solid #667eea; padding: 15px; margin: 15px 0; border-radius: 5px; }");
        content.append(".time { font-weight: bold; color: #667eea; font-size: 18px; }");
        content.append(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; }");
        content.append(".btn { display: inline-block; background: #667eea; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; margin: 10px 0; }");
        content.append("</style>");
        content.append("</head><body>");
        
        content.append("<div class='container'>");
        content.append("<div class='header'>");
        content.append("<h1>Thông Báo Thay Đổi Lịch Trình</h1>");
        content.append("</div>");
        
        content.append("<p>Kính chào <strong>").append(fullName).append("</strong>,</p>");
        content.append("<p>Chúng tôi thông báo đến bạn về những thay đổi lịch trình của tuyến <strong style='color: #667eea;'>").append(route.getRouteName()).append("</strong>:</p>");
        
        content.append("<div class='schedule-box'>");
        content.append("<h3>Lịch trình mới:</h3>");
        for (Schedules schedule : newSchedules) {
            content.append("<div class='time'>").append(timeFormat.format(schedule.getDepartureTime()));
            if (schedule.getArrivalTime() != null) {
                content.append(" → ").append(timeFormat.format(schedule.getArrivalTime()));
            }
            content.append("</div>");
        }
        
        content.append("<div class='footer'>");
        content.append("<p>Email này được gửi tự động từ <strong>Hệ thống Giao thông Công cộng</strong></p>");
        content.append("<p>Nếu bạn không muốn nhận email này, vui lòng cập nhật cài đặt thông báo trong ứng dụng.</p>");
        content.append("</div>");
        
        content.append("</div>");
        content.append("</body></html>");
        
        return content.toString();
    }

    @Override
    public String createRouteChangeEmailContent(String fullName, Routes oldRoute, Routes newRoute) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>");
        content.append("<html><head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<style>");
        content.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f4f4f4; }");
        content.append(".container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        content.append(".header { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); color: white; padding: 20px; text-align: center; border-radius: 8px; margin-bottom: 30px; }");
        content.append(".change-box { background: #fff5f5; border-left: 4px solid #ff6b6b; padding: 15px; margin: 15px 0; border-radius: 5px; }");
        content.append(".old { color: #999; text-decoration: line-through; }");
        content.append(".new { color: #ff6b6b; font-weight: bold; }");
        content.append(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; }");
        content.append(".btn { display: inline-block; background: #ff6b6b; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; margin: 10px 0; }");
        content.append("</style>");
        content.append("</head><body>");
        
        content.append("<div class='container'>");
        content.append("<div class='header'>");
        content.append("<h1>Thông Báo Thay Đổi Tuyến</h1>");
        content.append("</div>");
        
        content.append("<p>Kính chào <strong>").append(fullName).append("</strong>,</p>");
        content.append("<p>Chúng tôi thông báo đến bạn về những thay đổi trong tuyến <strong style='color: #ff6b6b;'>").append(newRoute.getRouteName()).append("</strong>:</p>");
        
        content.append("<div class='change-box'>");
        content.append("<h3>Những thay đổi:</h3>");
        
        if (!Objects.equals(oldRoute.getRouteName(), newRoute.getRouteName())) {
            content.append("<p><strong>Tên tuyến:</strong><br>");
            content.append("<span class='old'>").append(oldRoute.getRouteName()).append("</span><br>");
            content.append("<span class='new'>→ ").append(newRoute.getRouteName()).append("</span></p>");
        }
        
        if (!Objects.equals(oldRoute.getStartLocation(), newRoute.getStartLocation())) {
            content.append("<p><strong>Điểm đầu:</strong><br>");
            content.append("<span class='old'>").append(oldRoute.getStartLocation()).append("</span><br>");
            content.append("<span class='new'>→ ").append(newRoute.getStartLocation()).append("</span></p>");
        }
        
        if (!Objects.equals(oldRoute.getEndLocation(), newRoute.getEndLocation())) {
            content.append("<p><strong>Điểm cuối:</strong><br>");
            content.append("<span class='old'>").append(oldRoute.getEndLocation()).append("</span><br>");
            content.append("<span class='new'>→ ").append(newRoute.getEndLocation()).append("</span></p>");
        }
        
        content.append("<div class='footer'>");
        content.append("<p>Email này được gửi tự động từ <strong>Hệ thống Giao thông Công cộng</strong></p>");
        content.append("<p>Nếu bạn không muốn nhận email này, vui lòng cập nhật cài đặt thông báo trong ứng dụng.</p>");
        content.append("</div>");
        
        content.append("</div>");
        content.append("</body></html>");
        
        return content.toString();
    }
}