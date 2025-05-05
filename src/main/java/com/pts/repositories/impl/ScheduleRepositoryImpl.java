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
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class ScheduleRepositoryImpl implements ScheduleRepository {

    private final JdbcTemplate jdbcTemplate;

    // Chuỗi SQL dùng chung cho tất cả các truy vấn
    private static final String BASE_SELECT_SQL
            = "SELECT s.*, v.license_plate, v.type, r.name, r.start_location, r.end_location "
            + "FROM schedules s "
            + "LEFT JOIN vehicles v ON s.vehicle_id = v.id "
            + "LEFT JOIN routes r ON s.route_id = r.id";

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

            // Load route info
            Routes route = new Routes();
            route.setId(rs.getInt("route_id"));
            route.setName(rs.getString("name"));
            route.setStartLocation(rs.getString("start_location"));
            route.setEndLocation(rs.getString("end_location"));
            schedule.setRouteId(route);

            // Chuyển java.sql.Time sang java.util.Date
            java.sql.Time depTime = rs.getTime("departure_time");
            if (depTime != null) {
                schedule.setDepartureTime(new Date(depTime.getTime()));
            }

            java.sql.Time arrTime = rs.getTime("arrival_time");
            if (arrTime != null) {
                schedule.setArrivalTime(new Date(arrTime.getTime()));
            }

            // Thêm trường created_at
            java.sql.Timestamp createdTime = rs.getTimestamp("created_at");
            if (createdTime != null) {
                schedule.setCreatedAt(new Date(createdTime.getTime()));
            }

            return schedule;
        }
    };

    @Override
    public List<Schedules> findAll() {
        return jdbcTemplate.query(BASE_SELECT_SQL, fullScheduleRowMapper);
    }

    @Override
    public Optional<Schedules> findById(Integer id) {
        String sql = BASE_SELECT_SQL + " WHERE s.id = ?";
        List<Schedules> schedules = jdbcTemplate.query(sql, fullScheduleRowMapper, id);
        return schedules.isEmpty() ? Optional.empty() : Optional.of(schedules.get(0));
    }

    @Override
    public List<Schedules> findByVehicleId(Vehicles vehicleId) {
        String sql = BASE_SELECT_SQL + " WHERE s.vehicle_id = ?";
        return jdbcTemplate.query(sql, fullScheduleRowMapper, vehicleId.getId());
    }

    @Override
    public List<Schedules> findByRouteId(Routes routeId) {
        String sql = BASE_SELECT_SQL + " WHERE s.route_id = ?";
        return jdbcTemplate.query(sql, fullScheduleRowMapper, routeId.getId());
    }

    @Override
    public List<Schedules> findByRouteId(Integer routeId) {
        
        String sql = BASE_SELECT_SQL + " WHERE s.route_id = ?";
        return jdbcTemplate.query(sql, fullScheduleRowMapper, routeId);
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
            String sql = "INSERT INTO schedules (vehicle_id, route_id, departure_time, arrival_time) "
                    + "VALUES (?, ?, ?, ?)";

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, schedule.getVehicleId().getId());
                ps.setInt(2, schedule.getRouteId().getId());

                // Chuyển từ java.util.Date sang java.sql.Time
                if (schedule.getDepartureTime() != null) {
                    ps.setTime(3, new java.sql.Time(schedule.getDepartureTime().getTime()));
                } else {
                    ps.setNull(3, java.sql.Types.TIME);
                }

                if (schedule.getArrivalTime() != null) {
                    ps.setTime(4, new java.sql.Time(schedule.getArrivalTime().getTime()));
                } else {
                    ps.setNull(4, java.sql.Types.TIME);
                }

                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                schedule.setId(keyHolder.getKey().intValue());
            }
        } else {
            // Cập nhật
            String sql = "UPDATE schedules SET vehicle_id = ?, route_id = ?, departure_time = ?, arrival_time = ? "
                    + "WHERE id = ?";

            jdbcTemplate.update(sql,
                    schedule.getVehicleId().getId(),
                    schedule.getRouteId().getId(),
                    schedule.getDepartureTime() != null ? new java.sql.Time(schedule.getDepartureTime().getTime()) : null,
                    schedule.getArrivalTime() != null ? new java.sql.Time(schedule.getArrivalTime().getTime()) : null,
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
}
