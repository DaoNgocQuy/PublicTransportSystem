package com.pts.controllers;

import com.pts.pojo.Landmarks;
import com.pts.services.LandmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/landmarks")
public class LandmarkApiController {

    @Autowired
    private LandmarkService landmarkService;
    
    @GetMapping
    public ResponseEntity<List<Landmarks>> getAllLandmarks() {
        List<Landmarks> landmarks = landmarkService.getAllLandmarks();
        return ResponseEntity.ok(landmarks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getLandmarkById(@PathVariable Integer id) {
        Optional<Landmarks> landmark = landmarkService.getLandmarkById(id);
        if (landmark.isPresent()) {
            return ResponseEntity.ok(landmark.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy địa điểm với ID: " + id);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createLandmark(@RequestBody Landmarks landmark) {
        try {
            Landmarks savedLandmark = landmarkService.saveLandmark(landmark);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLandmark);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi tạo địa điểm: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLandmark(@PathVariable Integer id, @RequestBody Landmarks landmark) {
        if (!landmarkService.existsLandmarkById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy địa điểm với ID: " + id);
        }
        
        try {
            landmark.setId(id);
            Landmarks updatedLandmark = landmarkService.saveLandmark(landmark);
            return ResponseEntity.ok(updatedLandmark);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi cập nhật địa điểm: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLandmark(@PathVariable Integer id) {
        if (!landmarkService.existsLandmarkById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy địa điểm với ID: " + id);
        }
        
        try {
            landmarkService.deleteLandmark(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa địa điểm: " + e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Landmarks>> searchLandmarks(@RequestParam String keyword) {
        List<Landmarks> landmarks = landmarkService.searchLandmarks(keyword);
        return ResponseEntity.ok(landmarks);
    }
    
    @GetMapping("/by-name")
    public ResponseEntity<List<Landmarks>> getLandmarksByName(@RequestParam String name) {
        List<Landmarks> landmarks = landmarkService.findLandmarksByName(name);
        return ResponseEntity.ok(landmarks);
    }
    
    @GetMapping("/by-address")
    public ResponseEntity<List<Landmarks>> getLandmarksByAddress(@RequestParam String address) {
        List<Landmarks> landmarks = landmarkService.findLandmarksByAddress(address);
        return ResponseEntity.ok(landmarks);
    }
    
    @GetMapping("/by-type")
    public ResponseEntity<List<Landmarks>> getLandmarksByType(@RequestParam String type) {
        List<Landmarks> landmarks = landmarkService.findLandmarksByType(type);
        return ResponseEntity.ok(landmarks);
    }
    
    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyLandmarks(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "1000") Double radius) {
        try {
            List<Landmarks> landmarks = landmarkService.findNearbyLandmarks(lat, lng, radius);
            return ResponseEntity.ok(landmarks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi tìm địa điểm gần đây: " + e.getMessage());
        }
    }
}