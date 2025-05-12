package com.pts.repositories;

import com.pts.pojo.Landmarks;
import java.util.List;
import java.util.Optional;

public interface LandmarkRepository {
    
    List<Landmarks> findAll();
    
    Optional<Landmarks> findById(Integer id);
    
    Landmarks save(Landmarks landmark);
    
    void deleteById(Integer id);
    
    boolean existsById(Integer id);
    
    List<Landmarks> findByName(String name);
    
    List<Landmarks> findByAddress(String address);
    
    List<Landmarks> findByLandmarkType(String landmarkType);
    
    List<Landmarks> searchLandmarks(String keyword);
    
    List<Landmarks> findNearbyLandmarks(double latitude, double longitude, double radius);
}