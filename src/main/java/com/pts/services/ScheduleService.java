package com.pts.services;

import com.pts.pojo.Schedule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleService {
    List<Schedule> getAllSchedules();
    Optional<Schedule> getScheduleById(Long id);
    List<Schedule> getSchedulesByVehicle(Long vehicleId);
    List<Schedule> getSchedulesByRoute(Long routeId);
    List<Schedule> getSchedulesByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    List<Schedule> getSchedulesByStatus(String status);
    List<Schedule> getSchedulesByVehicleAndStatus(Long vehicleId, String status);
    List<Schedule> getSchedulesByRouteAndStatus(Long routeId, String status);
    List<Schedule> getSchedulesByTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime, String status);
    List<Schedule> getSchedulesByVehicleAndTimeRange(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);
    Schedule createSchedule(Schedule schedule);
    Schedule updateSchedule(Long id, Schedule scheduleDetails);
    void deleteSchedule(Long id);
} 