package com.pts.controllers;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
import com.pts.services.NotificationService;
import com.pts.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/notifications/settings")
public class ApiNotificationSettingsController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> saveSettings(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            
            if (userId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Vui lòng đăng nhập để sử dụng tính năng này");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            Integer routeId = (Integer) payload.get("route_id");
            Boolean notifyScheduleChanges = (Boolean) payload.get("notify_schedule_changes");
            Boolean notifyDelays = (Boolean) payload.get("notify_delays");
            
            if (routeId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "route_id là bắt buộc");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Thực hiện lưu thiết lập
            boolean result = notificationService.saveNotificationSetting(
                userId, routeId, notifyScheduleChanges, notifyDelays
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã lưu cài đặt thông báo");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể lưu cài đặt thông báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<?> deleteSettings(@PathVariable Integer routeId, HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            
            if (userId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Vui lòng đăng nhập để sử dụng tính năng này");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            boolean result = notificationService.deleteNotificationSetting(userId, routeId);
            if (result) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Đã xóa cài đặt thông báo");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể xóa cài đặt thông báo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserSettings(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            
            if (userId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Vui lòng đăng nhập để sử dụng tính năng này");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            List<Map<String, Object>> settings = notificationService.getNotificationSettings(userId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể lấy cài đặt thông báo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Phương thức private lấy userId từ JWT token
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                String username = JwtUtils.validateTokenAndGetUsername(token);
                
                if (username != null) {
                    Optional<Users> userOpt = userRepository.findByUsername(username);
                    if (userOpt.isPresent()) {
                        return userOpt.get().getId();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}