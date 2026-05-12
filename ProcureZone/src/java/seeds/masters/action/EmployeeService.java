/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.action;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.EmployeeDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class EmployeeService {

    private LdapService ldapser;
    private final Utils utils = new Utils();
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);

    public LdapService getLdapser() {
        return ldapser;
    }

    public void setLdapser(LdapService ldapser) {
        this.ldapser = ldapser;
    }

    public EmployeeService() throws Exception {
        ldapser = new LdapService();
    }

    public boolean checkLogin(String userName, String passWord) throws UnknownHostException {
        boolean login = false;
        List list1 = employeeDao.getPropertyById("count(empEmail)", " where empEmail = '" + userName + "' and empPassword='" + utils.getEncript(passWord) + "' and empStatus=1");
//        List list1 = employeeDao.getPropertyById("count(empEmail)", " where empEmail = '" + userName + "' and empPassword='" + passWord + "' and empStatus=1");
        Iterator it = list1.iterator();
        if (it.hasNext()) {
            Long log = (Long) it.next();
            if (log > 0) {

                login = true;
            }
        }
        return login;
    }

    public boolean checkLdapLogin(String userName, String passWord) throws UnknownHostException, Exception {
        boolean login = false;
        String[] domain = userName.split("@");
        if (domain.length > 0) {
            try {
                if (domain[1].equals("nslindia.com")) {
                    login = ldapser.ADLdap(userName, passWord);
                    System.out.println(" AD LDAP LOGIN " + login);
                } else {
                    login = ldapser.OpenLdap(userName, passWord);
                    System.out.println(" OPEN LDAP LOGIN " + login);
                }

            } catch (Exception e) {
                return false;
            }

        }
        if (login == true) {
            List list1 = employeeDao.getPropertyById("count(empEmail)", " where empEmail = '" + userName + "' and empStatus=1");
            Iterator it = list1.iterator();
            if (it.hasNext()) {
                Long log = (Long) it.next();
                if (log == 1) {
                    login = true;
                } else {
                    login = false;
                }
            }
        }
        return login;
    }

}
