package com.pts.pojo;

import java.time.LocalDateTime;

public class Notification {

    private Integer id;
    private Integer userId;
    private String message;
    private String notificationType;
    private Integer relatedEntityId;
    private Boolean isRead;
    private LocalDateTime createdAt;

    // Constructor mặc định
    public Notification() {
        this.isRead = false;
    }

    // Constructor với các tham số cơ bản
    public Notification(Integer userId, String message) {
        this();
        this.userId = userId;
        this.message = message;
    }

    // Constructor đầy đủ
    public Notification(Integer userId, String message, String notificationType, Integer relatedEntityId) {
        this(userId, message);
        this.notificationType = notificationType;
        this.relatedEntityId = relatedEntityId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Integer getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Integer relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Notification{"
                + "id=" + id
                + ", userId=" + userId
                + ", message='" + message + '\''
                + ", isRead=" + isRead
                + '}';
    }
}
