package com.pts.repositories.impl;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Route;
import com.pts.repositories.ScheduleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

@Repository
public class ScheduleRepositoryImpl implements ScheduleRepository {

    private final JdbcTemplate jdbcTemplate;

    public ScheduleRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Schedules> scheduleRowMapper = new RowMapper<Schedules>() {
        @Override
        public Schedules mapRow(ResultSet rs, int rowNum) throws SQLException {
            Schedules schedule = new Schedules();
            schedule.setId(rs.getInt("id"));
            
            Vehicles vehicle = new Vehicles();
            vehicle.setId(rs.getInt("vehicle_id"));
            schedule.setVehicleId(vehicle);
            
            Route route = new Route();
            route.setId(rs.getInt("route_id"));
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
            Route route = new Route();
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
            Route route = new Route();
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


    @Override
    public Schedules save(Schedules schedule) {
        if (schedule.getId() == null) {
            String sql = "INSERT INTO schedules (vehicle_id, route_id, departure_time, arrival_time) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    schedule.getVehicleId().getId(),
                    schedule.getRouteId().getId(),
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime());
            return schedule;
        } else {
            String sql = "UPDATE schedules SET vehicle_id = ?, route_id = ?, departure_time = ?, arrival_time = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    schedule.getVehicleId().getId(),
                    schedule.getRouteId().getId(),
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime(),
                    schedule.getId());
            return schedule;
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM schedules WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM schedules WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Schedules> findByVehicleId(Vehicles vehicleId) {
        String sql = "SELECT * FROM schedules WHERE vehicle_id = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, vehicleId.getId());
    }

    @Override
    public List<Schedules> findByRouteId(Route routeId) {
        String sql = "SELECT * FROM schedules WHERE route_id = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, routeId.getId());
    }

    @Override
    public List<Schedules> findByDepartureTimeBetween(Time startTime, Time endTime) {
        String sql = "SELECT * FROM schedules WHERE departure_time BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, startTime, endTime);
    }
}