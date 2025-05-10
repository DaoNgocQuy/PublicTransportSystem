package com.pts.repositories.impl;

import com.pts.repositories.FavoriteRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class FavoriteRepositoryImpl implements FavoriteRepository {

    @Autowired
    private LocalSessionFactoryBean sessionFactory;
    
    @Override
    public List<Map<String, Object>> getFavoritesByUserId(Integer userId) {
        Session session = sessionFactory.getObject().getCurrentSession();
        
        Query q = session.createNativeQuery(
            "SELECT f.id, f.user_id, f.route_id, f.created_at, r.name AS route_name, " +
            "r.start_location, r.end_location, r.operation_start_time, r.operation_end_time " +
            "FROM favorites f " +
            "JOIN routes r ON f.route_id = r.id " +
            "WHERE f.user_id = :userId"
        );
        q.setParameter("userId", userId);
        
        List<Object[]> results = q.getResultList();
        List<Map<String, Object>> favorites = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> favorite = new HashMap<>();
            favorite.put("id", row[0]);
            favorite.put("user_id", row[1]);
            favorite.put("route_id", row[2]);
            favorite.put("created_at", row[3]);
            favorite.put("route_name", row[4]);
            favorite.put("start_location", row[5]);
            favorite.put("end_location", row[6]);
            favorite.put("operation_start_time", row[7]);
            favorite.put("operation_end_time", row[8]);
            favorites.add(favorite);
        }
        
        return favorites;
    }

    @Override
    public boolean addFavorite(Integer userId, Integer routeId) {
        Session session = sessionFactory.getObject().getCurrentSession();
        
        try {
            System.out.println("Adding favorite for user: " + userId + ", route: " + routeId);
            
            // Kiểm tra xem đã tồn tại chưa
            Query checkQuery = session.createNativeQuery(
                    "SELECT COUNT(*) FROM favorites WHERE user_id = :userId AND route_id = :routeId"
            );
            checkQuery.setParameter("userId", userId);
            checkQuery.setParameter("routeId", routeId);

            Long count = (Long) checkQuery.getSingleResult();
            System.out.println("Existing favorites count: " + count);

            // Nếu đã tồn tại thì return true
            if (count > 0) {
                System.out.println("Favorite already exists");
                return true;
            }
            
            // Thêm mới bằng SQL trực tiếp
            System.out.println("Inserting new favorite record");
            Query insertQuery = session.createNativeQuery(
                    "INSERT INTO favorites (user_id, route_id, created_at) VALUES (:userId, :routeId, NOW())"
            );
            insertQuery.setParameter("userId", userId);
            insertQuery.setParameter("routeId", routeId);
            
            int result = insertQuery.executeUpdate();
            System.out.println("Insert result: " + result + " row(s) affected");
            return result > 0;
        } catch (Exception e) {
            System.out.println("Error in addFavorite: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeFavorite(Integer userId, Integer routeId) {
        Session session = sessionFactory.getObject().getCurrentSession();
        
        Query q = session.createNativeQuery(
            "DELETE FROM favorites WHERE user_id = :userId AND route_id = :routeId"
        );
        q.setParameter("userId", userId);
        q.setParameter("routeId", routeId);
        
        int result = q.executeUpdate();
        return result > 0;
    }

    @Override
    public boolean isFavorite(Integer userId, Integer routeId) {
        Session session = sessionFactory.getObject().getCurrentSession();
        
        Query q = session.createNativeQuery(
            "SELECT COUNT(*) FROM favorites WHERE user_id = :userId AND route_id = :routeId"
        );
        q.setParameter("userId", userId);
        q.setParameter("routeId", routeId);
        
        Long count = (Long) q.getSingleResult();
        return count > 0;
    }
}