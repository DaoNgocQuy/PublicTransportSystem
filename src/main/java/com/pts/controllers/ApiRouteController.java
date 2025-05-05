package com.pts.controllers;

import com.pts.pojo.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import com.pts.services.RouteService;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin // Cho phép truy cập từ các domain khác (nếu cần)
public class ApiRouteController {

    @Autowired
    private RouteService routesService;

    // API: Lấy danh sách tất cả các tuyến
    @GetMapping
    public ResponseEntity<List<Routes>> getAllRoutes() {
        List<Routes> routes = routesService.getAllRoutes();
        return new ResponseEntity<>(routes, HttpStatus.OK);
    }

    // API: Lấy thông tin tuyến theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Routes> getRouteById(@PathVariable Integer id) {
        Optional<Routes> route = routesService.getRouteById(id);
        return route.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // API: Thêm tuyến mới
    @PostMapping
    public ResponseEntity<Routes> addRoute(@RequestBody Routes route) {
        Routes savedRoute = routesService.saveRoute(route);
        return new ResponseEntity<>(savedRoute, HttpStatus.CREATED);
    }

    // API: Cập nhật thông tin tuyến
    @PutMapping("/{id}")
    public ResponseEntity<Routes> updateRoute(@PathVariable Integer id, @RequestBody Routes route) {
        if (!routesService.routeExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        route.setId(id);
        Routes updatedRoute = routesService.saveRoute(route);
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
