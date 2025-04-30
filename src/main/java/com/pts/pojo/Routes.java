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
@Table(name = "routes")
@NamedQueries({
    @NamedQuery(name = "Routes.findAll", query = "SELECT r FROM Routes r"),
    @NamedQuery(name = "Routes.findById", query = "SELECT r FROM Routes r WHERE r.id = :id"),
    @NamedQuery(name = "Routes.findByName", query = "SELECT r FROM Routes r WHERE r.name = :name"),
    @NamedQuery(name = "Routes.findByStartLocation", query = "SELECT r FROM Routes r WHERE r.startLocation = :startLocation"),
    @NamedQuery(name = "Routes.findByEndLocation", query = "SELECT r FROM Routes r WHERE r.endLocation = :endLocation"),
    @NamedQuery(name = "Routes.findByTotalStops", query = "SELECT r FROM Routes r WHERE r.totalStops = :totalStops"),
    @NamedQuery(name = "Routes.findByIsWalkingRoute", query = "SELECT r FROM Routes r WHERE r.isWalkingRoute = :isWalkingRoute")})
public class Routes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
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
    @Column(name = "total_stops")
    private Integer totalStops;
    @Column(name = "is_walking_route")
    private Boolean isWalkingRoute;
    @OneToMany(mappedBy = "routeId")
    private Collection<Favorites> favoritesCollection;
    @OneToMany(mappedBy = "fromRouteId")
    private Collection<Transfers> transfersCollection;
    @OneToMany(mappedBy = "toRouteId")
    private Collection<Transfers> transfersCollection1;
    @OneToMany(mappedBy = "routeId")
    private Collection<Schedules> schedulesCollection;
    @OneToMany(mappedBy = "routeId")
    private Collection<Stops> stopsCollection;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getIsWalkingRoute() {
        return isWalkingRoute;
    }

    public void setIsWalkingRoute(Boolean isWalkingRoute) {
        this.isWalkingRoute = isWalkingRoute;
    }

    public Collection<Favorites> getFavoritesCollection() {
        return favoritesCollection;
    }

    public void setFavoritesCollection(Collection<Favorites> favoritesCollection) {
        this.favoritesCollection = favoritesCollection;
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
    
}
