package com.exam.controller;

import com.exam.entity.User;
import com.exam.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

/**
 * Login Controller - Authentication and session management
 */
@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    /**
     * Login page
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if ("access_denied".equals(error)) {
            model.addAttribute("error", "Access denied. Insufficient permissions.");
            log.warn("Login page: access denied message shown");
        } else if ("invalid".equals(error)) {
            model.addAttribute("error", "Invalid username or password.");
        } else if ("logout".equals(error)) {
            model.addAttribute("message", "You have been logged out successfully.");
            log.info("User logged out successfully");
        }
        return "login";
    }

    /**
     * Handle login
     */
    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

        log.info("Login attempt for user: {}", username);

        try {
            User user = userService.login(username, password);
            if (user == null) {
                log.warn("Login failed for user '{}': invalid credentials", username);
                redirectAttributes.addFlashAttribute("error", "Invalid username or password.");
                return "redirect:/login?error=invalid";
            }

            if (user.getEnabled() != 1) {
                log.warn("Login failed for user '{}': account disabled", username);
                redirectAttributes.addFlashAttribute("error", "Account is disabled. Contact administrator.");
                return "redirect:/login?error=invalid";
            }

            // Set session
            session.setAttribute("loginUser", user);
            log.info("Login successful for user: {} (role: {})", username, user.getRole());

            // Redirect based on role
            switch (user.getRole()) {
                case "ADMIN":
                    return "redirect:/admin/dashboard";
                case "TEACHER":
                    return "redirect:/teacher/dashboard";
                case "STUDENT":
                    return "redirect:/student/dashboard";
                default:
                    log.warn("Unknown role '{}' for user: {}", user.getRole(), username);
                    return "redirect:/login";
            }
        } catch (Exception e) {
            log.error("Login error for user '{}': {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "A system error occurred. Please try again later.");
            return "redirect:/login?error=invalid";
        }
    }

    /**
     * Home page redirect
     */
    @GetMapping("/")
    public String home(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }
        switch (user.getRole()) {
            case "ADMIN": return "redirect:/admin/dashboard";
            case "TEACHER": return "redirect:/teacher/dashboard";
            case "STUDENT": return "redirect:/student/dashboard";
            default: return "redirect:/login";
        }
    }

    /**
     * Change Password Page
     */
    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        model.addAttribute("pageTitle", "Change Password");
        return "common/change-password";
    }

    /**
     * Handle Change Password
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/change-password";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters.");
            return "redirect:/change-password";
        }

        try {
            userService.changePassword(loginUser.getId(), currentPassword, newPassword);
            log.info("Password changed successfully for user: {}", loginUser.getUsername());
            redirectAttributes.addFlashAttribute("message", "Password changed successfully.");
        } catch (Exception e) {
            log.warn("Password change failed for user {}: {}", loginUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/change-password";
    }

    /**
     * Logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        log.info("User logging out: {}", user != null ? user.getUsername() : "unknown");
        session.invalidate();
        return "redirect:/login?error=logout";
    }
}
