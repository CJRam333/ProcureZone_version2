/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author ramesh.a
 */
public class TblPzMapCompanyPlantMaterial {
    private Integer mapId;
     private TblPzCompanyMaster tblCompanyMaster;
     private TblEmpMaster tblEmpMaster;
     private TblPzScheduleMaterialmaster tblMaterialMaster;
     private TblPlantMaster tblPlantMaster;
     private BigDecimal mapQuantityStores;
     private int mapStatus;
     private Date mapLmd;

    public TblPzMapCompanyPlantMaterial() {
    }

    public TblPzMapCompanyPlantMaterial(Integer mapId, TblPzCompanyMaster tblCompanyMaster, TblEmpMaster tblEmpMaster, TblPzScheduleMaterialmaster tblMaterialMaster, TblPlantMaster tblPlantMaster, BigDecimal mapQuantityStores, int mapStatus, Date mapLmd) {
        this.mapId = mapId;
        this.tblCompanyMaster = tblCompanyMaster;
        this.tblEmpMaster = tblEmpMaster;
        this.tblMaterialMaster = tblMaterialMaster;
        this.tblPlantMaster = tblPlantMaster;
        this.mapQuantityStores = mapQuantityStores;
        this.mapStatus = mapStatus;
        this.mapLmd = mapLmd;
    }

    public TblPzMapCompanyPlantMaterial(TblPzCompanyMaster tblCompanyMaster, TblEmpMaster tblEmpMaster, TblPzScheduleMaterialmaster tblMaterialMaster, TblPlantMaster tblPlantMaster, BigDecimal mapQuantityStores, int mapStatus, Date mapLmd) {
        this.tblCompanyMaster = tblCompanyMaster;
        this.tblEmpMaster = tblEmpMaster;
        this.tblMaterialMaster = tblMaterialMaster;
        this.tblPlantMaster = tblPlantMaster;
        this.mapQuantityStores = mapQuantityStores;
        this.mapStatus = mapStatus;
        this.mapLmd = mapLmd;
    }

    public Integer getMapId() {
        return mapId;
    }

    public void setMapId(Integer mapId) {
        this.mapId = mapId;
    }

    public TblPzCompanyMaster getTblCompanyMaster() {
        return tblCompanyMaster;
    }

    public void setTblCompanyMaster(TblPzCompanyMaster tblCompanyMaster) {
        this.tblCompanyMaster = tblCompanyMaster;
    }

    public TblEmpMaster getTblEmpMaster() {
        return tblEmpMaster;
    }

    public void setTblEmpMaster(TblEmpMaster tblEmpMaster) {
        this.tblEmpMaster = tblEmpMaster;
    }

    public TblPzScheduleMaterialmaster getTblMaterialMaster() {
        return tblMaterialMaster;
    }

    public void setTblMaterialMaster(TblPzScheduleMaterialmaster tblMaterialMaster) {
        this.tblMaterialMaster = tblMaterialMaster;
    }

    public TblPlantMaster getTblPlantMaster() {
        return tblPlantMaster;
    }

    public void setTblPlantMaster(TblPlantMaster tblPlantMaster) {
        this.tblPlantMaster = tblPlantMaster;
    }

    public BigDecimal getMapQuantityStores() {
        return mapQuantityStores;
    }

    public void setMapQuantityStores(BigDecimal mapQuantityStores) {
        this.mapQuantityStores = mapQuantityStores;
    }

    public int getMapStatus() {
        return mapStatus;
    }

    public void setMapStatus(int mapStatus) {
        this.mapStatus = mapStatus;
    }

    public Date getMapLmd() {
        return mapLmd;
    }

    public void setMapLmd(Date mapLmd) {
        this.mapLmd = mapLmd;
    }

    
     
    
}
