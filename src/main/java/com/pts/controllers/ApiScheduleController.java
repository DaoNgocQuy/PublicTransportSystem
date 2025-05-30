package com.pts.controllers;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import com.pts.services.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<Schedules>> getSchedulesByRoute(
            @PathVariable Integer routeId,
            @RequestParam(required = false) String direction) {

        Routes route = new Routes(routeId);
        List<Schedules> schedules = scheduleService.getSchedulesByRoute(route);

        if (schedules != null && !schedules.isEmpty()) {
            schedules.sort(Comparator.comparing(Schedules::getDepartureTime));
        }

        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/route/{routeId}/frequency")
    public ResponseEntity<?> getRouteFrequency(@PathVariable Integer routeId) {
        Routes route = new Routes(routeId);
        List<Schedules> schedules = scheduleService.getSchedulesByRoute(route);

        if (schedules == null || schedules.size() < 2) {
            return ResponseEntity.ok(Map.of(
                    "frequency", 0,
                    "formattedFrequency", "N/A",
                    "message", "Không đủ dữ liệu lịch trình để tính giãn cách"
            ));
        }

        // Sort schedules by departure time
        schedules.sort(Comparator.comparing(Schedules::getDepartureTime));

    

        // Calculate average time difference between consecutive schedules
        long totalDiffMinutes = 0;
        int count = 0;

        for (int i = 1; i < schedules.size(); i++) {
            Date prev = schedules.get(i - 1).getDepartureTime();
            Date curr = schedules.get(i).getDepartureTime();

            if (prev != null && curr != null) {
                // Calculate difference in minutes
                long diffMs = curr.getTime() - prev.getTime();
                long diffMinutes = diffMs / (60 * 1000);


                // Include differences up to and including 120 minutes (2 hours)
                if (diffMinutes > 0 && diffMinutes <= 120) {
                    totalDiffMinutes += diffMinutes;
                    count++;
                }
            }
        }

        // Use the actual calculated value or a default if no valid differences found
        int averageFrequency;
        if (count > 0) {
            averageFrequency = (int) Math.round((double) totalDiffMinutes / count);
        } else {
            // If we didn't find any valid intervals, use the actual time difference
            if (schedules.size() >= 2) {
                Date first = schedules.get(0).getDepartureTime();
                Date last = schedules.get(schedules.size() - 1).getDepartureTime();
                long diffMs = last.getTime() - first.getTime();
                averageFrequency = (int) Math.round(diffMs / (60 * 1000) / (schedules.size() - 1));
            } else {
                averageFrequency = 10; // Default value
            }
        }


        return ResponseEntity.ok(Map.of(
                "frequency", averageFrequency,
                "formattedFrequency", averageFrequency + " phút",
                "scheduleCount", schedules.size()
        ));
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
