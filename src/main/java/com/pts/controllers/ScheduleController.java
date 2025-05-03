package com.pts.controllers;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import com.pts.services.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<Schedules>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedules> getScheduleById(@PathVariable Long id) {
        return scheduleService.getScheduleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Schedules>> getSchedulesByVehicle(@PathVariable Vehicles vehicleId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByVehicle(vehicleId));
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<Schedules>> getSchedulesByRoute(@PathVariable Routes routeId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByRoute(routeId));
    }

    @GetMapping("/time-range")
    public ResponseEntity<List<Schedules>> getSchedulesByTimeRange(
            @RequestParam Date startTime,
            @RequestParam Date endTime) {
        return ResponseEntity.ok(scheduleService.getSchedulesByTimeRange(startTime, endTime));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Schedules>> getSchedulesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(scheduleService.getSchedulesByStatus(status));
    }

    @PostMapping
    public ResponseEntity<Schedules> createSchedule(@RequestBody Schedules schedule) {
        return ResponseEntity.ok(scheduleService.createSchedule(schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedules> updateSchedule(
            @PathVariable Long id,
            @RequestBody Schedules scheduleDetails) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, scheduleDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok().build();
    }
}