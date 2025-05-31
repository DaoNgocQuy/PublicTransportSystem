package com.pts.services.impl;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import com.pts.pojo.RouteStop;
import com.pts.repositories.StopRepository;
import com.pts.repositories.RoutesRepository;
import com.pts.repositories.RouteStopRepository;
import com.pts.services.StopService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StopServiceImpl implements StopService {

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RoutesRepository routesRepository;

    @Autowired
    private RouteStopRepository routeStopRepository;

    @Override
    public List<Stops> getAllStops() {
        return stopRepository.findAll();
    }

    @Override
    public Optional<Stops> getStopById(Integer id) {
        return stopRepository.findById(id);
    }

    @Override
    public Stops saveStop(Stops stop) {
        // Thiết lập giá trị mặc định nếu chưa có
        if (stop.getIsAccessible() == null) {
            stop.setIsAccessible(true);
        }

        return stopRepository.save(stop);
    }

    @Override
    public void deleteStop(Integer id) {
        stopRepository.deleteById(id);
    }

    @Override
    public boolean stopExists(Integer id) {
        return stopRepository.existsById(id);
    }

    @Override
    public List<Stops> findStopsByRouteId(Integer routeId) {
        return stopRepository.findByRouteId(routeId);
    }

    @Override
    public List<Stops> findStopsByRouteIdAndDirection(Integer routeId, Integer direction) {
        return stopRepository.findByRouteIdAndDirection(routeId, direction);
    }

    @Override
    public List<Stops> searchStops(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStops();
        }
        return stopRepository.searchStops(keyword);
    }

    @Override
    public List<Stops> findNearbyStops(double lat, double lng, int radiusInMeters) {
        // Tìm tất cả các trạm
        List<Stops> allStops = stopRepository.findAll();

        // Lọc các trạm trong bán kính
        double radiusInKm = radiusInMeters / 1000.0;
        return allStops.stream()
                .filter(stop -> {
                    double distance = calculateDistance(lat, lng, stop.getLatitude(), stop.getLongitude());
                    return distance <= radiusInKm;
                })
                .sorted((s1, s2) -> {
                    double d1 = calculateDistance(lat, lng, s1.getLatitude(), s1.getLongitude());
                    double d2 = calculateDistance(lat, lng, s2.getLatitude(), s2.getLongitude());
                    return Double.compare(d1, d2);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> findNearbyStopsFormatted(double lat, double lng, double radiusMeters) {
        // Giới hạn bán kính tối đa là 1000m
        if (radiusMeters > 1000) {
            radiusMeters = 1000;
        }

        List<Stops> nearbyStops = findNearbyStops(lat, lng, (int) radiusMeters);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Stops stop : nearbyStops) {
            // Bỏ qua trạm hiện tại nếu có
            if (stop.getLatitude() == lat && stop.getLongitude() == lng) {
                continue;
            }

            Map<String, Object> stopData = new HashMap<>();
            stopData.put("id", stop.getId());
            stopData.put("name", stop.getStopName());
            stopData.put("address", stop.getAddress());
            stopData.put("lat", stop.getLatitude());
            stopData.put("lng", stop.getLongitude());

            // Tính khoảng cách từ điểm hiện tại
            double distance = calculateDistance(lat, lng, stop.getLatitude(), stop.getLongitude());
            stopData.put("distance", Math.round(distance * 1000)); // Chuyển sang mét và làm tròn

            result.add(stopData);
        }

        return result;
    }

    

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the Earth in km
        final int R = 6371;

        // Convert degrees to radians
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance in km
        return R * c;
    }
}
