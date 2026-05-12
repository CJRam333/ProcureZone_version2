/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.issue.mailService;

import java.sql.SQLException;
import java.util.Map;
import javax.mail.MessagingException;
import pojo.TblIssueNote;
import seeds.global.service.DaoFactory;
import seeds.indent.mailService.EmailConfiguration;
import seeds.issue.daoImpl.IssueNoteDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class IssueNotification {

    private final IssueEmail ie = new IssueEmail();
    private final IssueMessage im = new IssueMessage();
    private final EmailConfiguration ec = new EmailConfiguration();
    private TblIssueNote tblIssueNote;
    private final IssueNoteDaoImpl issueNoteDao = DaoFactory.getDao(IssueNoteDaoImpl.class);

    public TblIssueNote getTblIssueNote() {
        return tblIssueNote;
    }

    public void setTblIssueNote(TblIssueNote tblIssueNote) {
        this.tblIssueNote = tblIssueNote;
    }

    public IssueNotification() throws Exception {
        tblIssueNote = new TblIssueNote();
    }

    public void NewIssueNoteRequestNotification(int req,int role) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblIssueNote = (TblIssueNote) issueNoteDao.getById(req);
        String subject = "New Issue Note Request-" + tblIssueNote.getIssueNoteNo();
        String message = im.getNewIssueNoteRequest(req);
        String toAddr;
        String ccAddr;
        if(role==4){
            toAddr = ie.getStoresEmailId(req);
            ccAddr = ie.getUserEmailId(req);
        }else{
            toAddr = ie.getRmEmailId(req);
            ccAddr = ie.getUserEmailId(req);
        }
        //toAddr = "ramesh.avv@nuziveeduseeds.com";
        //ccAddr = "ramesh.avv@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmail(toAddr, ccAddr, message, subject);
    }

    public void ApprovedIssueNoteRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblIssueNote = (TblIssueNote) issueNoteDao.getById(req);
        String subject = "Approved Issue Note Request-" + tblIssueNote.getIssueNoteNo();
        String message = im.getApprovedIssueNoteRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getStoresEmailId(req);
        ccAddr = ie.getRmEmailId(req) + "," +ie.getUserEmailId(req);
        //toAddr = "ramesh.avv@nuziveeduseeds.com";
        //ccAddr = "ramesh.avv@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmail(toAddr, ccAddr, message, subject);
    }

    public void RejectedIssueNoteRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblIssueNote = (TblIssueNote) issueNoteDao.getById(req);
        String subject = "Rejected Issue Note Request-" + tblIssueNote.getIssueNoteNo();
        String message = im.getRejectedIssueNoteRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getUserEmailId(req);
        ccAddr = ie.getRmEmailId(req);
        //toAddr = "ramesh.avv@nuziveeduseeds.com";
        //ccAddr = "ramesh.avv@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmail(toAddr, ccAddr, message, subject);
    }

    public void StoresRejectedIssueNoteRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblIssueNote = (TblIssueNote) issueNoteDao.getById(req);
        String subject = "Stores Rejected Issue Note Request-" + tblIssueNote.getIssueNoteNo();
        String message = im.getStoresRejectedIssueNoteRequest(req);
        String toAddr;
        String ccAddr;
        toAddr = ie.getUserEmailId(req);
        if (!ie.getRmEmailId(req).isEmpty()) {
            toAddr = toAddr + "," + ie.getRmEmailId(req);
        }
        ccAddr = ie.getStoresEmailId(req);
        //toAddr = "ramesh.avv@nuziveeduseeds.com";
        //ccAddr = "ramesh.avv@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmail(toAddr, ccAddr, message, subject);
    }

    public void StoresIssueNoteRequestNotification(int req) throws MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        tblIssueNote = (TblIssueNote) issueNoteDao.getById(req);
        String subject = "Stores Goods Issued Note Request-" + tblIssueNote.getIssueNoteNo();
        String message = im.getStoresIssueNoteRequest(req);
        String toAddr;
        String ccAddr;
        if (tblIssueNote.getTblIndentStatusByIssueNoteStoresbyStatus().getIndentStatusId() == 11) {
            toAddr = ie.getUserEmailId(req);
            //toAddr = toAddr + "," + ie.getRmEmailId(req);
            ccAddr = ie.getStoresEmailId(req);
        } else {
            toAddr = ie.getUserEmailId(req);
            ccAddr = ie.getStoresEmailId(req);
        }
        //toAddr = "ramesh.avv@nuziveeduseeds.com"; 
        //ccAddr = "ramesh.avv@nuziveeduseeds.com";
        System.out.println("toAddr " + toAddr);
        System.out.println("ccAddr " + ccAddr);
        ec.getSendEmail(toAddr, ccAddr, message, subject);
    }

}
