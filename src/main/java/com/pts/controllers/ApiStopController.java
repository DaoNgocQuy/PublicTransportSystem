package com.pts.controllers;

import com.pts.pojo.Stops;
import com.pts.pojo.Routes;
import com.pts.pojo.RouteStop;
import com.pts.services.StopService;
import com.pts.services.RouteService;
import com.pts.services.RouteStopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stops")
@CrossOrigin // Cho phép React truy cập API
public class ApiStopController {

    @Autowired
    private StopService stopService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private RouteStopService routeStopService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getStops(
            @RequestParam(required = false) String keyword) {

        List<Stops> stops;
        if (keyword != null && !keyword.isEmpty()) {
            stops = stopService.searchStops(keyword);
        } else {
            stops = stopService.getAllStops();
        }

        List<Map<String, Object>> result = formatStopsList(stops);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStopById(@PathVariable Integer id) {
        Optional<Stops> stopOpt = stopService.getStopById(id);

        if (!stopOpt.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không tìm thấy điểm dừng với ID " + id);
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        Map<String, Object> response = formatStop(stopOpt.get());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<Map<String, Object>>> getStopsByRouteId(@PathVariable Integer routeId) {
        List<Stops> stops = stopService.findStopsByRouteId(routeId);

        // Lấy thông tin về thứ tự các điểm dừng trong tuyến
        List<RouteStop> routeStops = routeStopService.findByRouteId(routeId);

        // Tạo map ánh xạ từ stop_id đến stop_order để gắn thứ tự vào mỗi điểm dừng
        Map<Integer, Integer> stopOrderMap = routeStops.stream()
                .collect(Collectors.toMap(
                        rs -> rs.getStop().getId(),
                        RouteStop::getStopOrder,
                        (existing, replacement) -> existing // Trong trường hợp trùng lặp, giữ giá trị đầu tiên
                ));

        // Gắn thông tin thứ tự vào các stops
        for (Stops stop : stops) {
            Integer order = stopOrderMap.get(stop.getId());
            if (order != null) {
                stop.setStopOrder(order);
            }
        }

        // Sắp xếp các stops theo thứ tự trước khi format
        stops.sort(Comparator.comparing(stop -> stopOrderMap.getOrDefault(stop.getId(), Integer.MAX_VALUE)));

        List<Map<String, Object>> result = formatStopsList(stops);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Format một đối tượng Stops thành định dạng JSON phù hợp cho frontend
     */
    private Map<String, Object> formatStop(Stops stop) {
        Map<String, Object> stopData = new HashMap<>();

        stopData.put("id", stop.getId());
        stopData.put("name", stop.getStopName());
        stopData.put("latitude", stop.getLatitude());
        stopData.put("longitude", stop.getLongitude());
        stopData.put("address", stop.getAddress());
        if (stop.getStopOrder() != null) {
            stopData.put("stopOrder", stop.getStopOrder());
        }

        if (stop.getIsAccessible() != null) {
            stopData.put("isAccessible", stop.getIsAccessible());
        } else {
            stopData.put("isAccessible", true); // Giá trị mặc định
        }

        // Lấy danh sách các tuyến đi qua điểm dừng này
        List<Routes> routes = routeService.findRoutesByStops(List.of(stop.getId()));
        if (routes != null && !routes.isEmpty()) {
            List<Map<String, Object>> routeInfos = new ArrayList<>();

            for (Routes route : routes) {
                Map<String, Object> routeInfo = new HashMap<>();
                routeInfo.put("id", route.getId());

                if (route.getName() != null) {
                    routeInfo.put("name", route.getName());
                }

                if (route.getStartLocation() != null && route.getEndLocation() != null) {
                    routeInfo.put("startLocation", route.getStartLocation());
                    routeInfo.put("endLocation", route.getEndLocation());
                    routeInfo.put("routePath", route.getStartLocation() + " - " + route.getEndLocation());
                }

                if (route.getRouteType() != null && route.getRouteType().getColorCode() != null) {
                    routeInfo.put("color", route.getRouteType().getColorCode());
                } else {
                    routeInfo.put("color", "#4CAF50"); // Default color
                }

                // Lấy thứ tự của điểm dừng trong tuyến này
                List<RouteStop> routeStops = routeStopService.findByRouteId(route.getId());
                for (RouteStop rs : routeStops) {
                    if (rs.getStop().getId().equals(stop.getId())) {
                        routeInfo.put("stopOrder", rs.getStopOrder());
                        // Có thể thêm direction vào đây nếu đã triển khai
                        if (rs.getDirection() != null) {
                            routeInfo.put("direction", rs.getDirection());
                        }
                        break;
                    }
                }

                routeInfos.add(routeInfo);
            }

            stopData.put("routes", routeInfos);
        }

        return stopData;
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Map<String, Object>>> getNearbyStops(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") double radius) {

        // Nếu bạn đã cài đặt phương thức findNearbyStopsFormatted trong StopService, hãy sử dụng nó
        List<Map<String, Object>> nearbyStops = stopService.findNearbyStopsFormatted(lat, lng, radius);

        // Hoặc nếu bạn muốn sử dụng logic cũ
        // List<Stops> nearbyStops = stopService.findNearbyStops(lat, lng, radius);
        // List<Map<String, Object>> result = formatStopsList(nearbyStops);
        return new ResponseEntity<>(nearbyStops, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createStop(@RequestBody Stops stop) {
        try {
            Stops savedStop = stopService.saveStop(stop);
            Map<String, Object> response = formatStop(savedStop);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không thể tạo điểm dừng: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStop(@PathVariable Integer id, @RequestBody Stops stop) {
        if (!stopService.stopExists(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không tìm thấy điểm dừng với ID " + id);
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        try {
            stop.setId(id);
            Stops updatedStop = stopService.saveStop(stop);
            Map<String, Object> response = formatStop(updatedStop);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không thể cập nhật điểm dừng: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStop(@PathVariable Integer id) {
        if (!stopService.stopExists(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không tìm thấy điểm dừng với ID " + id);
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        try {
            stopService.deleteStop(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa điểm dừng thành công");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không thể xóa điểm dừng: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    private List<Map<String, Object>> formatStopsList(List<Stops> stops) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Stops stop : stops) {
            result.add(formatStop(stop));
        }

        return result;
    }
}
