package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.AnswerRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import java.util.Map;

/**
 * Answer Record Mapper
 */
public interface AnswerRecordMapper extends BaseMapper<AnswerRecord> {

    @Select("SELECT ar.*, q.question_type, q.content, q.option_a, q.option_b, q.option_c, q.option_d, " +
            "q.correct_answer, q.explanation, q.score, q.category, q.difficulty " +
            "FROM answer_records ar " +
            "LEFT JOIN questions q ON ar.question_id = q.id " +
            "WHERE ar.exam_record_id = #{examRecordId} ORDER BY ar.id ASC")
    List<AnswerRecord> findByExamRecordId(@Param("examRecordId") Long examRecordId);

    @Select("SELECT * FROM answer_records WHERE exam_record_id = #{examRecordId} AND question_id = #{questionId}")
    AnswerRecord findByExamAndQuestion(@Param("examRecordId") Long examRecordId,
                                        @Param("questionId") Long questionId);

    @Update("UPDATE answer_records SET student_answer = #{answer} WHERE id = #{id}")
    int updateAnswer(@Param("id") Long id, @Param("answer") String answer);

    @Update("UPDATE answer_records SET is_correct = #{isCorrect}, score_earned = #{scoreEarned} WHERE id = #{id}")
    int gradeAnswer(@Param("id") Long id, @Param("isCorrect") Integer isCorrect,
                    @Param("scoreEarned") java.math.BigDecimal scoreEarned);

    /**
     * Question correct rate
     */
    @Select("SELECT " +
            "ar.question_id, q.content AS question_content, " +
            "COUNT(*) AS total_answers, " +
            "SUM(CASE WHEN ar.is_correct = 1 THEN 1 ELSE 0 END) AS correct_count, " +
            "ROUND(SUM(CASE WHEN ar.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS correct_rate " +
            "FROM answer_records ar " +
            "LEFT JOIN questions q ON ar.question_id = q.id " +
            "WHERE ar.exam_record_id IN (SELECT id FROM exam_records WHERE paper_id = #{paperId}) " +
            "AND ar.is_correct IS NOT NULL " +
            "GROUP BY ar.question_id, q.content ORDER BY correct_rate ASC")
    List<QuestionCorrectRate> getQuestionCorrectRates(@Param("paperId") Long paperId);

    /**
     * Most missed questions across all papers for a teacher
     */
    @Select("SELECT " +
            "ar.question_id, q.content AS question_content, q.category, q.difficulty, " +
            "COUNT(*) AS total_answers, " +
            "SUM(CASE WHEN ar.is_correct = 1 THEN 1 ELSE 0 END) AS correct_count, " +
            "ROUND(SUM(CASE WHEN ar.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS correct_rate " +
            "FROM answer_records ar " +
            "LEFT JOIN questions q ON ar.question_id = q.id " +
            "WHERE ar.exam_record_id IN (SELECT er.id FROM exam_records er LEFT JOIN exam_papers p ON er.paper_id = p.id WHERE p.created_by = #{teacherId}) " +
            "AND ar.is_correct IS NOT NULL " +
            "GROUP BY ar.question_id, q.content, q.category, q.difficulty " +
            "ORDER BY correct_rate ASC LIMIT #{limit}")
    List<Map<String, Object>> getMostMissedQuestions(@Param("teacherId") Long teacherId, @Param("limit") int limit);

    /**
     * Recent submissions for a teacher
     */
    @Select("SELECT er.id, er.student_id, er.paper_id, er.status, er.total_score, er.submit_time, " +
            "u.real_name AS student_name, p.title AS paper_title " +
            "FROM exam_records er " +
            "LEFT JOIN users u ON er.student_id = u.id " +
            "LEFT JOIN exam_papers p ON er.paper_id = p.id " +
            "WHERE p.created_by = #{teacherId} AND er.status IN ('GRADED', 'SUBMITTED') " +
            "ORDER BY er.submit_time DESC LIMIT #{limit}")
    List<Map<String, Object>> getRecentSubmissions(@Param("teacherId") Long teacherId, @Param("limit") int limit);
}
