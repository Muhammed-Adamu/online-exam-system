package com.exam.controller;

import com.exam.entity.Notification;
import com.exam.entity.User;
import com.exam.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notification Controller - REST API for notification bell
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Get recent notifications for the logged-in teacher
     */
    @GetMapping
    public Map<String, Object> getNotifications(HttpSession session,
                                                 @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            result.put("success", false);
            result.put("message", "Not logged in");
            return result;
        }

        List<Notification> notifications = notificationService.getRecentNotifications(loginUser.getId(), limit);
        int unreadCount = notificationService.getUnreadCount(loginUser.getId());

        result.put("success", true);
        result.put("notifications", notifications);
        result.put("unreadCount", unreadCount);
        return result;
    }

    /**
     * Get unread count only (lightweight polling)
     */
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            result.put("success", false);
            result.put("count", 0);
            return result;
        }

        int count = notificationService.getUnreadCount(loginUser.getId());
        result.put("success", true);
        result.put("count", count);
        return result;
    }

    /**
     * Mark a single notification as read
     */
    @PostMapping("/{id}/read")
    public Map<String, Object> markAsRead(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            result.put("success", false);
            return result;
        }

        notificationService.markAsRead(id);
        result.put("success", true);
        return result;
    }

    /**
     * Mark all notifications as read
     */
    @PostMapping("/read-all")
    public Map<String, Object> markAllAsRead(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            result.put("success", false);
            return result;
        }

        notificationService.markAllAsRead(loginUser.getId());
        result.put("success", true);
        return result;
    }
}
