package com.smartfitness.persistence;

import com.smartfitness.monitor.model.EquipmentStatusReport;
import com.smartfitness.monitor.ports.IMonitorRepository;

import java.sql.*;
import java.util.Date;
import javax.sql.DataSource;

/**
 * MonitorRepositoryImpl: IMonitorRepository 구현체로 DB_MONITOR와 통신합니다.
 * Role: Heartbeat 기록 및 알림 로그 저장을 구현합니다.
 * 
 * Database Schema (DB_MONITOR):
 * 
 * Table: equipment_status
 * - id (BIGINT, PK, AUTO_INCREMENT)
 * - equipment_id (VARCHAR(50), NOT NULL, INDEX)
 * - reported_at (TIMESTAMP, NOT NULL)
 * - is_fault (BOOLEAN, NOT NULL)
 * - details (TEXT)
 * - created_at (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
 * 
 * Table: notification_log
 * - id (BIGINT, PK, AUTO_INCREMENT)
 * - equipment_id (VARCHAR(50), NOT NULL)
 * - alert_message (TEXT, NOT NULL)
 * - notified_at (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
 * 
 * Tactic: Database per Service - DB_MONITOR는 Monitoring Service 전용
 */
public class MonitorRepositoryImpl implements IMonitorRepository {
    private final DataSource dataSource;

    public MonitorRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void saveStatus(EquipmentStatusReport report) {
        String sql = "INSERT INTO equipment_status (equipment_id, reported_at, is_fault, details) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, report.getEquipmentId());
            pstmt.setTimestamp(2, new Timestamp(report.getReportedAt().getTime()));
            pstmt.setBoolean(3, report.isFault());
            pstmt.setString(4, report.getDetails());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save equipment status for ID: " + 
                                     report.getEquipmentId(), e);
        }
    }

    @Override
    public Date findLastReportTime(String equipmentId) {
        String sql = "SELECT MAX(reported_at) as last_reported " +
                     "FROM equipment_status " +
                     "WHERE equipment_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, equipmentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("last_reported");
                    return timestamp != null ? new Date(timestamp.getTime()) : null;
                }
                return null;
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find last report time for equipment ID: " + 
                                     equipmentId, e);
        }
    }

    @Override
    public void saveNotificationLog(String equipmentId, String alertMessage) {
        String sql = "INSERT INTO notification_log (equipment_id, alert_message) " +
                     "VALUES (?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, equipmentId);
            pstmt.setString(2, alertMessage);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save notification log for equipment ID: " + 
                                     equipmentId, e);
        }
    }
    
    /**
     * 추가 유틸리티 메서드: 특정 시점 이후의 고장 기록 조회
     * (향후 분석 및 리포팅에 활용 가능)
     */
    public int countFaultsSince(String equipmentId, Date since) {
        String sql = "SELECT COUNT(*) as fault_count " +
                     "FROM equipment_status " +
                     "WHERE equipment_id = ? AND is_fault = true AND reported_at >= ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, equipmentId);
            pstmt.setTimestamp(2, new Timestamp(since.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("fault_count");
                }
                return 0;
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count faults for equipment ID: " + 
                                     equipmentId, e);
        }
    }
    
    /**
     * 추가 유틸리티 메서드: 모든 등록된 설비 ID 조회
     * (HeartbeatChecker의 하드코딩된 설비 목록을 대체)
     */
    public java.util.List<String> findAllEquipmentIds() {
        String sql = "SELECT DISTINCT equipment_id FROM equipment_status ORDER BY equipment_id";
        
        java.util.List<String> equipmentIds = new java.util.ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                equipmentIds.add(rs.getString("equipment_id"));
            }
            
            return equipmentIds;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve equipment IDs", e);
        }
    }
}
