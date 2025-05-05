package com.pts.controllers;

import com.pts.pojo.Users;
import com.pts.services.CloudinaryService;
import com.pts.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) MultipartFile avatar) {
        
        try {
            // Kiểm tra username, email và phone đã tồn tại chưa
            if (userService.existsByUsername(username)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên đăng nhập đã tồn tại"));
            }
            
            if (userService.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email đã tồn tại"));
            }
            
            if (phone != null && !phone.isEmpty() && userService.existsByPhone(phone)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Số điện thoại đã tồn tại"));
            }
            
            // Upload avatar nếu có
            String avatarUrl = null;
            if (avatar != null && !avatar.isEmpty()) {
                avatarUrl = cloudinaryService.uploadImage(avatar);
            }
            
            // Tạo đối tượng Users
            Users user = new Users();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setRole("ROLE_USER"); // User thường
            user.setIsActive(true);
            user.setCreatedAt(new Date());
            user.setAvatarUrl(avatarUrl);
            
            // Lưu user vào DB
            Users savedUser = userService.registerUser(user);
            
            // Trả về thông tin user (không bao gồm password)
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedUser.getId());
            response.put("username", savedUser.getUsername());
            response.put("email", savedUser.getEmail());
            response.put("fullName", savedUser.getFullName());
            response.put("phone", savedUser.getPhone());
            response.put("role", savedUser.getRole());
            response.put("avatarUrl", savedUser.getAvatarUrl());
            response.put("createdAt", savedUser.getCreatedAt());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        try {
            Optional<Users> result = userService.login(username, password);
            
            if (result.isPresent()) {
                Users user = result.get();
                
                // Cập nhật thời gian đăng nhập cuối
                user.setLastLogin(new Date());
                userService.updateLastLogin(user.getId());
                
                // Trả về thông tin user (không bao gồm password)
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                response.put("fullName", user.getFullName());
                response.put("phone", user.getPhone());
                response.put("role", user.getRole());
                response.put("avatarUrl", user.getAvatarUrl());
                response.put("lastLogin", user.getLastLogin());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Tên đăng nhập hoặc mật khẩu không đúng"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Integer id) {
        try {
            Optional<Users> user = userService.getUserById(id);
            if (user.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.get().getId());
                response.put("username", user.get().getUsername());
                response.put("email", user.get().getEmail());
                response.put("fullName", user.get().getFullName());
                response.put("phone", user.get().getPhone());
                response.put("role", user.get().getRole());
                response.put("avatarUrl", user.get().getAvatarUrl());
                response.put("createdAt", user.get().getCreatedAt());
                response.put("lastLogin", user.get().getLastLogin());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Integer id,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) MultipartFile avatar) {
        
        try {
            Optional<Users> userOpt = userService.getUserById(id);
            if (!userOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Users user = userOpt.get();
            
            // Cập nhật thông tin
            if (fullName != null && !fullName.isEmpty()) {
                user.setFullName(fullName);
            }
            
            if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
                if (userService.existsByEmail(email)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Email đã tồn tại"));
                }
                user.setEmail(email);
            }
            
            if (phone != null && !phone.isEmpty() && !phone.equals(user.getPhone())) {
                if (userService.existsByPhone(phone)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Số điện thoại đã tồn tại"));
                }
                user.setPhone(phone);
            }
            
            // Upload avatar mới nếu có
            if (avatar != null && !avatar.isEmpty()) {
                String avatarUrl = cloudinaryService.uploadImage(avatar);
                user.setAvatarUrl(avatarUrl);
            }
            
            // Cập nhật user
            Users updatedUser = userService.updateProfile(user);
            
            // Trả về thông tin đã cập nhật
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("email", updatedUser.getEmail());
            response.put("fullName", updatedUser.getFullName());
            response.put("phone", updatedUser.getPhone());
            response.put("avatarUrl", updatedUser.getAvatarUrl());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/change-password/{id}")
    public ResponseEntity<?> changePassword(
            @PathVariable Integer id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        
        try {
            boolean result = userService.changePassword(id, oldPassword, newPassword);
            if (result) {
                return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu cũ không đúng"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}