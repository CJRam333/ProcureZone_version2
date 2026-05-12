/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.daoImpl;

import seeds.masters.action.LdapService;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import pojo.TblEmpMaster;
import seeds.global.service.DaoFactory;
import seeds.global.service.DaoImplService;

/**
 *
 * @author ramesh.avv
 */
public class EmployeeDaoImpl extends DaoImplService {

    public EmployeeDaoImpl() throws Exception {
        super(TblEmpMaster.class);
    }
}
