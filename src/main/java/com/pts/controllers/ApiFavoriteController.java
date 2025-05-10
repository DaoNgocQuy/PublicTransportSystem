package com.pts.controllers;

import com.pts.pojo.Favorites;
import com.pts.pojo.Users;
import com.pts.services.FavoriteService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/favorites")
public class ApiFavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private ApiHelper apiHelper;
    
    @GetMapping
    public ResponseEntity<?> getUserFavorites(HttpServletRequest request) {
        try {
            Integer userId = apiHelper.getUserIdFromToken(request);
            
            if (userId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Vui lòng đăng nhập để sử dụng tính năng này");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            List<Map<String, Object>> favorites = favoriteService.getFavoritesByUserId(userId);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể lấy danh sách yêu thích");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Integer> payload, HttpServletRequest request) {
        try {
            // Lấy userId từ token
            Integer userId = apiHelper.getUserIdFromToken(request);
            
            if (userId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Vui lòng đăng nhập để sử dụng tính năng này");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            Integer routeId = payload.get("route_id");
            if (routeId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "route_id là bắt buộc");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Thực hiện thêm vào favorites
            boolean result = favoriteService.addFavorite(userId, routeId);
            System.out.println("Add favorite result: " + result);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã thêm vào danh sách yêu thích");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("Exception in addFavorite: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể thêm vào danh sách yêu thích: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{routeId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Integer routeId, HttpServletRequest request) {
        try {
            Integer userId = apiHelper.getUserIdFromToken(request);
            
            if (userId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Vui lòng đăng nhập để sử dụng tính năng này");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            boolean result = favoriteService.removeFavorite(userId, routeId);
            if (result) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Đã xóa khỏi danh sách yêu thích");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Không tìm thấy mục yêu thích");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể xóa khỏi danh sách yêu thích");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}