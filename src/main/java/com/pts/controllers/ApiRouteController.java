package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.pojo.RouteTypes;
import com.pts.pojo.Schedules;
import com.pts.services.RouteService;
import com.pts.services.ScheduleService;
import com.pts.services.StopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin
public class ApiRouteController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private StopService stopService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getRoutes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer typeId) {

        List<Routes> routes;

        // Lọc theo từ khóa nếu có
        if (keyword != null && !keyword.isEmpty()) {
            routes = routeService.searchRoutesByName(keyword);
        } else {
            routes = routeService.getAllRoutes();
        }

        // Lọc theo loại tuyến nếu có
        if (typeId != null) {
            routes = routes.stream()
                    .filter(route -> route.getRouteType() != null
                    && route.getRouteType().getId().equals(typeId))
                    .collect(Collectors.toList());
        }

        // Định dạng thời gian
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        // Chuyển đổi thành response format
        List<Map<String, Object>> result = new ArrayList<>();
        for (Routes route : routes) {
            Map<String, Object> routeInfo = new HashMap<>();

            // Thông tin cơ bản
            routeInfo.put("id", route.getId());
            routeInfo.put("name", route.getName());

            // Điểm đầu - điểm cuối
            routeInfo.put("route", route.getStartLocation() + " - " + route.getEndLocation());

            // Lấy thông tin về loại tuyến và icon
            RouteTypes routeType = route.getRouteType();
            if (routeType != null) {
                // Lấy icon URL từ RouteTypes nếu có
                String iconUrl = routeType.getIconUrl();
                routeInfo.put("icon", iconUrl != null && !iconUrl.isEmpty()
                        ? iconUrl : getDefaultIcon(routeType.getTypeName()));

                // Lấy mã màu từ RouteTypes nếu có
                String colorCode = routeType.getColorCode();
                routeInfo.put("color", colorCode != null && !colorCode.isEmpty()
                        ? colorCode : "#007bff"); // màu mặc định
            } else {
                // Giá trị mặc định nếu không có RouteTypes
                routeInfo.put("icon", "https://cdn-icons-png.flaticon.com/512/2554/2554642.png");
                routeInfo.put("color", "#007bff");
            }

            // Thời gian hoạt động
            if (route.getOperationStartTime() != null && route.getOperationEndTime() != null) {
                // Sử dụng thời gian hoạt động từ entity
                routeInfo.put("operatingHours",
                        timeFormat.format(route.getOperationStartTime()) + " - "
                        + timeFormat.format(route.getOperationEndTime()));
            } else {
                // Hoặc lấy từ lịch trình nếu không có
                routeInfo.put("operatingHours", getRouteOperatingHours(route.getId(), timeFormat));
            }

            result.add(routeInfo);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<?> findRoutes(
            @RequestParam("fromLat") double fromLat,
            @RequestParam("fromLng") double fromLng,
            @RequestParam("toLat") double toLat,
            @RequestParam("toLng") double toLng,
            @RequestParam(required = false, defaultValue = "1000") Double maxWalkDistance,
            @RequestParam(required = false, defaultValue = "2") Integer maxTransfers,
            @RequestParam(required = false, defaultValue = "LEAST_TIME") String routePriority) {

        try {
            // 1. Tạo response cho kết quả tìm đường
            double effectiveMaxDistance = Math.min(maxWalkDistance, 1000);

            // 2. Tìm các trạm gần điểm đi và điểm đến
            List<Map<String, Object>> nearbyStopsFromOrigin = findNearbyStops(fromLat, fromLng, effectiveMaxDistance);
            List<Map<String, Object>> nearbyStopsToDestination = findNearbyStops(toLat, toLng, effectiveMaxDistance);

            System.out.println("Tìm thấy " + nearbyStopsFromOrigin.size() + " trạm gần điểm đi");
            System.out.println("Tìm thấy " + nearbyStopsToDestination.size() + " trạm gần điểm đến");

            // Kiểm tra nếu không tìm thấy trạm nào gần điểm đi hoặc điểm đến
            if (nearbyStopsFromOrigin.isEmpty() || nearbyStopsToDestination.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "NO_STOPS_FOUND");
                errorResponse.put("message", "Không tìm thấy trạm nào trong bán kính " + effectiveMaxDistance + "m từ điểm đi hoặc điểm đến");
                return ResponseEntity.ok(errorResponse);
            }

            // 3. Tìm các tuyến đi qua các trạm gần điểm đi và điểm đến
            List<Routes> possibleRoutes = findPossibleRoutes(nearbyStopsFromOrigin, nearbyStopsToDestination);

            // Kiểm tra nếu không tìm thấy tuyến nào đi qua cả hai điểm
            if (possibleRoutes.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "NO_ROUTES_FOUND");
                errorResponse.put("message", "Không tìm thấy tuyến nào đi qua cả điểm đi và điểm đến trong bán kính " + effectiveMaxDistance + "m");
                return ResponseEntity.ok(errorResponse);
            }

            // 4. Định dạng kết quả thành các lựa chọn tuyến đường
            List<Map<String, Object>> routeOptions = new ArrayList<>();
            int optionId = 1;
            for (Routes route : possibleRoutes) {
                Map<String, Object> routeOption = formatRouteOption(route,
                        nearbyStopsFromOrigin, nearbyStopsToDestination,
                        fromLat, fromLng, toLat, toLng, optionId++);
                routeOptions.add(routeOption);
            }

            // 5. Sắp xếp các lựa chọn tuyến theo ưu tiên (thời gian, quãng đường, số lần chuyển tuyến)
            routeOptions = sortRouteOptions(routeOptions, routePriority);

            return ResponseEntity.ok(routeOptions);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Có lỗi xảy ra khi tìm tuyến đường: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

// Các phương thức hỗ trợ
    private List<Map<String, Object>> findNearbyStops(double lat, double lng, double maxDistance) {
        // Giới hạn bán kính tìm kiếm là 1000m
        double effectiveMaxDistance = Math.min(maxDistance, 1000);

        // Sử dụng phương thức từ stopService
        return stopService.findNearbyStopsFormatted(lat, lng, effectiveMaxDistance);
    }

    private List<Routes> findPossibleRoutes(List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops) {
        // Kiểm tra nếu không tìm thấy trạm gần điểm đi hoặc điểm đến
        if (fromStops.isEmpty() || toStops.isEmpty()) {
            System.out.println("Không tìm thấy trạm nào gần điểm đi hoặc điểm đến");
            return new ArrayList<>(); // Trả về danh sách rỗng
        }

        // Lấy routeIds từ các trạm gần điểm đi
        Set<Integer> fromRouteIds = new HashSet<>();
        for (Map<String, Object> stop : fromStops) {
            if (stop.containsKey("routeId")) {
                fromRouteIds.add((Integer) stop.get("routeId"));
            }
        }

        // Lấy routeIds từ các trạm gần điểm đến
        Set<Integer> toRouteIds = new HashSet<>();
        for (Map<String, Object> stop : toStops) {
            if (stop.containsKey("routeId")) {
                toRouteIds.add((Integer) stop.get("routeId"));
            }
        }

        // Tìm giao của hai tập hợp routeIds
        fromRouteIds.retainAll(toRouteIds);
        System.out.println("Số tuyến đi qua cả hai điểm: " + fromRouteIds.size());

        // Lấy thông tin chi tiết của các tuyến
        List<Routes> possibleRoutes = new ArrayList<>();
        for (Integer routeId : fromRouteIds) {
            Optional<Routes> routeOpt = routeService.getRouteById(routeId);
            routeOpt.ifPresent(possibleRoutes::add);
        }

        return possibleRoutes;
    }

    private Map<String, Object> formatRouteOption(Routes route,
            List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops,
            double fromLat, double fromLng, double toLat, double toLng, int optionId) {

        Map<String, Object> option = new HashMap<>();
        option.put("id", optionId);
        option.put("totalTime", calculateEstimatedTime(route, fromStops, toStops));
        option.put("totalDistance", calculateEstimatedDistance(route, fromLat, fromLng, toLat, toLng));
        option.put("walkingDistance", calculateWalkingDistance(fromStops, toStops, fromLat, fromLng, toLat, toLng));
        option.put("transfers", 0); // Giả định không có chuyển tuyến

        // Thông tin tuyến
        List<Map<String, Object>> routes = new ArrayList<>();
        Map<String, Object> routeInfo = new HashMap<>();
        routeInfo.put("number", route.getId().toString());
        routeInfo.put("name", route.getName());
        routeInfo.put("color", route.getRouteColor() != null ? route.getRouteColor() : "#4CAF50");
        routes.add(routeInfo);
        option.put("routes", routes);

        // Thông tin các chặng đi
        List<Map<String, Object>> legs = createLegs(route, fromStops, toStops, fromLat, fromLng, toLat, toLng);
        option.put("legs", legs);

        return option;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRouteById(@PathVariable Integer id) {
        Optional<Routes> routeOpt = routeService.getRouteById(id);

        if (!routeOpt.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không tìm thấy tuyến với ID " + id);
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        Routes route = routeOpt.get();

        // Định dạng thời gian
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        Map<String, Object> routeInfo = new HashMap<>();

        // Thông tin cơ bản
        routeInfo.put("id", route.getId());
        routeInfo.put("name", route.getName());

        // Điểm đầu - điểm cuối
        routeInfo.put("route", route.getStartLocation() + " - " + route.getEndLocation());

        // Lấy thông tin về loại tuyến và icon
        RouteTypes routeType = route.getRouteType();
        if (routeType != null) {
            // Thông tin từ RouteTypes
            routeInfo.put("routeTypeId", routeType.getId());
            routeInfo.put("routeTypeName", routeType.getTypeName());

            // Lấy icon URL từ RouteTypes nếu có
            String iconUrl = routeType.getIconUrl();
            routeInfo.put("icon", iconUrl != null && !iconUrl.isEmpty()
                    ? iconUrl : getDefaultIcon(routeType.getTypeName()));

            // Lấy mã màu từ RouteTypes nếu có
            String colorCode = routeType.getColorCode();
            routeInfo.put("color", colorCode != null && !colorCode.isEmpty()
                    ? colorCode : route.getRouteColor()); // sử dụng màu từ Routes nếu không có trong RouteTypes
        } else {
            // Giá trị mặc định nếu không có RouteTypes
            routeInfo.put("icon", "https://cdn-icons-png.flaticon.com/512/2554/2554642.png");
            routeInfo.put("color", route.getRouteColor() != null ? route.getRouteColor() : "#007bff");
        }

        // Thời gian hoạt động
        if (route.getOperationStartTime() != null && route.getOperationEndTime() != null) {
            // Sử dụng thời gian hoạt động từ entity
            routeInfo.put("operatingHours",
                    timeFormat.format(route.getOperationStartTime()) + " - "
                    + timeFormat.format(route.getOperationEndTime()));
        } else {
            // Hoặc lấy từ lịch trình nếu không có
            routeInfo.put("operatingHours", getRouteOperatingHours(route.getId(), timeFormat));
        }

        // Thông tin thêm về tuyến
        routeInfo.put("totalStops", route.getTotalStops());
        routeInfo.put("frequencyMinutes", route.getFrequencyMinutes());
        routeInfo.put("isWalkingRoute", route.getIsWalkingRoute());
        routeInfo.put("active", route.getActive());

        return new ResponseEntity<>(routeInfo, HttpStatus.OK);
    }

    /**
     * Lấy thông tin thời gian hoạt động của tuyến từ lịch trình
     *
     * @param routeId ID của tuyến
     * @param timeFormat Định dạng thời gian
     * @return Chuỗi thời gian hoạt động
     */
    private String getRouteOperatingHours(Integer routeId, SimpleDateFormat timeFormat) {
        List<Schedules> schedules = scheduleService.findSchedulesByRouteId(routeId);

        if (schedules == null || schedules.isEmpty()) {
            return "Chưa có thông tin";
        }

        // Tìm thời gian sớm nhất và muộn nhất
        Date earliestDeparture = null;
        Date latestArrival = null;

        for (Schedules schedule : schedules) {
            if (schedule.getDepartureTime() != null) {
                if (earliestDeparture == null || schedule.getDepartureTime().before(earliestDeparture)) {
                    earliestDeparture = schedule.getDepartureTime();
                }
            }

            if (schedule.getArrivalTime() != null) {
                if (latestArrival == null || schedule.getArrivalTime().after(latestArrival)) {
                    latestArrival = schedule.getArrivalTime();
                }
            }
        }

        if (earliestDeparture != null && latestArrival != null) {
            return timeFormat.format(earliestDeparture) + " - " + timeFormat.format(latestArrival);
        }

        return "Chưa có thông tin";
    }

    private String getDefaultIcon(String typeName) {
        if (typeName == null) {
            return "https://cdn-icons-png.flaticon.com/512/2554/2554642.png"; // icon bus mặc định
        }

        String typeNameLower = typeName.toLowerCase();

        if (typeNameLower.contains("metro") || typeNameLower.contains("tàu điện ngầm")) {
            return "https://cdn-icons-png.flaticon.com/512/3448/3448305.png";
        } else if (typeNameLower.contains("bus") || typeNameLower.contains("buýt")) {
            return "https://cdn-icons-png.flaticon.com/512/2554/2554642.png";
        } else if (typeNameLower.contains("tàu điện") || typeNameLower.contains("tramway")) {
            return "https://cdn-icons-png.flaticon.com/512/3011/3011570.png";
        } else if (typeNameLower.contains("phà") || typeNameLower.contains("ferry")) {
            return "https://cdn-icons-png.flaticon.com/512/2956/2956744.png";
        }

        return "https://cdn-icons-png.flaticon.com/512/2554/2554642.png"; // icon bus mặc định
    }

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
