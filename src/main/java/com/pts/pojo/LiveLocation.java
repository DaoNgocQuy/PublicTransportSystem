package com.pts.pojo;

import java.time.LocalDateTime;

public class LiveLocation {

    private Integer id;
    private Integer vehicleId;
    private Float latitude;
    private Float longitude;
    private Integer heading;
    private Float speed;
    private LocalDateTime lastUpdated;

    // Constructor mặc định
    public LiveLocation() {
    }

    // Constructor với các tham số cơ bản
    public LiveLocation(Integer vehicleId, Float latitude, Float longitude) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Constructor đầy đủ
    public LiveLocation(Integer vehicleId, Float latitude, Float longitude, Integer heading, Float speed) {
        this(vehicleId, latitude, longitude);
        this.heading = heading;
        this.speed = speed;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
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

    public Integer getHeading() {
        return heading;
    }

    public void setHeading(Integer heading) {
        this.heading = heading;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "LiveLocation{"
                + "id=" + id
                + ", vehicleId=" + vehicleId
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", speed=" + speed
                + '}';
    }
}
