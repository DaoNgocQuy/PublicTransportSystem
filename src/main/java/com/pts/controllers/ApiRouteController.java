package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/routes")
public class ApiRouteController {

    @Autowired
    private RouteService routeService;

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