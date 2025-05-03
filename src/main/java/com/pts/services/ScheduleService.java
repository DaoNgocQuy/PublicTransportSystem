package com.pts.services;

import com.pts.pojo.Schedules;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleService {
    List<Schedules> getAllSchedules();

    Optional<Schedules> getScheduleById(Long id);

    List<Schedules> getSchedulesByVehicle(Long vehicleId);

    List<Schedules> getSchedulesByRoute(Long routeId);

    List<Schedules> getSchedulesByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    List<Schedules> getSchedulesByStatus(String status);

    List<Schedules> getSchedulesByVehicleAndStatus(Long vehicleId, String status);

    List<Schedules> getSchedulesByRouteAndStatus(Long routeId, String status);

    List<Schedules> getSchedulesByTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime, String status);

    List<Schedules> getSchedulesByVehicleAndTimeRange(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);

    Schedules createSchedule(Schedules schedule);

    Schedules updateSchedule(Long id, Schedules scheduleDetails);

    void deleteSchedule(Long id);
}