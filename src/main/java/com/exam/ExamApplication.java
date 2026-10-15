package com.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Online Exam System - Main Application
 * Topic 5: Online Exam, Question Bank & Intelligent Test Assembly System
 */
@SpringBootApplication
@MapperScan("com.exam.mapper")
public class ExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
        System.out.println("========================================");
        System.out.println("  Online Exam System Started Successfully!");
        System.out.println("  Access: http://localhost:8080/exam");
        System.out.println("========================================");
    }
}
