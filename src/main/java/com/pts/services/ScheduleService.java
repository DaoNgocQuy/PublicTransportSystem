package com.pts.services;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

public interface ScheduleService {

    List<Schedules> getAllSchedules();

    Optional<Schedules> getScheduleById(Integer id);

    List<Schedules> getSchedulesByVehicle(Vehicles vehicleId);

    List<Schedules> getSchedulesByRoute(Routes routeId);

    List<Schedules> getSchedulesByTimeRange(Time startTime, Time endTime);

    List<Schedules> findSchedulesByRouteId(Integer routeId);

    Schedules createSchedule(Schedules schedule);

    Schedules updateSchedule(Integer id, Schedules scheduleDetails);

    void deleteSchedule(Integer id);
}
