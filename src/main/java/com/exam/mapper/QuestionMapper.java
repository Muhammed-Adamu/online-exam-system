package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.Question;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * Question Mapper
 */
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT * FROM questions WHERE question_type = #{questionType} ORDER BY created_at DESC")
    List<Question> findByType(@Param("questionType") String questionType);

    @Select("SELECT * FROM questions WHERE category = #{category} ORDER BY created_at DESC")
    List<Question> findByCategory(@Param("category") String category);

    @Select("SELECT * FROM questions WHERE difficulty = #{difficulty} ORDER BY created_at DESC")
    List<Question> findByDifficulty(@Param("difficulty") String difficulty);

    @Select("SELECT * FROM questions WHERE created_by = #{teacherId} ORDER BY created_at DESC")
    List<Question> findByCreator(@Param("teacherId") Long teacherId);

    @Select("<script>" +
            "SELECT * FROM questions WHERE 1=1" +
            "<if test='type != null and type != \"\"'> AND question_type = #{type}</if>" +
            "<if test='category != null and category != \"\"'> AND category = #{category}</if>" +
            "<if test='difficulty != null and difficulty != \"\"'> AND difficulty = #{difficulty}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND content LIKE CONCAT('%', #{keyword}, '%')</if>" +
            " ORDER BY created_at DESC" +
            "</script>")
    List<Question> searchQuestions(@Param("type") String type,
                                   @Param("category") String category,
                                   @Param("difficulty") String difficulty,
                                   @Param("keyword") String keyword);

    @Select("SELECT DISTINCT category FROM questions ORDER BY category")
    List<String> findAllCategories();

    @Select("SELECT COUNT(*) FROM questions WHERE question_type = #{type}")
    Long countByType(@Param("type") String type);

    @Select("SELECT COUNT(*) FROM questions WHERE difficulty = #{difficulty}")
    Long countByDifficulty(@Param("difficulty") String difficulty);

    /**
     * Get random questions by type and difficulty for rule-based test assembly
     */
    @Select("SELECT * FROM questions WHERE question_type = #{type} AND difficulty = #{difficulty} ORDER BY RAND() LIMIT #{limit}")
    List<Question> selectRandomQuestions(@Param("type") String type,
                                          @Param("difficulty") String difficulty,
                                          @Param("limit") Integer limit);
}
