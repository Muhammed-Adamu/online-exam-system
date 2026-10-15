package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * User Service
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Authenticate user login
     */
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && user.getEnabled() == 1) {
            String storedPassword = user.getPassword();

            // Support BCrypt, MD5, and plain text passwords (backward compatibility)
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                // BCrypt
                if (passwordEncoder.matches(password, storedPassword)) {
                    log.debug("BCrypt password matched for user: {}", username);
                    return user;
                }
            }

            // MD5 fallback (for legacy passwords)
            String md5Password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
            if (storedPassword.equals(password) || storedPassword.equals(md5Password)) {
                log.debug("MD5/plain password matched for user: {}", username);
                return user;
            }

            log.debug("Password mismatch for user: {}", username);
        }
        return null;
    }

    /**
     * Encode password using BCrypt
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Get user by ID
     */
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * Get all users (non-paginated, for small lists)
     */
    public List<User> getAllUsers() {
        return userMapper.selectList(new QueryWrapper<User>().orderByDesc("created_at"));
    }

    /**
     * Get all users with pagination
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> getPage(int current, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        return userMapper.selectPage(page,
                new QueryWrapper<User>().orderByDesc("created_at"));
    }

    /**
     * Get users by role
     */
    public List<User> getByRole(String role) {
        return userMapper.findByRole(role);
    }

    /**
     * Create user with BCrypt password encoding
     */
    public boolean createUser(User user) {
        // Check username uniqueness
        User existing = userMapper.findByUsername(user.getUsername());
        if (existing != null) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        // Encode password if not already BCrypt
        if (user.getPassword() != null && !isBCryptEncoded(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userMapper.insert(user) > 0;
    }

    /**
     * Update user. If password is empty, keep existing. Otherwise encode it.
     */
    public boolean updateUser(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // Keep existing password — don't update the password field
            User existing = userMapper.selectById(user.getId());
            if (existing != null) {
                user.setPassword(existing.getPassword());
            }
        } else if (!isBCryptEncoded(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userMapper.updateById(user) > 0;
    }

    /**
     * Check if a password string is already BCrypt-encoded
     */
    private boolean isBCryptEncoded(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    /**
     * Delete user
     */
    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    /**
     * Toggle user status (enable/disable)
     */
    public boolean toggleStatus(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setEnabled(user.getEnabled() == 1 ? 0 : 1);
            return userMapper.updateById(user) > 0;
        }
        return false;
    }

    /**
     * Get statistics by role
     */
    public Map<String, Long> getRoleCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("admin", userMapper.countByRole("ADMIN"));
        counts.put("teacher", userMapper.countByRole("TEACHER"));
        counts.put("student", userMapper.countByRole("STUDENT"));
        return counts;
    }

    /**
     * Change password for a user. Verifies old password matches before updating.
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }

        String stored = user.getPassword();

        // Verify old password
        boolean oldMatches = false;
        if (isBCryptEncoded(stored)) {
            oldMatches = passwordEncoder.matches(oldPassword, stored);
        } else {
            // Legacy MD5/plain fallback
            String md5Old = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
            oldMatches = stored.equals(oldPassword) || stored.equals(md5Old);
        }

        if (!oldMatches) {
            throw new RuntimeException("Current password is incorrect.");
        }

        // Encode and save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        return userMapper.updateById(user) > 0;
    }

    /**
     * Get all class names
     */
    public List<String> getAllClassNames() {
        return userMapper.findAllClassNames();
    }

    /**
     * Get students by class
     */
    public List<User> getStudentsByClass(String className) {
        return userMapper.findByClassName(className);
    }

    /**
     * Search users with optional filters
     */
    public List<User> searchUsers(String role, Integer status, String keyword) {
        return userMapper.searchUsers(role, status, keyword);
    }

    /**
     * Count users matching filters
     */
    public long countFiltered(String role, Integer status, String keyword) {
        return userMapper.countFiltered(role, status, keyword);
    }

    /**
     * Batch enable/disable users
     */
    public int batchUpdateStatus(List<Long> ids, boolean enable) {
        return userMapper.batchUpdateStatus(ids, enable ? 1 : 0);
    }

    /**
     * Batch delete users
     */
    public int batchDelete(List<Long> ids) {
        return userMapper.batchDelete(ids);
    }

    /**
     * Get recent system activity
     */
    public List<Map<String, Object>> getRecentActivity(int limit) {
        return userMapper.getRecentActivity(limit);
    }

    /**
     * Get today's stats for admin dashboard
     */
    public Map<String, Object> getTodayStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todaySubmissions", userMapper.countTodaySubmissions());
        stats.put("todayNewUsers", userMapper.countTodayNewUsers());
        return stats;
    }

    /**
     * Batch import users from Excel data
     */
    public int batchImportUsers(List<Map<String, String>> userDataList) {
        int count = 0;
        for (Map<String, String> data : userDataList) {
            User existing = userMapper.findByUsername(data.get("username"));
            if (existing != null) continue; // Skip existing users

            User user = new User();
            user.setUsername(data.get("username"));
            user.setPassword(passwordEncoder.encode(data.get("password")));
            user.setRealName(data.get("real_name"));
            user.setRole(data.get("role"));
            user.setEmail(data.get("email"));
            user.setPhone(data.get("phone"));
            user.setClassName(data.get("class_name"));
            user.setEnabled(1);
            userMapper.insert(user);
            count++;
        }
        return count;
    }
}
