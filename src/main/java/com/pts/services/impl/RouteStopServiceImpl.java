package com.pts.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pts.pojo.Routes;
import com.pts.pojo.RouteStop;
import com.pts.pojo.Stops;
import com.pts.repositories.RoutesRepository;
import com.pts.repositories.RouteStopRepository;
import com.pts.repositories.StopRepository;
import com.pts.services.RouteStopService;

@Service
public class RouteStopServiceImpl implements RouteStopService {

    @Autowired
    private RouteStopRepository routeStopRepository;

    @Autowired
    private RoutesRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    @Override
    public RouteStop save(RouteStop routeStop) {
        try {
            return routeStopRepository.save(routeStop);
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu RouteStop: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(RouteStop routeStop) {
        try {
            return routeStopRepository.update(routeStop);
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật RouteStop: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public RouteStop findById(Integer id) {
        try {
            return routeStopRepository.findById(id);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm RouteStop theo ID " + id + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<RouteStop> findAll() {
        try {
            return routeStopRepository.findAll();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả RouteStop: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<RouteStop> findByRouteId(Integer routeId) {
        try {
            return routeStopRepository.findByRouteIdOrderByStopOrder(routeId);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm RouteStop theo routeId " + routeId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<RouteStop> findByRouteIdAndDirection(Integer routeId, Integer direction) {
        try {
            return routeStopRepository.findByRouteIdAndDirectionOrderByStopOrder(routeId, direction);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm RouteStop theo routeId " + routeId
                    + " và direction " + direction + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<RouteStop> findByStopId(Integer stopId) {
        try {
            return routeStopRepository.findByStopId(stopId);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm RouteStop theo stopId " + stopId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        try {
            return routeStopRepository.deleteById(id);
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa RouteStop theo ID " + id + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteByRouteId(Integer routeId) {
        try {
            return routeStopRepository.deleteByRouteId(routeId);
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa RouteStop theo routeId " + routeId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteByRouteIdAndDirection(Integer routeId, Integer direction) {
        try {
            System.out.println("Đang xóa các trạm dừng cho tuyến " + routeId + " và chiều " + direction);
            return routeStopRepository.deleteByRouteIdAndDirection(routeId, direction);
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa RouteStop theo routeId " + routeId
                    + " và direction " + direction + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean reorderStops(Integer routeId, List<Integer> stopIds) {
        return reorderStops(routeId, stopIds, 1); // Mặc định là chiều đi (1)
    }

    @Override
    @Transactional
    public boolean reorderStops(Integer routeId, List<Integer> stopIds, Integer direction) {
        try {
            // Kiểm tra nếu danh sách trống
            if (stopIds == null || stopIds.isEmpty()) {
                System.out.println("Danh sách trạm dừng trống cho tuyến " + routeId
                        + ", chiều " + direction + ". Xóa tất cả trạm hiện tại.");
                return routeStopRepository.deleteByRouteIdAndDirection(routeId, direction);
            }

            Optional<Routes> routeOpt = routeRepository.findById(routeId);
            if (!routeOpt.isPresent()) {
                System.err.println("Không tìm thấy tuyến với ID: " + routeId);
                return false;
            }

            Routes route = routeOpt.get();
            System.out.println("Đang cập nhật thứ tự trạm dừng cho tuyến: " + route.getName()
                    + " (ID: " + routeId + "), chiều: " + (direction == 1 ? "đi" : "về"));

            // Tạo bản sao danh sách trạm hiện tại để ghi log thay đổi
            List<RouteStop> existingStops = routeStopRepository.findByRouteIdAndDirectionOrderByStopOrder(routeId, direction);
            System.out.println("Số trạm hiện tại: " + (existingStops != null ? existingStops.size() : 0));

            // Xóa các trạm theo tuyến và chiều cụ thể
            routeStopRepository.deleteByRouteIdAndDirection(routeId, direction);
            System.out.println("Đã xóa các trạm dừng hiện tại");

            // Thêm lại các trạm theo thứ tự mới
            int order = 1;
            int addedCount = 0;
            for (Integer stopId : stopIds) {
                Optional<Stops> stopOpt = stopRepository.findById(stopId);
                if (!stopOpt.isPresent()) {
                    System.err.println("Không tìm thấy trạm với ID: " + stopId);
                    continue;
                }

                Stops stop = stopOpt.get();

                RouteStop routeStop = new RouteStop();
                routeStop.setRoute(route);
                routeStop.setStop(stop);
                routeStop.setStopOrder(order++);
                routeStop.setDirection(direction);

                routeStopRepository.save(routeStop);
                addedCount++;
                System.out.println("Đã thêm trạm " + stop.getStopName()
                        + " vào vị trí " + (order - 1) + " cho tuyến " + routeId);
            }

            System.out.println("Đã hoàn thành cập nhật " + addedCount + " trạm dừng cho tuyến " + routeId);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật thứ tự trạm dừng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public RouteStop addStopToRoute(Integer routeId, Integer stopId) {
        return addStopToRoute(routeId, stopId, 1); // Mặc định là chiều đi (1)
    }

    @Override
    public RouteStop addStopToRoute(Integer routeId, Integer stopId, Integer direction) {
        try {
            Optional<Routes> routeOpt = routeRepository.findById(routeId);
            Optional<Stops> stopOpt = stopRepository.findById(stopId);

            if (!routeOpt.isPresent() || !stopOpt.isPresent()) {
                System.err.println("Không tìm thấy tuyến hoặc trạm dừng");
                return null;
            }

            // Kiểm tra xem trạm đã tồn tại trong tuyến và chiều này chưa
            List<RouteStop> existingStops = findByRouteIdAndDirection(routeId, direction);
            for (RouteStop rs : existingStops) {
                if (rs.getStop().getId().equals(stopId)) {
                    System.out.println("Trạm đã tồn tại trong tuyến và chiều này");
                    return rs; // Trạm đã tồn tại
                }
            }

            // Tính toán stopOrder mới
            int newOrder = 1;
            if (!existingStops.isEmpty()) {
                // Lấy stopOrder lớn nhất + 1
                newOrder = existingStops.stream()
                        .mapToInt(RouteStop::getStopOrder)
                        .max()
                        .orElse(0) + 1;
            }

            RouteStop routeStop = new RouteStop();
            routeStop.setRoute(routeOpt.get());
            routeStop.setStop(stopOpt.get());
            routeStop.setDirection(direction);
            routeStop.setStopOrder(newOrder);

            return save(routeStop);
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm trạm vào tuyến: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean swapStopOrder(Integer routeStopId1, Integer routeStopId2) {
        try {
            RouteStop routeStop1 = routeStopRepository.findById(routeStopId1);
            RouteStop routeStop2 = routeStopRepository.findById(routeStopId2);

            if (routeStop1 == null || routeStop2 == null) {
                System.err.println("Không tìm thấy một hoặc cả hai RouteStop");
                return false;
            }

            // Kiểm tra xem hai trạm có cùng tuyến và chiều không
            if (!routeStop1.getRoute().getId().equals(routeStop2.getRoute().getId())
                    || !routeStop1.getDirection().equals(routeStop2.getDirection())) {
                System.err.println("Hai trạm không thuộc cùng tuyến hoặc hướng");
                return false;
            }

            // Hoán đổi vị trí
            Integer tempOrder = routeStop1.getStopOrder();
            routeStop1.setStopOrder(routeStop2.getStopOrder());
            routeStop2.setStopOrder(tempOrder);

            // Lưu thay đổi
            routeStopRepository.update(routeStop1);
            routeStopRepository.update(routeStop2);

            System.out.println("Đã hoán đổi vị trí giữa trạm " + routeStop1.getStop().getStopName()
                    + " và " + routeStop2.getStop().getStopName());

            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi hoán đổi vị trí trạm dừng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Stops> getAvailableStopsForRoute(Integer routeId, Integer direction) {
        try {
            List<RouteStop> existingRouteStops = findByRouteIdAndDirection(routeId, direction);
            List<Integer> existingStopIds = existingRouteStops.stream()
                    .map(rs -> rs.getStop().getId())
                    .collect(Collectors.toList());

            List<Stops> allStops = stopRepository.findAll();
            return allStops.stream()
                    .filter(stop -> !existingStopIds.contains(stop.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách trạm có thể thêm: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getStopCoordinatesForRoute(Integer routeId, Integer direction) {
        try {
            List<RouteStop> routeStops = findByRouteIdAndDirection(routeId, direction);
            List<Map<String, Object>> result = new ArrayList<>();

            for (RouteStop rs : routeStops) {
                if (rs.getStop() != null && rs.getStop().getLatitude() != null && rs.getStop().getLongitude() != null) {
                    Map<String, Object> stopData = new HashMap<>();
                    stopData.put("id", rs.getStop().getId());
                    stopData.put("name", rs.getStop().getStopName());
                    stopData.put("lat", rs.getStop().getLatitude());
                    stopData.put("lng", rs.getStop().getLongitude());
                    stopData.put("order", rs.getStopOrder());
                    result.add(stopData);
                }
            }

            System.out.println("Đã lấy " + result.size() + " tọa độ trạm cho tuyến " + routeId
                    + ", chiều " + (direction == 1 ? "đi" : "về"));
            return result;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tọa độ trạm dừng: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean moveStopUp(Integer routeStopId) {
        try {
            RouteStop currentStop = routeStopRepository.findById(routeStopId);
            if (currentStop == null) {
                return false;
            }

            Integer routeId = currentStop.getRoute().getId();
            Integer direction = currentStop.getDirection();
            Integer currentOrder = currentStop.getStopOrder();

            if (currentOrder <= 1) {
                // Đã là trạm đầu tiên, không thể di chuyển lên nữa
                return false;
            }

            // Tìm trạm có thứ tự trước đó
            List<RouteStop> allStopsInRoute = findByRouteIdAndDirection(routeId, direction);
            RouteStop prevStop = null;
            for (RouteStop rs : allStopsInRoute) {
                if (rs.getStopOrder() == currentOrder - 1) {
                    prevStop = rs;
                    break;
                }
            }

            if (prevStop != null) {
                // Hoán đổi thứ tự với trạm trước đó
                currentStop.setStopOrder(currentOrder - 1);
                prevStop.setStopOrder(currentOrder);

                routeStopRepository.update(currentStop);
                routeStopRepository.update(prevStop);
                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("Lỗi khi di chuyển trạm lên: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean moveStopDown(Integer routeStopId) {
        try {
            RouteStop currentStop = routeStopRepository.findById(routeStopId);
            if (currentStop == null) {
                return false;
            }

            Integer routeId = currentStop.getRoute().getId();
            Integer direction = currentStop.getDirection();
            Integer currentOrder = currentStop.getStopOrder();

            List<RouteStop> allStopsInRoute = findByRouteIdAndDirection(routeId, direction);

            // Tìm thứ tự lớn nhất
            int maxOrder = allStopsInRoute.stream()
                    .mapToInt(RouteStop::getStopOrder)
                    .max()
                    .orElse(0);

            if (currentOrder >= maxOrder) {
                // Đã là trạm cuối cùng, không thể di chuyển xuống nữa
                return false;
            }

            // Tìm trạm có thứ tự sau đó
            RouteStop nextStop = null;
            for (RouteStop rs : allStopsInRoute) {
                if (rs.getStopOrder() == currentOrder + 1) {
                    nextStop = rs;
                    break;
                }
            }

            if (nextStop != null) {
                // Hoán đổi thứ tự với trạm sau đó
                currentStop.setStopOrder(currentOrder + 1);
                nextStop.setStopOrder(currentOrder);

                routeStopRepository.update(currentStop);
                routeStopRepository.update(nextStop);
                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("Lỗi khi di chuyển trạm xuống: " + e.getMessage());
            return false;
        }
    }
}
