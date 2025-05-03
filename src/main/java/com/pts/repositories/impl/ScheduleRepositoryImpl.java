package com.pts.repositories.impl;

import com.pts.pojo.Schedules;
import com.pts.repositories.ScheduleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
            schedule.setId(rs.getLong("id"));
            schedule.setVehicleId(rs.getLong("vehicle_id"));
            schedule.setRouteId(rs.getLong("route_id"));
            schedule.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
            schedule.setArrivalTime(rs.getTimestamp("arrival_time").toLocalDateTime());
            schedule.setStatus(rs.getString("status"));
            return schedule;
        }
    };

    @Override
    public List<Schedules> findAll() {
        String sql = "SELECT * FROM schedules";
        return jdbcTemplate.query(sql, scheduleRowMapper);
    }

    @Override
    public Optional<Schedules> findById(Long id) {
        String sql = "SELECT * FROM schedules WHERE id = ?";
        List<Schedules> schedules = jdbcTemplate.query(sql, scheduleRowMapper, id);
        return schedules.isEmpty() ? Optional.empty() : Optional.of(schedules.get(0));
    }

    @Override
    public Schedules save(Schedules schedule) {
        if (schedule.getId() == null) {
            String sql = "INSERT INTO schedules (vehicle_id, route_id, departure_time, arrival_time, status) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    schedule.getVehicleId(),
                    schedule.getRouteId(),
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime(),
                    schedule.getStatus());
            return schedule;
        } else {
            String sql = "UPDATE schedules SET vehicle_id = ?, route_id = ?, departure_time = ?, arrival_time = ?, status = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    schedule.getVehicleId(),
                    schedule.getRouteId(),
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime(),
                    schedule.getStatus(),
                    schedule.getId());
            return schedule;
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM schedules WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM schedules WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Schedules> findByVehicleId(Long vehicleId) {
        String sql = "SELECT * FROM schedules WHERE vehicle_id = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, vehicleId);
    }

    @Override
    public List<Schedules> findByRouteId(Long routeId) {
        String sql = "SELECT * FROM schedules WHERE route_id = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, routeId);
    }

    @Override
    public List<Schedules> findByDepartureTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM schedules WHERE departure_time BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, startTime, endTime);
    }

    @Override
    public List<Schedules> findByStatus(String status) {
        String sql = "SELECT * FROM schedules WHERE status = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, status);
    }

    @Override
    public List<Schedules> findByVehicleIdAndStatus(Long vehicleId, String status) {
        String sql = "SELECT * FROM schedules WHERE vehicle_id = ? AND status = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, vehicleId, status);
    }

    @Override
    public List<Schedules> findByRouteIdAndStatus(Long routeId, String status) {
        String sql = "SELECT * FROM schedules WHERE route_id = ? AND status = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, routeId, status);
    }

    @Override
    public List<Schedules> findByTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime, String status) {
        String sql = "SELECT * FROM schedules WHERE departure_time BETWEEN ? AND ? AND status = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, startTime, endTime, status);
    }

    @Override
    public List<Schedules> findByVehicleAndTimeRange(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM schedules WHERE vehicle_id = ? AND departure_time BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, vehicleId, startTime, endTime);
    }
}