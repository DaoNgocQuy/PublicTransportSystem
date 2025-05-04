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
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

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
    @NamedQuery(name = "Schedules.findByArrivalTime", query = "SELECT s FROM Schedules s WHERE s.arrivalTime = :arrivalTime"),
    @NamedQuery(name = "Schedules.findByDayOfWeek", query = "SELECT s FROM Schedules s WHERE s.dayOfWeek = :dayOfWeek"),
    @NamedQuery(name = "Schedules.findByIsWeekend", query = "SELECT s FROM Schedules s WHERE s.isWeekend = :isWeekend"),
    @NamedQuery(name = "Schedules.findByIsHoliday", query = "SELECT s FROM Schedules s WHERE s.isHoliday = :isHoliday"),
    @NamedQuery(name = "Schedules.findByEffectiveDate", query = "SELECT s FROM Schedules s WHERE s.effectiveDate = :effectiveDate"),
    @NamedQuery(name = "Schedules.findByExpirationDate", query = "SELECT s FROM Schedules s WHERE s.expirationDate = :expirationDate")})
public class Schedules implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "departure_time")
    @Temporal(TemporalType.TIME)
    private Date departureTime;
    @Column(name = "arrival_time")
    @Temporal(TemporalType.TIME)
    private Date arrivalTime;
    @Size(max = 10)
    @Column(name = "day_of_week")
    private String dayOfWeek;
    @Column(name = "is_weekend")
    private Boolean isWeekend;
    @Column(name = "is_holiday")
    private Boolean isHoliday;
    @Column(name = "effective_date")
    @Temporal(TemporalType.DATE)
    private Date effectiveDate;
    @Column(name = "expiration_date")
    @Temporal(TemporalType.DATE)
    private Date expirationDate;
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

    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Boolean getIsWeekend() {
        return isWeekend;
    }

    public void setIsWeekend(Boolean isWeekend) {
        this.isWeekend = isWeekend;
    }

    public Boolean getIsHoliday() {
        return isHoliday;
    }

    public void setIsHoliday(Boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
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
        // TODO: Warning - this method won't work in the case the id fields are not set
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
