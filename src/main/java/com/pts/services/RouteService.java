package com.pts.services;

import com.pts.pojo.Routes;
import java.util.List;

public interface RouteService {
    List<Routes> searchRoutes(String startLocation, String endLocation);
    List<Routes> getAllRoutes();
    Routes getRouteById(Integer id);
    List<Routes> getRoutesByType(boolean isWalkingRoute);
} 