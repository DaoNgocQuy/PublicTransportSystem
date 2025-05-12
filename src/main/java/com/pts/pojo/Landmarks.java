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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(name = "landmarks")  
@NamedQueries({
    @NamedQuery(name = "Landmarks.findAll", query = "SELECT l FROM Landmarks l"),
    @NamedQuery(name = "Landmarks.findById", query = "SELECT l FROM Landmarks l WHERE l.id = :id"),
    @NamedQuery(name = "Landmarks.findByName", query = "SELECT l FROM Landmarks l WHERE l.name = :name"),
    @NamedQuery(name = "Landmarks.findByAddress", query = "SELECT l FROM Landmarks l WHERE l.address = :address"),
    @NamedQuery(name = "Landmarks.findByLatitude", query = "SELECT l FROM Landmarks l WHERE l.latitude = :latitude"),
    @NamedQuery(name = "Landmarks.findByLongitude", query = "SELECT l FROM Landmarks l WHERE l.longitude = :longitude"),
    @NamedQuery(name = "Landmarks.findByLandmarkType", query = "SELECT l FROM Landmarks l WHERE l.landmarkType = :landmarkType"),
    @NamedQuery(name = "Landmarks.findByIcon", query = "SELECT l FROM Landmarks l WHERE l.icon = :icon"),
    @NamedQuery(name = "Landmarks.findByTags", query = "SELECT l FROM Landmarks l WHERE l.tags LIKE :tags"),
    @NamedQuery(name = "Landmarks.findNearby", query = "SELECT l FROM Landmarks l ORDER BY SQRT(POWER(l.latitude - :latitude, 2) + POWER(l.longitude - :longitude, 2))"),
})
public class Landmarks implements Serializable {

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
    @Column(name = "address")
    private String address;
    
    @Column(name = "latitude")
    private Float latitude;
    
    @Column(name = "longitude")
    private Float longitude;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Size(max = 50)
    @Column(name = "landmark_type")
    private String landmarkType;
    
    @Size(max = 50)
    @Column(name = "icon")
    private String icon;
    
    @Size(max = 255)
    @Column(name = "tags")
    private String tags;
    
    @JoinColumn(name = "nearest_stop_id", referencedColumnName = "id")
    @ManyToOne
    private Stops nearestStop;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;
    
    public Landmarks() {
    }
    
    public Landmarks(Integer id) {
        this.id = id;
    }
    
    public Landmarks(Integer id, String name) {
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLandmarkType() {
        return landmarkType;
    }
    
    public void setLandmarkType(String landmarkType) {
        this.landmarkType = landmarkType;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Stops getNearestStop() {
        return nearestStop;
    }
    
    public void setNearestStop(Stops nearestStop) {
        this.nearestStop = nearestStop;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        lastUpdated = new Date();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = new Date();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Landmarks)) {
            return false;
        }
        Landmarks other = (Landmarks) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "com.pts.pojo.Landmark[ id=" + id + " ]";
    }
}