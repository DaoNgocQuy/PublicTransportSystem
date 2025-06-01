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
@CrossOrigin
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
    public ResponseEntity<List<Map<String, Object>>> getStopsByRouteId(
            @PathVariable Integer routeId,
            @RequestParam(required = false) String direction) {

        List<Stops> stops;
        Integer directionValue = null;

        // Xác định giá trị direction (0: outbound/chiều đi, 1: return/chiều về)
        if (direction != null) {
            if (direction.equalsIgnoreCase("outbound")) {
                directionValue = 1; // Chuyển từ chuỗi "outbound" thành số 1
                System.out.println("API nhận direction=outbound, chuyển thành directionValue=" + directionValue);
            } else if (direction.equalsIgnoreCase("return")) {
                directionValue = 2; // Chuyển từ chuỗi "return" thành số 2
                System.out.println("API nhận direction=return, chuyển thành directionValue=" + directionValue);
            }
        } else {
            System.out.println("API không nhận được tham số direction");
        }

        // Lấy thông tin route_stops trước, vì nó chứa thứ tự và hướng đi
        List<RouteStop> routeStops;
        if (directionValue != null) {
            routeStops = routeStopService.findByRouteIdAndDirection(routeId, directionValue);
            System.out.println(
                    "Tìm thấy " + routeStops.size() + " trạm với routeId=" + routeId + ", direction=" + directionValue);
        } else {
            routeStops = routeStopService.findByRouteId(routeId);
            // Mặc định lấy chiều đi nếu không chỉ định direction
            routeStops = routeStops.stream()
                    .filter(rs -> rs.getDirection() == null || rs.getDirection() == 1) // Sửa từ 0 thành 1
                    .collect(Collectors.toList());
            System.out.println(
                    "Không chỉ định direction, lấy mặc định " + routeStops.size() + " trạm với routeId=" + routeId);
        }

        // Sắp xếp routeStops theo thứ tự tăng dần
        routeStops.sort(Comparator.comparing(RouteStop::getStopOrder));

        // Lấy ra danh sách stops từ routeStops đã sắp xếp
        stops = routeStops.stream()
                .map(RouteStop::getStop)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Gán thông tin từ route_stop vào mỗi stop
        for (int i = 0; i < stops.size(); i++) {
            Stops stop = stops.get(i);
            RouteStop rs = routeStops.get(i);

            stop.setStopOrder(rs.getStopOrder());

            if (directionValue != null) {
                stop.setDirection(directionValue);
            } else if (rs.getDirection() != null) {
                stop.setDirection(rs.getDirection());
            } else {
                // Mặc định là chiều đi (1) nếu không có direction
                stop.setDirection(1); // Sửa từ 0 thành 1
            }
        }

        // Format và trả về kết quả
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < stops.size(); i++) {
            Stops stop = stops.get(i);
            RouteStop rs = routeStops.get(i);

            Map<String, Object> stopData = new HashMap<>();
            stopData.put("id", stop.getId());
            stopData.put("name", stop.getStopName());
            stopData.put("latitude", stop.getLatitude());
            stopData.put("longitude", stop.getLongitude());
            stopData.put("address", stop.getAddress());
            stopData.put("stopOrder", rs.getStopOrder());
            stopData.put("direction", stop.getDirection());
            if (stop.getIsAccessible() != null) {
                stopData.put("isAccessible", stop.getIsAccessible());
            } else {
                stopData.put("isAccessible", true);
            }

            // Thêm thông tin về tuyến
            Routes route = rs.getRoute();
            if (route != null) {
                Map<String, Object> routeInfo = new HashMap<>();
                routeInfo.put("id", route.getId());
                routeInfo.put("name", route.getName());

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

                stopData.put("route", routeInfo);
            }

            result.add(stopData);
        }
        System.out.println("API trả về " + result.size() + " trạm với direction = " + directionValue);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Format một đối tượng Stops thành định dạng JSON phù hợp cho frontend Cải
     * tiến để hiển thị chính xác thông tin tuyến đi qua trạm dừng
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

        // Thêm direction nếu có
        if (stop.getDirection() != null) {
            stopData.put("direction", stop.getDirection());
        }

        if (stop.getIsAccessible() != null) {
            stopData.put("isAccessible", stop.getIsAccessible());
        } else {
            stopData.put("isAccessible", true); // Giá trị mặc định
        }

        // Lấy thông tin từ bảng route_stops để hiển thị chính xác mối quan hệ
        List<RouteStop> routeStops = routeStopService.findByStopId(stop.getId());
        if (routeStops != null && !routeStops.isEmpty()) {
            List<Map<String, Object>> routeInfos = new ArrayList<>();

            for (RouteStop rs : routeStops) {
                // Đảm bảo route không null
                Routes route = rs.getRoute();
                if (route == null) {
                    continue;
                }

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

                // Thêm thông tin từ bảng route_stops
                routeInfo.put("stopOrder", rs.getStopOrder());

                // Thêm thông tin hướng đi nếu có
                if (rs.getDirection() != null) {
                    routeInfo.put("direction", rs.getDirection());

                    // Thêm mô tả hướng đi nếu cần
                    if (rs.getDirection() == 1) { // Sửa từ 0 thành 1
                        routeInfo.put("directionName", "Chiều đi");
                    } else if (rs.getDirection() == 2) { // Sửa từ 1 thành 2
                        routeInfo.put("directionName", "Chiều về");
                    } else {
                        routeInfo.put("directionName", "Chưa xác định");
                    }
                }

                routeInfos.add(routeInfo);
            }

            stopData.put("routes", routeInfos);
        } else {
            // Phương pháp thay thế nếu không có thông tin route_stops
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

                    routeInfos.add(routeInfo);
                }

                stopData.put("routes", routeInfos);
            }
        }

        return stopData;
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Map<String, Object>>> getNearbyStops(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") double radius) {

        List<Map<String, Object>> nearbyStops = stopService.findNearbyStopsFormatted(lat, lng, radius);
        return new ResponseEntity<>(nearbyStops, HttpStatus.OK);
    }

    // Giữ nguyên các phương thức createStop, updateStop, deleteStop
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
            Map<String, Object> stopData = formatStop(stop);

            // Đảm bảo direction luôn được đưa vào kết quả nếu có trong stop
            if (stop.getDirection() != null) {
                stopData.put("direction", stop.getDirection());
            }

            result.add(stopData);
        }

        return result;
    }
}
