package com.pts.repositories.impl;

import com.pts.pojo.RouteTypes;
import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import com.pts.repositories.RoutesRepository;
import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Repository
public class RoutesRepositoryImpl implements RoutesRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoutesRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Routes> routesRowMapper = new RowMapper<Routes>() {
        @Override
        public Routes mapRow(ResultSet rs, int rowNum) throws SQLException {
            Routes route = new Routes();
            route.setId(rs.getInt("id"));
            route.setName(rs.getString("name"));
            route.setStartLocation(rs.getString("start_location"));
            route.setEndLocation(rs.getString("end_location"));

            // Kiểm tra null cho các trường có thể null
            if (rs.getObject("is_walking_route") != null) {
                route.setIsWalkingRoute(rs.getBoolean("is_walking_route"));
            }
            if (rs.getObject("is_active") != null) {
                route.setActive(rs.getBoolean("is_active"));
            }

            // Kiểm tra và lấy route_type_id
            Object routeTypeId = rs.getObject("route_type_id");
            if (routeTypeId != null) {
                route.setRouteTypeIdValue((Integer) routeTypeId);
            }

            // Xử lý các trường thời gian
            route.setOperationStartTime(rs.getTime("operation_start_time"));
            route.setOperationEndTime(rs.getTime("operation_end_time"));
            route.setFrequencyMinutes(rs.getInt("frequency_minutes"));
            route.setCreatedAt(rs.getTimestamp("created_at"));
            route.setLastUpdated(rs.getTimestamp("last_updated"));

            return route;
        }
    };

    private final RowMapper<Routes> routesWithTypeRowMapper = new RowMapper<Routes>() {
        @Override
        public Routes mapRow(ResultSet rs, int rowNum) throws SQLException {
            Routes route = routesRowMapper.mapRow(rs, rowNum);

            // Kiểm tra và lấy thông tin route_type
            try {
                // Sử dụng alias để tránh xung đột tên cột
                Object rtId = rs.getObject("rt_id");
                if (rtId != null) {
                    RouteTypes routeType = new RouteTypes();
                    routeType.setId(rs.getInt("rt_id"));

                    // Kiểm tra null cho các trường RouteTypes
                    String typeName = rs.getString("type_name");
                    if (typeName != null) {
                        routeType.setTypeName(typeName);
                    }

                    String colorCode = rs.getString("color_code");
                    if (colorCode != null) {
                        routeType.setColorCode(colorCode);
                    }

                    String description = rs.getString("description");
                    if (description != null) {
                        routeType.setDescription(description);
                    }

                    // Thiết lập RouteType cho route
                    route.setRouteType(routeType);
                }
            } catch (SQLException e) {
                // Ghi log nếu có lỗi
                System.err.println("Không thể lấy thông tin RouteType: " + e.getMessage());
            }

            return route;
        }
    };

    @Override
    public List<Routes> findAll() {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper);
    }

    @Override
    public Optional<Routes> findById(Integer id) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE r.id = ?";
        List<Routes> routes = jdbcTemplate.query(sql, routesWithTypeRowMapper, id);
        return routes.isEmpty() ? Optional.empty() : Optional.of(routes.get(0));
    }

    @Override
    public Routes save(Routes route) {
        if (route.getId() == null) {
            String sql = "INSERT INTO routes (name, route_type_id, start_location, end_location, total_stops, "
                    + "operation_start_time, operation_end_time, frequency_minutes, is_walking_route, is_active) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, route.getName());
                ps.setObject(2, route.getRouteTypeIdValue());
                ps.setString(3, route.getStartLocation());
                ps.setString(4, route.getEndLocation());
                ps.setObject(5, route.getTotalStops());
                ps.setObject(6, route.getOperationStartTime());
                ps.setObject(7, route.getOperationEndTime());
                ps.setObject(8, route.getFrequencyMinutes());
                ps.setObject(9, route.getIsWalkingRoute());
                ps.setObject(10, route.getActive());
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                route.setId(keyHolder.getKey().intValue());
            }
        } else {
            String sql = "UPDATE routes SET name = ?, route_type_id = ?, start_location = ?, end_location = ?, "
                    + "total_stops = ?, operation_start_time = ?, operation_end_time = ?, frequency_minutes = ?, "
                    + "is_walking_route = ?, is_active = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    route.getName(),
                    route.getRouteTypeIdValue(),
                    route.getStartLocation(),
                    route.getEndLocation(),
                    route.getTotalStops(), // Thêm dòng này
                    route.getOperationStartTime(),
                    route.getOperationEndTime(),
                    route.getFrequencyMinutes(),
                    route.getIsWalkingRoute(),
                    route.getActive(),
                    route.getId());
        }
        return route;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM routes WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM routes WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Routes> findByName(String name) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE r.name = ?";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper, name);
    }

    @Override
    public List<Routes> findByStartLocation(String startLocation) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE r.start_location = ?";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper, startLocation);
    }

    @Override
    public List<Routes> findByEndLocation(String endLocation) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE r.end_location = ?";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper, endLocation);
    }

    @Override
    public List<Routes> findByTotalStops(Integer totalStops) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE r.total_stops = ?";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper, totalStops);
    }

    @Override
    public List<Routes> findByIsWalkingRoute(Boolean isWalkingRoute) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE r.is_walking_route = ?";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper, isWalkingRoute);
    }

    @Override
    public List<Routes> findByIsActive(Boolean isActive) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE r.is_active = ?";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper, isActive);
    }

    @Override
    public List<Routes> findByRouteTypeId(Integer routeTypeId) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE r.route_type_id = ?";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper, routeTypeId);
    }

    @Override
    public List<Routes> searchRoutesByName(String keyword) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE r.name LIKE ? OR r.start_location LIKE ? OR r.end_location LIKE ?";
        String searchParam = "%" + keyword + "%";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper, searchParam, searchParam, searchParam);
    }

    @Override
    public List<Routes> findByStopId(Integer stopId) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "JOIN route_stops rs ON r.id = rs.route_id "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE rs.stop_id = ?";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper, stopId);
    }

    @Override
    public List<Routes> findByStopIdAndDirection(Integer stopId, Integer direction) {
        String sql = "SELECT r.*, rt.id as rt_id, rt.type_name, rt.color_code, rt.description "
                + "FROM routes r "
                + "JOIN route_stops rs ON r.id = rs.route_id "
                + "LEFT JOIN route_types rt ON r.route_type_id = rt.id "
                + "WHERE rs.stop_id = ? AND rs.direction = ?";
        return jdbcTemplate.query(sql, routesWithTypeRowMapper, stopId, direction);
    }

    @Override
    public List<Stops> findStopsByRouteId(Integer routeId) {
        String sql = "SELECT s.*, rs.stop_order, rs.direction FROM stops s "
                + "JOIN route_stops rs ON s.id = rs.stop_id "
                + "WHERE rs.route_id = ? ORDER BY rs.stop_order";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Stops stop = new Stops();
            stop.setId(rs.getInt("id"));
            stop.setStopName(rs.getString("stop_name"));
            stop.setLatitude(rs.getFloat("latitude"));
            stop.setLongitude(rs.getFloat("longitude"));
            stop.setAddress(rs.getString("address"));

            // Đọc thông tin stop_order và direction từ bảng route_stops
            stop.setStopOrder(rs.getInt("stop_order"));

            // Đọc direction nếu có
            int direction = rs.getInt("direction");
            if (!rs.wasNull()) {
                stop.setDirection(direction);
            }

            if (rs.getObject("is_accessible") != null) {
                stop.setIsAccessible(rs.getBoolean("is_accessible"));
            }

            return stop;
        }, routeId);
    }

    @Override
    public List<Stops> findStopsByRouteIdAndDirection(Integer routeId, Integer direction) {
        String sql = "SELECT s.*, rs.stop_order, rs.direction FROM stops s "
                + "JOIN route_stops rs ON s.id = rs.stop_id "
                + "WHERE rs.route_id = ? AND rs.direction = ? ORDER BY rs.stop_order";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Stops stop = new Stops();
            stop.setId(rs.getInt("id"));
            stop.setStopName(rs.getString("stop_name"));
            stop.setLatitude(rs.getFloat("latitude"));
            stop.setLongitude(rs.getFloat("longitude"));
            stop.setAddress(rs.getString("address"));

            // Đọc thông tin stop_order và direction từ bảng route_stops
            stop.setStopOrder(rs.getInt("stop_order"));
            stop.setDirection(rs.getInt("direction"));

            if (rs.getObject("is_accessible") != null) {
                stop.setIsAccessible(rs.getBoolean("is_accessible"));
            }

            return stop;
        }, routeId, direction);
    }

    @Override
    public Integer countStopsByRouteId(Integer routeId) {
        String sql = "SELECT COUNT(*) FROM route_stops WHERE route_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, routeId);
    }

    @Override
    public Integer countStopsByRouteIdAndDirection(Integer routeId, Integer direction) {
        String sql = "SELECT COUNT(*) FROM route_stops WHERE route_id = ? AND direction = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, routeId, direction);
    }

    @Override
    public void updateTotalStops(Integer routeId) {
        String sql = "UPDATE routes r SET total_stops = (SELECT COUNT(*) FROM route_stops rs WHERE rs.route_id = r.id) WHERE r.id = ?";
        jdbcTemplate.update(sql, routeId);
    }

    @Override
    public void updateRouteOperationDetails(Integer routeId, Time startTime, Time endTime, Integer frequencyMinutes) {
        try {
            String sql = "UPDATE routes SET operation_start_time = ?, operation_end_time = ?, frequency_minutes = ? WHERE id = ?";
            jdbcTemplate.update(sql, startTime, endTime, frequencyMinutes, routeId);
            System.out.println("Đã cập nhật chi tiết hoạt động của tuyến ID " + routeId
                    + ": startTime=" + startTime
                    + ", endTime=" + endTime
                    + ", frequency=" + frequencyMinutes);
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật chi tiết hoạt động: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
