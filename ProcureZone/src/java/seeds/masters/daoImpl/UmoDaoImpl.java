/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.daoImpl;

import java.util.ArrayList;
import java.util.List;
import pojo.TblUmoMaster;
import seeds.global.service.DaoFactory;
import seeds.global.service.DaoImplService;

/**
 *
 * @author ramesh.avv
 */
public class UmoDaoImpl extends DaoImplService{

    public UmoDaoImpl() {
        super(TblUmoMaster.class);
    }
    
    public List<TblUmoMaster> getUmo() throws Exception {
        list =(ArrayList<TblUmoMaster>) DaoFactory.getDao(UmoDaoImpl.class).getList("where umoStatus=1");
        return list;

    }
    
}
