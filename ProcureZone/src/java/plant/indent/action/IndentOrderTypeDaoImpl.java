/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.indent.action;

import pojo.TblPlantOrderType;
import seeds.global.service.DaoImplService;


public class IndentOrderTypeDaoImpl extends DaoImplService{

    public IndentOrderTypeDaoImpl() {
        super(TblPlantOrderType.class);
    }
    
}
