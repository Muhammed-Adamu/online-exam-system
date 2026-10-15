package com.exam.interceptor;

import com.exam.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Login Interceptor - Checks session for authenticated user
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");

        if (user == null) {
            // Not logged in, redirect to login page
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        // Check role-based access
        String path = request.getRequestURI();
        String role = user.getRole();

        if (path.contains("/admin/") && !"ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login?error=access_denied");
            return false;
        }
        if (path.contains("/teacher/") && !"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login?error=access_denied");
            return false;
        }
        if (path.contains("/student/") && !"STUDENT".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login?error=access_denied");
            return false;
        }

        return true;
    }
}
