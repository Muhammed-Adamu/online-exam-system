package com.exam.controller;

import com.exam.entity.ExamPaper;
import com.exam.entity.User;
import com.exam.service.ExamPaperService;
import com.exam.service.StatisticsService;
import com.exam.service.UserService;
import com.exam.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Admin Controller - User management, system overview, paper oversight
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ExamPaperService examPaperService;

    /**
     * Admin Dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("stats", statisticsService.getAdminDashboardStats());
        model.addAttribute("todayStats", userService.getTodayStats());
        model.addAttribute("recentActivity", userService.getRecentActivity(8));
        model.addAttribute("allPapers", examPaperService.getAllPapers());
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin/dashboard";
    }

    // ============================================
    // User Management
    // ============================================

    /**
     * User Management Page (with pagination and filters)
     */
    @GetMapping("/users")
    public String userManagement(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String role,
                                  @RequestParam(required = false) String statusStr,
                                  @RequestParam(required = false) String keyword,
                                  Model model, HttpSession session) {

        // Parse status filter — empty string means no filter
        Integer statusFilter = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                statusFilter = Integer.parseInt(statusStr);
            } catch (NumberFormatException ignored) {}
        }

        // Get filtered results
        List<User> allFiltered = userService.searchUsers(role, statusFilter, keyword);
        long total = allFiltered.size();

        // Manual pagination
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allFiltered.size());
        List<User> pagedUsers = fromIndex < allFiltered.size()
                ? allFiltered.subList(fromIndex, toIndex)
                : new ArrayList<>();

        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("users", pagedUsers);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalUsers", total);
        model.addAttribute("currentRole", role);
        model.addAttribute("currentStatus", statusFilter);
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("pageTitle", "User Management");
        return "admin/user-management";
    }

    /**
     * Create User Form
     */
    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("pageTitle", "Create User");
        return "admin/user-form";
    }

    /**
     * Create User
     */
    @PostMapping("/users/create")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            user.setEnabled(1);
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("message", "User created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Edit User Form
     */
    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getById(id);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Edit User");
        return "admin/user-form";
    }

    /**
     * Update User
     */
    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user,
                             RedirectAttributes redirectAttributes) {
        try {
            user.setId(id);
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("message", "User updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Toggle User Status (AJAX — supports both GET and POST)
     */
    @RequestMapping(value = "/users/toggle/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> toggleUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.toggleStatus(id);
        result.put("success", success);
        if (success) {
            User user = userService.getById(id);
            result.put("enabled", user.getEnabled());
            result.put("statusText", user.getEnabled() == 1 ? "Active" : "Disabled");
        }
        return result;
    }

    /**
     * Batch enable users (AJAX)
     */
    @PostMapping("/users/batch/enable")
    @ResponseBody
    public Map<String, Object> batchEnable(@RequestBody Map<String, List<Long>> payload) {
        Map<String, Object> result = new HashMap<>();
        List<Long> ids = payload.get("ids");
        if (ids == null || ids.isEmpty()) {
            result.put("success", false);
            result.put("message", "No users selected.");
            return result;
        }
        int count = userService.batchUpdateStatus(ids, true);
        result.put("success", true);
        result.put("count", count);
        result.put("message", count + " user(s) enabled.");
        return result;
    }

    /**
     * Batch disable users (AJAX)
     */
    @PostMapping("/users/batch/disable")
    @ResponseBody
    public Map<String, Object> batchDisable(@RequestBody Map<String, List<Long>> payload) {
        Map<String, Object> result = new HashMap<>();
        List<Long> ids = payload.get("ids");
        if (ids == null || ids.isEmpty()) {
            result.put("success", false);
            result.put("message", "No users selected.");
            return result;
        }
        int count = userService.batchUpdateStatus(ids, false);
        result.put("success", true);
        result.put("count", count);
        result.put("message", count + " user(s) disabled.");
        return result;
    }

    /**
     * Batch delete users (AJAX)
     */
    @PostMapping("/users/batch/delete")
    @ResponseBody
    public Map<String, Object> batchDelete(@RequestBody Map<String, List<Long>> payload) {
        Map<String, Object> result = new HashMap<>();
        List<Long> ids = payload.get("ids");
        if (ids == null || ids.isEmpty()) {
            result.put("success", false);
            result.put("message", "No users selected.");
            return result;
        }
        int count = userService.batchDelete(ids);
        result.put("success", true);
        result.put("count", count);
        result.put("message", count + " user(s) deleted.");
        return result;
    }

    /**
     * Delete User
     */
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "User deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ============================================
    // Paper Management Overview
    // ============================================

    /**
     * All papers overview (admin view)
     */
    @GetMapping("/papers")
    public String paperOverview(Model model) {
        model.addAttribute("papers", examPaperService.getAllPapers());
        model.addAttribute("pageTitle", "All Exam Papers");
        return "admin/paper-overview";
    }

    /**
     * Get system activity as JSON (for AJAX refresh)
     */
    @GetMapping("/activity")
    @ResponseBody
    public Map<String, Object> getActivity() {
        Map<String, Object> result = new HashMap<>();
        result.put("activity", userService.getRecentActivity(10));
        result.put("todayStats", userService.getTodayStats());
        return result;
    }

    /**
     * System Activity Log page
     */
    @GetMapping("/activity-log")
    public String activityLog(Model model) {
        model.addAttribute("activity", userService.getRecentActivity(50));
        model.addAttribute("todayStats", userService.getTodayStats());
        model.addAttribute("pageTitle", "Activity Log");
        return "admin/activity-log";
    }

    // ============================================
    // Bulk User Import
    // ============================================

    /**
     * User Import page
     */
    @GetMapping("/users/import")
    public String importPage(Model model) {
        model.addAttribute("pageTitle", "Import Users");
        return "admin/user-import";
    }

    /**
     * Handle user Excel import
     */
    @PostMapping("/users/import")
    public String importUsers(@RequestParam("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/admin/users/import";
        }
        try {
            List<Map<String, String>> userDataList = ExcelUtil.parseUsers(file.getInputStream());
            int count = userService.batchImportUsers(userDataList);
            redirectAttributes.addFlashAttribute("message",
                String.format("Successfully imported %d users.", count));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Reset user password (AJAX)
     */
    @PostMapping("/users/reset-password/{id}")
    @ResponseBody
    public Map<String, Object> resetPassword(@PathVariable Long id,
                                              @RequestParam String newPassword) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.getById(id);
            if (user != null) {
                user.setPassword(userService.encodePassword(newPassword));
                userService.updateUser(user);
                result.put("success", true);
                result.put("message", "Password reset successfully for " + user.getUsername());
            } else {
                result.put("success", false);
                result.put("message", "User not found.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
