package com.pts.pojo;

public class Stop {

    private Integer id;
    private Integer routeId;
    private String stopName;
    private Float latitude;
    private Float longitude;
    private Integer stopOrder;
    private String address;
    private Boolean hasShelter;
    private Boolean isAccessible;

    // Constructor mặc định
    public Stop() {
        this.hasShelter = false;
        this.isAccessible = true;
    }

    // Constructor với các tham số cơ bản
    public Stop(Integer routeId, String stopName, Integer stopOrder) {
        this();
        this.routeId = routeId;
        this.stopName = stopName;
        this.stopOrder = stopOrder;
    }

    // Constructor với thêm tọa độ
    public Stop(Integer routeId, String stopName, Float latitude, Float longitude, Integer stopOrder) {
        this(routeId, stopName, stopOrder);
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getHasShelter() {
        return hasShelter;
    }

    public void setHasShelter(Boolean hasShelter) {
        this.hasShelter = hasShelter;
    }

    public Boolean getIsAccessible() {
        return isAccessible;
    }

    public void setIsAccessible(Boolean isAccessible) {
        this.isAccessible = isAccessible;
    }

    @Override
    public String toString() {
        return "Stop{"
                + "id=" + id
                + ", routeId=" + routeId
                + ", stopName='" + stopName + '\''
                + ", stopOrder=" + stopOrder
                + '}';
    }
}
