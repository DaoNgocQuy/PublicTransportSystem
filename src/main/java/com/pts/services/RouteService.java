package com.pts.services;

import com.pts.pojo.Routes;
import java.util.List;
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
}
