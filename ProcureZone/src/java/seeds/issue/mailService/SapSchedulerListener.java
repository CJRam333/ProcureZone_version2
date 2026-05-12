package seeds.issue.mailService;

import java.util.Date;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

//import plant.sap.cronScheduler.SchedulerJob;

public class SapSchedulerListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            System.out.println("Scheduler Shutting down for Issue Note successfull on " + new Date());
            scheduler.shutdown();
        } catch (SchedulerException e) {
        }
    }

//    @Override
//    public void contextInitialized(ServletContextEvent arg0) {
//        JobDetail job = JobBuilder.newJob(SchedulerJob.class).withIdentity("IssuenoteJob", "IssuenoteDailyJob").build();
//        try {
//            System.out.println("Inside Scheduler for Issue Note");           
//            Trigger trigger = TriggerBuilder.newTrigger()
//    .withIdentity("IssuenoteJob", "IssuenoteDailyJob")
//    .withSchedule(CronScheduleBuilder.cronSchedule("0 01 02 * * ?"))
//    .build();
//            //Trigger trigger = TriggerBuilder.newTrigger().withIdentity("IssuenoteJob", "IssuenoteDailyJob").withSchedule(CronScheduleBuilder.cronSchedule("0 40 9 * * ?")).build();
//            //Trigger trigger = TriggerBuilder.newTrigger().withIdentity("IssuenoteJob", "IssuenoteDailyJob").withSchedule(CronScheduleBuilder.cronSchedule("0 10 11 * * ?")).build();
//            //Trigger trigger = TriggerBuilder.newTrigger().withIdentity("IssuenoteJob", "IssuenoteDailyJob").withSchedule(CronScheduleBuilder.cronSchedule("0 10 10 * * ?")).build();
//            Scheduler scheduler = new StdSchedulerFactory().getScheduler();                    
//            scheduler.start();
//            scheduler.scheduleJob(job, trigger);
//        } catch (SchedulerException e) {
//        }
//        
//    }
    
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
    try {
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();

        // Schedule first job
        JobDetail job1 = JobBuilder.newJob(SchedulerJob.class)
                .withIdentity("IssuenoteJob1", "IssuenoteDailyJob")
                .build();
        Trigger trigger1 = TriggerBuilder.newTrigger()
                .withIdentity("Trigger1", "IssuenoteDailyJob")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 21 10 * * ?"))
                .build();
        scheduler.scheduleJob(job1, trigger1);

        // Schedule second job
        JobDetail job2 = JobBuilder.newJob(SchedulerJob.class)
                .withIdentity("IssuenoteJob2", "IssuenoteDailyJob")
                .build();
        Trigger trigger2 = TriggerBuilder.newTrigger()
                .withIdentity("Trigger2", "IssuenoteDailyJob")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 55 11 * * ?"))
                .build();
        scheduler.scheduleJob(job2, trigger2);

//        // Schedule third job
//        JobDetail job3 = JobBuilder.newJob(plant.sap.cronScheduler.SchedulerJob.class)
//                .withIdentity("IssuenoteJob3", "IssuenoteDailyJob")
//                .build();
//        Trigger trigger3 = TriggerBuilder.newTrigger()
//                .withIdentity("Trigger3", "IssuenoteDailyJob")
//                .withSchedule(CronScheduleBuilder.cronSchedule("0 48 16 * * ?"))
//                .build();
//        scheduler.scheduleJob(job3, trigger3);
////
////        // Schedule fourth job
////        JobDetail job4 = JobBuilder.newJob(SchedulerJob.class)
////                .withIdentity("IssuenoteJob4", "IssuenoteDailyJob")
////                .build();
////        Trigger trigger4 = TriggerBuilder.newTrigger()
////                .withIdentity("Trigger4", "IssuenoteDailyJob")
////                .withSchedule(CronScheduleBuilder.cronSchedule("0 10 10 * * ?"))
////                .build();
////        scheduler.scheduleJob(job4, trigger4);
    } catch (SchedulerException e) {
        // Handle scheduler exception
    }
}

}
