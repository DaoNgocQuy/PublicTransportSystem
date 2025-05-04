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
import java.io.Serializable;
import java.sql.Time;

/**
 *
 * @author LEGION
 */
@Entity
@Table(name = "schedules")
@NamedQueries({
    @NamedQuery(name = "Schedules.findAll", query = "SELECT s FROM Schedules s"),
    @NamedQuery(name = "Schedules.findById", query = "SELECT s FROM Schedules s WHERE s.id = :id"),
    @NamedQuery(name = "Schedules.findByDepartureTime", query = "SELECT s FROM Schedules s WHERE s.departureTime = :departureTime"),
    @NamedQuery(name = "Schedules.findByArrivalTime", query = "SELECT s FROM Schedules s WHERE s.arrivalTime = :arrivalTime")
})
public class Schedules implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "departure_time")
    @Temporal(TemporalType.TIME)
    private Time departureTime;
    @Column(name = "arrival_time")
    @Temporal(TemporalType.TIME)
    private Time arrivalTime;
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    @ManyToOne
    private Routes routeId;
    @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
    @ManyToOne
    private Vehicles vehicleId;

    public Schedules() {
    }

    public Schedules(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Time getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Time departureTime) {
        this.departureTime = departureTime;
    }

    public Time getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Time arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Routes getRouteId() {
        return routeId;
    }

    public void setRouteId(Routes routeId) {
        this.routeId = routeId;
    }

    public Vehicles getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Vehicles vehicleId) {
        this.vehicleId = vehicleId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Schedules)) {
            return false;
        }
        Schedules other = (Schedules) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.Schedules[ id=" + id + " ]";
    }
}
