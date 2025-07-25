package com.pts.services;

import com.pts.pojo.RouteStop;
import com.pts.pojo.Stops;
import java.util.List;
import java.util.Map;

public interface RouteStopService {

    RouteStop save(RouteStop routeStop);

    boolean update(RouteStop routeStop);

    RouteStop findById(Integer id);

    List<RouteStop> findAll();

    List<RouteStop> findByRouteId(Integer routeId);

    List<RouteStop> findByRouteIdAndDirection(Integer routeId, Integer direction);

    List<RouteStop> findByStopId(Integer stopId);

    boolean deleteById(Integer id);

    boolean deleteByRouteId(Integer routeId);

    boolean deleteByRouteIdAndDirection(Integer routeId, Integer direction);

    boolean reorderStops(Integer routeId, List<Integer> stopIds);

    boolean reorderStops(Integer routeId, List<Integer> stopIds, Integer direction);

    RouteStop addStopToRoute(Integer routeId, Integer stopId, Integer direction, Integer stopOrder);

    boolean moveStopUp(Integer routeStopId);

    boolean moveStopDown(Integer routeStopId);

    Integer findMaxStopOrderByRouteIdAndDirection(Integer routeId, Integer direction);

    void shiftStopOrders(Integer routeId, Integer direction, Integer fromOrder);

    int countRoutesByStopId(Integer stopId);

    boolean deleteAllRouteStopsByStopId(Integer stopId);

    boolean deleteAndReorder(Integer routeStopId);

    int deleteByRouteIdStopIdAndDirection(Integer routeId, Integer stopId, Integer direction);

    boolean reorderAfterDelete(Integer routeId, Integer direction);
}
