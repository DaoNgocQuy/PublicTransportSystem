package com.pts.repositories;

import com.pts.pojo.RouteStop;
import java.util.List;

public interface RouteStopRepository {

    RouteStop save(RouteStop routeStop);

    boolean update(RouteStop routeStop);

    RouteStop findById(Integer id);

    List<RouteStop> findAll();

    List<RouteStop> findByRouteIdOrderByStopOrder(Integer routeId);

    // Thêm phương thức mới để lấy điểm dừng theo tuyến và chiều
    List<RouteStop> findByRouteIdAndDirectionOrderByStopOrder(Integer routeId, Integer direction);

    List<RouteStop> findByStopId(Integer stopId);

    boolean deleteById(Integer id);

    boolean deleteByRouteId(Integer routeId);

    // Thêm phương thức xóa theo tuyến và chiều
    boolean deleteByRouteIdAndDirection(Integer routeId, Integer direction);

    boolean existsByStopId(Integer stopId);

    Integer findMaxStopOrderByRouteId(Integer routeId);

    boolean swapStopOrders(Integer firstStopId, Integer secondStopId);

    boolean deleteAndReorder(Integer routeStopId);

    // Thêm phương thức để lấy stopOrder lớn nhất cho tuyến và chiều cụ thể
    Integer findMaxStopOrderByRouteIdAndDirection(Integer routeId, Integer direction);
}