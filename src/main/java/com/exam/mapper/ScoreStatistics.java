package com.exam.mapper;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Score Statistics DTO
 */
@Data
public class ScoreStatistics {
    private Integer totalAttempts;
    private BigDecimal averageScore;
    private BigDecimal highestScore;
    private BigDecimal lowestScore;
}
