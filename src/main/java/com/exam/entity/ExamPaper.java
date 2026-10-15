package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Exam Paper Entity
 */
@Data
@TableName("exam_papers")
public class ExamPaper {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Exam title */
    private String title;

    /** Exam description */
    private String description;

    /** Total score */
    private BigDecimal totalScore;

    /** Exam duration in minutes */
    private Integer durationMinutes;

    /** Available start time */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    /** Available end time */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;

    /** DRAFT, PUBLISHED, CLOSED */
    private String status;

    /** MANUAL or RULE_BASED assembly */
    private String assemblyType;

    /** Creator teacher ID */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // Non-table fields for display
    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private Integer questionCount;

    @TableField(exist = false)
    private Integer attemptCount;
}
