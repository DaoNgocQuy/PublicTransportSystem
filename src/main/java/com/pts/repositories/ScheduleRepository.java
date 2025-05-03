package com.pts.repositories;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    List<Schedules> findAll();
    Optional<Schedules> findById(Long id);
    Schedules save(Schedules schedule);
    void deleteById(Long id);
    boolean existsById(Long id);
    
    List<Schedules> findByVehicleId(Vehicles vehicleId);
    List<Schedules> findByRouteId(Routes routeId);
    List<Schedules> findByDepartureTimeBetween(Date startTime, Date endTime);
    List<Schedules> findByStatus(String status);
    List<Schedules> findByVehicleIdAndStatus(Vehicles vehicleId, String status);
    List<Schedules> findByRouteIdAndStatus(Routes routeId, String status);
    List<Schedules> findByTimeRangeAndStatus(Date startTime, Date endTime, String status);
    List<Schedules> findByVehicleAndTimeRange(Vehicles vehicleId, Date startTime, Date endTime);
} 