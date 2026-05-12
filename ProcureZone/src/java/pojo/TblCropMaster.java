/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

/**
 *
 * @author raghuvamshi.a
 */
public class TblCropMaster {
    
     private Integer cropcId;
     private int cropId;
     private String cropName;
     private int divisionId;
     private String divisionName;
     private int cropStatus;

    public TblCropMaster() {
    }

    public TblCropMaster(Integer cropcId, int cropId, String cropName, int divisionId, String divisionName, int cropStatus) {
        this.cropcId = cropcId;
        this.cropId = cropId;
        this.cropName = cropName;
        this.divisionId = divisionId;
        this.divisionName = divisionName;
        this.cropStatus = cropStatus;
    }

    public TblCropMaster(int cropId, String cropName, int divisionId, String divisionName, int cropStatus) {
        this.cropId = cropId;
        this.cropName = cropName;
        this.divisionId = divisionId;
        this.divisionName = divisionName;
        this.cropStatus = cropStatus;
    }

    public Integer getCropcId() {
        return cropcId;
    }

    public void setCropcId(Integer cropcId) {
        this.cropcId = cropcId;
    }

    public int getCropId() {
        return cropId;
    }

    public void setCropId(int cropId) {
        this.cropId = cropId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public int getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(int divisionId) {
        this.divisionId = divisionId;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public int getCropStatus() {
        return cropStatus;
    }

    public void setCropStatus(int cropStatus) {
        this.cropStatus = cropStatus;
    }

    
     
    
    
     
    
}
