package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Answer Record Entity - Individual question answers
 */
@Data
@TableName("answer_records")
public class AnswerRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examRecordId;

    private Long questionId;

    /** Student's submitted answer */
    private String studentAnswer;

    /** NULL=not graded, 0=wrong, 1=correct */
    private Integer isCorrect;

    private BigDecimal scoreEarned;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // Non-table fields for display
    @TableField(exist = false)
    private Question question;
}
