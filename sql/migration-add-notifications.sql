-- ============================================
-- Migration: Add Notifications Table
-- Run this script to add notification support
-- to an existing database without data loss.
-- ============================================

USE online_exam_system;

CREATE TABLE IF NOT EXISTS notifications (
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

SELECT 'Notifications table created successfully.' AS status;
