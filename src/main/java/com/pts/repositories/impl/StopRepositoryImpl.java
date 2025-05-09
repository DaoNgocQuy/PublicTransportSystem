package com.pts.repositories.impl;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import com.pts.repositories.StopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class StopRepositoryImpl implements StopRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public StopRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Stops> stopRowMapper = new RowMapper<Stops>() {
        @Override
        public Stops mapRow(ResultSet rs, int rowNum) throws SQLException {
            Stops stop = new Stops();
            stop.setId(rs.getInt("id"));
            stop.setStopName(rs.getString("stop_name"));
            stop.setLatitude(rs.getFloat("latitude"));
            stop.setLongitude(rs.getFloat("longitude"));
            stop.setStopOrder(rs.getInt("stop_order"));
            stop.setAddress(rs.getString("address"));

            // Kiểm tra null cho các trường Boolean
            Object isAccessibleObj = rs.getObject("is_accessible");
            if (isAccessibleObj != null) {
                stop.setIsAccessible(rs.getBoolean("is_accessible"));
            }

            // Kiểm tra route_id có thể null
            Object routeIdObj = rs.getObject("route_id");
            if (routeIdObj != null) {
                Routes route = new Routes();
                route.setId(rs.getInt("route_id"));
                stop.setRouteId(route);
            }

            return stop;
        }
    };

    // RowMapper với thông tin đầy đủ về tuyến đường
    private final RowMapper<Stops> stopWithRouteRowMapper = new RowMapper<Stops>() {
        @Override
        public Stops mapRow(ResultSet rs, int rowNum) throws SQLException {
            Stops stop = stopRowMapper.mapRow(rs, rowNum);

            // Nếu có thông tin về route
            Object routeIdObj = rs.getObject("route_id");
            if (routeIdObj != null) {
                Routes route = new Routes();
                route.setId(rs.getInt("route_id"));

                try {
                    route.setName(rs.getString("route_name"));
                    route.setStartLocation(rs.getString("start_location"));
                    route.setEndLocation(rs.getString("end_location"));
                } catch (SQLException e) {
                    // Bỏ qua nếu không có cột
                }

                stop.setRouteId(route);
            }

            return stop;
        }
    };

    @Override
    public List<Stops> findAll() {
        String sql = "SELECT s.*, r.name as route_name, r.start_location, r.end_location "
                + "FROM stops s "
                + "LEFT JOIN routes r ON s.route_id = r.id "
                + "ORDER BY s.stop_name";
        return jdbcTemplate.query(sql, stopWithRouteRowMapper);
    }

    @Override
    public Optional<Stops> findById(Integer id) {
        String sql = "SELECT s.*, r.name as route_name, r.start_location, r.end_location "
                + "FROM stops s "
                + "LEFT JOIN routes r ON s.route_id = r.id "
                + "WHERE s.id = ?";
        List<Stops> stops = jdbcTemplate.query(sql, stopWithRouteRowMapper, id);
        return stops.isEmpty() ? Optional.empty() : Optional.of(stops.get(0));
    }

    @Override
    public Stops save(Stops stop) {
        if (stop.getId() == null) {
            String sql = "INSERT INTO stops (stop_name, latitude, longitude, stop_order, address, has_shelter, is_accessible, route_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    stop.getStopName(),
                    stop.getLatitude(),
                    stop.getLongitude(),
                    stop.getStopOrder(),
                    stop.getAddress(),
                    stop.getIsAccessible(),
                    stop.getRouteId() != null ? stop.getRouteId().getId() : null);
        } else {
            String sql = "UPDATE stops SET stop_name = ?, latitude = ?, longitude = ?, stop_order = ?, address = ?, "
                    + "has_shelter = ?, is_accessible = ?, route_id = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    stop.getStopName(),
                    stop.getLatitude(),
                    stop.getLongitude(),
                    stop.getStopOrder(),
                    stop.getAddress(),
                    stop.getIsAccessible(),
                    stop.getRouteId() != null ? stop.getRouteId().getId() : null,
                    stop.getId());
        }
        return stop;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM stops WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM stops WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Stops> findByStopName(String stopName) {
        String sql = "SELECT s.*, r.name as route_name, r.start_location, r.end_location "
                + "FROM stops s "
                + "LEFT JOIN routes r ON s.route_id = r.id "
                + "WHERE s.stop_name = ?";
        return jdbcTemplate.query(sql, stopWithRouteRowMapper, stopName);
    }

    @Override
    public List<Stops> findByAddress(String address) {
        String sql = "SELECT s.*, r.name as route_name, r.start_location, r.end_location "
                + "FROM stops s "
                + "LEFT JOIN routes r ON s.route_id = r.id "
                + "WHERE s.address LIKE ?";
        return jdbcTemplate.query(sql, stopWithRouteRowMapper, "%" + address + "%");
    }

    @Override
    public List<Stops> findByRouteId(Integer routeId) {
        String sql = "SELECT s.*, r.name as route_name, r.start_location, r.end_location "
                + "FROM stops s "
                + "LEFT JOIN routes r ON s.route_id = r.id "
                + "WHERE s.route_id = ? "
                + "ORDER BY s.stop_order";
        return jdbcTemplate.query(sql, stopWithRouteRowMapper, routeId);
    }

    @Override
    public List<Stops> searchStops(String keyword) {
        String sql = "SELECT s.*, r.name as route_name, r.start_location, r.end_location "
                + "FROM stops s "
                + "LEFT JOIN routes r ON s.route_id = r.id "
                + "WHERE s.stop_name LIKE ? OR s.address LIKE ?";
        String searchParam = "%" + keyword + "%";
        return jdbcTemplate.query(sql, stopWithRouteRowMapper, searchParam, searchParam);
    }
    // Thêm vào StopRepositoryImpl.java

    @Override
    public List<Stops> findNearbyStops(double latitude, double longitude, double radius) {
        // Công thức Haversine để tính khoảng cách giữa 2 điểm trên bề mặt trái đất
        String sql = "SELECT s.*, r.name as route_name, r.start_location, r.end_location, "
                + "6371000 * acos(cos(radians(?)) * cos(radians(s.latitude)) * cos(radians(s.longitude) - radians(?)) "
                + "+ sin(radians(?)) * sin(radians(s.latitude))) AS distance "
                + "FROM stops s "
                + "LEFT JOIN routes r ON s.route_id = r.id "
                + "HAVING distance < ? "
                + "ORDER BY distance";

        return jdbcTemplate.query(sql, stopWithRouteRowMapper, latitude, longitude, latitude, radius);
    }
}
