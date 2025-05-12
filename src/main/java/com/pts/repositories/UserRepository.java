package com.pts.repositories;

import com.pts.pojo.Users;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    Optional<Users> findByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Users getUserByUsername(String username);
    List<Users> getAllUsers();
    boolean addUser(Users user);
    boolean updateUser(Users user);
    boolean deleteUser(int id);
    boolean updateLastLogin(int id);
    Users getUserById(Integer userId);
    boolean saveResetPasswordToken(Integer userId, String token, Date expiryTime);
    Optional<Integer> getUserIdByResetToken(String token);
    boolean isResetTokenExpired(String token);
    boolean deleteResetToken(String token);
}
