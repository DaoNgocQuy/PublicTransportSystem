package com.pts.services.impl;

import com.pts.pojo.Routes;
import com.pts.repositories.RouteRepository;
import com.pts.services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RouteServiceImpl implements RouteService {

    @Autowired
    private RouteRepository routeRepository;

    @Override
    public List<Routes> searchRoutes(String startLocation, String endLocation) {
        return routeRepository.searchRoutes(startLocation, endLocation);
    }

    @Override
    public List<Routes> getAllRoutes() {
        return routeRepository.getAllRoutes();
    }

    @Override
    public Routes getRouteById(Integer id) {
        return routeRepository.getRouteById(id);
    }

    @Override
    public List<Routes> getRoutesByType(boolean isWalkingRoute) {
        return routeRepository.getRoutesByType(isWalkingRoute);
    }

    @Override
    public boolean createRoute(Routes route) {
        return routeRepository.addRoute(route);
    }

    @Override
    public boolean updateRoute(Routes route) {
        return routeRepository.updateRoute(route);
    }

    @Override
    public boolean deleteRoute(Integer id) {
        return routeRepository.deleteRoute(id);
    }
} 