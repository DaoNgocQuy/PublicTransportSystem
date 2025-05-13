package com.pts.services.impl;

import com.pts.pojo.Stops;
import com.pts.repositories.StopRepository;
import com.pts.services.StopService;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public List<Stops> findNearbyStops(double latitude, double longitude, double radius) {
        return stopRepository.findNearbyStops(latitude, longitude, radius);
    }

    @Override
    public List<Map<String, Object>> findNearbyStopsFormatted(double lat, double lng, double radius) {
        List<Stops> nearbyStops = findNearbyStops(lat, lng, radius); // Sử dụng phương thức đã có
        return formatStopsList(nearbyStops);
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
