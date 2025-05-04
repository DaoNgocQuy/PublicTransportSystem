package com.pts.controllers;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import com.pts.services.ScheduleService;
import com.pts.services.VehicleService;
import com.pts.services.RoutesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Controller
@RequestMapping("/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private RoutesService routeService;

    @GetMapping
    public String listSchedules(Model model) {
        model.addAttribute("schedules", scheduleService.getAllSchedules());
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("routes", routeService.getAllRoutes());
        return "listSchedule";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("schedule", new Schedules());
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("routes", routeService.getAllRoutes());
        return "/addSchedule";
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
            
            return "addSchedule";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            model.addAttribute("routes", routeService.getAllRoutes());
            return "addSchedule";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Schedules schedule = scheduleService.getScheduleById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id:" + id));
        model.addAttribute("schedule", schedule);
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("routes", routeService.getAllRoutes());
        return "editSchedule";
    }

    @PostMapping("/edit/{id}")
    public String updateSchedule(@PathVariable Integer id,
                            @RequestParam("routeId.id") Integer routeId,
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
            
            // Tạo đối tượng Schedule mới với thông tin cập nhật
            Schedules scheduleDetails = new Schedules();
            scheduleDetails.setRouteId(route);
            scheduleDetails.setVehicleId(vehicle);
            
            // Chuyển đổi string time thành Date
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                java.util.Date depDate = sdf.parse(departureTime);
                java.util.Date arrDate = sdf.parse(arrivalTime);
                
                scheduleDetails.setDepartureTime(depDate);
                scheduleDetails.setArrivalTime(arrDate);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid time format", e);
            }
            
            // Gọi service với đúng tham số
            scheduleService.updateSchedule(id, scheduleDetails);
            return "redirect:/schedules";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("schedule", scheduleService.getScheduleById(id).orElse(new Schedules()));
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            model.addAttribute("routes", routeService.getAllRoutes());
            return "editSchedule";
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
            Model model) {
        
        try {
            List<Schedules> schedules;
            
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
                schedules = scheduleService.getSchedulesByRoute(route);
            } else if (vehicleId != null) {
                Vehicles vehicle = new Vehicles();
                vehicle.setId(vehicleId);
                schedules = scheduleService.getSchedulesByVehicle(vehicle);
            } else if (startTimeObj != null && endTimeObj != null) {
                schedules = scheduleService.getSchedulesByTimeRange(startTimeObj, endTimeObj);
            } else {
                schedules = scheduleService.getAllSchedules();
            }
            
            model.addAttribute("schedules", schedules);
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            model.addAttribute("routes", routeService.getAllRoutes());
            return "listSchedule";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("schedules", scheduleService.getAllSchedules());
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            model.addAttribute("routes", routeService.getAllRoutes());
            return "listSchedule";
        }
    }
}