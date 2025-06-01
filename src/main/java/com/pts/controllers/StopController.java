package com.pts.controllers;

import com.pts.pojo.Routes;
import com.pts.pojo.Stops;
import com.pts.pojo.RouteStop;
import com.pts.services.StopService;
import com.pts.services.RouteStopService;
import com.pts.services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/stops")
public class StopController {

    @Autowired
    private StopService stopService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private RouteStopService routeStopService;

    // Hiển thị form tạo trạm dừng mới
    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Integer routeId,
            @RequestParam(required = false, defaultValue = "1") Integer direction,
            Model model) {
        Stops stop = new Stops();
        model.addAttribute("stop", stop);

        // Lấy danh sách tất cả các tuyến
        model.addAttribute("routes", routeService.getAllRoutes());

        // Nếu được gọi từ trang chi tiết tuyến, lưu routeId và direction để tự động
        // thêm vào tuyến sau khi tạo
        if (routeId != null) {
            model.addAttribute("selectedRouteId", routeId);
            model.addAttribute("direction", direction);
        }

        return "stops/create";
    }

    @PostMapping("/create")
    public String createStop(@ModelAttribute("stop") Stops stop,
            @RequestParam(required = false) Integer routeId,
            @RequestParam(required = false, defaultValue = "1") Integer direction,
            @RequestParam(required = false, defaultValue = "0") Integer stopOrder,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "stops/create";
        }

        // Log thông tin đầu vào
        System.out.println("===== DEBUG CREATE STOP =====");
        System.out.println("routeId: " + routeId);
        System.out.println("direction: " + direction);
        System.out.println("stopOrder: " + stopOrder);
        System.out.println("Tên trạm: " + stop.getStopName());
        System.out.println("Vĩ độ: " + stop.getLatitude());
        System.out.println("Kinh độ: " + stop.getLongitude());

        try {
            // Lưu trạm dừng mới
            Stops savedStop = stopService.saveStop(stop);
            System.out.println("Đã lưu trạm, ID: " + savedStop.getId());

            // Nếu có routeId, thêm vào tuyến
            if (routeId != null && routeId > 0) {
                System.out.println("Thêm trạm vào tuyến: " + savedStop.getId() + " -> " + routeId);
                RouteStop addedRouteStop = routeStopService.addStopToRoute(routeId, savedStop.getId(), direction,
                        stopOrder);

                if (addedRouteStop != null) {
                    System.out.println("Thêm thành công, RouteStop ID: " + addedRouteStop.getId());
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Trạm dừng đã được tạo và thêm vào tuyến thành công!");
                    return "redirect:/routes/view/" + routeId + "?direction=" + direction;
                } else {
                    System.err.println("Lỗi: Không thể thêm trạm vào tuyến (addedRouteStop is null)");
                    redirectAttributes.addFlashAttribute("warningMessage",
                            "Trạm dừng đã được tạo nhưng không thể thêm vào tuyến. Vui lòng thêm thủ công.");
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Trạm dừng đã được tạo thành công!");
            return "redirect:/stops";
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo trạm hoặc thêm vào tuyến: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi tạo trạm: " + e.getMessage());
            return "stops/create";
        }
    }

    @GetMapping("/api/nearby")
    @ResponseBody
    public List<Map<String, Object>> getNearbyStopsApi(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @RequestParam(defaultValue = "500") int radius) {
        try {
            System.out.println("API tìm trạm lân cận: lat=" + lat + ", lng=" + lng + ", radius=" + radius);
            List<Map<String, Object>> results = stopService.findNearbyStopsFormatted(lat, lng, radius);
            System.out.println("Tìm thấy " + results.size() + " trạm lân cận");
            return results;
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm trạm lân cận: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Hiển thị form chỉnh sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Optional<Stops> stopOpt = stopService.getStopById(id);

        if (stopOpt.isPresent()) {
            Stops stop = stopOpt.get();
            model.addAttribute("stop", stop);

            // Lấy tất cả các tuyến để hiển thị trong dropdown
            List<Routes> allRoutes = routeService.getAllRoutes();
            model.addAttribute("routes", allRoutes);

            // Tạo cấu trúc dữ liệu cho routeInfoMap
            List<RouteStop> routeStops = routeStopService.findByStopId(id);
            Map<Integer, Map<String, Object>> routeInfoMap = new HashMap<>();

            for (RouteStop rs : routeStops) {
                Integer routeId = rs.getRoute().getId();
                Integer direction = rs.getDirection();
                Integer stopOrder = rs.getStopOrder();

                if (!routeInfoMap.containsKey(routeId)) {
                    Map<String, Object> routeInfo = new HashMap<>();
                    routeInfo.put("route", rs.getRoute());
                    routeInfo.put("directions", new HashMap<Integer, Integer>());
                    routeInfoMap.put(routeId, routeInfo);
                }

                // Lưu thông tin thứ tự trạm trong từng chiều của tuyến
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> directionOrderMap = (Map<Integer, Integer>) routeInfoMap.get(routeId)
                        .get("directions");
                directionOrderMap.put(direction, stopOrder);
            }

            model.addAttribute("routeInfoMap", routeInfoMap);

            return "stops/edit";
        } else {
            return "redirect:/stops?error=StopNotFound";
        }
    }

    // Xử lý submit form sửa
    @PostMapping("/edit/{id}")
    public String updateStop(@PathVariable("id") Integer id,
            @ModelAttribute("stop") Stops stop,
            @RequestParam(value = "routesToAdd", required = false) List<Integer> routesToAdd,
            @RequestParam(value = "directionsToAdd", required = false) List<Integer> directionsToAdd,
            @RequestParam(value = "stopOrdersToAdd", required = false) List<Integer> stopOrdersToAdd, // Thêm tham số
                                                                                                      // này
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "stops/edit";
        }

        // Set id và lưu thông tin trạm dừng
        stop.setId(id);
        stopService.saveStop(stop);

        // Nếu có tuyến mới được chọn để thêm trạm vào
        if (routesToAdd != null && !routesToAdd.isEmpty() && directionsToAdd != null && !directionsToAdd.isEmpty()) {
            // Đảm bảo rằng routesToAdd và directionsToAdd có cùng kích thước
            int minSize = Math.min(routesToAdd.size(), directionsToAdd.size());

            for (int i = 0; i < minSize; i++) {
                Integer routeId = routesToAdd.get(i);
                Integer direction = directionsToAdd.get(i);

                // Lấy stopOrder nếu có, nếu không đặt là null
                Integer stopOrder = (stopOrdersToAdd != null && i < stopOrdersToAdd.size())
                        ? stopOrdersToAdd.get(i)
                        : null;

                // Kiểm tra xem đã có trạm trong tuyến và chiều này chưa
                boolean exists = routeStopService.findByRouteIdAndDirection(routeId, direction).stream()
                        .anyMatch(rs -> rs.getStop().getId().equals(id));

                // Nếu chưa có, thêm trạm vào tuyến với chiều tương ứng và thứ tự nếu có
                if (!exists) {
                    // Sử dụng phương thức addStopToRoute với đầy đủ tham số
                    routeStopService.addStopToRoute(routeId, id, direction, stopOrder);
                }
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Trạm dừng đã được cập nhật thành công!");
        return "redirect:/stops/edit/" + id;
    }

    @GetMapping("/route/{routeId}/ajax")
    @ResponseBody
    public List<Map<String, Object>> getStopsByRouteAjax(
            @PathVariable("routeId") Integer routeId,
            @RequestParam(required = false, defaultValue = "1") Integer direction) {
        try {
            // Lấy danh sách trạm dừng theo tuyến và chiều
            List<Stops> stops = stopService.findStopsByRouteIdAndDirection(routeId, direction);

            // Fallback nếu không có dữ liệu cho chiều cụ thể
            if (stops == null || stops.isEmpty()) {
                stops = stopService.findStopsByRouteId(routeId);
            }

            // Lấy thông tin thứ tự từng trạm trong tuyến và chiều
            List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(routeId, direction);
            Map<Integer, Integer> stopOrderMap = routeStops.stream()
                    .collect(Collectors.toMap(
                            rs -> rs.getStop().getId(),
                            RouteStop::getStopOrder,
                            (existing, replacement) -> existing // Trong trường hợp trùng lặp, giữ giá trị đầu tiên
                    ));

            List<Map<String, Object>> result = new ArrayList<>();
            for (Stops stop : stops) {
                Map<String, Object> stopData = new HashMap<>();
                stopData.put("id", stop.getId());
                stopData.put("stopName", stop.getStopName());
                stopData.put("address", stop.getAddress());
                stopData.put("latitude", stop.getLatitude());
                stopData.put("longitude", stop.getLongitude());
                stopData.put("stopOrder", stopOrderMap.getOrDefault(stop.getId(), 0));
                result.add(stopData);
            }

            // Sắp xếp theo thứ tự
            result.sort(Comparator.comparing(m -> (Integer) m.get("stopOrder")));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Xem chi tiết trạm dừng
    @GetMapping("/view/{id}")
    public String viewStop(@PathVariable("id") Integer id, Model model) {
        Optional<Stops> stopOpt = stopService.getStopById(id);

        if (stopOpt.isPresent()) {
            Stops stop = stopOpt.get();
            model.addAttribute("stop", stop);

            // Lấy danh sách RouteStop cho trạm này
            List<RouteStop> routeStops = routeStopService.findByStopId(id);

            // Tạo cấu trúc dữ liệu lưu thông tin route và direction
            Map<Integer, Map<String, Object>> routeInfoMap = new HashMap<>();

            for (RouteStop rs : routeStops) {
                Integer routeId = rs.getRoute().getId();

                if (!routeInfoMap.containsKey(routeId)) {
                    Map<String, Object> routeInfo = new HashMap<>();
                    routeInfo.put("route", rs.getRoute());
                    routeInfo.put("directions", new HashMap<Integer, Integer>());
                    routeInfoMap.put(routeId, routeInfo);
                }

                // Lưu thông tin thứ tự (stop_order) của trạm trong từng chiều của tuyến
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> directionOrderMap = (Map<Integer, Integer>) routeInfoMap.get(routeId)
                        .get("directions");
                directionOrderMap.put(rs.getDirection(), rs.getStopOrder());
            }

            model.addAttribute("routeInfoMap", routeInfoMap);

            return "stops/view";
        } else {
            return "redirect:/stops?error=StopNotFound";
        }
    }

    // Xóa trạm dừng
    @GetMapping("/delete/{id}")
    public String deleteStop(@PathVariable("id") Integer id,
            @RequestParam(required = false) Integer returnToRoute,
            @RequestParam(required = false, defaultValue = "1") Integer direction,
            @RequestParam(required = false, defaultValue = "false") Boolean confirmed,
            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra xem trạm có tồn tại không
            Optional<Stops> stopOpt = stopService.getStopById(id);
            if (!stopOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Trạm dừng không tồn tại!");
                return "redirect:/stops";
            }

            // Kiểm tra xem trạm có trong tuyến nào không
            List<RouteStop> routeStops = routeStopService.findByStopId(id);
            if (!routeStops.isEmpty()) {
                // Hiển thị thông tin các tuyến mà trạm thuộc về
                StringBuilder routeInfo = new StringBuilder("Trạm này hiện thuộc các tuyến: ");
                Map<Integer, String> routeNames = new HashMap<>();

                for (RouteStop rs : routeStops) {
                    Integer routeId = rs.getRoute().getId();
                    String routeName = rs.getRoute().getRouteName();
                    if (!routeNames.containsKey(routeId)) {
                        routeNames.put(routeId, routeName);
                    }
                }

                boolean first = true;
                for (Map.Entry<Integer, String> entry : routeNames.entrySet()) {
                    if (!first) {
                        routeInfo.append(", ");
                    }
                    routeInfo.append(entry.getValue());
                    first = false;
                }

                // Cảnh báo người dùng
                redirectAttributes.addFlashAttribute("warningMessage",
                        routeInfo.toString() + ". Vui lòng xóa trạm khỏi các tuyến trước khi xóa hoàn toàn.");
                return "redirect:/stops/edit/" + id;
            }

            // Nếu không thuộc tuyến nào và chưa xác nhận, yêu cầu xác nhận
            if (!confirmed) {
                redirectAttributes.addFlashAttribute("confirmDeleteId", id);
                redirectAttributes.addFlashAttribute("confirmMessage",
                        "Bạn có chắc chắn muốn xóa trạm dừng này? Thao tác này không thể hoàn tác.");
                return "redirect:/stops";
            }

            // Nếu đã xác nhận và không thuộc tuyến nào, xóa trạm
            stopService.deleteStop(id);
            redirectAttributes.addFlashAttribute("successMessage", "Trạm dừng đã được xóa thành công!");

            // Điều hướng về trang chi tiết tuyến nếu có yêu cầu
            if (returnToRoute != null) {
                return "redirect:/routes/view/" + returnToRoute + "?direction=" + direction;
            }

            return "redirect:/stops";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa trạm: " + e.getMessage());
            return "redirect:/stops";
        }
    }

    @GetMapping("/forceDelete/{id}")
    public String forceDeleteStop(@PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra xem trạm có tồn tại không
            Optional<Stops> stopOpt = stopService.getStopById(id);
            if (!stopOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Trạm dừng không tồn tại!");
                return "redirect:/stops";
            }

            // Lấy tất cả route_stops của trạm này để xử lý từng cái
            List<RouteStop> routeStops = routeStopService.findByStopId(id);

            // Tạo map lưu trữ thông tin tuyến và chiều đã xử lý
            Map<String, Boolean> processedRouteDirections = new HashMap<>();

            // Xóa từng route_stop và cập nhật thứ tự
            for (RouteStop rs : routeStops) {
                Integer routeId = rs.getRoute().getId();
                Integer direction = rs.getDirection();
                String key = routeId + "-" + direction;

                // Chỉ xử lý mỗi cặp tuyến-chiều một lần
                if (!processedRouteDirections.containsKey(key)) {
                    // Xóa route_stop và sắp xếp lại thứ tự
                    routeStopService.deleteAndReorder(rs.getId());
                    processedRouteDirections.put(key, true);
                } else {
                    // Nếu đã xử lý tuyến-chiều này, chỉ cần xóa
                    routeStopService.deleteById(rs.getId());
                }
            }

            // Sau đó xóa trạm
            stopService.deleteStop(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xóa trạm dừng và loại bỏ khỏi tất cả các tuyến thành công!");

            return "redirect:/stops";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa trạm: " + e.getMessage());
            return "redirect:/stops";
        }
    }

    // Hiển thị danh sách trạm dừng theo tuyến và chiều
    @GetMapping("/route/{routeId}")
    public String listStopsByRoute(@PathVariable("routeId") Integer routeId,
            @RequestParam(required = false, defaultValue = "1") Integer direction,
            Model model) {
        // Lấy danh sách trạm dừng theo tuyến và chiều
        List<Stops> stops = stopService.findStopsByRouteIdAndDirection(routeId, direction);

        // Fallback nếu không có dữ liệu cho chiều cụ thể
        if (stops == null || stops.isEmpty()) {
            stops = stopService.findStopsByRouteId(routeId);
        }

        Optional<Routes> routeOpt = routeService.getRouteById(routeId);

        if (routeOpt.isPresent()) {
            Routes route = routeOpt.get();
            model.addAttribute("route", route);

            // Lấy thông tin thứ tự từng trạm trong tuyến và chiều
            List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(routeId, direction);
            Map<Integer, Integer> stopOrderMap = routeStops.stream()
                    .collect(Collectors.toMap(
                            rs -> rs.getStop().getId(),
                            RouteStop::getStopOrder,
                            (existing, replacement) -> existing // Trong trường hợp trùng lặp, giữ giá trị đầu tiên
                    ));

            // Sắp xếp các trạm theo thứ tự trong tuyến
            stops.sort((s1, s2) -> {
                Integer order1 = stopOrderMap.getOrDefault(s1.getId(), Integer.MAX_VALUE);
                Integer order2 = stopOrderMap.getOrDefault(s2.getId(), Integer.MAX_VALUE);
                return order1.compareTo(order2);
            });

            // Kiểm tra xem có các chiều khác không
            boolean hasInbound = !routeStopService.findByRouteIdAndDirection(routeId, 1).isEmpty();
            boolean hasOutbound = !routeStopService.findByRouteIdAndDirection(routeId, 2).isEmpty();
            model.addAttribute("hasInbound", hasInbound);
            model.addAttribute("hasOutbound", hasOutbound);
        }

        model.addAttribute("stops", stops);
        model.addAttribute("routeId", routeId);
        model.addAttribute("direction", direction);

        return "stops/routeStops";
    }

    // Thêm trạm vào tuyến (từ trang danh sách trạm)
    @PostMapping("/{stopId}/addToRoute")
    public String addStopToRoute(@PathVariable("stopId") Integer stopId,
            @RequestParam("routeId") Integer routeId,
            @RequestParam(required = false, defaultValue = "1") Integer direction,
            @RequestParam(required = false) Integer stopOrder, // Thêm tham số stopOrder
            RedirectAttributes redirectAttributes) {

        // Sử dụng phương thức addStopToRoute với đầy đủ các tham số
        RouteStop routeStop = routeStopService.addStopToRoute(routeId, stopId, direction, stopOrder);

        redirectAttributes.addFlashAttribute("successMessage",
                "Trạm đã được thêm vào tuyến (chiều " + (direction == 1 ? "đi" : "về") + ") thành công!");
        return "redirect:/stops/view/" + stopId;
    }

    @PostMapping("/updateOrder")
    public String updateStopOrder(
            @RequestParam("routeId") Integer routeId,
            @RequestParam("stopId") Integer stopId,
            @RequestParam("direction") Integer direction,
            @RequestParam("stopOrder") Integer newStopOrder,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("Cập nhật thứ tự trạm: Route=" + routeId + ", Stop=" + stopId
                    + ", Direction=" + direction + ", NewOrder=" + newStopOrder);

            // Kiểm tra giá trị đầu vào
            if (newStopOrder <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Thứ tự trạm phải lớn hơn 0");
                return "redirect:/stops/edit/" + stopId;
            }

            // Tìm RouteStop cần cập nhật
            List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(routeId, direction);
            RouteStop targetRouteStop = null;

            for (RouteStop rs : routeStops) {
                if (rs.getStop().getId().equals(stopId)) {
                    targetRouteStop = rs;
                    break;
                }
            }

            if (targetRouteStop != null) {
                // Lấy thứ tự hiện tại của trạm
                Integer currentOrder = targetRouteStop.getStopOrder();

                // Kiểm tra nếu thứ tự mới giống thứ tự cũ
                if (currentOrder.equals(newStopOrder)) {
                    // Không cần thay đổi gì
                    return "redirect:/stops/edit/" + stopId;
                }

                // Kiểm tra giới hạn thứ tự
                int maxOrder = routeStops.stream()
                        .mapToInt(RouteStop::getStopOrder)
                        .max()
                        .orElse(0);

                if (newStopOrder > maxOrder) {
                    newStopOrder = maxOrder;
                }

                System.out.println("Thay đổi thứ tự từ " + currentOrder + " sang " + newStopOrder);

                // Lưu thứ tự hiện tại để dùng sau
                int oldOrder = currentOrder;

                // Tạo giá trị tạm thời âm để tránh vi phạm unique key
                int tempOrder = -1 * oldOrder;
                targetRouteStop.setStopOrder(tempOrder);
                routeStopService.update(targetRouteStop);

                if (oldOrder < newStopOrder) {
                    // Di chuyển xuống: Giảm thứ tự các trạm ở giữa
                    for (RouteStop rs : routeStops) {
                        int order = rs.getStopOrder();
                        if (order > oldOrder && order <= newStopOrder && !rs.getId().equals(targetRouteStop.getId())) {
                            rs.setStopOrder(order - 1);
                            routeStopService.update(rs);
                        }
                    }
                } else {
                    // Di chuyển lên: Tăng thứ tự các trạm ở giữa
                    for (RouteStop rs : routeStops) {
                        int order = rs.getStopOrder();
                        if (order >= newStopOrder && order < oldOrder && !rs.getId().equals(targetRouteStop.getId())) {
                            rs.setStopOrder(order + 1);
                            routeStopService.update(rs);
                        }
                    }
                }

                // Cập nhật thứ tự mới cho trạm đích
                targetRouteStop.setStopOrder(newStopOrder);
                routeStopService.update(targetRouteStop);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã cập nhật thứ tự trạm thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không tìm thấy thông tin liên kết trạm-tuyến");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật thứ tự trạm: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi cập nhật thứ tự trạm: " + e.getMessage());
        }

        return "redirect:/stops/edit/" + stopId;
    }

    @GetMapping("/moveUp")
    public String moveStopUp(
            @RequestParam("routeId") Integer routeId,
            @RequestParam("stopId") Integer stopId,
            @RequestParam("direction") Integer direction,
            RedirectAttributes redirectAttributes) {

        try {
            // Tìm RouteStop
            List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(routeId, direction);
            RouteStop targetRouteStop = null;

            for (RouteStop rs : routeStops) {
                if (rs.getStop().getId().equals(stopId)) {
                    targetRouteStop = rs;
                    break;
                }
            }

            if (targetRouteStop != null && routeStopService.moveStopUp(targetRouteStop.getId())) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã di chuyển trạm lên thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi di chuyển trạm: " + e.getMessage());
        }

        return "redirect:/stops/edit/" + stopId;
    }

    @GetMapping("/moveDown")
    public String moveStopDown(
            @RequestParam("routeId") Integer routeId,
            @RequestParam("stopId") Integer stopId,
            @RequestParam("direction") Integer direction,
            RedirectAttributes redirectAttributes) {

        try {
            // Tìm RouteStop
            List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(routeId, direction);
            RouteStop targetRouteStop = null;

            for (RouteStop rs : routeStops) {
                if (rs.getStop().getId().equals(stopId)) {
                    targetRouteStop = rs;
                    break;
                }
            }

            if (targetRouteStop != null && routeStopService.moveStopDown(targetRouteStop.getId())) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã di chuyển trạm xuống thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi di chuyển trạm: " + e.getMessage());
        }

        return "redirect:/stops/edit/" + stopId;
    }

    // Xóa trạm khỏi tuyến
    @GetMapping("/{stopId}/removeFromRoute/{routeId}")
    public String removeStopFromRoute(@PathVariable("stopId") Integer stopId,
            @PathVariable("routeId") Integer routeId,
            @RequestParam(required = false, defaultValue = "1") Integer direction,
            @RequestParam(required = false) String returnTo,
            RedirectAttributes redirectAttributes) {
        try {
            // Tìm RouteStop dựa trên route_id, stop_id và direction
            List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(routeId, direction);
            RouteStop routeStopToRemove = null;

            for (RouteStop rs : routeStops) {
                if (rs.getStop().getId().equals(stopId)) {
                    routeStopToRemove = rs;
                    break;
                }
            }

            if (routeStopToRemove != null) {
                // Sử dụng deleteAndReorder thay vì deleteById để đảm bảo sắp xếp lại thứ tự
                boolean success = routeStopService.deleteAndReorder(routeStopToRemove.getId());
                if (success) {
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Trạm đã được xóa khỏi tuyến (chiều " + (direction == 1 ? "đi" : "về") + ") thành công!");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Không thể xóa trạm khỏi tuyến!");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không tìm thấy trạm trong tuyến này!");
            }

            // Điều hướng theo yêu cầu
            if ("route".equals(returnTo)) {
                return "redirect:/routes/view/" + routeId + "?direction=" + direction;
            } else {
                return "redirect:/stops/view/" + stopId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi xóa trạm khỏi tuyến: " + e.getMessage());

            if ("route".equals(returnTo)) {
                return "redirect:/routes/view/" + routeId + "?direction=" + direction;
            } else {
                return "redirect:/stops/view/" + stopId;
            }
        }
    }

    @GetMapping
    public String listStops(Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        System.out.println("=== DEBUG STOP PAGINATION ===");
        System.out.println("Keyword: " + keyword);
        System.out.println("Page: " + page);

        // Gọi service để lấy dữ liệu phân trang
        Map<String, Object> result = stopService.getStopsWithPagination(keyword, page);

        System.out.println("Result keys: " + result.keySet());
        System.out.println("Total items: " + result.get("totalItems"));
        System.out.println("Total pages: " + result.get("totalPages"));
        System.out.println("Current page: " + result.get("currentPage"));

        // Thêm tất cả dữ liệu vào model
        model.addAllAttributes(result);

        return "stops/list";
    }

    // Tìm kiếm trạm dừng
    @GetMapping("/search")
    public String searchStops(@RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        // Chuyển hướng về trang chính với keyword
        return "redirect:/stops?keyword=" + keyword + "&page=" + page;
    }
}
