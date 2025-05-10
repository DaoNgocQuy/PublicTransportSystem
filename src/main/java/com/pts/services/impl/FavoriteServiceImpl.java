package com.pts.services.impl;

import com.pts.repositories.FavoriteRepository;
import com.pts.services.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Override
    public List<Map<String, Object>> getFavoritesByUserId(Integer userId) {
        return favoriteRepository.getFavoritesByUserId(userId);
    }

    @Override
    public boolean addFavorite(Integer userId, Integer routeId) {
        return favoriteRepository.addFavorite(userId, routeId);
    }

    @Override
    public boolean removeFavorite(Integer userId, Integer routeId) {
        return favoriteRepository.removeFavorite(userId, routeId);
    }

    @Override
    public boolean isFavorite(Integer userId, Integer routeId) {
        return favoriteRepository.isFavorite(userId, routeId);
    }
}