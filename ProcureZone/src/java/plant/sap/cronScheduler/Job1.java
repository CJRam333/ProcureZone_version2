/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.sap.cronScheduler;

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
            System.out.println("Job started ..........................");
            SapCsvImport csvImport=new SapCsvImport();
            csvImport.ExcelToDatabase();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(Job1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
