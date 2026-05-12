/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.sap.cronScheduler;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import seeds.dbconfig.Util.Utils;

/**
 *
 * @author ramesh.a
 */
public class CronScheduler extends HttpServlet {
    
    private final Utils utils = new Utils();

    public void scheduleJob() {
        mergeCSVFiles();
        
        String jobname = "This is job for Plant";
        String triggername = "Trigger 2";
        String scheduletime = "17 40 * * * ?";
        System.out.println("raghu started cronjob plant_indents");
        //System.out.println("JOBNAME1=" + jobname + " TRIGGERNAME1=" + triggername + " JOBCRONEXPR1=" + scheduletime);
        JobDetail job = JobBuilder.newJob(SchedulerJob.class).withIdentity(jobname).build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggername).withSchedule(CronScheduleBuilder.cronSchedule(scheduletime)).build();
        Scheduler scheduler = null;
        System.out.println("Job initilized");
        try {
            System.out.println("try block");
            scheduler = new StdSchedulerFactory().getScheduler();
            System.out.println("one");
            scheduler.start();
            System.out.println("two");
            scheduler.scheduleJob(job, trigger);
            
            System.out.println("Job Triggered three");
        } catch (SchedulerException e) {
            try {

                scheduler.shutdown(true);
                System.out.println("Job not Triggered");
            } catch (SchedulerException f) {
            }
        }
    }

    /**
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println(" Initializing scheduled tasks from Job raghu .............");
        scheduleJob();
    }
    
    public String mergeCSVFiles() {
        
        String csvFile1 = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/Plant_Indent(" + utils.DateIn() + ").csv";
        String csvFile2 = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/Quality_Info(" + utils.DateIn() + ").csv";
        String mergedFile = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/merged(" + utils.DateIn() + ").csv";

        FileReader fileReader1 = null;
        FileReader fileReader2 = null;
        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;

        try {
            fileReader1 = new FileReader(csvFile1);
            fileReader2 = new FileReader(csvFile2);
            fileWriter = new FileWriter(mergedFile);
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

            Iterable<CSVRecord> records1 = CSVFormat.DEFAULT.parse(fileReader1);
            Iterable<CSVRecord> records2 = CSVFormat.DEFAULT.parse(fileReader2);

            // Read data from the first CSV file
            List<String[]> data1 = new ArrayList<String[]>();
            for (CSVRecord record : records1) {
                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    row[i] = record.get(i);
                }
                data1.add(row);
            }

            // Read data from the second CSV file
            List<String[]> data2 = new ArrayList<String[]>();
            for (CSVRecord record : records2) {
                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    row[i] = record.get(i);
                }
                data2.add(row);
            }

            // Merge the data side-by-side
            List<String[]> mergedData = new ArrayList<String[]>();
            Iterator<String[]> iterator1 = data1.iterator();
            Iterator<String[]> iterator2 = data2.iterator();
            while (iterator1.hasNext() && iterator2.hasNext()) {
                String[] row1 = iterator1.next();
                String[] row2 = iterator2.next();
                String[] mergedRow = new String[row1.length + row2.length];
                System.arraycopy(row1, 0, mergedRow, 0, row1.length);
                System.arraycopy(row2, 0, mergedRow, row1.length, row2.length);
                mergedData.add(mergedRow);
            }

            // Write merged data to the new CSV file
            for (String[] row : mergedData) {
                csvPrinter.printRecord((Object[]) row);
            }

            return "CSV files merged side-by-side successfully!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to merge CSV files side-by-side.";
        } finally {
            try {
                if (csvPrinter != null) {
                    csvPrinter.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (fileReader2 != null) {
                    fileReader2.close();
                }
                if (fileReader1 != null) {
                    fileReader1.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
}
