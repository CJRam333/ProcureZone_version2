/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.indent.mailService;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import plant.indent.action.IndentPzDaoImpl;
import pojo.TbPzlIndentMastera;
import pojo.TblIndentMaster;
import seeds.global.service.DaoFactory;
import seeds.indent.action.IndentPdfDetails;
import seeds.indent.daoImpl.IndentDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class IndentNotification {

    private final IndentEmail ie = new IndentEmail();
    private final IndentMessage im = new IndentMessage();
    private final EmailConfiguration ec = new EmailConfiguration();
    private final IndentPdfDetails ipdf = new IndentPdfDetails();
    private TblIndentMaster tblIndentMaster;
    private TbPzlIndentMastera tblPzIndentMaster;
     private final IndentPzDaoImpl indentpzDao = DaoFactory.getDao(IndentPzDaoImpl.class);
    private final IndentDaoImpl indentDao = DaoFactory.getDao(IndentDaoImpl.class);

    public TblIndentMaster getTblIndentMaster() {
        return tblIndentMaster;
    }

    public void setTblIndentMaster(TblIndentMaster tblIndentMaster) {
        this.tblIndentMaster = tblIndentMaster;
    }

    public IndentNotification() throws Exception {
        tblIndentMaster = new TblIndentMaster();
    }

    public void NewIndentRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
        String subject = "New Indent Request-" + tblIndentMaster.getIndentNo();
        String message = im.getNewIndentRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getRmEmailId(req);
        ccAddr = ie.getUserEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmail(toAddr, ccAddr, message, subject);
    }
    
    
    public void NewPzIndentRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        String subject = "Indent for Processing/Packing -" + tblPzIndentMaster.getIndentFinalNumber();
        String message = im.getPzNewIndentRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getPzTestInvMgrEmailId(req);
        ccAddr = ie.getPzUserEmailId(req);  
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPM(toAddr, ccAddr, message, subject);
    }
    
    
    
    public void NewPzQualityMgrNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        String subject = "Quality Passed Indent -" + tblPzIndentMaster.getIndentFinalNumber();
        String message = im.getPzNewIndentRequestOnew(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getQtyMagrEmailId(req);
        ccAddr = ie.getPzRmEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPM(toAddr, ccAddr, message, subject);
    }
    
   
    
    
    
    public void NewPzFlMgrNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        String subject = "Indent For Inventory Issue -" + tblPzIndentMaster.getIndentFinalNumber();
        String message = im.getPzFlMangrInventoryRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getPzflrMgrEmailId(req);
        ccAddr = ie.getPzUserEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPM(toAddr, ccAddr, message, subject);
    }
    
    
    
    
    
    public void NewPzDEONotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        String subject = "Indent For DEO Order -" + tblPzIndentMaster.getIndentFinalNumber();
        String message = im.getDeoProcessRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getPzInvMgrEmailId(req);
        ccAddr = ie.getPzRmEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPM(toAddr, ccAddr, message, subject);
    }
    
    
    public void NewPzFlInchargeNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        String subject = "Indent For Issue Receipt -" + tblPzIndentMaster.getIndentFinalNumber();
        String message = im.getFlInchargeRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getGoodsInchargeEmailId(req);
        ccAddr = ie.getPzUserEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPM(toAddr, ccAddr, message, subject);
        
        
    }
    
    
    
    
    public void NewPzIssueConfirmation(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        String subject = "Indent Issue Confirmation -" + tblPzIndentMaster.getIndentFinalNumber();
        String message = im.getIssueConfirmationReq(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getPzIssueConfirmEmailId(req);
        ccAddr = ie.getPzUserEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPM(toAddr, ccAddr, message, subject);
        
        
    }
    
    public void NewPzReceiptConfirmation(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        String subject = "Indent Receipt Confirmation -" + tblPzIndentMaster.getIndentFinalNumber();
        String message = im.getReceiptConfirmationReq(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getReceiptConfirmEmailId(req);
        ccAddr = ie.getPzUserEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPM(toAddr, ccAddr, message, subject);
        
        
    }
    
    public void NewPzGRNRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        String subject = "Indent Good Receipt Number -" + tblPzIndentMaster.getIndentFinalNumber();
        String message = im.getGrNumberReq(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getPzInvMgrEmailId(req);
        ccAddr = ie.getPzUserEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPM(toAddr, ccAddr, message, subject);
        
        
    }
    
    
    
    public void NewPzDeleteNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        String subject = "Indent Reject Notification -" + tblPzIndentMaster.getIndentFinalNumber();
        String message = im.getDeleteNotification(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getPzInvMgrEmailId(req);
        ccAddr = ie.getPzUserEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPM(toAddr, ccAddr, message, subject);
        
        
    }
    
    public void NewPzQualityDeleteNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        String subject = "Quality Reject Notification -" + tblPzIndentMaster.getIndentFinalNumber();
        String message = im.getQuityDeleteNotification(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getPzInvMgrEmailId(req);
        ccAddr = ie.getPzUserEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPM(toAddr, ccAddr, message, subject);
        
        
    }
    
    
    public void ApprovedIndentRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
        String subject = "Approved Indent Request-" + tblIndentMaster.getIndentNo();
        String message = im.getApprovedIndentRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getFinalRmEmailId(req);
        ccAddr = ie.getRmEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmail(toAddr, ccAddr, message, subject);
    }

    public void RejectedIndentRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
        String subject = "Rejected Indent Request-" + tblIndentMaster.getIndentNo();
        String message = im.getRejectedIndentRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getUserEmailId(req);
        ccAddr = ie.getRmEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmail(toAddr, ccAddr, message, subject);
    }

    public void FinalApprovedIndentRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, FileNotFoundException, Exception {
        tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
        String subject = "Final Approved Indent Request-" + tblIndentMaster.getIndentNo();
        String message = im.getFinalApprovedIndentRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getProcureMentEmailId(req);
        ccAddr = ie.getFinalRmEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmailPDF1(toAddr, ccAddr, message, subject, ipdf.exportToPdf1(req));
    }

    public void FinalRejectedIndentRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
        String subject = "Final Rejected Indent Request-" + tblIndentMaster.getIndentNo();
        String message = im.getFinalRejectedIndentRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getRmEmailId(req);
        ccAddr = ie.getFinalRmEmailId(req);
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmail(toAddr, ccAddr, message, subject);
    }

    public void ProcurementIndentRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
        String subject = "Procurement Indent Request-" + tblIndentMaster.getIndentNo();
        String message = im.getprocurementIndentRequest(req);
        String toAddr;
        String ccAddr;
        if (tblIndentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId() == 7 || tblIndentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId() == 8 || tblIndentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId() == 9) {
            toAddr = ie.getUserEmailId(req);
            toAddr=toAddr+","+ie.getRmEmailId(req);
            ccAddr = ie.getProcureMentEmailId(req);
        } else {
            toAddr = ie.getUserEmailId(req);
            ccAddr = ie.getProcureMentEmailId(req);
        }
        //toAddr = "raghuvamshi.a@nuziveeduseeds.com"; 
        //ccAddr = "raghuvamshi.a@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmail(toAddr, ccAddr, message, subject);
    }

    public static void main(String[] args) throws Exception {
        IndentNotification in = new IndentNotification();
        in.NewIndentRequestNotification(1);
    }
}
