/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.indent.action;


import java.util.ArrayList;
import java.util.List;
import pojo.TblPzScheduleMaterialmaster;
import pojo.TblUmoMaster;
import seeds.global.service.DaoFactory;
import seeds.global.service.DaoImplService;
import seeds.masters.daoImpl.UmoDaoImpl;

/**
 *
 * @author ramesh.a
 */
public class ScheduleMaterialDaoImpl extends DaoImplService {
    
    public ScheduleMaterialDaoImpl() {
        super(TblPzScheduleMaterialmaster.class);
    }
     public List<TblPzScheduleMaterialmaster> getmatBatch() throws Exception {
        list =(ArrayList<TblPzScheduleMaterialmaster>) DaoFactory.getDao(ScheduleMaterialDaoImpl.class).getList("where matStatus=1");
        return list;

    }
    
    
}
