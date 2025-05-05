package com.pts.services;

import com.pts.pojo.Users;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Users registerUser(Users user);
    Optional<Users> login(String username, String password);
    Users updateProfile(Users user);
    Optional<Users> getUserById(Integer id);
    boolean changePassword(Integer userId, String oldPassword, String newPassword);
    boolean updateLastLogin(Integer userId);
    
    // Kiểm tra tồn tại
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    
    // Phương thức cho phần quản trị
    Users registerNewUser(String username, String password, String email);
    Users registerNewUserWithAvatar(String username, String password, String email, String avatarUrl);
    List<Users> getAllUsers();
}