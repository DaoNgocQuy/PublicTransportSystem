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

    List<Routes> findRoutesByStartLocation(String startLocation);

    List<Routes> findRoutesByEndLocation(String endLocation);

    List<Routes> findActiveRoutes();

    List<Routes> findWalkingRoutes();

    List<Routes> findRoutesByRouteType(Integer routeTypeId);

    List<Routes> searchRoutesByName(String keyword);

    List<Map<String, Object>> findRoutesWithStops(double fromLat, double fromLng, double toLat, double toLng,
            double maxWalkDistance, int maxTransfers, String routePriority);

    List<Routes> findRoutesByStops(List<Integer> stopIds);

    List<Routes> findRoutesByStopAndDirection(Integer stopId, Integer direction);

    List<Stops> getStopsByRouteId(Integer routeId);

    List<Stops> getStopsByRouteIdAndDirection(Integer routeId, Integer direction);

    void recalculateRoute(Integer routeId);
}
