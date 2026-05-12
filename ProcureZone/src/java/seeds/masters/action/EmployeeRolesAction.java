/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.action;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import pojo.TblEmpMaster;
import pojo.TblMapEmpRoles;
import pojo.TblRolesMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.EmployeeDaoImpl;
import seeds.masters.daoImpl.EmployeeRolesDaoImpl;
import seeds.masters.daoImpl.RolesDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class EmployeeRolesAction extends ActionSupport implements ModelDriven<TblMapEmpRoles>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblMapEmpRoles tblMapEmpRoles;
    private List<TblMapEmpRoles> listTblMapEmpRoles;
    private final EmployeeRolesDaoImpl employeeRolesDao = DaoFactory.getDao(EmployeeRolesDaoImpl.class);
    private List<TblEmpMaster> listTblEmpMaster;
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);
    private List<TblRolesMaster> listTblRolesMaster;
    private final RolesDaoImpl rolesDao = DaoFactory.getDao(RolesDaoImpl.class);

    private String message;
    private int empNumber;
    private int roleId;
    private static int erId;

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

    public List<TblEmpMaster> getListTblEmpMaster() {
        return listTblEmpMaster;
    }

    public void setListTblEmpMaster(List<TblEmpMaster> listTblEmpMaster) {
        this.listTblEmpMaster = listTblEmpMaster;
    }

    public List<TblRolesMaster> getListTblRolesMaster() {
        return listTblRolesMaster;
    }

    public void setListTblRolesMaster(List<TblRolesMaster> listTblRolesMaster) {
        this.listTblRolesMaster = listTblRolesMaster;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getEmpNumber() {
        return empNumber;
    }

    public void setEmpNumber(int empNumber) {
        this.empNumber = empNumber;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public static int getErId() {
        return erId;
    }

    public static void setErId(int erId) {
        EmployeeRolesAction.erId = erId;
    }

    public EmployeeRolesAction() throws Exception {
        tblMapEmpRoles = new TblMapEmpRoles();
        listTblMapEmpRoles = new ArrayList<TblMapEmpRoles>();
        listTblEmpMaster = new ArrayList<TblEmpMaster>();
        listTblRolesMaster = new ArrayList<TblRolesMaster>();
    }

    public String getList() {
        listTblMapEmpRoles = (ArrayList<TblMapEmpRoles>) employeeRolesDao.getList("where empRolesStatus in (0,1) and tblEmpMasterByEmpNumber.empNumber not in (1)");
        if (!listTblMapEmpRoles.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void getEmployeeRolesList() {
        listTblEmpMaster = employeeDao.getList("where empStatus=1 and empNumber not in (1)");
        listTblRolesMaster = rolesDao.getList("where roleStatus=1");
    }

    public String addEmpRoles() {
        erId = 0;
        getEmployeeRolesList();
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (empNumber == 0) {
            addFieldError("empNumber", "Please Select Employee.");
            clear = false;
        }
        if (roleId == 0) {
            addFieldError("roleId", "Please Select Role.");
            clear = false;
        }
        return clear;
    }

    public String saveEmpRoles() throws ParseException {
        if (validation()) {
            listTblMapEmpRoles = (ArrayList<TblMapEmpRoles>) employeeRolesDao.getList("where empRolesStatus in (0,1) and tblEmpMasterByEmpNumber.empNumber=" + empNumber + " and tblRolesMaster.roleId=" + roleId + "");
           
                if (erId != 0) {
                    tblMapEmpRoles = (TblMapEmpRoles) employeeRolesDao.getById(erId);
                } else {
                    if (listTblMapEmpRoles.isEmpty()) {
                        tblMapEmpRoles = new TblMapEmpRoles();
                    } else {
                        addFieldError("empNumber", "Duplicate Employee or Role.");
                        getList();
                        getEmployeeRolesList();
                        return INPUT;
                    }
                }

                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(empNumber);
                tblMapEmpRoles.setTblEmpMasterByEmpNumber(empMaster);
                TblRolesMaster roleMaster = new TblRolesMaster();
                roleMaster.setRoleId(roleId);
                tblMapEmpRoles.setTblRolesMaster(roleMaster);
                tblMapEmpRoles.setEmpRolesStatus(1);
                TblEmpMaster empMaster1 = new TblEmpMaster();
                empMaster1.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblMapEmpRoles.setTblEmpMasterByEmpRolesLmu(empMaster1);
                tblMapEmpRoles.setEmpRolesLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = employeeRolesDao.save(tblMapEmpRoles);
                if (result) {
                    setTblMapEmpRoles(new TblMapEmpRoles());
                    setEmpNumber(0);
                    setRoleId(0);
                    setMessage("Employee Role Inserted Successfully");
                    return SUCCESS;
                } else {
                    getList();
                    getEmployeeRolesList();
                    return INPUT;
                }            
        } else {
            getList();
            getEmployeeRolesList();
            return INPUT;
        }
    }

    public String viewEmpRoles() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("empRolesId");
        getEmployeeRolesList();
        try {
            if (editId.length == 1) {
                this.tblMapEmpRoles = (TblMapEmpRoles) employeeRolesDao.getById(Integer.parseInt(editId[0]));
                setEmpNumber(tblMapEmpRoles.getTblEmpMasterByEmpNumber().getEmpNumber());
                setRoleId(tblMapEmpRoles.getTblRolesMaster().getRoleId());
                setErId(tblMapEmpRoles.getEmpRolesId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateEmpRoles() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("empRolesId");
        getEmployeeRolesList();
        try {
            if (editId.length == 1) {
                this.tblMapEmpRoles = (TblMapEmpRoles) employeeRolesDao.getById(Integer.parseInt(editId[0]));
                setEmpNumber(tblMapEmpRoles.getTblEmpMasterByEmpNumber().getEmpNumber());
                setRoleId(tblMapEmpRoles.getTblRolesMaster().getRoleId());
                setErId(tblMapEmpRoles.getEmpRolesId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteEmpRoles() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("empRolesId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblMapEmpRoles = (TblMapEmpRoles) employeeRolesDao.getById(Integer.parseInt(a));
                this.tblMapEmpRoles.setEmpRolesStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblMapEmpRoles.setTblEmpMasterByEmpRolesLmu(empMaster);
                this.tblMapEmpRoles.setEmpRolesLmd(utils.getDateFormat(utils.DateIn()));
                employeeRolesDao.save(this.tblMapEmpRoles);
            }
        }
        getList();
        return SUCCESS;
    }

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    public TblMapEmpRoles getModel() {
        return this.tblMapEmpRoles;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
