/*
 * and open the template in the editor.
 */
package seeds.issue.mailService;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author ramesh.a
 */
public class Job1 implements Job {
    
    /**
     *
     * @param arg0
     * @throws JobExecutionException
     * @throws Exception
     */
    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        try {
            System.out.println("Job started .............Material Master.............");
            SapCsvImport csvImport=new SapCsvImport();
            csvImport.InsertMaterialQuantityDetails();
            
        } catch (Exception ex) {
            Logger.getLogger(Job1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
