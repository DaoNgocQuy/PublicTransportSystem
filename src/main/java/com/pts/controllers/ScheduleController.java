package com.pts.controllers;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import com.pts.services.NotificationService;

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

    private NotificationService notificationService;   
    
    @GetMapping
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
            Schedules updatedSchedule = scheduleService.updateSchedule(id, existingSchedule); // Kiểm tra và gửi thông
                                                                                              // báo nếu có thay đổi
                                                                                              // thời gian
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
    }

    @GetMapping("/search")
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
            System.out.println("Old departure: "
                    + (oldSchedule.getDepartureTime() != null ? oldSchedule.getDepartureTime().toString() : "null"));
            System.out.println("New departure: "
                    + (newSchedule.getDepartureTime() != null ? newSchedule.getDepartureTime().toString() : "null"));
            System.out.println("Old arrival: "
                    + (oldSchedule.getArrivalTime() != null ? oldSchedule.getArrivalTime().toString() : "null"));
            System.out.println("New arrival: "
                    + (newSchedule.getArrivalTime() != null ? newSchedule.getArrivalTime().toString() : "null"));

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
            System.out.println("Phát hiện thay đổi: " + changed + " (departure: " + departureChanged + ", arrival: "
                    + arrivalChanged + ")");

            return changed;
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra thay đổi lịch trình: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }    
    
    private void sendScheduleChangeEmails(Routes route, Schedules oldSchedule, Schedules newSchedule) {
        // Tạo danh sách để tương thích với interface
        List<Schedules> oldSchedules = List.of(oldSchedule);
        List<Schedules> newSchedules = List.of(newSchedule);
        notificationService.sendScheduleChangeNotification(oldSchedules, newSchedules, route);
    }
}
