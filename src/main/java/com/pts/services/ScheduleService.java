package com.pts.services;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ScheduleService {

    List<Schedules> getAllSchedules();
    
    Map<String, Object> getSchedulesWithPagination(int page, int size);

    Optional<Schedules> getScheduleById(Integer id);

    List<Schedules> getSchedulesByVehicle(Vehicles vehicleId);
    
    Map<String, Object> getSchedulesByVehicleWithPagination(Vehicles vehicleId, int page, int size);

    List<Schedules> getSchedulesByRoute(Routes routeId);
    
    Map<String, Object> getSchedulesByRouteWithPagination(Routes routeId, int page, int size);

    List<Schedules> getSchedulesByTimeRange(Time startTime, Time endTime);
    
    Map<String, Object> getSchedulesByTimeRangeWithPagination(Time startTime, Time endTime, int page, int size);

    List<Schedules> findSchedulesByRouteId(Integer routeId);
    
    Map<String, Object> findSchedulesByRouteIdWithPagination(Integer routeId, int page, int size);

    Schedules createSchedule(Schedules schedule);

    Schedules updateSchedule(Integer id, Schedules scheduleDetails);

    void deleteSchedule(Integer id);
}
