package com.pts.controllers;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pts.pojo.RouteStop;
import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import com.pts.services.RouteStopService;
import com.pts.services.StopService;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/route-stops")
public class RouteStopController {

    @Autowired
    private RouteStopService routeStopService;

    @Autowired
    private StopService stopService;

    @GetMapping
    public ResponseEntity<List<RouteStop>> getAllRouteStops() {
        List<RouteStop> routeStops = routeStopService.findAll();
        return new ResponseEntity<>(routeStops, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteStop> getRouteStopById(@PathVariable Integer id) {
        RouteStop routeStop = routeStopService.findById(id);
        if (routeStop != null) {
            return new ResponseEntity<>(routeStop, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<RouteStop>> getRouteStopsByRouteId(@PathVariable Integer routeId) {
        List<RouteStop> routeStops = routeStopService.findByRouteId(routeId);
        return new ResponseEntity<>(routeStops, HttpStatus.OK);
    }

    @GetMapping("/stop/{stopId}")
    public ResponseEntity<List<RouteStop>> getRouteStopsByStopId(@PathVariable Integer stopId) {
        List<RouteStop> routeStops = routeStopService.findByStopId(stopId);
        return new ResponseEntity<>(routeStops, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<RouteStop> createRouteStop(@RequestBody RouteStop routeStop) {
        RouteStop savedRouteStop = routeStopService.save(routeStop);
        if (savedRouteStop != null) {
            return new ResponseEntity<>(savedRouteStop, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/route/{routeId}/stop/{stopId}")
    public ResponseEntity<RouteStop> addStopToRoute(
            @PathVariable Integer routeId,
            @PathVariable Integer stopId,
            @RequestParam(required = false, defaultValue = "1") Integer direction,
            @RequestParam(required = false) Integer stopOrder) {

        // Luôn sử dụng phương thức addStopToRoute, với đầy đủ các tham số
        RouteStop routeStop = routeStopService.addStopToRoute(routeId, stopId, direction, stopOrder);

        if (routeStop != null) {
            return new ResponseEntity<>(routeStop, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateRouteStop(
            @PathVariable Integer id,
            @RequestBody RouteStop routeStop) {

        Map<String, String> response = new HashMap<>();
        routeStop.setId(id);
        boolean updated = routeStopService.update(routeStop);

        if (updated) {
            response.put("message", "Route stop updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("message", "Failed to update route stop");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/route/{routeId}/reorder")
    public ResponseEntity<Map<String, String>> reorderRouteStops(
            @PathVariable Integer routeId,
            @RequestBody List<Integer> stopIds) {

        Map<String, String> response = new HashMap<>();
        boolean reordered = routeStopService.reorderStops(routeId, stopIds);

        if (reordered) {
            response.put("message", "Route stops reordered successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("message", "Failed to reorder route stops");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/route/{routeId}/stops")
    public ResponseEntity<List<Stops>> getStopsByRouteId(@PathVariable Integer routeId) {
        List<Stops> stops = stopService.findStopsByRouteId(routeId);
        return new ResponseEntity<>(stops, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRouteStop(@PathVariable Integer id) {
        Map<String, String> response = new HashMap<>();
        boolean deleted = routeStopService.deleteById(id);

        if (deleted) {
            response.put("message", "Route stop deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("message", "Failed to delete route stop");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/route/{routeId}")
    public ResponseEntity<Map<String, String>> deleteRouteStopsByRouteId(@PathVariable Integer routeId) {
        Map<String, String> response = new HashMap<>();
        boolean deleted = routeStopService.deleteByRouteId(routeId);

        if (deleted) {
            response.put("message", "All stops removed from route successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("message", "Failed to remove stops from route");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/route/{routeId}/stop/{stopId}")
    public ResponseEntity<Map<String, String>> removeStopFromRoute(
            @PathVariable Integer routeId,
            @PathVariable Integer stopId) {

        Map<String, String> response = new HashMap<>();
        List<RouteStop> routeStops = routeStopService.findByRouteId(routeId);

        for (RouteStop rs : routeStops) {
            if (rs.getStop().getId().equals(stopId)) {
                boolean deleted = routeStopService.deleteById(rs.getId());
                if (deleted) {
                    response.put("message", "Stop removed from route successfully");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
            }
        }

        response.put("message", "Failed to remove stop from route");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

}
