package com.pts.controllers;

import com.pts.pojo.Landmarks;
import com.pts.pojo.Stops;
import com.pts.services.LandmarkService;
import com.pts.services.StopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/landmarks")
public class LandmarkController {

    @Autowired
    private LandmarkService landmarkService;
    
    @Autowired
    private StopService stopService;
    
    @GetMapping
    public String getAllLandmarks(Model model) {
        List<Landmarks> landmarks = landmarkService.getAllLandmarks();
        model.addAttribute("landmarks", landmarks);
        return "admin/landmarks/list";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("landmark", new Landmarks());
        model.addAttribute("stops", stopService.getAllStops());
        return "admin/landmarks/create";
    }
    
    @PostMapping("/create")
    public String createLandmark(@ModelAttribute Landmarks landmark, 
                               @RequestParam(required = false) Integer nearestStopId,
                               RedirectAttributes redirectAttributes) {
        try {
            if (nearestStopId != null) {
                Optional<Stops> stop = stopService.getStopById(nearestStopId);
                stop.ifPresent(landmark::setNearestStop);
            }
            
            landmarkService.saveLandmark(landmark);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm địa điểm mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm địa điểm: " + e.getMessage());
        }
        return "redirect:/admin/landmarks";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Landmarks> landmark = landmarkService.getLandmarkById(id);
        if (landmark.isPresent()) {
            model.addAttribute("landmark", landmark.get());
            model.addAttribute("stops", stopService.getAllStops());
            return "admin/landmarks/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy địa điểm với ID: " + id);
            return "redirect:/admin/landmarks";
        }
    }
    
    @PostMapping("/edit/{id}")
    public String updateLandmark(@PathVariable Integer id, 
                               @ModelAttribute Landmarks landmark,
                               @RequestParam(required = false) Integer nearestStopId,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Landmarks> existingLandmark = landmarkService.getLandmarkById(id);
            if (existingLandmark.isPresent()) {
                landmark.setId(id);
                
                if (nearestStopId != null) {
                    Optional<Stops> stop = stopService.getStopById(nearestStopId);
                    stop.ifPresent(landmark::setNearestStop);
                } else {
                    landmark.setNearestStop(null);
                }
                
                landmarkService.saveLandmark(landmark);
                redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật địa điểm thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy địa điểm với ID: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật địa điểm: " + e.getMessage());
        }
        return "redirect:/admin/landmarks";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteLandmark(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            if (landmarkService.existsLandmarkById(id)) {
                landmarkService.deleteLandmark(id);
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa địa điểm thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy địa điểm với ID: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa địa điểm: " + e.getMessage());
        }
        return "redirect:/admin/landmarks";
    }
    
    @GetMapping("/search")
    public String searchLandmarks(@RequestParam String keyword, Model model) {
        List<Landmarks> landmarks = landmarkService.searchLandmarks(keyword);
        model.addAttribute("landmarks", landmarks);
        model.addAttribute("keyword", keyword);
        return "admin/landmarks/list";
    }
}