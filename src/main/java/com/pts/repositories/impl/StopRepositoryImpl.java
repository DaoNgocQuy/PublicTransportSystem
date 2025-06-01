/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.repositories.impl;

import com.pts.pojo.Stops;
import com.pts.repositories.StopRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;

/**
 *
 * @author LEGION
 */
@Repository
@Transactional
public class StopRepositoryImpl implements StopRepository {

    private static final int PAGE_SIZE = 20;
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Stops> findAll() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Stops> q = b.createQuery(Stops.class);
        Root root = q.from(Stops.class);
        q.select(root);
        q.orderBy(b.asc(root.get("stopName")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public Optional<Stops> findById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        Stops stop = s.get(Stops.class, id);
        return Optional.ofNullable(stop);
    }

    @Override
    public Stops save(Stops stop) {
        Session s = this.factory.getObject().getCurrentSession();
        if (stop.getId() == null) {
            s.persist(stop);
        } else {
            s.merge(stop);
        }
        return stop;
    }

    @Override
    public void deleteById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        Stops stop = this.findById(id).orElse(null);
        if (stop != null) {
            s.remove(stop);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(Stops.class);
        q.select(b.count(root));
        q.where(b.equal(root.get("id"), id));

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    @Override
    public List<Stops> searchStops(String keyword) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Stops> q = b.createQuery(Stops.class);
        Root root = q.from(Stops.class);
        q.select(root);

        if (keyword != null && !keyword.isEmpty()) {
            List<Predicate> predicates = new ArrayList<>();
            String searchPattern = String.format("%%%s%%", keyword);
            predicates.add(b.like(root.get("stopName"), searchPattern));
            predicates.add(b.like(root.get("address"), searchPattern));
            q.where(b.or(predicates.toArray(Predicate[]::new)));
        }

        q.orderBy(b.asc(root.get("stopName")));
        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Stops> findByStopName(String stopName) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Stops> q = b.createQuery(Stops.class);
        Root root = q.from(Stops.class);
        q.select(root);
        q.where(b.like(root.get("stopName"), String.format("%%%s%%", stopName)));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Stops> findByAddress(String address) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Stops> q = b.createQuery(Stops.class);
        Root root = q.from(Stops.class);
        q.select(root);
        q.where(b.like(root.get("address"), String.format("%%%s%%", address)));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Stops> findByRouteId(Integer routeId) {
        try {
            Session s = this.factory.getObject().getCurrentSession();

            // Thay đổi truy vấn để lấy cả stopOrder và không dùng DISTINCT
            String jpql = "SELECT s, rs.stopOrder FROM Stops s " +
                    "JOIN RouteStop rs ON s.id = rs.stop.id " +
                    "WHERE rs.route.id = :routeId " +
                    "ORDER BY rs.stopOrder";

            Query query = s.createQuery(jpql);
            query.setParameter("routeId", routeId);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            // Chuyển đổi kết quả và set stopOrder cho mỗi Stop
            List<Stops> stops = new ArrayList<>();
            for (Object[] result : results) {
                Stops stop = (Stops) result[0];
                Integer stopOrder = (Integer) result[1];
                stop.setStopOrder(stopOrder); // Set stopOrder cho Stop object
                stops.add(stop);
            }

            return stops;
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm stops theo route: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Stops> findByRouteIdAndDirection(Integer routeId, Integer direction) {
        try {
            Session s = this.factory.getObject().getCurrentSession();

            String jpql = "SELECT s, rs.stopOrder FROM Stops s " +
                    "JOIN RouteStop rs ON s.id = rs.stop.id " +
                    "WHERE rs.route.id = :routeId AND rs.direction = :direction " +
                    "ORDER BY rs.stopOrder";

            Query query = s.createQuery(jpql);
            query.setParameter("routeId", routeId);
            query.setParameter("direction", direction);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            // Chuyển đổi kết quả và set stopOrder cho mỗi Stop
            List<Stops> stops = new ArrayList<>();
            for (Object[] result : results) {
                Stops stop = (Stops) result[0];
                Integer stopOrder = (Integer) result[1];
                stop.setStopOrder(stopOrder); // Set stopOrder cho Stop object
                stops.add(stop);
            }

            return stops;
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm stops theo route và direction: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Stops> findNearbyStops(double latitude, double longitude, double radius) {
        Session s = this.factory.getObject().getCurrentSession();

        // Sử dụng native query cho Haversine formula
        String sql = "SELECT *, " +
                "6371000 * acos(cos(radians(?1)) * cos(radians(latitude)) * " +
                "cos(radians(longitude) - radians(?2)) + " +
                "sin(radians(?1)) * sin(radians(latitude))) AS distance " +
                "FROM stops " +
                "HAVING distance < ?3 " +
                "ORDER BY distance";

        Query query = s.createNativeQuery(sql, Stops.class);
        query.setParameter(1, latitude);
        query.setParameter(2, longitude);
        query.setParameter(3, radius);

        return query.getResultList();
    }

    @Override
    public List<Stops> findStopsByRouteIdAndStopOrderRange(Integer routeId, Integer startOrder, Integer endOrder) {
        try {
            Session s = this.factory.getObject().getCurrentSession();

            // Thay đổi truy vấn để lấy cả stopOrder và không dùng DISTINCT
            String jpql = "SELECT s, rs.stopOrder FROM Stops s " +
                    "JOIN RouteStop rs ON s.id = rs.stop.id " +
                    "WHERE rs.route.id = :routeId " +
                    "AND rs.stopOrder BETWEEN :startOrder AND :endOrder " +
                    "ORDER BY rs.stopOrder";

            Query query = s.createQuery(jpql);
            query.setParameter("routeId", routeId);
            query.setParameter("startOrder", startOrder);
            query.setParameter("endOrder", endOrder);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            // Chuyển đổi kết quả và set stopOrder cho mỗi Stop
            List<Stops> stops = new ArrayList<>();
            for (Object[] result : results) {
                Stops stop = (Stops) result[0];
                Integer stopOrder = (Integer) result[1];
                stop.setStopOrder(stopOrder); // Set stopOrder cho Stop object
                stops.add(stop);
            }

            return stops;
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm stops theo route và khoảng stopOrder: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}