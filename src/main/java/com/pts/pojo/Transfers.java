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
import java.io.Serializable;

/**
 *
 * @author LEGION
 */
@Entity
@Table(name = "transfers")
@NamedQueries({
    @NamedQuery(name = "Transfers.findAll", query = "SELECT t FROM Transfers t"),
    @NamedQuery(name = "Transfers.findById", query = "SELECT t FROM Transfers t WHERE t.id = :id"),
    @NamedQuery(name = "Transfers.findByTransferTimeMinutes", query = "SELECT t FROM Transfers t WHERE t.transferTimeMinutes = :transferTimeMinutes"),
    @NamedQuery(name = "Transfers.findByDistanceMeters", query = "SELECT t FROM Transfers t WHERE t.distanceMeters = :distanceMeters"),
    @NamedQuery(name = "Transfers.findByIsAccessible", query = "SELECT t FROM Transfers t WHERE t.isAccessible = :isAccessible")})
public class Transfers implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "transfer_time_minutes")
    private Integer transferTimeMinutes;
    @Column(name = "distance_meters")
    private Integer distanceMeters;
    @Column(name = "is_accessible")
    private Boolean isAccessible;
    @JoinColumn(name = "from_route_id", referencedColumnName = "id")
    @ManyToOne
    private Routes fromRouteId;
    @JoinColumn(name = "to_route_id", referencedColumnName = "id")
    @ManyToOne
    private Routes toRouteId;
    @JoinColumn(name = "transfer_stop_id", referencedColumnName = "id")
    @ManyToOne
    private Stops transferStopId;

    public Transfers() {
    }

    public Transfers(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTransferTimeMinutes() {
        return transferTimeMinutes;
    }

    public void setTransferTimeMinutes(Integer transferTimeMinutes) {
        this.transferTimeMinutes = transferTimeMinutes;
    }

    public Integer getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Integer distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Boolean getIsAccessible() {
        return isAccessible;
    }

    public void setIsAccessible(Boolean isAccessible) {
        this.isAccessible = isAccessible;
    }

    public Routes getFromRouteId() {
        return fromRouteId;
    }

    public void setFromRouteId(Routes fromRouteId) {
        this.fromRouteId = fromRouteId;
    }

    public Routes getToRouteId() {
        return toRouteId;
    }

    public void setToRouteId(Routes toRouteId) {
        this.toRouteId = toRouteId;
    }

    public Stops getTransferStopId() {
        return transferStopId;
    }

    public void setTransferStopId(Stops transferStopId) {
        this.transferStopId = transferStopId;
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
        if (!(object instanceof Transfers)) {
            return false;
        }
        Transfers other = (Transfers) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.Transfers[ id=" + id + " ]";
    }
    
}
