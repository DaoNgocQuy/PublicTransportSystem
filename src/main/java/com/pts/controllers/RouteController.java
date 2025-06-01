/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.pojo.RouteStop;
import com.pts.pojo.RouteTypes;
import com.pts.pojo.Schedules;
import com.pts.pojo.Stops;
import com.pts.services.RouteStopService;
import com.pts.services.RouteTypeService;
import com.pts.services.ScheduleService;
import com.pts.services.StopService;

import jakarta.servlet.http.HttpServletRequest;

import com.pts.repositories.NotificationRepository;
import com.pts.services.EmailService;
import java.text.SimpleDateFormat;
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
    private RouteTypeService routeTypeService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

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
            routes = routesService.searchRoutesByNameWithPagination(keyword, page * size, size);
            totalItems = routesService.countByNameContaining(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            // Lấy tất cả với phân trang
            routes = routesService.findAllWithPagination(page * size, size);
            totalItems = routesService.countAll();
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

            if (routeOptional.isPresent()) {
                Routes route = routeOptional.get();
                model.addAttribute("route", route);

                // Lấy danh sách trạm dừng theo chiều đã chọn
                List<Stops> stops = stopService.findStopsByRouteIdAndDirection(id, direction);

                if (stops == null || stops.isEmpty()) {
                    stops = stopService.findStopsByRouteId(id);
                }

                model.addAttribute("stops", stops != null ? stops : Collections.emptyList());

                // TẠO COORDINATES CHO BẢN ĐỒ
                List<double[]> coordinates = new ArrayList<>();
                if (stops != null && !stops.isEmpty()) {
                    for (Stops stop : stops) {
                        if (stop.getLatitude() != null && stop.getLongitude() != null) {
                            coordinates.add(new double[] { stop.getLatitude(), stop.getLongitude() });
                            System.out.println("Added coordinate: [" + stop.getLatitude() + ", " + stop.getLongitude()
                                    + "] for stop: " + stop.getStopName());
                        }
                    }
                }

                System.out.println("Total coordinates: " + coordinates.size());
                model.addAttribute("coordinates", coordinates);

                // Lấy thông tin chiều đi và chiều về
                boolean hasInbound = !routeStopService.findByRouteIdAndDirection(id, 1).isEmpty();
                boolean hasOutbound = !routeStopService.findByRouteIdAndDirection(id, 2).isEmpty();
                model.addAttribute("hasInbound", hasInbound);
                model.addAttribute("hasOutbound", hasOutbound);

                // Lấy danh sách lịch trình
                List<Schedules> schedules = scheduleService.findSchedulesByRouteId(id);
                model.addAttribute("schedules", schedules != null ? schedules : Collections.emptyList());

                return "routes/viewRoute";
            } else {
                return "redirect:/routes?error=RouteNotFound";
            }
        } catch (Exception e) {
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
            System.out.println("Đang thêm tuyến: " + route.getRouteName()); // SỬA FIELD NAME

            // Set default value for active
            if (route.getIsActive() == null) { // SỬA FIELD NAME
                route.setIsActive(true); // SỬA FIELD NAME
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
            routesService.updateTotalStops(savedRoute.getId()); // SỬA METHOD NAME

            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm tuyến " + savedRoute.getRouteName() + " thành công!"); // SỬA FIELD NAME
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
                model.addAttribute("title", "Chỉnh sửa tuyến " + route.getRouteName()); // SỬA FIELD NAME
                model.addAttribute("currentDirection", direction);
                model.addAttribute("routeTypes", routeTypeService.getAllRouteTypes());
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

    @PostMapping("/edit/{id}")
    public String updateRoute(@PathVariable("id") Integer id,
            @ModelAttribute Routes newRoute,
            @RequestParam(value = "routeTypeId", required = false) Integer routeTypeIdParam,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) { // Thêm để debug
        try {
            System.out.println("====== CONTROLLER METHOD CALLED ======");
            System.out.println("HTTP Method: " + request.getMethod());
            System.out.println("Request URI: " + request.getRequestURI());

            // In ra tất cả parameters
            System.out.println("=== ALL REQUEST PARAMETERS ===");
            for (String paramName : request.getParameterMap().keySet()) {
                String[] values = request.getParameterMap().get(paramName);
                System.out.println(paramName + " = " + String.join(", ", values));
            }

            System.out.println("=== DEBUG UPDATE ROUTE ===");
            System.out.println("Route ID: " + id);
            System.out.println("Route Name: " + newRoute.getRouteName());
            System.out.println("Start Location: " + newRoute.getStartLocation());
            System.out.println("End Location: " + newRoute.getEndLocation());
            System.out.println("Is Active from object: " + newRoute.getIsActive());
            System.out.println("Route Type ID Param: " + routeTypeIdParam);

            // Lấy thông tin tuyến cũ
            Optional<Routes> oldRouteOptional = routesService.getRouteById(id);
            if (oldRouteOptional.isPresent()) {
                Routes oldRoute = oldRouteOptional.get();
                newRoute.setIsActive(oldRoute.getIsActive());
            } else {
                newRoute.setIsActive(true); // giá trị mặc định
            }

            Routes oldRoute = oldRouteOptional.get();

            // KIỂM TRA CÁC TRƯỜNG BẮT BUỘC
            if (newRoute.getRouteName() == null || newRoute.getRouteName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên tuyến không được để trống!");
                return "redirect:/routes/edit/" + id;
            }

            if (newRoute.getStartLocation() == null || newRoute.getStartLocation().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Điểm bắt đầu không được để trống!");
                return "redirect:/routes/edit/" + id;
            }

            if (newRoute.getEndLocation() == null || newRoute.getEndLocation().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Điểm kết thúc không được để trống!");
                return "redirect:/routes/edit/" + id;
            }

            // Giữ lại các trường được tính toán tự động
            newRoute.setId(id);

            if (routeTypeIdParam != null && routeTypeIdParam > 0) {
                RouteTypes routeType = new RouteTypes();
                routeType.setId(routeTypeIdParam);
                newRoute.setRouteTypeId(routeType);
            } else {
                // Giữ nguyên loại tuyến từ route cũ
                newRoute.setRouteTypeId(oldRoute.getRouteTypeId());
            }

            // Giữ nguyên các field thời gian
            newRoute.setStartTime(oldRoute.getStartTime());
            newRoute.setEndTime(oldRoute.getEndTime());
            newRoute.setFrequencyMinutes(oldRoute.getFrequencyMinutes());
            newRoute.setTotalStops(oldRoute.getTotalStops());
            newRoute.setCreatedAt(oldRoute.getCreatedAt());

            System.out.println("Final isActive value: " + newRoute.getIsActive());
            System.out.println("About to save route...");

            // Cập nhật tuyến
            Routes savedRoute = routesService.saveRoute(newRoute);
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
            System.out.println("Route saved successfully with ID: " + savedRoute.getId());

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tuyến thành công!");
            return "redirect:/routes/view/" + id;

        } catch (Exception e) {
            System.err.println("ERROR in updateRoute: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật tuyến: " + e.getMessage());
            return "redirect:/routes/edit/" + id;
        }
    }

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
            @RequestParam(value = "stopOrder", required = false) Integer stopOrder) {

        routeStopService.addStopToRoute(routeId, stopId, direction, stopOrder);
        return "redirect:/routes/view/" + routeId + "?direction=" + direction;
    }

    // Xóa điểm dừng khỏi tuyến
    @GetMapping("/{routeId}/stops/{stopId}/remove")
    public String removeStopFromRoute(@PathVariable("routeId") Integer routeId,
            @PathVariable("stopId") Integer stopId,
            @RequestParam(value = "direction", required = false, defaultValue = "1") Integer direction) {
        // Tìm RouteStop dựa trên route_id, stop_id và direction
        List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(routeId, direction);
        for (RouteStop rs : routeStops) {
            if (rs.getStop().getId().equals(stopId)) {
                // Sử dụng deleteAndReorder thay vì deleteById để đảm bảo cập nhật thứ tự
                routeStopService.deleteAndReorder(rs.getId());
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
        // Kiểm tra tên tuyến - SỬA FIELD NAME
        boolean nameChanged = !Objects.equals(oldRoute.getRouteName(), newRoute.getRouteName());

        // Kiểm tra lộ trình (start_location và end_location)
        boolean startLocationChanged = !Objects.equals(oldRoute.getStartLocation(), newRoute.getStartLocation());
        boolean endLocationChanged = !Objects.equals(oldRoute.getEndLocation(), newRoute.getEndLocation());

        // Kiểm tra giờ hoạt động - SỬA FIELD NAME
        boolean startTimeChanged = !Objects.equals(oldRoute.getStartTime(), newRoute.getStartTime());
        boolean endTimeChanged = !Objects.equals(oldRoute.getEndTime(), newRoute.getEndTime());

        // Kiểm tra tần suất
        boolean frequencyChanged = !Objects.equals(oldRoute.getFrequencyMinutes(), newRoute.getFrequencyMinutes());

        return nameChanged || startLocationChanged || endLocationChanged || startTimeChanged || endTimeChanged
                || frequencyChanged;
    }

    // Thêm phương thức gửi email thông báo
    private void sendChangeEmails(Routes oldRoute, Routes newRoute) {
        try {
            // Lấy danh sách người dùng đăng ký nhận thông báo cho tuyến này
            List<Map<String, Object>> subscribers = notificationRepository.getUsersSubscribedToRoute(newRoute.getId());

            if (subscribers.isEmpty()) {
                System.out.println("Không có người đăng ký nhận thông báo cho tuyến " + newRoute.getId());
                return;
            }

            // Tạo tiêu đề email
            String subject = "Thông báo thay đổi thông tin tuyến " + newRoute.getRouteName(); // SỬA FIELD NAME

            // Gửi email cho mỗi người đăng ký
            for (Map<String, Object> subscriber : subscribers) {
                String email = (String) subscriber.get("email");
                String fullName = (String) subscriber.get("full_name");
                Boolean notifyScheduleChanges = (Boolean) subscriber.get("notify_schedule_changes");

                // Chỉ gửi cho người dùng đăng ký nhận thông báo và có email
                if (email != null && !email.isEmpty() && Boolean.TRUE.equals(notifyScheduleChanges)) {
                    // Tạo nội dung email
                    String content = createEmailContent(
                            fullName != null && !fullName.isEmpty() ? fullName : "Quý khách",
                            oldRoute,
                            newRoute);

                    // Gửi email
                    emailService.sendEmail(email, subject, content);
                    System.out.println("Đã gửi email thông báo thay đổi tuyến cho: " + email);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email thông báo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GetMapping("/api/getRouteData")
    @ResponseBody
    public Map<String, Object> getRouteData(@RequestParam("routeId") Integer routeId,
            @RequestParam("direction") Integer direction) {
        Map<String, Object> response = new HashMap<>();

        try {
            // SỬA LỖI: Dùng routeId thay vì id
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
                        coordinates.add(new double[] { stop.getLatitude(), stop.getLongitude() });
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

    // Tạo nội dung email
    private String createEmailContent(String fullName, Routes oldRoute, Routes newRoute) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        html.append("<div style='background-color: #4CAF50; color: white; padding: 20px; text-align: center;'>");
        html.append("<h1>Thông báo thay đổi thông tin tuyến</h1>");
        html.append("</div>");

        html.append("<div style='padding: 20px; background-color: #f9f9f9; border: 1px solid #ddd;'>");
        html.append("<p>Xin chào " + fullName + ",</p>");
        html.append("<p>Thông tin tuyến <strong>" + newRoute.getRouteName() + "</strong> đã được cập nhật.</p>"); // SỬA
                                                                                                                  // FIELD
                                                                                                                  // NAME

        html.append("<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>");
        html.append("<tr style='background-color: #f2f2f2;'>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Thông tin</th>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Trước khi thay đổi</th>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Sau khi thay đổi</th>");
        html.append("</tr>");

        // Nếu tên tuyến thay đổi - SỬA FIELD NAME
        if (!Objects.equals(oldRoute.getRouteName(), newRoute.getRouteName())) {
            html.append("<tr>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Tên tuyến</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + oldRoute.getRouteName() + "</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + newRoute.getRouteName() + "</td>");
            html.append("</tr>");
        }

        // Nếu điểm đầu/cuối thay đổi
        if (!Objects.equals(oldRoute.getStartLocation(), newRoute.getStartLocation())) {
            html.append("<tr>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Điểm đầu</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + oldRoute.getStartLocation() + "</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + newRoute.getStartLocation() + "</td>");
            html.append("</tr>");
        }

        if (!Objects.equals(oldRoute.getEndLocation(), newRoute.getEndLocation())) {
            html.append("<tr>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Điểm cuối</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + oldRoute.getEndLocation() + "</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + newRoute.getEndLocation() + "</td>");
            html.append("</tr>");
        }

        // Nếu giờ hoạt động thay đổi - SỬA FIELD NAME
        if (!Objects.equals(oldRoute.getStartTime(), newRoute.getStartTime())
                || !Objects.equals(oldRoute.getEndTime(), newRoute.getEndTime())) {

            String oldStartTime = oldRoute.getStartTime() != null
                    ? timeFormat.format(oldRoute.getStartTime())
                    : "N/A";
            String oldEndTime = oldRoute.getEndTime() != null
                    ? timeFormat.format(oldRoute.getEndTime())
                    : "N/A";
            String newStartTime = newRoute.getStartTime() != null
                    ? timeFormat.format(newRoute.getStartTime())
                    : "N/A";
            String newEndTime = newRoute.getEndTime() != null
                    ? timeFormat.format(newRoute.getEndTime())
                    : "N/A";

            html.append("<tr>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Giờ hoạt động</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + oldStartTime + " - " + oldEndTime
                    + "</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + newStartTime + " - " + newEndTime
                    + "</td>");
            html.append("</tr>");
        }

        // Nếu tần suất thay đổi
        if (!Objects.equals(oldRoute.getFrequencyMinutes(), newRoute.getFrequencyMinutes())) {
            String oldFreq = oldRoute.getFrequencyMinutes() != null ? oldRoute.getFrequencyMinutes() + " phút" : "N/A";
            String newFreq = newRoute.getFrequencyMinutes() != null ? newRoute.getFrequencyMinutes() + " phút" : "N/A";

            html.append("<tr>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Tần suất chạy</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + oldFreq + "</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + newFreq + "</td>");
            html.append("</tr>");
        }

        html.append("</table>");

        html.append("<p>Vui lòng lưu ý thông tin mới khi sử dụng tuyến này.</p>");
        html.append("<p>Trân trọng,<br>Hệ thống Vận tải Công cộng</p>");
        html.append("</div>");

        html.append("<div style='text-align: center; padding: 10px; color: #777; font-size: 12px;'>");
        html.append("<p>© 2025 Hệ thống Vận tải Công cộng<br>");
        html.append("Đây là email tự động, vui lòng không trả lời.</p>");
        html.append("</div>");

        html.append("</div></body></html>");

        return html.toString();
    }
}