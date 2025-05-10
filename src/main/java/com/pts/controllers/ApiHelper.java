package com.pts.controllers;

import com.pts.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import com.pts.pojo.Users;
import java.util.Optional;

@Component
public class ApiHelper {
    
    @Autowired
    private UserService userService;
    
    public Integer getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Giải mã token đơn giản (format: btoa(id:username:timestamp))
                String decodedToken = new String(java.util.Base64.getDecoder().decode(token));
                String[] parts = decodedToken.split(":");
                if (parts.length > 0) {
                    return Integer.parseInt(parts[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public Optional<Users> getUserFromToken(HttpServletRequest request) {
        Integer userId = getUserIdFromToken(request);
        if (userId != null) {
            return userService.getUserById(userId);
        }
        return Optional.empty();
    }
}