package com.pts.services.impl;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
import com.pts.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String uploadDir = "uploads/avatars/";

    @Override
    public Users registerUser(Users user, MultipartFile avatarFile) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("CITIZEN"); // Default role for new users

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = saveAvatar(avatarFile);
            user.setAvatarUrl(avatarUrl);
        }

        userRepository.addUser(user);
        return user;
    }

    @Override
    public Optional<Users> login(String username, String password) {
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }

    @Override
    public Users updateUserProfile(Integer userId, Users userDetails, MultipartFile avatarFile) {
        Users user = userRepository.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(userDetails.getEmail());
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = saveAvatar(avatarFile);
            user.setAvatarUrl(avatarUrl);
        }

        userRepository.updateUser(user);
        return user;
    }

    @Override
    public Optional<Users> getUserById(Integer id) {
        return Optional.ofNullable(userRepository.getUserById(id));
    }

    @Override
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        Users user = userRepository.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.updateUser(user);
        return true;
    }

    private String saveAvatar(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            return "/" + uploadDir + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save avatar", e);
        }
    }
}
