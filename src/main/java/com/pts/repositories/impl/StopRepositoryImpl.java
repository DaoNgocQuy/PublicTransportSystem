package com.pts.repositories.impl;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import com.pts.repositories.StopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            stop.setAddress(rs.getString("address"));

            // Kiểm tra null cho các trường Boolean
            Object isAccessibleObj = rs.getObject("is_accessible");
            if (isAccessibleObj != null) {
                stop.setIsAccessible(rs.getBoolean("is_accessible"));
            }

            // Kiểm tra xem ResultSet có chứa cột stop_order không (cho các truy vấn JOIN)
            try {
                rs.findColumn("stop_order");
                stop.setStopOrder(rs.getInt("stop_order"));
            } catch (SQLException ignored) {
                // Không có cột stop_order trong ResultSet
            }

            // Kiểm tra xem ResultSet có chứa cột direction không
            try {
                rs.findColumn("direction");
                stop.setDirection(rs.getInt("direction"));
            } catch (SQLException ignored) {
                // Không có cột direction trong ResultSet
            }

            return stop;
        }
    };

    @Override
    public List<Stops> findAll() {
        String sql = "SELECT * FROM stops ORDER BY stop_name";
        return jdbcTemplate.query(sql, stopRowMapper);
    }

    @Override
    public Optional<Stops> findById(Integer id) {
        String sql = "SELECT * FROM stops WHERE id = ?";
        List<Stops> stops = jdbcTemplate.query(sql, stopRowMapper, id);
        return stops.isEmpty() ? Optional.empty() : Optional.of(stops.get(0));
    }

    @Override
    public Stops save(Stops stop) {
        if (stop.getId() == null) {
            String sql = "INSERT INTO stops (stop_name, latitude, longitude, address, is_accessible) "
                    + "VALUES (?, ?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, stop.getStopName());
                ps.setObject(2, stop.getLatitude());
                ps.setObject(3, stop.getLongitude());
                ps.setString(4, stop.getAddress());
                ps.setObject(5, stop.getIsAccessible());
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                stop.setId(keyHolder.getKey().intValue());
            }
        } else {
            String sql = "UPDATE stops SET stop_name = ?, latitude = ?, longitude = ?, address = ?, "
                    + "is_accessible = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    stop.getStopName(),
                    stop.getLatitude(),
                    stop.getLongitude(),
                    stop.getAddress(),
                    stop.getIsAccessible(),
                    stop.getId());
        }
        return stop;
    }

    @Override
    public void deleteById(Integer id) {
        // Trước khi xóa điểm dừng, hãy xóa các tham chiếu trong bảng route_stops
        String deleteRouteStopsSql = "DELETE FROM route_stops WHERE stop_id = ?";
        jdbcTemplate.update(deleteRouteStopsSql, id);

        // Sau đó xóa điểm dừng
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
        String sql = "SELECT * FROM stops WHERE stop_name LIKE ?";
        return jdbcTemplate.query(sql, stopRowMapper, "%" + stopName + "%");
    }

    @Override
    public List<Stops> findByAddress(String address) {
        String sql = "SELECT * FROM stops WHERE address LIKE ?";
        return jdbcTemplate.query(sql, stopRowMapper, "%" + address + "%");
    }

    @Override
    public List<Stops> findByRouteId(Integer routeId) {
        String sql = "SELECT s.*, rs.stop_order, rs.direction FROM stops s "
                + "JOIN route_stops rs ON s.id = rs.stop_id "
                + "WHERE rs.route_id = ? "
                + "ORDER BY rs.stop_order";
        return jdbcTemplate.query(sql, stopRowMapper, routeId);
    }

    @Override
    public List<Stops> findByRouteIdAndDirection(Integer routeId, Integer direction) {
        // Thêm log và debug
        System.out.println("findByRouteIdAndDirection: Tìm trạm cho routeId=" + routeId + ", direction=" + direction);

        String sql = "SELECT s.* FROM stops s "
                + "JOIN route_stops rs ON s.id = rs.stop_id "
                + "WHERE rs.route_id = ? AND rs.direction = ? "
                + "ORDER BY rs.stop_order";

        List<Stops> stops = jdbcTemplate.query(sql, stopRowMapper, routeId, direction);

        // Debug kết quả
        System.out.println("findByRouteIdAndDirection: Tìm thấy " + stops.size() + " trạm");
        for (Stops stop : stops) {
            // Transfer the direction information to each stop
            stop.setDirection(direction);
        }

        return stops;
    }

    @Override
    public List<Stops> searchStops(String keyword) {
        String sql = "SELECT * FROM stops WHERE stop_name LIKE ? OR address LIKE ?";
        String searchParam = "%" + keyword + "%";
        return jdbcTemplate.query(sql, stopRowMapper, searchParam, searchParam);
    }

    @Override
    public List<Stops> findNearbyStops(double latitude, double longitude, double radius) {
        // Công thức Haversine để tính khoảng cách giữa 2 điểm trên bề mặt trái đất
        String sql = "SELECT *, "
                + "6371000 * acos(cos(radians(?)) * cos(radians(latitude)) * cos(radians(longitude) - radians(?)) "
                + "+ sin(radians(?)) * sin(radians(latitude))) AS distance "
                + "FROM stops "
                + "HAVING distance < ? "
                + "ORDER BY distance";

        return jdbcTemplate.query(sql, stopRowMapper, latitude, longitude, latitude, radius);
    }

    @Override
    public List<Stops> findStopsByRouteIdAndStopOrderRange(Integer routeId, Integer startOrder, Integer endOrder) {
        String sql = "SELECT s.* FROM stops s "
                + "JOIN route_stops rs ON s.id = rs.stop_id "
                + "WHERE rs.route_id = ? AND rs.stop_order BETWEEN ? AND ? "
                + "ORDER BY rs.stop_order";
        return jdbcTemplate.query(sql, stopRowMapper, routeId, startOrder, endOrder);
    }
}
