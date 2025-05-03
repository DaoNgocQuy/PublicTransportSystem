package com.pts.services.impl;

import com.pts.pojo.Schedules;
import com.pts.repositories.ScheduleRepository;
import com.pts.services.ScheduleService;
import com.pts.exceptions.ScheduleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    public Optional<Schedules> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    @Override
    public List<Schedules> getSchedulesByVehicle(Long vehicleId) {
        return scheduleRepository.findByVehicleId(vehicleId);
    }

    @Override
    public List<Schedules> getSchedulesByRoute(Long routeId) {
        return scheduleRepository.findByRouteId(routeId);
    }

    @Override
    public List<Schedules> getSchedulesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return scheduleRepository.findByDepartureTimeBetween(startTime, endTime);
    }

    @Override
    public List<Schedules> getSchedulesByStatus(String status) {
        return scheduleRepository.findByStatus(status);
    }

    @Override
    public List<Schedules> getSchedulesByVehicleAndStatus(Long vehicleId, String status) {
        return scheduleRepository.findByVehicleIdAndStatus(vehicleId, status);
    }

    @Override
    public List<Schedules> getSchedulesByRouteAndStatus(Long routeId, String status) {
        return scheduleRepository.findByRouteIdAndStatus(routeId, status);
    }

    @Override
    public List<Schedules> getSchedulesByTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime,
            String status) {
        return scheduleRepository.findByTimeRangeAndStatus(startTime, endTime, status);
    }

    @Override
    public List<Schedules> getSchedulesByVehicleAndTimeRange(Long vehicleId, LocalDateTime startTime,
            LocalDateTime endTime) {
        return scheduleRepository.findByVehicleAndTimeRange(vehicleId, startTime, endTime);
    }

    @Override
    @Transactional
    public Schedules createSchedule(Schedules schedule) {
        validateSchedule(schedule);
        return scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public Schedules updateSchedule(Long id, Schedules scheduleDetails) {
        Schedules schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleException("Schedules not found with id: " + id));

        schedule.setVehicleId(scheduleDetails.getVehicleId());
        schedule.setRouteId(scheduleDetails.getRouteId());
        schedule.setDepartureTime(scheduleDetails.getDepartureTime());
        schedule.setArrivalTime(scheduleDetails.getArrivalTime());
        schedule.setStatus(scheduleDetails.getStatus());

        validateSchedule(schedule);

        return scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ScheduleException("Schedules not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    private void validateSchedule(Schedules schedule) {
        if (schedule.getDepartureTime().isAfter(schedule.getArrivalTime())) {
            throw new ScheduleException("Departure time must be before arrival time");
        }

        List<Schedules> existingSchedules = scheduleRepository.findByVehicleId(schedule.getVehicleId());
        for (Schedules existing : existingSchedules) {
            if (existing.getId().equals(schedule.getId()))
                continue;

            if ((schedule.getDepartureTime().isBefore(existing.getArrivalTime()) &&
                    schedule.getArrivalTime().isAfter(existing.getDepartureTime()))) {
                throw new ScheduleException("Schedules overlaps with existing schedule for this vehicle");
            }
        }
    }
}