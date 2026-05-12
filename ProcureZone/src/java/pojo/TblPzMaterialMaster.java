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
public class TblPzMaterialMaster {
    
     private Integer materialId;
     private String materialCode;
     private String materialDesc;
     private String materialUom;
     private String materialVariety;
     private int materialPlant;
     private String materialGroup;
     private int materialStatus;
     private int materialPackType;
     private String plantName;

    public TblPzMaterialMaster() {
    }

    public TblPzMaterialMaster(Integer materialId, String materialCode, String materialDesc, String materialUom, String materialVariety, int materialPlant, String materialGroup, int materialStatus, int materialPackType, String plantName) {
        this.materialId = materialId;
        this.materialCode = materialCode;
        this.materialDesc = materialDesc;
        this.materialUom = materialUom;
        this.materialVariety = materialVariety;
        this.materialPlant = materialPlant;
        this.materialGroup = materialGroup;
        this.materialStatus = materialStatus;
        this.materialPackType = materialPackType;
        this.plantName = plantName;
    }

    public TblPzMaterialMaster(String materialCode, String materialDesc, String materialUom, String materialVariety, int materialPlant, String materialGroup, int materialStatus, int materialPackType, String plantName) {
        this.materialCode = materialCode;
        this.materialDesc = materialDesc;
        this.materialUom = materialUom;
        this.materialVariety = materialVariety;
        this.materialPlant = materialPlant;
        this.materialGroup = materialGroup;
        this.materialStatus = materialStatus;
        this.materialPackType = materialPackType;
        this.plantName = plantName;
    }

    public Integer getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Integer materialId) {
        this.materialId = materialId;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getMaterialDesc() {
        return materialDesc;
    }

    public void setMaterialDesc(String materialDesc) {
        this.materialDesc = materialDesc;
    }

    public String getMaterialUom() {
        return materialUom;
    }

    public void setMaterialUom(String materialUom) {
        this.materialUom = materialUom;
    }

    public String getMaterialVariety() {
        return materialVariety;
    }

    public void setMaterialVariety(String materialVariety) {
        this.materialVariety = materialVariety;
    }

    public int getMaterialPlant() {
        return materialPlant;
    }

    public void setMaterialPlant(int materialPlant) {
        this.materialPlant = materialPlant;
    }

    public String getMaterialGroup() {
        return materialGroup;
    }

    public void setMaterialGroup(String materialGroup) {
        this.materialGroup = materialGroup;
    }

    public int getMaterialStatus() {
        return materialStatus;
    }

    public void setMaterialStatus(int materialStatus) {
        this.materialStatus = materialStatus;
    }

    public int getMaterialPackType() {
        return materialPackType;
    }

    public void setMaterialPackType(int materialPackType) {
        this.materialPackType = materialPackType;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    
    
     
    
}
