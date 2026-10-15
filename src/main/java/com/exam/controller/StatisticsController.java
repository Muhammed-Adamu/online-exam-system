package com.exam.controller;

import com.exam.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Statistics Controller - Chart data API endpoints
 */
@Controller
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Get paper score distribution for charts
     */
    @GetMapping("/paper/{paperId}")
    @ResponseBody
    public Map<String, Object> getPaperStats(@PathVariable Long paperId) {
        return statisticsService.getPaperStatistics(paperId);
    }

    /**
     * Get question correct rates
     */
    @GetMapping("/correct-rates/{paperId}")
    @ResponseBody
    public Object getCorrectRates(@PathVariable Long paperId) {
        return statisticsService.getQuestionCorrectRates(paperId);
    }

    /**
     * Get class statistics
     */
    @GetMapping("/class")
    @ResponseBody
    public Object getClassStats(@RequestParam String className) {
        return statisticsService.getClassStatistics(className);
    }
}
