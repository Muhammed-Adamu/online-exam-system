package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.ExamRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import java.util.Map;

/**
 * Exam Record Mapper
 */
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    @Select("SELECT er.*, u.real_name AS student_name, p.title AS paper_title, " +
            "(SELECT COUNT(*) FROM paper_questions pq WHERE pq.paper_id = er.paper_id) AS question_count " +
            "FROM exam_records er " +
            "LEFT JOIN users u ON er.student_id = u.id " +
            "LEFT JOIN exam_papers p ON er.paper_id = p.id " +
            "WHERE er.student_id = #{studentId} ORDER BY er.created_at DESC")
    List<ExamRecord> findByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT er.*, u.real_name AS student_name, p.title AS paper_title, " +
            "(SELECT COUNT(*) FROM paper_questions pq WHERE pq.paper_id = er.paper_id) AS question_count " +
            "FROM exam_records er " +
            "LEFT JOIN users u ON er.student_id = u.id " +
            "LEFT JOIN exam_papers p ON er.paper_id = p.id " +
            "WHERE er.paper_id = #{paperId} ORDER BY er.total_score DESC")
    List<ExamRecord> findByPaperId(@Param("paperId") Long paperId);

    @Select("SELECT er.*, u.real_name AS student_name, p.title AS paper_title " +
            "FROM exam_records er " +
            "LEFT JOIN users u ON er.student_id = u.id " +
            "LEFT JOIN exam_papers p ON er.paper_id = p.id " +
            "WHERE er.id = #{id}")
    ExamRecord findByIdWithDetails(@Param("id") Long id);

    @Select("SELECT * FROM exam_records WHERE student_id = #{studentId} AND paper_id = #{paperId}")
    ExamRecord findByStudentAndPaper(@Param("studentId") Long studentId, @Param("paperId") Long paperId);

    @Update("UPDATE exam_records SET status = #{status}, submit_time = NOW(), " +
            "time_spent_seconds = #{timeSpent} WHERE id = #{id}")
    int submitExam(@Param("id") Long id, @Param("status") String status,
                    @Param("timeSpent") Integer timeSpent);

    /**
     * Score statistics for a paper
     */
    @Select("SELECT " +
            "COUNT(*) AS total_attempts, " +
            "AVG(total_score) AS average_score, " +
            "MAX(total_score) AS highest_score, " +
            "MIN(total_score) AS lowest_score " +
            "FROM exam_records WHERE paper_id = #{paperId} AND status IN ('SUBMITTED', 'GRADED')")
    ScoreStatistics getPaperStatistics(@Param("paperId") Long paperId);

    /**
     * Score distribution
     */
    @Select("SELECT " +
            "SUM(CASE WHEN total_score >= 90 THEN 1 ELSE 0 END) AS excellent, " +
            "SUM(CASE WHEN total_score >= 80 AND total_score < 90 THEN 1 ELSE 0 END) AS good, " +
            "SUM(CASE WHEN total_score >= 70 AND total_score < 80 THEN 1 ELSE 0 END) AS average, " +
            "SUM(CASE WHEN total_score >= 60 AND total_score < 70 THEN 1 ELSE 0 END) AS pass, " +
            "SUM(CASE WHEN total_score < 60 THEN 1 ELSE 0 END) AS fail " +
            "FROM exam_records WHERE paper_id = #{paperId} AND status IN ('SUBMITTED', 'GRADED')")
    ScoreDistribution getScoreDistribution(@Param("paperId") Long paperId);

    /**
     * Student score trend over time (last 10 exams)
     */
    @Select("SELECT er.total_score, er.submit_time, p.title AS paper_title " +
            "FROM exam_records er " +
            "LEFT JOIN exam_papers p ON er.paper_id = p.id " +
            "WHERE er.student_id = #{studentId} AND er.status IN ('GRADED', 'SUBMITTED') " +
            "AND er.total_score IS NOT NULL " +
            "ORDER BY er.submit_time ASC LIMIT #{limit}")
    List<Map<String, Object>> getScoreTrend(@Param("studentId") Long studentId, @Param("limit") int limit);
}
