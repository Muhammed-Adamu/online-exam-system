-- ============================================
-- Online Exam System - Test Data
-- ============================================

USE online_exam_system;

-- ============================================
-- Users (Passwords are BCrypt encoded: "123456")
-- BCrypt for "123456": $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- ============================================
INSERT INTO users (username, password, real_name, role, email, class_name) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'System Admin', 'ADMIN', 'admin@exam.com', NULL),
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Professor Wang', 'TEACHER', 'wang@exam.com', NULL),
('teacher2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Professor Li', 'TEACHER', 'li@exam.com', NULL),
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Zhang San', 'STUDENT', 'zhangsan@exam.com', 'CS2024-A'),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Li Si', 'STUDENT', 'lisi@exam.com', 'CS2024-A'),
('student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Wang Wu', 'STUDENT', 'wangwu@exam.com', 'CS2024-B');

-- ============================================
-- Questions - Single Choice (Java Programming)
-- ============================================
INSERT INTO questions (question_type, category, difficulty, score, content, option_a, option_b, option_c, option_d, correct_answer, explanation, created_by) VALUES
('SINGLE_CHOICE', 'Java Basics', 'EASY', 5.00, 'Which keyword is used to define a class in Java?', 'define', 'class', 'struct', 'type', 'B', 'The ''class'' keyword is used to declare a class in Java.', 2),
('SINGLE_CHOICE', 'Java Basics', 'EASY', 5.00, 'What is the default value of an int variable in Java?', 'null', '0', 'undefined', '-1', 'B', 'In Java, int is a primitive type and defaults to 0.', 2),
('SINGLE_CHOICE', 'Java Basics', 'MEDIUM', 5.00, 'Which collection allows duplicate elements?', 'Set', 'Map', 'List', 'TreeSet', 'C', 'List interface implementations (ArrayList, LinkedList) allow duplicate elements.', 2),
('SINGLE_CHOICE', 'OOP Concepts', 'MEDIUM', 5.00, 'Which principle allows a class to have multiple forms?', 'Encapsulation', 'Inheritance', 'Polymorphism', 'Abstraction', 'C', 'Polymorphism allows objects to take multiple forms through method overloading and overriding.', 2),
('SINGLE_CHOICE', 'OOP Concepts', 'HARD', 5.00, 'What happens when a class implements two interfaces with the same default method?', 'Compilation error', 'First interface method wins', 'The class must override the method', 'Runtime exception', 'C', 'If two interfaces provide the same default method, the implementing class must override it to resolve the conflict.', 2);

-- ============================================
-- Questions - Multiple Choice
-- ============================================
INSERT INTO questions (question_type, category, difficulty, score, content, option_a, option_b, option_c, option_d, correct_answer, explanation, created_by) VALUES
('MULTIPLE_CHOICE', 'Java Basics', 'MEDIUM', 10.00, 'Which of the following are primitive data types in Java? (Select all that apply)', 'int', 'String', 'boolean', 'Integer', 'A,C', 'int and boolean are primitive types. String is a class, Integer is a wrapper class.', 2),
('MULTIPLE_CHOICE', 'OOP Concepts', 'MEDIUM', 10.00, 'Which statements about abstract classes are correct? (Select all that apply)', 'They can have constructors', 'They can be instantiated', 'They can have concrete methods', 'They cannot be extended', 'A,C', 'Abstract classes can have constructors and concrete methods. They cannot be instantiated directly but can be extended.', 2),
('MULTIPLE_CHOICE', 'Exception Handling', 'HARD', 10.00, 'Which are checked exceptions in Java? (Select all that apply)', 'IOException', 'NullPointerException', 'SQLException', 'ArrayIndexOutOfBoundsException', 'A,C', 'IOException and SQLException are checked exceptions. NullPointerException and ArrayIndexOutOfBoundsException are unchecked (runtime) exceptions.', 2);

-- ============================================
-- Questions - True/False
-- ============================================
INSERT INTO questions (question_type, category, difficulty, score, content, option_a, option_b, option_c, option_d, correct_answer, explanation, created_by) VALUES
('TRUE_FALSE', 'Java Basics', 'EASY', 5.00, 'Java is a platform-independent language.', 'True', 'False', NULL, NULL, 'True', 'Java bytecode runs on any platform with a JVM, making it platform-independent.', 2),
('TRUE_FALSE', 'OOP Concepts', 'EASY', 5.00, 'A Java class can extend multiple parent classes.', 'True', 'False', NULL, NULL, 'False', 'Java supports single inheritance only. A class can extend only one parent class but can implement multiple interfaces.', 2),
('TRUE_FALSE', 'Java Basics', 'MEDIUM', 5.00, 'The ''final'' keyword prevents a method from being overridden.', 'True', 'False', NULL, NULL, 'True', 'A method declared as final cannot be overridden by subclasses.', 2);

-- ============================================
-- Questions - Fill in the Blank
-- ============================================
INSERT INTO questions (question_type, category, difficulty, score, content, option_a, option_b, option_c, option_d, correct_answer, explanation, created_by) VALUES
('FILL_BLANK', 'Java Basics', 'EASY', 5.00, 'The ______ method is the entry point of a Java program.', NULL, NULL, NULL, NULL, 'main', 'The main method signature is: public static void main(String[] args)', 2),
('FILL_BLANK', 'OOP Concepts', 'MEDIUM', 5.00, 'The process of hiding internal implementation details is called ______.', NULL, NULL, NULL, NULL, 'encapsulation', 'Encapsulation bundles data and methods and hides internal state from outside access.', 2),
('FILL_BLANK', 'Java Basics', 'MEDIUM', 5.00, '______ is used to allocate memory for new objects in Java.', NULL, NULL, NULL, NULL, 'new', 'The ''new'' keyword creates a new instance of a class by allocating memory on the heap.', 2);

-- ============================================
-- Exam Papers
-- ============================================
INSERT INTO exam_papers (title, description, total_score, duration_minutes, start_time, end_time, status, assembly_type, created_by) VALUES
('Java Midterm Exam', 'Midterm examination covering Java basics, OOP concepts, and exception handling.', 100.00, 90, '2026-06-01 08:00:00', '2026-12-31 23:59:59', 'PUBLISHED', 'MANUAL', 2),
('Java Programming Quiz 1', 'Quick quiz on Java fundamentals.', 50.00, 30, '2026-06-01 08:00:00', '2026-12-31 23:59:59', 'PUBLISHED', 'RULE_BASED', 2);

-- ============================================
-- Paper-Question assignments (Paper 1: Java Midterm)
-- ============================================
-- Assigning all questions to Paper 1 for comprehensive exam
INSERT INTO paper_questions (paper_id, question_id, question_order, score) VALUES
(1, 1, 1, 5.00),
(1, 2, 2, 5.00),
(1, 3, 3, 5.00),
(1, 4, 4, 5.00),
(1, 5, 5, 5.00),
(1, 6, 6, 10.00),
(1, 7, 7, 10.00),
(1, 8, 8, 10.00),
(1, 9, 9, 5.00),
(1, 10, 10, 5.00),
(1, 11, 11, 5.00),
(1, 12, 12, 5.00),
(1, 13, 13, 5.00),
(1, 14, 14, 5.00);

-- Paper 2: Quiz (subset of questions)
INSERT INTO paper_questions (paper_id, question_id, question_order, score) VALUES
(2, 1, 1, 5.00),
(2, 2, 2, 5.00),
(2, 9, 3, 5.00),
(2, 10, 4, 5.00),
(2, 12, 5, 5.00),
(2, 14, 6, 10.00),
(2, 6, 7, 10.00),
(2, 13, 8, 5.00);

-- ============================================
-- Sample Exam Records (Student 4 took Paper 1)
-- ============================================
INSERT INTO exam_records (student_id, paper_id, status, start_time, submit_time, total_score, time_spent_seconds) VALUES
(4, 1, 'GRADED', '2026-06-05 09:00:00', '2026-06-05 10:15:00', 75.00, 4500);

-- Sample answers for the graded exam
INSERT INTO answer_records (exam_record_id, question_id, student_answer, is_correct, score_earned) VALUES
(1, 1, 'B', 1, 5.00),
(1, 2, 'B', 1, 5.00),
(1, 3, 'C', 1, 5.00),
(1, 4, 'B', 0, 0.00),
(1, 5, 'C', 1, 5.00),
(1, 6, 'A,C', 1, 10.00),
(1, 7, 'A,B', 0, 0.00),
(1, 8, 'A,C', 1, 10.00),
(1, 9, 'True', 1, 5.00),
(1, 10, 'False', 1, 5.00),
(1, 11, 'False', 0, 0.00),
(1, 12, 'main', 1, 5.00),
(1, 13, 'encapsulation', 1, 5.00),
(1, 14, 'new', 1, 5.00);

-- Wrong answer notebook entries
INSERT INTO wrong_answer_notebook (student_id, question_id, wrong_answer, wrong_count) VALUES
(4, 4, 'B', 1),
(4, 7, 'A,B', 1),
(4, 11, 'False', 1);
