package com.pts.pojo;

import java.time.LocalTime;
import java.time.LocalDateTime;

public class Route {

    private Integer id;
    private String name;
    private Integer routeTypeId;
    private String startLocation;
    private String endLocation;
    private Integer totalStops;
    private LocalTime operationStartTime;
    private LocalTime operationEndTime;
    private Integer frequencyMinutes;
    private String routeColor;
    private Boolean isWalkingRoute;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;

    // Constructor mặc định
    public Route() {
        this.isWalkingRoute = false;
        this.isActive = true;
    }

    // Constructor với các tham số cơ bản
    public Route(String name, String startLocation, String endLocation, Integer totalStops) {
        this();
        this.name = name;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.totalStops = totalStops;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRouteTypeId() {
        return routeTypeId;
    }

    public void setRouteTypeId(Integer routeTypeId) {
        this.routeTypeId = routeTypeId;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public Integer getTotalStops() {
        return totalStops;
    }

    public void setTotalStops(Integer totalStops) {
        this.totalStops = totalStops;
    }

    public LocalTime getOperationStartTime() {
        return operationStartTime;
    }

    public void setOperationStartTime(LocalTime operationStartTime) {
        this.operationStartTime = operationStartTime;
    }

    public LocalTime getOperationEndTime() {
        return operationEndTime;
    }

    public void setOperationEndTime(LocalTime operationEndTime) {
        this.operationEndTime = operationEndTime;
    }

    public Integer getFrequencyMinutes() {
        return frequencyMinutes;
    }

    public void setFrequencyMinutes(Integer frequencyMinutes) {
        this.frequencyMinutes = frequencyMinutes;
    }

    public String getRouteColor() {
        return routeColor;
    }

    public void setRouteColor(String routeColor) {
        this.routeColor = routeColor;
    }

    public Boolean getIsWalkingRoute() {
        return isWalkingRoute;
    }

    public void setIsWalkingRoute(Boolean isWalkingRoute) {
        this.isWalkingRoute = isWalkingRoute;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "Routes{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", startLocation='" + startLocation + '\''
                + ", endLocation='" + endLocation + '\''
                + ", totalStops=" + totalStops
                + ", isWalkingRoute=" + isWalkingRoute
                + '}';
    }
}
