/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.indent.mailService;

import java.util.ArrayList;
import java.util.List;
import plant.indent.action.CropDaoImpl;
import plant.indent.action.IndentPzDaoImpl;
import plant.indent.action.IndentPzDetailsDaoImpl;
import pojo.TbPzlIndentMastera;
import pojo.TblCropMaster;
import pojo.TblIndentDetails;
import pojo.TblIndentMaster;
import pojo.TblPzIndentDetails;
import seeds.global.service.DaoFactory;
import seeds.indent.daoImpl.IndentDaoImpl;
import seeds.indent.daoImpl.IndentDetailsDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class IndentMessage {

    private TblIndentMaster tblIndentMaster;
    private List<TblIndentMaster> listTblIndentMaster;
    private final IndentDaoImpl indentDao = DaoFactory.getDao(IndentDaoImpl.class);
    private TblIndentDetails tblIndentDetails;
    private List<TblIndentDetails> listTblIndentDetails;
    private List<TblPzIndentDetails> listTblPzIndentDetails;
    private final IndentPzDetailsDaoImpl indentPzDetailsDao = DaoFactory.getDao(IndentPzDetailsDaoImpl.class);
    private final IndentDetailsDaoImpl indentDetailsDao = DaoFactory.getDao(IndentDetailsDaoImpl.class);
    private TbPzlIndentMastera tblPzIndentMaster;
     private final IndentPzDaoImpl indentpzDao = DaoFactory.getDao(IndentPzDaoImpl.class);
     
     private TblCropMaster tblCropMaster;
    private final CropDaoImpl cropDao = DaoFactory.getDao(CropDaoImpl.class);

    public List<TblPzIndentDetails> getListTblPzIndentDetails() {
        return listTblPzIndentDetails;
    }

    public void setListTblPzIndentDetails(List<TblPzIndentDetails> listTblPzIndentDetails) {
        this.listTblPzIndentDetails = listTblPzIndentDetails;
    }

    public TbPzlIndentMastera getTblPzIndentMaster() {
        return tblPzIndentMaster;
    }

    public void setTblPzIndentMaster(TbPzlIndentMastera tblPzIndentMaster) {
        this.tblPzIndentMaster = tblPzIndentMaster;
    }

    public TblCropMaster getTblCropMaster() {
        return tblCropMaster;
    }

    public void setTblCropMaster(TblCropMaster tblCropMaster) {
        this.tblCropMaster = tblCropMaster;
    }
    
    

    public TblIndentMaster getTblIndentMaster() {
        return tblIndentMaster;
    }

    public void setTblIndentMaster(TblIndentMaster tblIndentMaster) {
        this.tblIndentMaster = tblIndentMaster;
    }

    public List<TblIndentMaster> getListTblIndentMaster() {
        return listTblIndentMaster;
    }

    public void setListTblIndentMaster(List<TblIndentMaster> listTblIndentMaster) {
        this.listTblIndentMaster = listTblIndentMaster;
    }

    public TblIndentDetails getTblIndentDetails() {
        return tblIndentDetails;
    }

    public void setTblIndentDetails(TblIndentDetails tblIndentDetails) {
        this.tblIndentDetails = tblIndentDetails;
    }

    public List<TblIndentDetails> getListTblIndentDetails() {
        return listTblIndentDetails;
    }

    public void setListTblIndentDetails(List<TblIndentDetails> listTblIndentDetails) {
        this.listTblIndentDetails = listTblIndentDetails;
    }

    public IndentMessage() throws Exception {
        tblIndentMaster = new TblIndentMaster();
        listTblIndentMaster = new ArrayList<TblIndentMaster>();
        tblIndentDetails = new TblIndentDetails();
        listTblIndentDetails = new ArrayList<TblIndentDetails>();
    }
    
    public String getNewIndentRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            tblIndentMaster=(TblIndentMaster)indentDao.getById(req);            
            listTblIndentDetails=(ArrayList<TblIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>Project</font></td><td><font color='blue'>Vendor</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIndentDetails indentDetails:listTblIndentDetails){
                message2= message2 +"<tr><td> " + indentDetails.getTblMaterialMaster().getMaterialName()+ " - "+indentDetails.getTblMaterialMaster().getMaterialDesc()+" </td>"
                    + "<td> " + indentDetails.getTblUmoMaster().getUmoName()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsStockAval() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#C7241D align=\"center\"><h3><font color='white'>New Indent Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblIndentMaster.getIndentYear() + " </td>"
                    + "<td><font color='blue'>Date</font></td><td> " + tblIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblIndentMaster.getIndentNo() + " </td>"
                    + "<td><font color='blue'>Depertment</font></td><td> " + tblIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    + "<td><font color='blue'>Plant</font></td><td> " + tblIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentComments()+ " </td></tr>"                   
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
    
    
    
    
    
    
    
    
    public String getPzNewIndentRequest(int req) {
        //System.out.println("This is message request");
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            //System.out.println("This is try Block");
            tblPzIndentMaster=(TbPzlIndentMastera) indentpzDao.getById(req);
            //System.out.println("execute 1");
            listTblPzIndentDetails=(ArrayList<TblPzIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=10 and tblIndentMaster.indentId=" + req + "");
            //System.out.println("execute 2"+listTblPzIndentDetails);
            //System.out.println("execute 2");
            message1="</table><table border=1 style=\"width:800px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material</font></td><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>LotNumber</font></td><td><font color='blue'>Variety</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td><td><font color='blue'>Storage location</font></td><td><font color='blue'>STL</font></td><td><font color='blue'>ODV</font></td><td><font color='blue'>GOT</font></td><td><font color='blue'>ELISA</font></td></tr>";
           // System.out.println("This is try Block"+message1);
            message3="</table><table border=1 style=\"width:800px;font-family:calibri; font-size:16;\" cellspacing=0>";
            //System.out.println("This is try Block"+message3);
            for(TblPzIndentDetails indentDetails:listTblPzIndentDetails){
                
                
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
            //setIndentCrop(tblCropMaster.getCropName());
               // System.out.println("forloop Content");
                message2= message2 +"<tr><td>" + indentDetails.getIndentMatel() + " </td>"
                        + "<td> " + indentDetails.getIndentMatel()+ " - "+indentDetails.getIndentMateDesc()+" </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"
                    + "<td> " + indentDetails.getIndentLoteNum()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsRmQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsQty() + " </td>"
                    + "<td>" + indentDetails.getIndentStoLoc() + " </td>"
                    + "<td>" + indentDetails.getMatStl() + " </td>"
                    + "<td>" + indentDetails.getMatOdv() + " </td>"
                    + "<td>" + indentDetails.getMatGot() + " </td>"
                    + "<td>" + indentDetails.getMatElisa() + " </td></tr>";
                
            }
            message = "<table border=1 style=\"width:800px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#3399ff align=\"center\"><h3><font color='white'>Indent for Processing/Packing</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblPzIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblPzIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    //+ "<td><font color='blue'>Company</font></td><td> " + tblPzIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblPzIndentMaster.getIndentFinalNumber() + " </td>"
                    
                    + "<tr><td><font color='blue'>Crop</font></td><td> " + tblCropMaster.getCropName() + " </td>"
                    + "<tr><td><font color='blue'>Packing Type</font></td><td> " + tblPzIndentMaster.getIndentPackProcessa()+ " </td>"
                    + "<tr><td><font color='blue'>Output Material</font></td><td> " + tblPzIndentMaster.getIndentOutmaterial() + " </td>"
                    + "<tr><td><font color='blue'>Start date</font></td><td> " + tblPzIndentMaster.getIndentStartdate() + " </td>"
                    + "<tr><td><font color='blue'>Batch Number</font></td><td> " + tblPzIndentMaster.getIndentBatchNumbera() + " </td>"
                    + "<tr><td><font color='blue'>UOM</font></td><td> " + tblPzIndentMaster.getIndentUom() + " </td>"
                    + "<tr><td><font color='blue'>Line Code</font></td><td> " + tblPzIndentMaster.getIndentLinecode()+ '-'+tblPzIndentMaster.getIndentLinedesc() + " </td>"
                    + "<tr><td><font color='blue'>Output Quantity</font></td><td> " + tblPzIndentMaster.getIndentOutqty() + " </td>"
                    + "<tr><td><font color='blue'>Created By</font></td><td> " + tblPzIndentMaster.getTblEmpMasterByIndentLmu().getEmpEmail() + " </td>"
                    //+ "<td><font color='blue'>Depertment</font></td><td> " + tblPzIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    //+ "<tr><td><font color='blue'>Section</font></td><td> " + tblPzIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    //+ "<td><font color='blue'>Plant</font></td><td> " + tblPzIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblPzIndentMaster.getIndentComments()+ " </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>SPIMS</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>SPIMS</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
        //System.out.println("this is message"+message);
        }catch(Exception e){}
        //System.out.println("this is message"+message);
        return message;
    }
    
    
    
    
    public String getPzNewIndentRequestOnew(int req) {
        //System.out.println("This is message request");
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            //System.out.println("This is try Block");
            tblPzIndentMaster=(TbPzlIndentMastera) indentpzDao.getById(req);
            //System.out.println("execute 1");
            listTblPzIndentDetails=(ArrayList<TblPzIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + req + "");
            //System.out.println("execute 2"+listTblPzIndentDetails);
            //System.out.println("execute 2");
            message1="</table><table border=1 style=\"width:800px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material</font></td><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>LotNumber</font></td><td><font color='blue'>Variety</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td><td><font color='blue'>Storage location</font></td><td><font color='blue'>STL</font></td><td><font color='blue'>ODV</font></td><td><font color='blue'>GOT</font></td><td><font color='blue'>ELISA</font></td></tr>";
           // System.out.println("This is try Block"+message1);
            message3="</table><table border=1 style=\"width:800px;font-family:calibri; font-size:16;\" cellspacing=0>";
            //System.out.println("This is try Block"+message3);
            for(TblPzIndentDetails indentDetails:listTblPzIndentDetails){
                
                
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
            //setIndentCrop(tblCropMaster.getCropName());
               // System.out.println("forloop Content");
                message2= message2 +"<tr><td>" + indentDetails.getIndentMatel() + " </td>"
                        + "<td> " + indentDetails.getIndentMatel()+ " - "+indentDetails.getIndentMateDesc()+" </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"
                    + "<td> " + indentDetails.getIndentLoteNum()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsRmQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsQty() + " </td>"
                    + "<td>" + indentDetails.getIndentStoLoc() + " </td>"
                    + "<td>" + indentDetails.getMatStl() + " </td>"
                    + "<td>" + indentDetails.getMatOdv() + " </td>"
                    + "<td>" + indentDetails.getMatGot() + " </td>"
                    + "<td>" + indentDetails.getMatElisa() + " </td></tr>";
                
            }
            message = "<table border=1 style=\"width:800px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#3399ff align=\"center\"><h3><font color='white'>Quality Accepted Indent</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblPzIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblPzIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    //+ "<td><font color='blue'>Company</font></td><td> " + tblPzIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblPzIndentMaster.getIndentFinalNumber() + " </td>"
                    
                    + "<tr><td><font color='blue'>Crop</font></td><td> " + tblCropMaster.getCropName() + " </td>"
                    + "<tr><td><font color='blue'>Packing Type</font></td><td> " + tblPzIndentMaster.getIndentPackProcessa()+ " </td>"
                    + "<tr><td><font color='blue'>Output Material</font></td><td> " + tblPzIndentMaster.getIndentOutmaterial() + " </td>"
                    + "<tr><td><font color='blue'>Start date</font></td><td> " + tblPzIndentMaster.getIndentStartdate() + " </td>"
                    + "<tr><td><font color='blue'>Batch Number</font></td><td> " + tblPzIndentMaster.getIndentBatchNumbera() + " </td>"
                    + "<tr><td><font color='blue'>UOM</font></td><td> " + tblPzIndentMaster.getIndentUom() + " </td>"
                    + "<tr><td><font color='blue'>Line Code</font></td><td> " + tblPzIndentMaster.getIndentLinecode()+ '-'+tblPzIndentMaster.getIndentLinedesc() + " </td>"
                    + "<tr><td><font color='blue'>Output Quantity</font></td><td> " + tblPzIndentMaster.getIndentOutqty() + " </td>"
                    + "<tr><td><font color='blue'>Created By</font></td><td> " + tblPzIndentMaster.getTblEmpMasterByIndentLmu().getEmpEmail() + " </td>"
                    //+ "<td><font color='blue'>Depertment</font></td><td> " + tblPzIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    //+ "<tr><td><font color='blue'>Section</font></td><td> " + tblPzIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    //+ "<td><font color='blue'>Plant</font></td><td> " + tblPzIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblPzIndentMaster.getIndentFinalRemarks()+ " </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>SPIMS</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>SPIMS</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
        //System.out.println("this is message"+message);
        }catch(Exception e){}
        //System.out.println("this is message"+message);
        return message;
    }
    
    
    
    
    public String getDeoProcessRequest(int req) {
        System.out.println("This is message request");
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
         // System.out.println("This is try Block");
            tblPzIndentMaster=(TbPzlIndentMastera) indentpzDao.getById(req);
           // System.out.println("execute 1");
            listTblPzIndentDetails=(ArrayList<TblPzIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=2 and tblIndentMaster.indentId=" + req + "");
           
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material</font></td><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>LotNumber</font></td><td><font color='blue'>Variety</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td><td><font color='blue'>Storage location</font></td></tr>";
            //System.out.println("This is try Block"+message1);
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
           // System.out.println("This is try Block"+message3);
            for(TblPzIndentDetails indentDetails:listTblPzIndentDetails){
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
                tblCropMaster.getCropName();
               // System.out.println("forloop Content");
                message2= message2 +"<tr><td>" + indentDetails.getIndentMatel() + " </td>"
                        + "<td> " + indentDetails.getIndentMatel()+ " - "+indentDetails.getIndentMateDesc()+" </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"
                    + "<td> " + indentDetails.getIndentLoteNum()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsRmQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsQty() + " </td>"
                    + "<td>" + indentDetails.getIndentStoLoc() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#3399ff align=\"center\"><h3><font color='white'>Indent DEO Order </font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblPzIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblPzIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    //+ "<td><font color='blue'>Company</font></td><td> " + tblPzIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Crop</font></td><td> " + tblCropMaster.getCropName() + " </td>"
                    + "<tr><td><font color='blue'>Packing Type</font></td><td> " + tblPzIndentMaster.getIndentPackProcessa()+ " </td>"
                    + "<tr><td><font color='blue'>Output Material</font></td><td> " + tblPzIndentMaster.getIndentOutmaterial() + " </td>"
                    + "<tr><td><font color='blue'>Start date</font></td><td> " + tblPzIndentMaster.getIndentStartdate() + " </td>"
                    + "<tr><td><font color='blue'>Batch Number</font></td><td> " + tblPzIndentMaster.getIndentBatchNumbera() + " </td>"
                    + "<tr><td><font color='blue'>UOM</font></td><td> " + tblPzIndentMaster.getIndentUom() + " </td>"
                    + "<tr><td><font color='blue'>Line Code</font></td><td> " + tblPzIndentMaster.getIndentLinecode() + '-'+tblPzIndentMaster.getIndentLinedesc() + " </td>"
                    + "<tr><td><font color='blue'>Output Quantity</font></td><td> " + tblPzIndentMaster.getIndentOutqty() + " </td>"
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblPzIndentMaster.getIndentFinalNumber()+ " </td>"
                    + "<tr><td><font color='blue'>Order Number</font></td><td> " + tblPzIndentMaster.getIndentPoNumber()+ " </td>"
                    + "<tr><td><font color='blue'>Modified By</font></td><td> " + tblPzIndentMaster.getTblEmpMasterByIndentLmu().getEmpEmail() + " </td>"
                    //+ "<td><font color='blue'>Depertment</font></td><td> " + tblPzIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    //+ "<tr><td><font color='blue'>Section</font></td><td> " + tblPzIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    //+ "<td><font color='blue'>Plant</font></td><td> " + tblPzIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblPzIndentMaster.getIndentFinalRemarks()+ " </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>SPIMS</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>SPIMS</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
       // System.out.println("this is message"+message);
        }catch(Exception e){}
      //  System.out.println("this is message"+message);
        return message;
    }
    
    
    public String getPzFlMangrInventoryRequest(int req) {
//        System.out.println("This is message request");
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
//            System.out.println("This is try Block");
            tblPzIndentMaster=(TbPzlIndentMastera) indentpzDao.getById(req);
//            System.out.println("execute 1");
            listTblPzIndentDetails=(ArrayList<TblPzIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=3 and tblIndentMaster.indentId=" + req + "");
//            System.out.println("execute 2"+listTblPzIndentDetails);
               
                
//            System.out.println("execute 2");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material</font></td><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>LotNumber</font></td><td><font color='blue'>Variety</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td><td><font color='blue'>Storage location</font></td></tr>";
//            System.out.println("This is try Block"+message1);
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
//            System.out.println("This is try Block"+message3);
            for(TblPzIndentDetails indentDetails:listTblPzIndentDetails){
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
                tblCropMaster.getCropName();
//                System.out.println("forloop Content");
                message2= message2 +"<tr><td>" + indentDetails.getIndentMatel() + " </td>"
                        + "<td> " + indentDetails.getIndentMatel()+ " - "+indentDetails.getIndentMateDesc()+" </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"
                    + "<td> " + indentDetails.getIndentLoteNum()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsRmQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsQty() + " </td>"
                    + "<td>" + indentDetails.getIndentStoLoc() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#3399ff align=\"center\"><h3><font color='white'>Indent Inventory issue </font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblPzIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblPzIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    //+ "<td><font color='blue'>Company</font></td><td> " + tblPzIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblPzIndentMaster.getIndentFinalNumber() + " </td>"
                    + "<tr><td><font color='blue'>Crop</font></td><td> " + tblCropMaster.getCropName() + " </td>"
                    + "<tr><td><font color='blue'>Packing Type</font></td><td> " + tblPzIndentMaster.getIndentPackProcessa()+ " </td>"
                    + "<tr><td><font color='blue'>Output Material</font></td><td> " + tblPzIndentMaster.getIndentOutmaterial() + " </td>"
                    + "<tr><td><font color='blue'>Start date</font></td><td> " + tblPzIndentMaster.getIndentStartdate() + " </td>"
                    + "<tr><td><font color='blue'>Batch Number</font></td><td> " + tblPzIndentMaster.getIndentBatchNumbera() + " </td>"
                    + "<tr><td><font color='blue'>UOM</font></td><td> " + tblPzIndentMaster.getIndentUom() + " </td>"
                    + "<tr><td><font color='blue'>Line Code</font></td><td> " + tblPzIndentMaster.getIndentLinecode() + '-'+tblPzIndentMaster.getIndentLinedesc() + " </td>"
                    + "<tr><td><font color='blue'>Output Quantity</font></td><td> " + tblPzIndentMaster.getIndentOutqty() + " </td>"
                    + "<tr><td><font color='blue'>Order Number</font></td><td> " + tblPzIndentMaster.getIndentPoNumber()+ " </td>"
                    + "<tr><td><font color='blue'>Modified By</font></td><td> " + tblPzIndentMaster.getTblEmpMasterByIndentLmu().getEmpEmail() + " </td>"
                    //+ "<td><font color='blue'>Depertment</font></td><td> " + tblPzIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    //+ "<tr><td><font color='blue'>Section</font></td><td> " + tblPzIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    //+ "<td><font color='blue'>Plant</font></td><td> " + tblPzIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblPzIndentMaster.getIndentRemarks()+ " </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>SPIMS</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>SPIMS</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
//        System.out.println("this is message"+message);
        }catch(Exception e){}
//        System.out.println("this is message"+message);
        return message;
    }
    
    
    
    
    public String getIssueConfirmationReq(int req) {
        System.out.println("This is message request");
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
           // System.out.println("This is try Block");
            tblPzIndentMaster=(TbPzlIndentMastera) indentpzDao.getById(req);
          //  System.out.println("execute 1");
            listTblPzIndentDetails=(ArrayList<TblPzIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=4 and tblIndentMaster.indentId=" + req + "");
        
                
         //   System.out.println("execute 2");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material</font></td><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>LotNumber</font></td><td><font color='blue'>Variety</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td><td><font color='blue'>Storage location</font></td></tr>";
          //  System.out.println("This is try Block"+message1);
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
         //   System.out.println("This is try Block"+message3);
            for(TblPzIndentDetails indentDetails:listTblPzIndentDetails){
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
                tblCropMaster.getCropName();
           //     System.out.println("forloop Content");
                message2= message2 +"<tr><td>" + indentDetails.getIndentMatel() + " </td>"
                        + "<td> " + indentDetails.getIndentMatel()+ " - "+indentDetails.getIndentMateDesc()+" </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"
                    + "<td> " + indentDetails.getIndentLoteNum()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsRmQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsQty() + " </td>"
                    + "<td>" + indentDetails.getIndentStoLoc() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#3399ff align=\"center\"><h3><font color='white'>Inventory issued Confirmation  </font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblPzIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblPzIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    //+ "<td><font color='blue'>Company</font></td><td> " + tblPzIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    
                    + "<tr><td><font color='blue'>Crop</font></td><td> " + tblCropMaster.getCropName() + " </td>"
                    + "<tr><td><font color='blue'>Packing Type</font></td><td> " + tblPzIndentMaster.getIndentPackProcessa()+ " </td>"
                    + "<tr><td><font color='blue'>Output Material</font></td><td> " + tblPzIndentMaster.getIndentOutmaterial() + " </td>"
                    + "<tr><td><font color='blue'>Start date</font></td><td> " + tblPzIndentMaster.getIndentStartdate() + " </td>"
                    + "<tr><td><font color='blue'>Batch Number</font></td><td> " + tblPzIndentMaster.getIndentBatchNumbera() + " </td>"
                    + "<tr><td><font color='blue'>UOM</font></td><td> " + tblPzIndentMaster.getIndentUom() + " </td>"
                    + "<tr><td><font color='blue'>Line Code</font></td><td> " + tblPzIndentMaster.getIndentLinecode() + '-'+tblPzIndentMaster.getIndentLinedesc() + " </td>"
                    + "<tr><td><font color='blue'>Output Quantity</font></td><td> " + tblPzIndentMaster.getIndentOutqty() + " </td>"
                    
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblPzIndentMaster.getIndentFinalNumber() + " </td>"
                    + "<tr><td><font color='blue'>Order Number</font></td><td> " + tblPzIndentMaster.getIndentPoNumber()+ " </td>"
                    + "<tr><td><font color='blue'>Expected Output Qty</font></td><td> " + tblPzIndentMaster.getIndentOutqty()+ " </td>"
                    + "<tr><td><font color='blue'>Modified By</font></td><td> " + tblPzIndentMaster.getTblEmpMasterByIndentLmu().getEmpEmail() + " </td>"
                    //+ "<tr><td><font color='blue'>Actual Output Qty</font></td><td> " + tblPzIndentMaster.getIndentActOutQty()+ " </td>"
                    //+ "<tr><td><font color='blue'>GRN Number</font></td><td> " + tblPzIndentMaster.getIndentGrnNumber()+ " </td>"
                    //+ "<td><font color='blue'>Depertment</font></td><td> " + tblPzIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    //+ "<tr><td><font color='blue'>Section</font></td><td> " + tblPzIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    //+ "<td><font color='blue'>Plant</font></td><td> " + tblPzIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblPzIndentMaster.getIndentRemarks()+ " </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>SPIMS</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>SPIMS</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
       // System.out.println("this is message"+message);
        }catch(Exception e){}
       // System.out.println("this is message"+message);
        return message;
    }
    
    
    
    
    
    public String getReceiptConfirmationReq(int req) {
       // System.out.println("This is message request");
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
          //  System.out.println("This is try Block");
            tblPzIndentMaster=(TbPzlIndentMastera) indentpzDao.getById(req);
         //   System.out.println("execute 1");
            listTblPzIndentDetails=(ArrayList<TblPzIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=6 and tblIndentMaster.indentId=" + req + "");

         //   System.out.println("execute 2");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material</font></td><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>LotNumber</font></td><td><font color='blue'>Variety</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td><td><font color='blue'>Storage location</font></td></tr>";
        //    System.out.println("This is try Block"+message1);
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
        //    System.out.println("This is try Block"+message3);
            for(TblPzIndentDetails indentDetails:listTblPzIndentDetails){
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
                tblCropMaster.getCropName();
        //        System.out.println("forloop Content");
                message2= message2 +"<tr><td>" + indentDetails.getIndentMatel() + " </td>"
                        + "<td> " + indentDetails.getIndentMatel()+ " - "+indentDetails.getIndentMateDesc()+" </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"
                    + "<td> " + indentDetails.getIndentLoteNum()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsRmQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsQty() + " </td>"
                    + "<td>" + indentDetails.getIndentStoLoc() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#3399ff align=\"center\"><h3><font color='white'>Goods Receipt Confirmation </font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblPzIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblPzIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    //+ "<td><font color='blue'>Company</font></td><td> " + tblPzIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    
                    + "<tr><td><font color='blue'>Crop</font></td><td> " + tblCropMaster.getCropName() + " </td>"
                    + "<tr><td><font color='blue'>Packing Type</font></td><td> " + tblPzIndentMaster.getIndentPackProcessa()+ " </td>"
                    + "<tr><td><font color='blue'>Output Material</font></td><td> " + tblPzIndentMaster.getIndentOutmaterial() + " </td>"
                    + "<tr><td><font color='blue'>Start date</font></td><td> " + tblPzIndentMaster.getIndentStartdate() + " </td>"
                    + "<tr><td><font color='blue'>Batch Number</font></td><td> " + tblPzIndentMaster.getIndentBatchNumbera() + " </td>"
                    + "<tr><td><font color='blue'>UOM</font></td><td> " + tblPzIndentMaster.getIndentUom() + " </td>"
                    + "<tr><td><font color='blue'>Line Code</font></td><td> " + tblPzIndentMaster.getIndentLinecode() + '-'+tblPzIndentMaster.getIndentLinedesc() + " </td>"
                    + "<tr><td><font color='blue'>Output Quantity</font></td><td> " + tblPzIndentMaster.getIndentOutqty() + " </td>"
                    
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblPzIndentMaster.getIndentFinalNumber() + " </td>"
                    + "<tr><td><font color='blue'>Order Number</font></td><td> " + tblPzIndentMaster.getIndentPoNumber()+ " </td>"
                    + "<tr><td><font color='blue'>Expected Output Qty</font></td><td> " + tblPzIndentMaster.getIndentOutqty()+ " </td>"
                    + "<tr><td><font color='blue'>Modified By</font></td><td> " + tblPzIndentMaster.getTblEmpMasterByIndentLmu().getEmpEmail() + " </td>"
                    //+ "<tr><td><font color='blue'>Actual Output Qty</font></td><td> " + tblPzIndentMaster.getIndentActOutQty()+ " </td>"
                    //+ "<tr><td><font color='blue'>GRN Number</font></td><td> " + tblPzIndentMaster.getIndentGrnNumber()+ " </td>"
                    //+ "<td><font color='blue'>Depertment</font></td><td> " + tblPzIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    //+ "<tr><td><font color='blue'>Section</font></td><td> " + tblPzIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    //+ "<td><font color='blue'>Plant</font></td><td> " + tblPzIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblPzIndentMaster.getIndentRemarks()+ " </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>SPIMS</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>SPIMS</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
       // System.out.println("this is message"+message);
        }catch(Exception e){}
      //  System.out.println("this is message"+message);
        return message;
    }
    
    
    
    
    
    
    
    public String getFlInchargeRequest(int req) {
       // System.out.println("This is message request");
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
         //   System.out.println("This is try Block");
            tblPzIndentMaster=(TbPzlIndentMastera) indentpzDao.getById(req);
         //   System.out.println("execute 1");
            listTblPzIndentDetails=(ArrayList<TblPzIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=5 and tblIndentMaster.indentId=" + req + "");
    
         //   System.out.println("execute 2");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material</font></td><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>LotNumber</font></td><td><font color='blue'>Variety</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td><td><font color='blue'>Storage location</font></td></tr>";
        //    System.out.println("This is try Block"+message1);
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
        //    System.out.println("This is try Block"+message3);
            for(TblPzIndentDetails indentDetails:listTblPzIndentDetails){
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
                tblCropMaster.getCropName();
           //     System.out.println("forloop Content");
                message2= message2 +"<tr><td>" + indentDetails.getIndentMatel() + " </td>"
                        + "<td> " + indentDetails.getIndentMatel()+ " - "+indentDetails.getIndentMateDesc()+" </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"
                    + "<td> " + indentDetails.getIndentLoteNum()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsRmQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsQty() + " </td>"
                    + "<td>" + indentDetails.getIndentStoLoc() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#3399ff align=\"center\"><h3><font color='white'>Indent Receipt </font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblPzIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblPzIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    //+ "<td><font color='blue'>Company</font></td><td> " + tblPzIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    
                    + "<tr><td><font color='blue'>Crop</font></td><td> " + tblCropMaster.getCropName() + " </td>"
                    + "<tr><td><font color='blue'>Packing Type</font></td><td> " + tblPzIndentMaster.getIndentPackProcessa()+ " </td>"
                    + "<tr><td><font color='blue'>Output Material</font></td><td> " + tblPzIndentMaster.getIndentOutmaterial() + " </td>"
                    + "<tr><td><font color='blue'>Start date</font></td><td> " + tblPzIndentMaster.getIndentStartdate() + " </td>"
                    + "<tr><td><font color='blue'>Batch Number</font></td><td> " + tblPzIndentMaster.getIndentBatchNumbera() + " </td>"
                    + "<tr><td><font color='blue'>UOM</font></td><td> " + tblPzIndentMaster.getIndentUom() + " </td>"
                    + "<tr><td><font color='blue'>Line Code</font></td><td> " + tblPzIndentMaster.getIndentLinecode() + '-'+tblPzIndentMaster.getIndentLinedesc() + " </td>"
                    + "<tr><td><font color='blue'>Output Quantity</font></td><td> " + tblPzIndentMaster.getIndentOutqty() + " </td>"
                    
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblPzIndentMaster.getIndentFinalNumber() + " </td>"
                    + "<tr><td><font color='blue'>Order Number</font></td><td> " + tblPzIndentMaster.getIndentPoNumber()+ " </td>"
                    + "<tr><td><font color='blue'>Expected Output Qty</font></td><td> " + tblPzIndentMaster.getIndentOutqty()+ " </td>"
                    + "<tr><td><font color='blue'>Actual Output Qty</font></td><td> " + tblPzIndentMaster.getIndentActOutQty()+ " </td>"
                    + "<tr><td><font color='blue'>Storage Location</font></td><td> " + tblPzIndentMaster.getIndentStoLocation() + " </td>"
                    + "<tr><td><font color='blue'>Modified By</font></td><td> " + tblPzIndentMaster.getTblEmpMasterByIndentLmu().getEmpEmail() + " </td>"
                    //+ "<tr><td><font color='blue'>Section</font></td><td> " + tblPzIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    //+ "<td><font color='blue'>Plant</font></td><td> " + tblPzIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblPzIndentMaster.getIndentFlInchargeCommants()+ " </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>SPIMS</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>SPIMS</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
      //  System.out.println("this is message"+message);
        }catch(Exception e){}
      //  System.out.println("this is message"+message);
        return message;
    }
    
    public String getGrNumberReq(int req) {
     //   System.out.println("This is message request");
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
          //  System.out.println("This is try Block");
            tblPzIndentMaster=(TbPzlIndentMastera) indentpzDao.getById(req);
         //   System.out.println("execute 1");
            listTblPzIndentDetails=(ArrayList<TblPzIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=7 and tblIndentMaster.indentId=" + req + "");
         //   System.out.println("execute 2"+listTblPzIndentDetails);
         //   System.out.println("execute 2");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material</font></td><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>LotNumber</font></td><td><font color='blue'>Variety</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td><td><font color='blue'>Storage location</font></td></tr>";
         //   System.out.println("This is try Block"+message1);
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
         //   System.out.println("This is try Block"+message3);
            for(TblPzIndentDetails indentDetails:listTblPzIndentDetails){
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
                tblCropMaster.getCropName();
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
         //       System.out.println("forloop Content");
                message2= message2 +"<tr><td>" + indentDetails.getIndentMatel() + " </td>"
                        + "<td> " + indentDetails.getIndentMatel()+ " - "+indentDetails.getIndentMateDesc()+" </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"
                    + "<td> " + indentDetails.getIndentLoteNum()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsRmQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsQty() + " </td>"
                    + "<td>" + indentDetails.getIndentStoLoc() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#3399ff align=\"center\"><h3><font color='white'>Indent Good Receipt Number </font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblPzIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblPzIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    //+ "<td><font color='blue'>Company</font></td><td> " + tblPzIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    
                    + "<tr><td><font color='blue'>Crop</font></td><td> " + tblCropMaster.getCropName() + " </td>"
                    + "<tr><td><font color='blue'>Packing Type</font></td><td> " + tblPzIndentMaster.getIndentPackProcessa()+ " </td>"
                    + "<tr><td><font color='blue'>Output Material</font></td><td> " + tblPzIndentMaster.getIndentOutmaterial() + " </td>"
                    + "<tr><td><font color='blue'>Start date</font></td><td> " + tblPzIndentMaster.getIndentStartdate() + " </td>"
                    + "<tr><td><font color='blue'>Batch Number</font></td><td> " + tblPzIndentMaster.getIndentBatchNumbera() + " </td>"
                    + "<tr><td><font color='blue'>UOM</font></td><td> " + tblPzIndentMaster.getIndentUom() + " </td>"
                    + "<tr><td><font color='blue'>Line Code</font></td><td> " + tblPzIndentMaster.getIndentLinecode() + '-'+tblPzIndentMaster.getIndentLinedesc() + " </td>"
                    + "<tr><td><font color='blue'>Output Quantity</font></td><td> " + tblPzIndentMaster.getIndentOutqty() + " </td>"
                    
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblPzIndentMaster.getIndentFinalNumber() + " </td>"
                    + "<tr><td><font color='blue'>Order Number</font></td><td> " + tblPzIndentMaster.getIndentPoNumber()+ " </td>"
                    + "<tr><td><font color='blue'>Expected Output Qty</font></td><td> " + tblPzIndentMaster.getIndentOutqty()+ " </td>"
                    + "<tr><td><font color='blue'>Actual Output Qty</font></td><td> " + tblPzIndentMaster.getIndentActOutQty()+ " </td>"
                    + "<tr><td><font color='blue'>GRN Number</font></td><td> " + tblPzIndentMaster.getIndentGrnNumber()+ " </td>"
                    + "<tr><td><font color='blue'>Storage Location</font></td><td> " + tblPzIndentMaster.getIndentStoLocation() + " </td>"
                    + "<tr><td><font color='blue'>Modified By</font></td><td> " + tblPzIndentMaster.getTblEmpMasterByIndentLmu().getEmpEmail() + " </td>"
                    //+ "<td><font color='blue'>Depertment</font></td><td> " + tblPzIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    //+ "<tr><td><font color='blue'>Section</font></td><td> " + tblPzIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    //+ "<td><font color='blue'>Plant</font></td><td> " + tblPzIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblPzIndentMaster.getIndentGrnCommants()+ " </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>SPIMS</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>SPIMS</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
       // System.out.println("this is message"+message);
        }catch(Exception e){}
      //  System.out.println("this is message"+message);
        return message;
    }
    
    
    
    
    
    public String getDeleteNotification(int req) {
      //  System.out.println("This is message request");
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            //System.out.println("This is try Block");
            tblPzIndentMaster=(TbPzlIndentMastera) indentpzDao.getById(req);
            //System.out.println("execute 1");
            listTblPzIndentDetails=(ArrayList<TblPzIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=0 and tblIndentMaster.indentId=" + req + "");
            //System.out.println("execute 2"+listTblPzIndentDetails);
            //System.out.println("execute 2");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material</font></td><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>LotNumber</font></td><td><font color='blue'>Variety</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td><td><font color='blue'>Storage location</font></td></tr>";
            //System.out.println("This is try Block"+message1);
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            //System.out.println("This is try Block"+message3);
            for(TblPzIndentDetails indentDetails:listTblPzIndentDetails){
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
                tblCropMaster.getCropName();
               // System.out.println("forloop Content");
                message2= message2 +"<tr><td>" + indentDetails.getIndentMatel() + " </td>"
                        + "<td> " + indentDetails.getIndentMatel()+ " - "+indentDetails.getIndentMateDesc()+" </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"
                    + "<td> " + indentDetails.getIndentLoteNum()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsRmQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsQty() + " </td>"
                    + "<td>" + indentDetails.getIndentStoLoc() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#ff3342 align=\"center\"><h3><font color='white'>Indent Rejected Notification </font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblPzIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblPzIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    //+ "<td><font color='blue'>Company</font></td><td> " + tblPzIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    
                    + "<tr><td><font color='blue'>Crop</font></td><td> " + tblCropMaster.getCropName()+ " </td>"
                    + "<tr><td><font color='blue'>Packing Type</font></td><td> " + tblPzIndentMaster.getIndentPackProcessa()+ " </td>"
                    + "<tr><td><font color='blue'>Output Material</font></td><td> " + tblPzIndentMaster.getIndentOutmaterial() + " </td>"
                    + "<tr><td><font color='blue'>Start date</font></td><td> " + tblPzIndentMaster.getIndentStartdate() + " </td>"
                    + "<tr><td><font color='blue'>Batch Number</font></td><td> " + tblPzIndentMaster.getIndentBatchNumbera() + " </td>"
                    + "<tr><td><font color='blue'>UOM</font></td><td> " + tblPzIndentMaster.getIndentUom() + " </td>"
                    + "<tr><td><font color='blue'>Line Code</font></td><td> " + tblPzIndentMaster.getIndentLinecode() + '-'+tblPzIndentMaster.getIndentLinedesc() + " </td>"
                    + "<tr><td><font color='blue'>Output Quantity</font></td><td> " + tblPzIndentMaster.getIndentOutqty() + " </td>"
                    
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblPzIndentMaster.getIndentFinalNumber() + " </td>"
                    //+ "<tr><td><font color='blue'>Order Number</font></td><td> " + tblPzIndentMaster.getIndentPoNumber()+ " </td>"
                    + "<tr><td><font color='blue'>Expected Output Qty</font></td><td> " + tblPzIndentMaster.getIndentOutqty()+ " </td>"
                    //+ "<tr><td><font color='blue'>Actual Output Qty</font></td><td> " + tblPzIndentMaster.getIndentActOutQty()+ " </td>"
                    //+ "<tr><td><font color='blue'>GRN Number</font></td><td> " + tblPzIndentMaster.getIndentGrnNumber()+ " </td>"
                    //+ "<tr><td><font color='blue'>Storage Location</font></td><td> " + tblPzIndentMaster.getIndentStoLocation() + " </td>"
                    + "<tr><td><font color='blue'>Rejected By</font></td><td> " + tblPzIndentMaster.getTblEmpMasterByIndentLmu().getEmpEmail() + " </td>"
                    //+ "<td><font color='blue'>Depertment</font></td><td> " + tblPzIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    //+ "<tr><td><font color='blue'>Section</font></td><td> " + tblPzIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    //+ "<td><font color='blue'>Plant</font></td><td> " + tblPzIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblPzIndentMaster.getIndentComments()+" </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>SPIMS</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>SPIMS</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
      //  System.out.println("this is message"+message);
        }catch(Exception e){}
      //  System.out.println("this is message"+message);
        return message;
    }
    
    
    public String getQuityDeleteNotification(int req) {
      //  System.out.println("This is message request");
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            //System.out.println("This is try Block");
            tblPzIndentMaster=(TbPzlIndentMastera) indentpzDao.getById(req);
            //System.out.println("execute 1");
            listTblPzIndentDetails=(ArrayList<TblPzIndentDetails>)indentPzDetailsDao.getList("where indentDetailsStatus=0 and tblIndentMaster.indentId=" + req + "");
            //System.out.println("execute 2"+listTblPzIndentDetails);
            //System.out.println("execute 2");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material</font></td><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>LotNumber</font></td><td><font color='blue'>Variety</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td><td><font color='blue'>Storage location</font></td></tr>";
            //System.out.println("This is try Block"+message1);
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            //System.out.println("This is try Block"+message3);
            for(TblPzIndentDetails indentDetails:listTblPzIndentDetails){
                tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblPzIndentMaster.getIndentCrop()).get(0);
                System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
                tblCropMaster.getCropName();
               // System.out.println("forloop Content");
                message2= message2 +"<tr><td>" + indentDetails.getIndentMatel() + " </td>"
                        + "<td> " + indentDetails.getIndentMatel()+ " - "+indentDetails.getIndentMateDesc()+" </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"
                    + "<td> " + indentDetails.getIndentLoteNum()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsRmQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsQty() + " </td>"
                    + "<td>" + indentDetails.getIndentStoLoc() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#ff3342 align=\"center\"><h3><font color='white'>Indent Quality Rejected Notification </font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblPzIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblPzIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    //+ "<td><font color='blue'>Company</font></td><td> " + tblPzIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    
                    + "<tr><td><font color='blue'>Crop</font></td><td> " + tblCropMaster.getCropName()+ " </td>"
                    + "<tr><td><font color='blue'>Packing Type</font></td><td> " + tblPzIndentMaster.getIndentPackProcessa()+ " </td>"
                    + "<tr><td><font color='blue'>Output Material</font></td><td> " + tblPzIndentMaster.getIndentOutmaterial() + " </td>"
                    + "<tr><td><font color='blue'>Start date</font></td><td> " + tblPzIndentMaster.getIndentStartdate() + " </td>"
                    + "<tr><td><font color='blue'>Batch Number</font></td><td> " + tblPzIndentMaster.getIndentBatchNumbera() + " </td>"
                    + "<tr><td><font color='blue'>UOM</font></td><td> " + tblPzIndentMaster.getIndentUom() + " </td>"
                    + "<tr><td><font color='blue'>Line Code</font></td><td> " + tblPzIndentMaster.getIndentLinecode() + '-'+tblPzIndentMaster.getIndentLinedesc() + " </td>"
                    + "<tr><td><font color='blue'>Output Quantity</font></td><td> " + tblPzIndentMaster.getIndentOutqty() + " </td>"
                    
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblPzIndentMaster.getIndentFinalNumber() + " </td>"
                    //+ "<tr><td><font color='blue'>Order Number</font></td><td> " + tblPzIndentMaster.getIndentPoNumber()+ " </td>"
                    + "<tr><td><font color='blue'>Expected Output Qty</font></td><td> " + tblPzIndentMaster.getIndentOutqty()+ " </td>"
                    //+ "<tr><td><font color='blue'>Actual Output Qty</font></td><td> " + tblPzIndentMaster.getIndentActOutQty()+ " </td>"
                    //+ "<tr><td><font color='blue'>GRN Number</font></td><td> " + tblPzIndentMaster.getIndentGrnNumber()+ " </td>"
                    //+ "<tr><td><font color='blue'>Storage Location</font></td><td> " + tblPzIndentMaster.getIndentStoLocation() + " </td>"
                    + "<tr><td><font color='blue'>Rejected By</font></td><td> " + tblPzIndentMaster.getTblEmpMasterByIndentLmu().getEmpEmail() + " </td>"
                    //+ "<td><font color='blue'>Depertment</font></td><td> " + tblPzIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    //+ "<tr><td><font color='blue'>Section</font></td><td> " + tblPzIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    //+ "<td><font color='blue'>Plant</font></td><td> " + tblPzIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " +tblPzIndentMaster.getIndentComments()+" </td></tr>"                   
                    + "</table><table>"
                    + "<tr><td></td></tr><tr><td>Please login to <a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'><font color='red'>SPIMS</font></a></td></tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td>Thanks & Regards</td></tr>"
                    + "<tr><td><a href='http://procurezone.nslgroup.in/ProcureZone/' target='_blank'>"
                    + "<font color='red'>SPIMS</font></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td>&nbsp;</td></tr><tr><td style='font-size:12px; color:#999999;'>This is a Computer-genarated e-mail, please do not reply to this message.</td></tr></table>";
      //  System.out.println("this is message"+message);
        }catch(Exception e){}
      //  System.out.println("this is message"+message);
        return message;
    }
    
    
    
    public String getApprovedIndentRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            tblIndentMaster=(TblIndentMaster)indentDao.getById(req);
            listTblIndentDetails=(ArrayList<TblIndentDetails>)indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>Project</font></td><td><font color='blue'>Vendor</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>RM Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIndentDetails indentDetails:listTblIndentDetails){
                message2= message2 +"<tr><td> " + indentDetails.getTblMaterialMaster().getMaterialName()+ " - "+indentDetails.getTblMaterialMaster().getMaterialDesc()+ " </td>"
                    + "<td> " + indentDetails.getTblUmoMaster().getUmoName()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsRmQty()+ "</td>"
                    + "<td>" + indentDetails.getIndentDetailsStockAval() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#00cc00 align=\"center\"><h3><font color='white'>Approved Indent Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblIndentMaster.getIndentYear() + " </td>"
                    + "<tr><td><font color='blue'>Date</font></td><td> " + tblIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblIndentMaster.getIndentNo() + " </td>"
                    + "<td><font color='blue'>Depertment</font></td><td> " + tblIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    + "<td><font color='blue'>Plant</font></td><td> " + tblIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentComments()+ " </td></tr>"                   
                    + "<tr><td colspan=1><font color='blue'>Approved By</font></td><td colspan=3> " + tblIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Date</font></td><td colspan=3> " + tblIndentMaster.getIndentApprovedbyDate() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentRemarks() + "</td></tr>"
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
    
    public String getRejectedIndentRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            tblIndentMaster=(TblIndentMaster)indentDao.getById(req);
            listTblIndentDetails=(ArrayList<TblIndentDetails>)indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>Project</font></td><td><font color='blue'>Vendor</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>RM Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIndentDetails indentDetails:listTblIndentDetails){
                message2= message2 +"<tr><td> " + indentDetails.getTblMaterialMaster().getMaterialName()+ " - "+indentDetails.getTblMaterialMaster().getMaterialDesc()+ " </td>"
                    + "<td> " + indentDetails.getTblUmoMaster().getUmoName()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsRmQty()+ "</td>"
                    + "<td>" + indentDetails.getIndentDetailsStockAval() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#00cc00 align=\"center\"><h3><font color='white'>Rejected Indent Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblIndentMaster.getIndentYear() + " </td>"
                    + "<td><font color='blue'>Date</font></td><td> " + tblIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblIndentMaster.getIndentNo() + " </td>"
                    + "<td><font color='blue'>Depertment</font></td><td> " + tblIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    + "<td><font color='blue'>Plant</font></td><td> " + tblIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentComments()+ " </td></tr>"                   
                    + "<tr><td colspan=1><font color='blue'>Rejected By</font></td><td colspan=3> " + tblIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Rejected Date</font></td><td colspan=3> " + tblIndentMaster.getIndentApprovedbyDate() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Rejected Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentRemarks() + "</td></tr>"
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
    
    public String getFinalApprovedIndentRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            tblIndentMaster=(TblIndentMaster)indentDao.getById(req);
            listTblIndentDetails=(ArrayList<TblIndentDetails>)indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>Project</font></td><td><font color='blue'>Vendor</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>RM Quantity Requested</font></td><td><font color='blue'>Dept Head Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIndentDetails indentDetails:listTblIndentDetails){
                message2= message2 +"<tr><td> " + indentDetails.getTblMaterialMaster().getMaterialName()+ " - "+indentDetails.getTblMaterialMaster().getMaterialDesc()+ " </td>"
                    + "<td> " + indentDetails.getTblUmoMaster().getUmoName()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsRmQty()+ "</td>"
                    + "<td>" + indentDetails.getIndentDetailsDeptQty()+ "</td>"
                    + "<td>" + indentDetails.getIndentDetailsStockAval() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#00cc00 align=\"center\"><h3><font color='white'>Final Approved Indent Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblIndentMaster.getIndentYear() + " </td>"
                    + "<td><font color='blue'>Date</font></td><td> " + tblIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblIndentMaster.getIndentNo() + " </td>"
                    + "<td><font color='blue'>Depertment</font></td><td> " + tblIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    + "<td><font color='blue'>Plant</font></td><td> " + tblIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentComments()+ " </td></tr>"                   
                    + "<tr><td colspan=1><font color='blue'>Approved By</font></td><td colspan=3> " + tblIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Date</font></td><td colspan=3> " + tblIndentMaster.getIndentApprovedbyDate() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentRemarks() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Final Approved By</font></td><td colspan=3> " + tblIndentMaster.getTblEmpMasterByIndentFinalApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Final Approved Date</font></td><td colspan=3> " + tblIndentMaster.getIndentFinalDate()+ "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Final Approved Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentFinalRemarks() + "</td></tr>"
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
    
    public String getFinalRejectedIndentRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        try {
            tblIndentMaster=(TblIndentMaster)indentDao.getById(req);
            listTblIndentDetails=(ArrayList<TblIndentDetails>)indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>Project</font></td><td><font color='blue'>Vendor</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>RM Quantity Requested</font></td><td><font color='blue'>Dept Head Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIndentDetails indentDetails:listTblIndentDetails){
                message2= message2 +"<tr><td> " + indentDetails.getTblMaterialMaster().getMaterialName()+ " - "+indentDetails.getTblMaterialMaster().getMaterialDesc()+ " </td>"
                    + "<td> " + indentDetails.getTblUmoMaster().getUmoName()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsRmQty()+ "</td>"
                    + "<td>" + indentDetails.getIndentDetailsDeptQty()+ "</td>"
                    + "<td>" + indentDetails.getIndentDetailsStockAval() + " </td></tr>";
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#00cc00 align=\"center\"><h3><font color='white'>Final Rejected Indent Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblIndentMaster.getIndentYear() + " </td>"
                    + "<td><font color='blue'>Date</font></td><td> " + tblIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblIndentMaster.getIndentNo() + " </td>"
                    + "<td><font color='blue'>Depertment</font></td><td> " + tblIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    + "<td><font color='blue'>Plant</font></td><td> " + tblIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentComments()+ " </td></tr>"                   
                    + "<tr><td colspan=1><font color='blue'>Approved By</font></td><td colspan=3> " + tblIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Date</font></td><td colspan=3> " + tblIndentMaster.getIndentApprovedbyDate() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentRemarks() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Final Rejected By</font></td><td colspan=3> " + tblIndentMaster.getTblEmpMasterByIndentFinalApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Final Rejected Date</font></td><td colspan=3> " + tblIndentMaster.getIndentFinalDate()+ "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Final Rejected Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentFinalRemarks() + "</td></tr>"
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
    
    public String getprocurementIndentRequest(int req) {
        String message = "";
        String message1 = "";
        String message2 = "";
        String message3 = "";
        String message4 = "";
        try {
            tblIndentMaster=(TblIndentMaster)indentDao.getById(req);
            listTblIndentDetails=(ArrayList<TblIndentDetails>)indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + req + "");
            message1="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td><font color='blue'>Material Description</font></td><td><font color='blue'>UMO</font></td><td><font color='blue'>Project</font></td><td><font color='blue'>Vendor</font></td><td><font color='blue'>Quantity Requested</font></td><td><font color='blue'>RM Quantity Requested</font></td><td><font color='blue'>Dept Head Quantity Requested</font></td><td><font color='blue'>Available Stock</font></td></tr>";
            message3="</table><table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0>";
            for(TblIndentDetails indentDetails:listTblIndentDetails){
                message2= message2 +"<tr><td> " + indentDetails.getTblMaterialMaster().getMaterialName()+ " - "+indentDetails.getTblMaterialMaster().getMaterialDesc()+ " </td>"
                    + "<td> " + indentDetails.getTblUmoMaster().getUmoName()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsPurpose()+ " </td>"
                    + "<td> " + indentDetails.getIndentDetailsVendor()+ " </td>"    
                    + "<td>" + indentDetails.getIndentDetailsQty() + "</td>"
                    + "<td>" + indentDetails.getIndentDetailsRmQty()+ "</td>"
                    + "<td>" + indentDetails.getIndentDetailsDeptQty()+ "</td>"
                    + "<td>" + indentDetails.getIndentDetailsStockAval() + " </td></tr>";
            }
            if(tblIndentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==7){
                message4="<tr><td colspan=1><font color='blue'>Delivery Date</font></td><td colspan=3> " + tblIndentMaster.getIndentDeliveryDate() + " </td></tr>"
                        + "<tr><td colspan=1><font color='blue'>PO Number</font></td><td colspan=3> " + tblIndentMaster.getIndentPoNumber() + " </td></tr>" ;                  
            }
            message = "<table border=1 style=\"width:600px;font-family:calibri; font-size:16;\" cellspacing=0><tr><td colspan=4 bgcolor=#00cc00 align=\"center\"><h3><font color='white'>Procurement Indent Request</font></h3></td></tr>"
                    + "<tr><td><font color='blue'>Year</font></td><td> " + tblIndentMaster.getIndentYear() + " </td>"
                    + "<td><font color='blue'>Date</font></td><td> " + tblIndentMaster.getIndentDate() + " </td></tr>"
                    + "<tr><td><font color='blue'>Employee</font></td><td>" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName()+ " ("+tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId()+")" + "</td>"
                    + "<td><font color='blue'>Company</font></td><td> " + tblIndentMaster.getTblCompanyMaster().getCompName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Indent No</font></td><td> " + tblIndentMaster.getIndentNo() + " </td>"
                    + "<td><font color='blue'>Depertment</font></td><td> " + tblIndentMaster.getTblDepartmentMaster().getDeptName() + " </td></tr>"
                    + "<tr><td><font color='blue'>Section</font></td><td> " + tblIndentMaster.getTblSectionMaster().getSecName() + " </td>"
                    + "<td><font color='blue'>Plant</font></td><td> " + tblIndentMaster.getTblPlantMaster().getPlantName() + " </td></tr>"
                    + message1
                    + message2
                    + message3                    
                    + "<tr><td colspan=1><font color='blue'>Comments/Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentComments()+ " </td></tr>"                   
                    + message4
                    + "<tr><td colspan=1><font color='blue'>Approved By</font></td><td colspan=3> " + tblIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Date</font></td><td colspan=3> " + tblIndentMaster.getIndentApprovedbyDate() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Approved Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentRemarks() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Final Approved By</font></td><td colspan=3> " + tblIndentMaster.getTblEmpMasterByIndentFinalApprovedby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Final Approved Date</font></td><td colspan=3> " + tblIndentMaster.getIndentFinalDate()+ "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Final Approved Remarks</font></td><td colspan=3> " + tblIndentMaster.getIndentFinalRemarks() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Procurement By</font></td><td colspan=3> " + tblIndentMaster.getTblEmpMasterByIndentProcurementby().getEmpName() + "</td></tr>"
                    + "<tr><td colspan=1><font color='blue'>Procurement Status</font></td><td colspan=3> " + tblIndentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusName() + "</td></tr>"
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
    
public static void main(String[] args) throws Exception {
        IndentMessage ms = new IndentMessage();
        //System.out.println("Message " + ms.getprocurementIndentRequest(2));
    }
}
