/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.indent.mailService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import plant.indent.action.IndentPzDaoImpl;
import pojo.TbPzlIndentMastera;
import pojo.TblEmpMaster;
import pojo.TblIndentMaster;
import seeds.global.service.DaoFactory;
import seeds.indent.daoImpl.IndentDaoImpl;
import seeds.masters.daoImpl.EmployeeDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class IndentEmail {

    private TblIndentMaster tblIndentMaster;
    private TbPzlIndentMastera tblPzIndentMaster;
     private final IndentPzDaoImpl indentpzDao = DaoFactory.getDao(IndentPzDaoImpl.class);
    private final IndentDaoImpl indentDao = DaoFactory.getDao(IndentDaoImpl.class);
    private List<TblEmpMaster> listTblEmpMaster;
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);

    public TblIndentMaster getTblIndentMaster() {
        return tblIndentMaster;
    }

    public void setTblIndentMaster(TblIndentMaster tblIndentMaster) {
        this.tblIndentMaster = tblIndentMaster;
    }

    public List<TblEmpMaster> getListTblEmpMaster() {
        return listTblEmpMaster;
    }

    public void setListTblEmpMaster(List<TblEmpMaster> listTblEmpMaster) {
        this.listTblEmpMaster = listTblEmpMaster;
    }

    public IndentEmail() throws Exception {
        tblIndentMaster = new TblIndentMaster();
        listTblEmpMaster = new ArrayList<TblEmpMaster>();
    }

    public String getUserEmailId(int req) {
        String emailAddress = "";
        try {
            tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
        } catch (Exception e) {
        }
        if (tblIndentMaster != null) {
            TblEmpMaster empMaster = (TblEmpMaster) employeeDao.getList("where empStatus=1 and empNumber=" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpNumber() + "").get(0);
            emailAddress = empMaster.getEmpEmail();
        }
        return emailAddress;
    }
    
    
    public String getPzUserEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
            TblEmpMaster empMaster = (TblEmpMaster) employeeDao.getList("where empStatus=1 and empNumber=" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpNumber() + "").get(0);
            emailAddress = empMaster.getEmpEmail();
        }
        return emailAddress;
    }

    public String getRmEmailId(int req) {
        String emailAddress = "";
        try {
            tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
        } catch (Exception e) {
        }
        if (tblIndentMaster != null) {
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpNumber() + ")");
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
        }
        return emailAddress;
    }
    
    public String getPzRmEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpNumber() + ")");
            
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
        }
        return emailAddress;
    }
    
    
//    8 DEO TO FlIncharge
    public String getPzInvMgrEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =8 and a.Status =1 and a.roleStatus = 'ACTIVE' and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
//            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
    
//    13 goodsincharge to receiptconfirm
    public String getGoodsInchargeEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =13 and a.Status =1 and a.roleStatus = 'ACTIVE' and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
//            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
//    11 receiptconfirm to grn incharge
    public String getReceiptConfirmEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =11 and a.Status =1 and a.roleStatus = 'ACTIVE' and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
//            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
    
//    10 Issueconfirm to GoodsIncharge
    public String getPzIssueConfirmEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =10 and a.Status =1 and a.roleStatus = 'ACTIVE' and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
//            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
//    ReceiptConfirm
    
    public String getPzReceiptConfirmEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =11 and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
//            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
    public String getPzGoodsInchargeEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =13 and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
//            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
    
    public String getgoodsInchargeEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =13 and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
//            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
//    ReceiptConfirm
    public String getPzReceiptConfirmaEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =12 and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
//            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
//   12 FLIncharge To Issue Confirm
    public String getPzflrMgrEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =12 and a.Status =1 and a.roleStatus = 'ACTIVE' and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
//            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
    public String getPzIssuConfirmEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =11 and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
//            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
//9 PLM TO DEO
    public String getPzTestInvMgrEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMaster.empNumber from TblPzTblEmpPlantMap where tblRolesMaster.roleId = 9 and tblPlantMaster.plantId =" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =9 and a.Status =1 and a.roleStatus = 'ACTIVE' and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =14 and a.Status =1 and a.roleStatus = 'ACTIVE' )");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
    
    
    
    public String getQtyMagrEmailId(int req) {
        String emailAddress = "";
        try {
            tblPzIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(req);
        } catch (Exception e) {
        }
        if (tblPzIndentMaster != null) {
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMaster.empNumber from TblPzTblEmpPlantMap where tblRolesMaster.roleId = 9 and tblPlantMaster.plantId =" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =9 and a.Status =1 and a.roleStatus = 'ACTIVE' and a.tblPlantMaster.plantId=" + tblPzIndentMaster.getTblPlantMaster().getPlantId()+ ")");
//            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMaster.empNumber from TblPzTblEmpPlantMap as a, TblMapEmpRoles as  b where a.tblEmpMaster.empNumber= b.tblEmpMasterByEmpNumber.empNumber and b.tblRolesMaster.roleId =14 and a.Status =1 and a.roleStatus = 'ACTIVE' )");
             //listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")and empPlant = "+tblPzIndentMaster.getTblPlantMaster().getPlantId()+"");
            int a = tblPzIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber();
//            System.out.println("employee is is : "+a);
            System.out.println("superior id is : "+listTblEmpMaster);
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
        }
        return emailAddress;
    }
    
    

    public String getFinalRmEmailId(int req) {
        String emailAddress = "";
        try {
            tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
        } catch (Exception e) {
        }
        if (tblIndentMaster != null) {
            listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSup.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber =" + tblIndentMaster.getTblEmpMasterByIndentApprovedby().getEmpNumber() + ")");
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
        }
        return emailAddress;
    }

    public String getProcureMentEmailId(int req) {
        String emailAddress = "";
        try {
            tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
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
        IndentEmail id = new IndentEmail();
        id.getFinalRmEmailId(442);
    }

}
