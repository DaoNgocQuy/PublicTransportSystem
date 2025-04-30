package com.pts.repositories;

import com.pts.pojo.Users;
import java.util.List;

public interface UserRepository {
    Users getUserById(int id);
    Users getUserByUsername(String username);
    List<Users> getAllUsers();
    boolean addUser(Users user);
    boolean updateUser(Users user);
    boolean deleteUser(int id);
}
