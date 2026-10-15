package com.exam.controller;

import com.exam.entity.ExamPaper;
import com.exam.entity.ExamRecord;
import com.exam.entity.PaperQuestion;
import com.exam.entity.Question;
import com.exam.entity.User;
import com.exam.service.*;
import com.exam.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Teacher Controller - Question bank, exam paper, score analysis
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private ExamService examService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Teacher Dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        model.addAttribute("stats", statisticsService.getTeacherDashboardStats(loginUser.getId()));
        model.addAttribute("questionStats", questionService.getQuestionStats());
        model.addAttribute("papers", examPaperService.getPapersByCreator(loginUser.getId()));
        model.addAttribute("recentSubmissions", statisticsService.getRecentSubmissions(loginUser.getId(), 5));
        model.addAttribute("missedQuestions", statisticsService.getMostMissedQuestions(loginUser.getId(), 5));
        model.addAttribute("pageTitle", "Teacher Dashboard");
        return "teacher/dashboard";
    }

    /**
     * Question Analytics Page
     */
    @GetMapping("/question-analytics")
    public String questionAnalytics(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        model.addAttribute("missedQuestions", statisticsService.getMostMissedQuestions(loginUser.getId(), 30));
        model.addAttribute("pageTitle", "Question Analytics");
        return "teacher/question-analytics";
    }

    // ============================================
    // Question Bank Management
    // ============================================

    /**
     * Question Bank List (with pagination)
     */
    @GetMapping("/questions")
    public String questionBank(@RequestParam(required = false) String type,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String difficulty,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");

        // Use paginated search with filters
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Question> questionPage;
        if (type != null || category != null || difficulty != null || keyword != null) {
            questionPage = questionService.getPageWithFilters(page, size, type, category, difficulty, keyword);
        } else {
            questionPage = questionService.getPage(page, size);
        }

        model.addAttribute("questionPage", questionPage);
        model.addAttribute("questions", questionPage.getRecords());
        model.addAttribute("categories", questionService.getAllCategories());
        model.addAttribute("pageTitle", "Question Bank");
        // Preserve filter params for pagination links
        model.addAttribute("currentType", type);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentDifficulty", difficulty);
        model.addAttribute("currentKeyword", keyword);
        return "teacher/question-bank";
    }

    /**
     * Create Question Form
     */
    @GetMapping("/questions/create")
    public String createQuestionForm(Model model) {
        model.addAttribute("question", new Question());
        model.addAttribute("categories", questionService.getAllCategories());
        model.addAttribute("pageTitle", "Add Question");
        return "teacher/question-form";
    }

    /**
     * Create Question
     */
    @PostMapping("/questions/create")
    public String createQuestion(@ModelAttribute Question question, HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("loginUser");
        question.setCreatedBy(loginUser.getId());
        questionService.createQuestion(question);
        redirectAttributes.addFlashAttribute("message", "Question created successfully.");
        return "redirect:/teacher/questions";
    }

    /**
     * Edit Question Form
     */
    @GetMapping("/questions/edit/{id}")
    public String editQuestionForm(@PathVariable Long id, Model model) {
        model.addAttribute("question", questionService.getById(id));
        model.addAttribute("categories", questionService.getAllCategories());
        model.addAttribute("pageTitle", "Edit Question");
        return "teacher/question-form";
    }

    /**
     * Update Question
     */
    @PostMapping("/questions/edit/{id}")
    public String updateQuestion(@PathVariable Long id, @ModelAttribute Question question,
                                  RedirectAttributes redirectAttributes) {
        question.setId(id);
        questionService.updateQuestion(question);
        redirectAttributes.addFlashAttribute("message", "Question updated successfully.");
        return "redirect:/teacher/questions";
    }

    /**
     * Delete Question
     */
    @GetMapping("/questions/delete/{id}")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        questionService.deleteQuestion(id);
        redirectAttributes.addFlashAttribute("message", "Question deleted.");
        return "redirect:/teacher/questions";
    }

    // ============================================
    // Exam Paper Management
    // ============================================

    /**
     * Exam Paper List
     */
    @GetMapping("/papers")
    public String paperList(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        List<ExamPaper> papers = examPaperService.getPapersByCreator(loginUser.getId());
        model.addAttribute("papers", papers);
        model.addAttribute("pageTitle", "Exam Papers");
        return "teacher/exam-paper";
    }

    /**
     * Create Paper Form
     */
    @GetMapping("/papers/create")
    public String createPaperForm(Model model) {
        model.addAttribute("paper", new ExamPaper());
        model.addAttribute("pageTitle", "Create Exam Paper");
        return "teacher/paper-form";
    }

    /**
     * Create Paper
     */
    @PostMapping("/papers/create")
    public String createPaper(@ModelAttribute ExamPaper paper, HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("loginUser");
        paper.setCreatedBy(loginUser.getId());
        examPaperService.createPaper(paper);
        redirectAttributes.addFlashAttribute("message", "Paper created. Now add questions.");
        return "redirect:/teacher/papers/questions/" + paper.getId();
    }

    /**
     * Edit Paper Form
     */
    @GetMapping("/papers/edit/{id}")
    public String editPaperForm(@PathVariable Long id, Model model) {
        model.addAttribute("paper", examPaperService.getById(id));
        model.addAttribute("pageTitle", "Edit Exam Paper");
        return "teacher/paper-form";
    }

    /**
     * Update Paper
     */
    @PostMapping("/papers/edit/{id}")
    public String updatePaper(@PathVariable Long id, @ModelAttribute ExamPaper paper,
                               RedirectAttributes redirectAttributes) {
        paper.setId(id);
        examPaperService.updatePaper(paper);
        redirectAttributes.addFlashAttribute("message", "Paper updated.");
        return "redirect:/teacher/papers";
    }

    /**
     * Delete Paper
     */
    @GetMapping("/papers/delete/{id}")
    public String deletePaper(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        examPaperService.deletePaper(id);
        redirectAttributes.addFlashAttribute("message", "Paper deleted.");
        return "redirect:/teacher/papers";
    }

    /**
     * Manage Paper Questions
     */
    @GetMapping("/papers/questions/{paperId}")
    public String manageQuestions(@PathVariable Long paperId, Model model) {
        ExamPaper paper = examPaperService.getById(paperId);
        List<PaperQuestion> paperQuestions = examPaperService.getPaperQuestions(paperId);
        List<Question> allQuestions = questionService.getByCreator(paper.getCreatedBy());

        // Filter out already added questions
        Set<Long> addedIds = new HashSet<>();
        for (PaperQuestion pq : paperQuestions) {
            addedIds.add(pq.getQuestionId());
        }
        List<Question> availableQuestions = new ArrayList<>();
        for (Question q : allQuestions) {
            if (!addedIds.contains(q.getId())) {
                availableQuestions.add(q);
            }
        }

        model.addAttribute("paper", paper);
        model.addAttribute("paperQuestions", paperQuestions);
        model.addAttribute("availableQuestions", availableQuestions);
        model.addAttribute("pageTitle", "Manage Paper Questions");
        return "teacher/paper-questions";
    }

    /**
     * Add Question to Paper
     */
    @PostMapping("/papers/questions/{paperId}/add")
    public String addQuestion(@PathVariable Long paperId,
                               @RequestParam Long questionId,
                               @RequestParam(defaultValue = "5") BigDecimal score,
                               RedirectAttributes redirectAttributes) {
        try {
            examPaperService.addQuestionToPaper(paperId, questionId, score);
            redirectAttributes.addFlashAttribute("message", "Question added to paper.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/papers/questions/" + paperId;
    }

    /**
     * Remove Question from Paper
     */
    @GetMapping("/papers/questions/{paperId}/remove/{questionId}")
    public String removeQuestion(@PathVariable Long paperId,
                                  @PathVariable Long questionId,
                                  RedirectAttributes redirectAttributes) {
        examPaperService.removeQuestionFromPaper(paperId, questionId);
        redirectAttributes.addFlashAttribute("message", "Question removed from paper.");
        return "redirect:/teacher/papers/questions/" + paperId;
    }

    /**
     * Publish Paper
     */
    @GetMapping("/papers/publish/{id}")
    public String publishPaper(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        examPaperService.publishPaper(id);
        redirectAttributes.addFlashAttribute("message", "Paper published successfully.");
        return "redirect:/teacher/papers";
    }

    /**
     * Close Paper
     */
    @GetMapping("/papers/close/{id}")
    public String closePaper(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        examPaperService.closePaper(id);
        redirectAttributes.addFlashAttribute("message", "Paper closed.");
        return "redirect:/teacher/papers";
    }

    /**
     * Rule-based Test Assembly Page
     */
    @GetMapping("/papers/assemble/{paperId}")
    public String assemblePage(@PathVariable Long paperId, Model model) {
        ExamPaper paper = examPaperService.getById(paperId);
        model.addAttribute("paper", paper);
        model.addAttribute("categories", questionService.getAllCategories());
        model.addAttribute("pageTitle", "Rule-Based Test Assembly");
        return "teacher/test-assembly";
    }

    /**
     * Execute Rule-based Test Assembly
     */
    @PostMapping("/papers/assemble/{paperId}")
    @ResponseBody
    public Map<String, Object> executeAssembly(@PathVariable Long paperId,
                                                @RequestBody List<Map<String, Object>> rules) {
        return examPaperService.assembleByRules(paperId, rules);
    }

    /**
     * Import Questions Page
     */
    @GetMapping("/questions/import")
    public String importPage(Model model) {
        model.addAttribute("pageTitle", "Import Questions");
        return "teacher/question-import";
    }

    /**
     * Handle Excel File Upload for Question Import
     */
    @PostMapping("/questions/import")
    public String importQuestions(@RequestParam("file") MultipartFile file,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/teacher/questions/import";
        }

        try {
            List<Question> questions = ExcelUtil.parseQuestions(file.getInputStream(), loginUser.getId());
            int count = questionService.batchCreate(questions);
            redirectAttributes.addFlashAttribute("message",
                String.format("Successfully imported %d questions.", count));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }

        return "redirect:/teacher/questions";
    }

    /**
     * Export Exam Scores to Excel
     */
    @GetMapping("/analysis/export/{paperId}")
    @ResponseBody
    public ResponseEntity<byte[]> exportScores(@PathVariable Long paperId) {
        ExamPaper paper = examPaperService.getById(paperId);
        List<ExamRecord> records = examService.getPaperRecords(paperId);
        String filename = (paper != null ? paper.getTitle() : "exam-scores") + ".xlsx";

        try {
            byte[] excelData = ExcelUtil.exportExamRecords(records,
                paper != null ? paper.getTitle() : "Exam Scores");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export scores: " + e.getMessage());
        }
    }

    // ============================================
    // Score Analysis
    // ============================================

    /**
     * Score Analysis Page
     */
    @GetMapping("/analysis")
    public String scoreAnalysis(@RequestParam(required = false) Long paperId,
                                 Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        List<ExamPaper> papers = examPaperService.getPapersByCreator(loginUser.getId());
        model.addAttribute("papers", papers);
        model.addAttribute("classes", userService.getAllClassNames());
        model.addAttribute("selectedPaperId", paperId);
        model.addAttribute("pageTitle", "Score Analysis");
        return "teacher/score-analysis";
    }

    /**
     * Paper Statistics Data (AJAX)
     */
    @GetMapping("/analysis/paper/{paperId}")
    @ResponseBody
    public Map<String, Object> getPaperStats(@PathVariable Long paperId) {
        Map<String, Object> result = new HashMap<>();
        result.put("statistics", statisticsService.getPaperStatistics(paperId));
        result.put("correctRates", statisticsService.getQuestionCorrectRates(paperId));
        result.put("records", examService.getPaperRecords(paperId));
        return result;
    }

    /**
     * Class Analysis Data (AJAX)
     */
    @GetMapping("/analysis/class")
    @ResponseBody
    public List<Map<String, Object>> getClassStats(@RequestParam String className) {
        return statisticsService.getClassStatistics(className);
    }

    // ============================================
    // Notices to Students
    // ============================================

    /**
     * Send a notice to a specific student
     */
    @PostMapping("/notices/send")
    @ResponseBody
    public Map<String, Object> sendNotice(@RequestParam Long studentId,
                                          @RequestParam String message,
                                          HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            result.put("success", false);
            result.put("message", "Not logged in");
            return result;
        }

        try {
            notificationService.sendTeacherNotice(loginUser.getId(), studentId, message);
            result.put("success", true);
            result.put("message", "Notice sent successfully.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to send notice: " + e.getMessage());
        }
        return result;
    }

    /**
     * Get all students (for notice recipient selection)
     */
    @GetMapping("/students")
    @ResponseBody
    public List<Map<String, Object>> getStudents() {
        List<User> students = userService.getByRole("STUDENT");
        List<Map<String, Object>> result = new ArrayList<>();
        for (User s : students) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getRealName());
            map.put("username", s.getUsername());
            map.put("className", s.getClassName());
            result.add(map);
        }
        return result;
    }
}
