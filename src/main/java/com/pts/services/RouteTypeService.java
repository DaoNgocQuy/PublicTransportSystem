package com.pts.services;

import com.pts.pojo.RouteTypes;
import java.util.List;
import java.util.Optional;

public interface RouteTypeService {
    List<RouteTypes> getAllRouteTypes();
    Optional<RouteTypes> getRouteTypeById(Integer id);
    RouteTypes saveRouteType(RouteTypes routeType);
}