package com.pts.pojo;

import java.time.LocalDateTime;

public class Favorite {

    private Integer id;
    private Integer userId;
    private Integer routeId;
    private LocalDateTime createdAt;

    // Constructor mặc định
    public Favorite() {
    }

    // Constructor với các tham số cơ bản
    public Favorite(Integer userId, Integer routeId) {
        this.userId = userId;
        this.routeId = routeId;
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

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Favorite{"
                + "id=" + id
                + ", userId=" + userId
                + ", routeId=" + routeId
                + '}';
    }
}
