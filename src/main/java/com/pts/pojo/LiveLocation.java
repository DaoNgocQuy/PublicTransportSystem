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
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author LEGION
 */
@Entity
@Table(name = "live_location")
@NamedQueries({
    @NamedQuery(name = "LiveLocation.findAll", query = "SELECT l FROM LiveLocation l"),
    @NamedQuery(name = "LiveLocation.findById", query = "SELECT l FROM LiveLocation l WHERE l.id = :id"),
    @NamedQuery(name = "LiveLocation.findByLatitude", query = "SELECT l FROM LiveLocation l WHERE l.latitude = :latitude"),
    @NamedQuery(name = "LiveLocation.findByLongitude", query = "SELECT l FROM LiveLocation l WHERE l.longitude = :longitude"),
    @NamedQuery(name = "LiveLocation.findByLastUpdated", query = "SELECT l FROM LiveLocation l WHERE l.lastUpdated = :lastUpdated")})
public class LiveLocation implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "latitude")
    private double latitude;
    @Basic(optional = false)
    @NotNull
    @Column(name = "longitude")
    private double longitude;
    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;
    @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
    @ManyToOne
    private Vehicles vehicleId;

    public LiveLocation() {
    }

    public LiveLocation(Integer id) {
        this.id = id;
    }

    public LiveLocation(Integer id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
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
        if (!(object instanceof LiveLocation)) {
            return false;
        }
        LiveLocation other = (LiveLocation) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.LiveLocation[ id=" + id + " ]";
    }
    
}
