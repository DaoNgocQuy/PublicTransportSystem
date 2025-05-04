package com.pts.pojo;

import java.time.LocalDateTime;

public class Report {

    private Integer id;
    private Integer userId;
    private String location;
    private Float latitude;
    private Float longitude;
    private String description;
    private String imageUrl;
    private String reportType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    // Constructor mặc định
    public Report() {
        this.status = "PENDING";
    }

    // Constructor với các tham số cơ bản
    public Report(Integer userId, String location, String description, String reportType) {
        this();
        this.userId = userId;
        this.location = location;
        this.description = description;
        this.reportType = reportType;
    }

    // Constructor với tọa độ
    public Report(Integer userId, String location, Float latitude, Float longitude,
            String description, String reportType) {
        this(userId, location, description, reportType);
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    @Override
    public String toString() {
        return "Report{"
                + "id=" + id
                + ", userId=" + userId
                + ", location='" + location + '\''
                + ", reportType='" + reportType + '\''
                + ", status='" + status + '\''
                + ", createdAt=" + createdAt
                + '}';
    }
}
