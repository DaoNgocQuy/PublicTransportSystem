package com.pts.repositories;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    List<Schedules> findAll();
    List<Schedules> findAllWithPagination(int offset, int limit);
    int countAll();
    
    Optional<Schedules> findById(Integer id);
    Schedules save(Schedules schedule);
    void deleteById(Integer id);
    boolean existsById(Integer id);
    
    List<Schedules> findByVehicleId(Vehicles vehicleId);
    List<Schedules> findByVehicleIdWithPagination(Vehicles vehicleId, int offset, int limit);
    int countByVehicleId(Vehicles vehicleId);
    
    List<Schedules> findByRouteId(Routes routeId);
    List<Schedules> findByRouteIdWithPagination(Routes routeId, int offset, int limit);
    int countByRouteId(Routes routeId);
    
    List<Schedules> findByRouteId(Integer routeId);
    List<Schedules> findByRouteIdWithPagination(Integer routeId, int offset, int limit);
    int countByRouteId(Integer routeId);
    
    List<Schedules> findByDepartureTimeBetween(Time startTime, Time endTime);
    List<Schedules> findByDepartureTimeBetweenWithPagination(Time startTime, Time endTime, int offset, int limit);
    int countByDepartureTimeBetween(Time startTime, Time endTime);
}