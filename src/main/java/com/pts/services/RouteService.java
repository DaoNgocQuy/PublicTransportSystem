package com.pts.services;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RouteService {

    List<Routes> getAllRoutes();

    Optional<Routes> getRouteById(Integer id);

    Routes saveRoute(Routes route);

    void deleteRoute(Integer id);

    boolean routeExists(Integer id);

    List<Routes> findRoutesByName(String name);

    List<Routes> findActiveRoutes();

    List<Routes> findRoutesByRouteType(Integer routeTypeId);

    List<Routes> searchRoutesByName(String keyword);

    List<Routes> findRoutesByStops(List<Integer> stopIds);

    List<Routes> findRoutesByStopAndDirection(Integer stopId, Integer direction);

    List<Stops> getStopsByRouteId(Integer routeId);

    List<Stops> getStopsByRouteIdAndDirection(Integer routeId, Integer direction);

    void recalculateRoute(Integer routeId);

    Map<String, Object> findJourneyOptions(
            Double fromLat, Double fromLng, Double toLat, Double toLng,
            Integer maxWalkDistance, String priority);

    List<List<Double>> calculateOptimalWalkingPath(Double fromLat, Double fromLng, Double toLat, Double toLng);

    List<Routes> getAllRoutesWithPagination(int page, int size);

    int getTotalRoutes();

    // Phân trang cho tìm kiếm
    List<Routes> searchRoutesByNameWithPagination(String keyword, int page, int size);

    int getTotalRoutesByKeyword(String keyword);
}
