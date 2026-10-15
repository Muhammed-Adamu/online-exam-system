package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Paper-Question relationship Entity
 */
@Data
@TableName("paper_questions")
public class PaperQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private Long questionId;

    private Integer questionOrder;

    /** Score assigned for this question in this specific paper */
    private BigDecimal score;

    // Non-table field for display
    @TableField(exist = false)
    private Question question;
}
