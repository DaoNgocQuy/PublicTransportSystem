package com.pts.repositories.impl;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<Users> userMapper = new RowMapper<>() {
        @Override
        public Users mapRow(ResultSet rs, int rowNum) throws SQLException {
            Users u = new Users();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setPassword(rs.getString("password"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));
            u.setAvatarUrl(rs.getString("avatar_url"));
            return u;
        }
    };

    @Override
    public Users getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, userMapper, id);
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
        String sql = "INSERT INTO users(username, password, email, role, avatar_url) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, user.getUsername(), user.getPassword(),
                user.getEmail(), user.getRole(), user.getAvatarUrl()) > 0;
    }

    @Override
    public boolean updateUser(Users user) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, role = ?, avatar_url = ? WHERE id = ?";
        return jdbcTemplate.update(sql, user.getUsername(), user.getPassword(),
                user.getEmail(), user.getRole(), user.getAvatarUrl(), user.getId()) > 0;
    }

    @Override
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
