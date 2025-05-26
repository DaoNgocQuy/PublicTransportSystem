package com.pts.pojo;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity class representing the route_stops table Maps the relationship between
 * routes and stops with ordering
 */
@Entity
@Table(name = "route_stops")
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Routes route;

    @ManyToOne
    @JoinColumn(name = "stop_id", nullable = false)
    private Stops stop;
    @Column(name = "direction")
    private Integer direction;
    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public RouteStop() {
    }

    // Constructor with all fields
    public RouteStop(Integer id, Routes route, Stops stop, Integer stopOrder, Integer direction, LocalDateTime createdAt) {
        this.id = id;
        this.route = route;
        this.stop = stop;
        this.stopOrder = stopOrder;
        this.direction = direction;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RouteStop routeStop = (RouteStop) o;
        return Objects.equals(id, routeStop.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RouteStop{"
                + "id=" + id
                + ", route=" + (route != null ? route.getId() : null)
                + ", stop=" + (stop != null ? stop.getId() : null)
                + ", stopOrder=" + stopOrder
                + ", direction=" + direction
                + ", createdAt=" + createdAt
                + '}';
    }
}
