/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ramesh.a
 */
public class TbPzlIndentMastera implements java.io.Serializable {


     private Integer indentId;
     private TblPzCompanyMaster tblCompanyMaster;
     private TblDepartmentMaster tblDepartmentMaster;
     private TblEmpMaster tblEmpMasterByIndentEmp;
     private TblEmpMaster tblEmpMasterByIndentLmu;
     private TblEmpMaster tblEmpMasterByIndentFinalApprovedby;
     private TblEmpMaster tblEmpMasterByIndentProcurementby;
     private TblEmpMaster tblEmpMasterByIndentCreatedby;
     private TblEmpMaster tblEmpMasterByIndentApprovedby;
     private TblIndentStatus tblIndentStatusByIndentFinalStatus;
     private TblIndentStatus tblIndentStatusByIndentProcurementStatus;
     private TblIndentStatus tblIndentStatusByIndentApprovedStatus;
     private TblSectionMaster tblSectionMaster;
     private TblPlantMaster tblPlantMaster;
     private String indentNo;
     private String indentYear;
     private String indentDate;
     private String indentComments;
     private String indentDeliveryDate;
     private String indentPoNumber;
     private String indentApprovedbyDate;
     private String indentRemarks;
     private String indentFinalDate;
     private String indentFinalRemarks;
     private String IndentFinalNumber;
     private int indentCroptype;
     private String IndentCrop;
     private String indentProcpack;
     private String indentOutmaterial;
     private String indentStartdate;
     private String indentOutdesc;
     private String indentBatchnumber;
     private String indentUom;
     private String indentLinecode;
     private String indentLinedesc;
     private int indentOutqty;
     private int indentStatus;
     private Date indentLmd;
     
     private String indentOrderTypea;
     private String indentOrderTpDesc;
     private String indentBatchNumbera;
     
     private String indentPackProcessa;
     private BigDecimal indentActOutQty;
     private String IndentGrnNumber;
     
     private String indentFlInchargeCommants;
     private String indentGrnCommants;
     
     private String indentStoLocation;
     
     private Set tblIndentDetailses = new HashSet(0);
     private Set tblIndentProcurementLogses = new HashSet(0);

    public TbPzlIndentMastera() {
    }

    public TbPzlIndentMastera(Integer indentId, TblPzCompanyMaster tblCompanyMaster, TblDepartmentMaster tblDepartmentMaster, TblEmpMaster tblEmpMasterByIndentEmp, TblEmpMaster tblEmpMasterByIndentLmu, TblEmpMaster tblEmpMasterByIndentFinalApprovedby, TblEmpMaster tblEmpMasterByIndentProcurementby, TblEmpMaster tblEmpMasterByIndentCreatedby, TblEmpMaster tblEmpMasterByIndentApprovedby, TblIndentStatus tblIndentStatusByIndentFinalStatus, TblIndentStatus tblIndentStatusByIndentProcurementStatus, TblIndentStatus tblIndentStatusByIndentApprovedStatus, TblSectionMaster tblSectionMaster, TblPlantMaster tblPlantMaster, String indentNo, String indentYear, String indentDate, String indentComments, String indentDeliveryDate, String indentPoNumber, String indentApprovedbyDate, String indentRemarks, String indentFinalDate, String indentFinalRemarks, String IndentFinalNumber, int indentCroptype, String IndentCrop, String indentProcpack, String indentOutmaterial, String indentStartdate, String indentOutdesc, String indentBatchnumber, String indentUom, String indentLinecode, String indentLinedesc, int indentOutqty, int indentStatus, Date indentLmd, String indentOrderTypea, String indentOrderTpDesc, String indentBatchNumbera, String indentPackProcessa, BigDecimal indentActOutQty, String IndentGrnNumber, String indentFlInchargeCommants, String indentGrnCommants, String indentStoLocation) {
        this.indentId = indentId;
        this.tblCompanyMaster = tblCompanyMaster;
        this.tblDepartmentMaster = tblDepartmentMaster;
        this.tblEmpMasterByIndentEmp = tblEmpMasterByIndentEmp;
        this.tblEmpMasterByIndentLmu = tblEmpMasterByIndentLmu;
        this.tblEmpMasterByIndentFinalApprovedby = tblEmpMasterByIndentFinalApprovedby;
        this.tblEmpMasterByIndentProcurementby = tblEmpMasterByIndentProcurementby;
        this.tblEmpMasterByIndentCreatedby = tblEmpMasterByIndentCreatedby;
        this.tblEmpMasterByIndentApprovedby = tblEmpMasterByIndentApprovedby;
        this.tblIndentStatusByIndentFinalStatus = tblIndentStatusByIndentFinalStatus;
        this.tblIndentStatusByIndentProcurementStatus = tblIndentStatusByIndentProcurementStatus;
        this.tblIndentStatusByIndentApprovedStatus = tblIndentStatusByIndentApprovedStatus;
        this.tblSectionMaster = tblSectionMaster;
        this.tblPlantMaster = tblPlantMaster;
        this.indentNo = indentNo;
        this.indentYear = indentYear;
        this.indentDate = indentDate;
        this.indentComments = indentComments;
        this.indentDeliveryDate = indentDeliveryDate;
        this.indentPoNumber = indentPoNumber;
        this.indentApprovedbyDate = indentApprovedbyDate;
        this.indentRemarks = indentRemarks;
        this.indentFinalDate = indentFinalDate;
        this.indentFinalRemarks = indentFinalRemarks;
        this.IndentFinalNumber = IndentFinalNumber;
        this.indentCroptype = indentCroptype;
        this.IndentCrop = IndentCrop;
        this.indentProcpack = indentProcpack;
        this.indentOutmaterial = indentOutmaterial;
        this.indentStartdate = indentStartdate;
        this.indentOutdesc = indentOutdesc;
        this.indentBatchnumber = indentBatchnumber;
        this.indentUom = indentUom;
        this.indentLinecode = indentLinecode;
        this.indentLinedesc = indentLinedesc;
        this.indentOutqty = indentOutqty;
        this.indentStatus = indentStatus;
        this.indentLmd = indentLmd;
        this.indentOrderTypea = indentOrderTypea;
        this.indentOrderTpDesc = indentOrderTpDesc;
        this.indentBatchNumbera = indentBatchNumbera;
        this.indentPackProcessa = indentPackProcessa;
        this.indentActOutQty = indentActOutQty;
        this.IndentGrnNumber = IndentGrnNumber;
        this.indentFlInchargeCommants = indentFlInchargeCommants;
        this.indentGrnCommants = indentGrnCommants;
        this.indentStoLocation = indentStoLocation;
    }

    public TbPzlIndentMastera(TblPzCompanyMaster tblCompanyMaster, TblDepartmentMaster tblDepartmentMaster, TblEmpMaster tblEmpMasterByIndentEmp, TblEmpMaster tblEmpMasterByIndentLmu, TblEmpMaster tblEmpMasterByIndentFinalApprovedby, TblEmpMaster tblEmpMasterByIndentProcurementby, TblEmpMaster tblEmpMasterByIndentCreatedby, TblEmpMaster tblEmpMasterByIndentApprovedby, TblIndentStatus tblIndentStatusByIndentFinalStatus, TblIndentStatus tblIndentStatusByIndentProcurementStatus, TblIndentStatus tblIndentStatusByIndentApprovedStatus, TblSectionMaster tblSectionMaster, TblPlantMaster tblPlantMaster, String indentNo, String indentYear, String indentDate, String indentComments, String indentDeliveryDate, String indentPoNumber, String indentApprovedbyDate, String indentRemarks, String indentFinalDate, String indentFinalRemarks, String IndentFinalNumber, int indentCroptype, String IndentCrop, String indentProcpack, String indentOutmaterial, String indentStartdate, String indentOutdesc, String indentBatchnumber, String indentUom, String indentLinecode, String indentLinedesc, int indentOutqty, int indentStatus, Date indentLmd, String indentOrderTypea, String indentOrderTpDesc, String indentBatchNumbera, String indentPackProcessa, BigDecimal indentActOutQty, String IndentGrnNumber, String indentFlInchargeCommants, String indentGrnCommants, String indentStoLocation) {
        this.tblCompanyMaster = tblCompanyMaster;
        this.tblDepartmentMaster = tblDepartmentMaster;
        this.tblEmpMasterByIndentEmp = tblEmpMasterByIndentEmp;
        this.tblEmpMasterByIndentLmu = tblEmpMasterByIndentLmu;
        this.tblEmpMasterByIndentFinalApprovedby = tblEmpMasterByIndentFinalApprovedby;
        this.tblEmpMasterByIndentProcurementby = tblEmpMasterByIndentProcurementby;
        this.tblEmpMasterByIndentCreatedby = tblEmpMasterByIndentCreatedby;
        this.tblEmpMasterByIndentApprovedby = tblEmpMasterByIndentApprovedby;
        this.tblIndentStatusByIndentFinalStatus = tblIndentStatusByIndentFinalStatus;
        this.tblIndentStatusByIndentProcurementStatus = tblIndentStatusByIndentProcurementStatus;
        this.tblIndentStatusByIndentApprovedStatus = tblIndentStatusByIndentApprovedStatus;
        this.tblSectionMaster = tblSectionMaster;
        this.tblPlantMaster = tblPlantMaster;
        this.indentNo = indentNo;
        this.indentYear = indentYear;
        this.indentDate = indentDate;
        this.indentComments = indentComments;
        this.indentDeliveryDate = indentDeliveryDate;
        this.indentPoNumber = indentPoNumber;
        this.indentApprovedbyDate = indentApprovedbyDate;
        this.indentRemarks = indentRemarks;
        this.indentFinalDate = indentFinalDate;
        this.indentFinalRemarks = indentFinalRemarks;
        this.IndentFinalNumber = IndentFinalNumber;
        this.indentCroptype = indentCroptype;
        this.IndentCrop = IndentCrop;
        this.indentProcpack = indentProcpack;
        this.indentOutmaterial = indentOutmaterial;
        this.indentStartdate = indentStartdate;
        this.indentOutdesc = indentOutdesc;
        this.indentBatchnumber = indentBatchnumber;
        this.indentUom = indentUom;
        this.indentLinecode = indentLinecode;
        this.indentLinedesc = indentLinedesc;
        this.indentOutqty = indentOutqty;
        this.indentStatus = indentStatus;
        this.indentLmd = indentLmd;
        this.indentOrderTypea = indentOrderTypea;
        this.indentOrderTpDesc = indentOrderTpDesc;
        this.indentBatchNumbera = indentBatchNumbera;
        this.indentPackProcessa = indentPackProcessa;
        this.indentActOutQty = indentActOutQty;
        this.IndentGrnNumber = IndentGrnNumber;
        this.indentFlInchargeCommants = indentFlInchargeCommants;
        this.indentGrnCommants = indentGrnCommants;
        this.indentStoLocation = indentStoLocation;
    }

    public Integer getIndentId() {
        return indentId;
    }

    public void setIndentId(Integer indentId) {
        this.indentId = indentId;
    }

    public TblPzCompanyMaster getTblCompanyMaster() {
        return tblCompanyMaster;
    }

    public void setTblCompanyMaster(TblPzCompanyMaster tblCompanyMaster) {
        this.tblCompanyMaster = tblCompanyMaster;
    }

    public TblDepartmentMaster getTblDepartmentMaster() {
        return tblDepartmentMaster;
    }

    public void setTblDepartmentMaster(TblDepartmentMaster tblDepartmentMaster) {
        this.tblDepartmentMaster = tblDepartmentMaster;
    }

    public TblEmpMaster getTblEmpMasterByIndentEmp() {
        return tblEmpMasterByIndentEmp;
    }

    public void setTblEmpMasterByIndentEmp(TblEmpMaster tblEmpMasterByIndentEmp) {
        this.tblEmpMasterByIndentEmp = tblEmpMasterByIndentEmp;
    }

    public TblEmpMaster getTblEmpMasterByIndentLmu() {
        return tblEmpMasterByIndentLmu;
    }

    public void setTblEmpMasterByIndentLmu(TblEmpMaster tblEmpMasterByIndentLmu) {
        this.tblEmpMasterByIndentLmu = tblEmpMasterByIndentLmu;
    }

    public TblEmpMaster getTblEmpMasterByIndentFinalApprovedby() {
        return tblEmpMasterByIndentFinalApprovedby;
    }

    public void setTblEmpMasterByIndentFinalApprovedby(TblEmpMaster tblEmpMasterByIndentFinalApprovedby) {
        this.tblEmpMasterByIndentFinalApprovedby = tblEmpMasterByIndentFinalApprovedby;
    }

    public TblEmpMaster getTblEmpMasterByIndentProcurementby() {
        return tblEmpMasterByIndentProcurementby;
    }

    public void setTblEmpMasterByIndentProcurementby(TblEmpMaster tblEmpMasterByIndentProcurementby) {
        this.tblEmpMasterByIndentProcurementby = tblEmpMasterByIndentProcurementby;
    }

    public TblEmpMaster getTblEmpMasterByIndentCreatedby() {
        return tblEmpMasterByIndentCreatedby;
    }

    public void setTblEmpMasterByIndentCreatedby(TblEmpMaster tblEmpMasterByIndentCreatedby) {
        this.tblEmpMasterByIndentCreatedby = tblEmpMasterByIndentCreatedby;
    }

    public TblEmpMaster getTblEmpMasterByIndentApprovedby() {
        return tblEmpMasterByIndentApprovedby;
    }

    public void setTblEmpMasterByIndentApprovedby(TblEmpMaster tblEmpMasterByIndentApprovedby) {
        this.tblEmpMasterByIndentApprovedby = tblEmpMasterByIndentApprovedby;
    }

    public TblIndentStatus getTblIndentStatusByIndentFinalStatus() {
        return tblIndentStatusByIndentFinalStatus;
    }

    public void setTblIndentStatusByIndentFinalStatus(TblIndentStatus tblIndentStatusByIndentFinalStatus) {
        this.tblIndentStatusByIndentFinalStatus = tblIndentStatusByIndentFinalStatus;
    }

    public TblIndentStatus getTblIndentStatusByIndentProcurementStatus() {
        return tblIndentStatusByIndentProcurementStatus;
    }

    public void setTblIndentStatusByIndentProcurementStatus(TblIndentStatus tblIndentStatusByIndentProcurementStatus) {
        this.tblIndentStatusByIndentProcurementStatus = tblIndentStatusByIndentProcurementStatus;
    }

    public TblIndentStatus getTblIndentStatusByIndentApprovedStatus() {
        return tblIndentStatusByIndentApprovedStatus;
    }

    public void setTblIndentStatusByIndentApprovedStatus(TblIndentStatus tblIndentStatusByIndentApprovedStatus) {
        this.tblIndentStatusByIndentApprovedStatus = tblIndentStatusByIndentApprovedStatus;
    }

    public TblSectionMaster getTblSectionMaster() {
        return tblSectionMaster;
    }

    public void setTblSectionMaster(TblSectionMaster tblSectionMaster) {
        this.tblSectionMaster = tblSectionMaster;
    }

    public TblPlantMaster getTblPlantMaster() {
        return tblPlantMaster;
    }

    public void setTblPlantMaster(TblPlantMaster tblPlantMaster) {
        this.tblPlantMaster = tblPlantMaster;
    }

    public String getIndentNo() {
        return indentNo;
    }

    public void setIndentNo(String indentNo) {
        this.indentNo = indentNo;
    }

    public String getIndentYear() {
        return indentYear;
    }

    public void setIndentYear(String indentYear) {
        this.indentYear = indentYear;
    }

    public String getIndentDate() {
        return indentDate;
    }

    public void setIndentDate(String indentDate) {
        this.indentDate = indentDate;
    }

    public String getIndentComments() {
        return indentComments;
    }

    public void setIndentComments(String indentComments) {
        this.indentComments = indentComments;
    }

    public String getIndentDeliveryDate() {
        return indentDeliveryDate;
    }

    public void setIndentDeliveryDate(String indentDeliveryDate) {
        this.indentDeliveryDate = indentDeliveryDate;
    }

    public String getIndentPoNumber() {
        return indentPoNumber;
    }

    public void setIndentPoNumber(String indentPoNumber) {
        this.indentPoNumber = indentPoNumber;
    }

    public String getIndentApprovedbyDate() {
        return indentApprovedbyDate;
    }

    public void setIndentApprovedbyDate(String indentApprovedbyDate) {
        this.indentApprovedbyDate = indentApprovedbyDate;
    }

    public String getIndentRemarks() {
        return indentRemarks;
    }

    public void setIndentRemarks(String indentRemarks) {
        this.indentRemarks = indentRemarks;
    }

    public String getIndentFinalDate() {
        return indentFinalDate;
    }

    public void setIndentFinalDate(String indentFinalDate) {
        this.indentFinalDate = indentFinalDate;
    }

    public String getIndentFinalRemarks() {
        return indentFinalRemarks;
    }

    public void setIndentFinalRemarks(String indentFinalRemarks) {
        this.indentFinalRemarks = indentFinalRemarks;
    }

    public String getIndentFinalNumber() {
        return IndentFinalNumber;
    }

    public void setIndentFinalNumber(String IndentFinalNumber) {
        this.IndentFinalNumber = IndentFinalNumber;
    }

    public int getIndentCroptype() {
        return indentCroptype;
    }

    public void setIndentCroptype(int indentCroptype) {
        this.indentCroptype = indentCroptype;
    }

    public String getIndentCrop() {
        return IndentCrop;
    }

    public void setIndentCrop(String IndentCrop) {
        this.IndentCrop = IndentCrop;
    }

    public String getIndentProcpack() {
        return indentProcpack;
    }

    public void setIndentProcpack(String indentProcpack) {
        this.indentProcpack = indentProcpack;
    }

    public String getIndentOutmaterial() {
        return indentOutmaterial;
    }

    public void setIndentOutmaterial(String indentOutmaterial) {
        this.indentOutmaterial = indentOutmaterial;
    }

    public String getIndentStartdate() {
        return indentStartdate;
    }

    public void setIndentStartdate(String indentStartdate) {
        this.indentStartdate = indentStartdate;
    }

    public String getIndentOutdesc() {
        return indentOutdesc;
    }

    public void setIndentOutdesc(String indentOutdesc) {
        this.indentOutdesc = indentOutdesc;
    }

    public String getIndentBatchnumber() {
        return indentBatchnumber;
    }

    public void setIndentBatchnumber(String indentBatchnumber) {
        this.indentBatchnumber = indentBatchnumber;
    }

    public String getIndentUom() {
        return indentUom;
    }

    public void setIndentUom(String indentUom) {
        this.indentUom = indentUom;
    }

    public String getIndentLinecode() {
        return indentLinecode;
    }

    public void setIndentLinecode(String indentLinecode) {
        this.indentLinecode = indentLinecode;
    }

    public String getIndentLinedesc() {
        return indentLinedesc;
    }

    public void setIndentLinedesc(String indentLinedesc) {
        this.indentLinedesc = indentLinedesc;
    }

    public int getIndentOutqty() {
        return indentOutqty;
    }

    public void setIndentOutqty(int indentOutqty) {
        this.indentOutqty = indentOutqty;
    }

    public int getIndentStatus() {
        return indentStatus;
    }

    public void setIndentStatus(int indentStatus) {
        this.indentStatus = indentStatus;
    }

    public Date getIndentLmd() {
        return indentLmd;
    }

    public void setIndentLmd(Date indentLmd) {
        this.indentLmd = indentLmd;
    }

    public String getIndentOrderTypea() {
        return indentOrderTypea;
    }

    public void setIndentOrderTypea(String indentOrderTypea) {
        this.indentOrderTypea = indentOrderTypea;
    }

    public String getIndentOrderTpDesc() {
        return indentOrderTpDesc;
    }

    public void setIndentOrderTpDesc(String indentOrderTpDesc) {
        this.indentOrderTpDesc = indentOrderTpDesc;
    }

    public String getIndentBatchNumbera() {
        return indentBatchNumbera;
    }

    public void setIndentBatchNumbera(String indentBatchNumbera) {
        this.indentBatchNumbera = indentBatchNumbera;
    }

    public String getIndentPackProcessa() {
        return indentPackProcessa;
    }

    public void setIndentPackProcessa(String indentPackProcessa) {
        this.indentPackProcessa = indentPackProcessa;
    }

    public BigDecimal getIndentActOutQty() {
        return indentActOutQty;
    }

    public void setIndentActOutQty(BigDecimal indentActOutQty) {
        this.indentActOutQty = indentActOutQty;
    }

    public String getIndentGrnNumber() {
        return IndentGrnNumber;
    }

    public void setIndentGrnNumber(String IndentGrnNumber) {
        this.IndentGrnNumber = IndentGrnNumber;
    }

    public String getIndentFlInchargeCommants() {
        return indentFlInchargeCommants;
    }

    public void setIndentFlInchargeCommants(String indentFlInchargeCommants) {
        this.indentFlInchargeCommants = indentFlInchargeCommants;
    }

    public String getIndentGrnCommants() {
        return indentGrnCommants;
    }

    public void setIndentGrnCommants(String indentGrnCommants) {
        this.indentGrnCommants = indentGrnCommants;
    }

    public String getIndentStoLocation() {
        return indentStoLocation;
    }

    public void setIndentStoLocation(String indentStoLocation) {
        this.indentStoLocation = indentStoLocation;
    }

    public Set getTblIndentDetailses() {
        return tblIndentDetailses;
    }

    public void setTblIndentDetailses(Set tblIndentDetailses) {
        this.tblIndentDetailses = tblIndentDetailses;
    }

    public Set getTblIndentProcurementLogses() {
        return tblIndentProcurementLogses;
    }

    public void setTblIndentProcurementLogses(Set tblIndentProcurementLogses) {
        this.tblIndentProcurementLogses = tblIndentProcurementLogses;
    }

    

    
    
    
}
