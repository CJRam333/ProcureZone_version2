/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.indent.action;



import pojo.TblPzCropsType;
import seeds.global.service.DaoImplService;

/**
 *
 * @author ramesh.a
 */
public class PzCropTypeDaoImpl extends DaoImplService{
    
    public PzCropTypeDaoImpl() {
        super(TblPzCropsType.class);
    }
    
}
