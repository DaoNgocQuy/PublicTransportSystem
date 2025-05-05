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
@Table(name = "traffic_conditions")
@NamedQueries({
    @NamedQuery(name = "TrafficConditions.findAll", query = "SELECT t FROM TrafficConditions t"),
    @NamedQuery(name = "TrafficConditions.findById", query = "SELECT t FROM TrafficConditions t WHERE t.id = :id"),
    @NamedQuery(name = "TrafficConditions.findByConditionType", query = "SELECT t FROM TrafficConditions t WHERE t.conditionType = :conditionType"),
    @NamedQuery(name = "TrafficConditions.findByReportedAt", query = "SELECT t FROM TrafficConditions t WHERE t.reportedAt = :reportedAt"),
    @NamedQuery(name = "TrafficConditions.findByEstimatedDelayMinutes", query = "SELECT t FROM TrafficConditions t WHERE t.estimatedDelayMinutes = :estimatedDelayMinutes")})
public class TrafficConditions implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 9)
    @Column(name = "condition_type")
    private String conditionType;
    @Column(name = "reported_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportedAt;
    @Column(name = "estimated_delay_minutes")
    private Integer estimatedDelayMinutes;
    @JoinColumn(name = "route_segment_id", referencedColumnName = "id")
    @ManyToOne
    private RouteSegments routeSegmentId;
    @JoinColumn(name = "reported_by", referencedColumnName = "id")
    @ManyToOne
    private Users reportedBy;

    public TrafficConditions() {
    }

    public TrafficConditions(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public Date getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Date reportedAt) {
        this.reportedAt = reportedAt;
    }

    public Integer getEstimatedDelayMinutes() {
        return estimatedDelayMinutes;
    }

    public void setEstimatedDelayMinutes(Integer estimatedDelayMinutes) {
        this.estimatedDelayMinutes = estimatedDelayMinutes;
    }

    public RouteSegments getRouteSegmentId() {
        return routeSegmentId;
    }

    public void setRouteSegmentId(RouteSegments routeSegmentId) {
        this.routeSegmentId = routeSegmentId;
    }

    public Users getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Users reportedBy) {
        this.reportedBy = reportedBy;
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
        if (!(object instanceof TrafficConditions)) {
            return false;
        }
        TrafficConditions other = (TrafficConditions) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.pts.pojo.TrafficConditions[ id=" + id + " ]";
    }
    
}
