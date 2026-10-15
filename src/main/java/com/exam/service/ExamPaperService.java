package com.exam.service;

import com.exam.entity.ExamPaper;
import com.exam.entity.PaperQuestion;
import com.exam.entity.Question;
import com.exam.mapper.ExamPaperMapper;
import com.exam.mapper.PaperQuestionMapper;
import com.exam.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Exam Paper Service
 */
@Service
public class ExamPaperService {

    @Autowired
    private ExamPaperMapper examPaperMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * Get all exam papers with details
     */
    public List<ExamPaper> getAllPapers() {
        return examPaperMapper.findAllWithDetails();
    }

    /**
     * Get published papers (for students)
     */
    public List<ExamPaper> getPublishedPapers() {
        return examPaperMapper.findPublishedWithDetails();
    }

    /**
     * Get papers by creator
     */
    public List<ExamPaper> getPapersByCreator(Long teacherId) {
        return examPaperMapper.findByCreatorWithDetails(teacherId);
    }

    /**
     * Get paper by ID with details
     */
    public ExamPaper getById(Long id) {
        return examPaperMapper.findByIdWithDetails(id);
    }

    /**
     * Create exam paper
     */
    @Transactional
    public boolean createPaper(ExamPaper paper) {
        paper.setStatus("DRAFT");
        return examPaperMapper.insert(paper) > 0;
    }

    /**
     * Update exam paper
     */
    public boolean updatePaper(ExamPaper paper) {
        return examPaperMapper.updateById(paper) > 0;
    }

    /**
     * Delete exam paper
     */
    @Transactional
    public boolean deletePaper(Long id) {
        paperQuestionMapper.deleteByPaperId(id);
        return examPaperMapper.deleteById(id) > 0;
    }

    /**
     * Publish exam paper
     */
    public boolean publishPaper(Long id) {
        return examPaperMapper.updateStatus(id, "PUBLISHED") > 0;
    }

    /**
     * Close exam paper
     */
    public boolean closePaper(Long id) {
        return examPaperMapper.updateStatus(id, "CLOSED") > 0;
    }

    /**
     * Get questions in a paper (with full question details populated)
     */
    public List<PaperQuestion> getPaperQuestions(Long paperId) {
        List<PaperQuestion> paperQuestions = paperQuestionMapper.findByPaperId(paperId);
        // Populate the nested Question object for each PaperQuestion
        for (PaperQuestion pq : paperQuestions) {
            Question question = questionMapper.selectById(pq.getQuestionId());
            pq.setQuestion(question);
        }
        return paperQuestions;
    }

    /**
     * Add question to paper
     */
    public boolean addQuestionToPaper(Long paperId, Long questionId, BigDecimal score) {
        PaperQuestion existing = paperQuestionMapper.findByPaperAndQuestion(paperId, questionId);
        if (existing != null) {
            throw new RuntimeException("Question already exists in this paper");
        }

        Integer count = paperQuestionMapper.countByPaperId(paperId);
        PaperQuestion pq = new PaperQuestion();
        pq.setPaperId(paperId);
        pq.setQuestionId(questionId);
        pq.setQuestionOrder(count != null ? count + 1 : 1);
        pq.setScore(score);
        return paperQuestionMapper.insert(pq) > 0;
    }

    /**
     * Remove question from paper
     */
    public boolean removeQuestionFromPaper(Long paperId, Long questionId) {
        PaperQuestion pq = paperQuestionMapper.findByPaperAndQuestion(paperId, questionId);
        if (pq != null) {
            return paperQuestionMapper.deleteById(pq.getId()) > 0;
        }
        return false;
    }

    /**
     * Rule-based test assembly
     * @param paperId Paper ID
     * @param rules List of rules: Map with keys: type, difficulty, count, score
     */
    @Transactional
    public Map<String, Object> assembleByRules(Long paperId, List<Map<String, Object>> rules) {
        // Clear existing questions
        paperQuestionMapper.deleteByPaperId(paperId);

        Map<String, Object> result = new HashMap<>();
        int totalAdded = 0;
        BigDecimal totalScore = BigDecimal.ZERO;
        List<String> warnings = new ArrayList<>();
        int order = 1;

        for (Map<String, Object> rule : rules) {
            String type = (String) rule.get("type");
            String difficulty = (String) rule.get("difficulty");
            int count = Integer.parseInt(rule.get("count").toString());
            BigDecimal score = new BigDecimal(rule.get("score").toString());

            List<Question> questions = questionMapper.selectRandomQuestions(type, difficulty, count);

            if (questions.size() < count) {
                warnings.add(String.format(
                    "Only %d questions available for %s (%s), requested %d",
                    questions.size(), type, difficulty, count
                ));
            }

            for (Question q : questions) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(paperId);
                pq.setQuestionId(q.getId());
                pq.setQuestionOrder(order++);
                pq.setScore(score);
                paperQuestionMapper.insert(pq);
                totalAdded++;
                totalScore = totalScore.add(score);
            }
        }

        // Update paper total score
        ExamPaper paper = examPaperMapper.selectById(paperId);
        paper.setTotalScore(totalScore);
        paper.setAssemblyType("RULE_BASED");
        examPaperMapper.updateById(paper);

        result.put("totalAdded", totalAdded);
        result.put("totalScore", totalScore);
        result.put("warnings", warnings);
        return result;
    }
}
