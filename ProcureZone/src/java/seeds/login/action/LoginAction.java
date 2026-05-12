/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.login.action;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts2.interceptor.SessionAware;
import plant.indent.action.IndentPzDaoImpl;
import pojo.TbPzlIndentMastera;
import pojo.TblEmpMaster;
import pojo.TblIndentMaster;
import pojo.TblMapEmpRoles;
import seeds.global.service.DaoFactory;
import seeds.indent.daoImpl.IndentDaoImpl;
import seeds.masters.action.EmployeeService;
import seeds.masters.daoImpl.EmployeeDaoImpl;
import seeds.masters.daoImpl.EmployeeRolesDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class LoginAction extends ActionSupport implements SessionAware {

    private TblEmpMaster user;
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);
    private TblMapEmpRoles tblMapEmpRoles;
    private List<TblMapEmpRoles> listTblMapEmpRoles;
    private final EmployeeRolesDaoImpl employeeRolesDao = DaoFactory.getDao(EmployeeRolesDaoImpl.class);
    private EmployeeService empService;
    private List<TblIndentMaster> listTblIndentMaster;
    private List<TblIndentMaster> listTblIndentMaster1;
    private List<TblIndentMaster> listTblIndentMaster2;
    private List<TblIndentMaster> listTblIndentMaster3;
    private List<TbPzlIndentMastera> listTblpzIndentMaster;
    private List<TbPzlIndentMastera> listTblpzIndentMaster0;
    private List<TbPzlIndentMastera> listTblpzIndentMaster1;
    private List<TbPzlIndentMastera> listTblpzIndentMaster2;
    private List<TbPzlIndentMastera> listTblpzIndentMaster3;
    private List<TbPzlIndentMastera> listTblpzIndentMaster4;
    private List<TbPzlIndentMastera> listTblpzIndentMaster5;
    private List<TbPzlIndentMastera> listTblpzIndentMaster6;
    private List<TbPzlIndentMastera> listTblpzIndentMaster7;
    private List<TbPzlIndentMastera> listTblpzIndentMaster8;
    private List<TbPzlIndentMastera> listTblpzIndentMasterQcrejected;
    private final IndentDaoImpl indentDao = DaoFactory.getDao(IndentDaoImpl.class);
    private final IndentPzDaoImpl indentpzDao = DaoFactory.getDao(IndentPzDaoImpl.class);
   

    private Map session;
    private String userName;
    private String password;
    private String inValid;

    public TblEmpMaster getUser() {
        return user;
    }

    public void setUser(TblEmpMaster user) {
        this.user = user;
    }

    public TblMapEmpRoles getTblMapEmpRoles() {
        return tblMapEmpRoles;
    }

    public void setTblMapEmpRoles(TblMapEmpRoles tblMapEmpRoles) {
        this.tblMapEmpRoles = tblMapEmpRoles;
    }

    public List<TblMapEmpRoles> getListTblMapEmpRoles() {
        return listTblMapEmpRoles;
    }

    public void setListTblMapEmpRoles(List<TblMapEmpRoles> listTblMapEmpRoles) {
        this.listTblMapEmpRoles = listTblMapEmpRoles;
    }

    public EmployeeService getEmpService() {
        return empService;
    }

    public void setEmpService(EmployeeService empService) {
        this.empService = empService;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getInValid() {
        return inValid;
    }

    public void setInValid(String inValid) {
        this.inValid = inValid;
    }
    
    public List<TblIndentMaster> getListTblIndentMaster() {
        return listTblIndentMaster;
    }

    public void setListTblIndentMaster(List<TblIndentMaster> listTblIndentMaster) {
        this.listTblIndentMaster = listTblIndentMaster;
    }

    public List<TblIndentMaster> getListTblIndentMaster1() {
        return listTblIndentMaster1;
    }

    public void setListTblIndentMaster1(List<TblIndentMaster> listTblIndentMaster1) {
        this.listTblIndentMaster1 = listTblIndentMaster1;
    }

    public List<TblIndentMaster> getListTblIndentMaster2() {
        return listTblIndentMaster2;
    }

    public void setListTblIndentMaster2(List<TblIndentMaster> listTblIndentMaster2) {
        this.listTblIndentMaster2 = listTblIndentMaster2;
    }

    public List<TblIndentMaster> getListTblIndentMaster3() {
        return listTblIndentMaster3;
    }

    public void setListTblIndentMaster3(List<TblIndentMaster> listTblIndentMaster3) {
        this.listTblIndentMaster3 = listTblIndentMaster3;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster() {
        return listTblpzIndentMaster;
    }

    public void setListTblpzIndentMaster(List<TbPzlIndentMastera> listTblpzIndentMaster) {
        this.listTblpzIndentMaster = listTblpzIndentMaster;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster1() {
        return listTblpzIndentMaster1;
    }

    public void setListTblpzIndentMaster1(List<TbPzlIndentMastera> listTblpzIndentMaster1) {
        this.listTblpzIndentMaster1 = listTblpzIndentMaster1;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster2() {
        return listTblpzIndentMaster2;
    }

    public void setListTblpzIndentMaster2(List<TbPzlIndentMastera> listTblpzIndentMaster2) {
        this.listTblpzIndentMaster2 = listTblpzIndentMaster2;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster3() {
        return listTblpzIndentMaster3;
    }

    public void setListTblpzIndentMaster3(List<TbPzlIndentMastera> listTblpzIndentMaster3) {
        this.listTblpzIndentMaster3 = listTblpzIndentMaster3;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster4() {
        return listTblpzIndentMaster4;
    }

    public void setListTblpzIndentMaster4(List<TbPzlIndentMastera> listTblpzIndentMaster4) {
        this.listTblpzIndentMaster4 = listTblpzIndentMaster4;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster5() {
        return listTblpzIndentMaster5;
    }

    public void setListTblpzIndentMaster5(List<TbPzlIndentMastera> listTblpzIndentMaster5) {
        this.listTblpzIndentMaster5 = listTblpzIndentMaster5;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster6() {
        return listTblpzIndentMaster6;
    }

    public void setListTblpzIndentMaster6(List<TbPzlIndentMastera> listTblpzIndentMaster6) {
        this.listTblpzIndentMaster6 = listTblpzIndentMaster6;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster7() {
        return listTblpzIndentMaster7;
    }

    public void setListTblpzIndentMaster7(List<TbPzlIndentMastera> listTblpzIndentMaster7) {
        this.listTblpzIndentMaster7 = listTblpzIndentMaster7;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster0() {
        return listTblpzIndentMaster0;
    }

    public void setListTblpzIndentMaster0(List<TbPzlIndentMastera> listTblpzIndentMaster0) {
        this.listTblpzIndentMaster0 = listTblpzIndentMaster0;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster8() {
        return listTblpzIndentMaster8;
    }

    public void setListTblpzIndentMaster8(List<TbPzlIndentMastera> listTblpzIndentMaster8) {
        this.listTblpzIndentMaster8 = listTblpzIndentMaster8;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMasterQcrejected() {
        return listTblpzIndentMasterQcrejected;
    }

    public void setListTblpzIndentMasterQcrejected(List<TbPzlIndentMastera> listTblpzIndentMasterQcrejected) {
        this.listTblpzIndentMasterQcrejected = listTblpzIndentMasterQcrejected;
    }
    
    

    public LoginAction() throws Exception {
        user = new TblEmpMaster();
        tblMapEmpRoles = new TblMapEmpRoles();
        listTblMapEmpRoles = new ArrayList<TblMapEmpRoles>();
        empService = new EmployeeService();
        listTblIndentMaster = new ArrayList<TblIndentMaster>();
        listTblIndentMaster1 = new ArrayList<TblIndentMaster>();
        listTblIndentMaster2 = new ArrayList<TblIndentMaster>();
        listTblIndentMaster3 = new ArrayList<TblIndentMaster>();
        listTblpzIndentMaster = new ArrayList<TbPzlIndentMastera>();
    }

    public boolean validation() {
        boolean clear = true;
        if (userName.length() == 0 && userName != null) {
            addFieldError("userName", "Please Enter User Name.");
            clear = false;
        }
        if (password.length() == 0 && password != null) {
            addFieldError("password", "Please Enter Password.");
            clear = false;
        }
        return clear;
    }

    public String getLogin() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get("com.opensymphony.xwork2.dispatcher.HttpServletRequest");
        try {
            boolean status = false;
            if (this.userName.contains("@")) {
                status = empService.checkLdapLogin(userName, password);
//                status = empService.checkLogin(userName, password);
            } else {                
                status = empService.checkLogin(userName, password);
            }
            if (status) {
                try {
                    this.session.clear();
                    user = new TblEmpMaster();
                    user = this.getUserObject(userName, password);
                    if (!this.userName.equals(user.getEmpEmail())) {
                        throw new Exception();
                    }
                    listTblMapEmpRoles = (ArrayList<TblMapEmpRoles>) employeeRolesDao.getList("where tblEmpMasterByEmpNumber.empNumber= " + user.getEmpNumber() + "");
                    if (!listTblMapEmpRoles.isEmpty()) {
                        for (TblMapEmpRoles empRoles : listTblMapEmpRoles) {
                            if (empRoles.getTblRolesMaster().getRoleId() == 1) {
                                
                                System.out.println("hello");
                                this.session.put("SuperAdmin", empRoles.getTblRolesMaster().getRoleId());
                            }
                            if (empRoles.getTblRolesMaster().getRoleId() == 2) {
                                this.session.put("Admin", empRoles.getTblRolesMaster().getRoleId());
                            }
                            if (empRoles.getTblRolesMaster().getRoleId() == 3) {
                                this.session.put("role", empRoles.getTblRolesMaster().getRoleId());
                            }
                            if (empRoles.getTblRolesMaster().getRoleId() == 4) {
                                this.session.put("Supervisor", empRoles.getTblRolesMaster().getRoleId());
                             }
                            if (empRoles.getTblRolesMaster().getRoleId() == 5) {
                                this.session.put("DepartmentHead", empRoles.getTblRolesMaster().getRoleId());
                            }
                            if (empRoles.getTblRolesMaster().getRoleId() == 6) {
                                this.session.put("Procurement", empRoles.getTblRolesMaster().getRoleId());
                            }
                            
                            if (empRoles.getTblRolesMaster().getRoleId() == 7) {
                                System.out.println("plantmanager role");
                                this.session.put("PlantManager", empRoles.getTblRolesMaster().getRoleId());
                            }
                            
                            if (empRoles.getTblRolesMaster().getRoleId() == 8) {
                                this.session.put("FloorIncharge", empRoles.getTblRolesMaster().getRoleId());
                            }
                            
                            if (empRoles.getTblRolesMaster().getRoleId() == 9) {
                                this.session.put("DataEntryOperator", empRoles.getTblRolesMaster().getRoleId());
                            }
                            
                            if (empRoles.getTblRolesMaster().getRoleId() == 10) {
                                this.session.put("GoodsIncharge", empRoles.getTblRolesMaster().getRoleId());
                            }
                            
                            if (empRoles.getTblRolesMaster().getRoleId() == 11) {
                                this.session.put("GRNIncharge", empRoles.getTblRolesMaster().getRoleId());
                            }
                            
                            if (empRoles.getTblRolesMaster().getRoleId() == 12) {
                                this.session.put("IissueConfirm", empRoles.getTblRolesMaster().getRoleId());
                            }
                            
                            if (empRoles.getTblRolesMaster().getRoleId() == 13) {
                                this.session.put("ReceiptConfirm", empRoles.getTblRolesMaster().getRoleId());
                            }
                            
                            if (empRoles.getTblRolesMaster().getRoleId() == 14) {
                                this.session.put("QualityManager", empRoles.getTblRolesMaster().getRoleId());
                            }
                            this.session.put("view", empRoles.getTblRolesMaster().getRoleView());
                            this.session.put("add", empRoles.getTblRolesMaster().getRoleAdd());
                            this.session.put("edit", empRoles.getTblRolesMaster().getRoleEdit());
                            this.session.put("delete", empRoles.getTblRolesMaster().getRoleDelete());

                        }
                    }
                    this.session.put("login", "true");
                    this.session.put("empNumber", user.getEmpNumber());
                    this.session.put("plantId", user.getEmpPlant());
                    this.session.put("user", user.getEmpName());
                    this.session.put("location", user.getTblMapCompanyLocations());
                    this.session.put("joinDate", user.getEmpJoinDate());
                    this.session.put("ip", request.getRemoteAddr());
                    this.session.put("empImage", user.getEmpPath());
                    this.session.put("deptId", user.getTblDepartmentMaster().getDeptId());
                    System.out.println(new Date() + "--" + this.session.get("user") + " has Logged in ! from IP -" + request.getRemoteAddr());
                    //TblEmpMaster user1 = (TblEmpMaster) this.userDao.getById(user.getEmpNumber());
                    //user1.setUserLoginIp(request.getRemoteAddr());
                    //user1.setUserLmd(util.getDateFormat(util.DateIn()));
                    //this.userDao.save(user1);
                    getDashboardCount();
                    return SUCCESS;
                } catch (Exception e) {
                }
            } else {
                System.err.println(new Date() + "--" + this.userName + " has a failed Login from IP -" + request.getRemoteAddr());
                addFieldError("", "Invalid User Name Or Password");
                return INPUT;
            }
        } catch (Exception e) {
            System.err.println(new Date() + "--" + this.userName + " has a failed Login from IP -" + request.getRemoteAddr());
            addFieldError("", "Invalid User Name Or Password");
            return INPUT;
        }
        
        return INPUT;
    }

    public TblEmpMaster getUserObject(String userName, String password) {
        List list1 = employeeDao.getList("where empEmail = '" + userName + "' and empStatus=1");
        Iterator it = list1.iterator();
        user = new TblEmpMaster();
        if (it.hasNext()) {
            user = (TblEmpMaster) it.next();
        }
        return user;
    }
    
    public String getDashboardCount(){
        listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 and tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
        listTblIndentMaster1 = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 and tblEmpMasterByIndentCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
        listTblIndentMaster2 = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=1 and tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
        listTblIndentMaster3 = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblIndentStatusByIndentProcurementStatus.indentStatusId in (5,6,7,8)");
        listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (1,2,3,4,5,6,7,8,0,20) and  tblPlantMaster.plantId  in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where Status =1 and tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
        listTblpzIndentMaster0 = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (0) and  tblPlantMaster.plantId  in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where Status =1 and tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
        listTblpzIndentMaster1 = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (2) and  tblPlantMaster.plantId  in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where Status =1 and tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
        listTblpzIndentMaster2 = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (3) and  tblPlantMaster.plantId  in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where Status =1 and tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
        listTblpzIndentMaster3 = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (4) and  tblPlantMaster.plantId  in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where Status =1 and tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
        listTblpzIndentMaster4 = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (5) and  tblPlantMaster.plantId  in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where Status =1 and tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
        listTblpzIndentMaster5 = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (6) and  tblPlantMaster.plantId  in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where Status =1 and tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
        listTblpzIndentMaster6 = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (7) and  tblPlantMaster.plantId  in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where Status =1 and tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
        listTblpzIndentMaster7 = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (8) and  tblPlantMaster.plantId  in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where Status =1 and tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
        listTblpzIndentMaster8 = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (20) ");
//        listTblpzIndentMasterQcrejected = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus = 0 and tblEmpMasterByIndentLmu.empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId = 14");
        listTblpzIndentMasterQcrejected = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus = 0 and tblEmpMasterByIndentLmu.empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId = 14)");
        System.out.println("QC REJECTE SIZE : "+listTblpzIndentMasterQcrejected.size());
        
        
        return SUCCESS;
    }

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
