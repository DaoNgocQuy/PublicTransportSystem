package com.pts.controllers;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import com.pts.repositories.NotificationRepository;
import com.pts.services.EmailService;
import java.util.Map;
import com.pts.services.ScheduleService;
import com.pts.services.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import com.pts.services.RouteService;
import java.util.Date;

@Controller
@RequestMapping("/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private NotificationRepository notificationRepository;    @Autowired
    private EmailService emailService;    @GetMapping
    public String listSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        
        Map<String, Object> response = scheduleService.getSchedulesWithPagination(page, size);
        
        model.addAttribute("schedules", response.get("schedules"));
        model.addAttribute("currentPage", response.get("currentPage"));
        model.addAttribute("totalItems", response.get("totalItems"));
        model.addAttribute("totalPages", response.get("totalPages"));
        model.addAttribute("pageSize", size);
        
        // Debug print for pagination info
        System.out.println("Pagination Info:");
        System.out.println("Current Page: " + response.get("currentPage"));
        System.out.println("Total Items: " + response.get("totalItems"));
        System.out.println("Total Pages: " + response.get("totalPages"));
        System.out.println("Page Size: " + size);
        
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("routes", routeService.getAllRoutes());
        
        // Add the base URL for pagination
        model.addAttribute("searchUrl", "/schedules");
        
        return "schedules/listSchedule";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("schedule", new Schedules());
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("routes", routeService.getAllRoutes());
        return "schedules/addSchedule";
    }

    @PostMapping("/add")
    public String addSchedule(@RequestParam("routeId.id") Integer routeId,
            @RequestParam("vehicleId.id") Integer vehicleId,
            @RequestParam("departureTime") String departureTime,
            @RequestParam("arrivalTime") String arrivalTime,
            Model model) {
        try {
            // Tạo đối tượng Route và Vehicles từ ID
            Routes route = new Routes();
            route.setId(routeId);

            Vehicles vehicle = new Vehicles();
            vehicle.setId(vehicleId);

            // Tạo đối tượng Schedule
            Schedules schedule = new Schedules();
            schedule.setRouteId(route);
            schedule.setVehicleId(vehicle);

            // Chuyển đổi string time thành Date
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                java.util.Date depDate = sdf.parse(departureTime);
                java.util.Date arrDate = sdf.parse(arrivalTime);

                schedule.setDepartureTime(depDate);
                schedule.setArrivalTime(arrDate);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid time format", e);
            }

            scheduleService.createSchedule(schedule);

            // Thay vì chuyển hướng, trả về lại trang thêm mới với thông báo thành công
            model.addAttribute("success", "Thêm lịch trình thành công!");
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            model.addAttribute("routes", routeService.getAllRoutes());

            // Tạo đối tượng Schedule mới để form được reset
            model.addAttribute("schedule", new Schedules());

            return "schedules/addSchedule";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            model.addAttribute("routes", routeService.getAllRoutes());
            return "schedules/addSchedule";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Schedules schedule = scheduleService.getScheduleById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id:" + id));
        model.addAttribute("schedule", schedule);
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("routes", routeService.getAllRoutes());
        return "schedules/editSchedule";
    }

    @PostMapping("/edit/{id}")
    public String updateSchedule(@PathVariable Integer id,
            @RequestParam("routeId.id") Integer routeId,
            @RequestParam("vehicleId.id") Integer vehicleId,
            @RequestParam("departureTime") String departureTime,
            @RequestParam("arrivalTime") String arrivalTime,
            Model model) {
        try {
            // Chuyển đổi chuỗi thời gian thành đối tượng Date
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date deptTime = format.parse(departureTime);
            Date arrTime = format.parse(arrivalTime);

            // Lấy đối tượng Schedule hiện tại
            Schedules existingSchedule = scheduleService.getScheduleById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id:" + id));

            // Lưu lại để so sánh sau này
            Schedules oldSchedule = new Schedules();
            oldSchedule.setId(existingSchedule.getId());
            oldSchedule.setDepartureTime(existingSchedule.getDepartureTime());
            oldSchedule.setArrivalTime(existingSchedule.getArrivalTime());
            oldSchedule.setRouteId(existingSchedule.getRouteId());
            oldSchedule.setVehicleId(existingSchedule.getVehicleId());

            // Cập nhật thông tin mới
            Routes route = new Routes();
            route.setId(routeId);
            existingSchedule.setRouteId(route);

            Vehicles vehicle = new Vehicles();
            vehicle.setId(vehicleId);
            existingSchedule.setVehicleId(vehicle);

            existingSchedule.setDepartureTime(deptTime);
            existingSchedule.setArrivalTime(arrTime);

            // Lưu thay đổi
            Schedules updatedSchedule = scheduleService.updateSchedule(id, existingSchedule);            // Kiểm tra và gửi thông báo nếu có thay đổi thời gian
            if (hasScheduleTimeChanged(oldSchedule, updatedSchedule)) {
                // Lấy thông tin đầy đủ của route từ routeService
                Routes fullRoute = routeService.getRouteById(routeId)
                        .orElse(updatedSchedule.getRouteId()); // Fallback to the route in schedule if not found
                sendScheduleChangeEmails(fullRoute, oldSchedule, updatedSchedule);
            }

            return "redirect:/schedules";
        } catch (ParseException e) {
            model.addAttribute("error", "Lỗi định dạng thời gian: " + e.getMessage());
            return "schedules/editSchedule";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "schedules/editSchedule";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteSchedule(@PathVariable Integer id) {
        scheduleService.deleteSchedule(id);
        return "redirect:/schedules";
    }    @GetMapping("/search")
    public String searchSchedules(
            @RequestParam(required = false) Integer routeId,
            @RequestParam(required = false) Integer vehicleId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        try {
            Map<String, Object> response;

            // Chuyển đổi startTime và endTime từ String thành Time nếu có
            Time startTimeObj = null;
            Time endTimeObj = null;
            if (startTime != null && !startTime.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                startTimeObj = new Time(sdf.parse(startTime).getTime());
            }
            if (endTime != null && !endTime.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                endTimeObj = new Time(sdf.parse(endTime).getTime());
            }

            // Tìm kiếm dựa trên các điều kiện
            if (routeId != null) {
                Routes route = new Routes();
                route.setId(routeId);
                response = scheduleService.getSchedulesByRouteWithPagination(route, page, size);
                
                // Debug log
                System.out.println("Search by route: " + routeId);
            } else if (vehicleId != null) {
                Vehicles vehicle = new Vehicles();
                vehicle.setId(vehicleId);
                response = scheduleService.getSchedulesByVehicleWithPagination(vehicle, page, size);
                
                // Debug log
                System.out.println("Search by vehicle: " + vehicleId);
            } else if (startTimeObj != null && endTimeObj != null) {
                response = scheduleService.getSchedulesByTimeRangeWithPagination(startTimeObj, endTimeObj, page, size);
                
                // Debug log
                System.out.println("Search by time range: " + startTime + " - " + endTime);
            } else {
                response = scheduleService.getSchedulesWithPagination(page, size);
                
                // Debug log
                System.out.println("No search criteria, showing all with pagination");
            }

            model.addAttribute("schedules", response.get("schedules"));
            model.addAttribute("currentPage", response.get("currentPage"));
            model.addAttribute("totalItems", response.get("totalItems"));
            model.addAttribute("totalPages", response.get("totalPages"));
            model.addAttribute("pageSize", size);
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            model.addAttribute("routes", routeService.getAllRoutes());
            
            // Add search parameters for pagination links
            if (routeId != null) {
                model.addAttribute("routeId", routeId);
                System.out.println("Added routeId to model: " + routeId);
            }
            if (vehicleId != null) {
                model.addAttribute("vehicleId", vehicleId);
                System.out.println("Added vehicleId to model: " + vehicleId);
            }
            if (startTime != null) {
                model.addAttribute("startTime", startTime);
                System.out.println("Added startTime to model: " + startTime);
            }
            if (endTime != null) {
                model.addAttribute("endTime", endTime);
                System.out.println("Added endTime to model: " + endTime);
            }
            
            // Add the search URL for pagination
            model.addAttribute("searchUrl", "/schedules/search");
            
            return "schedules/listSchedule";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            
            // In case of error, fallback to regular pagination
            Map<String, Object> response = scheduleService.getSchedulesWithPagination(page, size);
            model.addAttribute("schedules", response.get("schedules"));
            model.addAttribute("currentPage", response.get("currentPage"));
            model.addAttribute("totalItems", response.get("totalItems"));
            model.addAttribute("totalPages", response.get("totalPages"));
            model.addAttribute("pageSize", size);
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            model.addAttribute("routes", routeService.getAllRoutes());
            
            return "schedules/listSchedule";
        }
    }

    private boolean hasScheduleTimeChanged(Schedules oldSchedule, Schedules newSchedule) {
        try {
            // Log thông tin để kiểm tra
            System.out.println("Kiểm tra thay đổi lịch trình:");
            System.out.println("Old departure: " + (oldSchedule.getDepartureTime() != null ? oldSchedule.getDepartureTime().toString() : "null"));
            System.out.println("New departure: " + (newSchedule.getDepartureTime() != null ? newSchedule.getDepartureTime().toString() : "null"));
            System.out.println("Old arrival: " + (oldSchedule.getArrivalTime() != null ? oldSchedule.getArrivalTime().toString() : "null"));
            System.out.println("New arrival: " + (newSchedule.getArrivalTime() != null ? newSchedule.getArrivalTime().toString() : "null"));

            // Kiểm tra thay đổi giờ khởi hành
            boolean departureChanged = false;
            if (oldSchedule.getDepartureTime() == null && newSchedule.getDepartureTime() != null) {
                departureChanged = true;
            } else if (oldSchedule.getDepartureTime() != null && newSchedule.getDepartureTime() == null) {
                departureChanged = true;
            } else if (oldSchedule.getDepartureTime() != null && newSchedule.getDepartureTime() != null) {
                // So sánh thời gian không xét đến ngày tháng năm
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm");
                String oldTime = format.format(oldSchedule.getDepartureTime());
                String newTime = format.format(newSchedule.getDepartureTime());
                departureChanged = !oldTime.equals(newTime);
            }

            // Kiểm tra thay đổi giờ đến
            boolean arrivalChanged = false;
            if (oldSchedule.getArrivalTime() == null && newSchedule.getArrivalTime() != null) {
                arrivalChanged = true;
            } else if (oldSchedule.getArrivalTime() != null && newSchedule.getArrivalTime() == null) {
                arrivalChanged = true;
            } else if (oldSchedule.getArrivalTime() != null && newSchedule.getArrivalTime() != null) {
                // So sánh thời gian không xét đến ngày tháng năm
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm");
                String oldTime = format.format(oldSchedule.getArrivalTime());
                String newTime = format.format(newSchedule.getArrivalTime());
                arrivalChanged = !oldTime.equals(newTime);
            }

            boolean changed = departureChanged || arrivalChanged;
            System.out.println("Phát hiện thay đổi: " + changed + " (departure: " + departureChanged + ", arrival: " + arrivalChanged + ")");

            return changed;
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra thay đổi lịch trình: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }    private void sendScheduleChangeEmails(Routes route, Schedules oldSchedule, Schedules newSchedule) {
        try {
            System.out.println("Bắt đầu gửi email thông báo thay đổi lịch trình cho tuyến " + route.getId());
            
            // Kiểm tra và xử lý trường hợp route.getName() là null
            String routeName = (route.getName() != null) ? route.getName() : "ID: " + route.getId();
            System.out.println("Tên tuyến: " + routeName);

            // Lấy danh sách người dùng đăng ký nhận thông báo
            List<Map<String, Object>> subscribers = notificationRepository.getUsersSubscribedToRoute(route.getId());

            System.out.println("Số người đăng ký nhận thông báo: " + subscribers.size());

            if (subscribers.isEmpty()) {
                System.out.println("Không có người đăng ký nhận thông báo cho tuyến " + route.getId());
                return;
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String oldDeparture = oldSchedule.getDepartureTime() != null
                    ? timeFormat.format(oldSchedule.getDepartureTime()) : "N/A";
            String oldArrival = oldSchedule.getArrivalTime() != null
                    ? timeFormat.format(oldSchedule.getArrivalTime()) : "N/A";
            String newDeparture = newSchedule.getDepartureTime() != null
                    ? timeFormat.format(newSchedule.getDepartureTime()) : "N/A";
            String newArrival = newSchedule.getArrivalTime() != null
                    ? timeFormat.format(newSchedule.getArrivalTime()) : "N/A";

            String subject = "Thông báo thay đổi lịch trình tuyến " + ((route.getName() != null) ? route.getName() : "ID: " + route.getId());

            for (Map<String, Object> subscriber : subscribers) {
                String email = (String) subscriber.get("email");
                String fullName = (String) subscriber.get("full_name");
                Boolean notifyScheduleChanges = (Boolean) subscriber.get("notify_schedule_changes");

                System.out.println("Người dùng: " + fullName + " (" + email + "), nhận thông báo: " + notifyScheduleChanges);

                // Chỉ gửi cho người dùng đăng ký nhận thông báo và có email
                if (email != null && !email.isEmpty() && Boolean.TRUE.equals(notifyScheduleChanges)) {
                    // Tạo nội dung email
                    String content = createScheduleChangeEmail(
                            fullName != null && !fullName.isEmpty() ? fullName : "Quý khách",
                            route,
                            oldDeparture, oldArrival,
                            newDeparture, newArrival
                    );

                    // Gửi email
                    System.out.println("Đang gửi email đến: " + email);
                    try {
                        emailService.sendEmail(email, subject, content);
                        System.out.println("Đã gửi email thông báo thay đổi lịch trình cho: " + email);
                    } catch (Exception e) {
                        System.err.println("Lỗi khi gửi email đến " + email + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Không gửi email cho người dùng này vì họ không đăng ký nhận thông báo hoặc không có email");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email thông báo thay đổi lịch trình: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Tạo nội dung email thông báo thay đổi lịch trình
    private String createScheduleChangeEmail(String fullName, Routes route,
            String oldDeparture, String oldArrival, String newDeparture, String newArrival) {

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        html.append("<div style='background-color: #4CAF50; color: white; padding: 20px; text-align: center;'>");
        html.append("<h1>Thông báo thay đổi lịch trình tuyến</h1>");
        html.append("</div>");        html.append("<div style='padding: 20px; background-color: #f9f9f9; border: 1px solid #ddd;'>");
        html.append("<p>Xin chào " + fullName + ",</p>");
        String routeName = (route.getName() != null) ? route.getName() : "ID: " + route.getId();
        html.append("<p>Lịch trình của tuyến <strong>" + routeName + "</strong> đã được cập nhật.</p>");

        html.append("<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>");
        html.append("<tr style='background-color: #f2f2f2;'>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Thời gian</th>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Trước khi thay đổi</th>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Sau khi thay đổi</th>");
        html.append("</tr>");

        html.append("<tr>");
        html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Giờ khởi hành</td>");
        html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + oldDeparture + "</td>");
        html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + newDeparture + "</td>");
        html.append("</tr>");

        html.append("<tr>");
        html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Giờ đến</td>");
        html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + oldArrival + "</td>");
        html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + newArrival + "</td>");
        html.append("</tr>");

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
