package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.pojo.Schedules;
import com.pts.pojo.Stops;
import com.pts.services.ScheduleService;
import com.pts.services.StopService;
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
import java.util.Map;

@Controller
@RequestMapping("/routes")
public class RouteController {

    @Autowired
    private RouteService routesService;

    @Autowired
    private StopService stopService; // Nếu có dịch vụ này

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

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
        try {
            // Lấy thông tin tuyến cũ để so sánh
            Optional<Routes> oldRouteOpt = routesService.getRouteById(id);
            
            if (oldRouteOpt.isPresent()) {
                Routes oldRoute = oldRouteOpt.get();
                
                // Đảm bảo ID được thiết lập
                route.setId(id);
                
                // Lưu tuyến đã cập nhật
                Routes updatedRoute = routesService.saveRoute(route);
                
                // Kiểm tra nếu có thay đổi về thông tin tuyến
                if (hasRouteChanged(oldRoute, route)) {
                    // Gửi email thông báo
                    sendChangeEmails(oldRoute, route);
                }
            }
            
            return "redirect:/routes";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/routes?error=" + e.getMessage();
        }
    }


    // Xóa tuyến
    @GetMapping("/delete/{id}")
    public String deleteRoute(@PathVariable("id") Integer id) {
        routesService.deleteRoute(id);
        return "redirect:/routes";
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
            String subject = "Thông báo thay đổi thông tin tuyến " + newRoute.getName();
            
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
                        newRoute
                    );
                    
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
        html.append("<p>Thông tin tuyến <strong>" + newRoute.getName() + "</strong> đã được cập nhật.</p>");
        
        html.append("<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>");
        html.append("<tr style='background-color: #f2f2f2;'>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Thông tin</th>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Trước khi thay đổi</th>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Sau khi thay đổi</th>");
        html.append("</tr>");
        
        // Nếu tên tuyến thay đổi
        if (!Objects.equals(oldRoute.getName(), newRoute.getName())) {
            html.append("<tr>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Tên tuyến</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + oldRoute.getName() + "</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + newRoute.getName() + "</td>");
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
        
        // Nếu giờ hoạt động thay đổi
        if (!Objects.equals(oldRoute.getOperationStartTime(), newRoute.getOperationStartTime()) ||
            !Objects.equals(oldRoute.getOperationEndTime(), newRoute.getOperationEndTime())) {
            
            String oldStartTime = oldRoute.getOperationStartTime() != null ? 
                                timeFormat.format(oldRoute.getOperationStartTime()) : "N/A";
            String oldEndTime = oldRoute.getOperationEndTime() != null ? 
                            timeFormat.format(oldRoute.getOperationEndTime()) : "N/A";
            String newStartTime = newRoute.getOperationStartTime() != null ? 
                                timeFormat.format(newRoute.getOperationStartTime()) : "N/A";
            String newEndTime = newRoute.getOperationEndTime() != null ? 
                            timeFormat.format(newRoute.getOperationEndTime()) : "N/A";
            
            html.append("<tr>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Giờ hoạt động</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + oldStartTime + " - " + oldEndTime + "</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + newStartTime + " - " + newEndTime + "</td>");
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