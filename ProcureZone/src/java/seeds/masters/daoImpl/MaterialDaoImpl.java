/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.daoImpl;

import java.util.ArrayList;
import java.util.List;
import pojo.TblMaterialMaster;
import seeds.global.service.DaoFactory;
import seeds.global.service.DaoImplService;

/**
 *
 * @author ramesh.avv
 */
public class MaterialDaoImpl extends DaoImplService {

    public MaterialDaoImpl() {
        super(TblMaterialMaster.class);
    }

    public List<TblMaterialMaster> getMaterial() throws Exception {
        list = (ArrayList<TblMaterialMaster>)DaoFactory.getDao(MaterialDaoImpl.class).getList("where materialStatus=1");
        return list;

    }

}
