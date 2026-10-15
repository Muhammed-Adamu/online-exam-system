package com.exam.mapper;

import lombok.Data;

/**
 * Score Distribution DTO
 */
@Data
public class ScoreDistribution {
    private Integer excellent;  // 90-100
    private Integer good;       // 80-89
    private Integer average;    // 70-79
    private Integer pass;       // 60-69
    private Integer fail;       // <60
}
