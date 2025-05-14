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

    List<Stops> findStopsByName(String stopName);

    List<Stops> findStopsByAddress(String address);

    List<Stops> findStopsByRouteId(Integer routeId);

    List<Stops> searchStops(String keyword);

    List<Stops> findNearbyStops(double latitude, double longitude, double radius);
    
    List<Map<String, Object>> findNearbyStopsFormatted(double lat, double lng, double radius);
}
