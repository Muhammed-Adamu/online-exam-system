package com.exam.mapper;

import lombok.Data;

/**
 * Question Correct Rate DTO
 */
@Data
public class QuestionCorrectRate {
    private Long questionId;
    private String questionContent;
    private Integer totalAnswers;
    private Integer correctCount;
    private Double correctRate;
}
