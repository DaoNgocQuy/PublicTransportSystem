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
import java.util.Comparator;
import java.util.Set;

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
            System.out.println("Cập nhật RouteStop ID=" + routeStop.getId()
                    + ", Route=" + routeStop.getRoute().getId()
                    + ", Stop=" + routeStop.getStop().getId()
                    + ", Order=" + routeStop.getStopOrder()
                    + ", Direction=" + routeStop.getDirection());
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
    @Transactional
    public RouteStop addStopToRoute(Integer routeId, Integer stopId, Integer direction, Integer stopOrder) {
        try {
            System.out.println("Thêm trạm vào tuyến: " + stopId + " -> " + routeId);
            System.out.println("Yêu cầu thứ tự trạm: " + stopOrder);

            // Lấy route và stop
            Optional<Routes> routeOpt = routeRepository.findById(routeId);
            Optional<Stops> stopOpt = stopRepository.findById(stopId);

            if (!routeOpt.isPresent() || !stopOpt.isPresent()) {
                System.err.println("Route hoặc Stop không tồn tại");
                return null;
            }

            Routes route = routeOpt.get();
            Stops stop = stopOpt.get();

            // Nếu stopOrder là null, tìm stopOrder lớn nhất và + 1
            if (stopOrder == null) {
                Integer maxOrder = findMaxStopOrderByRouteIdAndDirection(routeId, direction);
                stopOrder = (maxOrder != null) ? maxOrder + 1 : 1;
                System.out.println("Thứ tự trạm mới: " + stopOrder);
            } else {
                // Dời các trạm có stop_order >= stopOrder lên 1 đơn vị
                shiftStopOrders(routeId, direction, stopOrder);
            }

            // Tạo RouteStop mới
            RouteStop routeStop = new RouteStop();
            routeStop.setRoute(route);
            routeStop.setStop(stop);
            routeStop.setDirection(direction);
            routeStop.setStopOrder(stopOrder);

            return routeStopRepository.save(routeStop);
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm trạm vào tuyến: " + e.getMessage());
            e.printStackTrace();
            throw e; // Ném lại ngoại lệ để transaction được rollback
        }
    }

    /**
     * Tìm stopOrder lớn nhất cho một tuyến và chiều
     */
    public Integer findMaxStopOrderByRouteIdAndDirection(Integer routeId, Integer direction) {
        List<RouteStop> routeStops = routeStopRepository.findByRouteIdAndDirectionOrderByStopOrder(routeId, direction);
        if (routeStops == null || routeStops.isEmpty()) {
            return 0;
        }

        return routeStops.stream()
                .mapToInt(RouteStop::getStopOrder)
                .max()
                .orElse(0);
    }

    /**
     * Dời thứ tự các trạm để tạo chỗ cho trạm mới
     */
    @Transactional
    public void shiftStopOrders(Integer routeId, Integer direction, Integer fromOrder) {
        try {
            System.out.println("Dời thứ tự các trạm từ vị trí " + fromOrder);

            // Lấy danh sách các trạm cần dời (có thứ tự >= fromOrder), sắp xếp giảm dần
            List<RouteStop> routeStopsToShift = routeStopRepository.findByRouteIdAndDirectionOrderByStopOrder(routeId, direction)
                    .stream()
                    .filter(rs -> rs.getStopOrder() >= fromOrder)
                    .sorted(Comparator.comparing(RouteStop::getStopOrder).reversed())
                    .collect(Collectors.toList());

            // Dời từng trạm lên 1 đơn vị, bắt đầu từ trạm có thứ tự lớn nhất
            for (RouteStop rs : routeStopsToShift) {
                rs.setStopOrder(rs.getStopOrder() + 1);
                routeStopRepository.update(rs);
            }

            System.out.println("Đã dời " + routeStopsToShift.size() + " trạm");
        } catch (Exception e) {
            System.err.println("Lỗi khi dời thứ tự các trạm: " + e.getMessage());
            e.printStackTrace();
            throw e; // Ném lại ngoại lệ để transaction được rollback
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
    @Transactional
    public boolean deleteAllRouteStopsByStopId(Integer stopId) {
        try {
            // Lấy danh sách các route_stop liên quan đến trạm
            List<RouteStop> routeStops = findByStopId(stopId);

            // Xóa từng bản ghi
            for (RouteStop rs : routeStops) {
                routeStopRepository.deleteById(rs.getId());
            }

            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa tất cả route_stops của trạm " + stopId + ": " + e.getMessage());
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
    @Transactional
    public boolean moveStopUp(Integer routeStopId) {
        try {
            RouteStop currentStop = routeStopRepository.findById(routeStopId);
            if (currentStop == null) {
                System.err.println("Không tìm thấy RouteStop với ID: " + routeStopId);
                return false;
            }

            Integer routeId = currentStop.getRoute().getId();
            Integer direction = currentStop.getDirection();
            Integer currentOrder = currentStop.getStopOrder();

            System.out.println("Di chuyển trạm lên: RouteStop ID=" + routeStopId
                    + ", Stop=" + currentStop.getStop().getStopName()
                    + ", Order=" + currentOrder);

            if (currentOrder <= 1) {
                // Đã là trạm đầu tiên, không thể di chuyển lên nữa
                System.out.println("Không thể di chuyển lên vì đã là trạm đầu tiên");
                return false;
            }

            // Tìm trạm có thứ tự ngay trước đó
            List<RouteStop> allStopsInRoute = findByRouteIdAndDirection(routeId, direction);
            RouteStop prevStop = null;
            for (RouteStop rs : allStopsInRoute) {
                if (rs.getStopOrder() == currentOrder - 1) {
                    prevStop = rs;
                    break;
                }
            }

            if (prevStop == null) {
                System.err.println("Không tìm thấy trạm trước đó có thứ tự: " + (currentOrder - 1));
                return false;
            }

            System.out.println("Trạm trước: " + prevStop.getStop().getStopName()
                    + ", Order=" + prevStop.getStopOrder());

            // Sử dụng giá trị tạm thời để tránh vi phạm ràng buộc unique key
            Integer tempOrder = -1 * currentOrder; // Giá trị âm để đảm bảo không trùng với bất kỳ thứ tự nào

            // Bước 1: Đặt thứ tự hiện tại thành giá trị tạm thời
            currentStop.setStopOrder(tempOrder);
            if (!routeStopRepository.update(currentStop)) {
                System.err.println("Không thể cập nhật thứ tự tạm thời cho trạm hiện tại");
                return false;
            }

            // Bước 2: Cập nhật thứ tự trạm trước đó thành thứ tự hiện tại
            Integer prevOrder = prevStop.getStopOrder();
            prevStop.setStopOrder(currentOrder);
            if (!routeStopRepository.update(prevStop)) {
                // Rollback thứ tự trạm hiện tại nếu cập nhật thất bại
                currentStop.setStopOrder(currentOrder);
                routeStopRepository.update(currentStop);
                System.err.println("Không thể cập nhật thứ tự cho trạm trước đó");
                return false;
            }

            // Bước 3: Cập nhật thứ tự trạm hiện tại thành thứ tự trạm trước đó
            currentStop.setStopOrder(prevOrder);
            if (!routeStopRepository.update(currentStop)) {
                // Rollback thứ tự trạm trước đó nếu cập nhật thất bại
                prevStop.setStopOrder(prevOrder);
                routeStopRepository.update(prevStop);
                currentStop.setStopOrder(currentOrder);
                routeStopRepository.update(currentStop);
                System.err.println("Không thể cập nhật thứ tự cuối cùng cho trạm hiện tại");
                return false;
            }

            System.out.println("Di chuyển trạm thành công: " + currentStop.getStop().getStopName()
                    + " từ vị trí " + currentOrder + " lên vị trí " + prevOrder);

            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi di chuyển trạm lên: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean moveStopDown(Integer routeStopId) {
        try {
            RouteStop currentStop = routeStopRepository.findById(routeStopId);
            if (currentStop == null) {
                System.err.println("Không tìm thấy RouteStop với ID: " + routeStopId);
                return false;
            }

            Integer routeId = currentStop.getRoute().getId();
            Integer direction = currentStop.getDirection();
            Integer currentOrder = currentStop.getStopOrder();

            System.out.println("Di chuyển trạm xuống: RouteStop ID=" + routeStopId
                    + ", Stop=" + currentStop.getStop().getStopName()
                    + ", Order=" + currentOrder);

            // Tìm thứ tự lớn nhất trong tuyến và chiều này
            List<RouteStop> allStopsInRoute = findByRouteIdAndDirection(routeId, direction);
            int maxOrder = allStopsInRoute.stream()
                    .mapToInt(RouteStop::getStopOrder)
                    .max()
                    .orElse(0);

            if (currentOrder >= maxOrder) {
                // Đã là trạm cuối cùng, không thể di chuyển xuống nữa
                System.out.println("Không thể di chuyển xuống vì đã là trạm cuối cùng (Order=" + currentOrder + ", Max=" + maxOrder + ")");
                return false;
            }

            // Tìm trạm có thứ tự ngay sau đó
            RouteStop nextStop = null;
            for (RouteStop rs : allStopsInRoute) {
                if (rs.getStopOrder() == currentOrder + 1) {
                    nextStop = rs;
                    break;
                }
            }

            if (nextStop == null) {
                System.err.println("Không tìm thấy trạm sau đó có thứ tự: " + (currentOrder + 1));
                return false;
            }

            System.out.println("Trạm sau: " + nextStop.getStop().getStopName()
                    + ", Order=" + nextStop.getStopOrder());

            // Sử dụng giá trị tạm thời để tránh vi phạm ràng buộc unique key
            Integer tempOrder = -1 * currentOrder; // Giá trị âm để đảm bảo không trùng với bất kỳ thứ tự nào

            // Bước 1: Đặt thứ tự hiện tại thành giá trị tạm thời
            currentStop.setStopOrder(tempOrder);
            if (!routeStopRepository.update(currentStop)) {
                System.err.println("Không thể cập nhật thứ tự tạm thời cho trạm hiện tại");
                return false;
            }

            // Bước 2: Cập nhật thứ tự trạm sau đó thành thứ tự hiện tại
            Integer nextOrder = nextStop.getStopOrder();
            nextStop.setStopOrder(currentOrder);
            if (!routeStopRepository.update(nextStop)) {
                // Rollback thứ tự trạm hiện tại nếu cập nhật thất bại
                currentStop.setStopOrder(currentOrder);
                routeStopRepository.update(currentStop);
                System.err.println("Không thể cập nhật thứ tự cho trạm sau đó");
                return false;
            }

            // Bước 3: Cập nhật thứ tự trạm hiện tại thành thứ tự trạm sau đó
            currentStop.setStopOrder(nextOrder);
            if (!routeStopRepository.update(currentStop)) {
                // Rollback thứ tự trạm sau đó nếu cập nhật thất bại
                nextStop.setStopOrder(nextOrder);
                routeStopRepository.update(nextStop);
                currentStop.setStopOrder(currentOrder);
                routeStopRepository.update(currentStop);
                System.err.println("Không thể cập nhật thứ tự cuối cùng cho trạm hiện tại");
                return false;
            }

            System.out.println("Di chuyển trạm thành công: " + currentStop.getStop().getStopName()
                    + " từ vị trí " + currentOrder + " xuống vị trí " + nextOrder);

            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi di chuyển trạm xuống: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int countRoutesByStopId(Integer stopId) {
        try {
            // Đếm số lượng tuyến đường duy nhất đi qua trạm này
            List<RouteStop> routeStops = findByStopId(stopId);

            // Sử dụng Set để lọc các tuyến duy nhất (không trùng lặp)
            Set<Integer> uniqueRouteIds = routeStops.stream()
                    .map(rs -> rs.getRoute().getId())
                    .collect(Collectors.toSet());

            return uniqueRouteIds.size();
        } catch (Exception e) {
            System.err.println("Lỗi khi đếm số tuyến qua trạm: " + e.getMessage());
            return 0;
        }
    }
}
