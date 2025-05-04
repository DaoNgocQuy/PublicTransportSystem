/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.repositories;

import com.pts.pojo.Route;
import java.util.List;
import java.util.Optional;

public interface RoutesRepository {

    List<Route> findAll();

    Optional<Route> findById(Integer id);

    Route save(Route route);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<Route> findByName(String name);

    List<Route> findByStartLocation(String startLocation);

    List<Route> findByEndLocation(String endLocation);

    List<Route> findByTotalStops(Integer totalStops);

    List<Route> findByIsWalkingRoute(Boolean isWalkingRoute);

    List<Route> searchRoutesByName(String keyword);

}
