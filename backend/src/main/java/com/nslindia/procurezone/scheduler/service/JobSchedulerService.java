package com.nslindia.procurezone.scheduler.service;

import com.nslindia.procurezone.scheduler.job.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service to register and manage all scheduled jobs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobSchedulerService {

    private static final String GROUP_INVENTORY = "inventory";
    private static final String GROUP_INDENT = "indent";
    private static final String GROUP_MAINTENANCE = "maintenance";
    private static final String GROUP_ANALYTICS = "analytics";

    private final Scheduler scheduler;

    /**
     * Schedule all jobs on application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void scheduleJobs() {
        log.info("=== Scheduling Quartz Jobs ===");

        try {
            scheduleInventoryReorderAlertJob();
            schedulePendingIndentReminderJob();
            scheduleDailyInventorySummaryJob();
            scheduleDataCleanupJob();
            scheduleMonthlyProcurementAnalyticsJob();

            log.info("=== All Quartz Jobs Scheduled Successfully ===");
            log.info("Note: SAP Material Import uses Spring @Scheduled annotations");
        } catch (SchedulerException e) {
            log.error("Error scheduling jobs", e);
        }
    }

    /**
     * Job 1: Inventory Reorder Alert
     * Runs every day at 9:00 AM
     */
    private void scheduleInventoryReorderAlertJob() throws SchedulerException {
        JobDetail job = JobBuilder.newJob(InventoryReorderAlertJob.class)
                .withIdentity("inventoryReorderAlertJob", GROUP_INVENTORY)
                .withDescription("Alert for low stock items")
                .storeDurably()
                .build();

        // Cron: Every day at 09:00
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("inventoryReorderAlertTrigger", GROUP_INVENTORY)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 9 * * ?"))
                .build();

        if (!scheduler.checkExists(job.getKey())) {
            scheduler.scheduleJob(job, trigger);
            log.info("Scheduled: Inventory Reorder Alert Job - Daily at 09:00");
        }
    }

    /**
     * Job 2: Pending Indent Reminder
     * Runs every weekday at 10:00 AM
     */
    private void schedulePendingIndentReminderJob() throws SchedulerException {
        JobDetail job = JobBuilder.newJob(PendingIndentReminderJob.class)
                .withIdentity("pendingIndentReminderJob", GROUP_INDENT)
                .withDescription("Remind approvers of pending indents")
                .storeDurably()
                .build();

        // Cron: Every weekday (Mon-Fri) at 10:00
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("pendingIndentReminderTrigger", GROUP_INDENT)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 10 ? * MON-FRI"))
                .build();

        if (!scheduler.checkExists(job.getKey())) {
            scheduler.scheduleJob(job, trigger);
            log.info("Scheduled: Pending Indent Reminder Job - Weekdays at 10:00");
        }
    }

    /**
     * Job 3: Daily Inventory Summary
     * Runs every day at 6:00 PM
     */
    private void scheduleDailyInventorySummaryJob() throws SchedulerException {
        JobDetail job = JobBuilder.newJob(DailyInventorySummaryJob.class)
                .withIdentity("dailyInventorySummaryJob", GROUP_INVENTORY)
                .withDescription("Generate daily inventory summary report")
                .storeDurably()
                .build();

        // Cron: Every day at 18:00
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("dailyInventorySummaryTrigger", GROUP_INVENTORY)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 18 * * ?"))
                .build();

        if (!scheduler.checkExists(job.getKey())) {
            scheduler.scheduleJob(job, trigger);
            log.info("Scheduled: Daily Inventory Summary Job - Daily at 18:00");
        }
    }

    /**
     * Job 4: Data Cleanup
     * Runs every Sunday at 2:00 AM
     */
    private void scheduleDataCleanupJob() throws SchedulerException {
        JobDetail job = JobBuilder.newJob(DataCleanupJob.class)
                .withIdentity("dataCleanupJob", GROUP_MAINTENANCE)
                .withDescription("Cleanup and archive old data")
                .storeDurably()
                .build();

        // Cron: Every Sunday at 02:00
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("dataCleanupTrigger", GROUP_MAINTENANCE)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 ? * SUN"))
                .build();

        if (!scheduler.checkExists(job.getKey())) {
            scheduler.scheduleJob(job, trigger);
            log.info("Scheduled: Data Cleanup Job - Every Sunday at 02:00");
        }
    }

    /**
     * Job 5: Monthly Procurement Analytics
     * Runs on 1st day of every month at 8:00 AM
     */
    private void scheduleMonthlyProcurementAnalyticsJob() throws SchedulerException {
        JobDetail job = JobBuilder.newJob(MonthlyProcurementAnalyticsJob.class)
                .withIdentity("monthlyProcurementAnalyticsJob", GROUP_ANALYTICS)
                .withDescription("Generate monthly procurement analytics report")
                .storeDurably()
                .build();

        // Cron: 1st day of month at 08:00
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("monthlyProcurementAnalyticsTrigger", GROUP_ANALYTICS)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 1 * ?"))
                .build();

        if (!scheduler.checkExists(job.getKey())) {
            scheduler.scheduleJob(job, trigger);
            log.info("Scheduled: Monthly Procurement Analytics Job - 1st of month at 08:00");
        }
    }

    /**
     * Manually trigger a job (for testing)
     */
    public void triggerJob(String jobName, String group) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, group);
        scheduler.triggerJob(jobKey);
        log.info("Manually triggered job: {}.{}", group, jobName);
    }

    /**
     * Pause a job
     */
    public void pauseJob(String jobName, String group) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, group);
        scheduler.pauseJob(jobKey);
        log.info("Paused job: {}.{}", group, jobName);
    }

    /**
     * Resume a job
     */
    public void resumeJob(String jobName, String group) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, group);
        scheduler.resumeJob(jobKey);
        log.info("Resumed job: {}.{}", group, jobName);
    }
}
