package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Exam Record Entity - Tracks a student taking an exam
 */
@Data
@TableName("exam_records")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long paperId;

    /** IN_PROGRESS, SUBMITTED, GRADED, TIMEOUT */
    private String status;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    private BigDecimal totalScore;

    /** Actual time spent in seconds */
    private Integer timeSpentSeconds;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // Non-table fields for display
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String paperTitle;

    @TableField(exist = false)
    private Integer questionCount;
}
