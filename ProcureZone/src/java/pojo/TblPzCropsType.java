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
public class TblPzCropsType {
    
    private Integer cropsId;
     private String cropName;
     private int divisionCode;
     private int divisionId;
     private int divisionStatus;

    public TblPzCropsType() {
    }

    public TblPzCropsType(Integer cropsId, String cropName, int divisionCode, int divisionId, int divisionStatus) {
        this.cropsId = cropsId;
        this.cropName = cropName;
        this.divisionCode = divisionCode;
        this.divisionId = divisionId;
        this.divisionStatus = divisionStatus;
    }

    public TblPzCropsType(String cropName, int divisionCode, int divisionId, int divisionStatus) {
        this.cropName = cropName;
        this.divisionCode = divisionCode;
        this.divisionId = divisionId;
        this.divisionStatus = divisionStatus;
    }

    public Integer getCropsId() {
        return cropsId;
    }

    public void setCropsId(Integer cropsId) {
        this.cropsId = cropsId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public int getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(int divisionCode) {
        this.divisionCode = divisionCode;
    }

    public int getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(int divisionId) {
        this.divisionId = divisionId;
    }

    public int getDivisionStatus() {
        return divisionStatus;
    }

    public void setDivisionStatus(int divisionStatus) {
        this.divisionStatus = divisionStatus;
    }
     
     
     
     
    
}
