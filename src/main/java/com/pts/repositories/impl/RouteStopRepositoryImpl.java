/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.repositories.impl;

import com.pts.pojo.RouteStop;
import com.pts.repositories.RouteStopRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author LEGION
 */
@Repository
@Transactional
public class RouteStopRepositoryImpl implements RouteStopRepository {

    @Autowired
    private LocalSessionFactoryBean factory;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RouteStop save(RouteStop routeStop) {
        Session s = this.factory.getObject().getCurrentSession();
        if (routeStop.getId() == null) {
            s.persist(routeStop);
        } else {
            s.merge(routeStop);
        }
        return routeStop;
    }

    @Override
    public boolean update(RouteStop routeStop) {
        try {
            Session s = this.factory.getObject().getCurrentSession();
            s.merge(routeStop);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public RouteStop findById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.get(RouteStop.class, id);
    }

    @Override
    public List<RouteStop> findAll() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<RouteStop> q = b.createQuery(RouteStop.class);
        Root root = q.from(RouteStop.class);
        q.select(root);
        q.orderBy(b.asc(root.get("route").get("id")), b.asc(root.get("stopOrder")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<RouteStop> findByRouteIdOrderByStopOrder(Integer routeId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<RouteStop> q = b.createQuery(RouteStop.class);
        Root root = q.from(RouteStop.class);
        q.select(root);
        q.where(b.equal(root.get("route").get("id"), routeId));
        q.orderBy(b.asc(root.get("stopOrder")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<RouteStop> findByRouteIdAndDirectionOrderByStopOrder(Integer routeId, Integer direction) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<RouteStop> q = b.createQuery(RouteStop.class);
        Root root = q.from(RouteStop.class);
        q.select(root);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.equal(root.get("route").get("id"), routeId));
        predicates.add(b.equal(root.get("direction"), direction));
        q.where(predicates.toArray(Predicate[]::new));
        q.orderBy(b.asc(root.get("stopOrder")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<RouteStop> findByStopId(Integer stopId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<RouteStop> q = b.createQuery(RouteStop.class);
        Root root = q.from(RouteStop.class);
        q.select(root);
        q.where(b.equal(root.get("stop").get("id"), stopId));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public boolean deleteById(Integer id) {
        try {
            RouteStop routeStop = findById(id);
            if (routeStop != null) {
                Session s = this.factory.getObject().getCurrentSession();
                s.remove(routeStop);
                s.flush(); // Đảm bảo thay đổi được đẩy xuống DB ngay lập tức
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa RouteStop với ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteByRouteId(Integer routeId) {
        try {
            Session s = this.factory.getObject().getCurrentSession();
            String jpql = "DELETE FROM RouteStop rs WHERE rs.route.id = :routeId";
            Query query = s.createQuery(jpql);
            query.setParameter("routeId", routeId);
            int deletedCount = query.executeUpdate();
            return deletedCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteByRouteIdAndDirection(Integer routeId, Integer direction) {
        try {
            Session s = this.factory.getObject().getCurrentSession();
            String jpql = "DELETE FROM RouteStop rs WHERE rs.route.id = :routeId AND rs.direction = :direction";
            Query query = s.createQuery(jpql);
            query.setParameter("routeId", routeId);
            query.setParameter("direction", direction);
            int deletedCount = query.executeUpdate();
            return deletedCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Các method còn lại implement tương tự...
    @Override
    public boolean existsByStopId(Integer stopId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(RouteStop.class);
        q.select(b.count(root));
        q.where(b.equal(root.get("stop").get("id"), stopId));

        Query query = s.createQuery(q);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    @Override
    public Integer findMaxStopOrderByRouteId(Integer routeId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Integer> q = b.createQuery(Integer.class);
        Root root = q.from(RouteStop.class);
        q.select(b.max(root.get("stopOrder")));
        q.where(b.equal(root.get("route").get("id"), routeId));

        Query query = s.createQuery(q);
        Integer maxOrder = (Integer) query.getSingleResult();
        return maxOrder != null ? maxOrder : 0;
    }

    @Override
    public Integer findMaxStopOrderByRouteIdAndDirection(Integer routeId, Integer direction) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Integer> q = b.createQuery(Integer.class);
        Root root = q.from(RouteStop.class);
        q.select(b.max(root.get("stopOrder")));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.equal(root.get("route").get("id"), routeId));
        predicates.add(b.equal(root.get("direction"), direction));
        q.where(predicates.toArray(Predicate[]::new));

        Query query = s.createQuery(q);
        Integer maxOrder = (Integer) query.getSingleResult();
        return maxOrder != null ? maxOrder : 0;
    }

    @Override
    public boolean swapStopOrders(Integer firstStopId, Integer secondStopId) {
        try {
            // Lấy thông tin hai trạm
            RouteStop first = findById(firstStopId);
            RouteStop second = findById(secondStopId);

            if (first == null || second == null) {
                return false;
            }

            // Lưu thứ tự ban đầu
            int firstOrder = first.getStopOrder();
            int secondOrder = second.getStopOrder();

            // Sử dụng một giá trị tạm thời lớn không có khả năng xung đột
            // Đặt giá trị tạm thời -999999 cho trạm thứ nhất
            entityManager.createNativeQuery(
                    "UPDATE route_stops SET stop_order = -999999 WHERE id = :id")
                    .setParameter("id", firstStopId)
                    .executeUpdate();

            // Cập nhật thứ tự cho trạm thứ hai
            entityManager.createNativeQuery(
                    "UPDATE route_stops SET stop_order = :newOrder WHERE id = :id")
                    .setParameter("newOrder", firstOrder)
                    .setParameter("id", secondStopId)
                    .executeUpdate();

            // Cập nhật thứ tự cho trạm thứ nhất
            entityManager.createNativeQuery(
                    "UPDATE route_stops SET stop_order = :newOrder WHERE id = :id")
                    .setParameter("newOrder", secondOrder)
                    .setParameter("id", firstStopId)
                    .executeUpdate();

            // Flush để đảm bảo các thay đổi được lưu xuống DB
            entityManager.flush();
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi swap thứ tự trạm: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteAndReorder(Integer routeStopId) {
        try {
            // Lấy thông tin trạm cần xóa
            RouteStop routeStop = findById(routeStopId);
            if (routeStop == null) {
                return false;
            }

            Integer routeId = routeStop.getRoute().getId();
            Integer direction = routeStop.getDirection();
            Integer deletedOrder = routeStop.getStopOrder();

            System.out.println("Xóa trạm #" + routeStopId + " với thứ tự " + deletedOrder +
                    " từ tuyến " + routeId + ", chiều " + direction);

            // Xóa trạm
            Session s = this.factory.getObject().getCurrentSession();
            s.remove(routeStop);

            // Dịch chuyển tất cả các trạm có thứ tự lớn hơn lên trước 1 đơn vị
            String updateQuery = "UPDATE RouteStop rs SET rs.stopOrder = rs.stopOrder - 1 " +
                    "WHERE rs.route.id = :routeId AND rs.direction = :direction " +
                    "AND rs.stopOrder > :deletedOrder";

            Query query = s.createQuery(updateQuery);
            query.setParameter("routeId", routeId);
            query.setParameter("direction", direction);
            query.setParameter("deletedOrder", deletedOrder);

            int updatedCount = query.executeUpdate();
            System.out.println("Đã cập nhật " + updatedCount + " trạm còn lại");

            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa và sắp xếp lại trạm: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}