/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.issue.daoImpl;

import pojo.TblIssueNote;
import seeds.global.service.DaoFactory;
import seeds.global.service.DaoImplService;

/**
 *
 * @author ramesh.avv
 */
public class IssueNoteDaoImpl extends DaoImplService{

    public IssueNoteDaoImpl() {
        super(TblIssueNote.class);
    }
    
}
