package com.smartfitness.monitoring.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: Quartz Scheduler
 * Component: QuartzScheduler
 * 
 * Implements: ISchedulerService
 * Technology: Spring @Scheduled + Quartz
 * 
 * DD-04: Triggers EquipmentHealthChecker every 10 seconds
 * 
 * Reference: 05_MonitoringServiceComponent.puml
 */
@Slf4j
@Component
public class QuartzScheduler implements ISchedulerService {
    
    @Override
    public void scheduleHealthCheck() {
        log.info("Scheduling equipment health check (every 10 seconds)");
        
        // Stub: In production, configure Quartz job
        // JobDetail job = JobBuilder.newJob(EquipmentHealthCheckJob.class)
        //     .withIdentity("equipmentHealthCheck", "monitoring")
        //     .build();
        //
        // Trigger trigger = TriggerBuilder.newTrigger()
        //     .withIdentity("equipmentHealthCheckTrigger", "monitoring")
        //     .startNow()
        //     .withSchedule(SimpleScheduleBuilder.simpleSchedule()
        //         .withIntervalInSeconds(10)
        //         .repeatForever())
        //     .build();
        //
        // scheduler.scheduleJob(job, trigger);
        
        // Note: Currently using @Scheduled annotation in EquipmentHealthChecker
        log.debug("Using @Scheduled annotation for health check");
    }
}

