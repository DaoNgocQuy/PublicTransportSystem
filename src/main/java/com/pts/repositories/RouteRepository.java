package com.pts.repositories;

import com.pts.pojo.Routes;
import java.util.List;

public interface RouteRepository {
    List<Routes> getAllRoutes();
    Routes getRouteById(Integer id);
    List<Routes> searchRoutes(String startLocation, String endLocation);
    List<Routes> getRoutesByType(boolean isWalkingRoute);
} 