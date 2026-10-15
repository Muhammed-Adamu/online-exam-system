package com.exam.controller;

import com.exam.entity.*;
import com.exam.mapper.ExamRecordMapper;
import com.exam.mapper.WrongAnswerMapper;
import com.exam.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student Controller - Exam taking, score viewing, wrong answers
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private ExamService examService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private WrongAnswerMapper wrongAnswerMapper;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    /**
     * Student Dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");

        model.addAttribute("stats", statisticsService.getStudentDashboardStats(loginUser.getId()));
        model.addAttribute("publishedPapers", examPaperService.getPublishedPapers());
        model.addAttribute("recentRecords", examService.getStudentRecords(loginUser.getId()));
        model.addAttribute("allRecords", examService.getStudentRecords(loginUser.getId()));
        model.addAttribute("wrongCount", wrongAnswerMapper.countUnmastered(loginUser.getId()));
        model.addAttribute("scoreTrend", examRecordMapper.getScoreTrend(loginUser.getId(), 10));
        model.addAttribute("pageTitle", "Student Dashboard");
        return "student/dashboard";
    }

    /**
     * Available Exams List
     */
    @GetMapping("/exams")
    public String examList(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        List<ExamPaper> publishedPapers = examPaperService.getPublishedPapers();
        List<ExamRecord> myRecords = examService.getStudentRecords(loginUser.getId());
        List<ExamPaper> availablePapers = examPaperService.getPublishedPapers();

        // Filter out already taken papers
        Map<Long, ExamRecord> recordMap = new HashMap<>();
        for (ExamRecord record : myRecords) {
            if (!"IN_PROGRESS".equals(record.getStatus())) {
                recordMap.put(record.getPaperId(), record);
            }
        }

        model.addAttribute("papers", availablePapers);
        model.addAttribute("records", myRecords);
        model.addAttribute("recordMap", recordMap);
        model.addAttribute("pageTitle", "Available Exams");
        return "student/exam-list";
    }

    /**
     * Start / Enter Exam
     */
    @GetMapping("/exam/start/{paperId}")
    public String startExam(@PathVariable Long paperId, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("loginUser");

        try {
            ExamRecord record = examService.startExam(loginUser.getId(), paperId);
            return "redirect:/student/exam/take/" + record.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/student/exams";
        }
    }

    /**
     * Take Exam Page
     */
    @GetMapping("/exam/take/{recordId}")
    public String takeExam(@PathVariable Long recordId, Model model, HttpSession session) {
        ExamRecord record = examService.getRecordById(recordId);
        User loginUser = (User) session.getAttribute("loginUser");

        // Security check
        if (record == null || !record.getStudentId().equals(loginUser.getId())) {
            return "redirect:/student/exams";
        }

        if (!"IN_PROGRESS".equals(record.getStatus())) {
            return "redirect:/student/exam/result/" + recordId;
        }

        ExamPaper paper = examPaperService.getById(record.getPaperId());
        List<PaperQuestion> paperQuestions = examPaperService.getPaperQuestions(record.getPaperId());
        List<AnswerRecord> answers = examService.getExamAnswers(recordId);

        // Build answer map
        Map<Long, String> answerMap = new HashMap<>();
        for (AnswerRecord ar : answers) {
            answerMap.put(ar.getQuestionId(), ar.getStudentAnswer());
        }

        model.addAttribute("record", record);
        model.addAttribute("paper", paper);
        model.addAttribute("paperQuestions", paperQuestions);
        model.addAttribute("answerMap", answerMap);
        model.addAttribute("pageTitle", paper.getTitle());
        return "student/take-exam";
    }

    /**
     * Save answer via AJAX
     */
    @PostMapping("/exam/save-answer")
    @ResponseBody
    public Map<String, Object> saveAnswer(@RequestParam Long recordId,
                                           @RequestParam Long questionId,
                                           @RequestParam(required = false) String answer) {
        Map<String, Object> result = new HashMap<>();
        boolean success = examService.saveAnswer(recordId, questionId, answer);
        result.put("success", success);
        return result;
    }

    /**
     * Submit Exam
     */
    @PostMapping("/exam/submit")
    @ResponseBody
    public Map<String, Object> submitExam(@RequestParam Long recordId,
                                           @RequestParam Integer timeSpent) {
        Map<String, Object> result = new HashMap<>();
        try {
            ExamRecord record = examService.submitExam(recordId, timeSpent);
            result.put("success", true);
            result.put("score", record.getTotalScore());
            result.put("redirectUrl", "/exam/student/exam/result/" + recordId);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * Exam Result Page
     */
    @GetMapping("/exam/result/{recordId}")
    public String examResult(@PathVariable Long recordId, Model model, HttpSession session) {
        ExamRecord record = examService.getRecordById(recordId);
        User loginUser = (User) session.getAttribute("loginUser");

        if (record == null || !record.getStudentId().equals(loginUser.getId())) {
            return "redirect:/student/exams";
        }

        ExamPaper paper = examPaperService.getById(record.getPaperId());
        List<AnswerRecord> answers = examService.getExamAnswers(recordId);

        model.addAttribute("record", record);
        model.addAttribute("paper", paper);
        model.addAttribute("answers", answers);
        model.addAttribute("pageTitle", "Exam Result");
        return "student/exam-result";
    }

    /**
     * My Scores Page
     */
    @GetMapping("/scores")
    public String myScores(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        List<ExamRecord> records = examService.getStudentRecords(loginUser.getId());

        model.addAttribute("records", records);
        model.addAttribute("pageTitle", "My Scores");
        return "student/my-scores";
    }

    /**
     * Wrong Answer Notebook
     */
    @GetMapping("/wrong-answers")
    public String wrongAnswers(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        List<WrongAnswer> wrongAnswers = wrongAnswerMapper.findByStudentId(loginUser.getId());

        // Populate question details for each wrong answer
        for (WrongAnswer wa : wrongAnswers) {
            wa.setQuestion(questionService.getById(wa.getQuestionId()));
        }

        model.addAttribute("wrongAnswers", wrongAnswers);
        model.addAttribute("pageTitle", "Wrong Answer Notebook");
        return "student/wrong-answers";
    }

    /**
     * Mark wrong answer as mastered
     */
    @PostMapping("/wrong-answers/master/{id}")
    @ResponseBody
    public Map<String, Object> markMastered(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        wrongAnswerMapper.markAsMastered(id);
        result.put("success", true);
        return result;
    }
}
