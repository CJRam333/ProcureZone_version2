package seeds.issue.mailService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SchedulerJob1 implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
           SapCsvImport csvImport=new SapCsvImport();           
           csvImport.MaterialQuantityDetailsReport();
        } catch (Exception e) {
        }
    }
}
