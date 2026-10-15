package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Notification Entity - Teacher notifications for exam submissions
 */
@Data
@TableName("notifications")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Teacher ID who receives this notification */
    private Long recipientId;

    /** Notification message content */
    private String message;

    /** Type: EXAM_SUBMITTED, etc. */
    private String type;

    /** Reference ID (e.g., exam_record_id for quick link) */
    private Long referenceId;

    /** Whether the recipient has read it */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // Non-table fields for display
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String paperTitle;

    @TableField(exist = false)
    private Long paperId;

    @TableField(exist = false)
    private String timeAgo;
}
