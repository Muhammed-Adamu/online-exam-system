package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import java.util.Map;

/**
 * User Mapper
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM users WHERE role = #{role} AND enabled = 1 ORDER BY created_at DESC")
    List<User> findByRole(@Param("role") String role);

    @Select("SELECT * FROM users WHERE class_name = #{className} AND enabled = 1")
    List<User> findByClassName(@Param("className") String className);

    @Select("SELECT COUNT(*) FROM users WHERE role = #{role}")
    Long countByRole(@Param("role") String role);

    @Select("SELECT DISTINCT class_name FROM users WHERE class_name IS NOT NULL ORDER BY class_name")
    List<String> findAllClassNames();

    /**
     * Search users with optional filters
     */
    @Select("<script>" +
            "SELECT * FROM users WHERE 1=1 " +
            "<if test='role != null and role != \"\"'>AND role = #{role}</if> " +
            "<if test='status != null'>AND enabled = #{status}</if> " +
            "<if test='keyword != null and keyword != \"\"'>AND (username LIKE CONCAT('%',#{keyword},'%') OR real_name LIKE CONCAT('%',#{keyword},'%'))</if> " +
            "ORDER BY created_at DESC</script>")
    List<User> searchUsers(@Param("role") String role,
                           @Param("status") Integer status,
                           @Param("keyword") String keyword);

    /**
     * Count users with optional filters (for pagination)
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM users WHERE 1=1 " +
            "<if test='role != null and role != \"\"'>AND role = #{role}</if> " +
            "<if test='status != null'>AND enabled = #{status}</if> " +
            "<if test='keyword != null and keyword != \"\"'>AND (username LIKE CONCAT('%',#{keyword},'%') OR real_name LIKE CONCAT('%',#{keyword},'%'))</if> " +
            "</script>")
    long countFiltered(@Param("role") String role,
                       @Param("status") Integer status,
                       @Param("keyword") String keyword);

    /**
     * Batch update user statuses
     */
    @Update("<script>" +
            "UPDATE users SET enabled = #{enabled} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("enabled") Integer enabled);

    /**
     * Batch delete users
     */
    @Update("<script>" +
            "DELETE FROM users WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int batchDelete(@Param("ids") List<Long> ids);

    /**
     * Get system activity: recent registrations + exam submissions
     */
    @Select("(SELECT 'REGISTRATION' AS activity_type, u.id AS ref_id, u.real_name AS actor_name, " +
            "u.role AS actor_role, " +
            "CONCAT('Joined as ', LOWER(u.role)) AS detail, " +
            "u.created_at AS activity_time " +
            "FROM users u) " +
            "UNION ALL " +
            "(SELECT 'EXAM_SUBMISSION' AS activity_type, er.id AS ref_id, " +
            "stu.real_name AS actor_name, 'STUDENT' AS actor_role, " +
            "CONCAT('Scored ', CAST(COALESCE(er.total_score, 0) AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci, ' on ', p.title) AS detail, " +
            "COALESCE(er.submit_time, er.created_at) AS activity_time " +
            "FROM exam_records er " +
            "LEFT JOIN users stu ON er.student_id = stu.id " +
            "LEFT JOIN exam_papers p ON er.paper_id = p.id " +
            "WHERE er.status IN ('GRADED', 'SUBMITTED')) " +
            "ORDER BY activity_time DESC LIMIT #{limit}")
    List<Map<String, Object>> getRecentActivity(@Param("limit") int limit);

    /**
     * Count today's submissions
     */
    @Select("SELECT COUNT(*) FROM exam_records WHERE DATE(submit_time) = CURDATE() AND status IN ('GRADED', 'SUBMITTED')")
    long countTodaySubmissions();

    /**
     * Count today's new users
     */
    @Select("SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE()")
    long countTodayNewUsers();
}
