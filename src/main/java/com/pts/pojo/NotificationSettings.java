package com.pts.pojo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "notification_settings")
@NamedQueries({
    @NamedQuery(name = "NotificationSettings.findAll", query = "SELECT n FROM NotificationSettings n"),
    @NamedQuery(name = "NotificationSettings.findById", query = "SELECT n FROM NotificationSettings n WHERE n.id = :id"),
    @NamedQuery(name = "NotificationSettings.findByUserId", query = "SELECT n FROM NotificationSettings n WHERE n.userId = :userId"),
    @NamedQuery(name = "NotificationSettings.findByRouteId", query = "SELECT n FROM NotificationSettings n WHERE n.routeId = :routeId"),
    @NamedQuery(name = "NotificationSettings.findByUserAndRoute", query = "SELECT n FROM NotificationSettings n WHERE n.userId = :userId AND n.routeId = :routeId")
})
public class NotificationSettings implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "notify_schedule_changes")
    private Boolean notifyScheduleChanges;
    
    @Column(name = "notify_delays")
    private Boolean notifyDelays;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Users userId;
    
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Routes routeId;

    public NotificationSettings() {
    }

    public NotificationSettings(Integer id) {
        this.id = id;
    }

    // Constructor với các tham số cần thiết
    public NotificationSettings(Users userId, Routes routeId, Boolean notifyScheduleChanges, Boolean notifyDelays) {
        this.userId = userId;
        this.routeId = routeId;
        this.notifyScheduleChanges = notifyScheduleChanges;
        this.notifyDelays = notifyDelays;
        this.createdAt = new Date(); // Thời gian hiện tại
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getNotifyScheduleChanges() {
        return notifyScheduleChanges;
    }

    public void setNotifyScheduleChanges(Boolean notifyScheduleChanges) {
        this.notifyScheduleChanges = notifyScheduleChanges;
    }

    public Boolean getNotifyDelays() {
        return notifyDelays;
    }

    public void setNotifyDelays(Boolean notifyDelays) {
        this.notifyDelays = notifyDelays;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Routes getRouteId() {
        return routeId;
    }

    public void setRouteId(Routes routeId) {
        this.routeId = routeId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof NotificationSettings)) {
            return false;
        }
        NotificationSettings other = (NotificationSettings) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.NotificationSettings[ id=" + id + " ]";
    }
}