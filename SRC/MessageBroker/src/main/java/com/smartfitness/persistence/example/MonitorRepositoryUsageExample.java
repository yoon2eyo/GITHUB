package com.smartfitness.persistence.example;

import com.smartfitness.monitor.model.EquipmentStatusReport;
import com.smartfitness.monitor.ports.IMonitorRepository;
import com.smartfitness.persistence.MonitorRepositoryImpl;
import com.smartfitness.persistence.config.MonitorDataSourceConfig;

import javax.sql.DataSource;
import java.util.Date;

/**
 * MonitorRepositoryUsageExample: MonitorRepositoryImpl 사용 예제
 * 
 * Purpose: UC-20, UC-21 구현을 위한 Repository 활용 방법 데모
 */
public class MonitorRepositoryUsageExample {

    public static void main(String[] args) {
        // 1. DataSource 생성
        DataSource dataSource = MonitorDataSourceConfig.createMonitorDataSource();
        
        // 2. Repository 인스턴스 생성
        IMonitorRepository repository = new MonitorRepositoryImpl(dataSource);
        
        // ===== UC-20: 설비 상태 보고 =====
        System.out.println("=== UC-20: Equipment Status Reporting ===");
        
        // 정상 상태 보고
        EquipmentStatusReport normalReport = new EquipmentStatusReport(
            "GATE-01",
            new Date(),
            false, // 정상
            "Heartbeat - Normal operation"
        );
        
        repository.saveStatus(normalReport);
        System.out.println("✅ Normal status saved for GATE-01");
        
        // 고장 상태 보고
        EquipmentStatusReport faultReport = new EquipmentStatusReport(
            "CAM-01",
            new Date(),
            true, // 고장
            "Camera lens malfunction detected"
        );
        
        repository.saveStatus(faultReport);
        System.out.println("⚠️  Fault status saved for CAM-01");
        
        // ===== UC-21: 설비 상태 모니터링 =====
        System.out.println("\n=== UC-21: Equipment Health Monitoring ===");
        
        // 최근 보고 시각 조회
        Date lastReportTime = repository.findLastReportTime("GATE-01");
        System.out.println("Last report time for GATE-01: " + lastReportTime);
        
        // 타임아웃 체크 (30초 기준)
        long now = System.currentTimeMillis();
        long threshold = 30_000L; // 30초
        
        if (lastReportTime == null || (now - lastReportTime.getTime() > threshold)) {
            System.out.println("⚠️  GATE-01 timeout detected! Last report > 30 seconds ago");
            
            // 알림 발송 내역 저장
            repository.saveNotificationLog("GATE-01", "Heartbeat Timeout - No response for 30+ seconds");
            System.out.println("📧 Notification log saved");
        } else {
            System.out.println("✅ GATE-01 is healthy (reported within 30 seconds)");
        }
        
        // ===== 추가 기능: 설비 목록 조회 =====
        System.out.println("\n=== Additional Feature: Equipment List ===");
        
        if (repository instanceof MonitorRepositoryImpl) {
            MonitorRepositoryImpl impl = (MonitorRepositoryImpl) repository;
            
            // 모든 설비 ID 조회
            java.util.List<String> equipmentIds = impl.findAllEquipmentIds();
            System.out.println("Registered equipment IDs: " + equipmentIds);
            
            // 특정 기간 내 고장 횟수 조회
            Date since = new Date(now - 86400000L); // 지난 24시간
            int faultCount = impl.countFaultsSince("CAM-01", since);
            System.out.println("Fault count for CAM-01 in last 24h: " + faultCount);
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
}
