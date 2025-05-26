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
    
    RouteStop addStopToRoute(Integer routeId, Integer stopId);
    
    RouteStop addStopToRoute(Integer routeId, Integer stopId, Integer direction);
    
    boolean swapStopOrder(Integer routeStopId1, Integer routeStopId2);
    
    List<Stops> getAvailableStopsForRoute(Integer routeId, Integer direction);
    
    List<Map<String, Object>> getStopCoordinatesForRoute(Integer routeId, Integer direction);
    
    boolean moveStopUp(Integer routeStopId);
    
    boolean moveStopDown(Integer routeStopId);
}