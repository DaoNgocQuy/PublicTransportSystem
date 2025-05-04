/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.repositories;

import com.pts.pojo.Routes;
import java.util.List;
import java.util.Optional;

public interface RoutesRepository {

    List<Routes> findAll();

    Optional<Routes> findById(Integer id);

    Routes save(Routes route);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<Routes> findByName(String name);

    List<Routes> findByStartLocation(String startLocation);

    List<Routes> findByEndLocation(String endLocation);

    List<Routes> findByTotalStops(Integer totalStops);

    List<Routes> findByIsWalkingRoute(Boolean isWalkingRoute);

    List<Routes> searchRoutesByName(String keyword);

}
