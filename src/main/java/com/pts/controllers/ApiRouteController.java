package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.pojo.RouteTypes;
import com.pts.pojo.Schedules;
import com.pts.services.RouteService;
import com.pts.services.ScheduleService;
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
                routeInfo.put("icon", iconUrl != null && !iconUrl.isEmpty() ? 
                        iconUrl : getDefaultIcon(routeType.getTypeName()));
                
                // Lấy mã màu từ RouteTypes nếu có
                String colorCode = routeType.getColorCode();
                routeInfo.put("color", colorCode != null && !colorCode.isEmpty() ? 
                        colorCode : "#007bff"); // màu mặc định
            } else {
                // Giá trị mặc định nếu không có RouteTypes
                routeInfo.put("icon", "https://cdn-icons-png.flaticon.com/512/2554/2554642.png");
                routeInfo.put("color", "#007bff");
            }
            
            // Thời gian hoạt động
            if (route.getOperationStartTime() != null && route.getOperationEndTime() != null) {
                // Sử dụng thời gian hoạt động từ entity
                routeInfo.put("operatingHours", 
                    timeFormat.format(route.getOperationStartTime()) + " - " + 
                    timeFormat.format(route.getOperationEndTime()));
            } else {
                // Hoặc lấy từ lịch trình nếu không có
                routeInfo.put("operatingHours", getRouteOperatingHours(route.getId(), timeFormat));
            }
            
            result.add(routeInfo);
        }
        
        return new ResponseEntity<>(result, HttpStatus.OK);
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
            routeInfo.put("icon", iconUrl != null && !iconUrl.isEmpty() ? 
                    iconUrl : getDefaultIcon(routeType.getTypeName()));
            
            // Lấy mã màu từ RouteTypes nếu có
            String colorCode = routeType.getColorCode();
            routeInfo.put("color", colorCode != null && !colorCode.isEmpty() ? 
                    colorCode : route.getRouteColor()); // sử dụng màu từ Routes nếu không có trong RouteTypes
        } else {
            // Giá trị mặc định nếu không có RouteTypes
            routeInfo.put("icon", "https://cdn-icons-png.flaticon.com/512/2554/2554642.png");
            routeInfo.put("color", route.getRouteColor() != null ? route.getRouteColor() : "#007bff");
        }
        
        // Thời gian hoạt động
        if (route.getOperationStartTime() != null && route.getOperationEndTime() != null) {
            // Sử dụng thời gian hoạt động từ entity
            routeInfo.put("operatingHours", 
                timeFormat.format(route.getOperationStartTime()) + " - " + 
                timeFormat.format(route.getOperationEndTime()));
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
}