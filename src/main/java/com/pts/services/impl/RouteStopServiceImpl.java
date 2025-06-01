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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
    @PersistenceContext
    private EntityManager entityManager;

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
            System.out.println("Đang cập nhật thứ tự trạm dừng cho tuyến: " + route.getRouteName()
                    + " (ID: " + routeId + "), chiều: " + (direction == 1 ? "đi" : "về"));

            // Xóa các trạm theo tuyến và chiều cụ thể TRƯỚC khi thêm vào để tránh vi phạm
            // unique key
            // Sử dụng native query để đảm bảo tất cả các trạm được xóa
            int deletedCount = entityManager.createNativeQuery(
                    "DELETE FROM route_stops WHERE route_id = :routeId AND direction = :direction")
                    .setParameter("routeId", routeId)
                    .setParameter("direction", direction)
                    .executeUpdate();

            System.out.println("Đã xóa " + deletedCount + " trạm hiện có");

            // Thêm lại các trạm theo thứ tự mới
            int order = 1;
            int addedCount = 0;
            for (Integer stopId : stopIds) {
                // Sử dụng native query để thêm trạm mới, tránh vấn đề với JPA/Hibernate
                int inserted = entityManager.createNativeQuery(
                        "INSERT INTO route_stops (route_id, stop_id, direction, stop_order, created_at) " +
                                "VALUES (:routeId, :stopId, :direction, :stopOrder, NOW())")
                        .setParameter("routeId", routeId)
                        .setParameter("stopId", stopId)
                        .setParameter("direction", direction)
                        .setParameter("stopOrder", order++)
                        .executeUpdate();

                if (inserted > 0) {
                    addedCount++;
                    System.out.println("Đã thêm trạm ID " + stopId + " vào vị trí " + (order - 1));
                }
            }

            // Flush để đảm bảo các thay đổi được lưu xuống DB
            entityManager.flush();

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

            // Kiểm tra xem trạm đã tồn tại trong tuyến và chiều này chưa
            List<RouteStop> existingStops = findByRouteIdAndDirection(routeId, direction);
            for (RouteStop rs : existingStops) {
                if (rs.getStop().getId().equals(stopId)) {
                    System.out.println("Trạm đã tồn tại trong tuyến và chiều này, không thêm lại");
                    return rs;
                }
            }

            // Nếu stopOrder là null, thêm vào cuối
            if (stopOrder == null) {
                Integer maxOrder = findMaxStopOrderByRouteIdAndDirection(routeId, direction);
                stopOrder = (maxOrder != null) ? maxOrder + 1 : 1;
                System.out.println("Thứ tự trạm mới (cuối cùng): " + stopOrder);
            } else {
                // Dời tất cả các trạm có stopOrder >= stopOrder lên 1 để tạo chỗ trống
                deferStopsForInsertion(routeId, direction, stopOrder);
                System.out.println("Đã dời các trạm từ vị trí " + stopOrder + " để chèn trạm mới");
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

    @Transactional
    public void deferStopsForInsertion(Integer routeId, Integer direction, Integer fromOrder) {
        try {
            System.out.println("Dời các trạm từ vị trí " + fromOrder + " để chèn trạm mới");

            // Lấy danh sách các trạm cần dời (có thứ tự >= fromOrder)
            List<RouteStop> routeStopsToDefer = findByRouteIdAndDirection(routeId, direction).stream()
                    .filter(rs -> rs.getStopOrder() >= fromOrder)
                    .sorted(Comparator.comparing(RouteStop::getStopOrder).reversed()) // Sắp xếp giảm dần để tránh xung
                                                                                      // đột
                    .collect(Collectors.toList());

            // Dời các trạm theo thứ tự giảm dần (từ cuối lên)
            // Thực hiện bằng native query để tránh vấn đề với JPA/Hibernate
            for (RouteStop rs : routeStopsToDefer) {
                entityManager.createNativeQuery(
                        "UPDATE route_stops SET stop_order = :newOrder WHERE id = :id")
                        .setParameter("newOrder", rs.getStopOrder() + 1)
                        .setParameter("id", rs.getId())
                        .executeUpdate();
            }

            // Flush để đảm bảo các thay đổi được lưu xuống DB
            entityManager.flush();

            System.out.println("Đã dời " + routeStopsToDefer.size() + " trạm để tạo chỗ trống");
        } catch (Exception e) {
            System.err.println("Lỗi khi dời các trạm: " + e.getMessage());
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

    @Override
    @Transactional
    public int deleteByRouteIdStopIdAndDirection(Integer routeId, Integer stopId, Integer direction) {
        try {
            System.out.println("Xóa liên kết giữa tuyến " + routeId + ", trạm " + stopId + ", chiều " + direction);
            int result = entityManager.createNativeQuery(
                    "DELETE FROM route_stops WHERE route_id = :routeId AND stop_id = :stopId AND direction = :direction")
                    .setParameter("routeId", routeId)
                    .setParameter("stopId", stopId)
                    .setParameter("direction", direction)
                    .executeUpdate();

            System.out.println("Đã xóa " + result + " bản ghi");
            return result;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa route_stop: " + e.getMessage());
            e.printStackTrace();
            throw e; // re-throw để transaction rollback
        }
    }

    @Override
    @Transactional
    public boolean reorderAfterDelete(Integer routeId, Integer direction) {
        try {
            System.out.println("Sắp xếp lại thứ tự cho tuyến " + routeId + ", chiều " + direction);

            // Lấy danh sách các route_stops hiện có
            List<RouteStop> existingStops = findByRouteIdAndDirection(routeId, direction);
            if (existingStops.isEmpty()) {
                System.out.println("Không có trạm nào cần sắp xếp lại");
                return true;
            }

            // Sắp xếp theo thứ tự hiện tại
            existingStops.sort(Comparator.comparing(RouteStop::getStopOrder));

            // Sắp xếp lại từ 1
            int newOrder = 1;
            for (RouteStop stop : existingStops) {
                // Chỉ cập nhật nếu thứ tự thay đổi
                if (stop.getStopOrder() != newOrder) {
                    System.out.println("Cập nhật trạm ID " + stop.getId() +
                            " từ thứ tự " + stop.getStopOrder() +
                            " thành " + newOrder);

                    // Cập nhật thứ tự
                    entityManager.createNativeQuery(
                            "UPDATE route_stops SET stop_order = :newOrder WHERE id = :id")
                            .setParameter("newOrder", newOrder)
                            .setParameter("id", stop.getId())
                            .executeUpdate();
                }
                newOrder++;
            }

            System.out.println("Đã sắp xếp lại thứ tự " + existingStops.size() + " trạm");
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi sắp xếp lại thứ tự: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Dời thứ tự các trạm để tạo chỗ cho trạm mới
     */
    @Transactional
    public void shiftStopOrders(Integer routeId, Integer direction, Integer fromOrder) {
        try {
            System.out.println("Dời thứ tự các trạm từ vị trí " + fromOrder);

            // Lấy danh sách các trạm cần dời (có thứ tự >= fromOrder), sắp xếp giảm dần
            List<RouteStop> routeStopsToShift = routeStopRepository
                    .findByRouteIdAndDirectionOrderByStopOrder(routeId, direction)
                    .stream()
                    .filter(rs -> rs.getStopOrder() >= fromOrder)
                    .sorted(Comparator.comparing(RouteStop::getStopOrder).reversed())
                    .collect(Collectors.toList());

            // Đặt tạm các giá trị âm để tránh conflict
            for (RouteStop rs : routeStopsToShift) {
                rs.setStopOrder(-rs.getStopOrder());
                routeStopRepository.update(rs);
            }

            // Sau đó, gán các giá trị mới (+1) từ giá trị tạm thời
            for (RouteStop rs : routeStopsToShift) {
                rs.setStopOrder(-(rs.getStopOrder()) + 1);
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
    @Transactional
    public boolean deleteAllRouteStopsByStopId(Integer stopId) {
        try {
            // Lấy danh sách các route_stop liên quan đến trạm
            List<RouteStop> routeStops = findByStopId(stopId);

            // Tạo map lưu trữ thông tin tuyến và chiều đã xử lý
            Map<String, Boolean> processedRouteDirections = new HashMap<>();

            // Xóa từng bản ghi và cập nhật thứ tự
            for (RouteStop rs : routeStops) {
                Integer routeId = rs.getRoute().getId();
                Integer direction = rs.getDirection();
                String key = routeId + "-" + direction;

                // Chỉ xử lý mỗi cặp tuyến-chiều một lần để tránh xung đột
                if (!processedRouteDirections.containsKey(key)) {
                    // Xóa route_stop và sắp xếp lại thứ tự
                    deleteAndReorder(rs.getId());
                    processedRouteDirections.put(key, true);
                } else {
                    // Nếu đã xử lý tuyến-chiều này, chỉ cần xóa
                    routeStopRepository.deleteById(rs.getId());
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa tất cả route_stops của trạm " + stopId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean moveStopUp(Integer routeStopId) {
        try {
            // Lấy thông tin trạm cần di chuyển
            RouteStop currentStop = routeStopRepository.findById(routeStopId);
            if (currentStop == null) {
                System.err.println("Không tìm thấy RouteStop với ID: " + routeStopId);
                return false;
            }

            Integer routeId = currentStop.getRoute().getId();
            Integer direction = currentStop.getDirection();
            Integer currentOrder = currentStop.getStopOrder();

            if (currentOrder <= 1) {
                // Đã là trạm đầu tiên, không thể di chuyển lên nữa
                System.out.println("Đây đã là trạm đầu tiên, không thể di chuyển lên");
                return false;
            }

            // Lấy danh sách tất cả các trạm theo tuyến và chiều
            List<RouteStop> allStops = findByRouteIdAndDirection(routeId, direction);

            // Tìm trạm có thứ tự ngay trước đó
            RouteStop prevStop = null;
            for (RouteStop rs : allStops) {
                if (rs.getStopOrder() == currentOrder - 1) {
                    prevStop = rs;
                    break;
                }
            }

            if (prevStop == null) {
                System.err.println("Không tìm thấy trạm có thứ tự " + (currentOrder - 1));
                return false;
            }

            // Đơn giản hóa: Chỉ hoán đổi thứ tự giữa hai trạm liền kề
            System.out.println("Di chuyển trạm #" + currentStop.getId() + " từ vị trí " + currentOrder + " lên vị trí "
                    + (currentOrder - 1));

            // Lưu ID của cả hai trạm để xác định sau khi query lại từ DB
            Integer currentStopId = currentStop.getId();
            Integer prevStopId = prevStop.getId();

            // Cập nhật trong DB sử dụng native query để tránh vấn đề với JPA/Hibernate
            // cache
            // Sử dụng SQL trực tiếp để cập nhật
            boolean success = routeStopRepository.swapStopOrders(currentStopId, prevStopId);

            if (success) {
                System.out.println("Đã di chuyển trạm lên thành công");
            } else {
                System.err.println("Không thể di chuyển trạm");
            }

            return success;
        } catch (Exception e) {
            System.err.println("Lỗi khi di chuyển trạm lên: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw để transaction được rollback
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

            // Tìm thứ tự lớn nhất trong tuyến và chiều này
            int maxOrder = findMaxStopOrderByRouteIdAndDirection(routeId, direction);

            if (currentOrder >= maxOrder) {
                // Đã là trạm cuối cùng
                System.out.println("Đây đã là trạm cuối cùng, không thể di chuyển xuống");
                return false;
            }

            // Lấy danh sách tất cả các trạm theo tuyến và chiều
            List<RouteStop> allStops = findByRouteIdAndDirection(routeId, direction);

            // Tìm trạm có thứ tự ngay sau đó
            RouteStop nextStop = null;
            for (RouteStop rs : allStops) {
                if (rs.getStopOrder() == currentOrder + 1) {
                    nextStop = rs;
                    break;
                }
            }

            if (nextStop == null) {
                System.err.println("Không tìm thấy trạm có thứ tự " + (currentOrder + 1));
                return false;
            }

            // Đơn giản hóa: Chỉ hoán đổi thứ tự giữa hai trạm liền kề
            System.out.println("Di chuyển trạm #" + currentStop.getId() + " từ vị trí " + currentOrder
                    + " xuống vị trí " + (currentOrder + 1));

            // Lưu ID của cả hai trạm để xác định sau khi query lại từ DB
            Integer currentStopId = currentStop.getId();
            Integer nextStopId = nextStop.getId();

            // Cập nhật trong DB sử dụng native query để tránh vấn đề với JPA/Hibernate
            // cache
            // Sử dụng SQL trực tiếp để cập nhật
            boolean success = routeStopRepository.swapStopOrders(currentStopId, nextStopId);

            if (success) {
                System.out.println("Đã di chuyển trạm xuống thành công");
            } else {
                System.err.println("Không thể di chuyển trạm");
            }

            return success;
        } catch (Exception e) {
            System.err.println("Lỗi khi di chuyển trạm xuống: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw để transaction được rollback
        }
    }

    @Override
    @Transactional
    public boolean deleteAndReorder(Integer routeStopId) {
        try {
            // Lấy thông tin trạm cần xóa TRƯỚC khi xóa nó
            RouteStop routeStop = routeStopRepository.findById(routeStopId);
            if (routeStop == null) {
                System.err.println("Không tìm thấy route_stop với ID: " + routeStopId);
                return false;
            }

            // Lưu các giá trị cần thiết trước khi xóa
            Integer routeId = routeStop.getRoute().getId();
            Integer direction = routeStop.getDirection();
            Integer deletedOrder = routeStop.getStopOrder();
            Integer stopId = routeStop.getStop().getId();

            System.out.println("Xóa trạm #" + routeStopId + " (Stop ID: " + stopId + ") với thứ tự " + deletedOrder +
                    " từ tuyến " + routeId + ", chiều " + direction);

            // Xóa trạm khỏi route_stops (chỉ xóa liên kết, không xóa trạm)
            boolean deleteSuccess = routeStopRepository.deleteById(routeStopId);

            if (deleteSuccess) {
                // Sắp xếp lại thứ tự bằng cách cập nhật trực tiếp trong database
                // Tìm tất cả các trạm có thứ tự > deletedOrder và giảm thứ tự xuống 1
                entityManager.createQuery(
                        "UPDATE RouteStop rs SET rs.stopOrder = rs.stopOrder - 1 " +
                                "WHERE rs.route.id = :routeId AND rs.direction = :direction " +
                                "AND rs.stopOrder > :deletedOrder")
                        .setParameter("routeId", routeId)
                        .setParameter("direction", direction)
                        .setParameter("deletedOrder", deletedOrder)
                        .executeUpdate();

                System.out.println("Đã xóa trạm và sắp xếp lại thứ tự cho tuyến " + routeId + " chiều " + direction);
                return true;
            } else {
                System.err.println("Không thể xóa route_stop với ID: " + routeStopId);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa và sắp xếp lại trạm: " + e.getMessage());
            e.printStackTrace();
            throw e; // re-throw để transaction rollback
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
