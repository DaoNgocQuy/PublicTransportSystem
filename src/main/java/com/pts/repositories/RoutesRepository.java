// filepath: c:\PTS\PublicTransportSystem\src\main\java\com\pts\repositories\RoutesRepository.java
package com.pts.repositories;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

public interface RoutesRepository {

    List<Routes> findAll();

    Optional<Routes> findById(Integer id);

    Routes save(Routes route);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<Routes> findByName(String name);

    List<Routes> findByStartLocation(String startLocation);

    List<Routes> findByEndLocation(String endLocation);

    List<Routes> findByTotalStops(Integer totalStops);

    List<Routes> findByIsActive(Boolean isActive);

    List<Routes> findByRouteTypeId(Integer routeTypeId);

    List<Routes> searchRoutesByName(String keyword);

    List<Routes> findByStopId(Integer stopId);

    List<Routes> findByStopIdAndDirection(Integer stopId, Integer direction);

    List<Stops> findStopsByRouteId(Integer routeId);

    List<Stops> findStopsByRouteIdAndDirection(Integer routeId, Integer direction);

    Integer countStopsByRouteId(Integer routeId);

    Integer countStopsByRouteIdAndDirection(Integer routeId, Integer direction);

    void updateTotalStops(Integer routeId);

    void updateRouteOperationDetails(Integer routeId, Time startTime, Time endTime, Integer frequencyMinutes);
    List<Routes> findAllWithPagination(int offset, int limit);
    int countAll();
    
    // Phân trang cho tìm kiếm
    List<Routes> searchRoutesByNameWithPagination(String keyword, int offset, int limit);
    int countByNameContaining(String keyword);
}
