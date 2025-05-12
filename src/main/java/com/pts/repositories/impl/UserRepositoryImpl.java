package com.pts.repositories.impl;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;

import jakarta.transaction.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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

    @Override
    @Transactional
    public boolean updateUser(Users user) {
        try {
            String sql = "UPDATE users SET ";
            List<Object> params = new ArrayList<>();
            
            // Danh sách các trường có thể cập nhật và giá trị tương ứng
            Map<String, Object> fields = new HashMap<>();
            
            if (user.getFullName() != null) {
                fields.put("full_name", user.getFullName());
            }
            
            if (user.getEmail() != null) {
                fields.put("email", user.getEmail());
            }
            
            if (user.getPhone() != null) {
                fields.put("phone", user.getPhone());
            }
            
            if (user.getPassword() != null) {
                fields.put("password", user.getPassword());
            }
            
            if (user.getAvatarUrl() != null) {
                fields.put("avatar_url", user.getAvatarUrl());
            }
            
            if (fields.isEmpty()) {
                return false; // Không có gì để cập nhật
            }
            
            // Xây dựng câu SQL động
            StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET ");
            boolean first = true;
            
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                if (!first) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append(entry.getKey()).append(" = ?");
                params.add(entry.getValue());
                first = false;
            }
            
            sqlBuilder.append(" WHERE id = ?");
            params.add(user.getId());
            
            int rowsAffected = jdbcTemplate.update(sqlBuilder.toString(), params.toArray());
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Users getUserById(Integer userId) {
        try {
            String sql = "SELECT * FROM users WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, userRowMapper, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // RowMapper để ánh xạ kết quả truy vấn thành đối tượng Users
    private final RowMapper<Users> userRowMapper = (resultSet, rowNum) -> {
        Users user = new Users();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setRole(resultSet.getString("role"));
        user.setAvatarUrl(resultSet.getString("avatar_url"));
        
        // Xử lý các trường ngày tháng
        try {
            java.sql.Timestamp createdAtTs = resultSet.getTimestamp("created_at");
            if (createdAtTs != null) {
                user.setCreatedAt(new Date(createdAtTs.getTime()));
            }
            
            java.sql.Timestamp lastLoginTs = resultSet.getTimestamp("last_login");
            if (lastLoginTs != null) {
                user.setLastLogin(new Date(lastLoginTs.getTime()));
            }
        } catch (SQLException e) {
            // Bỏ qua nếu không có các trường này
        }
        
        return user;
    };

    @Override
    @Transactional
    public boolean saveResetPasswordToken(Integer userId, String token, Date expiryTime) {
        try {
            // Xóa token cũ nếu có
            String deleteSql = "DELETE FROM reset_password_token WHERE user_id = ?";
            jdbcTemplate.update(deleteSql, userId);
            
            // Thêm token mới
            String sql = "INSERT INTO reset_password_token (user_id, token, expiry_date) VALUES (?, ?, ?)";
            int result = jdbcTemplate.update(sql, userId, token, new java.sql.Timestamp(expiryTime.getTime()));
            
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<Integer> getUserIdByResetToken(String token) {
        try {
            String sql = "SELECT user_id FROM reset_password_token WHERE token = ?";
            Integer userId = jdbcTemplate.queryForObject(sql, Integer.class, token);
            return Optional.ofNullable(userId);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public boolean isResetTokenExpired(String token) {
        try {
            String sql = "SELECT expiry_date < NOW() as expired FROM reset_password_token WHERE token = ?";
            Boolean expired = jdbcTemplate.queryForObject(sql, Boolean.class, token);
            return expired != null && expired;
        } catch (EmptyResultDataAccessException e) {
            return true; // Nếu không tìm thấy token coi như đã hết hạn
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    @Transactional
    public boolean deleteResetToken(String token) {
        try {
            String sql = "DELETE FROM reset_password_token WHERE token = ?";
            int result = jdbcTemplate.update(sql, token);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}