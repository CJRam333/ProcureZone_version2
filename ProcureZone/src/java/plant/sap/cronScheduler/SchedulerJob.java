package plant.sap.cronScheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
 
public class SchedulerJob implements Job {
    

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException ,IndexOutOfBoundsException{
        System.out.println("schedule job implementation stated");
        try {
           SapCsvImport csvImport=new SapCsvImport();
           csvImport.ExcelToDatabase();
           System.out.println("master imported initilized here......"+csvImport);          
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception triggered no job run");
            
        }
    }
    
    public void execute1()throws JobExecutionException ,IndexOutOfBoundsException{
        
        try {
           SapCsvImport csvImport=new SapCsvImport();
           csvImport.ExcelToDatabase();
           System.out.println("master imported initilized here......"+csvImport);          
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception triggered no job run");
            
        }
    }

    
}
