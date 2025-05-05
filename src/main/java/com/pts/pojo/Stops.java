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
@Table(name = "stops")
@NamedQueries({
    @NamedQuery(name = "Stops.findAll", query = "SELECT s FROM Stops s"),
    @NamedQuery(name = "Stops.findById", query = "SELECT s FROM Stops s WHERE s.id = :id"),
    @NamedQuery(name = "Stops.findByStopName", query = "SELECT s FROM Stops s WHERE s.stopName = :stopName"),
    @NamedQuery(name = "Stops.findByLatitude", query = "SELECT s FROM Stops s WHERE s.latitude = :latitude"),
    @NamedQuery(name = "Stops.findByLongitude", query = "SELECT s FROM Stops s WHERE s.longitude = :longitude"),
    @NamedQuery(name = "Stops.findByStopOrder", query = "SELECT s FROM Stops s WHERE s.stopOrder = :stopOrder"),
    @NamedQuery(name = "Stops.findByAddress", query = "SELECT s FROM Stops s WHERE s.address = :address"),
    @NamedQuery(name = "Stops.findByIsAccessible", query = "SELECT s FROM Stops s WHERE s.isAccessible = :isAccessible")})
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
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "latitude")
    private Float latitude;
    @Column(name = "longitude")
    private Float longitude;
    @Column(name = "stop_order")
    private Integer stopOrder;
    @Size(max = 255)
    @Column(name = "address")
    private String address;

    @Column(name = "is_accessible")
    private Boolean isAccessible;
    @OneToMany(mappedBy = "fromStopId")
    private Collection<RouteSegments> routeSegmentsCollection;
    @OneToMany(mappedBy = "toStopId")
    private Collection<RouteSegments> routeSegmentsCollection1;
    @OneToMany(mappedBy = "transferStopId")
    private Collection<Transfers> transfersCollection;
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    @ManyToOne
    private Routes routeId;

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

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
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

    public Collection<RouteSegments> getRouteSegmentsCollection() {
        return routeSegmentsCollection;
    }

    public void setRouteSegmentsCollection(Collection<RouteSegments> routeSegmentsCollection) {
        this.routeSegmentsCollection = routeSegmentsCollection;
    }

    public Collection<RouteSegments> getRouteSegmentsCollection1() {
        return routeSegmentsCollection1;
    }

    public void setRouteSegmentsCollection1(Collection<RouteSegments> routeSegmentsCollection1) {
        this.routeSegmentsCollection1 = routeSegmentsCollection1;
    }

    public Collection<Transfers> getTransfersCollection() {
        return transfersCollection;
    }

    public void setTransfersCollection(Collection<Transfers> transfersCollection) {
        this.transfersCollection = transfersCollection;
    }

    public Routes getRouteId() {
        return routeId;
    }

    public void setRouteId(Routes routeId) {
        this.routeId = routeId;
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
