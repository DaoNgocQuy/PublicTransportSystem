package com.pts.repositories;

import com.pts.pojo.Schedule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    List<Schedule> findAll();
    Optional<Schedule> findById(Long id);
    Schedule save(Schedule schedule);
    void deleteById(Long id);
    boolean existsById(Long id);
    
    List<Schedule> findByVehicleId(Long vehicleId);
    List<Schedule> findByRouteId(Long routeId);
    List<Schedule> findByDepartureTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<Schedule> findByStatus(String status);
    List<Schedule> findByVehicleIdAndStatus(Long vehicleId, String status);
    List<Schedule> findByRouteIdAndStatus(Long routeId, String status);
    List<Schedule> findByTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime, String status);
    List<Schedule> findByVehicleAndTimeRange(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);
} 