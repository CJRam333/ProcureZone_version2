    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.sap.cronScheduler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
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
public class SapSchedulerListenerPlant implements ServletContextListener {
    
    private SchedulerJob schedulerJob;
    private final Utils utils = new Utils();

    public SchedulerJob getSchedulerJob() {
        return schedulerJob;
    }

    public void setSchedulerJob(SchedulerJob schedulerJob) {
        this.schedulerJob = schedulerJob;
    }
    
    
    
    
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            System.out.println("Scheduler Shutting down for Pz Issue Note successfull on " + new Date());
            scheduler.shutdown();
        } catch (SchedulerException e) {
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        
       // Create a timer instance
        Timer timer = new Timer();

        // Get the current date and time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 16);  // Set the hour of the day for execution (in 24-hour format)
        calendar.set(Calendar.MINUTE, 36);        // Set the minute of the hour for execution
        calendar.set(Calendar.SECOND, 0);        // Set the second of the minute for execution

        // Schedule the timer task to run at the specified time every day
        timer.schedule(new MyTimerTask(), calendar.getTime(), 24 * 60 * 60 * 1000);  // 24 hours interval
        
        JobDetail job = JobBuilder.newJob(SchedulerJob.class).withIdentity("PzIssuenoteJob", "PzIssuenoteDailyJob").build();
        try {
            System.out.println("Inside Scheduler for Pz modified Issue Note");           
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("PzIssuenoteJob", "PzIssuenoteDailyJob").withSchedule(CronScheduleBuilder.cronSchedule("0 30 1 * * ?")).build();
            System.out.println("triggerd job is : "+trigger);
            System.out.println(" job is : "+job);
            
//Trigger trigger = TriggerBuilder.newTrigger().withIdentity("IssuenoteJob", "IssuenoteDailyJob").withSchedule(CronScheduleBuilder.cronSchedule("0 40 9 * * ?")).build();
            //Trigger trigger = TriggerBuilder.newTrigger().withIdentity("IssuenoteJob", "IssuenoteDailyJob").withSchedule(CronScheduleBuilder.cronSchedule("0 10 11 * * ?")).build();
            //Trigger trigger = TriggerBuilder.newTrigger().withIdentity("IssuenoteJob", "IssuenoteDailyJob").withSchedule(CronScheduleBuilder.cronSchedule("0 10 10 * * ?")).build();
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();                    
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            
        }
    }
    
    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                // Method to execute
                executeMethod();
            } catch (JobExecutionException ex) {
                Logger.getLogger(SapSchedulerListenerPlant.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SapSchedulerListenerPlant.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void executeMethod() throws JobExecutionException, FileNotFoundException, IOException {
        
        
        // Code logic for the method you want to execute
        // Add your implementation here
        System.out.println("Method Schedule Executed");
        mergeCSVFiles();
        System.out.println("CSV FILES MERGED NEW");
        // Connection parameters for your SQL database
        String url = "jdbc:mysql://localhost:3306/seeds_indent";
        String user = "root";
//        String password = "ezone160@172169160";
        String password = "password";

        // Path to the Excel file to be read
//        String excelFilePath = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/Plant_Indent(" + utils.DateIn() + ").CSV";
//        String excelFilePath = "E:/Plant_Indent(" + utils.DateIn() + ").csv";
            String excelFilePath = "E:/merged(" + utils.DateIn() + ").csv";
        System.out.println("Dat is "+utils.DateIn());
        

        try {
            // Create a JDBC connection to the database
            java.sql.Connection conn = DriverManager.getConnection(url, user, password);
            
            Statement statement1 = conn.createStatement();
            String truncateSql = "TRUNCATE TABLE pz_schedule_sap_material_master";
            statement1.executeUpdate(truncateSql);

            // Create a SQL statement to insert data into the database
            

            
            String sql = "INSERT INTO pz_schedule_sap_material_master (company_code, plant_code, storage_location,material_code,"
                    + "material_desc,material_uom,batch,quantity,material_type,"
                    + "material_group,material_group_desc,variety_type,variety,"
                    + "crop_type,crop_group,stl,odv,got,elisa,status,"
                    + "mat_code_id,SDCLS,STATS,SKIPD,"
                    + "INSPDT,MOISTURE,PURE_SEED,INERT_MATTER,OCS_COUNT,WEED_SEED_COUNT,GRAIN,"
                    + "BLACK_SEEDS,PINHOLE_SEEDS,ODV_RES,BULK_DENSITY,THSW,COLD_VIGOUR_GERM_NORMAL,"
                    + "FIRST_COUNT_NORMAL,GERM_NORMAL,FET_NORMAL,SOIL_COUNT_DAYS,AAV_GERM_NORMAL,"
                    + "GOT_GP,GOT_FEMALE,GOT_OTHERS,BG1,BG2,HT,FQR,"
                    + "stp_one,lmd) VALUES (?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,"
                    + "?,?)";
            
            
            
            PreparedStatement statement = conn.prepareStatement(sql);

            // Open the CSV file for reading
            BufferedReader reader = new BufferedReader(new FileReader(excelFilePath));

            // Loop through each line in the CSV file
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse the line into separate fields
                String[] fields = line.split(",");
                
//               Get the values from the fields
                int col1 = Integer.parseInt(fields[0]);
                int col2 = Integer.parseInt(fields[1]);
                String  col3 = fields[2];
                String  col4 = fields[3];
                String  col5 = fields[4];
                String  col6 = fields[5];
                String  col7 = fields[6];
                Double  col8 =  Double.valueOf(fields[7]);
                String  col9 =  fields[8];
                int  col10 = Integer.parseInt(fields[9]);
                String  col11 = fields[10];
                String  col12 = fields[11];
                String  col13 = fields[12];
                String  col14 = fields[13];
                String  col15 = fields[14];
                String  col16 = fields[15];
                String  col17 = fields[16];
                String  col18 = fields[17];
                String  col19 = fields[18];
                int col20 = 1;
                int col21 = Integer.parseInt(fields[9]);
                String  col22 = fields.length > 19 && fields[19] != null && !fields[19].isEmpty() ? fields[19] : null;
                String  col23 = fields.length > 20 && fields[20] != null && !fields[20].isEmpty() ? fields[20] : null;
                String  col24 = fields.length > 21 && fields[21] != null && !fields[21].isEmpty() ? fields[21] : null;
                String  col25 = fields.length > 26 && fields[26] != null && !fields[26].isEmpty() ? fields[26] : null;
                String  col26 = fields.length > 27 && fields[27] != null && !fields[27].isEmpty() ? fields[27] : null;
                String  col27 = fields.length > 28 && fields[28] != null && !fields[28].isEmpty() ? fields[28] : null;
                String  col28 = fields.length > 29 && fields[29] != null && !fields[29].isEmpty() ? fields[29] : null;
                String  col29 = fields.length > 30 && fields[30] != null && !fields[30].isEmpty() ? fields[30] : null;
                String  col30 = fields.length > 31 && fields[31] != null && !fields[31].isEmpty() ? fields[31] : null;
                String  col31 = fields.length > 32 && fields[32] != null && !fields[32].isEmpty() ? fields[32] : null;
                String  col32 = fields.length > 33 && fields[33] != null && !fields[33].isEmpty() ? fields[33] : null;
                String  col33 = fields.length > 34 && fields[34] != null && !fields[34].isEmpty() ? fields[34] : null;
                String  col34 = fields.length > 35 && fields[35] != null && !fields[35].isEmpty() ? fields[35] : null;
                String  col35 = fields.length > 36 && fields[36] != null && !fields[36].isEmpty() ? fields[36] : null;
                String  col36 = fields.length > 37 && fields[37] != null && !fields[37].isEmpty() ? fields[37] : null;
                String  col37 = fields.length > 38 && fields[38] != null && !fields[38].isEmpty() ? fields[38] : null;
                String  col38 = fields.length > 39 && fields[39] != null && !fields[39].isEmpty() ? fields[39] : null;
                String  col39 = fields.length > 40 && fields[40] != null && !fields[40].isEmpty() ? fields[40] : null;
                String  col40 = fields.length > 41 && fields[41] != null && !fields[41].isEmpty() ? fields[41] : null;
                String  col41 = fields.length > 42 && fields[42] != null && !fields[42].isEmpty() ? fields[42] : null;
                String  col42 = fields.length > 43 && fields[43] != null && !fields[43].isEmpty() ? fields[43] : null;
                
                String  col43 = fields.length > 44 && fields[44] != null && !fields[44].isEmpty() ? fields[44] : null;
                String  col44 = fields.length > 45 && fields[45] != null && !fields[45].isEmpty() ? fields[45] : null;
                String  col45 = fields.length > 46 && fields[46] != null && !fields[46].isEmpty() ? fields[46] : null;
                String  col46 = fields.length > 47 && fields[47] != null && !fields[47].isEmpty() ? fields[47] : null;
                String  col47 = fields.length > 48 && fields[48] != null && !fields[48].isEmpty() ? fields[48] : null;
                String  col48 = fields.length > 45 && fields[45] != null && !fields[45].isEmpty() ? fields[45] : null;
                String  col49 = fields.length > 46 && fields[46] != null && !fields[46].isEmpty() ? fields[46] : null;
                
                String  col50 = fields.length > 47 && fields[47] != null && !fields[47].isEmpty() ? fields[47] : null;
                String  col51 = utils.DateIn()+" -- "+utils.TimeIn();
//                String  col60 = fields[57];
//                String  col61 = fields[58];

//                // Set the values in the SQL statement
                statement.setInt(1, col1);
                statement.setInt(2, col2);
                statement.setString(3, col3);
                statement.setString(4, col4);
                statement.setString(5, col5);
                statement.setString(6, col6);
                statement.setString(7, col7);
                statement.setDouble(8, col8);
                statement.setString(9, col9);
                statement.setInt(10, col10);
                statement.setString(11, col11);
                statement.setString(12, col12);
                statement.setString(13, col13);
                statement.setString(14, col14);
                statement.setString(15, col15);
                statement.setString(16, col16);
                statement.setString(17, col17);
                statement.setString(18, col18);
                statement.setString(19, col19);
                statement.setInt(20, col20);
                statement.setInt(21, col21);
                statement.setString(22, col22);
                statement.setString(23, col23);
                statement.setString(24, col24);
                statement.setString(25, col25);
                statement.setString(26, col26);
                statement.setString(27, col27);
                statement.setString(28, col28);
                statement.setString(29, col29);
                statement.setString(30, col30);
                statement.setString(31, col31);
                statement.setString(32, col32);
                statement.setString(33, col33);
                statement.setString(34, col34);
                statement.setString(35, col35);
                statement.setString(36, col36);
                statement.setString(37, col37);
                statement.setString(38, col38);
                statement.setString(39, col39);
                statement.setString(40, col40);
                statement.setString(41, col41);
                statement.setString(42, col42);
                
                statement.setString(43, col43);
                statement.setString(44, col44);
                statement.setString(45, col45);
                statement.setString(46, col46);
                statement.setString(47, col47);
                statement.setString(48, col48);
                statement.setString(49, col49);
                statement.setString(50, col50);
                statement.setString(51, col51);
//                
                

                // Execute the SQL statement to insert the row
                statement.executeUpdate();
            }

            // Close the JDBC connection and the CSV file reader
            statement.close();
            conn.close();
            reader.close();

            System.out.println("Data imported successfully.");

        } catch (SQLException e) {
            
            e.printStackTrace();
        }
        
     
    }
    public String mergeCSVFiles() {
        
//        String csvFile1 = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/Plant_Indent(" + utils.DateIn() + ").csv";
//        String csvFile2 = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/Quality_Info(" + utils.DateIn() + ").csv";
//        String mergedFile = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/merged(" + utils.DateIn() + ").csv";
        
        String csvFile1 = "E:/Plant_Indent(" + utils.DateIn() + ").csv";
        String csvFile2 = "E:/Quality_Info(" + utils.DateIn() + ").csv";
        String mergedFile = "E:/merged(" + utils.DateIn() + ").csv";


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
            }
        }
        
    }
   
    

    

  
}
