package com.pts.repositories.impl;

import com.pts.pojo.RouteTypes;
import com.pts.repositories.RouteTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class RouteTypeRepositoryImpl implements RouteTypeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<RouteTypes> getAllRouteTypes() {
        Query query = entityManager.createQuery("SELECT rt FROM RouteTypes rt", RouteTypes.class);
        return query.getResultList();
    }

    @Override
    public Optional<RouteTypes> getRouteTypeById(Integer id) {
        RouteTypes routeType = entityManager.find(RouteTypes.class, id);
        return routeType != null ? Optional.of(routeType) : Optional.empty();
    }

    @Override
    public RouteTypes save(RouteTypes routeType) {
        if (routeType.getId() == null) {
            entityManager.persist(routeType);
            return routeType;
        } else {
            return entityManager.merge(routeType);
        }
    }

}