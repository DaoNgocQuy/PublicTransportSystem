/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author 84353
 */
package com.pts.services;

import com.pts.pojo.Route;
import java.util.List;
import java.util.Optional;

public interface RoutesService {

    List<Route> getAllRoutes();

    Optional<Route> getRouteById(Integer id);

    Route saveRoute(Route route);

    void deleteRoute(Integer id);

    boolean routeExists(Integer id);

    List<Route> findRoutesByName(String name);

    public List<Route> searchRoutesByName(String keyword);
}
