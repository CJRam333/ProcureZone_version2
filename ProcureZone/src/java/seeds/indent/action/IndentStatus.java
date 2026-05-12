/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.indent.action;

/**
 *
 * @author ramesh.avv
 */
public class IndentStatus {
    
    private int statusId;
    private String StatusName;

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return StatusName;
    }

    public void setStatusName(String StatusName) {
        this.StatusName = StatusName;
    }

    public IndentStatus(int statusId, String StatusName) {
        this.statusId = statusId;
        this.StatusName = StatusName;
    }
    
}
