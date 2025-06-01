package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.pojo.RouteStop;
import com.pts.pojo.Schedules;
import com.pts.pojo.Stops;
import com.pts.services.RouteStopService;
import com.pts.services.ScheduleService;
import com.pts.services.StopService;
import com.pts.services.NotificationService;

import java.util.Objects;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.pts.services.RouteService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/routes")
public class RouteController {

    @Autowired
    private RouteService routesService;

    @Autowired
    private StopService stopService;

    @Autowired
    private RouteStopService routeStopService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private NotificationService notificationService;

    // Hiển thị danh sách tuyến
    @GetMapping
    public String listRoutes(Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        // Cố định số lượng mỗi trang là 5
        final int size = 5;

        // Validate input params
        if (page < 0) {
            page = 0;
        }

        List<Routes> routes;
        int totalItems;
        int totalPages;

        if (keyword != null && !keyword.isEmpty()) {
            // Tìm kiếm với phân trang
            routes = routesService.searchRoutesByNameWithPagination(keyword, page, size);
            totalItems = routesService.getTotalRoutesByKeyword(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            // Lấy tất cả với phân trang
            routes = routesService.getAllRoutesWithPagination(page, size);
            totalItems = routesService.getTotalRoutes();
        }

        totalPages = (int) Math.ceil((double) totalItems / (double) size);

        model.addAttribute("routes", routes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);

        return "routes/listRoute";
    }

    @GetMapping("/view/{id}")
    public String viewRouteDetails(@PathVariable("id") Integer id,
            @RequestParam(value = "direction", required = false, defaultValue = "1") Integer direction,
            Model model) {
        try {
            // Lấy thông tin tuyến đường theo ID
            Optional<Routes> routeOptional = routesService.getRouteById(id);

            // Kiểm tra tuyến có tồn tại không
            if (routeOptional.isPresent()) {
                Routes route = routeOptional.get();

                // Kiểm tra nếu bất kỳ trường nào là NULL, tính toán lại
                if (route.getFrequencyMinutes() == 0
                        || route.getOperationStartTime() == null
                        || route.getOperationEndTime() == null) {

                    // Lưu lại tuyến sẽ kích hoạt updateRouteCalculatedFields()
                    routesService.saveRoute(route);

                    // Lấy lại tuyến sau khi cập nhật
                    routeOptional = routesService.getRouteById(id);
                    route = routeOptional.get();

                    System.out.println("Đã tự động tính thông tin cho tuyến ID " + id);
                }
                model.addAttribute("route", route);
                model.addAttribute("title", "Chi tiết tuyến " + route.getName());
                model.addAttribute("currentDirection", direction);

                // Lấy danh sách trạm dừng theo chiều đã chọn
                List<Stops> stops = stopService.findStopsByRouteIdAndDirection(id, direction);

                if (stops == null || stops.isEmpty()) {
                    // Nếu không tìm thấy dữ liệu cho chiều đã chọn, thử lấy dữ liệu cho mọi chiều
                    stops = stopService.findStopsByRouteId(id);
                }

                // Lấy thông tin về thứ tự dừng cho chiều đã chọn
                List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(id, direction);

                if (routeStops == null || routeStops.isEmpty()) {
                    // Nếu không tìm thấy dữ liệu cho chiều đã chọn, thử lấy dữ liệu cho mọi chiều
                    routeStops = routeStopService.findByRouteId(id);
                }

                // Tạo map ánh xạ từ stop_id đến stop_order để có thể hiển thị thứ tự
                Map<Integer, Integer> stopOrderMap = routeStops.stream()
                        .collect(Collectors.toMap(
                                rs -> rs.getStop().getId(),
                                RouteStop::getStopOrder,
                                (existing, replacement) -> existing // Nếu trùng lặp, giữ giá trị đầu tiên
                        ));

                // Gắn thông tin về thứ tự dừng vào các Stop objects
                for (Stops stop : stops) {
                    Integer order = stopOrderMap.get(stop.getId());
                    if (order != null) {
                        stop.setStopOrder(order);
                    }
                }

                // Sắp xếp trạm theo thứ tự
                stops.sort(Comparator.comparing(Stops::getStopOrder));

                model.addAttribute("stops", stops != null ? stops : Collections.emptyList());

                // Lấy thông tin chiều đi và chiều về
                boolean hasInbound = !routeStopService.findByRouteIdAndDirection(id, 1).isEmpty();
                boolean hasOutbound = !routeStopService.findByRouteIdAndDirection(id, 2).isEmpty();
                model.addAttribute("hasInbound", hasInbound);
                model.addAttribute("hasOutbound", hasOutbound);

                List<double[]> coordinates = new ArrayList<>();
                if (stops != null) {
                    for (Stops stop : stops) {
                        if (stop.getLatitude() != null && stop.getLongitude() != null) {
                            coordinates.add(new double[]{stop.getLatitude(), stop.getLongitude()});
                        }
                    }
                }
                model.addAttribute("coordinates", coordinates);

                // Lấy danh sách lịch trình
                List<Schedules> schedules = scheduleService.findSchedulesByRouteId(id);
                model.addAttribute("schedules", schedules != null ? schedules : Collections.emptyList());

                return "routes/viewRoute";
            } else {
                // Nếu không tìm thấy tuyến, chuyển hướng về danh sách với thông báo lỗi
                return "redirect:/routes?error=RouteNotFound";
            }
        } catch (Exception e) {
            // Xử lý ngoại lệ, ghi log và hiển thị trang lỗi
            System.err.println("Lỗi khi xem chi tiết tuyến ID " + id + ": " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("errorMessage", "Lỗi khi xem chi tiết tuyến: " + e.getMessage());
            return "error";
        }
    }

    // Hiển thị form thêm tuyến mới
    @GetMapping("/add")
    public String addRouteForm(Model model) {
        model.addAttribute("route", new Routes());
        // Thêm danh sách tất cả các điểm dừng để có thể chọn khi tạo tuyến
        model.addAttribute("allStops", stopService.getAllStops());
        return "routes/addRoute"; // Tên view (addRoute.html)
    }

    @PostMapping("/add")
    public String addRoute(@ModelAttribute("route") Routes route,
            @RequestParam(value = "selectedStopsInbound", required = false) String selectedStopsInboundStr,
            @RequestParam(value = "selectedStopsOutbound", required = false) String selectedStopsOutboundStr,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Đang thêm tuyến: " + route.getName());

            // Set default value for active
            if (route.getActive() == null) {
                route.setActive(true);
            }

            // Lưu thông tin tuyến
            Routes savedRoute = routesService.saveRoute(route);

            // Kiểm tra nếu lưu tuyến thành công
            if (savedRoute.getId() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu thông tin tuyến!");
                return "redirect:/routes/add";
            }

            System.out.println("Đã lưu tuyến ID: " + savedRoute.getId());

            // Xử lý các trạm dừng chiều đi
            List<Integer> selectedStopsInbound = new ArrayList<>();
            if (selectedStopsInboundStr != null && !selectedStopsInboundStr.isEmpty()) {
                String[] stopIdsArray = selectedStopsInboundStr.split(",");
                for (String stopIdStr : stopIdsArray) {
                    try {
                        selectedStopsInbound.add(Integer.parseInt(stopIdStr));
                    } catch (NumberFormatException e) {
                        System.err.println("Lỗi khi chuyển đổi ID trạm: " + stopIdStr);
                    }
                }
            }

            // Xử lý các trạm dừng chiều về
            List<Integer> selectedStopsOutbound = new ArrayList<>();
            if (selectedStopsOutboundStr != null && !selectedStopsOutboundStr.isEmpty()) {
                String[] stopIdsArray = selectedStopsOutboundStr.split(",");
                for (String stopIdStr : stopIdsArray) {
                    try {
                        selectedStopsOutbound.add(Integer.parseInt(stopIdStr));
                    } catch (NumberFormatException e) {
                        System.err.println("Lỗi khi chuyển đổi ID trạm: " + stopIdStr);
                    }
                }
            }

            // Thêm các trạm dừng chiều đi (direction = 1)
            if (!selectedStopsInbound.isEmpty()) {
                System.out.println("Thêm " + selectedStopsInbound.size() + " trạm cho chiều đi");
                boolean success = routeStopService.reorderStops(savedRoute.getId(), selectedStopsInbound, 1);
                if (!success) {
                    System.err.println("Có lỗi khi thêm trạm dừng chiều đi");
                }
            }

            // Thêm các trạm dừng chiều về (direction = 2)
            if (!selectedStopsOutbound.isEmpty()) {
                System.out.println("Thêm " + selectedStopsOutbound.size() + " trạm cho chiều về");
                boolean success = routeStopService.reorderStops(savedRoute.getId(), selectedStopsOutbound, 2);
                if (!success) {
                    System.err.println("Có lỗi khi thêm trạm dừng chiều về");
                }
            }

            // Tính toán lại các thông số cho tuyến (như tổng số trạm)
            routesService.recalculateRoute(savedRoute.getId());

            redirectAttributes.addFlashAttribute("successMessage", "Thêm tuyến " + savedRoute.getName() + " thành công!");
            return "redirect:/routes/view/" + savedRoute.getId();
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm tuyến: " + e.getMessage());
            return "redirect:/routes/add";
        }
    }

    // Hiển thị form chỉnh sửa tuyến
    @GetMapping("/edit/{id}")
    public String showEditRouteForm(@PathVariable("id") Integer id,
            @RequestParam(value = "direction", required = false, defaultValue = "1") Integer direction,
            Model model) {
        try {
            // Lấy thông tin tuyến
            Optional<Routes> routeOptional = routesService.getRouteById(id);

            if (routeOptional.isPresent()) {
                Routes route = routeOptional.get();
                model.addAttribute("route", route);
                model.addAttribute("title", "Chỉnh sửa tuyến " + route.getName());
                model.addAttribute("currentDirection", direction);

                // Lấy danh sách tất cả điểm dừng để chọn
                model.addAttribute("allStops", stopService.getAllStops());

                // Lấy danh sách điểm dừng chiều đi
                List<Stops> selectedStopsInbound = stopService.findStopsByRouteIdAndDirection(id, 1);
                model.addAttribute("selectedStopsInbound", selectedStopsInbound);

                // Lấy danh sách điểm dừng chiều về
                List<Stops> selectedStopsOutbound = stopService.findStopsByRouteIdAndDirection(id, 2);
                model.addAttribute("selectedStopsOutbound", selectedStopsOutbound);

                return "routes/editRoute";
            } else {
                return "redirect:/routes?error=RouteNotFound";
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi hiển thị form chỉnh sửa tuyến ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return "redirect:/routes?error=" + e.getMessage();
        }
    }
// Thêm phương thức này sau phương thức showEditRouteForm

    @PostMapping("/edit/{id}")
    public String updateRoute(@PathVariable("id") Integer id,
            @ModelAttribute Routes newRoute,
            RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin tuyến cũ
            Optional<Routes> oldRouteOptional = routesService.getRouteById(id);

            if (!oldRouteOptional.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tuyến có ID " + id);
                return "redirect:/routes";
            }

            Routes oldRoute = oldRouteOptional.get();

            // Giữ lại các trường được tính toán tự động
            newRoute.setId(id);

            // Kiểu dữ liệu đã được chuyển đổi đúng
            if (oldRoute.getOperationStartTime() != null) {
                newRoute.setOperationStartTime(new java.sql.Time(oldRoute.getOperationStartTime().getTime()));
            }
            if (oldRoute.getOperationEndTime() != null) {
                newRoute.setOperationEndTime(new java.sql.Time(oldRoute.getOperationEndTime().getTime()));
            }

            newRoute.setFrequencyMinutes(oldRoute.getFrequencyMinutes());
            newRoute.setTotalStops(oldRoute.getTotalStops());
            newRoute.setCreatedAt(oldRoute.getCreatedAt());

            // Cập nhật tuyến
            Routes savedRoute = routesService.saveRoute(newRoute);

            // Kiểm tra nếu có thay đổi đáng kể
            boolean significantChanges = hasRouteChanged(oldRoute, savedRoute);

            // Nếu có thay đổi đáng kể, gửi thông báo
            if (significantChanges) {
                // Đặt lịch gửi email thông báo thay đổi tuyến
                sendChangeEmails(oldRoute, savedRoute);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Cập nhật tuyến thành công! Thông báo đã được gửi cho hành khách đăng ký.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tuyến thành công!");
            }

            return "redirect:/routes/view/" + id;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật tuyến: " + e.getMessage());
            return "redirect:/routes/edit/" + id;
        }
    }

    // Xử lý cập nhật tuyến
    // Xóa tuyến
    @GetMapping("/delete/{id}")
    public String deleteRoute(@PathVariable("id") Integer id) {
        routesService.deleteRoute(id);
        return "redirect:/routes";
    }

    // Thêm điểm dừng vào tuyến
    @PostMapping("/{routeId}/stops/add")
    public String addStopToRoute(@PathVariable("routeId") Integer routeId,
            @RequestParam("stopId") Integer stopId,
            @RequestParam(value = "direction", required = false, defaultValue = "1") Integer direction,
            @RequestParam(value = "stopOrder", required = false) Integer stopOrder) { // Thêm tham số stopOrder

        routeStopService.addStopToRoute(routeId, stopId, direction, stopOrder);
        return "redirect:/routes/view/" + routeId + "?direction=" + direction;
    }

    // Xóa điểm dừng khỏi tuyến
    @GetMapping("/{routeId}/stops/{stopId}/remove")
    public String removeStopFromRoute(@PathVariable("routeId") Integer routeId,
            @PathVariable("stopId") Integer stopId,
            @RequestParam(value = "direction", required = false, defaultValue = "1") Integer direction) {
        // Tìm RouteStop dựa trên route_id và stop_id và direction
        List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(routeId, direction);
        for (RouteStop rs : routeStops) {
            if (rs.getStop().getId().equals(stopId)) {
                routeStopService.deleteById(rs.getId());
                break;
            }
        }
        return "redirect:/routes/view/" + routeId + "?direction=" + direction;
    }

    // Thay đổi thứ tự điểm dừng
    @PostMapping("/{routeId}/stops/reorder")
    public String reorderRouteStops(@PathVariable("routeId") Integer routeId,
            @RequestBody List<Integer> stopIds,
            @RequestParam(value = "direction", required = false, defaultValue = "1") Integer direction) {
        routeStopService.reorderStops(routeId, stopIds, direction);
        return "redirect:/routes/view/" + routeId + "?direction=" + direction;
    }

    private boolean hasRouteChanged(Routes oldRoute, Routes newRoute) {
        // Kiểm tra tên tuyến
        boolean nameChanged = !Objects.equals(oldRoute.getName(), newRoute.getName());

        // Kiểm tra lộ trình (start_location và end_location)
        boolean startLocationChanged = !Objects.equals(oldRoute.getStartLocation(), newRoute.getStartLocation());
        boolean endLocationChanged = !Objects.equals(oldRoute.getEndLocation(), newRoute.getEndLocation());

        // Kiểm tra giờ hoạt động
        boolean startTimeChanged = !Objects.equals(oldRoute.getOperationStartTime(), newRoute.getOperationStartTime());
        boolean endTimeChanged = !Objects.equals(oldRoute.getOperationEndTime(), newRoute.getOperationEndTime());

        // Kiểm tra tần suất
        boolean frequencyChanged = !Objects.equals(oldRoute.getFrequencyMinutes(), newRoute.getFrequencyMinutes());

        return nameChanged || startLocationChanged || endLocationChanged || startTimeChanged || endTimeChanged || frequencyChanged;
    }

    private void sendChangeEmails(Routes oldRoute, Routes newRoute) {
        notificationService.sendRouteChangeNotification(oldRoute, newRoute);
    }

    @GetMapping("/api/getRouteData")
    @ResponseBody
    public Map<String, Object> getRouteData(@RequestParam("routeId") Integer routeId,
            @RequestParam("direction") Integer direction) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Sửa từ findById thành getRouteById
            Optional<Routes> routeOptional = routesService.getRouteById(routeId);

            if (routeOptional.isPresent()) {
                Routes route = routeOptional.get();
                response.put("route", route);

                // Lấy danh sách trạm dừng theo chiều
                List<Stops> stops = stopService.findStopsByRouteIdAndDirection(routeId, direction);
                response.put("stops", stops);

                // Lấy tọa độ
                List<double[]> coordinates = new ArrayList<>();
                for (Stops stop : stops) {
                    if (stop.getLatitude() != null && stop.getLongitude() != null) {
                        coordinates.add(new double[]{stop.getLatitude(), stop.getLongitude()});
                    }
                }
                response.put("coordinates", coordinates);
            } else {
                response.put("error", "Route not found");
            }

            return response;
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return response;
        }
    }
}
