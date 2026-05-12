/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.daoImpl;

import pojo.TblCompanyMaster;
import seeds.global.service.DaoImplService;

/**
 *
 * @author ramesh.avv
 */
public class CompanyDaoImpl extends DaoImplService{
    
    public CompanyDaoImpl() {
        super(TblCompanyMaster.class);
    }    
}
