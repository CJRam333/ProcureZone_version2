/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.indent.mailService;

import com.sun.mail.smtp.SMTPTransport;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.Date;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import seeds.dbconfig.Util.Utils;

/**
 *
 * @author ramesh.avv
 */
public class EmailConfiguration {

    private final Utils utils = new Utils();

    public void getSendEmail(String toAddr, String ccAddr, String msg, String sub) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        try {
            Session mailSession = Session.getInstance(System.getProperties());//587
            //Transport transport = new SMTPTransport(mailSession, new URLName("mail.nslindia.com"));
            //transport.connect("mail.nslindia.com", 25, "admingroup", "Secure@123");
            Transport transport = new SMTPTransport(mailSession, new URLName("172.16.65.65"));
            transport.connect("172.16.65.65", 25, "", "");
            //transport.connect("smtp.nslgroup.in", 25, "ezone@nslgroup.co.in", "Intex@159");
            MimeMessage m = new MimeMessage(mailSession);
            //m.setFrom(new InternetAddress("Procure Zone<ezone@nslindia.com>"));
            m.setFrom(new InternetAddress("Procure<ezone@nslgroup.co.in>"));
            m.setSubject(sub);
            Address[] toUser = InternetAddress.parse(toAddr);
            Address[] ccUser = InternetAddress.parse(ccAddr);
            if ("".equals(toAddr) && toAddr == null) {
                if (!"".equals(ccAddr) && ccAddr != null) {
                    m.setRecipients(javax.mail.Message.RecipientType.TO, ccUser);
                    m.setRecipients(javax.mail.Message.RecipientType.CC, ccUser);
                }
            } else {
                m.setRecipients(javax.mail.Message.RecipientType.TO, toUser);
                if (!"".equals(ccAddr) && ccAddr != null) {
                    m.setRecipients(javax.mail.Message.RecipientType.CC, ccUser);
                }
            }
            m.setSentDate(new java.util.Date());
            String msg1 = msg;
            m.setContent(msg1, "text/html");
            transport.sendMessage(m, m.getAllRecipients());
            System.out.println("Transport Code :   "+transport);
            transport.close();
        } catch (MessagingException e) {

        }
    }
    
    public void getSendEmailPM(String toAddr, String ccAddr, String msg, String sub) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        try {
            Session mailSession = Session.getInstance(System.getProperties());//587
            //Transport transport = new SMTPTransport(mailSession, new URLName("mail.nslindia.com"));
            //transport.connect("mail.nslindia.com", 25, "admingroup", "Secure@123");
            Transport transport = new SMTPTransport(mailSession, new URLName("172.16.65.65"));
            transport.connect("172.16.65.65", 25, "", "");
            //transport.connect("smtp.nslgroup.in", 25, "ezone@nslgroup.co.in", "Intex@159");
            MimeMessage m = new MimeMessage(mailSession);
            //m.setFrom(new InternetAddress("Procure Zone<ezone@nslindia.com>"));
            m.setFrom(new InternetAddress("NSLDAKSH<nsldaksh@nslgroup.co.in>"));
            m.setSubject(sub);
            Address[] toUser = InternetAddress.parse(toAddr);
            Address[] ccUser = InternetAddress.parse(ccAddr);
            if ("".equals(toAddr) && toAddr == null) {
                if (!"".equals(ccAddr) && ccAddr != null) {
                    m.setRecipients(javax.mail.Message.RecipientType.TO, ccUser);
                    m.setRecipients(javax.mail.Message.RecipientType.CC, ccUser);
                }
            } else {
                m.setRecipients(javax.mail.Message.RecipientType.TO, toUser);
                if (!"".equals(ccAddr) && ccAddr != null) {
                    m.setRecipients(javax.mail.Message.RecipientType.CC, ccUser);
                }
            }
            m.setSentDate(new java.util.Date());
            String msg1 = msg;
            m.setContent(msg1, "text/html");
            transport.sendMessage(m, m.getAllRecipients());
            System.out.println("Transport Code :   "+transport);
            transport.close();
        } catch (MessagingException e) {

        }
    }
    
    

    public void getSendEmailPDF(String toAddr, String ccAddr, String msg, String sub, ByteArrayOutputStream baos) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        try {
            String filePath = "Indent(" + utils.DateIn() + ").pdf";
            Session mailSession = Session.getDefaultInstance(System.getProperties());
            Transport transport = new SMTPTransport(mailSession, new URLName("172.16.65.65"));
            //transport.connect("smtp.nslgroup.in", 25, "ezone@nslgroup.co.in", "Intex@159");
            transport.connect("172.16.65.65", 25, "", "");
            //Transport transport = new SMTPTransport(mailSession, new URLName("mail.nslindia.com"));
            //transport.connect("mail.nslindia.com", 25, "admingroup", "Secure@123");
            MimeMessage m = new MimeMessage(mailSession);
            //m.setFrom(new InternetAddress("Seeds Indent<ezone@nslindia.com>"));
            m.setFrom(new InternetAddress("Seeds Indent<ezone@nslgroup.co.in>"));
            Address[] toUser = InternetAddress.parse(toAddr);
            Address[] ccUser = InternetAddress.parse(ccAddr);
            if (("".equals(toAddr)) && (toAddr == null)) {
                if ((!"".equals(ccAddr)) && (ccAddr != null)) {
                    m.setRecipients(javax.mail.Message.RecipientType.TO, ccUser);
                    m.setRecipients(javax.mail.Message.RecipientType.CC, ccUser);
                }
            } else {
                m.setRecipients(javax.mail.Message.RecipientType.TO, toUser);
                if ((!"".equals(ccAddr)) && (ccAddr != null)) {
                    m.setRecipients(javax.mail.Message.RecipientType.CC, ccUser);
                }
            }
            Multipart multi = new MimeMultipart();
            String msg1 = msg;
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(msg1, "text/html");
            multi.addBodyPart(mbp);
            
            DataSource source = new ByteArrayDataSource(baos.toByteArray(), "application/pdf");

            mbp = new MimeBodyPart();
            String filename = filePath;
            // DataSource source = new FileDataSource(filename);
            mbp.setDataHandler(new DataHandler(source));
            mbp.setHeader("contentType", "application/pdf");
            mbp.setFileName(filename);
            multi.addBodyPart(mbp);

            m.setContent(multi);
            m.setSubject(sub);
            m.setSentDate(new Date());
            transport.sendMessage(m, m.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
        }
    }
    
    public void getSendEmailPDF1(String toAddr, String ccAddr, String msg, String sub, String file) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        int i = 0;
        try {
            String filePath = file;
            Session mailSession = Session.getDefaultInstance(System.getProperties());
            Transport transport = new SMTPTransport(mailSession, new URLName("172.16.65.65"));
            //transport.connect("smtp.nslgroup.in", 25, "ezone@nslgroup.co.in", "Intex@159");
            transport.connect("172.16.65.65", 25, "", "");
            //Transport transport = new SMTPTransport(mailSession, new URLName("mail.nslindia.com"));
            //transport.connect("mail.nslindia.com", 25, "admingroup", "Secure@123");
            MimeMessage m = new MimeMessage(mailSession);
            //m.setFrom(new InternetAddress("Seeds Indent<ezone@nslindia.com>"));
            m.setFrom(new InternetAddress("Seeds Indent<ezone@nslgroup.co.in>"));
            Address[] toUser = InternetAddress.parse(toAddr);
            Address[] ccUser = InternetAddress.parse(ccAddr);
            if ("".equals(toAddr) && toAddr == null) {
                if (!"".equals(ccAddr) && ccAddr != null) {
                    m.setRecipients(javax.mail.Message.RecipientType.TO, ccUser);
                    m.setRecipients(javax.mail.Message.RecipientType.CC, ccUser);
                }
            } else {
                m.setRecipients(javax.mail.Message.RecipientType.TO, toUser);
                if (!"".equals(ccAddr) && ccAddr != null) {
                    m.setRecipients(javax.mail.Message.RecipientType.CC, ccUser);
                }
            }
            Multipart multi = new MimeMultipart();
            String msg1 = msg;
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(msg1, "text/html");
            multi.addBodyPart(mbp);
            mbp = new MimeBodyPart();

            String filename = filePath;
            DataSource source = new FileDataSource(filename);
            mbp.setDataHandler(new DataHandler(source));
            mbp.setFileName("Indent(" + utils.DateIn() + ").pdf");
            multi.addBodyPart(mbp);
            m.setContent(multi);
            m.setSubject(sub);
            m.setSentDate(new java.util.Date());
            transport.sendMessage(m, m.getAllRecipients());
            transport.close();  
        } catch (MessagingException e) {
        }
    }
}
