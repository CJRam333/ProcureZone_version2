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
public class TblPlantOrderType {
    
     private Integer orderId;
     private Integer orderCompany;
     private String orderDesc;
     private String orderNoid;
     private Integer orderStatus;

    public TblPlantOrderType() {
    }

    public TblPlantOrderType(Integer orderId, Integer orderCompany, String orderDesc, String orderNoid, Integer orderStatus) {
        this.orderId = orderId;
        this.orderCompany = orderCompany;
        this.orderDesc = orderDesc;
        this.orderNoid = orderNoid;
        this.orderStatus = orderStatus;
    }

    public TblPlantOrderType(Integer orderCompany, String orderDesc, String orderNoid, Integer orderStatus) {
        this.orderCompany = orderCompany;
        this.orderDesc = orderDesc;
        this.orderNoid = orderNoid;
        this.orderStatus = orderStatus;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getOrderCompany() {
        return orderCompany;
    }

    public void setOrderCompany(Integer orderCompany) {
        this.orderCompany = orderCompany;
    }

    public String getOrderDesc() {
        return orderDesc;
    }

    public void setOrderDesc(String orderDesc) {
        this.orderDesc = orderDesc;
    }

    public String getOrderNoid() {
        return orderNoid;
    }

    public void setOrderNoid(String orderNoid) {
        this.orderNoid = orderNoid;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    
     
    
}
