package com.pts.controllers;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
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
@RequestMapping("/auth")
public class ApiAuthController {

    @Autowired
    private com.pts.services.EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserRepository userRepository; 

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of(
            "message", "API endpoint đang hoạt động",
            "time", new Date().toString()
        );
    }

    @GetMapping("/test-connection")
    public ResponseEntity<?> testConnection() {
        return ResponseEntity.ok(Map.of(
            "message", "API connection successful",
            "timestamp", new Date()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) MultipartFile avatar) {
        
        try {
            // Kiểm tra username trước khi tạo user
            System.out.println("Checking if username exists: " + username);
            if (userService.existsByUsername(username)) {
                System.out.println("Username already exists: " + username);
                return ResponseEntity.badRequest().body(Map.of("error", "Tên đăng nhập đã tồn tại"));
            }
            
            // Kiểm tra email
            if (userService.existsByEmail(email)) {
                System.out.println("Email already exists: " + email);
                return ResponseEntity.badRequest().body(Map.of("error", "Email đã tồn tại"));
            }
            
            // Kiểm tra phone
            if (phone != null && !phone.isEmpty() && userService.existsByPhone(phone)) {
                System.out.println("Phone already exists: " + phone);
                return ResponseEntity.badRequest().body(Map.of("error", "Số điện thoại đã tồn tại"));
            }
            
            // Kiểm tra mật khẩu
            if (password.length() < 8) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu phải có ít nhất 8 ký tự"));
            }
            
            // Kiểm tra mật khẩu có chứa ký tự đặc biệt, chữ hoa, chữ thường và số
            boolean hasDigit = false;
            boolean hasLower = false;
            boolean hasUpper = false;
            boolean hasSpecial = false;
            String specialChars = "@$!%*?&";
            
            for (char c : password.toCharArray()) {
                if (Character.isDigit(c)) {
                    hasDigit = true;
                } else if (Character.isLowerCase(c)) {
                    hasLower = true;
                } else if (Character.isUpperCase(c)) {
                    hasUpper = true;
                } else if (specialChars.contains(String.valueOf(c))) {
                    hasSpecial = true;
                }
            }
            
            if (!hasDigit || !hasLower || !hasUpper || !hasSpecial) {
                return ResponseEntity.badRequest().body(Map.of("error", 
                    "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt (@$!%*?&)"));
            }
            
            // Upload avatar nếu có
            String avatarUrl = null;
            if (avatar != null && !avatar.isEmpty()) {
                System.out.println("Uploading avatar for user: " + username);
                avatarUrl = cloudinaryService.uploadImage(avatar);
                System.out.println("Avatar URL: " + avatarUrl);
            }
            
            // Tạo đối tượng Users
            Users user = new Users();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setRole("USER");
            user.setIsActive(true);
            user.setCreatedAt(new Date());
            user.setAvatarUrl(avatarUrl);
            
            System.out.println("Saving user to database: " + username);
            
            try {
                // Lưu user vào DB
                Users savedUser = userService.registerUser(user);

                if (savedUser == null || savedUser.getId() == null) {
                    System.err.println("User was returned but without ID");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Lỗi khi lưu người dùng"));
                }
                
                System.out.println("User successfully registered with ID: " + savedUser.getId());
                
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
            } catch (RuntimeException e) {
                // Bắt lỗi từ UserService
                System.err.println("Error when registering user: " + e.getMessage());
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in register endpoint: " + e.getMessage());
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

    @PostMapping("/change-password/{userId}")
    public ResponseEntity<?> changePassword(
            @PathVariable Integer userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        
        try {
            // Xác thực người dùng
            Optional<Users> userOpt = userService.getUserById(userId);
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy người dùng"));
            }
            
            Users user = userOpt.get();
            
            // Kiểm tra mật khẩu cũ
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Mật khẩu hiện tại không đúng"));
            }
            
            // Kiểm tra mật khẩu mới có giống mật khẩu cũ không
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Mật khẩu mới không được trùng với mật khẩu hiện tại"));
            }
            
            // Cập nhật mật khẩu mới
            user.setPassword(passwordEncoder.encode(newPassword));
            boolean updated = userService.updateUser(user);
            
            if (!updated) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể cập nhật mật khẩu"));
            }
            
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            // Kiểm tra email có tồn tại trong hệ thống không
            Optional<Users> userOpt = userRepository.findByEmail(email);
            
            if (!userOpt.isPresent()) {
                // Vì lý do bảo mật, không nên tiết lộ email tồn tại hay không
                return ResponseEntity.ok(Map.of(
                    "message", "Nếu email tồn tại trong hệ thống, hướng dẫn khôi phục mật khẩu sẽ được gửi đến email của bạn"
                ));
            }
            
            Users user = userOpt.get();
            
            // Tạo token ngẫu nhiên
            String token = generateResetToken();
            
            // Lưu token và thời gian hết hạn (30 phút từ thời điểm hiện tại)
            Date expiryTime = new Date(System.currentTimeMillis() + 30 * 60 * 1000); // 30 phút
            boolean saved = userService.saveResetPasswordToken(user.getId(), token, expiryTime);
            
            if (!saved) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Không thể xử lý yêu cầu đặt lại mật khẩu. Vui lòng thử lại sau."));
            }
            
            // Gửi email với mã xác nhận
            boolean emailSent = emailService.sendResetPasswordEmail(user.getEmail(), token, user.getFullName());
            
            if (!emailSent) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau."));
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Hướng dẫn đặt lại mật khẩu đã được gửi đến email " + maskEmail(email)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            // Kiểm tra token có hợp lệ không
            Optional<Integer> userIdOpt = userService.validateResetPasswordToken(token);
            
            if (!userIdOpt.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token không hợp lệ hoặc đã hết hạn"));
            }
            
            Integer userId = userIdOpt.get();
            
            // Kiểm tra độ phức tạp của mật khẩu
            boolean hasDigit = false;
            boolean hasLower = false;
            boolean hasUpper = false;
            boolean hasSpecial = false;
            String specialChars = "@$!%*?&";
            
            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Mật khẩu phải có ít nhất 8 ký tự"));
            }
            
            for (char c : newPassword.toCharArray()) {
                if (Character.isDigit(c)) {
                    hasDigit = true;
                } else if (Character.isLowerCase(c)) {
                    hasLower = true;
                } else if (Character.isUpperCase(c)) {
                    hasUpper = true;
                } else if (specialChars.contains(String.valueOf(c))) {
                    hasSpecial = true;
                }
            }
            
            if (!hasDigit || !hasLower || !hasUpper || !hasSpecial) {
                return ResponseEntity.badRequest().body(Map.of("error", 
                    "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt (@$!%*?&)"));
            }
            
            // Cập nhật mật khẩu mới
            Users user = userService.getUserById(userId).get();
            user.setPassword(passwordEncoder.encode(newPassword));
            boolean updated = userService.updateUser(user);
            
            if (!updated) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể đặt lại mật khẩu. Vui lòng thử lại sau."));
            }
            
            // Xóa token đã sử dụng
            userService.deleteResetPasswordToken(token);
            
            return ResponseEntity.ok(Map.of(
                "message", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới."
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Hàm hỗ trợ tạo token
    private String generateResetToken() {
        byte[] randomBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // Hàm che một phần email
    private String maskEmail(String email) {
        if (email == null || email.length() <= 4 || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        String maskedUsername = username.substring(0, Math.min(2, username.length())) + 
                            "*".repeat(Math.max(0, username.length() - 2));
        
        return maskedUsername + "@" + domain;
    }
}