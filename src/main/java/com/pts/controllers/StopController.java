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

    // Hiển thị danh sách tất cả trạm dừng
    @GetMapping
    public String listAllStops(Model model) {
        List<Stops> stops = stopService.getAllStops();
        model.addAttribute("stops", stops);
        return "stops/list";
    }

    // Hiển thị form tạo trạm dừng mới
    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Integer routeId,
            @RequestParam(required = false, defaultValue = "1") Integer direction,
            Model model) {
        Stops stop = new Stops();
        model.addAttribute("stop", stop);

        // Lấy danh sách tất cả các tuyến
        model.addAttribute("routes", routeService.getAllRoutes());

        // Nếu được gọi từ trang chi tiết tuyến, lưu routeId và direction để tự động thêm vào tuyến sau khi tạo
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
                RouteStop addedRouteStop = routeStopService.addStopToRoute(routeId, savedStop.getId(), direction, stopOrder);

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

            // Tạo cấu trúc dữ liệu để lưu thông tin về tuyến và chiều
            Map<Integer, Map<String, Object>> routeDirectionInfo = new HashMap<>();

            // Lấy thông tin từng chiều cho các tuyến mà trạm này thuộc về
            List<RouteStop> routeStops = routeStopService.findByStopId(id);
            for (RouteStop rs : routeStops) {
                Integer routeId = rs.getRoute().getId();
                Integer stopDirection = rs.getDirection();

                if (!routeDirectionInfo.containsKey(routeId)) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("routeName", rs.getRoute().getName());
                    info.put("directions", new ArrayList<Integer>());
                    routeDirectionInfo.put(routeId, info);
                }

                @SuppressWarnings("unchecked")
                List<Integer> directions = (List<Integer>) routeDirectionInfo.get(routeId).get("directions");
                if (!directions.contains(stopDirection)) {
                    directions.add(stopDirection);
                }
            }

            model.addAttribute("routeDirectionInfo", routeDirectionInfo);

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
            @RequestParam(value = "stopOrdersToAdd", required = false) List<Integer> stopOrdersToAdd, // Thêm tham số này
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
                        ? stopOrdersToAdd.get(i) : null;

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
        return "redirect:/stops/view/" + id;
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
                Map<Integer, Integer> directionOrderMap = (Map<Integer, Integer>) routeInfoMap.get(routeId).get("directions");
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
            RedirectAttributes redirectAttributes) {
        // Kiểm tra xem trạm có tồn tại không
        if (!stopService.stopExists(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trạm dừng không tồn tại!");
            return "redirect:/stops";
        }

        // Xóa trạm (điều này cũng sẽ xóa các liên kết trong bảng route_stops)
        stopService.deleteStop(id);

        redirectAttributes.addFlashAttribute("successMessage", "Trạm dừng đã được xóa thành công!");

        // Điều hướng về trang chi tiết tuyến nếu có yêu cầu
        if (returnToRoute != null) {
            return "redirect:/routes/view/" + returnToRoute + "?direction=" + direction;
        }

        return "redirect:/stops";
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

    // Xóa trạm khỏi tuyến
    @GetMapping("/{stopId}/removeFromRoute/{routeId}")
    public String removeStopFromRoute(@PathVariable("stopId") Integer stopId,
            @PathVariable("routeId") Integer routeId,
            @RequestParam(required = false, defaultValue = "1") Integer direction,
            @RequestParam(required = false) String returnTo,
            RedirectAttributes redirectAttributes) {
        // Tìm RouteStop dựa trên route_id, stop_id và direction
        List<RouteStop> routeStops = routeStopService.findByRouteIdAndDirection(routeId, direction);
        for (RouteStop rs : routeStops) {
            if (rs.getStop().getId().equals(stopId)) {
                routeStopService.deleteById(rs.getId());
                redirectAttributes.addFlashAttribute("successMessage",
                        "Trạm đã được xóa khỏi tuyến (chiều " + (direction == 1 ? "đi" : "về") + ") thành công!");
                break;
            }
        }

        // Điều hướng theo yêu cầu
        if ("route".equals(returnTo)) {
            return "redirect:/routes/view/" + routeId + "?direction=" + direction;
        } else {
            return "redirect:/stops/view/" + stopId;
        }
    }

    // Tìm kiếm trạm dừng
    @GetMapping("/search")
    public String searchStops(@RequestParam("keyword") String keyword, Model model) {
        List<Stops> stops = stopService.searchStops(keyword);
        model.addAttribute("stops", stops);
        model.addAttribute("keyword", keyword);
        return "stops/list";
    }
}
