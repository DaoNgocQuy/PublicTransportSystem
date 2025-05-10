package com.pts.services;

import java.util.List;
import java.util.Map;

public interface FavoriteService {
    List<Map<String, Object>> getFavoritesByUserId(Integer userId);
    boolean addFavorite(Integer userId, Integer routeId);
    boolean removeFavorite(Integer userId, Integer routeId);
    boolean isFavorite(Integer userId, Integer routeId);
}