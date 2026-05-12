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
public class TblPlantLineCode implements java.io.Serializable{
    
    private Integer lineId;
     private Integer linePlant;
     private String lineActivtyType;
     private String lineCode;
     private String lineDesc;
     private String lineActDesc;
     private Integer lineStatus;

    public TblPlantLineCode() {
    }

    public TblPlantLineCode(Integer lineId, Integer linePlant, String lineActivtyType, String lineCode, String lineDesc, String lineActDesc, Integer lineStatus) {
        this.lineId = lineId;
        this.linePlant = linePlant;
        this.lineActivtyType = lineActivtyType;
        this.lineCode = lineCode;
        this.lineDesc = lineDesc;
        this.lineActDesc = lineActDesc;
        this.lineStatus = lineStatus;
    }

    public TblPlantLineCode(Integer linePlant, String lineActivtyType, String lineCode, String lineDesc, String lineActDesc, Integer lineStatus) {
        this.linePlant = linePlant;
        this.lineActivtyType = lineActivtyType;
        this.lineCode = lineCode;
        this.lineDesc = lineDesc;
        this.lineActDesc = lineActDesc;
        this.lineStatus = lineStatus;
    }

    public Integer getLineId() {
        return lineId;
    }

    public void setLineId(Integer lineId) {
        this.lineId = lineId;
    }

    public Integer getLinePlant() {
        return linePlant;
    }

    public void setLinePlant(Integer linePlant) {
        this.linePlant = linePlant;
    }

    public String getLineActivtyType() {
        return lineActivtyType;
    }

    public void setLineActivtyType(String lineActivtyType) {
        this.lineActivtyType = lineActivtyType;
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

    public String getLineActDesc() {
        return lineActDesc;
    }

    public void setLineActDesc(String lineActDesc) {
        this.lineActDesc = lineActDesc;
    }

    public Integer getLineStatus() {
        return lineStatus;
    }

    public void setLineStatus(Integer lineStatus) {
        this.lineStatus = lineStatus;
    }
     
     

    
}
