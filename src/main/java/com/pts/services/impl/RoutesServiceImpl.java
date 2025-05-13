package com.pts.services.impl;

import com.pts.pojo.Routes;
import com.pts.repositories.RoutesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import com.pts.services.RouteService;
import com.pts.services.StopService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class RoutesServiceImpl implements RouteService {

    @Autowired
    private RoutesRepository routesRepository;
    @Autowired
    private StopService stopService;

    @Override
    public List<Routes> getAllRoutes() {
        return routesRepository.findAll();
    }

    @Override
    public Optional<Routes> getRouteById(Integer id) {
        return routesRepository.findById(id);
    }

    @Override
    public Routes saveRoute(Routes route) {
        // Đảm bảo các giá trị mặc định được thiết lập
        if (route.getActive() == null) {
            route.setActive(true);
        }

        if (route.getIsWalkingRoute() == null) {
            route.setIsWalkingRoute(false);
        }

        return routesRepository.save(route);
    }

    @Override
    public void deleteRoute(Integer id) {
        routesRepository.deleteById(id);
    }

    @Override
    public boolean routeExists(Integer id) {
        return routesRepository.existsById(id);
    }

    @Override
    public List<Routes> findRoutesByName(String name) {
        return routesRepository.findByName(name);
    }

    @Override
    public List<Routes> findRoutesByStartLocation(String startLocation) {
        return routesRepository.findByStartLocation(startLocation);
    }

    @Override
    public List<Routes> findRoutesByEndLocation(String endLocation) {
        return routesRepository.findByEndLocation(endLocation);
    }

    @Override
    public List<Routes> findActiveRoutes() {
        return routesRepository.findByIsActive(true);
    }

    @Override
    public List<Routes> findWalkingRoutes() {
        return routesRepository.findByIsWalkingRoute(true);
    }

    @Override
    public List<Routes> findRoutesByRouteType(Integer routeTypeId) {
        return routesRepository.findByRouteTypeId(routeTypeId);
    }

    @Override
    public List<Routes> searchRoutesByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllRoutes();
        }
        return routesRepository.searchRoutesByName(keyword);
    }

    @Override
    public List<Map<String, Object>> findRoutesWithStops(double fromLat, double fromLng,
            double toLat, double toLng, double maxWalkDistance,
            int maxTransfers, String routePriority) {

        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 1. Tìm trạm gần điểm đi và điểm đến
            List<Map<String, Object>> fromStops = stopService.findNearbyStopsFormatted(fromLat, fromLng, maxWalkDistance);
            List<Map<String, Object>> toStops = stopService.findNearbyStopsFormatted(toLat, toLng, maxWalkDistance);
            // 2. Tìm các tuyến đi qua cả từ điểm đi đến điểm đến
            List<Routes> directRoutes = findDirectRoutes(fromStops, toStops);

            // 3. Nếu maxTransfers > 0, tìm các tuyến cần chuyển
            List<List<Routes>> transferRoutes = new ArrayList<>();
            if (maxTransfers > 0) {
                transferRoutes = findTransferRoutes(fromStops, toStops, maxTransfers);
            }

            // 4. Định dạng kết quả
            result = formatRoutesResult(directRoutes, transferRoutes, fromStops, toStops,
                    fromLat, fromLng, toLat, toLng, routePriority);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<Routes> findRoutesByStops(List<Integer> stopIds) {
        if (stopIds == null || stopIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Triển khai tìm kiếm các tuyến đi qua các điểm dừng
        // Có thể cần thêm vào RoutesRepository nếu chưa có
        return new ArrayList<>(); // Trả về danh sách rỗng tạm thời
    }

// Tìm các tuyến đi trực tiếp (không cần chuyển tuyến)
    private List<Routes> findDirectRoutes(List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops) {
        List<Routes> directRoutes = new ArrayList<>();

        // Chuyển đổi danh sách Map thành danh sách ID của trạm
        List<Integer> fromStopIds = extractStopIds(fromStops);
        List<Integer> toStopIds = extractStopIds(toStops);

        // Lấy tất cả các tuyến
        List<Routes> allRoutes = getAllRoutes();

        // Tìm các tuyến đi qua cả điểm đầu và điểm cuối
        for (Routes route : allRoutes) {
            if (routeContainsStops(route, fromStopIds, toStopIds)) {
                directRoutes.add(route);
            }
        }

        return directRoutes;
    }

// Kiểm tra xem một tuyến có đi qua cả điểm đầu và điểm cuối không
    private boolean routeContainsStops(Routes route, List<Integer> fromStopIds, List<Integer> toStopIds) {
        // Lấy tất cả các ID trạm của tuyến này
        // Đây là phần giả định, thực tế cần triển khai theo cấu trúc dữ liệu của bạn
        List<Integer> routeStopIds = new ArrayList<>();

        // Kiểm tra xem tuyến có chứa ít nhất một điểm đầu và một điểm cuối không
        boolean containsFromStop = false;
        boolean containsToStop = false;

        for (Integer routeStopId : routeStopIds) {
            if (fromStopIds.contains(routeStopId)) {
                containsFromStop = true;
            }

            if (toStopIds.contains(routeStopId)) {
                containsToStop = true;
            }

            if (containsFromStop && containsToStop) {
                return true;
            }
        }

        return false;
    }

// Trích xuất ID của các trạm từ danh sách Map
    private List<Integer> extractStopIds(List<Map<String, Object>> stops) {
        List<Integer> stopIds = new ArrayList<>();

        for (Map<String, Object> stop : stops) {
            if (stop.containsKey("id")) {
                Object idObj = stop.get("id");
                if (idObj instanceof Integer) {
                    stopIds.add((Integer) idObj);
                } else if (idObj instanceof String) {
                    try {
                        stopIds.add(Integer.parseInt((String) idObj));
                    } catch (NumberFormatException e) {
                        // Bỏ qua nếu không thể chuyển đổi
                    }
                }
            }
        }

        return stopIds;
    }

// Tìm các tuyến đường cần chuyển tuyến
    private List<List<Routes>> findTransferRoutes(List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops, int maxTransfers) {
        List<List<Routes>> transferRoutes = new ArrayList<>();

        if (maxTransfers <= 0) {
            return transferRoutes;
        }

        // Triển khai thuật toán tìm đường đi với chuyển tuyến
        // Đây là một bài toán phức tạp, bên dưới chỉ là ví dụ đơn giản
        // Danh sách tạm thời để kiểm tra
        List<Routes> transferRoute = new ArrayList<>();
        transferRoute.add(getAllRoutes().isEmpty() ? null : getAllRoutes().get(0));
        transferRoute.add(getAllRoutes().size() > 1 ? getAllRoutes().get(1) : null);

        if (transferRoute.get(0) != null && transferRoute.get(1) != null) {
            transferRoutes.add(transferRoute);
        }

        return transferRoutes;
    }

// Định dạng kết quả để trả về
    private List<Map<String, Object>> formatRoutesResult(List<Routes> directRoutes, List<List<Routes>> transferRoutes,
            List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops,
            double fromLat, double fromLng, double toLat, double toLng, String routePriority) {

        List<Map<String, Object>> result = new ArrayList<>();

        // Xử lý các tuyến đi trực tiếp
        int optionId = 1;
        for (Routes route : directRoutes) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", optionId++);
            option.put("name", "Tuyến " + route.getName());
            option.put("routeId", route.getId());
            option.put("totalTime", calculateEstimatedTime(route, fromStops, toStops));
            option.put("totalDistance", calculateEstimatedDistance(route, fromLat, fromLng, toLat, toLng));
            option.put("walkingDistance", calculateWalkingDistance(fromStops, toStops));
            option.put("transfers", 0);

            // Thông tin về hành trình
            List<Map<String, Object>> legs = createLegs(route, fromStops, toStops, fromLat, fromLng, toLat, toLng);
            option.put("legs", legs);

            result.add(option);
        }

        // Xử lý các tuyến cần chuyển tuyến
        for (List<Routes> transferRoute : transferRoutes) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", optionId++);

            StringBuilder nameBuilder = new StringBuilder("Tuyến ");
            for (int i = 0; i < transferRoute.size(); i++) {
                if (i > 0) {
                    nameBuilder.append(" → ");
                }
                nameBuilder.append(transferRoute.get(i).getName());
            }
            option.put("name", nameBuilder.toString());

            // Tính toán các thông số của hành trình chuyển tuyến
            int totalTime = 0;
            double totalDistance = 0;
            double walkingDistance = 0;

            for (Routes route : transferRoute) {
                totalTime += calculateEstimatedTime(route, fromStops, toStops);
                totalDistance += calculateEstimatedDistance(route, fromLat, fromLng, toLat, toLng) / transferRoute.size();
            }

            // Đặt các thông số
            option.put("totalTime", totalTime);
            option.put("totalDistance", totalDistance);
            option.put("walkingDistance", walkingDistance);
            option.put("transfers", transferRoute.size() - 1);

            // Thông tin về hành trình - đây là một ví dụ đơn giản
            List<Map<String, Object>> legs = new ArrayList<>();
            option.put("legs", legs);

            result.add(option);
        }

        // Sắp xếp kết quả theo ưu tiên
        return sortRouteOptions(result, routePriority);
    }

// Sắp xếp các lựa chọn tuyến đường theo ưu tiên
    private List<Map<String, Object>> sortRouteOptions(List<Map<String, Object>> options, String routePriority) {
        if (options == null || options.isEmpty()) {
            return options;
        }

        Comparator<Map<String, Object>> comparator;

        if ("LEAST_TIME".equals(routePriority)) {
            comparator = Comparator.comparingInt(o -> ((Number) o.getOrDefault("totalTime", Integer.MAX_VALUE)).intValue());
        } else if ("LEAST_DISTANCE".equals(routePriority)) {
            comparator = Comparator.comparingDouble(o -> ((Number) o.getOrDefault("totalDistance", Double.MAX_VALUE)).doubleValue());
        } else if ("LEAST_TRANSFERS".equals(routePriority)) {
            comparator = Comparator.comparingInt(o -> ((Number) o.getOrDefault("transfers", Integer.MAX_VALUE)).intValue());
        } else {
            // Mặc định sắp xếp theo thời gian
            comparator = Comparator.comparingInt(o -> ((Number) o.getOrDefault("totalTime", Integer.MAX_VALUE)).intValue());
        }

        options.sort(comparator);
        return options;
    }

// Tính toán thời gian ước tính
    private int calculateEstimatedTime(Routes route, List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops) {
        // Triển khai tính toán thời gian ước tính
        // Đây là một tính toán đơn giản
        return 30 + (int) (Math.random() * 60); // 30-90 phút
    }

// Tính toán khoảng cách ước tính
    private double calculateEstimatedDistance(Routes route, double fromLat, double fromLng, double toLat, double toLng) {
        // Tính khoảng cách giữa hai điểm (km) - công thức Haversine
        return calculateHaversineDistance(fromLat, fromLng, toLat, toLng);
    }

// Tính toán khoảng cách đi bộ
    private double calculateWalkingDistance(List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops) {
        // Triển khai tính toán khoảng cách đi bộ
        // Đây là một giá trị giả định
        return 250 + Math.random() * 750; // 250-1000m
    }

// Tạo các chặng đường đi
    private List<Map<String, Object>> createLegs(Routes route, List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops,
            double fromLat, double fromLng, double toLat, double toLng) {

        List<Map<String, Object>> legs = new ArrayList<>();

        // Chặng đi từ vị trí người dùng đến trạm đầu
        Map<String, Object> firstLeg = new HashMap<>();
        firstLeg.put("type", "WALK");
        firstLeg.put("distance", 250 + Math.random() * 250); // 250-500m
        firstLeg.put("duration", 3 + (int) (Math.random() * 7)); // 3-10 phút
        firstLeg.put("from", Map.of("lat", fromLat, "lng", fromLng, "name", "Vị trí của bạn"));

        // Lấy thông tin trạm gần nhất từ điểm đầu
        Map<String, Object> nearestFromStop = findNearestStop(fromStops, fromLat, fromLng);
        firstLeg.put("to", nearestFromStop);
        legs.add(firstLeg);

        // Chặng đi bằng phương tiện công cộng
        Map<String, Object> busLeg = new HashMap<>();
        busLeg.put("type", "BUS");
        busLeg.put("routeId", route.getId());
        busLeg.put("routeName", route.getName());
        busLeg.put("routeColor", route.getRouteColor() != null ? route.getRouteColor() : "#4CAF50");
        busLeg.put("distance", calculateEstimatedDistance(route, fromLat, fromLng, toLat, toLng) * 1000); // m
        busLeg.put("duration", calculateEstimatedTime(route, fromStops, toStops));
        busLeg.put("from", nearestFromStop);

        // Lấy thông tin trạm gần nhất từ điểm cuối
        Map<String, Object> nearestToStop = findNearestStop(toStops, toLat, toLng);
        busLeg.put("to", nearestToStop);
        legs.add(busLeg);

        // Chặng đi từ trạm cuối đến vị trí đích
        Map<String, Object> lastLeg = new HashMap<>();
        lastLeg.put("type", "WALK");
        lastLeg.put("distance", 250 + Math.random() * 250); // 250-500m
        lastLeg.put("duration", 3 + (int) (Math.random() * 7)); // 3-10 phút
        lastLeg.put("from", nearestToStop);
        lastLeg.put("to", Map.of("lat", toLat, "lng", toLng, "name", "Điểm đến của bạn"));
        legs.add(lastLeg);

        return legs;
    }

// Tìm trạm gần nhất
    private Map<String, Object> findNearestStop(List<Map<String, Object>> stops, double lat, double lng) {
        if (stops == null || stops.isEmpty()) {
            return Map.of(
                    "id", 0,
                    "name", "Trạm không xác định",
                    "lat", lat,
                    "lng", lng
            );
        }

        Map<String, Object> nearestStop = stops.get(0);
        double minDistance = Double.MAX_VALUE;

        for (Map<String, Object> stop : stops) {
            Double stopLat = getDoubleValue(stop, "latitude", "lat");
            Double stopLng = getDoubleValue(stop, "longitude", "lng");

            if (stopLat != null && stopLng != null) {
                double distance = calculateHaversineDistance(lat, lng, stopLat, stopLng);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestStop = stop;
                }
            }
        }

        // Chuẩn hóa định dạng trạm
        Map<String, Object> standardStop = new HashMap<>();
        standardStop.put("id", nearestStop.getOrDefault("id", 0));
        standardStop.put("name", nearestStop.getOrDefault("name", nearestStop.getOrDefault("stop_name", "Trạm không xác định")));
        standardStop.put("lat", getDoubleValue(nearestStop, "latitude", "lat"));
        standardStop.put("lng", getDoubleValue(nearestStop, "longitude", "lng"));

        return standardStop;
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

// Lấy giá trị double từ Map với các key thay thế
    private Double getDoubleValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                Object value = map.get(key);
                if (value instanceof Double) {
                    return (Double) value;
                } else if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else if (value instanceof String) {
                    try {
                        return Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        // Bỏ qua lỗi và thử key tiếp theo
                    }
                }
            }
        }
        return null;
    }
}
