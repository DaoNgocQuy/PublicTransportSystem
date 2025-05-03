package com.pts.repositories;

import com.pts.pojo.Schedules;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    List<Schedules> findAll();
    Optional<Schedules> findById(Long id);
    Schedules save(Schedules schedule);
    void deleteById(Long id);
    boolean existsById(Long id);
    
    List<Schedules> findByVehicleId(Long vehicleId);
    List<Schedules> findByRouteId(Long routeId);
    List<Schedules> findByDepartureTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<Schedules> findByStatus(String status);
    List<Schedules> findByVehicleIdAndStatus(Long vehicleId, String status);
    List<Schedules> findByRouteIdAndStatus(Long routeId, String status);
    List<Schedules> findByTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime, String status);
    List<Schedules> findByVehicleAndTimeRange(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);
} 