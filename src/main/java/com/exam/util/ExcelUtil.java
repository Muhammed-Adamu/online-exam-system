package com.exam.util;

import com.exam.entity.ExamRecord;
import com.exam.entity.Question;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Excel utility for question import and score export
 */
public class ExcelUtil {

    /**
     * Parse questions from an Excel file (.xlsx)
     * Expected columns: question_type, category, difficulty, score, content,
     *                   option_a, option_b, option_c, option_d, correct_answer, explanation
     * Row 1 is the header (skipped)
     */
    public static List<Question> parseQuestions(InputStream inputStream, Long createdBy) throws IOException {
        List<Question> questions = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 0;

            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue; // Skip header row
                if (isRowEmpty(row)) continue; // Skip empty rows

                try {
                    Question q = new Question();
                    q.setCreatedBy(createdBy);

                    String type = getCellString(row, 0).trim().toUpperCase();
                    if (!isValidType(type)) {
                        errors.add("Row " + rowNum + ": Invalid question type '" + type + "'");
                        continue;
                    }
                    q.setQuestionType(type);

                    String category = getCellString(row, 1).trim();
                    if (category.isEmpty()) {
                        errors.add("Row " + rowNum + ": Category is required");
                        continue;
                    }
                    q.setCategory(category);

                    String difficulty = getCellString(row, 2).trim().toUpperCase();
                    if (!isValidDifficulty(difficulty)) {
                        errors.add("Row " + rowNum + ": Invalid difficulty '" + difficulty + "'");
                        continue;
                    }
                    q.setDifficulty(difficulty);

                    String scoreStr = getCellString(row, 3).trim();
                    q.setScore(scoreStr.isEmpty() ? new BigDecimal("5") : new BigDecimal(scoreStr));

                    String content = getCellString(row, 4).trim();
                    if (content.isEmpty()) {
                        errors.add("Row " + rowNum + ": Question content is required");
                        continue;
                    }
                    q.setContent(content);

                    q.setOptionA(getCellString(row, 5).trim());
                    q.setOptionB(getCellString(row, 6).trim());
                    q.setOptionC(getCellString(row, 7).trim());
                    q.setOptionD(getCellString(row, 8).trim());

                    String answer = getCellString(row, 9).trim();
                    if (answer.isEmpty()) {
                        errors.add("Row " + rowNum + ": Correct answer is required");
                        continue;
                    }
                    q.setCorrectAnswer(answer);

                    q.setExplanation(getCellString(row, 10).trim());

                    questions.add(q);
                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException("Import errors: " + String.join("; ", errors));
        }

        return questions;
    }

    /**
     * Export exam records to Excel (.xlsx)
     */
    public static byte[] exportExamRecords(List<ExamRecord> records, String paperTitle) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exam Scores");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Exam Scores: " + paperTitle);
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            // Header row
            Row headerRow = sheet.createRow(1);
            String[] headers = {"#", "Student Name", "Status", "Score", "Full Marks", "Time Spent (min)", "Submitted"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 2;
            for (ExamRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 2);
                row.createCell(1).setCellValue(record.getStudentName() != null ? record.getStudentName() : "N/A");
                row.createCell(2).setCellValue(record.getStatus());
                row.createCell(3).setCellValue(record.getTotalScore() != null ? record.getTotalScore().doubleValue() : 0);
                row.createCell(4).setCellValue(100); // Will be overridden if paper info available
                row.createCell(5).setCellValue(record.getTimeSpentSeconds() != null ? record.getTimeSpentSeconds() / 60.0 : 0);
                row.createCell(6).setCellValue(record.getSubmitTime() != null ? record.getSubmitTime().toString().replace("T", " ") : "-");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Ensure minimum width
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Parse users from an Excel file (.xlsx)
     * Expected columns: username, password, real_name, role, email, phone, class_name
     */
    public static List<Map<String, String>> parseUsers(InputStream inputStream) throws IOException {
        List<Map<String, String>> users = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 0;
            String[] cols = {"username", "password", "real_name", "role", "email", "phone", "class_name"};

            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue;
                if (isRowEmpty(row)) continue;

                try {
                    Map<String, String> user = new LinkedHashMap<>();
                    for (int i = 0; i < cols.length; i++) {
                        user.put(cols[i], getCellString(row, i).trim());
                    }

                    if (user.get("username").isEmpty()) {
                        errors.add("Row " + rowNum + ": Username is required");
                        continue;
                    }
                    if (user.get("password").isEmpty()) {
                        errors.add("Row " + rowNum + ": Password is required");
                        continue;
                    }
                    String role = user.get("role").toUpperCase();
                    if (!Arrays.asList("ADMIN", "TEACHER", "STUDENT").contains(role)) {
                        errors.add("Row " + rowNum + ": Invalid role '" + role + "'");
                        continue;
                    }
                    user.put("role", role);
                    users.add(user);
                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException("Import errors: " + String.join("; ", errors));
        }
        return users;
    }

    // ---- Helper methods ----

    private static String getCellString(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private static boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidType(String type) {
        return Arrays.asList("SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE", "FILL_BLANK").contains(type);
    }

    private static boolean isValidDifficulty(String difficulty) {
        return Arrays.asList("EASY", "MEDIUM", "HARD").contains(difficulty);
    }
}
