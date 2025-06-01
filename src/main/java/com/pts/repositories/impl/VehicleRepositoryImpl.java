package com.pts.repositories.impl;

import com.pts.pojo.Vehicles;
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
        String sql = "SELECT * FROM vehicles";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToVehicle(rs));
    }

    @Override
    public Vehicles getVehicleById(Integer id) {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToVehicle(rs), id);
    }

    @Override
    public Vehicles getVehicleByLicensePlate(String licensePlate) {
        String sql = "SELECT * FROM vehicles WHERE license_plate = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToVehicle(rs), licensePlate);
    }

    @Override
    public boolean addVehicle(Vehicles vehicle) {
        String sql = "INSERT INTO vehicles (vehicle_name, type, license_plate) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql,
                vehicle.getVehicleName(),
                vehicle.getType(),
                vehicle.getLicensePlate()) > 0;
    }

    @Override
    public boolean updateVehicle(Vehicles vehicle) {
        String sql = "UPDATE vehicles SET vehicle_name = ?, type = ?, license_plate = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                vehicle.getVehicleName(),
                vehicle.getType(),
                vehicle.getLicensePlate(),
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
        vehicle.setVehicleName(rs.getString("vehicle_name"));
        vehicle.setType(rs.getString("type"));
        vehicle.setLicensePlate(rs.getString("license_plate"));

        return vehicle;
    }
}