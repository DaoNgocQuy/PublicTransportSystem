package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.pojo.Schedules;
import com.pts.pojo.Stops;
import com.pts.services.ScheduleService;
import com.pts.services.StopService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.pts.services.RouteService;
import java.util.Comparator;

@Controller
@RequestMapping("/routes")
public class RouteController {

    @Autowired
    private RouteService routesService;

    @Autowired
    private StopService stopService; // Nếu có dịch vụ này

    @Autowired
    private ScheduleService scheduleService;

    // Hiển thị danh sách tuyến
    @GetMapping
    public String listRoutes(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<Routes> routes;

        if (keyword != null && !keyword.isEmpty()) {
            // Tìm kiếm theo keyword
            routes = routesService.searchRoutesByName(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            // Lấy tất cả tuyến
            routes = routesService.getAllRoutes();
        }

        model.addAttribute("routes", routes);
        return "routes/listRoute";
    }

    @GetMapping("/view/{id}")
    public String viewRouteDetails(@PathVariable("id") Integer id, Model model) {
        try {
            // Lấy thông tin tuyến đường theo ID
            Optional<Routes> routeOptional = routesService.getRouteById(id);

            // Kiểm tra tuyến có tồn tại không
            if (routeOptional.isPresent()) {
                Routes route = routeOptional.get();
                model.addAttribute("route", route);
                model.addAttribute("title", "Chi tiết tuyến " + route.getName());

                // Lấy danh sách trạm dừng và sắp xếp theo thứ tự
                List<Stops> stops = stopService.findStopsByRouteId(id);
                if (stops != null && !stops.isEmpty()) {
                    stops.sort(Comparator.comparing(Stops::getStopOrder)); // Sắp xếp trạm theo thứ tự
                }
                model.addAttribute("stops", stops != null ? stops : Collections.emptyList());

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
        return "routes/addRoute"; // Tên view (addRoute.html)
    }

    @PostMapping("/add")
    public String addRoute(@ModelAttribute("route") Routes route) {
        // Lưu thông tin tuyến
        routesService.saveRoute(route);
        return "redirect:/routes"; // Chuyển hướng về danh sách tuyến
    }

    // Hiển thị form chỉnh sửa tuyến
    @GetMapping("/edit/{id}")
    public String editRouteForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("route", routesService.getRouteById(id).orElse(null));
        return "routes/editRoute"; // Trả về view "edit.html"
    }

    // Xử lý cập nhật tuyến
    @PostMapping("/edit/{id}")
    public String updateRoute(@PathVariable("id") Integer id, @ModelAttribute("route") Routes route) {
        route.setId(id);
        routesService.saveRoute(route);
        return "redirect:/routes";
    }

    // Xóa tuyến
    @GetMapping("/delete/{id}")
    public String deleteRoute(@PathVariable("id") Integer id) {
        routesService.deleteRoute(id);
        return "redirect:/routes";
    }
}
