/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.repositories.impl;

import com.pts.pojo.Route;
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

    private final RowMapper<Route> routesRowMapper = new RowMapper<Route>() {
        @Override
        public Route mapRow(ResultSet rs, int rowNum) throws SQLException {
            Route route = new Route();
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
    public List<Route> findAll() {
        String sql = "SELECT * FROM routes";
        return jdbcTemplate.query(sql, routesRowMapper);
    }

    @Override
    public Optional<Route> findById(Integer id) {
        String sql = "SELECT * FROM routes WHERE id = ?";
        List<Route> routes = jdbcTemplate.query(sql, routesRowMapper, id);
        return routes.isEmpty() ? Optional.empty() : Optional.of(routes.get(0));
    }

    @Override
    public Route save(Route route) {
        if (route.getId() == null) {
            String sql = "INSERT INTO routes (name, start_location, end_location, total_stops, is_walking_route) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    route.getName(),
                    route.getStartLocation(),
                    route.getEndLocation(),
                    route.getTotalStops(),
                    route.getIsWalkingRoute());
        } else {
            String sql = "UPDATE routes SET name = ?, start_location = ?, end_location = ?, total_stops = ?, is_walking_route = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    route.getName(),
                    route.getStartLocation(),
                    route.getEndLocation(),
                    route.getTotalStops(),
                    route.getIsWalkingRoute(),
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
    public List<Route> findByName(String name) {
        String sql = "SELECT * FROM routes WHERE name = ?";
        return jdbcTemplate.query(sql, routesRowMapper, name);
    }

    @Override
    public List<Route> findByStartLocation(String startLocation) {
        String sql = "SELECT * FROM routes WHERE start_location = ?";
        return jdbcTemplate.query(sql, routesRowMapper, startLocation);
    }

    @Override
    public List<Route> findByEndLocation(String endLocation) {
        String sql = "SELECT * FROM routes WHERE end_location = ?";
        return jdbcTemplate.query(sql, routesRowMapper, endLocation);
    }

    @Override
    public List<Route> findByTotalStops(Integer totalStops) {
        String sql = "SELECT * FROM routes WHERE total_stops = ?";
        return jdbcTemplate.query(sql, routesRowMapper, totalStops);
    }

    @Override
    public List<Route> findByIsWalkingRoute(Boolean isWalkingRoute) {
        String sql = "SELECT * FROM routes WHERE is_walking_route = ?";
        return jdbcTemplate.query(sql, routesRowMapper, isWalkingRoute);
    }

    @Override
    public List<Route> searchRoutesByName(String keyword) {
        String sql = "SELECT * FROM routes WHERE name LIKE ?";
        return jdbcTemplate.query(sql, routesRowMapper, "%" + keyword + "%");
    }
}
