package com.pts.services.impl;

import com.pts.pojo.Routes;
import com.pts.repositories.RoutesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import com.pts.services.RouteService;

@Service
public class RoutesServiceImpl implements RouteService {

    @Autowired
    private RoutesRepository routesRepository;

    @Override
    public List<Routes> getAllRoutes() {
        return routesRepository.findAll();
    }

    @Override
    public Optional<Routes> getRouteById(Integer id) {
        return routesRepository.findById(id);
    }

    @Override
    public Routes saveRoute(Routes route) {
        // Đảm bảo các giá trị mặc định được thiết lập
        if (route.getActive() == null) {
            route.setActive(true);
        }

        if (route.getIsWalkingRoute() == null) {
            route.setIsWalkingRoute(false);
        }

        return routesRepository.save(route);
    }

    @Override
    public void deleteRoute(Integer id) {
        routesRepository.deleteById(id);
    }

    @Override
    public boolean routeExists(Integer id) {
        return routesRepository.existsById(id);
    }

    @Override
    public List<Routes> findRoutesByName(String name) {
        return routesRepository.findByName(name);
    }

    @Override
    public List<Routes> findRoutesByStartLocation(String startLocation) {
        return routesRepository.findByStartLocation(startLocation);
    }

    @Override
    public List<Routes> findRoutesByEndLocation(String endLocation) {
        return routesRepository.findByEndLocation(endLocation);
    }

    @Override
    public List<Routes> findActiveRoutes() {
        return routesRepository.findByIsActive(true);
    }

    @Override
    public List<Routes> findWalkingRoutes() {
        return routesRepository.findByIsWalkingRoute(true);
    }

    @Override
    public List<Routes> findRoutesByRouteType(Integer routeTypeId) {
        return routesRepository.findByRouteTypeId(routeTypeId);
    }

    @Override
    public List<Routes> searchRoutesByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllRoutes();
        }
        return routesRepository.searchRoutesByName(keyword);
    }
}
