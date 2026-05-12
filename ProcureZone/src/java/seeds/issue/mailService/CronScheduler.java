/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.issue.mailService;

import com.sun.mail.smtp.SMTPTransport;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import seeds.indent.mailService.EmailConfiguration;

/**
 *
 * @author ramesh.a
 */
public class CronScheduler extends HttpServlet {
    
  

    public void scheduleJob() {
        String jobname = "This is job for Matrial";
        String triggername = "Trigger 1";
        String scheduletime = "0 43 10 * * ?";
//        String toAddr = "aparnarama.p@nuziveeduseeds.com";  // Recipient email
//        String ccAddr = "aparnarama.p@nuziveeduseeds.com";  // You can add any CC addresses here if necessary
//        String subject = "Job Schedule Notification";  // Email subject
//        String message = "The scheduled job for material processing is about to run.";  // Email body message
        
        //System.out.println("JOBNAME1=" + jobname + " TRIGGERNAME1=" + triggername + " JOBCRONEXPR1=" + scheduletime);
        
        

        JobDetail job;
        job = JobBuilder.newJob(Job1.class).withIdentity(jobname).build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggername).withSchedule(CronScheduleBuilder.cronSchedule(scheduletime)).build();
        Scheduler scheduler = null;
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            try {
                scheduler.shutdown(true);
            } catch (SchedulerException f) {
                f.printStackTrace();
            }
        }
        
//        try {
//            getSendEmail(toAddr, ccAddr, message, subject);
//            System.out.println("Mail Sent");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println(" Initializing scheduled tasks from Job.............");
        scheduleJob();
    }
    
    
//    public void getSendEmail(String toAddr, String ccAddr, String msg, String sub) throws MessagingException {
//        try {
//            // Set up the mail session
//            Session mailSession = Session.getInstance(System.getProperties());
//            Transport transport = new SMTPTransport(mailSession, new URLName("172.16.65.65"));
//            transport.connect("172.16.65.65", 25, "", "");
//
//            // Create the message
//            MimeMessage m = new MimeMessage(mailSession);
//            m.setFrom(new InternetAddress("Procure<ezone@nslgroup.co.in>"));
//            m.setSubject(sub);
//
//            // Set recipients
//            Address[] toUser = InternetAddress.parse(toAddr);
//            Address[] ccUser = InternetAddress.parse(ccAddr);
//
//            if ("".equals(toAddr) && toAddr == null) {
//                if (!"".equals(ccAddr) && ccAddr != null) {
//                    m.setRecipients(javax.mail.Message.RecipientType.TO, ccUser);
//                    m.setRecipients(javax.mail.Message.RecipientType.CC, ccUser);
//                }
//            } else {
//                m.setRecipients(javax.mail.Message.RecipientType.TO, toUser);
//                if (!"".equals(ccAddr) && ccAddr != null) {
//                    m.setRecipients(javax.mail.Message.RecipientType.CC, ccUser);
//                }
//            }
//            
//            m.setSentDate(new java.util.Date());
//            m.setContent(msg, "text/html");
//
//            // Send the message
//            transport.sendMessage(m, m.getAllRecipients());
//            System.out.println("Email sent successfully");
//            transport.close();
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//    }
}
