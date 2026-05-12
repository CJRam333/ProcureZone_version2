/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import java.util.Date;

/**
 *
 * @author ramesh.a
 */
public class TblPzPlantStoLoction implements java.io.Serializable  {
    
    private int plantId;
     private int plantCode;
     private String plantName;
     private String stoLoc;
     private String stoLocDesc;
     private int plantStatus;

    public TblPzPlantStoLoction() {
    }

    public TblPzPlantStoLoction(int plantId, int plantCode, String plantName, String stoLoc, String stoLocDesc, int plantStatus) {
        this.plantId = plantId;
        this.plantCode = plantCode;
        this.plantName = plantName;
        this.stoLoc = stoLoc;
        this.stoLocDesc = stoLocDesc;
        this.plantStatus = plantStatus;
    }

    public TblPzPlantStoLoction(int plantCode, String plantName, String stoLoc, String stoLocDesc, int plantStatus) {
        this.plantCode = plantCode;
        this.plantName = plantName;
        this.stoLoc = stoLoc;
        this.stoLocDesc = stoLocDesc;
        this.plantStatus = plantStatus;
    }

    public int getPlantId() {
        return plantId;
    }

    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public int getPlantCode() {
        return plantCode;
    }

    public void setPlantCode(int plantCode) {
        this.plantCode = plantCode;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getStoLoc() {
        return stoLoc;
    }

    public void setStoLoc(String stoLoc) {
        this.stoLoc = stoLoc;
    }

    public String getStoLocDesc() {
        return stoLocDesc;
    }

    public void setStoLocDesc(String stoLocDesc) {
        this.stoLocDesc = stoLocDesc;
    }

    public int getPlantStatus() {
        return plantStatus;
    }

    public void setPlantStatus(int plantStatus) {
        this.plantStatus = plantStatus;
    }

    
    
    

    

     
     
    
}
