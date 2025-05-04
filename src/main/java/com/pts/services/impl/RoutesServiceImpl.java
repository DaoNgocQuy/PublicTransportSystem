/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.services.impl;

import com.pts.pojo.Routes;
import com.pts.repositories.RoutesRepository;
import com.pts.services.RoutesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoutesServiceImpl implements RoutesService {

    @Autowired
    private RoutesRepository routesRepository;

    @Override
    public List<Routes> getAllRoutes() {
        return routesRepository.findAll();
    }

    public List<Routes> searchRoutesByName(String keyword) {
        return routesRepository.searchRoutesByName(keyword);
    }

    @Override
    public Optional<Routes> getRouteById(Integer id) {
        return routesRepository.findById(id);
    }

    @Override
    public Routes saveRoute(Routes route) {
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
}
