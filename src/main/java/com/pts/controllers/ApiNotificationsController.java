package com.pts.controllers;

import com.pts.pojo.Users;
import com.pts.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/notifications")
public class ApiNotificationsController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<?> getUserNotifications() {
        try {
            // Lấy thông tin user đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users currentUser = (Users) authentication.getPrincipal();
            
            List<Map<String, Object>> notifications = notificationService.getNotificationsByUserId(currentUser.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể lấy danh sách thông báo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        try {
            // Lấy thông tin user đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users currentUser = (Users) authentication.getPrincipal();
            
            List<Map<String, Object>> notifications = notificationService.getUnreadNotifications(currentUser.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể lấy danh sách thông báo chưa đọc");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer id) {
        try {
            // Lấy thông tin user đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users currentUser = (Users) authentication.getPrincipal();
            
            boolean result = notificationService.markAsRead(id, currentUser.getId());
            if (result) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Đã đánh dấu thông báo là đã đọc");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể đánh dấu thông báo là đã đọc");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        try {
            // Lấy thông tin user đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users currentUser = (Users) authentication.getPrincipal();
            
            notificationService.markAllAsRead(currentUser.getId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã đánh dấu tất cả thông báo là đã đọc");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể đánh dấu tất cả thông báo là đã đọc");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}