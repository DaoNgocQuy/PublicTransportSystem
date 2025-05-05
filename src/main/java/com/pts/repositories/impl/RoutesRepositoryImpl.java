package com.pts.repositories.impl;

import com.pts.pojo.RouteTypes;
import com.pts.pojo.Routes;
import com.pts.repositories.RoutesRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
            route.setTotalStops(rs.getInt("total_stops"));

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
            route.setRouteColor(rs.getString("route_color"));
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
                    + "operation_start_time, operation_end_time, frequency_minutes, route_color, is_walking_route, is_active) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    route.getName(),
                    route.getRouteTypeIdValue(),
                    route.getStartLocation(),
                    route.getEndLocation(),
                    route.getTotalStops(),
                    route.getOperationStartTime(),
                    route.getOperationEndTime(),
                    route.getFrequencyMinutes(),
                    route.getRouteColor(),
                    route.getIsWalkingRoute(),
                    route.getActive());
        } else {
            String sql = "UPDATE routes SET name = ?, route_type_id = ?, start_location = ?, end_location = ?, "
                    + "total_stops = ?, operation_start_time = ?, operation_end_time = ?, frequency_minutes = ?, "
                    + "route_color = ?, is_walking_route = ?, is_active = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    route.getName(),
                    route.getRouteTypeIdValue(),
                    route.getStartLocation(),
                    route.getEndLocation(),
                    route.getTotalStops(),
                    route.getOperationStartTime(),
                    route.getOperationEndTime(),
                    route.getFrequencyMinutes(),
                    route.getRouteColor(),
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
}
