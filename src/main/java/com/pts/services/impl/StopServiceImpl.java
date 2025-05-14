package com.pts.services.impl;

import com.pts.pojo.Stops;
import com.pts.repositories.StopRepository;
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
    public List<Stops> searchStops(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStops();
        }
        return stopRepository.searchStops(keyword);
    }
    // Thêm vào StopServiceImpl.java

    @Override
    public List<Stops> findNearbyStops(double lat, double lng, double radiusMeters) {
        // Giới hạn bán kính tìm kiếm là 1000m
        double effectiveRadius = Math.min(radiusMeters, 1000);

        // Chuyển đổi bán kính từ mét sang độ (xấp xỉ)
        double radiusDegrees = effectiveRadius / 111000.0; // 1 độ ~ 111km tại xích đạo

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

        // Chuyển đổi bán kính từ mét sang độ (xấp xỉ)
        double radiusDegrees = effectiveRadius / 111000.0; // 1 độ ~ 111km tại xích đạo

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

            // Thêm thông tin về tuyến nếu có
            if (stop.getRouteId() != null) {
                stopMap.put("routeId", stop.getRouteId().getId());
                stopMap.put("routeName", stop.getRouteId().getName());
                stopMap.put("routeColor", stop.getRouteId().getRouteColor());
            }

            result.add(stopMap);
        }

        // Sắp xếp kết quả theo khoảng cách gần nhất
        result.sort(Comparator.comparingDouble(stop -> ((Number) stop.get("distance")).doubleValue()));

        return result;
    }

    private List<Stops> findNearbyStopsInternal(double lat, double lng, double radius) {
        // Thực hiện tìm kiếm trạm gần trong radius (mét)
        // Phần này có thể cần thêm vào StopRepository nếu chưa có

        // Đây là phiên bản đơn giản: lấy tất cả trạm và lọc theo khoảng cách
        List<Stops> allStops = getAllStops();
        List<Stops> nearbyStops = new ArrayList<>();

        for (Stops stop : allStops) {
            if (stop.getLatitude() != null && stop.getLongitude() != null) {
                double distance = calculateHaversineDistance(
                        lat, lng,
                        stop.getLatitude(), stop.getLongitude()
                ) * 1000; // Chuyển từ km sang m

                if (distance <= radius) {
                    nearbyStops.add(stop);
                }
            }
        }

        return nearbyStops;
    }

// Định dạng danh sách các trạm
    private List<Map<String, Object>> formatStopsList(List<Stops> stops) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Stops stop : stops) {
            Map<String, Object> stopInfo = new HashMap<>();

            stopInfo.put("id", stop.getId());
            stopInfo.put("name", stop.getStopName());
            stopInfo.put("latitude", stop.getLatitude());
            stopInfo.put("longitude", stop.getLongitude());
            stopInfo.put("address", stop.getAddress());

            if (stop.getRouteId() != null) {
                stopInfo.put("routeId", stop.getRouteId().getId());
                stopInfo.put("routeName", stop.getRouteId().getName());
            }

            result.add(stopInfo);
        }

        return result;
    }

// Công thức Haversine tính khoảng cách giữa hai điểm trên mặt đất
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
