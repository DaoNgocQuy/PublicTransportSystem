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
    public List<Stops> findStopsByName(String stopName) {
        return stopRepository.findByStopName(stopName);
    }

    @Override
    public List<Stops> findStopsByAddress(String address) {
        return stopRepository.findByAddress(address);
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
    public List<Stops> findNearbyStops(double lat, double lng, double radiusMeters) {
        // Giới hạn bán kính tìm kiếm là 1000m
        double effectiveRadius = Math.min(radiusMeters, 1000);

        // Lấy tất cả trạm từ repository
        List<Stops> allStops = stopRepository.findAll();

        // Lọc ra các trạm trong bán kính
        return allStops.stream().filter(stop -> {
            if (stop.getLatitude() == null || stop.getLongitude() == null) {
                return false;
            }

            double stopLat = stop.getLatitude();
            double stopLng = stop.getLongitude();

            // Tính khoảng cách bằng công thức Haversine
            double distance = calculateHaversineDistance(lat, lng, stopLat, stopLng);

            // Chuyển km thành m và so sánh với bán kính
            return (distance * 1000) <= effectiveRadius;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> findNearbyStopsFormatted(double lat, double lng, double radiusMeters) {
        // Giới hạn bán kính tối đa là 1000m
        double effectiveRadius = Math.min(radiusMeters, 1000);

        List<Stops> nearbyStops = findNearbyStops(lat, lng, effectiveRadius);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Stops stop : nearbyStops) {
            Map<String, Object> stopMap = new HashMap<>();
            stopMap.put("id", stop.getId());
            stopMap.put("name", stop.getStopName());
            stopMap.put("address", stop.getAddress());
            stopMap.put("latitude", stop.getLatitude());
            stopMap.put("longitude", stop.getLongitude());

            // Tính khoảng cách chính xác từ vị trí người dùng đến trạm
            double distance = calculateHaversineDistance(lat, lng, stop.getLatitude(), stop.getLongitude()) * 1000; // Chuyển km -> m
            stopMap.put("distance", Math.round(distance)); // Làm tròn khoảng cách

            // Lấy danh sách các tuyến đi qua điểm dừng này thông qua bảng route_stops
            List<RouteStop> routeStops = routeStopRepository.findByStopId(stop.getId());
            if (!routeStops.isEmpty()) {
                List<Map<String, Object>> routeInfos = new ArrayList<>();

                for (RouteStop rs : routeStops) {
                    Routes route = rs.getRoute();
                    Map<String, Object> routeMap = new HashMap<>();
                    routeMap.put("id", route.getId());
                    routeMap.put("name", route.getName());
                    routeMap.put("color", route.getRouteColor());
                    routeMap.put("stopOrder", rs.getStopOrder());

                    // Thêm thông tin chiều
                    if (rs.getDirection() != null) {
                        routeMap.put("direction", rs.getDirection());
                    }

                    routeInfos.add(routeMap);
                }

                stopMap.put("routes", routeInfos);
            }

            result.add(stopMap);
        }

        // Sắp xếp kết quả theo khoảng cách gần nhất
        result.sort(Comparator.comparingDouble(stop -> ((Number) stop.get("distance")).doubleValue()));

        return result;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Bán kính trái đất trong km
        final int R = 6371;

        // Chuyển đổi độ sang radian
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Công thức Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Khoảng cách theo km
        return R * c;
    }
}
