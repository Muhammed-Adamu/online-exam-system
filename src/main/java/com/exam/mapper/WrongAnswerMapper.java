package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.WrongAnswer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * Wrong Answer Mapper
 */
public interface WrongAnswerMapper extends BaseMapper<WrongAnswer> {

    @Select("SELECT wa.*, q.question_type, q.content, q.option_a, q.option_b, q.option_c, q.option_d, " +
            "q.correct_answer, q.explanation, q.category, q.difficulty " +
            "FROM wrong_answer_notebook wa " +
            "LEFT JOIN questions q ON wa.question_id = q.id " +
            "WHERE wa.student_id = #{studentId} ORDER BY wa.last_wrong_time DESC")
    List<WrongAnswer> findByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT * FROM wrong_answer_notebook WHERE student_id = #{studentId} AND question_id = #{questionId}")
    WrongAnswer findByStudentAndQuestion(@Param("studentId") Long studentId,
                                          @Param("questionId") Long questionId);

    @Update("UPDATE wrong_answer_notebook SET wrong_count = wrong_count + 1, " +
            "wrong_answer = #{wrongAnswer}, last_wrong_time = NOW() " +
            "WHERE student_id = #{studentId} AND question_id = #{questionId}")
    int incrementWrongCount(@Param("studentId") Long studentId,
                             @Param("questionId") Long questionId,
                             @Param("wrongAnswer") String wrongAnswer);

    @Update("UPDATE wrong_answer_notebook SET mastered = 1 WHERE id = #{id}")
    int markAsMastered(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM wrong_answer_notebook WHERE student_id = #{studentId} AND mastered = 0")
    Integer countUnmastered(@Param("studentId") Long studentId);
}
