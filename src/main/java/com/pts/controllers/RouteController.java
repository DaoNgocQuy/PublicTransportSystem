package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.services.RoutesService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/routes")
public class RouteController {

    @Autowired
    private RoutesService routesService;

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
        return "listRoute";
    }

    // Hiển thị form thêm tuyến mới
    @GetMapping("/add")
    public String addRouteForm(Model model) {
        model.addAttribute("route", new Routes());
        return "addRoute"; // Tên view (addRoute.html)
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
        return "editRoute"; // Trả về view "edit.html"
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
