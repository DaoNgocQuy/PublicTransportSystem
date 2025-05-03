package com.pts.services.impl;

import com.pts.pojo.Schedule;
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
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    @Override
    public List<Schedule> getSchedulesByVehicle(Long vehicleId) {
        return scheduleRepository.findByVehicleId(vehicleId);
    }

    @Override
    public List<Schedule> getSchedulesByRoute(Long routeId) {
        return scheduleRepository.findByRouteId(routeId);
    }

    @Override
    public List<Schedule> getSchedulesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return scheduleRepository.findByDepartureTimeBetween(startTime, endTime);
    }

    @Override
    public List<Schedule> getSchedulesByStatus(String status) {
        return scheduleRepository.findByStatus(status);
    }

    @Override
    public List<Schedule> getSchedulesByVehicleAndStatus(Long vehicleId, String status) {
        return scheduleRepository.findByVehicleIdAndStatus(vehicleId, status);
    }

    @Override
    public List<Schedule> getSchedulesByRouteAndStatus(Long routeId, String status) {
        return scheduleRepository.findByRouteIdAndStatus(routeId, status);
    }

    @Override
    public List<Schedule> getSchedulesByTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime,
            String status) {
        return scheduleRepository.findByTimeRangeAndStatus(startTime, endTime, status);
    }

    @Override
    public List<Schedule> getSchedulesByVehicleAndTimeRange(Long vehicleId, LocalDateTime startTime,
            LocalDateTime endTime) {
        return scheduleRepository.findByVehicleAndTimeRange(vehicleId, startTime, endTime);
    }

    @Override
    @Transactional
    public Schedule createSchedule(Schedule schedule) {
        validateSchedule(schedule);
        return scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public Schedule updateSchedule(Long id, Schedule scheduleDetails) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleException("Schedule not found with id: " + id));

        schedule.setVehicle(scheduleDetails.getVehicle());
        schedule.setRoute(scheduleDetails.getRoute());
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
            throw new ScheduleException("Schedule not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    private void validateSchedule(Schedule schedule) {
        if (schedule.getDepartureTime().isAfter(schedule.getArrivalTime())) {
            throw new ScheduleException("Departure time must be before arrival time");
        }

        List<Schedule> existingSchedules = scheduleRepository.findByVehicleId(schedule.getVehicle().getId());
        for (Schedule existing : existingSchedules) {
            if (existing.getId().equals(schedule.getId()))
                continue;

            if ((schedule.getDepartureTime().isBefore(existing.getArrivalTime()) &&
                    schedule.getArrivalTime().isAfter(existing.getDepartureTime()))) {
                throw new ScheduleException("Schedule overlaps with existing schedule for this vehicle");
            }
        }
    }
}