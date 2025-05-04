package com.pts.pojo;

import java.time.LocalDateTime;

public class TrafficCondition {

    private Integer id;
    private Integer routeSegmentId;
    private String conditionType;
    private Integer reportedBy;
    private LocalDateTime reportedAt;
    private Integer estimatedDelayMinutes;

    // Constructor mặc định
    public TrafficCondition() {
        this.conditionType = "NORMAL";
    }

    // Constructor với các tham số cơ bản
    public TrafficCondition(Integer routeSegmentId, String conditionType, Integer reportedBy) {
        this();
        this.routeSegmentId = routeSegmentId;
        this.conditionType = conditionType;
        this.reportedBy = reportedBy;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRouteSegmentId() {
        return routeSegmentId;
    }

    public void setRouteSegmentId(Integer routeSegmentId) {
        this.routeSegmentId = routeSegmentId;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public Integer getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Integer reportedBy) {
        this.reportedBy = reportedBy;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public Integer getEstimatedDelayMinutes() {
        return estimatedDelayMinutes;
    }

    public void setEstimatedDelayMinutes(Integer estimatedDelayMinutes) {
        this.estimatedDelayMinutes = estimatedDelayMinutes;
    }

    @Override
    public String toString() {
        return "TrafficCondition{"
                + "id=" + id
                + ", routeSegmentId=" + routeSegmentId
                + ", conditionType='" + conditionType + '\''
                + ", estimatedDelayMinutes=" + estimatedDelayMinutes
                + ", reportedAt=" + reportedAt
                + '}';
    }
}
