package com.exam.service;

import com.exam.entity.*;
import com.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Exam Service - Core exam logic
 */
@Service
public class ExamService {

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

    @Autowired
    private ExamPaperMapper examPaperMapper;

    @Autowired
    private NotificationService notificationService;

    /**
     * Start an exam for a student
     */
    @Transactional
    public ExamRecord startExam(Long studentId, Long paperId) {
        // Check for duplicate
        ExamRecord existing = examRecordMapper.findByStudentAndPaper(studentId, paperId);
        if (existing != null && !"IN_PROGRESS".equals(existing.getStatus())) {
            throw new RuntimeException("You have already taken this exam.");
        }
        if (existing != null && "IN_PROGRESS".equals(existing.getStatus())) {
            return examRecordMapper.findByIdWithDetails(existing.getId());
        }

        // Create new exam record
        ExamRecord record = new ExamRecord();
        record.setStudentId(studentId);
        record.setPaperId(paperId);
        record.setStatus("IN_PROGRESS");
        record.setStartTime(LocalDateTime.now());
        examRecordMapper.insert(record);

        // Create answer records for each question
        List<PaperQuestion> paperQuestions = paperQuestionMapper.findByPaperId(paperId);
        for (PaperQuestion pq : paperQuestions) {
            AnswerRecord ar = new AnswerRecord();
            ar.setExamRecordId(record.getId());
            ar.setQuestionId(pq.getQuestionId());
            ar.setScoreEarned(BigDecimal.ZERO);
            answerRecordMapper.insert(ar);
        }

        return examRecordMapper.findByIdWithDetails(record.getId());
    }

    /**
     * Save a single answer (AJAX)
     */
    public boolean saveAnswer(Long examRecordId, Long questionId, String answer) {
        AnswerRecord ar = answerRecordMapper.findByExamAndQuestion(examRecordId, questionId);
        if (ar != null) {
            return answerRecordMapper.updateAnswer(ar.getId(), answer) > 0;
        }
        return false;
    }

    /**
     * Submit exam and grade automatically
     */
    @Transactional
    public ExamRecord submitExam(Long examRecordId, Integer timeSpentSeconds) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null) {
            throw new RuntimeException("Exam record not found.");
        }
        if (!"IN_PROGRESS".equals(record.getStatus())) {
            throw new RuntimeException("Exam already submitted.");
        }

        // Server-side time enforcement — prevent submission after time expires
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        if (paper != null && record.getStartTime() != null) {
            long elapsedSeconds = Duration.between(record.getStartTime(), LocalDateTime.now()).getSeconds();
            long allowedSeconds = paper.getDurationMinutes() * 60L;
            // 2-minute grace period for network latency
            if (elapsedSeconds > allowedSeconds + 120) {
                record.setStatus("TIMEOUT");
                record.setSubmitTime(LocalDateTime.now());
                examRecordMapper.updateById(record);
                throw new RuntimeException(
                    String.format("Exam time has expired. Allowed: %d min, elapsed: %d min.",
                        allowedSeconds / 60, elapsedSeconds / 60));
            }
        }

        // Grade all answers
        List<AnswerRecord> answers = answerRecordMapper.findByExamRecordId(examRecordId);
        BigDecimal totalScore = BigDecimal.ZERO;

        for (AnswerRecord ar : answers) {
            Question question = questionMapper.selectById(ar.getQuestionId());
            if (question == null) continue;

            PaperQuestion pq = paperQuestionMapper.findByPaperAndQuestion(
                    record.getPaperId(), ar.getQuestionId());
            BigDecimal questionScore = pq != null ? pq.getScore() : question.getScore();

            boolean isCorrect = gradeAnswer(ar.getStudentAnswer(), question);
            BigDecimal earned = isCorrect ? questionScore : BigDecimal.ZERO;

            answerRecordMapper.gradeAnswer(ar.getId(), isCorrect ? 1 : 0, earned);
            totalScore = totalScore.add(earned);

            // Handle wrong answers
            if (!isCorrect) {
                WrongAnswer wa = wrongAnswerMapper.findByStudentAndQuestion(
                        record.getStudentId(), ar.getQuestionId());
                if (wa == null) {
                    wa = new WrongAnswer();
                    wa.setStudentId(record.getStudentId());
                    wa.setQuestionId(ar.getQuestionId());
                    wa.setWrongAnswer(ar.getStudentAnswer());
                    wa.setWrongCount(1);
                    wa.setMastered(0);
                    wa.setLastWrongTime(LocalDateTime.now());
                    wrongAnswerMapper.insert(wa);
                } else {
                    wrongAnswerMapper.incrementWrongCount(
                            record.getStudentId(), ar.getQuestionId(), ar.getStudentAnswer());
                }
            }
        }

        // Update exam record
        record.setStatus("GRADED");
        record.setSubmitTime(LocalDateTime.now());
        record.setTotalScore(totalScore);
        record.setTimeSpentSeconds(timeSpentSeconds);
        examRecordMapper.updateById(record);

        // Create notification for the paper's teacher
        try {
            notificationService.createExamSubmittedNotification(record);
        } catch (Exception e) {
            // Don't fail the submission if notification fails
            System.err.println("[WARN] Failed to create notification: " + e.getMessage());
        }

        return record;
    }

    /**
     * Auto-grade a single answer against the correct answer
     */
    private boolean gradeAnswer(String studentAnswer, Question question) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return false;
        }

        String student = studentAnswer.trim();
        String correct = question.getCorrectAnswer().trim();

        switch (question.getQuestionType()) {
            case "SINGLE_CHOICE":
            case "TRUE_FALSE":
                // Exact match for single choice and true/false
                return student.equalsIgnoreCase(correct);

            case "MULTIPLE_CHOICE":
                // Normalize comma-separated answers and compare as sets
                Set<String> studentSet = new TreeSet<>(
                        Arrays.asList(student.toUpperCase().split("\\s*,\\s*")));
                Set<String> correctSet = new TreeSet<>(
                        Arrays.asList(correct.toUpperCase().split("\\s*,\\s*")));
                return studentSet.equals(correctSet);

            case "FILL_BLANK":
                // Case-insensitive contains match for fill-in-the-blank
                return student.equalsIgnoreCase(correct)
                        || student.toLowerCase().contains(correct.toLowerCase());

            default:
                return false;
        }
    }

    /**
     * Get student exam records
     */
    public List<ExamRecord> getStudentRecords(Long studentId) {
        return examRecordMapper.findByStudentId(studentId);
    }

    /**
     * Get paper exam records (for teacher analysis)
     */
    public List<ExamRecord> getPaperRecords(Long paperId) {
        return examRecordMapper.findByPaperId(paperId);
    }

    /**
     * Get exam record by ID
     */
    public ExamRecord getRecordById(Long id) {
        return examRecordMapper.findByIdWithDetails(id);
    }

    /**
     * Get answers for an exam record (with question details populated)
     */
    public List<AnswerRecord> getExamAnswers(Long examRecordId) {
        List<AnswerRecord> answers = answerRecordMapper.findByExamRecordId(examRecordId);
        // Populate the nested Question object for each AnswerRecord
        for (AnswerRecord ar : answers) {
            Question question = questionMapper.selectById(ar.getQuestionId());
            ar.setQuestion(question);
        }
        return answers;
    }
}
