package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.pojo.RouteStop;
import com.pts.pojo.RouteTypes;
import com.pts.pojo.Schedules;
import com.pts.pojo.Stops;
import com.pts.services.RouteService;
import com.pts.services.RouteStopService;
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

    @Autowired
    private RouteStopService routeStopService;

    /**
     * Lấy danh sách tất cả các tuyến Hỗ trợ tìm kiếm theo tên và loại tuyến
     */
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

        // Format định dạng thời gian
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        // Chuyển đổi sang định dạng JSON cho frontend
        List<Map<String, Object>> result = new ArrayList<>();
        for (Routes route : routes) {
            Map<String, Object> routeInfo = new HashMap<>();

            // Thông tin cơ bản
            routeInfo.put("id", route.getId());
            routeInfo.put("name", route.getName());
            routeInfo.put("route", route.getStartLocation() + " - " + route.getEndLocation());

            // Thông tin về loại tuyến và biểu tượng
            RouteTypes routeType = route.getRouteType();
            if (routeType != null) {
                // Lấy URL biểu tượng từ RouteTypes
                String iconUrl = routeType.getIconUrl();
                routeInfo.put("icon", iconUrl != null && !iconUrl.isEmpty()
                        ? iconUrl
                        : getDefaultIcon(routeType.getTypeName()));

                // Lấy mã màu từ RouteTypes
                String colorCode = routeType.getColorCode();
                routeInfo.put("color", colorCode != null && !colorCode.isEmpty()
                        ? colorCode
                        : "#007bff"); // màu mặc định
            } else {
                // Giá trị mặc định nếu không có thông tin RouteTypes
                routeInfo.put("icon", "https://cdn-icons-png.flaticon.com/512/2554/2554642.png");
                routeInfo.put("color", "#007bff");
            }

            // Thời gian hoạt động
            if (route.getOperationStartTime() != null && route.getOperationEndTime() != null) {
                routeInfo.put("operatingHours",
                        timeFormat.format(route.getOperationStartTime()) + " - "
                        + timeFormat.format(route.getOperationEndTime()));
            } else {
                routeInfo.put("operatingHours", getRouteOperatingHours(route.getId(), timeFormat));
            }

            result.add(routeInfo);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Lấy chi tiết một tuyến theo ID Bao gồm cả các trạm dừng theo hai chiều
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRouteById(@PathVariable Integer id) {
        Optional<Routes> routeOpt = routeService.getRouteById(id);

        if (!routeOpt.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không tìm thấy tuyến với ID " + id);
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        Routes route = routeOpt.get();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Map<String, Object> routeInfo = new HashMap<>();

        // Thông tin cơ bản
        routeInfo.put("id", route.getId());
        routeInfo.put("name", route.getName());
        routeInfo.put("route", route.getStartLocation() + " - " + route.getEndLocation());

        RouteTypes routeType = route.getRouteType();
        if (routeType != null) {

            routeInfo.put("routeTypeId", routeType.getId());
            routeInfo.put("routeTypeName", routeType.getTypeName());

            String iconUrl = routeType.getIconUrl();
            routeInfo.put("icon", iconUrl != null && !iconUrl.isEmpty()
                    ? iconUrl
                    : getDefaultIcon(routeType.getTypeName()));

            String colorCode = routeType.getColorCode();
            routeInfo.put("color", colorCode != null && !colorCode.isEmpty()
                    ? colorCode
                    : "#007bff");
        } else {
            routeInfo.put("icon", "https://cdn-icons-png.flaticon.com/512/2554/2554642.png");
            routeInfo.put("color", "#007bff");
        }

        // Thời gian hoạt động
        if (route.getOperationStartTime() != null && route.getOperationEndTime() != null) {
            routeInfo.put("operatingHours",
                    timeFormat.format(route.getOperationStartTime()) + " - "
                    + timeFormat.format(route.getOperationEndTime()));
        } else {
            routeInfo.put("operatingHours", getRouteOperatingHours(route.getId(), timeFormat));
        }

        // Thông tin bổ sung về tuyến
        routeInfo.put("totalStops", route.getTotalStops());
        routeInfo.put("frequencyMinutes", route.getFrequencyMinutes());
        routeInfo.put("isWalkingRoute", route.getIsWalkingRoute());
        routeInfo.put("active", route.getActive());

        // Lấy danh sách trạm dừng cho cả hai chiều
        List<Map<String, Object>> stopsInbound = getFormattedStops(route.getId(), 1);
        List<Map<String, Object>> stopsOutbound = getFormattedStops(route.getId(), 2);

        routeInfo.put("stopsInbound", stopsInbound);
        routeInfo.put("stopsOutbound", stopsOutbound);

        return new ResponseEntity<>(routeInfo, HttpStatus.OK);
    }

    /**
     * Phương thức đơn giản trả về trạm gần đây - để sử dụng cho frontend
     */
    @GetMapping("/nearby-stops")
    public ResponseEntity<List<Map<String, Object>>> getNearbyStops(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") double radius) {

        List<Map<String, Object>> nearbyStops = stopService.findNearbyStopsFormatted(lat, lng, radius);
        return ResponseEntity.ok(nearbyStops);
    }

    /**
     * API tìm tuyến đơn giản - chỉ trả về các tuyến đi qua khu vực gần vị trí
     * người dùng Không tính toán chi tiết lộ trình
     */
    @GetMapping("/find-journey")
    public ResponseEntity<?> findJourney(
            @RequestParam Double fromLat,
            @RequestParam Double fromLng,
            @RequestParam Double toLat,
            @RequestParam Double toLng,
            @RequestParam(required = false, defaultValue = "500") Integer maxWalkDistance,
            @RequestParam(required = false, defaultValue = "LEAST_TIME") String priority) {

        try {
            Map<String, Object> result = routeService.findJourneyOptions(
                    fromLat, fromLng, toLat, toLng, maxWalkDistance, priority);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Lỗi khi tìm phương án di chuyển: " + e.getMessage()));
        }
    }

    @GetMapping("/simple-search")
    public ResponseEntity<?> findSimpleRoutes(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") double radius) {

        try {
            // Tìm các trạm gần vị trí
            List<Map<String, Object>> nearbyStops = stopService.findNearbyStopsFormatted(lat, lng, radius);

            if (nearbyStops.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "NO_STOPS_FOUND");
                error.put("message", "Không tìm thấy trạm nào trong bán kính " + radius + "m");
                return ResponseEntity.ok(error);
            }

            // Trích xuất ID các trạm
            List<Integer> stopIds = new ArrayList<>();
            for (Map<String, Object> stop : nearbyStops) {
                if (stop.containsKey("id")) {
                    Object idObj = stop.get("id");
                    if (idObj instanceof Integer) {
                        stopIds.add((Integer) idObj);
                    } else if (idObj instanceof String) {
                        try {
                            stopIds.add(Integer.parseInt((String) idObj));
                        } catch (NumberFormatException e) {
                            // Bỏ qua nếu không chuyển đổi được
                        }
                    }
                }
            }

            // Tìm các tuyến đi qua các trạm này
            List<Routes> routes = routeService.findRoutesByStops(stopIds);

            if (routes.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "NO_ROUTES_FOUND");
                error.put("message", "Không tìm thấy tuyến nào đi qua khu vực này");
                return ResponseEntity.ok(error);
            }

            // Format kết quả đơn giản
            List<Map<String, Object>> result = new ArrayList<>();
            for (Routes route : routes) {
                Map<String, Object> routeInfo = new HashMap<>();
                routeInfo.put("id", route.getId());
                routeInfo.put("name", route.getName());
                routeInfo.put("route", route.getStartLocation() + " - " + route.getEndLocation());

                // Thông tin về loại tuyến
                RouteTypes routeType = route.getRouteType();
                if (routeType != null) {
                    routeInfo.put("icon", routeType.getIconUrl());
                    routeInfo.put("color", routeType.getColorCode());
                }

                result.add(routeInfo);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Đã xảy ra lỗi khi tìm kiếm tuyến: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/walking-path")
    public ResponseEntity<?> getWalkingPath(
            @RequestParam Double fromLat,
            @RequestParam Double fromLng,
            @RequestParam Double toLat,
            @RequestParam Double toLng,
            @RequestParam(required = false) String pathType) {

        try {
            // Sử dụng các service đã có để tính toán đường đi bộ
            List<List<Double>> path = routeService.calculateOptimalWalkingPath(fromLat, fromLng, toLat, toLng);

            Map<String, Object> result = new HashMap<>();
            result.put("path", path);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Lỗi khi tính đường đi bộ: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách trạm dừng được định dạng cho một tuyến ở một hướng cụ thể
     */
    private List<Map<String, Object>> getFormattedStops(Integer routeId, Integer direction) {
        List<Stops> stops = stopService.findStopsByRouteIdAndDirection(routeId, direction);
        if (stops == null) {
            return new ArrayList<>();
        }

        // Lấy thông tin route_stops để biết thứ tự các trạm
        List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(routeId, direction);

        // Tạo map từ ID trạm đến thứ tự trạm
        Map<Integer, Integer> stopOrderMap = new HashMap<>();
        for (RouteStop rs : routeStops) {
            stopOrderMap.put(rs.getStop().getId(), rs.getStopOrder());
        }

        // Định dạng danh sách trạm với thứ tự
        List<Map<String, Object>> formattedStops = new ArrayList<>();
        for (Stops stop : stops) {
            Map<String, Object> stopMap = new HashMap<>();
            stopMap.put("id", stop.getId());
            stopMap.put("name", stop.getStopName());
            stopMap.put("lat", stop.getLatitude());
            stopMap.put("lng", stop.getLongitude());
            stopMap.put("address", stop.getAddress());
            stopMap.put("isAccessible", stop.getIsAccessible());

            // Thêm thứ tự trạm nếu có
            Integer order = stopOrderMap.get(stop.getId());
            if (order != null) {
                stopMap.put("stopOrder", order);
            }

            formattedStops.add(stopMap);
        }

        // Sắp xếp theo thứ tự trạm
        formattedStops.sort(Comparator.comparingInt(s -> (Integer) s.getOrDefault("stopOrder", Integer.MAX_VALUE)));

        return formattedStops;
    }

    /**
     * Lấy thời gian hoạt động của tuyến từ lịch trình
     */
    private String getRouteOperatingHours(Integer routeId, SimpleDateFormat timeFormat) {
        List<Schedules> schedules = scheduleService.findSchedulesByRouteId(routeId);

        if (schedules == null || schedules.isEmpty()) {
            return "Không có thông tin";
        }

        // Tìm thời gian khởi hành sớm nhất và thời gian đến muộn nhất
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

        return "Không có thông tin";
    }

    /**
     * Lấy biểu tượng mặc định dựa trên tên loại tuyến
     */
    private String getDefaultIcon(String typeName) {
        if (typeName == null) {
            return "https://cdn-icons-png.flaticon.com/512/2554/2554642.png"; // Biểu tượng xe buýt mặc định
        }

        String typeNameLower = typeName.toLowerCase();

        if (typeNameLower.contains("metro") || typeNameLower.contains("subway")) {
            return "https://cdn-icons-png.flaticon.com/512/3448/3448305.png";
        } else if (typeNameLower.contains("bus")) {
            return "https://cdn-icons-png.flaticon.com/512/2554/2554642.png";
        } else if (typeNameLower.contains("tram") || typeNameLower.contains("light rail")) {
            return "https://cdn-icons-png.flaticon.com/512/3011/3011570.png";
        } else if (typeNameLower.contains("ferry") || typeNameLower.contains("boat")) {
            return "https://cdn-icons-png.flaticon.com/512/2956/2956744.png";
        }

        return "https://cdn-icons-png.flaticon.com/512/2554/2554642.png"; // Biểu tượng xe buýt mặc định
    }

    // Phần Search đã được ẩn và thay thế bằng simple-search đơn giản hơn
    // Các phương thức tính toán phức tạp đã được loại bỏ
}
