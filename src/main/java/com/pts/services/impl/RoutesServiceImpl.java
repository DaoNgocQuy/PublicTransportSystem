package com.pts.services.impl;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import com.pts.pojo.RouteStop;
import com.pts.pojo.Schedules;
import com.pts.repositories.RoutesRepository;
import com.pts.repositories.RouteStopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.pts.services.RouteService;
import com.pts.services.StopService;
import com.pts.services.ScheduleService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.sql.Time;
import java.util.Calendar;
import java.util.Collections;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Service
public class RoutesServiceImpl implements RouteService {

    @Autowired
    private RoutesRepository routesRepository;

    @Autowired
    private StopService stopService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RouteStopRepository routeStopRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Routes> getAllRoutes() {
        return routesRepository.findAll();
    }

    @Override
    public Optional<Routes> getRouteById(Integer id) {
        return routesRepository.findById(id);
    }

    @Override
    public Routes saveRoute(Routes route) {
        // Set default values
        if (route.getActive() == null) {
            route.setActive(true);
        }

        if (route.getIsWalkingRoute() == null) {
            route.setIsWalkingRoute(false);
        }

        // Save route without calculated fields first
        Routes savedRoute = routesRepository.save(route);

        if (savedRoute.getId() != null) {
            // Update calculated fields
            updateRouteCalculatedFields(savedRoute.getId());
        }

        // Return the route with updated fields
        return routesRepository.findById(savedRoute.getId()).orElse(savedRoute);
    }

    /**
     * Updates calculated fields for a route: - total_stops: Count of stops on
     * the route - operation_start_time: Time of first scheduled departure -
     * operation_end_time: Time of last scheduled departure - frequency_minutes:
     * Average time between schedules
     */
    @Override
    public void deleteRoute(Integer id) {
        // Delete route_stops links first
        routeStopRepository.deleteByRouteId(id);

        // Then delete the route
        routesRepository.deleteById(id);
    }

    @Override
    public boolean routeExists(Integer id) {
        return routesRepository.existsById(id);
    }

    @Override
    public List<Routes> findRoutesByName(String name) {
        return routesRepository.findByName(name);
    }

    @Override
    public List<Routes> findRoutesByStartLocation(String startLocation) {
        return routesRepository.findByStartLocation(startLocation);
    }

    @Override
    public List<Routes> findRoutesByEndLocation(String endLocation) {
        return routesRepository.findByEndLocation(endLocation);
    }

    @Override
    public List<Routes> findActiveRoutes() {
        return routesRepository.findByIsActive(true);
    }

    @Override
    public List<Routes> findWalkingRoutes() {
        return routesRepository.findByIsWalkingRoute(true);
    }

    @Override
    public List<Routes> findRoutesByRouteType(Integer routeTypeId) {
        return routesRepository.findByRouteTypeId(routeTypeId);
    }

    @Override
    public List<Routes> searchRoutesByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllRoutes();
        }
        return routesRepository.searchRoutesByName(keyword);
    }

    @Override
    public List<Routes> findRoutesByStops(List<Integer> stopIds) {
        if (stopIds == null || stopIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all routes
        List<Routes> allRoutes = getAllRoutes();

        // Filter routes that pass through all specified stops
        return allRoutes.stream()
                .filter(route -> {
                    // Get all stops for this route
                    List<Stops> routeStops = routesRepository.findStopsByRouteId(route.getId());
                    List<Integer> routeStopIds = routeStops.stream()
                            .map(Stops::getId)
                            .collect(Collectors.toList());

                    // Check if route contains all specified stops
                    return routeStopIds.containsAll(stopIds);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Routes> findRoutesByStopAndDirection(Integer stopId, Integer direction) {
        return routesRepository.findByStopIdAndDirection(stopId, direction);
    }

    @Override
    public List<Stops> getStopsByRouteId(Integer routeId) {
        return routesRepository.findStopsByRouteId(routeId);
    }

    @Override
    public List<Stops> getStopsByRouteIdAndDirection(Integer routeId, Integer direction) {
        return routesRepository.findStopsByRouteIdAndDirection(routeId, direction);
    }

    @Override
    public List<Map<String, Object>> findRoutesWithStops(double fromLat, double fromLng,
            double toLat, double toLng, double maxWalkDistance,
            int maxTransfers, String routePriority) {

        List<Map<String, Object>> result = new ArrayList<>();

        try {
            double effectiveMaxDistance = Math.min(maxWalkDistance, 1000);

            // 1. Find stops near start and end points
            List<Map<String, Object>> fromStops = stopService.findNearbyStopsFormatted(fromLat, fromLng, effectiveMaxDistance);
            List<Map<String, Object>> toStops = stopService.findNearbyStopsFormatted(toLat, toLng, effectiveMaxDistance);

            // 2. Find routes that go from start to end
            List<Routes> directRoutes = findDirectRoutes(fromStops, toStops);

            // 3. If maxTransfers > 0, find routes that require transfers
            List<List<Routes>> transferRoutes = new ArrayList<>();
            if (maxTransfers > 0) {
                transferRoutes = findTransferRoutes(fromStops, toStops, maxTransfers);
            }

            // 4. Format results
            result = formatRoutesResult(directRoutes, transferRoutes, fromStops, toStops,
                    fromLat, fromLng, toLat, toLng, routePriority);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // Find direct routes (no transfers needed)
    private List<Routes> findDirectRoutes(List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops) {
        List<Routes> directRoutes = new ArrayList<>();
        Set<Integer> fromStopIds = extractStopIds(fromStops);
        Set<Integer> toStopIds = extractStopIds(toStops);

        if (fromStopIds.isEmpty() || toStopIds.isEmpty()) {
            return directRoutes;
        }

        // Get all routes that pass through the first stop
        Set<Routes> potentialRoutes = new HashSet<>();
        for (Integer stopId : fromStopIds) {
            List<Routes> routes = routesRepository.findByStopId(stopId);
            potentialRoutes.addAll(routes);
        }

        // Check which of these routes also pass through the end stop
        for (Routes route : potentialRoutes) {
            List<Stops> routeStops = routesRepository.findStopsByRouteId(route.getId());
            Set<Integer> routeStopIds = routeStops.stream()
                    .map(Stops::getId)
                    .collect(Collectors.toSet());

            boolean hasToStop = false;
            for (Integer toId : toStopIds) {
                if (routeStopIds.contains(toId)) {
                    hasToStop = true;
                    break;
                }
            }

            if (hasToStop) {
                directRoutes.add(route);
            }
        }

        return directRoutes;
    }

    // Extract stop IDs from the list of Map objects
    private Set<Integer> extractStopIds(List<Map<String, Object>> stops) {
        Set<Integer> stopIds = new HashSet<>();

        for (Map<String, Object> stop : stops) {
            if (stop.containsKey("id")) {
                Object idObj = stop.get("id");
                if (idObj instanceof Integer) {
                    stopIds.add((Integer) idObj);
                } else if (idObj instanceof String) {
                    try {
                        stopIds.add(Integer.parseInt((String) idObj));
                    } catch (NumberFormatException e) {
                        // Skip if conversion fails
                    }
                }
            }
        }

        return stopIds;
    }

    // Find routes that require transfers
    private List<List<Routes>> findTransferRoutes(List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops, int maxTransfers) {
        List<List<Routes>> transferRoutes = new ArrayList<>();
        Set<Integer> fromStopIds = extractStopIds(fromStops);
        Set<Integer> toStopIds = extractStopIds(toStops);

        if (maxTransfers <= 0 || fromStopIds.isEmpty() || toStopIds.isEmpty()) {
            return transferRoutes;
        }

        // Find all routes passing through starting points
        Set<Routes> startRoutes = new HashSet<>();
        for (Integer stopId : fromStopIds) {
            startRoutes.addAll(routesRepository.findByStopId(stopId));
        }

        // Find all routes passing through destination points
        Set<Routes> endRoutes = new HashSet<>();
        for (Integer stopId : toStopIds) {
            endRoutes.addAll(routesRepository.findByStopId(stopId));
        }

        // Remove direct routes as they're already handled separately
        startRoutes.removeAll(endRoutes);

        // Find routes with transfers (maxTransfers = 1)
        for (Routes startRoute : startRoutes) {
            // Get all stops on the starting route
            List<Stops> startRouteStops = routesRepository.findStopsByRouteId(startRoute.getId());

            // For each stop on the starting route, check if any routes go from here to destination
            for (Stops transferStop : startRouteStops) {
                List<Routes> transferRoutesList = routesRepository.findByStopId(transferStop.getId());

                for (Routes transferRoute : transferRoutesList) {
                    // Skip if it's the same as starting route to avoid duplicates
                    if (transferRoute.getId().equals(startRoute.getId())) {
                        continue;
                    }

                    // Check if transfer route passes through destination
                    List<Stops> transferRouteStops = routesRepository.findStopsByRouteId(transferRoute.getId());
                    Set<Integer> transferRouteStopIds = transferRouteStops.stream()
                            .map(Stops::getId)
                            .collect(Collectors.toSet());

                    boolean hasEndStop = false;
                    for (Integer endStopId : toStopIds) {
                        if (transferRouteStopIds.contains(endStopId)) {
                            hasEndStop = true;
                            break;
                        }
                    }

                    if (hasEndStop) {
                        // Found a valid route sequence with 1 transfer
                        List<Routes> routeSequence = new ArrayList<>();
                        routeSequence.add(startRoute);
                        routeSequence.add(transferRoute);
                        transferRoutes.add(routeSequence);
                    }
                }
            }
        }

        return transferRoutes;
    }

    // Format results for return
    private List<Map<String, Object>> formatRoutesResult(List<Routes> directRoutes, List<List<Routes>> transferRoutes,
            List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops,
            double fromLat, double fromLng, double toLat, double toLng, String routePriority) {

        List<Map<String, Object>> result = new ArrayList<>();

        // Process direct routes
        int optionId = 1;
        for (Routes route : directRoutes) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", optionId++);
            option.put("name", "Tuyến " + route.getName());
            option.put("routeId", route.getId());
            option.put("totalTime", calculateEstimatedTime(route, fromStops, toStops));
            option.put("totalDistance", calculateEstimatedDistance(route, fromLat, fromLng, toLat, toLng) * 1000); // Convert to meters
            option.put("walkingDistance", calculateWalkingDistance(fromStops, toStops, fromLat, fromLng, toLat, toLng));
            option.put("transfers", 0);

            // Journey information
            List<Map<String, Object>> legs = createLegs(route, fromStops, toStops, fromLat, fromLng, toLat, toLng);
            option.put("legs", legs);

            // Add route display information
            List<Map<String, Object>> routes = new ArrayList<>();
            Map<String, Object> routeInfo = new HashMap<>();
            routeInfo.put("number", route.getId().toString());
            routeInfo.put("name", route.getName());
            routeInfo.put("color", route.getRouteTypeColor() != null ? route.getRouteTypeColor() : "#4CAF50");
            routes.add(routeInfo);
            option.put("routes", routes);

            result.add(option);
        }

        // Process routes that require transfers
        for (List<Routes> transferRoute : transferRoutes) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", optionId++);

            StringBuilder nameBuilder = new StringBuilder("Tuyến ");
            for (int i = 0; i < transferRoute.size(); i++) {
                if (i > 0) {
                    nameBuilder.append(" → ");
                }
                nameBuilder.append(transferRoute.get(i).getName());
            }
            option.put("name", nameBuilder.toString());

            // Calculate journey parameters
            int totalTime = 0;
            double totalDistance = 0;
            double walkingDistance = 0;

            for (Routes route : transferRoute) {
                totalTime += calculateEstimatedTime(route, fromStops, toStops);
                totalDistance += calculateEstimatedDistance(route, fromLat, fromLng, toLat, toLng) / transferRoute.size();
            }

            // Set parameters
            option.put("totalTime", totalTime);
            option.put("totalDistance", totalDistance);
            option.put("walkingDistance", walkingDistance);
            option.put("transfers", transferRoute.size() - 1);

            // Journey information - simplified example
            List<Map<String, Object>> legs = new ArrayList<>();
            option.put("legs", legs);

            result.add(option);
        }

        // Sort results by priority
        return sortRouteOptions(result, routePriority);
    }

    private void updateRouteCalculatedFields(Integer routeId) {
        try {
            System.out.println("Bắt đầu tính toán dữ liệu cho tuyến ID: " + routeId);

            var schedules = scheduleService.findSchedulesByRouteId(routeId);
            System.out.println("Tìm thấy " + (schedules != null ? schedules.size() : 0) + " lịch trình");

            if (schedules != null && !schedules.isEmpty()) {
                // Sort schedules by departure time
                schedules.sort(Comparator.comparing(s -> s.getDepartureTime()));

                // Set operation start time from first schedule
                Time startTime = new Time(schedules.get(0).getDepartureTime().getTime());
                System.out.println("Thời gian bắt đầu: " + startTime);

                // Set operation end time from last schedule
                Time endTime = new Time(schedules.get(schedules.size() - 1).getDepartureTime().getTime());
                System.out.println("Thời gian kết thúc: " + endTime);

                // Calculate frequency - THUẬT TOÁN ĐƠN GIẢN HƠN
                Integer frequencyMinutes = null;

                if (schedules.size() >= 2) {
                    // Đơn giản hóa thuật toán: Tính trung bình tất cả khoảng cách thời gian hợp lý
                    int totalMinutes = 0;
                    int validIntervals = 0;

                    for (int i = 0; i < schedules.size() - 1; i++) {
                        if (schedules.get(i).getDepartureTime() != null && schedules.get(i + 1).getDepartureTime() != null) {
                            long diffMs = schedules.get(i + 1).getDepartureTime().getTime()
                                    - schedules.get(i).getDepartureTime().getTime();
                            int diffMinutes = (int) (diffMs / (60 * 1000));

                            // Chấp nhận khoảng cách từ 1 phút đến 4 giờ 
                            if (diffMinutes > 0 && diffMinutes <= 240) {
                                totalMinutes += diffMinutes;
                                validIntervals++;
                                System.out.println("Khoảng cách lịch trình " + i + " và " + (i + 1) + ": " + diffMinutes + " phút");
                            }
                        }
                    }

                    if (validIntervals > 0) {
                        frequencyMinutes = totalMinutes / validIntervals;
                        System.out.println("Tần suất tính được: " + frequencyMinutes + " phút ("
                                + totalMinutes + " / " + validIntervals + ")");
                    } else {
                        // Nếu không có khoảng thời gian hợp lệ, sử dụng phương pháp khác: thời gian hoạt động / số chuyến
                        long operatingMs = endTime.getTime() - startTime.getTime();
                        int operatingMinutes = (int) (operatingMs / (60 * 1000));

                        if (operatingMinutes > 0) {
                            // Số chuyến - 1 = số khoảng thời gian giữa các chuyến
                            frequencyMinutes = operatingMinutes / Math.max(1, schedules.size() - 1);
                            System.out.println("Tần suất tính theo thời gian hoạt động: " + frequencyMinutes + " phút");
                        } else {
                            // Giá trị mặc định
                            frequencyMinutes = 30;
                            System.out.println("Sử dụng tần suất mặc định: " + frequencyMinutes + " phút");
                        }
                    }
                } else {
                    // Nếu chỉ có 1 lịch trình, đặt giá trị mặc định
                    frequencyMinutes = 60;
                    System.out.println("Chỉ có 1 lịch trình, sử dụng giá trị mặc định: " + frequencyMinutes + " phút");
                }

                // Đảm bảo luôn có giá trị hợp lệ cho frequency_minutes (không được null)
                if (frequencyMinutes == null || frequencyMinutes <= 0) {
                    frequencyMinutes = 30; // Mặc định 30 phút nếu tính ra giá trị không hợp lệ
                    System.out.println("Frequency không hợp lệ, sử dụng mặc định: " + frequencyMinutes + " phút");
                }

                // Update the route with calculated values using JDBC directly for better debugging
                String sql = "UPDATE routes SET operation_start_time = ?, operation_end_time = ?, frequency_minutes = ? WHERE id = ?";
                int rowsUpdated = jdbcTemplate.update(sql, startTime, endTime, frequencyMinutes, routeId);
                System.out.println("Cập nhật " + rowsUpdated + " dòng: start=" + startTime
                        + ", end=" + endTime + ", freq=" + frequencyMinutes);

                // Double-check if the update was successful
                String checkSql = "SELECT frequency_minutes FROM routes WHERE id = ?";
                Integer savedFreq = jdbcTemplate.queryForObject(checkSql, Integer.class, routeId);
                System.out.println("Kiểm tra frequency đã lưu: " + savedFreq);

            } else {
                System.out.println("Không tìm thấy lịch trình cho tuyến ID " + routeId);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tính toán dữ liệu tuyến ID " + routeId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Integer calculateFrequency(List<Schedules> schedules) {
        if (schedules == null || schedules.size() < 2) {
            System.out.println("Not enough schedules to calculate frequency");
            return null;
        }

        System.out.println("Calculating frequency from " + schedules.size() + " schedules");

        // Group by hour of day
        Map<Integer, List<Schedules>> schedulesByHour = new HashMap<>();

        for (Schedules schedule : schedules) {
            if (schedule.getDepartureTime() == null) {
                continue;
            }

            // Group by hour of day
            Calendar cal = Calendar.getInstance();
            cal.setTime(schedule.getDepartureTime());
            int hour = cal.get(Calendar.HOUR_OF_DAY);

            if (!schedulesByHour.containsKey(hour)) {
                schedulesByHour.put(hour, new ArrayList<>());
            }
            schedulesByHour.get(hour).add(schedule);
        }

        System.out.println("Grouped schedules into " + schedulesByHour.size() + " hours");

        // Calculate frequencies for each hour group
        List<Integer> hourlyFrequencies = new ArrayList<>();

        for (Map.Entry<Integer, List<Schedules>> entry : schedulesByHour.entrySet()) {
            List<Schedules> hourSchedules = entry.getValue();
            int hour = entry.getKey();

            if (hourSchedules.size() >= 2) {
                // Sort by departure time
                hourSchedules.sort(Comparator.comparing(s -> s.getDepartureTime()));

                // Calculate intervals between consecutive departures
                List<Integer> intervals = new ArrayList<>();

                for (int i = 0; i < hourSchedules.size() - 1; i++) {
                    long diffMs = hourSchedules.get(i + 1).getDepartureTime().getTime()
                            - hourSchedules.get(i).getDepartureTime().getTime();
                    int diffMinutes = (int) (diffMs / (60 * 1000));

                    // Only consider reasonable intervals (1-120 minutes between services)
                    if (diffMinutes > 0 && diffMinutes <= 120) {
                        intervals.add(diffMinutes);
                    }
                }

                // Calculate median interval for this hour if we have enough data
                if (!intervals.isEmpty()) {
                    Collections.sort(intervals);
                    int median = intervals.get(intervals.size() / 2);
                    hourlyFrequencies.add(median);
                    System.out.println("Hour " + hour + ": " + hourSchedules.size() + " schedules, median frequency: " + median + " minutes");
                } else {
                    System.out.println("Hour " + hour + ": No valid intervals found");
                }
            } else {
                System.out.println("Hour " + hour + ": Not enough schedules (" + hourSchedules.size() + ")");
            }
        }

        // If we have hourly frequencies, use their median
        if (!hourlyFrequencies.isEmpty()) {
            Collections.sort(hourlyFrequencies);
            int result = hourlyFrequencies.get(hourlyFrequencies.size() / 2);
            System.out.println("Final frequency (median of hourly frequencies): " + result + " minutes");
            return result;
        }

        System.out.println("No hourly frequencies found, falling back to overall calculation");

        // Fallback: calculate overall frequency using all schedules
        List<Integer> allIntervals = new ArrayList<>();

        for (int i = 0; i < schedules.size() - 1; i++) {
            if (schedules.get(i).getDepartureTime() != null && schedules.get(i + 1).getDepartureTime() != null) {
                long diffMs = schedules.get(i + 1).getDepartureTime().getTime()
                        - schedules.get(i).getDepartureTime().getTime();
                int diffMinutes = (int) (diffMs / (60 * 1000));

                // Only count reasonable intervals
                if (diffMinutes > 0 && diffMinutes <= 120) {
                    allIntervals.add(diffMinutes);
                    System.out.println("Found interval: " + diffMinutes + " minutes");
                }
            }
        }

        if (!allIntervals.isEmpty()) {
            Collections.sort(allIntervals);
            // Use median to avoid outliers affecting the result
            int result = allIntervals.get(allIntervals.size() / 2);
            System.out.println("Final frequency (fallback calculation): " + result + " minutes");
            return result;
        }

        System.out.println("Could not calculate frequency");
        return null;
    }

    // Sort route options by priority
    private List<Map<String, Object>> sortRouteOptions(List<Map<String, Object>> options, String routePriority) {
        if (options == null || options.isEmpty()) {
            return options;
        }

        Comparator<Map<String, Object>> comparator;

        if ("LEAST_TIME".equals(routePriority)) {
            comparator = Comparator.comparingInt(o -> ((Number) o.getOrDefault("totalTime", Integer.MAX_VALUE)).intValue());
        } else if ("LEAST_DISTANCE".equals(routePriority)) {
            comparator = Comparator.comparingDouble(o -> ((Number) o.getOrDefault("totalDistance", Double.MAX_VALUE)).doubleValue());
        } else if ("LEAST_TRANSFERS".equals(routePriority)) {
            comparator = Comparator.comparingInt(o -> ((Number) o.getOrDefault("transfers", Integer.MAX_VALUE)).intValue());
        } else {
            // Default sort by time
            comparator = Comparator.comparingInt(o -> ((Number) o.getOrDefault("totalTime", Integer.MAX_VALUE)).intValue());
        }

        options.sort(comparator);
        return options;
    }

    // Calculate estimated time
    private int calculateEstimatedTime(Routes route, List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops) {
        // Find stops on this route at both ends
        Map<String, Object> fromStop = findStopOnRoute(fromStops, route.getId());
        Map<String, Object> toStop = findStopOnRoute(toStops, route.getId());

        if (fromStop == null || toStop == null) {
            return 30; // Default value
        }

        // If stop_order information is available, use it to calculate time
        Integer fromOrder = (Integer) fromStop.getOrDefault("stopOrder", 0);
        Integer toOrder = (Integer) toStop.getOrDefault("stopOrder", 0);

        // Calculate number of stops between points
        int stopsBetween = Math.abs(toOrder - fromOrder);

        // Estimate time: 2 minutes/stop + 5 minutes base
        return stopsBetween * 2 + 5;
    }

    private Map<String, Object> findStopOnRoute(List<Map<String, Object>> stops, Integer routeId) {
        // Find stop based on routes that pass through it
        for (Map<String, Object> stop : stops) {
            if (stop.containsKey("routes")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> routes = (List<Map<String, Object>>) stop.get("routes");
                for (Map<String, Object> route : routes) {
                    if (route.containsKey("id") && routeId.equals(route.get("id"))) {
                        return stop;
                    }
                }
            }
        }
        return null;
    }

    // Calculate estimated distance
    private double calculateEstimatedDistance(Routes route, double fromLat, double fromLng, double toLat, double toLng) {
        // Calculate distance between two points (km) - Haversine formula
        return calculateHaversineDistance(fromLat, fromLng, toLat, toLng);
    }

    // Calculate walking distance
    private double calculateWalkingDistance(List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops,
            double fromLat, double fromLng, double toLat, double toLng) {

        // Calculate distance from start to nearest stop
        Map<String, Object> nearestFromStop = findNearestStop(fromStops, fromLat, fromLng);
        double distanceToFirstStop = 0;

        if (nearestFromStop != null) {
            Double stopLat = getDoubleValue(nearestFromStop, "latitude", "lat");
            Double stopLng = getDoubleValue(nearestFromStop, "longitude", "lng");

            if (stopLat != null && stopLng != null) {
                distanceToFirstStop = calculateHaversineDistance(fromLat, fromLng, stopLat, stopLng) * 1000; // Convert km to m
            }
        }

        // Calculate distance from nearest stop to destination
        Map<String, Object> nearestToStop = findNearestStop(toStops, toLat, toLng);
        double distanceFromLastStop = 0;

        if (nearestToStop != null) {
            Double stopLat = getDoubleValue(nearestToStop, "latitude", "lat");
            Double stopLng = getDoubleValue(nearestToStop, "longitude", "lng");

            if (stopLat != null && stopLng != null) {
                distanceFromLastStop = calculateHaversineDistance(stopLat, stopLng, toLat, toLng) * 1000; // Convert km to m
            }
        }

        return distanceToFirstStop + distanceFromLastStop;
    }

    // Create journey legs
    private List<Map<String, Object>> createLegs(Routes route, List<Map<String, Object>> fromStops, List<Map<String, Object>> toStops,
            double fromLat, double fromLng, double toLat, double toLng) {

        List<Map<String, Object>> legs = new ArrayList<>();

        // Get nearest stop from start point
        Map<String, Object> nearestFromStop = findNearestStop(fromStops, fromLat, fromLng);

        // Calculate actual walking distance and time from start to stop
        double walkToStopDistance = 0;
        int walkToStopDuration = 0;

        if (nearestFromStop != null) {
            Double stopLat = getDoubleValue(nearestFromStop, "latitude", "lat");
            Double stopLng = getDoubleValue(nearestFromStop, "longitude", "lng");

            if (stopLat != null && stopLng != null) {
                // Calculate actual distance
                walkToStopDistance = calculateHaversineDistance(fromLat, fromLng, stopLat, stopLng) * 1000; // m
                // Calculate walking time (assume average speed 80m/minute)
                walkToStopDuration = (int) Math.ceil(walkToStopDistance / 80);
            }
        }

        // Leg from user location to first stop
        Map<String, Object> firstLeg = new HashMap<>();
        firstLeg.put("type", "WALK");
        firstLeg.put("distance", walkToStopDistance); // Actual distance
        firstLeg.put("duration", walkToStopDuration); // Actual time
        firstLeg.put("from", Map.of("lat", fromLat, "lng", fromLng, "name", "Your location"));
        firstLeg.put("to", nearestFromStop);
        legs.add(firstLeg);

        // Transit leg
        Map<String, Object> busLeg = new HashMap<>();
        busLeg.put("type", "BUS");
        busLeg.put("routeId", route.getId());
        busLeg.put("routeNumber", route.getId().toString());
        busLeg.put("routeName", route.getName());
        busLeg.put("routeColor", route.getRouteTypeColor() != null ? route.getRouteTypeColor() : "#4CAF50");
        busLeg.put("distance", calculateEstimatedDistance(route, fromLat, fromLng, toLat, toLng) * 1000); // m
        busLeg.put("duration", calculateEstimatedTime(route, fromStops, toStops));
        busLeg.put("from", nearestFromStop);

        // Get nearest stop to destination
        Map<String, Object> nearestToStop = findNearestStop(toStops, toLat, toLng);
        busLeg.put("to", nearestToStop);
        legs.add(busLeg);

        // Calculate actual walking distance and time from stop to destination
        double walkFromStopDistance = 0;
        int walkFromStopDuration = 0;

        if (nearestToStop != null) {
            Double stopLat = getDoubleValue(nearestToStop, "latitude", "lat");
            Double stopLng = getDoubleValue(nearestToStop, "longitude", "lng");

            if (stopLat != null && stopLng != null) {
                // Calculate actual distance
                walkFromStopDistance = calculateHaversineDistance(stopLat, stopLng, toLat, toLng) * 1000; // m
                // Calculate walking time (assume average speed 80m/minute)
                walkFromStopDuration = (int) Math.ceil(walkFromStopDistance / 80);
            }
        }

        // Leg from final stop to destination
        Map<String, Object> lastLeg = new HashMap<>();
        lastLeg.put("type", "WALK");
        lastLeg.put("distance", walkFromStopDistance); // Actual distance
        lastLeg.put("duration", walkFromStopDuration); // Actual time
        lastLeg.put("from", nearestToStop);
        lastLeg.put("to", Map.of("lat", toLat, "lng", toLng, "name", "Your destination"));
        legs.add(lastLeg);

        return legs;
    }

    @Override
    public void recalculateRoute(Integer routeId) {
        updateRouteCalculatedFields(routeId);
    }

    // Find nearest stop
    private Map<String, Object> findNearestStop(List<Map<String, Object>> stops, double lat, double lng) {
        if (stops == null || stops.isEmpty()) {
            return Map.of(
                    "id", 0,
                    "name", "Undefined stop",
                    "lat", lat,
                    "lng", lng
            );
        }

        Map<String, Object> nearestStop = stops.get(0);
        double minDistance = Double.MAX_VALUE;

        for (Map<String, Object> stop : stops) {
            Double stopLat = getDoubleValue(stop, "latitude", "lat");
            Double stopLng = getDoubleValue(stop, "longitude", "lng");

            if (stopLat != null && stopLng != null) {
                double distance = calculateHaversineDistance(lat, lng, stopLat, stopLng);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestStop = stop;
                }
            }
        }

        // Standardize stop format
        Map<String, Object> standardStop = new HashMap<>();
        standardStop.put("id", nearestStop.getOrDefault("id", 0));
        standardStop.put("name", nearestStop.getOrDefault("name", nearestStop.getOrDefault("stop_name", "Undefined stop")));
        standardStop.put("lat", getDoubleValue(nearestStop, "latitude", "lat"));
        standardStop.put("lng", getDoubleValue(nearestStop, "longitude", "lng"));

        return standardStop;
    }

    // Haversine formula to calculate distance between two points on Earth
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Earth radius in km
        final int R = 6371;

        // Convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance in km
        return R * c;
    }

    // Get double value from Map with alternative keys
    private Double getDoubleValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                Object value = map.get(key);
                if (value instanceof Double) {
                    return (Double) value;
                } else if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else if (value instanceof String) {
                    try {
                        return Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        // Skip error and try next key
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> findJourneyOptions(
            Double fromLat, Double fromLng, Double toLat, Double toLng,
            Integer maxWalkDistance, String priority) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> options = new ArrayList<>();

        try {
            // 1. Tìm các trạm gần điểm đi
            List<Stops> nearbyFromStops = stopService.findNearbyStops(fromLat, fromLng, maxWalkDistance);
            if (nearbyFromStops.isEmpty()) {
                result.put("error", "Không tìm thấy trạm nào gần điểm xuất phát");
                result.put("options", Collections.emptyList());
                return result;
            }

            // 2. Tìm các trạm gần điểm đến
            List<Stops> nearbyToStops = stopService.findNearbyStops(toLat, toLng, maxWalkDistance);
            if (nearbyToStops.isEmpty()) {
                result.put("error", "Không tìm thấy trạm nào gần điểm đến");
                result.put("options", Collections.emptyList());
                return result;
            }

            // 3. Tìm các tuyến đường phù hợp
            List<Map<String, Object>> journeyOptions = findJourneyOptionsForStops(
                    nearbyFromStops, nearbyToStops, fromLat, fromLng, toLat, toLng);

            // 4. Sắp xếp kết quả theo priority
            sortJourneyOptions(journeyOptions, priority);

            result.put("status", "success");
            result.put("options", journeyOptions);
            result.put("count", journeyOptions.size());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "Lỗi tìm phương án di chuyển: " + e.getMessage());
            result.put("options", Collections.emptyList());
        }

        return result;
    }

    /**
     * Tìm các phương án di chuyển giữa các cặp trạm
     */
    private List<Map<String, Object>> findJourneyOptionsForStops(
            List<Stops> fromStops, List<Stops> toStops,
            Double fromLat, Double fromLng, Double toLat, Double toLng) {

        List<Map<String, Object>> journeyOptions = new ArrayList<>();
        int optionId = 1;

        // Xem xét từng cặp trạm đi/đến
        for (Stops fromStop : fromStops) {
            for (Stops toStop : toStops) {
                // Tìm các tuyến đi qua cả hai trạm
                List<Routes> routes = findDirectRoutesForStops(fromStop.getId(), toStop.getId());

                for (Routes route : routes) {
                    // Lấy thông tin chi tiết về trạm trên tuyến
                    List<RouteStop> routeStops = routeStopRepository.findByRouteIdOrderByStopOrder(route.getId());

                    // Tìm vị trí của fromStop và toStop trên tuyến
                    RouteStop fromStopOnRoute = findRouteStopByStopId(routeStops, fromStop.getId());
                    RouteStop toStopOnRoute = findRouteStopByStopId(routeStops, toStop.getId());

                    // Nếu trạm đi phải nằm trước trạm đến trên tuyến đường
                    if (fromStopOnRoute != null && toStopOnRoute != null
                            && fromStopOnRoute.getStopOrder() < toStopOnRoute.getStopOrder()) {

                        // Tạo phương án di chuyển
                        Map<String, Object> option = createJourneyOption(
                                optionId++, route, fromStop, toStop, fromStopOnRoute, toStopOnRoute,
                                fromLat, fromLng, toLat, toLng
                        );

                        journeyOptions.add(option);
                    }
                }
            }
        }

        return journeyOptions;
    }

    /**
     * Tìm các tuyến đi qua cả trạm đi và trạm đến theo thứ tự
     */
    private List<Routes> findDirectRoutesForStops(Integer fromStopId, Integer toStopId) {
        List<Routes> result = new ArrayList<>();

        // Tìm các tuyến đi qua trạm xuất phát
        List<Routes> fromRoutes = routesRepository.findByStopId(fromStopId);

        // Kiểm tra từng tuyến xem có đi qua trạm đến không
        for (Routes route : fromRoutes) {
            List<Stops> routeStops = routesRepository.findStopsByRouteId(route.getId());

            // Kiểm tra xem tuyến có đi qua trạm đến không
            boolean containsToStop = routeStops.stream()
                    .anyMatch(stop -> stop.getId().equals(toStopId));

            if (containsToStop) {
                result.add(route);
            }
        }

        return result;
    }

    /**
     * Tạo thông tin chi tiết một phương án di chuyển
     */
    private Map<String, Object> createJourneyOption(
            int optionId, Routes route, Stops fromStop, Stops toStop,
            RouteStop fromStopOnRoute, RouteStop toStopOnRoute,
            Double originLat, Double originLng, Double destLat, Double destLng) {

        Map<String, Object> option = new HashMap<>();

        // Thông tin cơ bản
        option.put("id", optionId);
        option.put("name", "Tuyến " + route.getName());
        option.put("routeId", route.getId());

        // Tính số trạm phải đi qua
        int numStops = toStopOnRoute.getStopOrder() - fromStopOnRoute.getStopOrder();
        option.put("numStops", numStops);

        // Tính thời gian di chuyển (3 phút mỗi trạm)
        int busTime = numStops * 3;

        // Tính khoảng cách và thời gian đi bộ
        double walkToFirstStop = calculateDistance(originLat, originLng, fromStop.getLatitude(), fromStop.getLongitude());
        double walkFromLastStop = calculateDistance(toStop.getLatitude(), toStop.getLongitude(), destLat, destLng);

        double walkDistanceMeters = (walkToFirstStop + walkFromLastStop) * 1000; // Đổi sang mét
        int walkTimeMinutes = (int) Math.ceil(walkDistanceMeters / 80); // Tốc độ đi bộ ~80m/phút

        option.put("totalTime", busTime + walkTimeMinutes);
        option.put("walkingDistance", walkDistanceMeters);

        option.put("transfers", 0); // Không có chuyển tuyến

        // Thông tin chi tiết các chặng hành trình
        List<Map<String, Object>> legs = new ArrayList<>();

        // Chặng 1: Đi bộ đến trạm đầu
        Map<String, Object> walkToStopLeg = new HashMap<>();
        walkToStopLeg.put("type", "WALK");
        walkToStopLeg.put("distance", walkToFirstStop * 1000);
        walkToStopLeg.put("duration", (int) Math.ceil(walkToFirstStop * 1000 / 80));
        walkToStopLeg.put("from", Map.of(
                "name", "Vị trí của bạn",
                "lat", originLat,
                "lng", originLng
        ));
        walkToStopLeg.put("to", Map.of(
                "name", fromStop.getStopName(),
                "lat", fromStop.getLatitude(),
                "lng", fromStop.getLongitude(),
                "id", fromStop.getId()
        ));
        legs.add(walkToStopLeg);

        // Chặng 2: Đi xe buýt
        Map<String, Object> busLeg = new HashMap<>();
        double busDistanceKm = 0;
// Calculate bus distance between fromStop and toStop based on route path
// Simplified: use straight-line distance between stops
        busDistanceKm = calculateDistance(
                fromStop.getLatitude(), fromStop.getLongitude(),
                toStop.getLatitude(), toStop.getLongitude()
        );
        double busDistanceMeters = busDistanceKm * 1000;

// Set distance on the bus leg
        busLeg.put("distance", busDistanceMeters);

// Calculate and set total distance
        double totalDistance = walkDistanceMeters + busDistanceMeters;
        option.put("totalDistance", totalDistance);
        busLeg.put("type", "BUS");
        busLeg.put("routeId", route.getId());
        busLeg.put("routeNumber", route.getName());
        busLeg.put("routeName", route.getName());
        busLeg.put("duration", busTime);
        busLeg.put("stops", numStops);
        busLeg.put("from", Map.of(
                "name", fromStop.getStopName(),
                "lat", fromStop.getLatitude(),
                "lng", fromStop.getLongitude(),
                "id", fromStop.getId()
        ));
        busLeg.put("to", Map.of(
                "name", toStop.getStopName(),
                "lat", toStop.getLatitude(),
                "lng", toStop.getLongitude(),
                "id", toStop.getId()
        ));
        legs.add(busLeg);

        // Chặng 3: Đi bộ từ trạm đến điểm đích
        Map<String, Object> walkFromStopLeg = new HashMap<>();
        walkFromStopLeg.put("type", "WALK");
        walkFromStopLeg.put("distance", walkFromLastStop * 1000);
        walkFromStopLeg.put("duration", (int) Math.ceil(walkFromLastStop * 1000 / 80));
        walkFromStopLeg.put("from", Map.of(
                "name", toStop.getStopName(),
                "lat", toStop.getLatitude(),
                "lng", toStop.getLongitude(),
                "id", toStop.getId()
        ));
        walkFromStopLeg.put("to", Map.of(
                "name", "Điểm đến của bạn",
                "lat", destLat,
                "lng", destLng
        ));
        legs.add(walkFromStopLeg);

        option.put("legs", legs);

        return option;
    }

    /**
     * Tìm RouteStop cho một stopId cụ thể
     */
    private RouteStop findRouteStopByStopId(List<RouteStop> routeStops, Integer stopId) {
        return routeStops.stream()
                .filter(rs -> rs.getStop().getId().equals(stopId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Tính khoảng cách giữa hai điểm địa lý sử dụng công thức Haversine
     *
     * @return Khoảng cách tính bằng km
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the Earth in km
        final int R = 6371;

        // Convert degrees to radians
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance in km
        return R * c;
    }

    /**
     * Sắp xếp các phương án di chuyển theo tiêu chí người dùng chọn
     */
    private void sortJourneyOptions(List<Map<String, Object>> options, String priority) {
        if (priority == null || priority.isEmpty()) {
            priority = "LEAST_TIME";
        }

        Comparator<Map<String, Object>> comparator;

        switch (priority.toUpperCase()) {
            case "LEAST_WALKING":
                comparator = Comparator.comparingDouble(o -> ((Number) o.get("walkingDistance")).doubleValue());
                break;
            case "LEAST_STOPS":
                comparator = Comparator.comparingInt(o -> ((Number) o.get("numStops")).intValue());
                break;
            case "LEAST_TIME":
            default:
                comparator = Comparator.comparingInt(o -> ((Number) o.get("totalTime")).intValue());
                break;
        }

        options.sort(comparator);
    }

    @Override
    public List<List<Double>> calculateOptimalWalkingPath(Double fromLat, Double fromLng, Double toLat, Double toLng) {
        List<List<Double>> path = new ArrayList<>();

        try {
            // Tính khoảng cách trực tiếp
            double directDistance = calculateDistance(fromLat, fromLng, toLat, toLng);

            // Nếu khoảng cách nhỏ (< 300m), tạo đường thẳng
            if (directDistance < 0.3) {
                path.add(List.of(fromLat, fromLng));
                path.add(List.of(toLat, toLng));
                return path;
            }

            // Đối với khoảng cách xa hơn, sử dụng OSRM API để tính đường đi tối ưu
            String url = String.format(
                    "https://router.project-osrm.org/route/v1/foot/%f,%f;%f,%f?overview=full&geometries=geojson&alternatives=true",
                    fromLng, fromLat, toLng, toLat
            );

            // Sử dụng RestTemplate để gọi API
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "Ok".equals(response.get("code")) && response.containsKey("routes")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");

                if (!routes.isEmpty()) {
                    // Chọn đường đi ngắn nhất
                    Map<String, Object> shortestRoute = routes.get(0);
                    Double shortestDistance = (Double) shortestRoute.get("distance");

                    for (int i = 1; i < routes.size(); i++) {
                        Double distance = (Double) routes.get(i).get("distance");
                        if (distance < shortestDistance) {
                            shortestRoute = routes.get(i);
                            shortestDistance = distance;
                        }
                    }

                    // So sánh với đường thẳng - nếu đường OSRM dài hơn 30% -> dùng đường thẳng
                    if (shortestDistance > directDistance * 1300) { // Convert km to m for comparison
                        path.add(List.of(fromLat, fromLng));
                        path.add(List.of(toLat, toLng));
                    } else {
                        // Extract coordinates from GeoJSON
                        @SuppressWarnings("unchecked")
                        Map<String, Object> geometry = (Map<String, Object>) shortestRoute.get("geometry");

                        @SuppressWarnings("unchecked")
                        List<List<Double>> coordinates = (List<List<Double>>) geometry.get("coordinates");

                        // Chuyển đổi format [lng, lat] từ GeoJSON thành [lat, lng] cho frontend
                        for (List<Double> coord : coordinates) {
                            path.add(List.of(coord.get(1), coord.get(0)));
                        }
                    }
                }
            } else {
                // Fallback - direct path
                path.add(List.of(fromLat, fromLng));
                path.add(List.of(toLat, toLng));
            }
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();

            // Fallback - direct path
            path.add(List.of(fromLat, fromLng));
            path.add(List.of(toLat, toLng));
        }

        return path;
    }
}
