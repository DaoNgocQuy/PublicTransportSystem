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
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author LEGION
 */
@Entity
@Table(name = "vehicles")
@NamedQueries({
    @NamedQuery(name = "Vehicles.findAll", query = "SELECT v FROM Vehicles v"),
    @NamedQuery(name = "Vehicles.findById", query = "SELECT v FROM Vehicles v WHERE v.id = :id"),
    @NamedQuery(name = "Vehicles.findByVehicleName", query = "SELECT v FROM Vehicles v WHERE v.vehicleName = :vehicleName"),
    @NamedQuery(name = "Vehicles.findByType", query = "SELECT v FROM Vehicles v WHERE v.type = :type"),
    @NamedQuery(name = "Vehicles.findByLicensePlate", query = "SELECT v FROM Vehicles v WHERE v.licensePlate = :licensePlate"),
    @NamedQuery(name = "Vehicles.findByCapacity", query = "SELECT v FROM Vehicles v WHERE v.capacity = :capacity"),
    @NamedQuery(name = "Vehicles.findByIsAccessible", query = "SELECT v FROM Vehicles v WHERE v.isAccessible = :isAccessible"),
    @NamedQuery(name = "Vehicles.findByIsAirConditioned", query = "SELECT v FROM Vehicles v WHERE v.isAirConditioned = :isAirConditioned"),
    @NamedQuery(name = "Vehicles.findByProductionYear", query = "SELECT v FROM Vehicles v WHERE v.productionYear = :productionYear"),
    @NamedQuery(name = "Vehicles.findByStatus", query = "SELECT v FROM Vehicles v WHERE v.status = :status")})
public class Vehicles implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 100)
    @Column(name = "vehicle_name")
    private String vehicleName;
    @Size(max = 20)
    @Column(name = "type")
    private String type;
    @Size(max = 20)
    @Column(name = "license_plate")
    private String licensePlate;
    @Column(name = "capacity")
    private Integer capacity;
    @Column(name = "is_accessible")
    private Boolean isAccessible;
    @Column(name = "is_air_conditioned")
    private Boolean isAirConditioned;
    @Column(name = "production_year")
    private Integer productionYear;
    @Size(max = 20)
    @Column(name = "status")
    private String status;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    private Users userId;
    @OneToMany(mappedBy = "vehicleId")
    private Collection<LiveLocation> liveLocationCollection;
    @OneToMany(mappedBy = "vehicleId")
    private Collection<Schedules> schedulesCollection;

    public Vehicles() {
    }

    public Vehicles(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getIsAccessible() {
        return isAccessible;
    }

    public void setIsAccessible(Boolean isAccessible) {
        this.isAccessible = isAccessible;
    }

    public Boolean getIsAirConditioned() {
        return isAirConditioned;
    }

    public void setIsAirConditioned(Boolean isAirConditioned) {
        this.isAirConditioned = isAirConditioned;
    }

    public Integer getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(Integer productionYear) {
        this.productionYear = productionYear;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Collection<LiveLocation> getLiveLocationCollection() {
        return liveLocationCollection;
    }

    public void setLiveLocationCollection(Collection<LiveLocation> liveLocationCollection) {
        this.liveLocationCollection = liveLocationCollection;
    }

    public Collection<Schedules> getSchedulesCollection() {
        return schedulesCollection;
    }

    public void setSchedulesCollection(Collection<Schedules> schedulesCollection) {
        this.schedulesCollection = schedulesCollection;
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
        if (!(object instanceof Vehicles)) {
            return false;
        }
        Vehicles other = (Vehicles) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.Vehicles[ id=" + id + " ]";
    }
    
}
