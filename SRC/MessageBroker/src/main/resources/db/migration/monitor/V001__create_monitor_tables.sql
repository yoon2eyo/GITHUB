-- DB_MONITOR 초기화 스크립트
-- Purpose: UC-20, UC-21 설비 모니터링을 위한 테이블 생성
-- Database per Service: Monitoring Service 전용 스키마

-- =====================================================
-- Table: equipment_status
-- Description: 설비의 주기적 상태 보고 및 하트비트 기록
-- UC-20: 설비 상태 보고 데이터 저장
-- UC-21: 최근 보고 시각 조회를 통한 타임아웃 감지
-- =====================================================
CREATE TABLE IF NOT EXISTS equipment_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id VARCHAR(50) NOT NULL COMMENT '설비 식별자 (예: GATE-01, CAM-01)',
    reported_at TIMESTAMP NOT NULL COMMENT '설비가 상태를 보고한 시각',
    is_fault BOOLEAN NOT NULL DEFAULT FALSE COMMENT '고장 여부 (true: 고장, false: 정상)',
    details TEXT COMMENT '상태 상세 정보 (고장 사유, 추가 메타데이터)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '레코드 생성 시각',
    
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_reported_at (reported_at),
    INDEX idx_equipment_reported (equipment_id, reported_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='설비 상태 보고 이력 (Heartbeat 및 고장 보고)';

-- =====================================================
-- Table: notification_log
-- Description: 설비 고장 알림 발송 내역 저장
-- UC-20 대안-2a: 고장 상태 보고 시 알림 발송 내역 기록
-- UC-21 대안-3a: 타임아웃 감지 시 알림 발송 내역 기록
-- =====================================================
CREATE TABLE IF NOT EXISTS notification_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id VARCHAR(50) NOT NULL COMMENT '고장 발생 설비 ID',
    alert_message TEXT NOT NULL COMMENT '알림 메시지 내용',
    notified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '알림 발송 시각',
    
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_notified_at (notified_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='설비 고장 알림 발송 이력';

-- =====================================================
-- 초기 데이터: 테스트용 설비 등록
-- =====================================================
-- 초기 상태 보고 (설비가 정상 작동 중임을 표시)
INSERT INTO equipment_status (equipment_id, reported_at, is_fault, details) VALUES
('GATE-01', CURRENT_TIMESTAMP, FALSE, 'Initial registration - Normal'),
('CAM-01', CURRENT_TIMESTAMP, FALSE, 'Initial registration - Normal'),
('GATE-02', CURRENT_TIMESTAMP, FALSE, 'Initial registration - Normal');

-- =====================================================
-- Performance Optimization
-- =====================================================
-- Tactic: Maintain Multiple Copies
-- 최근 상태 조회 성능 최적화를 위한 복합 인덱스
-- findLastReportTime 쿼리가 INDEX ONLY SCAN 활용 가능
CREATE INDEX idx_equipment_status_optimization 
ON equipment_status (equipment_id, reported_at DESC, is_fault);

-- =====================================================
-- Data Retention Policy (Optional)
-- =====================================================
-- 30일 이상 경과한 정상 상태 기록 자동 삭제 (배치 작업)
-- 고장 기록은 영구 보관 (분석 목적)
-- DELIMITER $$
-- CREATE EVENT IF NOT EXISTS cleanup_old_status
-- ON SCHEDULE EVERY 1 DAY
-- DO
-- BEGIN
--     DELETE FROM equipment_status 
--     WHERE is_fault = FALSE 
--       AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
-- END$$
-- DELIMITER ;
