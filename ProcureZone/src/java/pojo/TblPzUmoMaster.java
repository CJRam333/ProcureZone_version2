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
public class TblPzUmoMaster {
    
    private Integer umoId;
     private String umoCode;
     private String umoNamea;
     private Integer umoStatus;

    public TblPzUmoMaster() {
    }

    public TblPzUmoMaster(Integer umoId, String umoCode, String umoNamea, Integer umoStatus) {
        this.umoId = umoId;
        this.umoCode = umoCode;
        this.umoNamea = umoNamea;
        this.umoStatus = umoStatus;
    }

    public TblPzUmoMaster(String umoCode, String umoNamea, Integer umoStatus) {
        this.umoCode = umoCode;
        this.umoNamea = umoNamea;
        this.umoStatus = umoStatus;
    }

    public Integer getUmoId() {
        return umoId;
    }

    public void setUmoId(Integer umoId) {
        this.umoId = umoId;
    }

    public String getUmoCode() {
        return umoCode;
    }

    public void setUmoCode(String umoCode) {
        this.umoCode = umoCode;
    }

    public String getUmoNamea() {
        return umoNamea;
    }

    public void setUmoNamea(String umoNamea) {
        this.umoNamea = umoNamea;
    }

    public Integer getUmoStatus() {
        return umoStatus;
    }

    public void setUmoStatus(Integer umoStatus) {
        this.umoStatus = umoStatus;
    }

    

   
    
}
