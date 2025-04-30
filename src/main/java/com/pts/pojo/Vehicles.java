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
import jakarta.validation.constraints.NotNull;
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
    @NamedQuery(name = "Vehicles.findByType", query = "SELECT v FROM Vehicles v WHERE v.type = :type"),
    @NamedQuery(name = "Vehicles.findByLicensePlate", query = "SELECT v FROM Vehicles v WHERE v.licensePlate = :licensePlate"),
    @NamedQuery(name = "Vehicles.findByCapacity", query = "SELECT v FROM Vehicles v WHERE v.capacity = :capacity")})
public class Vehicles implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 9)
    @Column(name = "type")
    private String type;
    @Size(max = 20)
    @Column(name = "license_plate")
    private String licensePlate;
    @Column(name = "capacity")
    private Integer capacity;
    @OneToMany(mappedBy = "vehicleId")
    private Collection<LiveLocation> liveLocationCollection;
    @OneToMany(mappedBy = "vehicleId")
    private Collection<Schedules> schedulesCollection;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    private Users userId;

    public Vehicles() {
    }

    public Vehicles(Integer id) {
        this.id = id;
    }

    public Vehicles(Integer id, String type) {
        this.id = id;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
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
