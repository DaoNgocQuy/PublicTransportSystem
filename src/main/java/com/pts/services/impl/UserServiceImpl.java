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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Users registerUser(Users user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty() && userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        // Đảm bảo password đã được mã hóa
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
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
    public Users updateProfile(Users user) {
        // Không cho phép thay đổi username hoặc password qua phương thức này
        Users existingUser = userRepository.getUserById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        // Giữ lại mật khẩu hiện tại
        user.setPassword(existingUser.getPassword());
        
        // Giữ nguyên các trường khác nếu không được cập nhật
        if (user.getUsername() == null) user.setUsername(existingUser.getUsername());
        if (user.getRole() == null) user.setRole(existingUser.getRole());
        if (user.getIsActive() == null) user.setIsActive(existingUser.getIsActive());
        if (user.getCreatedAt() == null) user.setCreatedAt(existingUser.getCreatedAt());
        if (user.getLastLogin() == null) user.setLastLogin(existingUser.getLastLogin());

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
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.updateUser(user);
        return true;
    }
    
    @Override
    public boolean updateLastLogin(Integer userId) {
        return userRepository.updateLastLogin(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
    
    @Override
    public Users registerNewUser(String username, String password, String email) {
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("ROLE_ADMIN"); // Đảm bảo người dùng có role ADMIN
        user.setIsActive(true); // Set trạng thái active
        user.setCreatedAt(new Date()); // Set thời gian tạo
        
        userRepository.addUser(user);
        return user;
    }

    @Override
    public Users registerNewUserWithAvatar(String username, String password, String email, String avatarUrl) {
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("ROLE_ADMIN"); // Đảm bảo người dùng có role ADMIN
        user.setIsActive(true); // Set trạng thái active
        user.setCreatedAt(new Date()); // Set thời gian tạo
        
        // Set avatar URL nếu có
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            user.setAvatarUrl(avatarUrl);
        }
        
        userRepository.addUser(user);
        return user;
    }
    
    @Override
    public List<Users> getAllUsers() {
        return userRepository.getAllUsers();
    }
}