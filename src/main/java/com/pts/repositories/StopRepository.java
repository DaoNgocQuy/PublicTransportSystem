package com.pts.repositories;

import com.pts.pojo.Stops;

import java.util.List;

import java.util.Optional;

public interface StopRepository {

    List<Stops> findAll();

    Optional<Stops> findById(Integer id);

    Stops save(Stops stop);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<Stops> findByStopName(String stopName);

    List<Stops> findByAddress(String address);

    List<Stops> findByRouteId(Integer routeId);

    List<Stops> findByRouteIdAndDirection(Integer routeId, Integer direction);

    List<Stops> searchStops(String keyword);

    List<Stops> findNearbyStops(double latitude, double longitude, double radius);

    List<Stops> findStopsByRouteIdAndStopOrderRange(Integer routeId, Integer startOrder, Integer endOrder);

}
