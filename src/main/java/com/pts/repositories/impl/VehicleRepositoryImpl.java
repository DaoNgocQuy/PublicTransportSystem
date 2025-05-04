/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.repositories.impl;

import com.pts.pojo.Vehicles;
import com.pts.pojo.Users;
import com.pts.repositories.VehicleRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VehicleRepositoryImpl implements VehicleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Vehicles> getAllVehicles() {
        String sql = "SELECT v.*, u.id as user_id, u.username, u.password, u.role " +
                    "FROM vehicles v LEFT JOIN users u ON v.user_id = u.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToVehicle(rs));
    }

    @Override
    public Vehicles getVehicleById(Integer id) {
        String sql = "SELECT v.*, u.id as user_id, u.username, u.password, u.role " +
                    "FROM vehicles v LEFT JOIN users u ON v.user_id = u.id " +
                    "WHERE v.id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToVehicle(rs), id);
    }

    @Override
    public Vehicles getVehicleByLicensePlate(String licensePlate) {
        String sql = "SELECT v.*, u.id as user_id, u.username, u.password, u.role " +
                    "FROM vehicles v LEFT JOIN users u ON v.user_id = u.id " +
                    "WHERE v.license_plate = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToVehicle(rs), licensePlate);
    }

    @Override
    public List<Vehicles> getVehiclesByUserId(Integer userId) {
        String sql = "SELECT v.*, u.id as user_id, u.username, u.password, u.role " +
                    "FROM vehicles v LEFT JOIN users u ON v.user_id = u.id " +
                    "WHERE v.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToVehicle(rs), userId);
    }

    @Override
    public boolean addVehicle(Vehicles vehicle) {
        String sql = "INSERT INTO vehicles (user_id, type, license_plate, capacity) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, 
            vehicle.getUserId() != null ? vehicle.getUserId().getId() : null, 
            vehicle.getType(), 
            vehicle.getLicensePlate(), 
            vehicle.getCapacity()) > 0;
    }

    @Override
    public boolean updateVehicle(Vehicles vehicle) {
        String sql = "UPDATE vehicles SET user_id = ?, type = ?, license_plate = ?, capacity = ? WHERE id = ?";
        return jdbcTemplate.update(sql, 
            vehicle.getUserId() != null ? vehicle.getUserId().getId() : null, 
            vehicle.getType(), 
            vehicle.getLicensePlate(), 
            vehicle.getCapacity(),
            vehicle.getId()) > 0;
    }

    @Override
    public boolean deleteVehicle(Integer id) {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    private Vehicles mapRowToVehicle(ResultSet rs) throws SQLException {
        Vehicles vehicle = new Vehicles();
        vehicle.setId(rs.getInt("id"));
        
        // Only create Users object if user_id is not null
        if (rs.getObject("user_id") != null) {
            Users user = new Users();
            user.setId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));
            vehicle.setUserId(user);
        }
        
        // Set other vehicle fields
        vehicle.setType(rs.getString("type"));
        vehicle.setLicensePlate(rs.getString("license_plate"));
        
        // Handle capacity which might be null
        Object capacityObj = rs.getObject("capacity");
        if (capacityObj != null) {
            vehicle.setCapacity((Integer) capacityObj);
        }
        
        return vehicle;
    }
}
