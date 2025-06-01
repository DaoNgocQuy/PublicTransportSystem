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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author LEGION
 */
@Entity
@Table(name = "route_types")
@NamedQueries({
        @NamedQuery(name = "RouteTypes.findAll", query = "SELECT r FROM RouteTypes r ORDER BY r.typeName"),
        @NamedQuery(name = "RouteTypes.findById", query = "SELECT r FROM RouteTypes r WHERE r.id = :id"),
        @NamedQuery(name = "RouteTypes.findByTypeName", query = "SELECT r FROM RouteTypes r WHERE r.typeName = :typeName"),
        @NamedQuery(name = "RouteTypes.findByDescription", query = "SELECT r FROM RouteTypes r WHERE r.description = :description"),
        @NamedQuery(name = "RouteTypes.findByIconUrl", query = "SELECT r FROM RouteTypes r WHERE r.iconUrl = :iconUrl"),
        @NamedQuery(name = "RouteTypes.findByColorCode", query = "SELECT r FROM RouteTypes r WHERE r.colorCode = :colorCode")
})
public class RouteTypes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "type_name")
    private String typeName;
    @Size(max = 255)
    @Column(name = "description")
    private String description;
    @Size(max = 255)
    @Column(name = "icon_url")
    private String iconUrl;
    @Size(max = 10)
    @Column(name = "color_code")
    private String colorCode;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "routeTypeId")
    @JsonIgnore
    private Set<Routes> routesSet;

    public RouteTypes() {
    }

    public RouteTypes(Integer id) {
        this.id = id;
    }

    public RouteTypes(Integer id, String typeName) {
        this.id = id;
        this.typeName = typeName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public Set<Routes> getRoutesSet() {
        return routesSet;
    }

    public void setRoutesSet(Set<Routes> routesSet) {
        this.routesSet = routesSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RouteTypes)) {
            return false;
        }
        RouteTypes other = (RouteTypes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.RouteTypes[ id=" + id + " ]";
    }
}