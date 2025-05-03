package com.pts.services.impl;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
import com.pts.services.UserService;
import com.pts.services.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Users getUserById(int id) {
        return userRepository.getUserById(id);
    }

    @Override
    public Users getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    @Override
    public List<Users> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public boolean addUser(Users user) {
        return userRepository.addUser(user);
    }

    @Override
    public boolean updateUser(Users user) {
        return userRepository.updateUser(user);
    }

    @Override
    public boolean deleteUser(int id) {
        return userRepository.deleteUser(id);
    }
}
