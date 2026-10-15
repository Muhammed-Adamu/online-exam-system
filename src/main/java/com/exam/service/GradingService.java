package com.exam.service;

import com.exam.entity.*;
import com.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Grading Service - Auto grading and score management
 */
@Service
public class GradingService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private WrongAnswerMapper wrongAnswerMapper;

    /**
     * Re-grade a specific exam (for manual review)
     */
    @Transactional
    public ExamRecord regradeExam(Long examRecordId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null) return null;

        List<AnswerRecord> answers = answerRecordMapper.findByExamRecordId(examRecordId);
        BigDecimal totalScore = BigDecimal.ZERO;

        for (AnswerRecord ar : answers) {
            Question question = questionMapper.selectById(ar.getQuestionId());
            if (question == null) continue;

            PaperQuestion pq = paperQuestionMapper.findByPaperAndQuestion(
                    record.getPaperId(), ar.getQuestionId());
            BigDecimal questionScore = pq != null ? pq.getScore() : question.getScore();

            boolean isCorrect = checkAnswer(ar.getStudentAnswer(), question);
            BigDecimal earned = isCorrect ? questionScore : BigDecimal.ZERO;

            answerRecordMapper.gradeAnswer(ar.getId(), isCorrect ? 1 : 0, earned);
            totalScore = totalScore.add(earned);
        }

        record.setTotalScore(totalScore);
        record.setStatus("GRADED");
        examRecordMapper.updateById(record);

        return record;
    }

    private boolean checkAnswer(String studentAnswer, Question question) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) return false;
        String sa = studentAnswer.trim();
        String ca = question.getCorrectAnswer().trim();

        switch (question.getQuestionType()) {
            case "SINGLE_CHOICE":
            case "TRUE_FALSE":
                return sa.equalsIgnoreCase(ca);
            case "MULTIPLE_CHOICE":
                Set<String> ss = new TreeSet<>(Arrays.asList(sa.toUpperCase().split("\\s*,\\s*")));
                Set<String> cs = new TreeSet<>(Arrays.asList(ca.toUpperCase().split("\\s*,\\s*")));
                return ss.equals(cs);
            case "FILL_BLANK":
                return sa.equalsIgnoreCase(ca) || sa.toLowerCase().contains(ca.toLowerCase());
            default:
                return false;
        }
    }
}
