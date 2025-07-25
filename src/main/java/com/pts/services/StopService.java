package com.pts.services;

import com.pts.pojo.Stops;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StopService {

    List<Stops> getAllStops();

    Optional<Stops> getStopById(Integer id);

    Stops saveStop(Stops stop);

    void deleteStop(Integer id);

    boolean stopExists(Integer id);

    List<Stops> findStopsByRouteId(Integer routeId);

    List<Stops> findStopsByRouteIdAndDirection(Integer routeId, Integer direction);

    List<Stops> searchStops(String keyword);

    List<Stops> findNearbyStops(double lat, double lng, int radiusInMeters);

    List<Map<String, Object>> findNearbyStopsFormatted(double lat, double lng, double radiusMeters);

    Map<String, Object> getStopsWithPagination(String keyword, int page);

}
