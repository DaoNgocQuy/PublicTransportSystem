package com.pts.controllers;

import com.pts.pojo.Users;
import com.pts.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestPart("user") Users user,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            Users registeredUser = userService.registerUser(user, avatarFile);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        try {
//            return userService.login(username, password)
//                    .map(ResponseEntity::ok)
//                    .orElse(ResponseEntity.badRequest().body("Invalid username or password"));
            return userService.login(username, password)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Integer userId,
            @RequestPart("user") Users userDetails,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            Users updatedUser = userService.updateUserProfile(userId, userDetails, avatarFile);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password/{userId}")
    public ResponseEntity<?> changePassword(
            @PathVariable Integer userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            if (success) {
                return ResponseEntity.ok("Password changed successfully");
            } else {
                return ResponseEntity.badRequest().body("Invalid old password");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
