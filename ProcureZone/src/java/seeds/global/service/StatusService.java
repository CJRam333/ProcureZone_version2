/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.global.service;

/**
 *
 * @author Prabhakar
 */
public class StatusService {

    private int activeStatus;
    private int inactiveStatus;

    public StatusService() {
        this.activeStatus = 1;
        this.inactiveStatus = 0;
    }

    public int getActiveStatus() {
        return activeStatus;
    }

    public int getInactiveStatus() {
        return inactiveStatus;
    }

}
