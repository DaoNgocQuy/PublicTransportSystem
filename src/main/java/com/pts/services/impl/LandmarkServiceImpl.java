package com.pts.services.impl;

import com.pts.pojo.Landmarks;
import com.pts.repositories.LandmarkRepository;
import com.pts.services.LandmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LandmarkServiceImpl implements LandmarkService {

    private final LandmarkRepository landmarkRepository;
    
    @Autowired
    public LandmarkServiceImpl(LandmarkRepository landmarkRepository) {
        this.landmarkRepository = landmarkRepository;
    }
    
    @Override
    public List<Landmarks> getAllLandmarks() {
        return landmarkRepository.findAll();
    }
    
    @Override
    public Optional<Landmarks> getLandmarkById(Integer id) {
        return landmarkRepository.findById(id);
    }
    
    @Override
    public Landmarks saveLandmark(Landmarks landmark) {
        return landmarkRepository.save(landmark);
    }
    
    @Override
    public void deleteLandmark(Integer id) {
        landmarkRepository.deleteById(id);
    }
    
    @Override
    public boolean existsLandmarkById(Integer id) {
        return landmarkRepository.existsById(id);
    }
    
    @Override
    public List<Landmarks> findLandmarksByName(String name) {
        return landmarkRepository.findByName(name);
    }
    
    @Override
    public List<Landmarks> findLandmarksByAddress(String address) {
        return landmarkRepository.findByAddress(address);
    }
    
    @Override
    public List<Landmarks> findLandmarksByType(String type) {
        return landmarkRepository.findByLandmarkType(type);
    }
    
    @Override
    public List<Landmarks> searchLandmarks(String keyword) {
        return landmarkRepository.searchLandmarks(keyword);
    }
    
    @Override
    public List<Landmarks> findNearbyLandmarks(double latitude, double longitude, double radius) {
        return landmarkRepository.findNearbyLandmarks(latitude, longitude, radius);
    }
}