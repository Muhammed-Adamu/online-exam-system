package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.ExamPaper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * Exam Paper Mapper
 */
public interface ExamPaperMapper extends BaseMapper<ExamPaper> {

    @Select("SELECT p.*, u.real_name AS creator_name, " +
            "(SELECT COUNT(*) FROM paper_questions pq WHERE pq.paper_id = p.id) AS question_count " +
            "FROM exam_papers p LEFT JOIN users u ON p.created_by = u.id " +
            "ORDER BY p.created_at DESC")
    List<ExamPaper> findAllWithDetails();

    @Select("SELECT p.*, u.real_name AS creator_name, " +
            "(SELECT COUNT(*) FROM paper_questions pq WHERE pq.paper_id = p.id) AS question_count " +
            "FROM exam_papers p LEFT JOIN users u ON p.created_by = u.id " +
            "WHERE p.status = 'PUBLISHED' ORDER BY p.created_at DESC")
    List<ExamPaper> findPublishedWithDetails();

    @Select("SELECT p.*, u.real_name AS creator_name, " +
            "(SELECT COUNT(*) FROM paper_questions pq WHERE pq.paper_id = p.id) AS question_count " +
            "FROM exam_papers p LEFT JOIN users u ON p.created_by = u.id " +
            "WHERE p.created_by = #{teacherId} ORDER BY p.created_at DESC")
    List<ExamPaper> findByCreatorWithDetails(@Param("teacherId") Long teacherId);

    @Select("SELECT p.*, u.real_name AS creator_name, " +
            "(SELECT COUNT(*) FROM paper_questions pq WHERE pq.paper_id = p.id) AS question_count, " +
            "(SELECT COUNT(*) FROM exam_records er WHERE er.paper_id = p.id) AS attempt_count " +
            "FROM exam_papers p LEFT JOIN users u ON p.created_by = u.id " +
            "WHERE p.id = #{id}")
    ExamPaper findByIdWithDetails(@Param("id") Long id);

    @Update("UPDATE exam_papers SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
