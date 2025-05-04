package com.pts.repositories.impl;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import com.pts.repositories.ScheduleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

@Repository
public class ScheduleRepositoryImpl implements ScheduleRepository {

    private final JdbcTemplate jdbcTemplate;

    // Chuỗi SQL dùng chung cho tất cả các truy vấn
    private static final String BASE_SELECT_SQL = 
        "SELECT s.*, v.license_plate, v.type, r.name, r.start_location, r.end_location " +
        "FROM schedules s " +
        "LEFT JOIN vehicles v ON s.vehicle_id = v.id " + 
        "LEFT JOIN routes r ON s.route_id = r.id";

    public ScheduleRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper dùng chung cho tất cả các truy vấn
    private final RowMapper<Schedules> fullScheduleRowMapper = new RowMapper<Schedules>() {
        @Override
        public Schedules mapRow(ResultSet rs, int rowNum) throws SQLException {
            Schedules schedule = new Schedules();
            schedule.setId(rs.getInt("id"));
            
            // Load vehicle info
            Vehicles vehicle = new Vehicles();
            vehicle.setId(rs.getInt("vehicle_id"));
            vehicle.setLicensePlate(rs.getString("license_plate"));
            vehicle.setType(rs.getString("type"));
            schedule.setVehicleId(vehicle);
            Routes route = new Routes();
            route.setId(rs.getInt("route_id"));
            route.setName(rs.getString("name"));
            route.setStartLocation(rs.getString("start_location"));
            route.setEndLocation(rs.getString("end_location"));
            schedule.setRouteId(route);
            
            schedule.setDepartureTime(rs.getTime("departure_time"));
            schedule.setArrivalTime(rs.getTime("arrival_time"));
            
            return schedule;
        }
    };

    @Override
    public List<Schedules> findAll() {
        String sql = "SELECT s.*, v.license_plate, v.type, r.name, r.start_location, r.end_location " +
                    "FROM schedules s " +
                    "LEFT JOIN vehicles v ON s.vehicle_id = v.id " + 
                    "LEFT JOIN routes r ON s.route_id = r.id";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Schedules schedule = new Schedules();
            schedule.setId(rs.getInt("id"));
            
            // Load vehicle info
            Vehicles vehicle = new Vehicles();
            vehicle.setId(rs.getInt("vehicle_id"));
            vehicle.setLicensePlate(rs.getString("license_plate"));
            vehicle.setType(rs.getString("type"));
            schedule.setVehicleId(vehicle);
            
            // Load route info
            Routes route = new Routes();
            route.setId(rs.getInt("route_id"));
            route.setName(rs.getString("name"));
            route.setStartLocation(rs.getString("start_location"));
            route.setEndLocation(rs.getString("end_location"));
            schedule.setRouteId(route);
            
            schedule.setDepartureTime(rs.getTime("departure_time"));
            schedule.setArrivalTime(rs.getTime("arrival_time"));
            
            return schedule;
        });
    }

    @Override
    public Optional<Schedules> findById(Integer id) {
        String sql = "SELECT s.*, v.license_plate, v.type, r.name, r.start_location, r.end_location " +
                    "FROM schedules s " +
                    "LEFT JOIN vehicles v ON s.vehicle_id = v.id " + 
                    "LEFT JOIN routes r ON s.route_id = r.id " +
                    "WHERE s.id = ?";
        
        List<Schedules> schedules = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Schedules schedule = new Schedules();
            schedule.setId(rs.getInt("id"));
            
            // Load vehicle info
            Vehicles vehicle = new Vehicles();
            vehicle.setId(rs.getInt("vehicle_id"));
            vehicle.setLicensePlate(rs.getString("license_plate"));
            vehicle.setType(rs.getString("type"));
            schedule.setVehicleId(vehicle);
            
            // Load route info
            Routes route = new Routes();
            route.setId(rs.getInt("route_id"));
            route.setName(rs.getString("name"));
            route.setStartLocation(rs.getString("start_location"));
            route.setEndLocation(rs.getString("end_location"));
            schedule.setRouteId(route);
            
            schedule.setDepartureTime(rs.getTime("departure_time"));
            schedule.setArrivalTime(rs.getTime("arrival_time"));
            
            return schedule;
        }, id);
        return schedules.isEmpty() ? Optional.empty() : Optional.of(schedules.get(0));
    }

    public List<Schedules> findByRoute(Routes route) {
        String sql = BASE_SELECT_SQL + " WHERE s.route_id = ?";
        return jdbcTemplate.query(sql, fullScheduleRowMapper, route.getId());
    }

    public List<Schedules> findByVehicle(Vehicles vehicle) {
        String sql = BASE_SELECT_SQL + " WHERE s.vehicle_id = ?";
        return jdbcTemplate.query(sql, fullScheduleRowMapper, vehicle.getId());
    }

    @Override
    public List<Schedules> findByDepartureTimeBetween(Time startTime, Time endTime) {
        String sql = BASE_SELECT_SQL + " WHERE s.departure_time BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, fullScheduleRowMapper, startTime, endTime);
    }

    @Override
    public Schedules save(Schedules schedule) {
        if (schedule.getId() == null || schedule.getId() == 0) {
            // Thêm mới
            KeyHolder keyHolder = new GeneratedKeyHolder();
            String sql = "INSERT INTO schedules (vehicle_id, route_id, departure_time, arrival_time) VALUES (?, ?, ?, ?)";
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, schedule.getVehicleId().getId());
                ps.setInt(2, schedule.getRouteId().getId());
                ps.setTime(3, (Time) schedule.getDepartureTime());
                ps.setTime(4, (Time) schedule.getArrivalTime());
                return ps;
            }, keyHolder);
            
            if (keyHolder.getKey() != null) {
                schedule.setId(keyHolder.getKey().intValue());
            }
        } else {
            // Cập nhật
            String sql = "UPDATE schedules SET vehicle_id = ?, route_id = ?, departure_time = ?, arrival_time = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    schedule.getVehicleId().getId(),
                    schedule.getRouteId().getId(),
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime(),
                    schedule.getId());
        }
        return schedule;
    }

    @Override
    public void deleteById(Integer id) {
        jdbcTemplate.update("DELETE FROM schedules WHERE id = ?", id);
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM schedules WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Schedules> findByVehicleId(Vehicles vehicleId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<Schedules> findByRouteId(Routes routeId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}