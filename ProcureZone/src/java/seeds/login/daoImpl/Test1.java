package seeds.login.daoImpl;

import seeds.global.service.DaoFactory;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author root
 */
public class Test1 extends Thread {

    Date d = new Date();

    public Test1() throws Exception {
    }

    @Override
    public void run() {
        while (true) {
           // System.out.println("Database connection was alive from " + d);
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
            }
        }
    }

    public void run1() {
        int x = 10;
        int y = 10;
        int sum = 0;
        sum = x = y;
        //System.out.println("ttttt" + sum);
    }

    public void getConnectionThread() {
        setPriority(MIN_PRIORITY);
        this.setDaemon(true);
        start();
    }

    public static void main(String args[]) throws Exception {

    }

}
