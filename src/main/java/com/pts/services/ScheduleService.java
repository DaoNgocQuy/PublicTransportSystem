package com.pts.services;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ScheduleService {
    List<Schedules> getAllSchedules();

    Optional<Schedules> getScheduleById(Long id);

    List<Schedules> getSchedulesByVehicle(Vehicles vehicleId);

    List<Schedules> getSchedulesByRoute(Routes routeId);

    List<Schedules> getSchedulesByTimeRange(Date startTime, Date endTime);

    List<Schedules> getSchedulesByStatus(String status);

    List<Schedules> getSchedulesByVehicleAndStatus(Vehicles vehicleId, String status);

    List<Schedules> getSchedulesByRouteAndStatus(Routes routeId, String status);

    List<Schedules> getSchedulesByTimeRangeAndStatus(Date startTime, Date endTime, String status);

    List<Schedules> getSchedulesByVehicleAndTimeRange(Vehicles vehicleId, Date startTime, Date endTime);

    Schedules createSchedule(Schedules schedule);

    Schedules updateSchedule(Long id, Schedules scheduleDetails);

    void deleteSchedule(Long id);
}