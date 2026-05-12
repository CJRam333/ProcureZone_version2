/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import plant.indent.action.plantEmpMapImpl;
import seeds.global.service.DaoFactory;


/**
 *
 * @author ramesh.a
 */
public class TblPzTblEmpPlantMap implements java.io.Serializable {
    
    private Integer Id;
     private TblEmpMaster tblEmpMaster;
     private TblCompanyMaster tblCompanyMaster;
     private TblPlantMaster tblPlantMaster;
     private TblRolesMaster tblRolesMaster;
     private int Status;
     private String roleStatus;
     

    public TblPzTblEmpPlantMap() {
    }

    public TblPzTblEmpPlantMap(Integer Id, TblEmpMaster tblEmpMaster, TblCompanyMaster tblCompanyMaster, TblPlantMaster tblPlantMaster, TblRolesMaster tblRolesMaster, int Status, String roleStatus) {
        this.Id = Id;
        this.tblEmpMaster = tblEmpMaster;
        this.tblCompanyMaster = tblCompanyMaster;
        this.tblPlantMaster = tblPlantMaster;
        this.tblRolesMaster = tblRolesMaster;
        this.Status = Status;
        this.roleStatus = roleStatus;
    }

    public TblPzTblEmpPlantMap(TblEmpMaster tblEmpMaster, TblCompanyMaster tblCompanyMaster, TblPlantMaster tblPlantMaster, TblRolesMaster tblRolesMaster, int Status, String roleStatus) {
        this.tblEmpMaster = tblEmpMaster;
        this.tblCompanyMaster = tblCompanyMaster;
        this.tblPlantMaster = tblPlantMaster;
        this.tblRolesMaster = tblRolesMaster;
        this.Status = Status;
        this.roleStatus = roleStatus;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer Id) {
        this.Id = Id;
    }

    public TblEmpMaster getTblEmpMaster() {
        return tblEmpMaster;
    }

    public void setTblEmpMaster(TblEmpMaster tblEmpMaster) {
        this.tblEmpMaster = tblEmpMaster;
    }

    public TblCompanyMaster getTblCompanyMaster() {
        return tblCompanyMaster;
    }

    public void setTblCompanyMaster(TblCompanyMaster tblCompanyMaster) {
        this.tblCompanyMaster = tblCompanyMaster;
    }

    public TblPlantMaster getTblPlantMaster() {
        return tblPlantMaster;
    }

    public void setTblPlantMaster(TblPlantMaster tblPlantMaster) {
        this.tblPlantMaster = tblPlantMaster;
    }

    public TblRolesMaster getTblRolesMaster() {
        return tblRolesMaster;
    }

    public void setTblRolesMaster(TblRolesMaster tblRolesMaster) {
        this.tblRolesMaster = tblRolesMaster;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int Status) {
        this.Status = Status;
    }

    public String getRoleStatus() {
        return roleStatus;
    }

    public void setRoleStatus(String roleStatus) {
        this.roleStatus = roleStatus;
    }

    

    
    

    
}
