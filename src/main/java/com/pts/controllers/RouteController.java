package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping
    public ResponseEntity<?> createRoute(@RequestBody Routes route) {
        try {
            // TODO: Implement create route logic
            return ResponseEntity.ok("Route created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoute(@PathVariable Integer id, @RequestBody Routes route) {
        try {
            // TODO: Implement update route logic
            return ResponseEntity.ok("Route updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoute(@PathVariable Integer id) {
        try {
            // TODO: Implement delete route logic
            return ResponseEntity.ok("Route deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllRoutes() {
        try {
            List<Routes> routes = routeService.getAllRoutes();
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRouteById(@PathVariable Integer id) {
        try {
            Routes route = routeService.getRouteById(id);
            if (route != null) {
                return ResponseEntity.ok(route);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchRoutes(
            @RequestParam String startLocation,
            @RequestParam String endLocation) {
        try {
            List<Routes> routes = routeService.searchRoutes(startLocation, endLocation);
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/type")
    public ResponseEntity<?> getRoutesByType(@RequestParam boolean isWalkingRoute) {
        try {
            List<Routes> routes = routeService.getRoutesByType(isWalkingRoute);
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 