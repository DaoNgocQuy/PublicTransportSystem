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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author LEGION
 */
@Entity
@Table(name = "route_stops", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "route_id", "direction", "stop_order" })
})
@NamedQueries({
        @NamedQuery(name = "RouteStop.findAll", query = "SELECT r FROM RouteStop r"),
        @NamedQuery(name = "RouteStop.findById", query = "SELECT r FROM RouteStop r WHERE r.id = :id"),
        @NamedQuery(name = "RouteStop.findByRouteId", query = "SELECT r FROM RouteStop r WHERE r.route.id = :routeId ORDER BY r.stopOrder"),
        @NamedQuery(name = "RouteStop.findByStopId", query = "SELECT r FROM RouteStop r WHERE r.stop.id = :stopId"),
        @NamedQuery(name = "RouteStop.findByDirection", query = "SELECT r FROM RouteStop r WHERE r.direction = :direction"),
        @NamedQuery(name = "RouteStop.findByStopOrder", query = "SELECT r FROM RouteStop r WHERE r.stopOrder = :stopOrder")
})
public class RouteStop implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "direction")
    private Integer direction;
    @Basic(optional = false)
    @Column(name = "stop_order")
    private Integer stopOrder;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Routes route;
    @JoinColumn(name = "stop_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Stops stop;

    public RouteStop() {
    }

    public RouteStop(Integer id) {
        this.id = id;
    }

    public RouteStop(Integer id, Integer direction, Integer stopOrder) {
        this.id = id;
        this.direction = direction;
        this.stopOrder = stopOrder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Routes getRoute() {
        return route;
    }

    public void setRoute(Routes route) {
        this.route = route;
    }

    public Stops getStop() {
        return stop;
    }

    public void setStop(Stops stop) {
        this.stop = stop;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RouteStop)) {
            return false;
        }
        RouteStop other = (RouteStop) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.RouteStop[ id=" + id + " ]";
    }
}