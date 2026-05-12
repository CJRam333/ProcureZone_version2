/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

/**
 *
 * @author ramesh.a
 */
public class TbPzlIndentMaster  implements java.io.Serializable {
    
private Integer indentId;
private Integer indentCode;
private String empNo;
private Integer empPlant;
private String indentNo;
private String indentDate;
private String indentRemarks;

private String cropType;
private String indCrop;
private String packPross;
private String outMaterial;
private String outDesc;
private Integer batchNumber;
private String masterUom;
private String lineCode;
private String lineDesc;
private String expoutQty;
private Integer indentStatus;

    public TbPzlIndentMaster() {
    }

    public TbPzlIndentMaster(Integer indentId, Integer indentCode, String empNo, Integer empPlant, String indentNo, String indentDate, String indentRemarks, String cropType, String indCrop, String packPross, String outMaterial, String outDesc, Integer batchNumber, String masterUom, String lineCode, String lineDesc, String expoutQty, Integer indentStatus) {
        this.indentId = indentId;
        this.indentCode = indentCode;
        this.empNo = empNo;
        this.empPlant = empPlant;
        this.indentNo = indentNo;
        this.indentDate = indentDate;
        this.indentRemarks = indentRemarks;
        this.cropType = cropType;
        this.indCrop = indCrop;
        this.packPross = packPross;
        this.outMaterial = outMaterial;
        this.outDesc = outDesc;
        this.batchNumber = batchNumber;
        this.masterUom = masterUom;
        this.lineCode = lineCode;
        this.lineDesc = lineDesc;
        this.expoutQty = expoutQty;
        this.indentStatus = indentStatus;
    }

    public TbPzlIndentMaster(Integer indentCode, String empNo, Integer empPlant, String indentNo, String indentDate, String indentRemarks, String cropType, String indCrop, String packPross, String outMaterial, String outDesc, Integer batchNumber, String masterUom, String lineCode, String lineDesc, String expoutQty, Integer indentStatus) {
        this.indentCode = indentCode;
        this.empNo = empNo;
        this.empPlant = empPlant;
        this.indentNo = indentNo;
        this.indentDate = indentDate;
        this.indentRemarks = indentRemarks;
        this.cropType = cropType;
        this.indCrop = indCrop;
        this.packPross = packPross;
        this.outMaterial = outMaterial;
        this.outDesc = outDesc;
        this.batchNumber = batchNumber;
        this.masterUom = masterUom;
        this.lineCode = lineCode;
        this.lineDesc = lineDesc;
        this.expoutQty = expoutQty;
        this.indentStatus = indentStatus;
    }

    public Integer getIndentId() {
        return indentId;
    }

    public void setIndentId(Integer indentId) {
        this.indentId = indentId;
    }

    public Integer getIndentCode() {
        return indentCode;
    }

    public void setIndentCode(Integer indentCode) {
        this.indentCode = indentCode;
    }

    public String getEmpNo() {
        return empNo;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public Integer getEmpPlant() {
        return empPlant;
    }

    public void setEmpPlant(Integer empPlant) {
        this.empPlant = empPlant;
    }

    public String getIndentNo() {
        return indentNo;
    }

    public void setIndentNo(String indentNo) {
        this.indentNo = indentNo;
    }

    public String getIndentDate() {
        return indentDate;
    }

    public void setIndentDate(String indentDate) {
        this.indentDate = indentDate;
    }

    public String getIndentRemarks() {
        return indentRemarks;
    }

    public void setIndentRemarks(String indentRemarks) {
        this.indentRemarks = indentRemarks;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public String getIndCrop() {
        return indCrop;
    }

    public void setIndCrop(String indCrop) {
        this.indCrop = indCrop;
    }

    public String getPackPross() {
        return packPross;
    }

    public void setPackPross(String packPross) {
        this.packPross = packPross;
    }

    public String getOutMaterial() {
        return outMaterial;
    }

    public void setOutMaterial(String outMaterial) {
        this.outMaterial = outMaterial;
    }

    public String getOutDesc() {
        return outDesc;
    }

    public void setOutDesc(String outDesc) {
        this.outDesc = outDesc;
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getMasterUom() {
        return masterUom;
    }

    public void setMasterUom(String masterUom) {
        this.masterUom = masterUom;
    }

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    public String getLineDesc() {
        return lineDesc;
    }

    public void setLineDesc(String lineDesc) {
        this.lineDesc = lineDesc;
    }

    public String getExpoutQty() {
        return expoutQty;
    }

    public void setExpoutQty(String expoutQty) {
        this.expoutQty = expoutQty;
    }

    public Integer getIndentStatus() {
        return indentStatus;
    }

    public void setIndentStatus(Integer indentStatus) {
        this.indentStatus = indentStatus;
    }

    

    
}
