package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import com.pts.services.StopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import com.pts.services.RouteService;

@Controller
@RequestMapping("/stops")
public class StopController {

    @Autowired
    private StopService stopService;

    @Autowired
    private RouteService routeService;

    // Hiển thị danh sách tất cả trạm dừng
    @GetMapping
    public String listAllStops(Model model) {
        List<Stops> stops = stopService.getAllStops();
        model.addAttribute("stops", stops);
        return "stops/list";
    }

    // Hiển thị form tạo trạm dừng mới
    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Integer routeId, Model model) {
        Stops stop = new Stops();
        
        // Nếu có routeId, tự động chọn tuyến
        if (routeId != null) {
            Routes route = new Routes();
            route.setId(routeId);
            stop.setRouteId(route);
        }
        
        model.addAttribute("stop", stop);
        model.addAttribute("routes", routeService.getAllRoutes());
        return "stops/create";
    }

    // Xử lý submit form tạo mới
    @PostMapping("/create")
    public String createStop(@ModelAttribute("stop") Stops stop, 
                            BindingResult result, 
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "stops/create";
        }
        
        stopService.saveStop(stop);
        redirectAttributes.addFlashAttribute("successMessage", "Trạm dừng đã được tạo thành công!");
        
        // Điều hướng về trang chi tiết tuyến nếu có routeId
        if (stop.getRouteId() != null) {
            return "redirect:/routes/view/" + stop.getRouteId().getId();
        }
        
        return "redirect:/stops";
    }

    // Hiển thị form chỉnh sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Optional<Stops> stop = stopService.getStopById(id);
        
        if (stop.isPresent()) {
            model.addAttribute("stop", stop.get());
            model.addAttribute("routes", routeService.getAllRoutes());
            return "stops/edit";
        } else {
            return "redirect:/stops?error=StopNotFound";
        }
    }

    // Xử lý submit form sửa
    @PostMapping("/edit/{id}")
    public String updateStop(@PathVariable("id") Integer id, 
                            @ModelAttribute("stop") Stops stop, 
                            BindingResult result, 
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "stops/edit";
        }
        
        stop.setId(id);
        stopService.saveStop(stop);
        redirectAttributes.addFlashAttribute("successMessage", "Trạm dừng đã được cập nhật thành công!");
        
        // Điều hướng về trang chi tiết tuyến nếu có routeId
        if (stop.getRouteId() != null) {
            return "redirect:/routes/view/" + stop.getRouteId().getId();
        }
        
        return "redirect:/stops";
    }

    // Xem chi tiết trạm dừng
    @GetMapping("/view/{id}")
    public String viewStop(@PathVariable("id") Integer id, Model model) {
        Optional<Stops> stop = stopService.getStopById(id);
        
        if (stop.isPresent()) {
            model.addAttribute("stop", stop.get());
            return "stops/view";
        } else {
            return "redirect:/stops?error=StopNotFound";
        }
    }

    // Xóa trạm dừng
    @GetMapping("/delete/{id}")
    public String deleteStop(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Optional<Stops> stop = stopService.getStopById(id);
        Integer routeId = null;
        
        // Lưu routeId trước khi xóa để điều hướng nếu cần
        if (stop.isPresent() && stop.get().getRouteId() != null) {
            routeId = stop.get().getRouteId().getId();
        }
        
        stopService.deleteStop(id);
        redirectAttributes.addFlashAttribute("successMessage", "Trạm dừng đã được xóa thành công!");
        
        // Điều hướng về trang chi tiết tuyến nếu có routeId
        if (routeId != null) {
            return "redirect:/routes/view/" + routeId;
        }
        
        return "redirect:/stops";
    }
    
    // Hiển thị danh sách trạm dừng theo tuyến
    @GetMapping("/route/{routeId}")
    public String listStopsByRoute(@PathVariable("routeId") Integer routeId, Model model) {
        List<Stops> stops = stopService.findStopsByRouteId(routeId);
        Optional<Routes> route = routeService.getRouteById(routeId);
        
        model.addAttribute("stops", stops);
        if (route.isPresent()) {
            model.addAttribute("route", route.get());
        }
        
        return "stops/routeStops";
    }
}