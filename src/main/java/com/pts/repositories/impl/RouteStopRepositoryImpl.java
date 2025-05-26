package com.pts.repositories.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.pts.pojo.Routes;
import com.pts.pojo.RouteStop;
import com.pts.pojo.Stops;
import com.pts.repositories.RoutesRepository;
import com.pts.repositories.RouteStopRepository;
import com.pts.repositories.StopRepository;

@Repository
public class RouteStopRepositoryImpl implements RouteStopRepository {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RoutesRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    @Override
    public RouteStop save(RouteStop routeStop) {
        // Cập nhật SQL để thêm direction vào bảng
        String sql = "INSERT INTO route_stops (route_id, stop_id, stop_order, direction) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, routeStop.getRoute().getId());
            ps.setInt(2, routeStop.getStop().getId());
            ps.setInt(3, routeStop.getStopOrder());

            // Kiểm tra và thiết lập direction
            if (routeStop.getDirection() != null) {
                ps.setInt(4, routeStop.getDirection());
            } else {
                ps.setObject(4, null);  // Nếu không có chiều, sẽ để NULL
            }

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        routeStop.setId(rs.getInt(1));
                        return routeStop;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean update(RouteStop routeStop) {
        // Cập nhật SQL để bao gồm direction
        String sql = "UPDATE route_stops SET route_id = ?, stop_id = ?, stop_order = ?, direction = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, routeStop.getRoute().getId());
            ps.setInt(2, routeStop.getStop().getId());
            ps.setInt(3, routeStop.getStopOrder());

            // Kiểm tra và thiết lập direction
            if (routeStop.getDirection() != null) {
                ps.setInt(4, routeStop.getDirection());
            } else {
                ps.setObject(4, null);  // Nếu không có chiều, sẽ để NULL
            }

            ps.setInt(5, routeStop.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public RouteStop findById(Integer id) {
        String sql = "SELECT * FROM route_stops WHERE id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRouteStop(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<RouteStop> findAll() {
        String sql = "SELECT * FROM route_stops";
        List<RouteStop> routeStops = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RouteStop routeStop = mapRowToRouteStop(rs);
                if (routeStop != null) {
                    routeStops.add(routeStop);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return routeStops;
    }

    @Override
    public List<RouteStop> findByRouteIdOrderByStopOrder(Integer routeId) {
        String sql = "SELECT * FROM route_stops WHERE route_id = ? ORDER BY stop_order";
        List<RouteStop> routeStops = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, routeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RouteStop routeStop = mapRowToRouteStop(rs);
                    if (routeStop != null) {
                        routeStops.add(routeStop);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return routeStops;
    }

    @Override
    public List<RouteStop> findByRouteIdAndDirectionOrderByStopOrder(Integer routeId, Integer direction) {
        String sql = "SELECT * FROM route_stops WHERE route_id = ? AND direction = ? ORDER BY stop_order";
        List<RouteStop> routeStops = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, routeId);
            ps.setInt(2, direction);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RouteStop routeStop = mapRowToRouteStop(rs);
                    if (routeStop != null) {
                        routeStops.add(routeStop);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return routeStops;
    }

    @Override
    public List<RouteStop> findByStopId(Integer stopId) {
        String sql = "SELECT * FROM route_stops WHERE stop_id = ?";
        List<RouteStop> routeStops = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stopId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RouteStop routeStop = mapRowToRouteStop(rs);
                    if (routeStop != null) {
                        routeStops.add(routeStop);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return routeStops;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM route_stops WHERE id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteByRouteId(Integer routeId) {
        String sql = "DELETE FROM route_stops WHERE route_id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, routeId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteByRouteIdAndDirection(Integer routeId, Integer direction) {
        String sql = "DELETE FROM route_stops WHERE route_id = ? AND direction = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, routeId);
            ps.setInt(2, direction);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean existsByStopId(Integer stopId) {
        String sql = "SELECT COUNT(*) FROM route_stops WHERE stop_id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stopId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Integer findMaxStopOrderByRouteId(Integer routeId) {
        String sql = "SELECT MAX(stop_order) FROM route_stops WHERE route_id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, routeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public Integer findMaxStopOrderByRouteIdAndDirection(Integer routeId, Integer direction) {
        String sql = "SELECT MAX(stop_order) FROM route_stops WHERE route_id = ? AND direction = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, routeId);
            ps.setInt(2, direction);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private RouteStop mapRowToRouteStop(ResultSet rs) throws SQLException {
        RouteStop routeStop = new RouteStop();
        routeStop.setId(rs.getInt("id"));

        Optional<Routes> routeOpt = routeRepository.findById(rs.getInt("route_id"));
        Optional<Stops> stopOpt = stopRepository.findById(rs.getInt("stop_id"));

        if (routeOpt.isPresent() && stopOpt.isPresent()) {
            routeStop.setRoute(routeOpt.get());
            routeStop.setStop(stopOpt.get());
            routeStop.setStopOrder(rs.getInt("stop_order"));

            // Đọc trường direction từ cơ sở dữ liệu
            int direction = rs.getInt("direction");
            if (!rs.wasNull()) {
                routeStop.setDirection(direction);
            }

            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                routeStop.setCreatedAt(createdAt.toLocalDateTime());
            }

            return routeStop;
        }

        return null;
    }
}
