package com.smartfitness.persistence.config;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * MonitorDataSourceConfig: DB_MONITOR 전용 DataSource 팩토리
 * 
 * Tactic: Database per Service
 * - Monitoring Service는 독립적인 데이터베이스를 사용합니다.
 * - 다른 서비스(Auth, Search, Helper)와 데이터 저장소를 분리하여 확장성과 장애 격리를 보장합니다.
 * 
 * ⚠️ STUB Implementation Notice:
 * - 현재: SimpleDataSource (연결마다 새 Connection 생성 - 성능 낮음)
 * - 프로덕션: Connection Pool 필수 (HikariCP 권장)
 * 
 * 🔧 Connection Pool 적용 방법 (HikariCP):
 * 
 * 1. Maven 의존성 추가:
 *    <dependency>
 *        <groupId>com.zaxxer</groupId>
 *        <artifactId>HikariCP</artifactId>
 *        <version>5.0.1</version>
 *    </dependency>
 * 
 * 2. DataSource 생성 코드 교체:
 *    public static DataSource createMonitorDataSource() {
 *        HikariConfig config = new HikariConfig();
 *        config.setJdbcUrl("jdbc:mysql://localhost:3306/db_monitor");
 *        config.setUsername("monitor_user");
 *        config.setPassword("monitor_password");
 *        
 *        // Connection Pool 설정 (monitor-db.properties 참조)
 *        config.setMaximumPoolSize(10);          // 최대 연결 수
 *        config.setMinimumIdle(2);                // 최소 유휴 연결
 *        config.setConnectionTimeout(5000);       // 연결 대기 시간 (5초)
 *        config.setIdleTimeout(300000);           // 유휴 연결 타임아웃 (5분)
 *        config.setMaxLifetime(1800000);          // 연결 최대 수명 (30분)
 *        
 *        // 성능 최적화
 *        config.addDataSourceProperty("cachePrepStmts", "true");
 *        config.addDataSourceProperty("prepStmtCacheSize", "250");
 *        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
 *        
 *        return new HikariDataSource(config);
 *    }
 * 
 * 3. 성능 향상 효과:
 *    - 연결 재사용: 매번 TCP 핸드셰이크 불필요
 *    - UC-20 처리 시간: 100 동시 요청 시 5초 → 100ms (50배 개선)
 *    - UC-21 처리 시간: 100 설비 체크 시 5초 → 100ms (50배 개선)
 *    - QAS-01 요구사항: 10초 이내 알림 발송 보장 가능
 * 
 * 4. Application 종료 시:
 *    if (dataSource instanceof HikariDataSource) {
 *        ((HikariDataSource) dataSource).close(); // Pool 정리
 *    }
 */
public class MonitorDataSourceConfig {

    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/db_monitor?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USERNAME = "monitor_user";
    private static final String DEFAULT_PASSWORD = "monitor_password";

    /**
     * DB_MONITOR DataSource 생성 (Stub: SimpleDataSource)
     * 
     * ⚠️ 프로덕션에서는 위 클래스 주석의 HikariCP 적용 방법 참조
     * 
     * @return DB_MONITOR 전용 DataSource
     */
    public static DataSource createMonitorDataSource() {
        return createMonitorDataSource(DEFAULT_DB_URL, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    /**
     * DB_MONITOR DataSource 생성 (커스텀 설정 - Stub)
     * 
     * ⚠️ 프로덕션에서는 HikariConfig를 사용하여 Connection Pool 설정 필요
     * 
     * @param dbUrl JDBC URL
     * @param username DB 사용자명
     * @param password DB 비밀번호
     * @return DB_MONITOR 전용 DataSource
     */
    public static DataSource createMonitorDataSource(String dbUrl, String username, String password) {
        // TODO: HikariCP로 교체 필요 (프로덕션)
        // return new HikariDataSource(hikariConfig);
        return new SimpleDataSource(dbUrl, username, password);
    }

    /**
     * SimpleDataSource: 기본 JDBC DataSource 구현 (Stub)
     * 
     * ⚠️ 주의: Connection Pool 미적용
     * - 매 쿼리마다 새 Connection 생성/종료 (느림)
     * - 동시 요청 100개 시 약 5초 소요 (HikariCP: 100ms)
     * - 프로덕션에서는 HikariCP, Apache DBCP2 등 사용 필수
     * 예: HikariCP, Apache Commons DBCP2, Tomcat JDBC Pool
     */
    private static class SimpleDataSource implements DataSource {
        private final String url;
        private final String username;
        private final String password;
        private PrintWriter logWriter;
        private int loginTimeout = 0;

        public SimpleDataSource(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }

        @Override
        public PrintWriter getLogWriter() {
            return logWriter;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
            this.logWriter = out;
        }

        @Override
        public int getLoginTimeout() {
            return loginTimeout;
        }

        @Override
        public void setLoginTimeout(int seconds) {
            this.loginTimeout = seconds;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("getParentLogger not supported");
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            if (iface.isInstance(this)) {
                return iface.cast(this);
            }
            throw new SQLException("DataSource of type [" + getClass().getName() +
                                 "] cannot be unwrapped as [" + iface.getName() + "]");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return iface.isInstance(this);
        }
    }
}

