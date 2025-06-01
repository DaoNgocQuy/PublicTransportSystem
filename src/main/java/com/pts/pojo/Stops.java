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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author LEGION
 */
@Entity
@Table(name = "stops")
@NamedQueries({
        @NamedQuery(name = "Stops.findAll", query = "SELECT s FROM Stops s ORDER BY s.stopName"),
        @NamedQuery(name = "Stops.findById", query = "SELECT s FROM Stops s WHERE s.id = :id"),
        @NamedQuery(name = "Stops.findByStopName", query = "SELECT s FROM Stops s WHERE s.stopName = :stopName"),
        @NamedQuery(name = "Stops.findByLatitude", query = "SELECT s FROM Stops s WHERE s.latitude = :latitude"),
        @NamedQuery(name = "Stops.findByLongitude", query = "SELECT s FROM Stops s WHERE s.longitude = :longitude"),
        @NamedQuery(name = "Stops.findByAddress", query = "SELECT s FROM Stops s WHERE s.address = :address"),
        @NamedQuery(name = "Stops.findByIsAccessible", query = "SELECT s FROM Stops s WHERE s.isAccessible = :isAccessible")
})
public class Stops implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "stop_name")
    private String stopName;
    @Column(name = "latitude")
    private Float latitude;
    @Column(name = "longitude")
    private Float longitude;
    @Size(max = 255)
    @Column(name = "address")
    private String address;
    @Column(name = "is_accessible")
    private Boolean isAccessible;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "stop")
    @JsonIgnore
    private Set<RouteStop> routeStopSet;
    @Transient
    private Integer stopOrder;
    @Transient
    private Integer direction;

    public Stops() {
    }

    public Stops(Integer id) {
        this.id = id;
    }

    public Stops(Integer id, String stopName) {
        this.id = id;
        this.stopName = stopName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsAccessible() {
        return isAccessible;
    }

    public void setIsAccessible(Boolean isAccessible) {
        this.isAccessible = isAccessible;
    }

    public Set<RouteStop> getRouteStopSet() {
        return routeStopSet;
    }

    public void setRouteStopSet(Set<RouteStop> routeStopSet) {
        this.routeStopSet = routeStopSet;
    }

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Stops)) {
            return false;
        }
        Stops other = (Stops) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.Stops[ id=" + id + " ]";
    }
}