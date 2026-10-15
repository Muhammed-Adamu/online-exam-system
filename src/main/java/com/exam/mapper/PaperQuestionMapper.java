package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.PaperQuestion;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * Paper Question Mapper
 */
public interface PaperQuestionMapper extends BaseMapper<PaperQuestion> {

    @Select("SELECT pq.*, q.question_type, q.category, q.difficulty, q.content, " +
            "q.option_a, q.option_b, q.option_c, q.option_d, q.correct_answer, q.explanation " +
            "FROM paper_questions pq LEFT JOIN questions q ON pq.question_id = q.id " +
            "WHERE pq.paper_id = #{paperId} ORDER BY pq.question_order ASC")
    List<PaperQuestion> findByPaperId(@Param("paperId") Long paperId);

    @Select("SELECT COUNT(*) FROM paper_questions WHERE paper_id = #{paperId}")
    Integer countByPaperId(@Param("paperId") Long paperId);

    @Delete("DELETE FROM paper_questions WHERE paper_id = #{paperId}")
    int deleteByPaperId(@Param("paperId") Long paperId);

    @Select("SELECT pq.* FROM paper_questions pq WHERE pq.paper_id = #{paperId} AND pq.question_id = #{questionId}")
    PaperQuestion findByPaperAndQuestion(@Param("paperId") Long paperId, @Param("questionId") Long questionId);
}
