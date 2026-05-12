/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.login.daoImpl;

import pojo.TblUserMaster;
import seeds.global.service.DaoImplService;

/**
 *
 * @author ramesh.avv
 */
public class userDaoImpl extends DaoImplService{

    public userDaoImpl() {
        super(TblUserMaster.class);
    }
    
}
