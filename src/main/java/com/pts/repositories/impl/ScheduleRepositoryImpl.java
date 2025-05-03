package com.pts.repositories.impl;

import com.pts.pojo.Schedule;
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

    private final RowMapper<Schedule> scheduleRowMapper = new RowMapper<Schedule>() {
        @Override
        public Schedule mapRow(ResultSet rs, int rowNum) throws SQLException {
            Schedule schedule = new Schedule();
            schedule.setId(rs.getLong("id"));
            // Set other fields from ResultSet
            schedule.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
            schedule.setArrivalTime(rs.getTimestamp("arrival_time").toLocalDateTime());
            schedule.setStatus(rs.getString("status"));
            return schedule;
        }
    };

    @Override
    public List<Schedule> findAll() {
        String sql = "SELECT * FROM schedules";
        return jdbcTemplate.query(sql, scheduleRowMapper);
    }

    @Override
    public Optional<Schedule> findById(Long id) {
        String sql = "SELECT * FROM schedules WHERE id = ?";
        List<Schedule> schedules = jdbcTemplate.query(sql, scheduleRowMapper, id);
        return schedules.isEmpty() ? Optional.empty() : Optional.of(schedules.get(0));
    }

    @Override
    public Schedule save(Schedule schedule) {
        if (schedule.getId() == null) {
            String sql = "INSERT INTO schedules (vehicle_id, route_id, departure_time, arrival_time, status) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    schedule.getVehicle().getId(),
                    schedule.getRoute().getId(),
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime(),
                    schedule.getStatus());
            return schedule;
        } else {
            String sql = "UPDATE schedules SET vehicle_id = ?, route_id = ?, departure_time = ?, arrival_time = ?, status = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    schedule.getVehicle().getId(),
                    schedule.getRoute().getId(),
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
    public List<Schedule> findByVehicleId(Long vehicleId) {
        String sql = "SELECT * FROM schedules WHERE vehicle_id = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, vehicleId);
    }

    @Override
    public List<Schedule> findByRouteId(Long routeId) {
        String sql = "SELECT * FROM schedules WHERE route_id = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, routeId);
    }

    @Override
    public List<Schedule> findByDepartureTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM schedules WHERE departure_time BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, startTime, endTime);
    }

    @Override
    public List<Schedule> findByStatus(String status) {
        String sql = "SELECT * FROM schedules WHERE status = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, status);
    }

    @Override
    public List<Schedule> findByVehicleIdAndStatus(Long vehicleId, String status) {
        String sql = "SELECT * FROM schedules WHERE vehicle_id = ? AND status = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, vehicleId, status);
    }

    @Override
    public List<Schedule> findByRouteIdAndStatus(Long routeId, String status) {
        String sql = "SELECT * FROM schedules WHERE route_id = ? AND status = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, routeId, status);
    }

    @Override
    public List<Schedule> findByTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime, String status) {
        String sql = "SELECT * FROM schedules WHERE departure_time >= ? AND arrival_time <= ? AND status = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, startTime, endTime, status);
    }

    @Override
    public List<Schedule> findByVehicleAndTimeRange(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM schedules WHERE vehicle_id = ? AND departure_time >= ? AND arrival_time <= ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, vehicleId, startTime, endTime);
    }
}