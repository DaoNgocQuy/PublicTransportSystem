package com.pts.services.impl;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.repositories.ScheduleRepository;
import com.pts.services.ScheduleService;
import com.pts.exceptions.ScheduleException;
import com.pts.pojo.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    public List<Schedules> getAllSchedules() {
        return scheduleRepository.findAll();
    }
      @Override
    public Map<String, Object> getSchedulesWithPagination(int page, int size) {
        Map<String, Object> result = new HashMap<>();
        int offset = page * size;
        
        List<Schedules> schedules = scheduleRepository.findAllWithPagination(offset, size);
        int totalItems = scheduleRepository.countAll();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        System.out.println("Pagination debug:");
        System.out.println("page=" + page + ", size=" + size + ", offset=" + offset);
        System.out.println("totalItems=" + totalItems + ", totalPages=" + totalPages);
        System.out.println("schedules.size()=" + schedules.size());
        
        result.put("schedules", schedules);
        result.put("currentPage", page);
        result.put("totalItems", totalItems);
        result.put("totalPages", totalPages);
        
        return result;
    }

    @Override
    public Optional<Schedules> getScheduleById(Integer id) {
        return scheduleRepository.findById(id);
    }

    @Override
    public List<Schedules> getSchedulesByVehicle(Vehicles vehicleId) {
        return scheduleRepository.findByVehicleId(vehicleId);
    }
    
    @Override
    public Map<String, Object> getSchedulesByVehicleWithPagination(Vehicles vehicleId, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        int offset = page * size;
        
        List<Schedules> schedules = scheduleRepository.findByVehicleIdWithPagination(vehicleId, offset, size);
        int totalItems = scheduleRepository.countByVehicleId(vehicleId);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        result.put("schedules", schedules);
        result.put("currentPage", page);
        result.put("totalItems", totalItems);
        result.put("totalPages", totalPages);
        
        return result;
    }

    @Override
    public List<Schedules> getSchedulesByRoute(Routes routeId) {
        return scheduleRepository.findByRouteId(routeId);
    }
    
    @Override
    public Map<String, Object> getSchedulesByRouteWithPagination(Routes routeId, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        int offset = page * size;
        
        List<Schedules> schedules = scheduleRepository.findByRouteIdWithPagination(routeId, offset, size);
        int totalItems = scheduleRepository.countByRouteId(routeId);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        result.put("schedules", schedules);
        result.put("currentPage", page);
        result.put("totalItems", totalItems);
        result.put("totalPages", totalPages);
        
        return result;
    }

    @Override
    public List<Schedules> getSchedulesByTimeRange(Time startTime, Time endTime) {
        return scheduleRepository.findByDepartureTimeBetween(startTime, endTime);
    }
    
    @Override
    public Map<String, Object> getSchedulesByTimeRangeWithPagination(Time startTime, Time endTime, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        int offset = page * size;
        
        List<Schedules> schedules = scheduleRepository.findByDepartureTimeBetweenWithPagination(startTime, endTime, offset, size);
        int totalItems = scheduleRepository.countByDepartureTimeBetween(startTime, endTime);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        result.put("schedules", schedules);
        result.put("currentPage", page);
        result.put("totalItems", totalItems);
        result.put("totalPages", totalPages);
        
        return result;
    }

    @Override
    public List<Schedules> findSchedulesByRouteId(Integer routeId) {
        return scheduleRepository.findByRouteId(routeId);
    }
    
    @Override
    public Map<String, Object> findSchedulesByRouteIdWithPagination(Integer routeId, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        int offset = page * size;
        
        List<Schedules> schedules = scheduleRepository.findByRouteIdWithPagination(routeId, offset, size);
        int totalItems = scheduleRepository.countByRouteId(routeId);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        result.put("schedules", schedules);
        result.put("currentPage", page);
        result.put("totalItems", totalItems);
        result.put("totalPages", totalPages);
        
        return result;
    }

    @Override
    @Transactional
    public Schedules createSchedule(Schedules schedule) {
        validateSchedule(schedule);
        return scheduleRepository.save(schedule);
    }    @Override
    @Transactional
    public Schedules updateSchedule(Integer id, Schedules scheduleDetails) {
        Schedules schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleException("Schedule not found with id: " + id));

        // Lưu thông tin createdAt trước khi cập nhật
        Date createdAt = schedule.getCreatedAt();

        // Cập nhật các trường thông tin
        schedule.setVehicleId(scheduleDetails.getVehicleId());
        schedule.setRouteId(scheduleDetails.getRouteId());
        schedule.setDepartureTime(scheduleDetails.getDepartureTime());
        schedule.setArrivalTime(scheduleDetails.getArrivalTime());
        
        // Giữ lại thông tin createdAt
        schedule.setCreatedAt(createdAt);

        validateSchedule(schedule);

        return scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Integer id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ScheduleException("Schedule not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    private void validateSchedule(Schedules schedule) {
        if (schedule.getDepartureTime().after(schedule.getArrivalTime())) {
            throw new ScheduleException("Departure time must be before arrival time");
        }

        List<Schedules> existingSchedules = scheduleRepository.findByVehicleId(schedule.getVehicleId());
        for (Schedules existing : existingSchedules) {
            if (existing.getId().equals(schedule.getId())) {
                continue;
            }

            if ((schedule.getDepartureTime().before(existing.getArrivalTime())
                    && schedule.getArrivalTime().after(existing.getDepartureTime()))) {
                throw new ScheduleException("Schedule overlaps with existing schedule for this vehicle");
            }
        }
    }

    public List<Schedules> getSchedulesByTimeRange(Date startTime, Date endTime) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public List<Schedules> getSchedulesByStatus(String status) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public List<Schedules> getSchedulesByVehicleAndStatus(Vehicles vehicleId, String status) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public List<Schedules> getSchedulesByRouteAndStatus(Routes routeId, String status) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public List<Schedules> getSchedulesByTimeRangeAndStatus(Date startTime, Date endTime, String status) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public List<Schedules> getSchedulesByVehicleAndTimeRange(Vehicles vehicleId, Date startTime, Date endTime) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public Schedules updateSchedule(Long id, Schedules scheduleDetails) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void deleteSchedule(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
