/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.issue.mailService;

import java.util.ArrayList;
import java.util.Iterator;import seeds.masters.daoImpl.EmployeeRolesDaoImpl;

import java.util.List;
import pojo.TblEmpMaster;
import pojo.TblIssueNote;
import pojo.TblMapEmpRoles;
import seeds.global.service.DaoFactory;
import seeds.issue.daoImpl.IssueNoteDaoImpl;
import seeds.masters.daoImpl.EmployeeDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class IssueEmail {
    private TblIssueNote tblIssueNote;
    private final IssueNoteDaoImpl issueNoteDao = DaoFactory.getDao(IssueNoteDaoImpl.class);
    private List<TblEmpMaster> listTblEmpMaster;
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);
    private TblMapEmpRoles tblMapEmpRoles;
    private final EmployeeRolesDaoImpl employeeRolesDao=DaoFactory.getDao(EmployeeRolesDaoImpl.class);

    public TblIssueNote getTblIssueNote() {
        return tblIssueNote;
    }

    public void setTblIssueNote(TblIssueNote tblIssueNote) {
        this.tblIssueNote = tblIssueNote;
    }

    public List<TblEmpMaster> getListTblEmpMaster() {
        return listTblEmpMaster;
    }

    public void setListTblEmpMaster(List<TblEmpMaster> listTblEmpMaster) {
        this.listTblEmpMaster = listTblEmpMaster;
    }

    public TblMapEmpRoles getTblMapEmpRoles() {
        return tblMapEmpRoles;
    }

    public void setTblMapEmpRoles(TblMapEmpRoles tblMapEmpRoles) {
        this.tblMapEmpRoles = tblMapEmpRoles;
    }

    public IssueEmail() throws Exception{
        tblIssueNote = new TblIssueNote();
        listTblEmpMaster = new ArrayList<TblEmpMaster>();
        tblMapEmpRoles=new TblMapEmpRoles();
    }
    
    public String getUserEmailId(int req) {
        String emailAddress = "";
        try {
            tblIssueNote = (TblIssueNote) issueNoteDao.getById(req);
        } catch (Exception e) {
        }
        if (tblIssueNote != null) {
            TblEmpMaster empMaster = (TblEmpMaster) employeeDao.getList("where empStatus=1 and empNumber=" + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpNumber() + "").get(0);
            emailAddress = empMaster.getEmpEmail();
        }
        return emailAddress;
    }

    public String getRmEmailId(int req) {
        String emailAddress = "";
        try {
            tblIssueNote = (TblIssueNote) issueNoteDao.getById(req);
        } catch (Exception e) {
        }
        if (tblIssueNote != null) {
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpNumber() + ")");
            if (!listTblEmpMaster.isEmpty()) {
                Iterator it = listTblEmpMaster.iterator();
                int s = 0;
                String k = "";
                String m = "";
                while (it.hasNext()) {
                    TblEmpMaster empMaster = (TblEmpMaster) it.next();
                    tblMapEmpRoles=(TblMapEmpRoles)employeeRolesDao.getList("where empRolesStatus=1 and tblEmpMasterByEmpNumber.empNumber=" + empMaster.getEmpNumber() + " ").get(0);
                    if(tblMapEmpRoles.getTblRolesMaster().getRoleId()!=5){
                    if (s > 0) {
                        k = ",";
                    }
                    m = m + k + empMaster.getEmpEmail();
                    s++;
                    }
                }
                if (!m.equals("")) {
                    emailAddress = m;
                }
            }
        }
        
        return emailAddress;
    }
    
    public String getStoresEmailId(int req) {
        String emailAddress = "";
        try {
            tblIssueNote = (TblIssueNote) issueNoteDao.getById(req);
        } catch (Exception e) {
        }
        listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId=6 and empRolesStatus=1)");
        //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and tblDepartmentMaster.deptId =" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getTblDepartmentMaster().getDeptId() + " and empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId=6 and empRolesStatus=1)");
        if (!listTblEmpMaster.isEmpty()) {
            Iterator it = listTblEmpMaster.iterator();
            int s = 0;
            String k = "";
            String m = "";
            while (it.hasNext()) {
                TblEmpMaster empMaster = (TblEmpMaster) it.next();
                if (s > 0) {
                    k = ",";
                }
                m = m + k + empMaster.getEmpEmail();
                s++;
            }
            if (!m.equals("")) {
                emailAddress = m;
            }
        }
        return emailAddress;
    }
     public static void main(String[] args) throws Exception {
        IssueEmail ie=new IssueEmail();
        ie.getRmEmailId(1);
                
    }
}
