package com.pts.repositories;

import com.pts.pojo.RouteTypes;
import java.util.List;
import java.util.Optional;

public interface RouteTypeRepository {
    List<RouteTypes> getAllRouteTypes();

    Optional<RouteTypes> getRouteTypeById(Integer id);

    RouteTypes save(RouteTypes routeType);

}