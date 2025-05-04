package com.pts.controllers;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Route;
import com.pts.services.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ApiScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<Schedules>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedules> getScheduleById(@PathVariable Integer id) {
        return scheduleService.getScheduleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Schedules>> getSchedulesByVehicle(@PathVariable Integer vehicleId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByVehicle(new Vehicles(vehicleId)));
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<Schedules>> getSchedulesByRoute(@PathVariable Integer routeId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByRoute(new Route(routeId)));
    }

    @GetMapping("/time-range")
    public ResponseEntity<List<Schedules>> getSchedulesByTimeRange(
            @RequestParam Time startTime,
            @RequestParam Time endTime) {
        return ResponseEntity.ok(scheduleService.getSchedulesByTimeRange(startTime, endTime));
    }

    @PostMapping
    public ResponseEntity<Schedules> createSchedule(@RequestBody Schedules schedule) {
        return ResponseEntity.ok(scheduleService.createSchedule(schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedules> updateSchedule(
            @PathVariable Integer id,
            @RequestBody Schedules scheduleDetails) {
        try {
            return ResponseEntity.ok(scheduleService.updateSchedule(id, scheduleDetails));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer id) {
        try {
            scheduleService.deleteSchedule(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}