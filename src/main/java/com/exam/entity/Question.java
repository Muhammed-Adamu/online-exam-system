package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Question Entity - Supports SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK
 */
@Data
@TableName("questions")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK */
    private String questionType;

    /** Subject category (e.g., Java Basics, OOP Concepts) */
    private String category;

    /** EASY, MEDIUM, HARD */
    private String difficulty;

    /** Default score for this question */
    private BigDecimal score;

    /** Question stem / content */
    private String content;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    /** Correct answer (comma-separated for multiple choice) */
    private String correctAnswer;

    /** Answer explanation */
    private String explanation;

    /** Creator teacher ID */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * Get options as a LinkedHashMap for Thymeleaf iteration
     * Used in exam templates to display answer choices
     */
    public java.util.Map<String, String> getOptions() {
        java.util.Map<String, String> options = new java.util.LinkedHashMap<>();
        if (optionA != null && !optionA.isEmpty()) options.put("A", optionA);
        if (optionB != null && !optionB.isEmpty()) options.put("B", optionB);
        if (optionC != null && !optionC.isEmpty()) options.put("C", optionC);
        if (optionD != null && !optionD.isEmpty()) options.put("D", optionD);
        return options;
    }
}
