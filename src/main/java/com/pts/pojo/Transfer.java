package com.pts.pojo;

public class Transfer {

    private Integer id;
    private Integer fromRouteId;
    private Integer toRouteId;
    private Integer transferStopId;
    private Integer transferTimeMinutes;
    private Integer distanceMeters;
    private Boolean isAccessible;

    // Constructor mặc định
    public Transfer() {
        this.transferTimeMinutes = 5;
        this.isAccessible = true;
    }

    // Constructor với các tham số cơ bản
    public Transfer(Integer fromRouteId, Integer toRouteId, Integer transferStopId) {
        this();
        this.fromRouteId = fromRouteId;
        this.toRouteId = toRouteId;
        this.transferStopId = transferStopId;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromRouteId() {
        return fromRouteId;
    }

    public void setFromRouteId(Integer fromRouteId) {
        this.fromRouteId = fromRouteId;
    }

    public Integer getToRouteId() {
        return toRouteId;
    }

    public void setToRouteId(Integer toRouteId) {
        this.toRouteId = toRouteId;
    }

    public Integer getTransferStopId() {
        return transferStopId;
    }

    public void setTransferStopId(Integer transferStopId) {
        this.transferStopId = transferStopId;
    }

    public Integer getTransferTimeMinutes() {
        return transferTimeMinutes;
    }

    public void setTransferTimeMinutes(Integer transferTimeMinutes) {
        this.transferTimeMinutes = transferTimeMinutes;
    }

    public Integer getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Integer distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Boolean getIsAccessible() {
        return isAccessible;
    }

    public void setIsAccessible(Boolean isAccessible) {
        this.isAccessible = isAccessible;
    }

    @Override
    public String toString() {
        return "Transfer{"
                + "id=" + id
                + ", fromRouteId=" + fromRouteId
                + ", toRouteId=" + toRouteId
                + ", transferStopId=" + transferStopId
                + ", transferTimeMinutes=" + transferTimeMinutes
                + '}';
    }
}
