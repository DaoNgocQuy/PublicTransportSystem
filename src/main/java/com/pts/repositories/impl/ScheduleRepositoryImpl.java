/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.repositories.impl;

import com.pts.pojo.Schedules;
import com.pts.pojo.Vehicles;
import com.pts.pojo.Routes;
import com.pts.repositories.ScheduleRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author LEGION
 */
@Repository
@Transactional
public class ScheduleRepositoryImpl implements ScheduleRepository {

    private static final int PAGE_SIZE = 20;
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Schedules> findAll() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);
        q.orderBy(b.asc(root.get("id")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Schedules> findAllWithPagination(int offset, int limit) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);
        q.orderBy(b.asc(root.get("id")));

        Query query = s.createQuery(q);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public int countAll() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(Schedules.class);
        q.select(b.count(root));

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count.intValue();
    }

    @Override
    public Optional<Schedules> findById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        Schedules schedule = s.get(Schedules.class, id);
        return Optional.ofNullable(schedule);
    }

    @Override
    public List<Schedules> findByVehicleId(Vehicles vehicleId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);
        q.where(b.equal(root.get("vehicleId").get("id"), vehicleId.getId()));
        q.orderBy(b.asc(root.get("departureTime")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Schedules> findByVehicleIdWithPagination(Vehicles vehicleId, int offset, int limit) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);
        q.where(b.equal(root.get("vehicleId").get("id"), vehicleId.getId()));
        q.orderBy(b.asc(root.get("departureTime")));

        Query query = s.createQuery(q);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public int countByVehicleId(Vehicles vehicleId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(Schedules.class);
        q.select(b.count(root));
        q.where(b.equal(root.get("vehicleId").get("id"), vehicleId.getId()));

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count.intValue();
    }

    @Override
    public List<Schedules> findByRouteId(Routes routeId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);
        q.where(b.equal(root.get("routeId").get("id"), routeId.getId()));
        q.orderBy(b.asc(root.get("departureTime")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Schedules> findByRouteIdWithPagination(Routes routeId, int offset, int limit) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);
        q.where(b.equal(root.get("routeId").get("id"), routeId.getId()));
        q.orderBy(b.asc(root.get("departureTime")));

        Query query = s.createQuery(q);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public int countByRouteId(Routes routeId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(Schedules.class);
        q.select(b.count(root));
        q.where(b.equal(root.get("routeId").get("id"), routeId.getId()));

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count.intValue();
    }

    @Override
    public List<Schedules> findByRouteId(Integer routeId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);
        q.where(b.equal(root.get("routeId").get("id"), routeId));
        q.orderBy(b.asc(root.get("departureTime")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Schedules> findByRouteIdWithPagination(Integer routeId, int offset, int limit) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);
        q.where(b.equal(root.get("routeId").get("id"), routeId));
        q.orderBy(b.asc(root.get("departureTime")));

        Query query = s.createQuery(q);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public int countByRouteId(Integer routeId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(Schedules.class);
        q.select(b.count(root));
        q.where(b.equal(root.get("routeId").get("id"), routeId));

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count.intValue();
    }

    @Override
    public List<Schedules> findByDepartureTimeBetween(Time startTime, Time endTime) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.greaterThanOrEqualTo(root.get("departureTime"), startTime));
        predicates.add(b.lessThanOrEqualTo(root.get("departureTime"), endTime));
        q.where(predicates.toArray(Predicate[]::new));
        q.orderBy(b.asc(root.get("departureTime")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Schedules> findByDepartureTimeBetweenWithPagination(Time startTime, Time endTime, int offset,
            int limit) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.greaterThanOrEqualTo(root.get("departureTime"), startTime));
        predicates.add(b.lessThanOrEqualTo(root.get("departureTime"), endTime));
        q.where(predicates.toArray(Predicate[]::new));
        q.orderBy(b.asc(root.get("departureTime")));

        Query query = s.createQuery(q);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public int countByDepartureTimeBetween(Time startTime, Time endTime) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(Schedules.class);
        q.select(b.count(root));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.greaterThanOrEqualTo(root.get("departureTime"), startTime));
        predicates.add(b.lessThanOrEqualTo(root.get("departureTime"), endTime));
        q.where(predicates.toArray(Predicate[]::new));

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count.intValue();
    }

    @Override
    public Schedules save(Schedules schedule) {
        Session s = this.factory.getObject().getCurrentSession();
        if (schedule.getId() == null || schedule.getId() == 0) {
            s.persist(schedule);
        } else {
            s.merge(schedule);
        }
        return schedule;
    }

    @Override
    public void deleteById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        Schedules schedule = this.findById(id).orElse(null);
        if (schedule != null) {
            s.remove(schedule);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(Schedules.class);
        q.select(b.count(root));
        q.where(b.equal(root.get("id"), id));

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    // Thêm các method utility theo kiểu thầy
    public List<Schedules> getSchedulesWithFilters(java.util.Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            String vehicleId = params.get("vehicleId");
            if (vehicleId != null && !vehicleId.isEmpty()) {
                predicates.add(b.equal(root.get("vehicleId").get("id"), Integer.parseInt(vehicleId)));
            }

            String routeId = params.get("routeId");
            if (routeId != null && !routeId.isEmpty()) {
                predicates.add(b.equal(root.get("routeId").get("id"), Integer.parseInt(routeId)));
            }

            String startTime = params.get("startTime");
            if (startTime != null && !startTime.isEmpty()) {
                predicates.add(b.greaterThanOrEqualTo(root.get("departureTime"), Time.valueOf(startTime)));
            }

            String endTime = params.get("endTime");
            if (endTime != null && !endTime.isEmpty()) {
                predicates.add(b.lessThanOrEqualTo(root.get("departureTime"), Time.valueOf(endTime)));
            }

            q.where(predicates.toArray(Predicate[]::new));

            String orderBy = params.get("orderBy");
            if (orderBy != null && !orderBy.isEmpty()) {
                q.orderBy(b.asc(root.get(orderBy)));
            } else {
                q.orderBy(b.asc(root.get("departureTime")));
            }
        }

        Query query = s.createQuery(q);

        if (params != null && params.containsKey("page")) {
            int page = Integer.parseInt(params.get("page"));
            int start = (page - 1) * PAGE_SIZE;

            query.setMaxResults(PAGE_SIZE);
            query.setFirstResult(start);
        }

        return query.getResultList();
    }

    public List<Schedules> findSchedulesForToday() {
        Session s = this.factory.getObject().getCurrentSession();

        // Sử dụng JPQL cho complex query
        String jpql = "SELECT s FROM Schedules s " +
                "WHERE DATE(s.createdAt) = CURRENT_DATE " +
                "ORDER BY s.departureTime";

        Query query = s.createQuery(jpql, Schedules.class);
        return query.getResultList();
    }

    public List<Schedules> findSchedulesByVehicleLicensePlate(String licensePlate) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);
        q.where(b.equal(root.get("vehicleId").get("licensePlate"), licensePlate));
        q.orderBy(b.asc(root.get("departureTime")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    public List<Schedules> findSchedulesByRouteNameContaining(String routeName) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Schedules> q = b.createQuery(Schedules.class);
        Root root = q.from(Schedules.class);
        q.select(root);
        q.where(b.like(root.get("routeId").get("routeName"), String.format("%%%s%%", routeName)));
        q.orderBy(b.asc(root.get("departureTime")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }
}