package com.pts.repositories.impl;

import com.pts.pojo.Landmarks;
import com.pts.pojo.Stops;
import com.pts.repositories.LandmarkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class LandmarkRepositoryImpl implements LandmarkRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedTemplate;
    private final SimpleJdbcInsert insertAction;

    @Autowired
    public LandmarkRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.insertAction = new SimpleJdbcInsert(dataSource)
                .withTableName("landmarks")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Landmarks> findAll() {
        String sql = "SELECT l.*, s.stop_name as stop_name FROM landmarks l "
                + "LEFT JOIN stops s ON l.nearest_stop_id = s.id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Landmarks.class));
    }

    @Override
    public Optional<Landmarks> findById(Integer id) {
        String sql = "SELECT l.*, s.stop_name as stop_name FROM landmarks l "
                + "LEFT JOIN stops s ON l.nearest_stop_id = s.id WHERE l.id = ?";
        List<Landmarks> landmarks = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Landmarks.class), id);
        return landmarks.isEmpty() ? Optional.empty() : Optional.of(landmarks.get(0));
    }

    @Override
    public Landmarks save(Landmarks landmark) {
        if (landmark.getId() != null && existsById(landmark.getId())) {
            // Update
            String sql = "UPDATE landmarks SET name = :name, address = :address, latitude = :latitude, "
                    + "longitude = :longitude, description = :description, landmark_type = :landmarkType, "
                    + "icon = :icon, tags = :tags, nearest_stop_id = :nearestStopId, "
                    + "last_updated = CURRENT_TIMESTAMP WHERE id = :id";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", landmark.getId());
            params.addValue("name", landmark.getName());
            params.addValue("address", landmark.getAddress());
            params.addValue("latitude", landmark.getLatitude());
            params.addValue("longitude", landmark.getLongitude());
            params.addValue("description", landmark.getDescription());
            params.addValue("landmarkType", landmark.getLandmarkType());
            params.addValue("icon", landmark.getIcon());
            params.addValue("tags", landmark.getTags());
            params.addValue("nearestStopId", landmark.getNearestStop() != null ? landmark.getNearestStop().getId() : null);
            
            namedTemplate.update(sql, params);
            return landmark;
        } else {
            // Insert
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", landmark.getName());
            params.addValue("address", landmark.getAddress());
            params.addValue("latitude", landmark.getLatitude());
            params.addValue("longitude", landmark.getLongitude());
            params.addValue("description", landmark.getDescription());
            params.addValue("landmark_type", landmark.getLandmarkType());
            params.addValue("icon", landmark.getIcon());
            params.addValue("tags", landmark.getTags());
            params.addValue("nearest_stop_id", landmark.getNearestStop() != null ? landmark.getNearestStop().getId() : null);
            
            Number id = insertAction.executeAndReturnKey(params.getValues());
            landmark.setId(id.intValue());
            return landmark;
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM landmarks WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM landmarks WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Landmarks> findByName(String name) {
        String sql = "SELECT l.*, s.stop_name as stop_name FROM landmarks l "
                + "LEFT JOIN stops s ON l.nearest_stop_id = s.id WHERE l.name = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Landmarks.class), name);
    }

    @Override
    public List<Landmarks> findByAddress(String address) {
        String sql = "SELECT l.*, s.stop_name as stop_name FROM landmarks l "
                + "LEFT JOIN stops s ON l.nearest_stop_id = s.id WHERE l.address LIKE ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Landmarks.class), "%" + address + "%");
    }

    @Override
    public List<Landmarks> findByLandmarkType(String landmarkType) {
        String sql = "SELECT l.*, s.stop_name as stop_name FROM landmarks l "
                + "LEFT JOIN stops s ON l.nearest_stop_id = s.id WHERE l.landmark_type = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Landmarks.class), landmarkType);
    }

    @Override
    public List<Landmarks> searchLandmarks(String keyword) {
        String sql = "SELECT l.*, s.stop_name as stop_name FROM landmarks l "
                + "LEFT JOIN stops s ON l.nearest_stop_id = s.id "
                + "WHERE LOWER(l.name) LIKE LOWER(?) OR LOWER(l.address) LIKE LOWER(?) OR LOWER(l.tags) LIKE LOWER(?)";
        String searchPattern = "%" + keyword + "%";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Landmarks.class), 
                searchPattern, searchPattern, searchPattern);
    }

    @Override
    public List<Landmarks> findNearbyLandmarks(double latitude, double longitude, double radius) {
        String sql = "SELECT l.*, s.stop_name as stop_name FROM landmarks l "
                + "LEFT JOIN stops s ON l.nearest_stop_id = s.id "
                + "WHERE (6371 * acos(cos(radians(?)) * cos(radians(l.latitude)) * "
                + "cos(radians(l.longitude) - radians(?)) + "
                + "sin(radians(?)) * sin(radians(l.latitude)))) * 1000 <= ? "
                + "ORDER BY (6371 * acos(cos(radians(?)) * cos(radians(l.latitude)) * "
                + "cos(radians(l.longitude) - radians(?)) + "
                + "sin(radians(?)) * sin(radians(l.latitude)))) * 1000 ASC";
        
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Landmarks.class),
                latitude, longitude, latitude, radius, latitude, longitude, latitude);
    }
}