package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.Notification;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * Notification Mapper
 */
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT n.*, u.real_name AS student_name, p.title AS paper_title, " +
            "er.paper_id AS paper_id " +
            "FROM notifications n " +
            "LEFT JOIN exam_records er ON n.reference_id = er.id " +
            "LEFT JOIN users u ON er.student_id = u.id " +
            "LEFT JOIN exam_papers p ON er.paper_id = p.id " +
            "WHERE n.recipient_id = #{recipientId} " +
            "ORDER BY n.created_at DESC LIMIT #{limit}")
    List<Notification> findByRecipientWithDetails(@Param("recipientId") Long recipientId,
                                                   @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM notifications WHERE recipient_id = #{recipientId} AND is_read = 0")
    int countUnread(@Param("recipientId") Long recipientId);

    @Update("UPDATE notifications SET is_read = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    @Update("UPDATE notifications SET is_read = 1 WHERE recipient_id = #{recipientId} AND is_read = 0")
    int markAllAsRead(@Param("recipientId") Long recipientId);
}
