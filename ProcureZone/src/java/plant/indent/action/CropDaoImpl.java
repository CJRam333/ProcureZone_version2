/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.indent.action;



import pojo.TblCropMaster;
import seeds.global.service.DaoImplService;

/**
 *
 * @author raghuvamshi.a
 */
public class CropDaoImpl extends DaoImplService{
    
    public CropDaoImpl() {
        super(TblCropMaster.class);
    }

    
    
}
