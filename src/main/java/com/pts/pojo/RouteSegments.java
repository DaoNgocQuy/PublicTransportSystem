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
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author LEGION
 */
@Entity
@Table(name = "route_segments")
@NamedQueries({
    @NamedQuery(name = "RouteSegments.findAll", query = "SELECT r FROM RouteSegments r"),
    @NamedQuery(name = "RouteSegments.findById", query = "SELECT r FROM RouteSegments r WHERE r.id = :id"),
    @NamedQuery(name = "RouteSegments.findByDistanceKm", query = "SELECT r FROM RouteSegments r WHERE r.distanceKm = :distanceKm"),
    @NamedQuery(name = "RouteSegments.findByAverageTimeMinutes", query = "SELECT r FROM RouteSegments r WHERE r.averageTimeMinutes = :averageTimeMinutes")})
public class RouteSegments implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "distance_km")
    private Float distanceKm;
    @Column(name = "average_time_minutes")
    private Integer averageTimeMinutes;
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    @ManyToOne
    private Routes routeId;
    @JoinColumn(name = "from_stop_id", referencedColumnName = "id")
    @ManyToOne
    private Stops fromStopId;
    @JoinColumn(name = "to_stop_id", referencedColumnName = "id")
    @ManyToOne
    private Stops toStopId;
    @OneToMany(mappedBy = "routeSegmentId")
    private Collection<TrafficConditions> trafficConditionsCollection;

    public RouteSegments() {
    }

    public RouteSegments(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Float distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getAverageTimeMinutes() {
        return averageTimeMinutes;
    }

    public void setAverageTimeMinutes(Integer averageTimeMinutes) {
        this.averageTimeMinutes = averageTimeMinutes;
    }

    public Routes getRouteId() {
        return routeId;
    }

    public void setRouteId(Routes routeId) {
        this.routeId = routeId;
    }

    public Stops getFromStopId() {
        return fromStopId;
    }

    public void setFromStopId(Stops fromStopId) {
        this.fromStopId = fromStopId;
    }

    public Stops getToStopId() {
        return toStopId;
    }

    public void setToStopId(Stops toStopId) {
        this.toStopId = toStopId;
    }

    public Collection<TrafficConditions> getTrafficConditionsCollection() {
        return trafficConditionsCollection;
    }

    public void setTrafficConditionsCollection(Collection<TrafficConditions> trafficConditionsCollection) {
        this.trafficConditionsCollection = trafficConditionsCollection;
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
        if (!(object instanceof RouteSegments)) {
            return false;
        }
        RouteSegments other = (RouteSegments) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.RouteSegments[ id=" + id + " ]";
    }
    
}
