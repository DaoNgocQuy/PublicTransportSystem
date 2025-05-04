package com.pts.controllers;

import com.pts.pojo.Route;
import com.pts.services.RoutesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin // Cho phép truy cập từ các domain khác (nếu cần)
public class ApiRouteController {

    @Autowired
    private RoutesService routesService;

    // API: Lấy danh sách tất cả các tuyến
    @GetMapping
    public ResponseEntity<List<Route>> getAllRoutes() {
        List<Route> routes = routesService.getAllRoutes();
        return new ResponseEntity<>(routes, HttpStatus.OK);
    }

    // API: Lấy thông tin tuyến theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable Integer id) {
        Optional<Route> route = routesService.getRouteById(id);
        return route.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // API: Thêm tuyến mới
    @PostMapping
    public ResponseEntity<Route> addRoute(@RequestBody Route route) {
        Route savedRoute = routesService.saveRoute(route);
        return new ResponseEntity<>(savedRoute, HttpStatus.CREATED);
    }

    // API: Cập nhật thông tin tuyến
    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Integer id, @RequestBody Route route) {
        if (!routesService.routeExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        route.setId(id);
        Route updatedRoute = routesService.saveRoute(route);
        return new ResponseEntity<>(updatedRoute, HttpStatus.OK);
    }

    // API: Xóa tuyến theo ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoute(@PathVariable Integer id) {
        if (routesService.routeExists(id)) {
            routesService.deleteRoute(id);
        }
    }
}
