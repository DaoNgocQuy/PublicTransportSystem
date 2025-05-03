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
            boolean success = routeService.createRoute(route);
            if (success) {
                return ResponseEntity.ok("Route created successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to create route");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoute(@PathVariable Integer id, @RequestBody Routes route) {
        try {
            route.setId(id);
            boolean success = routeService.updateRoute(route);
            if (success) {
                return ResponseEntity.ok("Route updated successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to update route");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoute(@PathVariable Integer id) {
        try {
            boolean success = routeService.deleteRoute(id);
            if (success) {
                return ResponseEntity.ok("Route deleted successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to delete route");
            }
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