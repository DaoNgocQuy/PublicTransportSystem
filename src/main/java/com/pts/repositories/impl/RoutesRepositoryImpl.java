/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.repositories.impl;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import com.pts.repositories.RoutesRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.sql.Time;
import java.util.ArrayList;
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
public class RoutesRepositoryImpl implements RoutesRepository {

    private static final int PAGE_SIZE = 10;
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Routes> findAll() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Routes> q = b.createQuery(Routes.class);
        Root root = q.from(Routes.class);
        q.select(root);
        q.orderBy(b.asc(root.get("routeName")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public Optional<Routes> findById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        Routes route = s.get(Routes.class, id);
        return Optional.ofNullable(route);
    }

    @Override
    public Routes save(Routes route) {
        Session s = this.factory.getObject().getCurrentSession();
        if (route.getId() == null) {
            s.persist(route);
        } else {
            s.merge(route);
        }
        return route;
    }

    @Override
    public void deleteById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        Routes route = this.findById(id).orElse(null);
        if (route != null) {
            s.remove(route);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(Routes.class);
        q.select(b.count(root));
        q.where(b.equal(root.get("id"), id));

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    @Override
    public List<Routes> findByName(String name) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Routes> q = b.createQuery(Routes.class);
        Root root = q.from(Routes.class);
        q.select(root);
        q.where(b.equal(root.get("routeName"), name));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Routes> findByIsActive(Boolean isActive) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Routes> q = b.createQuery(Routes.class);
        Root root = q.from(Routes.class);
        q.select(root);
        q.where(b.equal(root.get("isActive"), isActive));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Routes> searchRoutesByName(String keyword) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Routes> q = b.createQuery(Routes.class);
        Root root = q.from(Routes.class);
        q.select(root);

        if (keyword != null && !keyword.isEmpty()) {
            List<Predicate> predicates = new ArrayList<>();
            String searchPattern = String.format("%%%s%%", keyword);
            predicates.add(b.like(root.get("routeName"), searchPattern));
            predicates.add(b.like(root.get("startLocation"), searchPattern));
            predicates.add(b.like(root.get("endLocation"), searchPattern));
            q.where(b.or(predicates.toArray(Predicate[]::new)));
        }

        q.orderBy(b.asc(root.get("routeName")));
        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Routes> findByRouteTypeId(Integer routeTypeId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Routes> q = b.createQuery(Routes.class);
        Root root = q.from(Routes.class);
        q.select(root);
        q.where(b.equal(root.get("routeTypeId").get("id"), routeTypeId));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Routes> findByStartLocation(String startLocation) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Routes> q = b.createQuery(Routes.class);
        Root root = q.from(Routes.class);
        q.select(root);
        q.where(b.equal(root.get("startLocation"), startLocation));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Routes> findByEndLocation(String endLocation) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Routes> q = b.createQuery(Routes.class);
        Root root = q.from(Routes.class);
        q.select(root);
        q.where(b.equal(root.get("endLocation"), endLocation));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Routes> findByTotalStops(Integer totalStops) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Routes> q = b.createQuery(Routes.class);
        Root root = q.from(Routes.class);
        q.select(root);
        q.where(b.equal(root.get("totalStops"), totalStops));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Routes> findByStopId(Integer stopId) {
        Session s = this.factory.getObject().getCurrentSession();

        String jpql = "SELECT DISTINCT r FROM Routes r " +
                "JOIN r.routeStopSet rs " +
                "WHERE rs.stop.id = :stopId";

        Query query = s.createQuery(jpql, Routes.class);
        query.setParameter("stopId", stopId);
        return query.getResultList();
    }

    @Override
    public List<Routes> findByStopIdAndDirection(Integer stopId, Integer direction) {
        Session s = this.factory.getObject().getCurrentSession();

        String jpql = "SELECT DISTINCT r FROM Routes r " +
                "JOIN r.routeStopSet rs " +
                "WHERE rs.stop.id = :stopId AND rs.direction = :direction";

        Query query = s.createQuery(jpql, Routes.class);
        query.setParameter("stopId", stopId);
        query.setParameter("direction", direction);
        return query.getResultList();
    }

    @Override
    public List<Stops> findStopsByRouteId(Integer routeId) {
        Session s = this.factory.getObject().getCurrentSession();

        // Modified query to include stop_order in the SELECT clause when using DISTINCT
        String jpql = "SELECT DISTINCT s, rs.stopOrder FROM Stops s " +
                "JOIN s.routeStopSet rs " +
                "WHERE rs.route.id = :routeId " +
                "ORDER BY rs.stopOrder";

        Query query = s.createQuery(jpql);
        query.setParameter("routeId", routeId);

        List<Object[]> results = query.getResultList();
        List<Stops> stops = new ArrayList<>();

        // Extract Stops objects from results
        for (Object[] result : results) {
            Stops stop = (Stops) result[0];
            Integer stopOrder = (Integer) result[1];
            stop.setStopOrder(stopOrder); // Set the stop order if your Stops class has this field
            stops.add(stop);
        }

        return stops;
    }

    @Override
    public List<Stops> findStopsByRouteIdAndDirection(Integer routeId, Integer direction) {
        Session s = this.factory.getObject().getCurrentSession();

        // Modified query to include stop_order in the SELECT clause
        String jpql = "SELECT DISTINCT s, rs.stopOrder FROM Stops s " +
                "JOIN s.routeStopSet rs " +
                "WHERE rs.route.id = :routeId AND rs.direction = :direction " +
                "ORDER BY rs.stopOrder";

        Query query = s.createQuery(jpql);
        query.setParameter("routeId", routeId);
        query.setParameter("direction", direction);

        List<Object[]> results = query.getResultList();
        List<Stops> stops = new ArrayList<>();

        // Extract Stops objects from results
        for (Object[] result : results) {
            Stops stop = (Stops) result[0];
            Integer stopOrder = (Integer) result[1];
            stop.setStopOrder(stopOrder);
            stops.add(stop);
        }

        return stops;
    }

    @Override
    public Integer countStopsByRouteId(Integer routeId) {
        Session s = this.factory.getObject().getCurrentSession();

        String jpql = "SELECT COUNT(rs) FROM RouteStop rs WHERE rs.route.id = :routeId";
        Query query = s.createQuery(jpql);
        query.setParameter("routeId", routeId);

        Long count = (Long) query.getSingleResult();
        return count.intValue();
    }

    @Override
    public Integer countStopsByRouteIdAndDirection(Integer routeId, Integer direction) {
        Session s = this.factory.getObject().getCurrentSession();

        String jpql = "SELECT COUNT(rs) FROM RouteStop rs WHERE rs.route.id = :routeId AND rs.direction = :direction";
        Query query = s.createQuery(jpql);
        query.setParameter("routeId", routeId);
        query.setParameter("direction", direction);

        Long count = (Long) query.getSingleResult();
        return count.intValue();
    }

    @Override
    public void updateTotalStops(Integer routeId) {
        Session s = this.factory.getObject().getCurrentSession();

        String jpql = "UPDATE Routes r SET r.totalStops = " +
                "(SELECT COUNT(rs) FROM RouteStop rs WHERE rs.route.id = r.id) " +
                "WHERE r.id = :routeId";

        Query query = s.createQuery(jpql);
        query.setParameter("routeId", routeId);
        query.executeUpdate();
    }

    @Override
    public void updateRouteOperationDetails(Integer routeId, Time startTime, Time endTime, Integer frequencyMinutes) {
        Session s = this.factory.getObject().getCurrentSession();

        String jpql = "UPDATE Routes r SET r.startTime = :startTime, r.endTime = :endTime " +
                "WHERE r.id = :routeId";

        Query query = s.createQuery(jpql);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        query.setParameter("routeId", routeId);
        query.executeUpdate();
    }

    @Override
    public List<Routes> findAllWithPagination(int offset, int limit) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Routes> q = b.createQuery(Routes.class);
        Root root = q.from(Routes.class);
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
        Root root = q.from(Routes.class);
        q.select(b.count(root));

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count.intValue();
    }

    @Override
    public List<Routes> searchRoutesByNameWithPagination(String keyword, int offset, int limit) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Routes> q = b.createQuery(Routes.class);
        Root root = q.from(Routes.class);
        q.select(root);

        if (keyword != null && !keyword.isEmpty()) {
            List<Predicate> predicates = new ArrayList<>();
            String searchPattern = String.format("%%%s%%", keyword);
            predicates.add(b.like(root.get("routeName"), searchPattern));
            predicates.add(b.like(root.get("startLocation"), searchPattern));
            predicates.add(b.like(root.get("endLocation"), searchPattern));
            q.where(b.or(predicates.toArray(Predicate[]::new)));
        }

        q.orderBy(b.asc(root.get("id")));
        Query query = s.createQuery(q);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    @Override
    public int countByNameContaining(String keyword) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(Routes.class);
        q.select(b.count(root));

        if (keyword != null && !keyword.isEmpty()) {
            List<Predicate> predicates = new ArrayList<>();
            String searchPattern = String.format("%%%s%%", keyword);
            predicates.add(b.like(root.get("routeName"), searchPattern));
            predicates.add(b.like(root.get("startLocation"), searchPattern));
            predicates.add(b.like(root.get("endLocation"), searchPattern));
            q.where(b.or(predicates.toArray(Predicate[]::new)));
        }

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count.intValue();
    }
}