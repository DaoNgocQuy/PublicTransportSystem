package com.pts.repositories.impl;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
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
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
    
    @Override
    public boolean existsByPhone(String phone) {
        String sql = "SELECT COUNT(*) FROM users WHERE phone = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, phone);
        return count != null && count > 0;
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
    public boolean addUser(Users user) {
        String sql = "INSERT INTO users(username, password, email, role, avatar_url, full_name, phone, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, 
                user.getUsername(), 
                user.getPassword(),
                user.getEmail(), 
                user.getRole(), 
                user.getAvatarUrl(),
                user.getFullName(),
                user.getPhone(),
                user.getIsActive() != null ? user.getIsActive() : true,
                user.getCreatedAt() != null ? user.getCreatedAt() : new Date()) > 0;
    }

    @Override
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
    public boolean updateLastLogin(int id) {
        String sql = "UPDATE users SET last_login = ? WHERE id = ?";
        return jdbcTemplate.update(sql, new Date(), id) > 0;
    }

    @Override
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}