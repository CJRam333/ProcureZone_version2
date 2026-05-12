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
public class TblPzProcPack {
    
     private Integer colId;
     private String colCode;
     private String colName;
     private Integer colStatus;

    public TblPzProcPack() {
    }

    public TblPzProcPack(Integer colId, String colCode, String colName, Integer colStatus) {
        this.colId = colId;
        this.colCode = colCode;
        this.colName = colName;
        this.colStatus = colStatus;
    }

    public TblPzProcPack(String colCode, String colName, Integer colStatus) {
        this.colCode = colCode;
        this.colName = colName;
        this.colStatus = colStatus;
    }

    public Integer getColId() {
        return colId;
    }

    public void setColId(Integer colId) {
        this.colId = colId;
    }

    public String getColCode() {
        return colCode;
    }

    public void setColCode(String colCode) {
        this.colCode = colCode;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public Integer getColStatus() {
        return colStatus;
    }

    public void setColStatus(Integer colStatus) {
        this.colStatus = colStatus;
    }
     
     
    
}
