package com.pts.pojo;

import java.time.LocalDateTime;

public class RouteSearchHistory {

    private Integer id;
    private Integer userId;
    private String startLocation;
    private String endLocation;
    private LocalDateTime searchDatetime;

    // Constructor mặc định
    public RouteSearchHistory() {
    }

    // Constructor với các tham số cơ bản
    public RouteSearchHistory(Integer userId, String startLocation, String endLocation) {
        this.userId = userId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
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

    public LocalDateTime getSearchDatetime() {
        return searchDatetime;
    }

    public void setSearchDatetime(LocalDateTime searchDatetime) {
        this.searchDatetime = searchDatetime;
    }

    @Override
    public String toString() {
        return "RouteSearchHistory{"
                + "id=" + id
                + ", userId=" + userId
                + ", startLocation='" + startLocation + '\''
                + ", endLocation='" + endLocation + '\''
                + ", searchDatetime=" + searchDatetime
                + '}';
    }
}
