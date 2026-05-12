/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.sap.cronScheduler;

import pojo.TblPzMapCompanyPlantMaterial;
import seeds.global.service.DaoImplService;

/**
 *
 * @author ramesh.a
 */
public class CompPzPlantMatcronDaoImpl  extends DaoImplService{

    public CompPzPlantMatcronDaoImpl() {
        super(TblPzMapCompanyPlantMaterial.class);
    }
    
}
