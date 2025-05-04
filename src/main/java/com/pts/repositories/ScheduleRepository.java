package com.pts.repositories;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Route;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    List<Schedules> findAll();
    Optional<Schedules> findById(Integer id);
    Schedules save(Schedules schedule);
    void deleteById(Integer id);
    boolean existsById(Integer id);
    
    List<Schedules> findByVehicleId(Vehicles vehicleId);
    List<Schedules> findByRouteId(Route routeId);
    List<Schedules> findByDepartureTimeBetween(Time startTime, Time endTime);
} 