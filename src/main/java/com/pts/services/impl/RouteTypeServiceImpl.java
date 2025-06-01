package com.pts.services.impl;

import com.pts.pojo.RouteTypes;
import com.pts.repositories.RouteTypeRepository;
import com.pts.services.RouteTypeService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RouteTypeServiceImpl implements RouteTypeService {

    @Autowired
    private RouteTypeRepository routeTypeRepository;

    @Override
    public List<RouteTypes> getAllRouteTypes() {
        return routeTypeRepository.getAllRouteTypes();
    }

    @Override
    public Optional<RouteTypes> getRouteTypeById(Integer id) {
        return routeTypeRepository.getRouteTypeById(id);
    }

    @Override
    public RouteTypes saveRouteType(RouteTypes routeType) {
        return routeTypeRepository.save(routeType);
    }
}