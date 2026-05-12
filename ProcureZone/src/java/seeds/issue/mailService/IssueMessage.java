/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.issue.mailService;

import java.util.ArrayList;
import pojo.TblIssueNote;
import pojo.TblIssueNoteDetails;
import seeds.global.service.DaoFactory;
import seeds.issue.daoImpl.IssueNoteDaoImpl;
import seeds.issue.daoImpl.IssueNoteDetailsDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class IssueMessage {
    
    private TblIssueNote tblIssueNote;
    private ArrayList<TblIssueNote> listTblIssueNote;
    private final IssueNoteDaoImpl issueNoteDao = DaoFactory.getDao(IssueNoteDaoImpl.class);
    private TblIssueNoteDetails tblIssueNoteDetails;
    private ArrayList<TblIssueNoteDetails> listTblIssueNoteDetails;
    private final IssueNoteDetailsDaoImpl issueNoteDetailsDao = DaoFactory.getDao(IssueNoteDetailsDaoImpl.class);

    public TblIssueNote getTblIssueNote() {
        return tblIssueNote;
    }

    public void setTblIssueNote(TblIssueNote tblIssueNote) {
        this.tblIssueNote = tblIssueNote;
    }

    public ArrayList<TblIssueNote> getListTblIssueNote() {
        return listTblIssueNote;
    }

    public void setListTblIssueNote(ArrayList<TblIssueNote> listTblIssueNote) {
        this.listTblIssueNote = listTblIssueNote;
    }

    public TblIssueNoteDetails getTblIssueNoteDetails() {
        return tblIssueNoteDetails;
    }

    public void setTblIssueNoteDetails(TblIssueNoteDetails tblIssueNoteDetails) {
        this.tblIssueNoteDetails = tblIssueNoteDetails;
    }

    public ArrayList<TblIssueNoteDetails> getListTblIssueNoteDetails() {
        return listTblIssueNoteDetails;
    }

    public void setListTblIssueNoteDetails(ArrayList<TblIssueNoteDetails> listTblIssueNoteDetails) {
        this.listTblIssueNoteDetails = listTblIssueNoteDetails;
    }

    public IssueMessage() throws Exception{
        tblIssueNote = new TblIssueNote();
        listTblIssueNote = new ArrayList<TblIssueNote>();
        tblIssueNoteDetails = new TblIssueNoteDetails();
        listTblIssueNoteDetails = new ArrayList<TblIssueNoteDetails>();
    }
    
    public String getNewIssueNoteRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            tblIssueNote=(TblIssueNote)issueNoteDao.getById(req);
            listTblIssueNoteDetails=(ArrayList<TblIssueNoteDetails>)issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>Requested Quantity</font></td><td><font color='blue'>Quantity in Stores</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIssueNoteDetails issueNoteDetails:listTblIssueNoteDetails){
                message2= message2 +"<tr><td> " + issueNoteDetails.getTblMaterialMaster().getMaterialName()+ " - "+issueNoteDetails.getTblMaterialMaster().getMaterialDesc()+" </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsRequestedQuantity()+ " </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsQuantityStores()+ " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#C7241D align=\"center\"><h3><font color='white'>New Issue Note Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Issue Note No</font></td><td> " + tblIssueNote.getIssueNoteNo()+ " </td>"
                    + "<td><font color='blue'>EmpId</font></td><td> " + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId()+ " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpName()+ " ("+tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIssueNote.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Plant</font></td><td> " + tblIssueNote.getTblPlantMaster().getPlantName()+ " </td>"
                    + "<td><font color='blue'>Department</font></td><td> " + tblIssueNote.getTblDepartmentMaster().getDeptName()+ " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIssueNote.getTblSectionMaster().getSecName()+ " </td>"
                    + "<td><font color='blue'>Year</font></td><td> " + tblIssueNote.getIssueNoteYear()+ " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteCreatedRemarks()+ " </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>Procure Zone</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>Procure Zone</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
        }catch(Exception e){}
        return message;
    }
    
    public String getApprovedIssueNoteRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            tblIssueNote=(TblIssueNote)issueNoteDao.getById(req);
            listTblIssueNoteDetails=(ArrayList<TblIssueNoteDetails>)issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>Requested Quantity</font></td><td><font color='blue'>Quantity in Stores</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIssueNoteDetails issueNoteDetails:listTblIssueNoteDetails){
                message2= message2 +"<tr><td> " + issueNoteDetails.getTblMaterialMaster().getMaterialName()+ " - "+issueNoteDetails.getTblMaterialMaster().getMaterialDesc()+" </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsRequestedQuantity()+ " </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsQuantityStores()+ " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#C7241D align=\"center\"><h3><font color='white'>Approved Issue Note Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Issue Note No</font></td><td> " + tblIssueNote.getIssueNoteNo()+ " </td>"
                    + "<td><font color='blue'>EmpId</font></td><td> " + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId()+ " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpName()+ " ("+tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIssueNote.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Plant</font></td><td> " + tblIssueNote.getTblPlantMaster().getPlantName()+ " </td>"
                    + "<td><font color='blue'>Department</font></td><td> " + tblIssueNote.getTblDepartmentMaster().getDeptName()+ " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIssueNote.getTblSectionMaster().getSecName()+ " </td>"
                    + "<td><font color='blue'>Year</font></td><td> " + tblIssueNote.getIssueNoteYear()+ " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteCreatedRemarks()+ " </td></tr>"                  
                    + "<tr><td colspan=1><font color='blue'>Approved By</font></td><td colspan=3> " + tblIssueNote.getTblEmpMasterByIssueNoteApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Date</font></td><td colspan=3> " + tblIssueNote.getIssueNoteApprovedbyDate()+ "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteApprovedbyRemarks()+ "</td></tr>"
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>Procure Zone</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>Procure Zone</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
        }catch(Exception e){}
        return message;
    }
    
    public String getRejectedIssueNoteRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            tblIssueNote=(TblIssueNote)issueNoteDao.getById(req);
            listTblIssueNoteDetails=(ArrayList<TblIssueNoteDetails>)issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>Requested Quantity</font></td><td><font color='blue'>Quantity in Stores</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIssueNoteDetails issueNoteDetails:listTblIssueNoteDetails){
                message2= message2 +"<tr><td> " + issueNoteDetails.getTblMaterialMaster().getMaterialName()+ " - "+issueNoteDetails.getTblMaterialMaster().getMaterialDesc()+" </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsRequestedQuantity()+ " </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsQuantityStores()+ " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#C7241D align=\"center\"><h3><font color='white'>Rejected Issue Note Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Issue Note No</font></td><td> " + tblIssueNote.getIssueNoteNo()+ " </td>"
                    + "<td><font color='blue'>EmpId</font></td><td> " + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId()+ " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpName()+ " ("+tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIssueNote.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Plant</font></td><td> " + tblIssueNote.getTblPlantMaster().getPlantName()+ " </td>"
                    + "<td><font color='blue'>Department</font></td><td> " + tblIssueNote.getTblDepartmentMaster().getDeptName()+ " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIssueNote.getTblSectionMaster().getSecName()+ " </td>"
                    + "<td><font color='blue'>Year</font></td><td> " + tblIssueNote.getIssueNoteYear()+ " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteCreatedRemarks()+ " </td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Rejected By</font></td><td colspan=3> " + tblIssueNote.getTblEmpMasterByIssueNoteApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Rejected Date</font></td><td colspan=3> " + tblIssueNote.getIssueNoteApprovedbyDate() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Rejected Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteApprovedbyRemarks() + "</td></tr>"
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>Procure Zone</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>Procure Zone</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
        }catch(Exception e){}
        return message;
    }
    
    public String getStoresRejectedIssueNoteRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            tblIssueNote=(TblIssueNote)issueNoteDao.getById(req);
            listTblIssueNoteDetails=(ArrayList<TblIssueNoteDetails>)issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>Opening Quantity</font></td><td><font color='blue'>Requested Quantity</font></td><td><font color='blue'>Issued Quantity</font></td><td><font color='blue'>Quantity in Stores</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIssueNoteDetails issueNoteDetails:listTblIssueNoteDetails){
                message2= message2 +"<tr><td> " + issueNoteDetails.getTblMaterialMaster().getMaterialName()+ " - "+issueNoteDetails.getTblMaterialMaster().getMaterialDesc()+" </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsOpeningQuantity()+ " </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsRequestedQuantity()+ " </td>"
                    //+ "<td> " + issueNoteDetails.getIssueNoteDetailsQuantity()+ " </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsIssuedQuantity()+ " </td>"    
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsQuantityStores()+ " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#C7241D align=\"center\"><h3><font color='white'>Stores Rejected Issue Note Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Issue Note No</font></td><td> " + tblIssueNote.getIssueNoteNo()+ " </td>"
                    + "<td><font color='blue'>EmpId</font></td><td> " + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId()+ " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpName()+ " ("+tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIssueNote.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Plant</font></td><td> " + tblIssueNote.getTblPlantMaster().getPlantName()+ " </td>"
                    + "<td><font color='blue'>Department</font></td><td> " + tblIssueNote.getTblDepartmentMaster().getDeptName()+ " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIssueNote.getTblSectionMaster().getSecName()+ " </td>"
                    + "<td><font color='blue'>Year</font></td><td> " + tblIssueNote.getIssueNoteYear()+ " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteCreatedRemarks()+ " </td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved By</font></td><td colspan=3> " + tblIssueNote.getTblEmpMasterByIssueNoteApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Date</font></td><td colspan=3> " + tblIssueNote.getIssueNoteApprovedbyDate() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteApprovedbyRemarks() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Stores Rejected  By</font></td><td colspan=3> " + tblIssueNote.getTblEmpMasterByIssueNoteStoresby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Stores Rejected Status</font></td><td colspan=3> " + tblIssueNote.getTblIndentStatusByIssueNoteStoresbyStatus().getIndentStatusName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Stores Rejected Date</font></td><td colspan=3> " + tblIssueNote.getIssueNoteStoresbyDate()+ "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Stores Rejected Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteStoresbyRemarks()+ "</td></tr>"
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>Procure Zone</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>Procure Zone</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
        }catch(Exception e){}
        return message;
    }
    
    public String getStoresIssueNoteRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            tblIssueNote=(TblIssueNote)issueNoteDao.getById(req);
            listTblIssueNoteDetails=(ArrayList<TblIssueNoteDetails>)issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>Opening Quantity</font></td><td><font color='blue'>Requested Quantity</font></td><td><font color='blue'>Issued Quantity</font></td><td><font color='blue'>Quantity in Stores</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIssueNoteDetails issueNoteDetails:listTblIssueNoteDetails){
                message2= message2 +"<tr><td> " + issueNoteDetails.getTblMaterialMaster().getMaterialName()+ " - "+issueNoteDetails.getTblMaterialMaster().getMaterialDesc()+" </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsOpeningQuantity()+ " </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsRequestedQuantity()+ " </td>"
                    //+ "<td> " + issueNoteDetails.getIssueNoteDetailsQuantity()+ " </td>"
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsIssuedQuantity()+ " </td>"    
                    + "<td> " + issueNoteDetails.getIssueNoteDetailsQuantityStores()+ " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#C7241D align=\"center\"><h3><font color='white'>Stores Goods Issued Note Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Issue Note No</font></td><td> " + tblIssueNote.getIssueNoteNo()+ " </td>"
                    + "<td><font color='blue'>EmpId</font></td><td> " + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId()+ " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpName()+ " ("+tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIssueNote.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Plant</font></td><td> " + tblIssueNote.getTblPlantMaster().getPlantName()+ " </td>"
                    + "<td><font color='blue'>Department</font></td><td> " + tblIssueNote.getTblDepartmentMaster().getDeptName()+ " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIssueNote.getTblSectionMaster().getSecName()+ " </td>"
                    + "<td><font color='blue'>Year</font></td><td> " + tblIssueNote.getIssueNoteYear()+ " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteCreatedRemarks()+ " </td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved By</font></td><td colspan=3> " + tblIssueNote.getTblEmpMasterByIssueNoteApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Date</font></td><td colspan=3> " + tblIssueNote.getIssueNoteApprovedbyDate() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteApprovedbyRemarks() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Stores By</font></td><td colspan=3> " + tblIssueNote.getTblEmpMasterByIssueNoteStoresby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Stores Status</font></td><td colspan=3> " + tblIssueNote.getTblIndentStatusByIssueNoteStoresbyStatus().getIndentStatusName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Stores Date</font></td><td colspan=3> " + tblIssueNote.getIssueNoteStoresbyDate()+ "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Stores Remarks</font></td><td colspan=3> " + tblIssueNote.getIssueNoteStoresbyRemarks()+ "</td></tr>"
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>Procure Zone</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>Procure Zone</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
        }catch(Exception e){}
        return message;
    }
    
}
