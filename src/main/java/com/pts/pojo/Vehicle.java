package com.pts.pojo;

public class Vehicle {

    private Integer id;
    private Integer userId;
    private String vehicleName;
    private String type;
    private String licensePlate;
    private Integer capacity;
    private Boolean isAccessible;
    private Boolean isAirConditioned;
    private Integer productionYear;
    private String status;

    // Constructor mặc định
    public Vehicle() {
        this.isAccessible = false;
        this.isAirConditioned = false;
        this.status = "ACTIVE";
    }

    // Constructor với các tham số cơ bản
    public Vehicle(String vehicleName, String type) {
        this();
        this.vehicleName = vehicleName;
        this.type = type;
    }

    // Constructor đầy đủ
    public Vehicle(Integer userId, String vehicleName, String type, String licensePlate, Integer capacity) {
        this(vehicleName, type);
        this.userId = userId;
        this.licensePlate = licensePlate;
        this.capacity = capacity;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getIsAccessible() {
        return isAccessible;
    }

    public void setIsAccessible(Boolean isAccessible) {
        this.isAccessible = isAccessible;
    }

    public Boolean getIsAirConditioned() {
        return isAirConditioned;
    }

    public void setIsAirConditioned(Boolean isAirConditioned) {
        this.isAirConditioned = isAirConditioned;
    }

    public Integer getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(Integer productionYear) {
        this.productionYear = productionYear;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Vehicle{"
                + "id=" + id
                + ", type='" + type + '\''
                + ", vehicleName='" + vehicleName + '\''
                + ", licensePlate='" + licensePlate + '\''
                + ", capacity=" + capacity
                + '}';
    }
}
