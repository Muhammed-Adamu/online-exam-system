-- ============================================
-- Online Exam System - Database Schema
-- Topic 5: Online Exam, Question Bank & Intelligent Test Assembly
-- ============================================

CREATE DATABASE IF NOT EXISTS online_exam_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE online_exam_system;

-- ============================================
-- 1. User Table
-- ============================================
DROP TABLE IF EXISTS answer_records;
DROP TABLE IF EXISTS wrong_answer_notebook;
DROP TABLE IF EXISTS exam_records;
DROP TABLE IF EXISTS paper_questions;
DROP TABLE IF EXISTS exam_papers;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
    email VARCHAR(100),
    phone VARCHAR(20),
    class_name VARCHAR(50),
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role),
    INDEX idx_class (class_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. Question Bank Table
-- ============================================
CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_type ENUM('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE', 'FILL_BLANK') NOT NULL,
    category VARCHAR(100) NOT NULL COMMENT 'Subject/Category',
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL DEFAULT 'MEDIUM',
    score DECIMAL(5,2) NOT NULL DEFAULT 5.00,
    content TEXT NOT NULL COMMENT 'Question stem',
    option_a VARCHAR(500) COMMENT 'Option A',
    option_b VARCHAR(500) COMMENT 'Option B',
    option_c VARCHAR(500) COMMENT 'Option C',
    option_d VARCHAR(500) COMMENT 'Option D',
    correct_answer VARCHAR(500) NOT NULL COMMENT 'Correct answer',
    explanation TEXT COMMENT 'Answer explanation',
    created_by BIGINT COMMENT 'Creator (teacher)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (question_type),
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty),
    INDEX idx_creator (created_by),
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. Exam Paper Table
-- ============================================
CREATE TABLE exam_papers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    total_score DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    duration_minutes INT NOT NULL DEFAULT 60 COMMENT 'Exam duration in minutes',
    start_time DATETIME COMMENT 'Exam available from',
    end_time DATETIME COMMENT 'Exam available until',
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED') NOT NULL DEFAULT 'DRAFT',
    assembly_type ENUM('MANUAL', 'RULE_BASED') NOT NULL DEFAULT 'MANUAL',
    created_by BIGINT NOT NULL COMMENT 'Creator (teacher)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_creator (created_by),
    INDEX idx_start_time (start_time),
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. Paper-Question Relationship Table
-- ============================================
CREATE TABLE paper_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paper_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT NOT NULL DEFAULT 0,
    score DECIMAL(5,2) NOT NULL DEFAULT 5.00 COMMENT 'Score for this question in this paper',
    INDEX idx_paper (paper_id),
    INDEX idx_question (question_id),
    FOREIGN KEY (paper_id) REFERENCES exam_papers(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_paper_question (paper_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 5. Exam Record Table (Student taking exam)
-- ============================================
CREATE TABLE exam_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    paper_id BIGINT NOT NULL,
    status ENUM('IN_PROGRESS', 'SUBMITTED', 'GRADED', 'TIMEOUT') NOT NULL DEFAULT 'IN_PROGRESS',
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submit_time DATETIME,
    total_score DECIMAL(5,2),
    time_spent_seconds INT COMMENT 'Actual time spent',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student (student_id),
    INDEX idx_paper (paper_id),
    INDEX idx_status (status),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (paper_id) REFERENCES exam_papers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_paper (student_id, paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 6. Answer Record Table (Individual answers)
-- ============================================
CREATE TABLE answer_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_record_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    student_answer TEXT COMMENT 'Student submitted answer',
    is_correct TINYINT(1) COMMENT 'NULL=not graded, 0=wrong, 1=correct',
    score_earned DECIMAL(5,2) DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exam_record (exam_record_id),
    INDEX idx_question (question_id),
    FOREIGN KEY (exam_record_id) REFERENCES exam_records(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_exam_question (exam_record_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 7. Wrong Answer Notebook Table
-- ============================================
CREATE TABLE wrong_answer_notebook (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    wrong_answer TEXT COMMENT 'What student answered',
    wrong_count INT NOT NULL DEFAULT 1 COMMENT 'Times answered wrong',
    last_wrong_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mastered TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Has student mastered?',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student (student_id),
    INDEX idx_question (question_id),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_question (student_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 8. Notifications Table (Teacher notifications)
-- ============================================
DROP TABLE IF EXISTS notifications;

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL COMMENT 'Teacher who receives this notification',
    message VARCHAR(500) NOT NULL COMMENT 'Notification content',
    type VARCHAR(50) NOT NULL DEFAULT 'EXAM_SUBMITTED' COMMENT 'Notification type',
    reference_id BIGINT COMMENT 'Reference exam_record_id for quick link',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=unread, 1=read',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recipient (recipient_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created (created_at),
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 9. Score Statistics View (Materialized via query)
-- ============================================
-- Statistics are computed via service layer queries
