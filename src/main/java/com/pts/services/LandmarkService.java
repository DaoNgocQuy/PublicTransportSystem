package com.pts.services;

import com.pts.pojo.Landmarks;
import java.util.List;
import java.util.Optional;

public interface LandmarkService {
    
    List<Landmarks> getAllLandmarks();
    
    Optional<Landmarks> getLandmarkById(Integer id);
    
    Landmarks saveLandmark(Landmarks landmark);
    
    void deleteLandmark(Integer id);
    
    boolean existsLandmarkById(Integer id);
    
    List<Landmarks> findLandmarksByName(String name);
    
    List<Landmarks> findLandmarksByAddress(String address);
    
    List<Landmarks> findLandmarksByType(String type);
    
    List<Landmarks> searchLandmarks(String keyword);
    
    List<Landmarks> findNearbyLandmarks(double latitude, double longitude, double radius);
}