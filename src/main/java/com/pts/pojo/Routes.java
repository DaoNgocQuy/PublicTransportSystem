/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author LEGION
 */
@Entity
@Table(name = "routes")
@NamedQueries({
    @NamedQuery(name = "Routes.findAll", query = "SELECT r FROM Routes r ORDER BY r.routeName"),
    @NamedQuery(name = "Routes.findById", query = "SELECT r FROM Routes r WHERE r.id = :id"),
    @NamedQuery(name = "Routes.findByRouteName", query = "SELECT r FROM Routes r WHERE r.routeName = :routeName"),
    @NamedQuery(name = "Routes.findByStartLocation", query = "SELECT r FROM Routes r WHERE r.startLocation = :startLocation"),
    @NamedQuery(name = "Routes.findByEndLocation", query = "SELECT r FROM Routes r WHERE r.endLocation = :endLocation"),
    @NamedQuery(name = "Routes.findByTotalStops", query = "SELECT r FROM Routes r WHERE r.totalStops = :totalStops"),
    @NamedQuery(name = "Routes.findByIsActive", query = "SELECT r FROM Routes r WHERE r.isActive = :isActive")
})
public class Routes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name")
    private String routeName;
    @Size(max = 255)
    @Column(name = "start_location")
    private String startLocation;
    @Size(max = 255)
    @Column(name = "end_location")
    private String endLocation;
    @Column(name = "total_stops")
    private Integer totalStops;
    @Column(name = "frequency_minutes")
    private Integer frequencyMinutes;
    @Column(name = "operation_start_time")
    @Temporal(TemporalType.TIME)
    private Date startTime;
    @Column(name = "operation_end_time")
    @Temporal(TemporalType.TIME)
    private Date endTime;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "route_type_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    @JsonIgnore
    private RouteTypes routeTypeId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "route")
    @JsonIgnore
    private Set<RouteStop> routeStopSet;

    public Routes() {
    }

    public Routes(Integer id) {
        this.id = id;
    }

    public Routes(Integer id, String routeName) {
        this.id = id;
        this.routeName = routeName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Integer getFrequencyMinutes() {
        return frequencyMinutes;
    }

    public void setFrequencyMinutes(Integer frequencyMinutes) {
        this.frequencyMinutes = frequencyMinutes;
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

    public Integer getTotalStops() {
        return totalStops;
    }

    public void setTotalStops(Integer totalStops) {
        this.totalStops = totalStops;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public RouteTypes getRouteTypeId() {
        return routeTypeId;
    }

    public void setRouteTypeId(RouteTypes routeTypeId) {
        this.routeTypeId = routeTypeId;
    }

    public Set<RouteStop> getRouteStopSet() {
        return routeStopSet;
    }

    public void setRouteStopSet(Set<RouteStop> routeStopSet) {
        this.routeStopSet = routeStopSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Routes)) {
            return false;
        }
        Routes other = (Routes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.Routes[ id=" + id + " ]";
    }
}
