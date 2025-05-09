package com.pts.controllers;

import com.pts.pojo.Stops;
import com.pts.services.StopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/stops")
@CrossOrigin // Cho phép React truy cập API
public class ApiStopController {

    @Autowired
    private StopService stopService;

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
        stopData.put("stopOrder", stop.getStopOrder());

        if (stop.getIsAccessible() != null) {
            stopData.put("isAccessible", stop.getIsAccessible());
        } else {
            stopData.put("isAccessible", true); // Giá trị mặc định
        }

        // Thông tin về tuyến nếu có
        if (stop.getRouteId() != null) {
            Map<String, Object> routeInfo = new HashMap<>();
            routeInfo.put("id", stop.getRouteId().getId());

            // Thêm thông tin chi tiết về tuyến nếu có
            if (stop.getRouteId().getName() != null) {
                routeInfo.put("name", stop.getRouteId().getName());
            }

            if (stop.getRouteId().getStartLocation() != null && stop.getRouteId().getEndLocation() != null) {
                routeInfo.put("startLocation", stop.getRouteId().getStartLocation());
                routeInfo.put("endLocation", stop.getRouteId().getEndLocation());
                routeInfo.put("routePath",
                        stop.getRouteId().getStartLocation() + " - " + stop.getRouteId().getEndLocation());
            }

            stopData.put("route", routeInfo);
        }

        return stopData;
    }

    
    @GetMapping("/nearby")
    public ResponseEntity<List<Map<String, Object>>> getNearbyStops(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") double radius) {

        List<Stops> nearbyStops = stopService.findNearbyStops(lat, lng, radius);
        List<Map<String, Object>> result = formatStopsList(nearbyStops);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private List<Map<String, Object>> formatStopsList(List<Stops> stops) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Stops stop : stops) {
            result.add(formatStop(stop));
        }

        return result;
    }
}
