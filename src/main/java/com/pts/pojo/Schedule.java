package com.pts.pojo;

import java.time.LocalTime;
import java.time.LocalDate;

public class Schedule {

    private Integer id;
    private Integer routeId;
    private Integer vehicleId;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String dayOfWeek;
    private Boolean isWeekend;
    private Boolean isHoliday;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;

    // Constructor mặc định
    public Schedule() {
        this.isWeekend = false;
        this.isHoliday = false;
    }

    // Constructor với các tham số cơ bản
    public Schedule(Integer routeId, Integer vehicleId, LocalTime departureTime, LocalTime arrivalTime) {
        this();
        this.routeId = routeId;
        this.vehicleId = vehicleId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Boolean getIsWeekend() {
        return isWeekend;
    }

    public void setIsWeekend(Boolean isWeekend) {
        this.isWeekend = isWeekend;
    }

    public Boolean getIsHoliday() {
        return isHoliday;
    }

    public void setIsHoliday(Boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return "Schedule{"
                + "id=" + id
                + ", routeId=" + routeId
                + ", departureTime=" + departureTime
                + ", arrivalTime=" + arrivalTime
                + '}';
    }
}
