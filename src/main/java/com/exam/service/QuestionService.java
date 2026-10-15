package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.Question;
import com.exam.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Question Service - Question bank management
 */
@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * Get question by ID
     */
    public Question getById(Long id) {
        return questionMapper.selectById(id);
    }

    /**
     * Get all questions with pagination
     */
    public Page<Question> getPage(int current, int size) {
        Page<Question> page = new Page<>(current, size);
        return questionMapper.selectPage(page,
                new QueryWrapper<Question>().orderByDesc("created_at"));
    }

    /**
     * Get questions with filters and pagination
     */
    public Page<Question> getPageWithFilters(int current, int size, String type, String category,
                                             String difficulty, String keyword) {
        Page<Question> page = new Page<>(current, size);
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        if (type != null && !type.isEmpty()) wrapper.eq("question_type", type);
        if (category != null && !category.isEmpty()) wrapper.eq("category", category);
        if (difficulty != null && !difficulty.isEmpty()) wrapper.eq("difficulty", difficulty);
        if (keyword != null && !keyword.isEmpty()) wrapper.like("content", keyword);
        wrapper.orderByDesc("created_at");
        return questionMapper.selectPage(page, wrapper);
    }

    /**
     * Get questions by type
     */
    public List<Question> getByType(String type) {
        return questionMapper.findByType(type);
    }

    /**
     * Get questions by category
     */
    public List<Question> getByCategory(String category) {
        return questionMapper.findByCategory(category);
    }

    /**
     * Get questions by creator (teacher)
     */
    public List<Question> getByCreator(Long teacherId) {
        return questionMapper.findByCreator(teacherId);
    }

    /**
     * Search questions with filters
     */
    public List<Question> searchQuestions(String type, String category, String difficulty, String keyword) {
        return questionMapper.searchQuestions(type, category, difficulty, keyword);
    }

    /**
     * Create a new question
     */
    public boolean createQuestion(Question question) {
        return questionMapper.insert(question) > 0;
    }

    /**
     * Update a question
     */
    public boolean updateQuestion(Question question) {
        return questionMapper.updateById(question) > 0;
    }

    /**
     * Delete a question
     */
    public boolean deleteQuestion(Long id) {
        return questionMapper.deleteById(id) > 0;
    }

    /**
     * Batch create questions (for Excel import)
     */
    public int batchCreate(List<Question> questions) {
        int count = 0;
        for (Question q : questions) {
            if (questionMapper.insert(q) > 0) count++;
        }
        return count;
    }

    /**
     * Get all categories
     */
    public List<String> getAllCategories() {
        return questionMapper.findAllCategories();
    }

    /**
     * Get question statistics
     */
    public Map<String, Long> getQuestionStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", questionMapper.selectCount(null));
        stats.put("singleChoice", questionMapper.countByType("SINGLE_CHOICE"));
        stats.put("multipleChoice", questionMapper.countByType("MULTIPLE_CHOICE"));
        stats.put("trueFalse", questionMapper.countByType("TRUE_FALSE"));
        stats.put("fillBlank", questionMapper.countByType("FILL_BLANK"));
        stats.put("easy", questionMapper.countByDifficulty("EASY"));
        stats.put("medium", questionMapper.countByDifficulty("MEDIUM"));
        stats.put("hard", questionMapper.countByDifficulty("HARD"));
        return stats;
    }

    /**
     * Get random questions by type and difficulty for test assembly
     */
    public List<Question> getRandomQuestions(String type, String difficulty, Integer limit) {
        return questionMapper.selectRandomQuestions(type, difficulty, limit);
    }
}
