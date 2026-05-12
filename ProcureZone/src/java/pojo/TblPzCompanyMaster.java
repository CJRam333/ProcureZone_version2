/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ramesh.a
 */
public class TblPzCompanyMaster {
    
    private Integer compId;
     private TblEmpMaster tblEmpMaster;
     private String compCode;
     private String compName;
     private int compStatus;
     private Date compLmd;
     private Set tblMapCompanyLocations = new HashSet(0);
     private Set tblMapCompanyDepartments = new HashSet(0);
     private Set tblEmpMasters = new HashSet(0);

    public TblPzCompanyMaster() {
    }

    public TblPzCompanyMaster(Integer compId, TblEmpMaster tblEmpMaster, String compCode, String compName, int compStatus, Date compLmd) {
        this.compId = compId;
        this.tblEmpMaster = tblEmpMaster;
        this.compCode = compCode;
        this.compName = compName;
        this.compStatus = compStatus;
        this.compLmd = compLmd;
    }

    public TblPzCompanyMaster(TblEmpMaster tblEmpMaster, String compCode, String compName, int compStatus, Date compLmd) {
        this.tblEmpMaster = tblEmpMaster;
        this.compCode = compCode;
        this.compName = compName;
        this.compStatus = compStatus;
        this.compLmd = compLmd;
    }

    public Integer getCompId() {
        return compId;
    }

    public void setCompId(Integer compId) {
        this.compId = compId;
    }

    public TblEmpMaster getTblEmpMaster() {
        return tblEmpMaster;
    }

    public void setTblEmpMaster(TblEmpMaster tblEmpMaster) {
        this.tblEmpMaster = tblEmpMaster;
    }

    public String getCompCode() {
        return compCode;
    }

    public void setCompCode(String compCode) {
        this.compCode = compCode;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public int getCompStatus() {
        return compStatus;
    }

    public void setCompStatus(int compStatus) {
        this.compStatus = compStatus;
    }

    public Date getCompLmd() {
        return compLmd;
    }

    public void setCompLmd(Date compLmd) {
        this.compLmd = compLmd;
    }

    public Set getTblMapCompanyLocations() {
        return tblMapCompanyLocations;
    }

    public void setTblMapCompanyLocations(Set tblMapCompanyLocations) {
        this.tblMapCompanyLocations = tblMapCompanyLocations;
    }

    public Set getTblMapCompanyDepartments() {
        return tblMapCompanyDepartments;
    }

    public void setTblMapCompanyDepartments(Set tblMapCompanyDepartments) {
        this.tblMapCompanyDepartments = tblMapCompanyDepartments;
    }

    public Set getTblEmpMasters() {
        return tblEmpMasters;
    }

    public void setTblEmpMasters(Set tblEmpMasters) {
        this.tblEmpMasters = tblEmpMasters;
    }

    

    
    
    
}
