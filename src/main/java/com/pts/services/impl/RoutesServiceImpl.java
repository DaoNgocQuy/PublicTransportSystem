package com.pts.services.impl;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import com.pts.pojo.RouteStop;
import com.pts.repositories.RoutesRepository;
import com.pts.repositories.RouteStopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.pts.services.RouteService;
import com.pts.services.StopService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class RoutesServiceImpl implements RouteService {

    @Autowired
    private RoutesRepository routesRepository;

    @Autowired
    private StopService stopService;

    @Autowired
    private RouteStopRepository routeStopRepository;

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

        // Lưu route
        Routes savedRoute = routesRepository.save(route);

        // Cập nhật tổng số điểm dừng
        if (savedRoute.getId() != null) {
            routesRepository.updateTotalStops(savedRoute.getId());
        }

        return savedRoute;
    }

    @Override
    public void deleteRoute(Integer id) {
        // Xóa các liên kết trong bảng route_stops trước
        routeStopRepository.deleteByRouteId(id);

        // Sau đó xóa tuyến
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
    public List<Routes> findRoutesByStops(List<Integer> stopIds) {
        if (stopIds == null || stopIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Lấy tất cả các tuyến
        List<Routes> allRoutes = getAllRoutes();

        // Lọc các tuyến đi qua tất cả điểm dừng được chỉ định
        return allRoutes.stream()
                .filter(route -> {
                    // Lấy tất cả các điểm dừng của tuyến này
                    List<Stops> routeStops = routesRepository.findStopsByRouteId(route.getId());
                    List<Integer> routeStopIds = routeStops.stream()
                            .map(Stops::getId)
                            .collect(Collectors.toList());

                    // Kiểm tra xem tuyến có chứa tất cả các điểm dừng không
                    return routeStopIds.containsAll(stopIds);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Routes> findRoutesByStopAndDirection(Integer stopId, Integer direction) {
        return routesRepository.findByStopIdAndDirection(stopId, direction);
    }

    @Override
    public List<Stops> getStopsByRouteId(Integer routeId) {
        return routesRepository.findStopsByRouteId(routeId);
    }

    @Override
    public List<Stops> getStopsByRouteIdAndDirection(Integer routeId, Integer direction) {
        return routesRepository.findStopsByRouteIdAndDirection(routeId, direction);
    }

    @Override
    public List<Map<String, Object>> findRoutesWithStops(double fromLat, double fromLng,
            double toLat, double toLng, double maxWalkDistance,
            int maxTransfers, String routePriority) {

        List<Map<String, Object>> result = new ArrayList<>();

        try {
            double effectiveMaxDistance = Math.min(maxWalkDistance, 1000);

            // 1. Tìm trạm gần điểm đi và điểm đến
            List<Map<String, Object>> fromStops = stopService.findNearbyStopsFormatted(fromLat, fromLng, effectiveMaxDistance);
            List<Map<String, Object>> toStops = stopService.findNearbyStopsFormatted(toLat, toLng, effectiveMaxDistance);

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

    // Tìm các tuyến đi trực tiếp (không cần chuyển tuyến)
    private List<Routes> findDirectRoutes(List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops) {
        List<Routes> directRoutes = new ArrayList<>();
        Set<Integer> fromStopIds = extractStopIds(fromStops);
        Set<Integer> toStopIds = extractStopIds(toStops);

        if (fromStopIds.isEmpty() || toStopIds.isEmpty()) {
            return directRoutes;
        }

        // Lấy tất cả các tuyến đi qua điểm dừng đầu tiên
        Set<Routes> potentialRoutes = new HashSet<>();
        for (Integer stopId : fromStopIds) {
            List<Routes> routes = routesRepository.findByStopId(stopId);
            potentialRoutes.addAll(routes);
        }

        // Kiểm tra xem những tuyến này có đi qua điểm dừng cuối cùng không
        for (Routes route : potentialRoutes) {
            List<Stops> routeStops = routesRepository.findStopsByRouteId(route.getId());
            Set<Integer> routeStopIds = routeStops.stream()
                    .map(Stops::getId)
                    .collect(Collectors.toSet());

            boolean hasToStop = false;
            for (Integer toId : toStopIds) {
                if (routeStopIds.contains(toId)) {
                    hasToStop = true;
                    break;
                }
            }

            if (hasToStop) {
                directRoutes.add(route);
            }
        }

        return directRoutes;
    }

    // Trích xuất ID của các trạm từ danh sách Map
    private Set<Integer> extractStopIds(List<Map<String, Object>> stops) {
        Set<Integer> stopIds = new HashSet<>();

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
        Set<Integer> fromStopIds = extractStopIds(fromStops);
        Set<Integer> toStopIds = extractStopIds(toStops);

        if (maxTransfers <= 0 || fromStopIds.isEmpty() || toStopIds.isEmpty()) {
            return transferRoutes;
        }

        // Tìm tất cả các tuyến đi qua các điểm xuất phát
        Set<Routes> startRoutes = new HashSet<>();
        for (Integer stopId : fromStopIds) {
            startRoutes.addAll(routesRepository.findByStopId(stopId));
        }

        // Tìm tất cả các tuyến đi qua các điểm đích
        Set<Routes> endRoutes = new HashSet<>();
        for (Integer stopId : toStopIds) {
            endRoutes.addAll(routesRepository.findByStopId(stopId));
        }

        // Nếu có ít nhất 1 tuyến chung giữa điểm xuất phát và điểm đích, thì đó là tuyến trực tiếp
        // và đã được xử lý trong findDirectRoutes, không cần xử lý ở đây nữa.
        startRoutes.removeAll(endRoutes);

        // Tìm các tuyến có điểm chuyển tiếp (maxTransfers = 1)
        for (Routes startRoute : startRoutes) {
            // Lấy danh sách tất cả các điểm dừng của tuyến xuất phát
            List<Stops> startRouteStops = routesRepository.findStopsByRouteId(startRoute.getId());

            // Với mỗi điểm dừng của tuyến xuất phát, kiểm tra xem có tuyến nào đi từ điểm này đến điểm đích không
            for (Stops transferStop : startRouteStops) {
                List<Routes> transferRoutesList = routesRepository.findByStopId(transferStop.getId());

                for (Routes transferRoute : transferRoutesList) {
                    // Loại bỏ tuyến xuất phát để tránh trùng lặp
                    if (transferRoute.getId().equals(startRoute.getId())) {
                        continue;
                    }

                    // Kiểm tra xem tuyến chuyển tiếp có đi qua điểm đích không
                    List<Stops> transferRouteStops = routesRepository.findStopsByRouteId(transferRoute.getId());
                    Set<Integer> transferRouteStopIds = transferRouteStops.stream()
                            .map(Stops::getId)
                            .collect(Collectors.toSet());

                    boolean hasEndStop = false;
                    for (Integer endStopId : toStopIds) {
                        if (transferRouteStopIds.contains(endStopId)) {
                            hasEndStop = true;
                            break;
                        }
                    }

                    if (hasEndStop) {
                        // Tìm thấy một chuỗi tuyến hợp lệ với 1 lần chuyển tiếp
                        List<Routes> routeSequence = new ArrayList<>();
                        routeSequence.add(startRoute);
                        routeSequence.add(transferRoute);
                        transferRoutes.add(routeSequence);
                    }
                }
            }
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
            option.put("totalDistance", calculateEstimatedDistance(route, fromLat, fromLng, toLat, toLng) * 1000); // Convert to meters
            option.put("walkingDistance", calculateWalkingDistance(fromStops, toStops, fromLat, fromLng, toLat, toLng));
            option.put("transfers", 0);

            // Thông tin về hành trình
            List<Map<String, Object>> legs = createLegs(route, fromStops, toStops, fromLat, fromLng, toLat, toLng);
            option.put("legs", legs);

            // Thêm thông tin routes để hiển thị
            List<Map<String, Object>> routes = new ArrayList<>();
            Map<String, Object> routeInfo = new HashMap<>();
            routeInfo.put("number", route.getId().toString());
            routeInfo.put("name", route.getName());
            routeInfo.put("color", route.getRouteColor() != null ? route.getRouteColor() : "#4CAF50");
            routes.add(routeInfo);
            option.put("routes", routes);

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
        // Tìm trạm thuộc tuyến này ở cả hai điểm
        Map<String, Object> fromStop = findStopOnRoute(fromStops, route.getId());
        Map<String, Object> toStop = findStopOnRoute(toStops, route.getId());

        if (fromStop == null || toStop == null) {
            return 30; // Giá trị mặc định
        }

        // Nếu có thông tin stop_order, dùng nó để tính thời gian
        Integer fromOrder = (Integer) fromStop.getOrDefault("stopOrder", 0);
        Integer toOrder = (Integer) toStop.getOrDefault("stopOrder", 0);

        // Tính số trạm dừng giữa hai điểm
        int stopsBetween = Math.abs(toOrder - fromOrder);

        // Ước tính thời gian: 2 phút/trạm + 5 phút cơ bản
        return stopsBetween * 2 + 5;
    }

    private Map<String, Object> findStopOnRoute(List<Map<String, Object>> stops, Integer routeId) {
        // Cập nhật để tìm stop dựa vào các routes đi qua nó (từ cấu trúc mới của vị trí dừng)
        for (Map<String, Object> stop : stops) {
            if (stop.containsKey("routes")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> routes = (List<Map<String, Object>>) stop.get("routes");
                for (Map<String, Object> route : routes) {
                    if (route.containsKey("id") && routeId.equals(route.get("id"))) {
                        return stop;
                    }
                }
            }
        }
        return null;
    }

    // Tính toán khoảng cách ước tính
    private double calculateEstimatedDistance(Routes route, double fromLat, double fromLng, double toLat, double toLng) {
        // Tính khoảng cách giữa hai điểm (km) - công thức Haversine
        return calculateHaversineDistance(fromLat, fromLng, toLat, toLng);
    }

    // Tính toán khoảng cách đi bộ
    private double calculateWalkingDistance(List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops,
            double fromLat, double fromLng, double toLat, double toLng) {

        // Tính khoảng cách từ điểm đi đến trạm gần nhất
        Map<String, Object> nearestFromStop = findNearestStop(fromStops, fromLat, fromLng);
        double distanceToFirstStop = 0;

        if (nearestFromStop != null) {
            Double stopLat = getDoubleValue(nearestFromStop, "latitude", "lat");
            Double stopLng = getDoubleValue(nearestFromStop, "longitude", "lng");

            if (stopLat != null && stopLng != null) {
                distanceToFirstStop = calculateHaversineDistance(fromLat, fromLng, stopLat, stopLng) * 1000; // Chuyển km -> m
            }
        }

        // Tính khoảng cách từ trạm gần nhất đến điểm đến
        Map<String, Object> nearestToStop = findNearestStop(toStops, toLat, toLng);
        double distanceFromLastStop = 0;

        if (nearestToStop != null) {
            Double stopLat = getDoubleValue(nearestToStop, "latitude", "lat");
            Double stopLng = getDoubleValue(nearestToStop, "longitude", "lng");

            if (stopLat != null && stopLng != null) {
                distanceFromLastStop = calculateHaversineDistance(stopLat, stopLng, toLat, toLng) * 1000; // Chuyển km -> m
            }
        }

        return distanceToFirstStop + distanceFromLastStop;
    }

    // Tạo các chặng đường đi
    private List<Map<String, Object>> createLegs(Routes route, List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops,
            double fromLat, double fromLng, double toLat, double toLng) {

        List<Map<String, Object>> legs = new ArrayList<>();

        // Lấy thông tin trạm gần nhất từ điểm đầu
        Map<String, Object> nearestFromStop = findNearestStop(fromStops, fromLat, fromLng);

        // Tính khoảng cách và thời gian đi bộ thực tế từ điểm đi đến trạm
        double walkToStopDistance = 0;
        int walkToStopDuration = 0;

        if (nearestFromStop != null) {
            Double stopLat = getDoubleValue(nearestFromStop, "latitude", "lat");
            Double stopLng = getDoubleValue(nearestFromStop, "longitude", "lng");

            if (stopLat != null && stopLng != null) {
                // Tính khoảng cách thực tế
                walkToStopDistance = calculateHaversineDistance(fromLat, fromLng, stopLat, stopLng) * 1000; // m
                // Tính thời gian đi bộ (giả sử tốc độ trung bình 80m/phút)
                walkToStopDuration = (int) Math.ceil(walkToStopDistance / 80);
            }
        }

        // Chặng đi từ vị trí người dùng đến trạm đầu
        Map<String, Object> firstLeg = new HashMap<>();
        firstLeg.put("type", "WALK");
        firstLeg.put("distance", walkToStopDistance); // Khoảng cách thực tế
        firstLeg.put("duration", walkToStopDuration); // Thời gian thực tế
        firstLeg.put("from", Map.of("lat", fromLat, "lng", fromLng, "name", "Vị trí của bạn"));
        firstLeg.put("to", nearestFromStop);
        legs.add(firstLeg);

        // Chặng đi bằng phương tiện công cộng
        Map<String, Object> busLeg = new HashMap<>();
        busLeg.put("type", "BUS");
        busLeg.put("routeId", route.getId());
        busLeg.put("routeNumber", route.getId().toString()); // Thêm routeNumber
        busLeg.put("routeName", route.getName());
        busLeg.put("routeColor", route.getRouteColor() != null ? route.getRouteColor() : "#4CAF50");
        busLeg.put("distance", calculateEstimatedDistance(route, fromLat, fromLng, toLat, toLng) * 1000); // m
        busLeg.put("duration", calculateEstimatedTime(route, fromStops, toStops));
        busLeg.put("from", nearestFromStop);

        // Lấy thông tin trạm gần nhất từ điểm cuối
        Map<String, Object> nearestToStop = findNearestStop(toStops, toLat, toLng);
        busLeg.put("to", nearestToStop);
        legs.add(busLeg);

        // Tính khoảng cách và thời gian đi bộ thực tế từ trạm đến điểm đến
        double walkFromStopDistance = 0;
        int walkFromStopDuration = 0;

        if (nearestToStop != null) {
            Double stopLat = getDoubleValue(nearestToStop, "latitude", "lat");
            Double stopLng = getDoubleValue(nearestToStop, "longitude", "lng");

            if (stopLat != null && stopLng != null) {
                // Tính khoảng cách thực tế
                walkFromStopDistance = calculateHaversineDistance(stopLat, stopLng, toLat, toLng) * 1000; // m
                // Tính thời gian đi bộ (giả sử tốc độ trung bình 80m/phút)
                walkFromStopDuration = (int) Math.ceil(walkFromStopDistance / 80);
            }
        }

        // Chặng đi từ trạm cuối đến vị trí đích
        Map<String, Object> lastLeg = new HashMap<>();
        lastLeg.put("type", "WALK");
        lastLeg.put("distance", walkFromStopDistance); // Khoảng cách thực tế
        lastLeg.put("duration", walkFromStopDuration); // Thời gian thực tế
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
