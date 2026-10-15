package com.exam.service;

import com.exam.entity.ExamPaper;
import com.exam.entity.ExamRecord;
import com.exam.entity.Notification;
import com.exam.entity.User;
import com.exam.mapper.ExamPaperMapper;
import com.exam.mapper.NotificationMapper;
import com.exam.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification Service - Create and manage teacher notifications
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ExamPaperMapper examPaperMapper;

    /**
     * Create a notification when a student submits an exam (for the teacher)
     */
    public Notification createExamSubmittedNotification(ExamRecord record) {
        User student = userMapper.selectById(record.getStudentId());
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());

        if (paper == null || student == null || paper.getCreatedBy() == null) {
            return null;
        }

        String studentName = student.getRealName() != null ? student.getRealName() : student.getUsername();
        String paperTitle = paper.getTitle();

        Notification notification = new Notification();
        notification.setRecipientId(paper.getCreatedBy());
        notification.setType("EXAM_SUBMITTED");
        notification.setReferenceId(record.getId());
        notification.setIsRead(0);
        notification.setMessage(String.format(
            "%s submitted \"%s\" — Score: %s",
            studentName,
            paperTitle,
            record.getTotalScore() != null ? record.getTotalScore().toString() : "N/A"
        ));

        notificationMapper.insert(notification);

        // Also notify the student that their exam was graded
        createExamGradedNotification(record, paper, student);

        return notification;
    }

    /**
     * Create a notification for a student when their exam is graded
     */
    public Notification createExamGradedNotification(ExamRecord record, ExamPaper paper, User student) {
        if (student == null) return null;

        String paperTitle = paper != null ? paper.getTitle() : "Unknown Paper";
        String scoreStr = record.getTotalScore() != null ? record.getTotalScore().toString() : "N/A";

        Notification notification = new Notification();
        notification.setRecipientId(student.getId());
        notification.setType("EXAM_GRADED");
        notification.setReferenceId(record.getId());
        notification.setIsRead(0);
        notification.setMessage(String.format(
            "Your exam \"%s\" has been graded — Score: %s",
            paperTitle, scoreStr
        ));

        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * Send a notice from a teacher to a specific student
     */
    public Notification sendTeacherNotice(Long teacherId, Long studentId, String message) {
        User teacher = userMapper.selectById(teacherId);
        String teacherName = teacher != null ? (teacher.getRealName() != null ? teacher.getRealName() : teacher.getUsername()) : "Teacher";

        Notification notification = new Notification();
        notification.setRecipientId(studentId);
        notification.setType("TEACHER_NOTICE");
        notification.setReferenceId(null);
        notification.setIsRead(0);
        notification.setMessage(String.format("[From %s] %s", teacherName, message));

        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * Get recent notifications for a teacher
     */
    public List<Notification> getRecentNotifications(Long teacherId, int limit) {
        List<Notification> notifications = notificationMapper.findByRecipientWithDetails(teacherId, limit);
        // Compute relative time string
        for (Notification n : notifications) {
            n.setTimeAgo(formatTimeAgo(n.getCreatedAt()));
        }
        return notifications;
    }

    /**
     * Get unread notification count
     */
    public int getUnreadCount(Long teacherId) {
        return notificationMapper.countUnread(teacherId);
    }

    /**
     * Mark a single notification as read
     */
    public boolean markAsRead(Long id) {
        return notificationMapper.markAsRead(id) > 0;
    }

    /**
     * Mark all notifications as read for a teacher
     */
    public boolean markAllAsRead(Long teacherId) {
        return notificationMapper.markAllAsRead(teacherId) > 0;
    }

    /**
     * Format a time as a relative "time ago" string
     */
    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        long seconds = Duration.between(dateTime, LocalDateTime.now()).getSeconds();

        if (seconds < 60) return "just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        if (days < 7) return days + "d ago";
        return dateTime.toLocalDate().toString();
    }
}
