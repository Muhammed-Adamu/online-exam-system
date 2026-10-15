package com.exam.service;

import com.exam.entity.*;
import com.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Statistics Service - Score analysis, charts data
 */
@Service
public class StatisticsService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    @Autowired
    private ExamPaperMapper examPaperMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private WrongAnswerMapper wrongAnswerMapper;

    /**
     * Get paper score statistics
     */
    public Map<String, Object> getPaperStatistics(Long paperId) {
        Map<String, Object> stats = new HashMap<>();

        ScoreStatistics scoreStats = examRecordMapper.getPaperStatistics(paperId);
        ScoreDistribution distribution = examRecordMapper.getScoreDistribution(paperId);

        stats.put("totalAttempts", scoreStats.getTotalAttempts());
        stats.put("averageScore", scoreStats.getAverageScore());
        stats.put("highestScore", scoreStats.getHighestScore());
        stats.put("lowestScore", scoreStats.getLowestScore());

        // Score distribution chart data
        Map<String, Integer> distributionMap = new LinkedHashMap<>();
        distributionMap.put("Excellent (90-100)", distribution.getExcellent());
        distributionMap.put("Good (80-89)", distribution.getGood());
        distributionMap.put("Average (70-79)", distribution.getAverage());
        distributionMap.put("Pass (60-69)", distribution.getPass());
        distributionMap.put("Fail (<60)", distribution.getFail());
        stats.put("distribution", distributionMap);

        return stats;
    }

    /**
     * Get question correct rates for a paper (ECharts data)
     */
    public List<QuestionCorrectRate> getQuestionCorrectRates(Long paperId) {
        return answerRecordMapper.getQuestionCorrectRates(paperId);
    }

    /**
     * Get class performance statistics
     */
    public List<Map<String, Object>> getClassStatistics(String className) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<User> students = userMapper.findByClassName(className);

        for (User student : students) {
            List<ExamRecord> records = examRecordMapper.findByStudentId(student.getId());
            if (!records.isEmpty()) {
                Map<String, Object> studentStat = new HashMap<>();
                studentStat.put("studentName", student.getRealName());
                studentStat.put("examsTaken", records.size());

                BigDecimal avgScore = records.stream()
                        .filter(r -> r.getTotalScore() != null)
                        .map(ExamRecord::getTotalScore)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(new BigDecimal(Math.max(1, records.stream()
                                .filter(r -> r.getTotalScore() != null).count())), 2, RoundingMode.HALF_UP);
                studentStat.put("averageScore", avgScore);
                result.add(studentStat);
            }
        }

        return result;
    }

    /**
     * Get teacher dashboard statistics
     */
    public Map<String, Object> getTeacherDashboardStats(Long teacherId) {
        Map<String, Object> stats = new HashMap<>();

        // Papers created
        List<ExamPaper> papers = examPaperMapper.findByCreatorWithDetails(teacherId);
        stats.put("totalPapers", papers.size());
        stats.put("publishedPapers", papers.stream().filter(p -> "PUBLISHED".equals(p.getStatus())).count());

        // Total questions in bank
        stats.put("totalQuestions", questionMapper.selectCount(null));

        // Total students
        stats.put("totalStudents", userMapper.countByRole("STUDENT"));

        return stats;
    }

    /**
     * Get student dashboard statistics
     */
    public Map<String, Object> getStudentDashboardStats(Long studentId) {
        Map<String, Object> stats = new HashMap<>();

        // Exams taken
        List<ExamRecord> records = examRecordMapper.findByStudentId(studentId);
        long completed = records.stream()
                .filter(r -> "GRADED".equals(r.getStatus()) || "SUBMITTED".equals(r.getStatus()))
                .count();
        stats.put("examsTaken", completed);

        // Average score
        if (completed > 0) {
            BigDecimal avgScore = records.stream()
                    .filter(r -> r.getTotalScore() != null && "GRADED".equals(r.getStatus()))
                    .map(ExamRecord::getTotalScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(Math.max(1, records.stream()
                            .filter(r -> r.getTotalScore() != null && "GRADED".equals(r.getStatus()))
                            .count())), 2, RoundingMode.HALF_UP);
            stats.put("averageScore", avgScore);
        } else {
            stats.put("averageScore", BigDecimal.ZERO);
        }

        // Wrong answers count
        stats.put("wrongAnswers", wrongAnswerMapper.countUnmastered(studentId));

        return stats;
    }


    /**
     * Get admin dashboard statistics
     */
    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalAdmins", userMapper.countByRole("ADMIN"));
        stats.put("totalTeachers", userMapper.countByRole("TEACHER"));
        stats.put("totalStudents", userMapper.countByRole("STUDENT"));
        stats.put("totalQuestions", questionMapper.selectCount(null));
        stats.put("totalPapers", examPaperMapper.selectCount(null));

        return stats;
    }

    /**
     * Get most missed questions for a teacher
     */
    public List<Map<String, Object>> getMostMissedQuestions(Long teacherId, int limit) {
        return answerRecordMapper.getMostMissedQuestions(teacherId, limit);
    }

    /**
     * Get recent submissions for a teacher
     */
    public List<Map<String, Object>> getRecentSubmissions(Long teacherId, int limit) {
        return answerRecordMapper.getRecentSubmissions(teacherId, limit);
    }
}
