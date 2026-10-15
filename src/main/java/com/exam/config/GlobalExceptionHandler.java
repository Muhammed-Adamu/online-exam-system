package com.exam.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

/**
 * Global exception handler - provides centralized error logging and user-friendly messages
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SQLException.class)
    public String handleSQLException(SQLException ex, HttpServletRequest request) {
        log.error("[DB-ERROR] {} | URL: {} | Message: {}",
                ex.getClass().getSimpleName(),
                request.getRequestURL(),
                ex.getMessage(),
                ex);
        return "redirect:/login?error=system";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, RedirectAttributes redirectAttributes,
                                         HttpServletRequest request) {
        log.error("[RUNTIME-ERROR] {} | URL: {} | Message: {}",
                ex.getClass().getSimpleName(),
                request.getRequestURL(),
                ex.getMessage(),
                ex);
        redirectAttributes.addFlashAttribute("error", "Operation failed: " + ex.getMessage());
        // Redirect to login with error instead of "/" to avoid redirect loops
        return "redirect:/login?error=system";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, RedirectAttributes redirectAttributes,
                                         HttpServletRequest request) {
        log.error("[UNEXPECTED-ERROR] {} | URL: {} | Message: {}",
                ex.getClass().getSimpleName(),
                request.getRequestURL(),
                ex.getMessage(),
                ex);
        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred.");
        return "redirect:/login?error=system";
    }
}
