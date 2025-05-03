package com.pts.services;

import com.pts.pojo.Users;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

public interface UserService {
    Users registerUser(Users user, MultipartFile avatarFile);
    Optional<Users> login(String username, String password);
    Users updateUserProfile(Integer userId, Users userDetails, MultipartFile avatarFile);
    Optional<Users> getUserById(Integer id);
    boolean changePassword(Integer userId, String oldPassword, String newPassword);
}
