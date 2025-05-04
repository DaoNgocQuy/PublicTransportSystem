package com.pts.pojo;

public class RouteSegment {

    private Integer id;
    private Integer routeId;
    private Integer fromStopId;
    private Integer toStopId;
    private Float distanceKm;
    private Integer averageTimeMinutes;

    // Constructor mặc định
    public RouteSegment() {
    }

    // Constructor với các tham số cơ bản
    public RouteSegment(Integer routeId, Integer fromStopId, Integer toStopId) {
        this.routeId = routeId;
        this.fromStopId = fromStopId;
        this.toStopId = toStopId;
    }

    // Constructor đầy đủ
    public RouteSegment(Integer routeId, Integer fromStopId, Integer toStopId,
            Float distanceKm, Integer averageTimeMinutes) {
        this(routeId, fromStopId, toStopId);
        this.distanceKm = distanceKm;
        this.averageTimeMinutes = averageTimeMinutes;
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

    public Integer getFromStopId() {
        return fromStopId;
    }

    public void setFromStopId(Integer fromStopId) {
        this.fromStopId = fromStopId;
    }

    public Integer getToStopId() {
        return toStopId;
    }

    public void setToStopId(Integer toStopId) {
        this.toStopId = toStopId;
    }

    public Float getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Float distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getAverageTimeMinutes() {
        return averageTimeMinutes;
    }

    public void setAverageTimeMinutes(Integer averageTimeMinutes) {
        this.averageTimeMinutes = averageTimeMinutes;
    }

    @Override
    public String toString() {
        return "RouteSegment{"
                + "id=" + id
                + ", routeId=" + routeId
                + ", fromStopId=" + fromStopId
                + ", toStopId=" + toStopId
                + ", distanceKm=" + distanceKm
                + ", averageTimeMinutes=" + averageTimeMinutes
                + '}';
    }
}
