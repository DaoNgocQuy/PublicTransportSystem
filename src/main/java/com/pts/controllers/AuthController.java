package com.pts.controllers;

import com.pts.pojo.Users;
import com.pts.services.CloudinaryService;
import com.pts.services.UserService;
import java.io.IOException;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String email,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) MultipartFile avatar, // Bỏ comment
            Model model) {
        
        try {
            // Xác thực dữ liệu cơ bản
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp");
                return "register";
            }
            
            // Kiểm tra username và email đã tồn tại chưa
            if (userService.existsByUsername(username)) {
                model.addAttribute("error", "Tên đăng nhập đã tồn tại");
                return "register";
            }
            
            if (userService.existsByEmail(email)) {
                model.addAttribute("error", "Email đã tồn tại");
                return "register";
            }
            
            // Xử lý avatar
            String avatarUrl = null;
            if (avatar != null && !avatar.isEmpty()) {
                try {
                    avatarUrl = cloudinaryService.uploadImage(avatar);
                } catch (IOException e) {
                    model.addAttribute("error", "Không thể upload ảnh: " + e.getMessage());
                    return "register";
                }
            }
            // Nếu không có avatar, có thể đặt ảnh mặc định
            if (avatarUrl == null) {
                avatarUrl = "https://res.cloudinary.com/dxxwcby8l/image/upload/v1620123456/default-avatar.png";
            }
            
            // Tạo đối tượng Users
            Users user = new Users();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setRole("ADMIN"); // Hoặc "USER" tùy vào logic của bạn
            user.setIsActive(true);
            user.setCreatedAt(new Date());
            user.setAvatarUrl(avatarUrl); // Bỏ comment
            
            // Đăng ký và redirect
            userService.registerUser(user);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "register";
        }
    }
}