/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.pojo;

import jakarta.persistence.Basic;
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
import java.util.Collection;
import java.util.Date;
import java.sql.Time;

/**
 *
 * @author LEGION
 */
@Entity
@Table(name = "routes")
@NamedQueries({
    @NamedQuery(name = "Routes.findAll", query = "SELECT r FROM Routes r"),
    @NamedQuery(name = "Routes.findById", query = "SELECT r FROM Routes r WHERE r.id = :id"),
    @NamedQuery(name = "Routes.findByName", query = "SELECT r FROM Routes r WHERE r.name = :name"),
    @NamedQuery(name = "Routes.findByStartLocation", query = "SELECT r FROM Routes r WHERE r.startLocation = :startLocation"),
    @NamedQuery(name = "Routes.findByEndLocation", query = "SELECT r FROM Routes r WHERE r.endLocation = :endLocation"),
    @NamedQuery(name = "Routes.findByTotalStops", query = "SELECT r FROM Routes r WHERE r.totalStops = :totalStops"),
    @NamedQuery(name = "Routes.findByOperationStartTime", query = "SELECT r FROM Routes r WHERE r.operationStartTime = :operationStartTime"),
    @NamedQuery(name = "Routes.findByOperationEndTime", query = "SELECT r FROM Routes r WHERE r.operationEndTime = :operationEndTime"),
    @NamedQuery(name = "Routes.findByFrequencyMinutes", query = "SELECT r FROM Routes r WHERE r.frequencyMinutes = :frequencyMinutes"),
    @NamedQuery(name = "Routes.findByIsWalkingRoute", query = "SELECT r FROM Routes r WHERE r.isWalkingRoute = :isWalkingRoute"),
    @NamedQuery(name = "Routes.findByIsActive", query = "SELECT r FROM Routes r WHERE r.isActive = :isActive"),
    @NamedQuery(name = "Routes.findByCreatedAt", query = "SELECT r FROM Routes r WHERE r.createdAt = :createdAt"),
    @NamedQuery(name = "Routes.findByLastUpdated", query = "SELECT r FROM Routes r WHERE r.lastUpdated = :lastUpdated")})
public class Routes implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name")
    private String name;
    @Size(max = 255)
    @Column(name = "start_location")
    private String startLocation;
    @Size(max = 255)
    @Column(name = "end_location")
    private String endLocation;
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "total_stops")
    private Integer totalStops;
    @Column(name = "operation_start_time")
    @Temporal(TemporalType.TIME)
    private Date operationStartTime;
    @Column(name = "operation_end_time")
    @Temporal(TemporalType.TIME)
    private Date operationEndTime;
    @Column(name = "frequency_minutes")
    private Integer frequencyMinutes;
    @Column(name = "is_walking_route")
    private Boolean isWalkingRoute;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;
    @OneToMany(mappedBy = "routeId")
    private Collection<Favorites> favoritesCollection;
    @OneToMany(mappedBy = "routeId")
    private Collection<RouteSegments> routeSegmentsCollection;
    @JoinColumn(name = "route_type_id", referencedColumnName = "id")
    @ManyToOne
    private RouteTypes routeTypeId;
    @OneToMany(mappedBy = "fromRouteId")
    private Collection<Transfers> transfersCollection;
    @OneToMany(mappedBy = "toRouteId")
    private Collection<Transfers> transfersCollection1;
    @OneToMany(mappedBy = "routeId")
    private Collection<Schedules> schedulesCollection;
    @OneToMany(mappedBy = "routeId")
    private Collection<Stops> stopsCollection;
    @OneToMany(mappedBy = "routeId")
    private Collection<RouteRatings> routeRatingsCollection;

    public Routes() {
    }

    public Routes(Integer id) {
        this.id = id;
    }

    public Routes(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getOperationStartTime() {
        return operationStartTime;
    }

    public void setOperationStartTime(Time time) {
        if (time != null) {
            this.operationStartTime = new Date(time.getTime());
        } else {
            this.operationStartTime = null;
        }
    }

    public void setOperationEndTime(Time time) {
        if (time != null) {
            this.operationEndTime = new Date(time.getTime());
        } else {
            this.operationEndTime = null;
        }
    }

    public Date getOperationEndTime() {
        return operationEndTime;
    }

    public void setOperationEndTime(Date operationEndTime) {
        this.operationEndTime = operationEndTime;
    }

    public Integer getFrequencyMinutes() {
        return frequencyMinutes;
    }

    public void setFrequencyMinutes(Integer frequencyMinutes) {
        this.frequencyMinutes = frequencyMinutes;
    }

    

    public Boolean getIsWalkingRoute() {
        return isWalkingRoute;
    }

    public void setIsWalkingRoute(Boolean isWalkingRoute) {
        this.isWalkingRoute = isWalkingRoute;
    }

    public Boolean getActive() {
        return this.isActive;
    }

    public void setActive(Boolean active) {
        this.isActive = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Collection<Favorites> getFavoritesCollection() {
        return favoritesCollection;
    }

    public void setFavoritesCollection(Collection<Favorites> favoritesCollection) {
        this.favoritesCollection = favoritesCollection;
    }

    public Collection<RouteSegments> getRouteSegmentsCollection() {
        return routeSegmentsCollection;
    }

    public void setRouteSegmentsCollection(Collection<RouteSegments> routeSegmentsCollection) {
        this.routeSegmentsCollection = routeSegmentsCollection;
    }

    public RouteTypes getRouteType() {
        return this.routeTypeId;
    }

    public void setRouteType(RouteTypes routeType) {
        this.routeTypeId = routeType;
    }

    public Integer getRouteTypeIdValue() {
        return (this.routeTypeId != null) ? this.routeTypeId.getId() : null;
    }

    public void setRouteTypeIdValue(Integer id) {
        if (id != null) {
            if (this.routeTypeId == null) {
                this.routeTypeId = new RouteTypes(id);
            } else {
                this.routeTypeId.setId(id);
            }
        } else {
            this.routeTypeId = null;
        }
    }

    public String getRouteTypeName() {
        return (this.routeTypeId != null) ? this.routeTypeId.getTypeName() : null;
    }

    public String getRouteTypeColor() {
        return (this.routeTypeId != null) ? this.routeTypeId.getColorCode() : null;
    }

    public Collection<Transfers> getTransfersCollection() {
        return transfersCollection;
    }

    public void setTransfersCollection(Collection<Transfers> transfersCollection) {
        this.transfersCollection = transfersCollection;
    }

    public Collection<Transfers> getTransfersCollection1() {
        return transfersCollection1;
    }

    public void setTransfersCollection1(Collection<Transfers> transfersCollection1) {
        this.transfersCollection1 = transfersCollection1;
    }

    public Collection<Schedules> getSchedulesCollection() {
        return schedulesCollection;
    }

    public void setSchedulesCollection(Collection<Schedules> schedulesCollection) {
        this.schedulesCollection = schedulesCollection;
    }

    public Collection<Stops> getStopsCollection() {
        return stopsCollection;
    }

    public void setStopsCollection(Collection<Stops> stopsCollection) {
        this.stopsCollection = stopsCollection;
    }

    public Collection<RouteRatings> getRouteRatingsCollection() {
        return routeRatingsCollection;
    }

    public void setRouteRatingsCollection(Collection<RouteRatings> routeRatingsCollection) {
        this.routeRatingsCollection = routeRatingsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
