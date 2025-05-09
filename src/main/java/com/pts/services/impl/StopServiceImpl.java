package com.pts.services.impl;

import com.pts.pojo.Stops;
import com.pts.repositories.StopRepository;
import com.pts.services.StopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StopServiceImpl implements StopService {

    @Autowired
    private StopRepository stopRepository;

    @Override
    public List<Stops> getAllStops() {
        return stopRepository.findAll();
    }

    @Override
    public Optional<Stops> getStopById(Integer id) {
        return stopRepository.findById(id);
    }

    @Override
    public Stops saveStop(Stops stop) {
        // Thiết lập giá trị mặc định nếu chưa có

        if (stop.getIsAccessible() == null) {
            stop.setIsAccessible(true);
        }

        return stopRepository.save(stop);
    }

    @Override
    public void deleteStop(Integer id) {
        stopRepository.deleteById(id);
    }

    @Override
    public boolean stopExists(Integer id) {
        return stopRepository.existsById(id);
    }

    @Override
    public List<Stops> findStopsByName(String stopName) {
        return stopRepository.findByStopName(stopName);
    }

    @Override
    public List<Stops> findStopsByAddress(String address) {
        return stopRepository.findByAddress(address);
    }

    @Override
    public List<Stops> findStopsByRouteId(Integer routeId) {
        return stopRepository.findByRouteId(routeId);
    }

    @Override
    public List<Stops> searchStops(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStops();
        }
        return stopRepository.searchStops(keyword);
    }
    // Thêm vào StopServiceImpl.java

    @Override
    public List<Stops> findNearbyStops(double latitude, double longitude, double radius) {
        return stopRepository.findNearbyStops(latitude, longitude, radius);
    }
}
