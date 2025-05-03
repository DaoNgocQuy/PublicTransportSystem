package com.pts.repositories.impl;

import com.pts.pojo.Routes;
import com.pts.repositories.RouteRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RouteRepositoryImpl implements RouteRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<Routes> routeMapper = new RowMapper<>() {
        @Override
        public Routes mapRow(ResultSet rs, int rowNum) throws SQLException {
            Routes route = new Routes();
            route.setId(rs.getInt("id"));
            route.setName(rs.getString("name"));
            route.setStartLocation(rs.getString("start_location"));
            route.setEndLocation(rs.getString("end_location"));
            route.setTotalStops(rs.getInt("total_stops"));
            route.setIsWalkingRoute(rs.getBoolean("is_walking_route"));
            return route;
        }
    };

    @Override
    public List<Routes> getAllRoutes() {
        String sql = "SELECT * FROM routes";
        return jdbcTemplate.query(sql, routeMapper);
    }

    @Override
    public Routes getRouteById(Integer id) {
        String sql = "SELECT * FROM routes WHERE id = ?";
        List<Routes> results = jdbcTemplate.query(sql, routeMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Routes> searchRoutes(String startLocation, String endLocation) {
        String sql = "SELECT * FROM routes WHERE start_location LIKE ? AND end_location LIKE ?";
        return jdbcTemplate.query(sql, routeMapper, 
            "%" + startLocation + "%", 
            "%" + endLocation + "%");
    }

    @Override
    public List<Routes> getRoutesByType(boolean isWalkingRoute) {
        String sql = "SELECT * FROM routes WHERE is_walking_route = ?";
        return jdbcTemplate.query(sql, routeMapper, isWalkingRoute);
    }

    @Override
    public boolean addRoute(Routes route) {
        String sql = "INSERT INTO routes(name, start_location, end_location, total_stops, is_walking_route) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, 
            route.getName(),
            route.getStartLocation(),
            route.getEndLocation(),
            route.getTotalStops(),
            route.getIsWalkingRoute()) > 0;
    }

    @Override
    public boolean updateRoute(Routes route) {
        String sql = "UPDATE routes SET name = ?, start_location = ?, end_location = ?, total_stops = ?, is_walking_route = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
            route.getName(),
            route.getStartLocation(),
            route.getEndLocation(),
            route.getTotalStops(),
            route.getIsWalkingRoute(),
            route.getId()) > 0;
    }

    @Override
    public boolean deleteRoute(Integer id) {
        String sql = "DELETE FROM routes WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
} 