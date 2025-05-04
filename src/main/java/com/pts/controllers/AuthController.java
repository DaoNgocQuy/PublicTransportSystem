package com.pts.controllers;

import com.pts.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    
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
            Model model) {
        
        try {
            // Xác thực dữ liệu
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp");
                return "register";
            }
            
            if (userService.existsByUsername(username)) {
                model.addAttribute("error", "Tên đăng nhập đã tồn tại");
                return "register";
            }
            
            if (userService.existsByEmail(email)) {
                model.addAttribute("error", "Email đã tồn tại");
                return "register";
            }
            
            // Đăng ký người dùng mới
            userService.registerNewUser(username, password, email);
            
            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "register";
        }
    }
}