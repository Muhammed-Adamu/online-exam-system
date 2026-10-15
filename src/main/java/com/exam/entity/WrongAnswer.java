package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Wrong Answer Notebook Entity
 */
@Data
@TableName("wrong_answer_notebook")
public class WrongAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long questionId;

    /** What the student answered incorrectly */
    private String wrongAnswer;

    /** Number of times answered wrong */
    private Integer wrongCount;

    private LocalDateTime lastWrongTime;

    /** Has the student mastered this question? */
    private Integer mastered;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // Non-table fields for display
    @TableField(exist = false)
    private Question question;
}
