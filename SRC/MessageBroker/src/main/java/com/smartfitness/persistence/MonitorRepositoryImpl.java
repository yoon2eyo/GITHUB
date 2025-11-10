package com.smartfitness.persistence;

import com.smartfitness.monitor.model.EquipmentStatusReport;
import com.smartfitness.monitor.ports.IMonitorRepository;

import java.util.Date;
import java.util.List;
import javax.sql.DataSource;

public class MonitorRepositoryImpl implements IMonitorRepository {
    private final DataSource dataSource;

    public MonitorRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void saveStatus(EquipmentStatusReport report) {
        // TODO: Insert into DB_MONITOR equipment_status table.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Date findLastReportTime(String equipmentId) {
        // TODO: SELECT MAX(received_at) ...
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<EquipmentStatusReport> findRecentFaults(Date since) {
        // TODO: Query DB_MONITOR for fault records.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
