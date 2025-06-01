package com.pts.services.impl;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
import com.pts.services.UserService;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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

        // Đảm bảo role phù hợp với ràng buộc database
        if (user.getRole() == null || user.getRole().startsWith("ROLE_")) {
            String role = user.getRole() != null ? user.getRole().replace("ROLE_", "") : "USER";
            user.setRole(role);
        }

        // Đảm bảo password đã được mã hóa
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Thêm log để debug
        System.out.println("Attempting to add user to database: " + user.getUsername());
        
        boolean success = userRepository.addUser(user);
        
        if (!success) {
            System.err.println("Failed to add user to database: " + user.getUsername());
            throw new RuntimeException("Không thể lưu thông tin người dùng. Vui lòng thử lại sau.");
        }
        
        System.out.println("User successfully added to database with ID: " + user.getId());
        
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
    public Optional<Users> getUserById(Integer userId) {
        Users user = userRepository.getUserById(userId);
        return Optional.ofNullable(user);
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
    public boolean updateUser(Users user) {
        return userRepository.updateUser(user);
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
    public List<Users> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public Users registerNewUser(String username, String password, String email) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Users registerNewUserWithAvatar(String username, String password, String email, String avatarUrl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean saveResetPasswordToken(Integer userId, String token, Date expiryTime) {
        return userRepository.saveResetPasswordToken(userId, token, expiryTime);
    }

    @Override
    public Optional<Integer> validateResetPasswordToken(String token) {
        // Kiểm tra token có tồn tại không
        Optional<Integer> userIdOpt = userRepository.getUserIdByResetToken(token);
        
        if (!userIdOpt.isPresent()) {
            return Optional.empty();
        }
        
        // Kiểm tra token có hết hạn chưa
        boolean isExpired = userRepository.isResetTokenExpired(token);
        if (isExpired) {
            return Optional.empty();
        }
        
        return userIdOpt;
    }

    @Override
    public boolean deleteResetPasswordToken(String token) {
        return userRepository.deleteResetToken(token);
    }
}