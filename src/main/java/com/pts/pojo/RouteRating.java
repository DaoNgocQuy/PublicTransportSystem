package com.pts.pojo;

import java.time.LocalDateTime;

public class RouteRating {

    private Integer id;
    private Integer userId;
    private Integer routeId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    // Constructor mặc định
    public RouteRating() {
    }

    // Constructor với các tham số cơ bản
    public RouteRating(Integer userId, Integer routeId, Integer rating) {
        this.userId = userId;
        this.routeId = routeId;
        this.rating = rating;
    }

    // Constructor với comment
    public RouteRating(Integer userId, Integer routeId, Integer rating, String comment) {
        this(userId, routeId, rating);
        this.comment = comment;
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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        if (rating < 1) {
            rating = 1;
        }
        if (rating > 5) {
            rating = 5;
        }
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "RouteRating{"
                + "id=" + id
                + ", userId=" + userId
                + ", routeId=" + routeId
                + ", rating=" + rating
                + '}';
    }
}
