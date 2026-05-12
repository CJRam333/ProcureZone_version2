/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.sap.cronScheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author ramesh.a
 */
public class SchedulerJob1 implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
           seeds.issue.mailService.SapCsvImport csvImport=new seeds.issue.mailService.SapCsvImport();           
           csvImport.MaterialQuantityDetailsReport();
        } catch (Exception e) {
        }
    }
}
