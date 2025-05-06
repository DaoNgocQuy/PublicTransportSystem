package com.pts.repositories.impl;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;

import jakarta.transaction.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<Users> userMapper = new RowMapper<Users>() {
        @Override
        public Users mapRow(ResultSet rs, int rowNum) throws SQLException {
            Users u = new Users();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setPassword(rs.getString("password"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));
            u.setAvatarUrl(rs.getString("avatar_url"));
            u.setFullName(rs.getString("full_name"));
            u.setPhone(rs.getString("phone"));
            u.setCreatedAt(rs.getTimestamp("created_at"));
            u.setLastLogin(rs.getTimestamp("last_login"));
            u.setIsActive(rs.getBoolean("is_active"));
            return u;
        }
    };
    
    @Override
    public Optional<Users> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<Users> results = jdbcTemplate.query(sql, userMapper, username);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<Users> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<Users> results = jdbcTemplate.query(sql, userMapper, email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<Users> findByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";
        List<Users> results = jdbcTemplate.query(sql, userMapper, phone);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByUsername(String username) {
        try {
            System.out.println("Checking if username exists: " + username);
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
            System.out.println("Result for username " + username + ": " + count);
            return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("Error checking username existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
            System.out.println("Result for email " + email + ": " + count);
            return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean existsByPhone(String phone) {
        try {
        String sql = "SELECT COUNT(*) FROM users WHERE phone = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, phone);
        System.out.println("Result for phone " + phone + ": " + count);
        return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("Error checking phone existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Users getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<Users> results = jdbcTemplate.query(sql, userMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public Users getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<Users> results = jdbcTemplate.query(sql, userMapper, username);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Users> getAllUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userMapper);
    }

    @Override
    @Transactional
    public boolean addUser(Users user) {
        String sql = "INSERT INTO users(username, password, email, role, avatar_url, full_name, phone, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try {
            System.out.println("SQL: " + sql);
            System.out.println("User data: " + user.getUsername() + ", " + user.getEmail() + ", " + user.getFullName());
            
            // Kiểm tra kết nối trực tiếp
            try {
                Integer test = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                System.out.println("DB connection test: " + (test != null && test == 1 ? "OK" : "FAILED"));
            } catch (Exception e) {
                System.err.println("DB connection test failed: " + e.getMessage());
            }
            
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            int result = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getRole());
                ps.setString(5, user.getAvatarUrl());
                ps.setString(6, user.getFullName());
                ps.setString(7, user.getPhone());
                ps.setBoolean(8, user.getIsActive() != null ? user.getIsActive() : true);
                ps.setTimestamp(9, user.getCreatedAt() != null ? 
                    new java.sql.Timestamp(user.getCreatedAt().getTime()) : 
                    new java.sql.Timestamp(System.currentTimeMillis()));
                
                // Log SQL để debug
                System.out.println("Executing SQL with params: " + ps);
                
                return ps;
            }, keyHolder);
            
            System.out.println("Insert result: " + result + ", key: " + (keyHolder.getKey() != null ? keyHolder.getKey() : "null"));
            
            if (result > 0 && keyHolder.getKey() != null) {
                user.setId(keyHolder.getKey().intValue());
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateUser(Users user) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, role = ?, avatar_url = ?, full_name = ?, phone = ?, is_active = ? WHERE id = ?";
        return jdbcTemplate.update(sql, 
                user.getUsername(), 
                user.getPassword(),
                user.getEmail(), 
                user.getRole(), 
                user.getAvatarUrl(),
                user.getFullName(),
                user.getPhone(),
                user.getIsActive(),
                user.getId()) > 0;
    }
    
    @Override
    @Transactional
    public boolean updateLastLogin(int id) {
        String sql = "UPDATE users SET last_login = ? WHERE id = ?";
        return jdbcTemplate.update(sql, new Date(), id) > 0;
    }

    @Override
    @Transactional
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}