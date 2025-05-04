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
@Table(name = "route_search_history")
@NamedQueries({
    @NamedQuery(name = "RouteSearchHistory.findAll", query = "SELECT r FROM RouteSearchHistory r"),
    @NamedQuery(name = "RouteSearchHistory.findById", query = "SELECT r FROM RouteSearchHistory r WHERE r.id = :id"),
    @NamedQuery(name = "RouteSearchHistory.findByStartLocation", query = "SELECT r FROM RouteSearchHistory r WHERE r.startLocation = :startLocation"),
    @NamedQuery(name = "RouteSearchHistory.findByEndLocation", query = "SELECT r FROM RouteSearchHistory r WHERE r.endLocation = :endLocation"),
    @NamedQuery(name = "RouteSearchHistory.findBySearchDatetime", query = "SELECT r FROM RouteSearchHistory r WHERE r.searchDatetime = :searchDatetime")})
public class RouteSearchHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 255)
    @Column(name = "start_location")
    private String startLocation;
    @Size(max = 255)
    @Column(name = "end_location")
    private String endLocation;
    @Column(name = "search_datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date searchDatetime;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    private Users userId;

    public RouteSearchHistory() {
    }

    public RouteSearchHistory(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getSearchDatetime() {
        return searchDatetime;
    }

    public void setSearchDatetime(Date searchDatetime) {
        this.searchDatetime = searchDatetime;
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
        if (!(object instanceof RouteSearchHistory)) {
            return false;
        }
        RouteSearchHistory other = (RouteSearchHistory) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.RouteSearchHistory[ id=" + id + " ]";
    }
    
}
