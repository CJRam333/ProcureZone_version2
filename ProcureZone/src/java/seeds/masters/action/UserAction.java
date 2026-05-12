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
import pojo.TblUserMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.EmployeeDaoImpl;
import seeds.masters.daoImpl.UserDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class UserAction extends ActionSupport implements ModelDriven<TblUserMaster>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblUserMaster tblUserMaster;
    private List<TblUserMaster> listTblUserMaster;
    private final UserDaoImpl userDao = DaoFactory.getDao(UserDaoImpl.class);
    private List<TblEmpMaster> listTblEmpMaster;
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);

    private String message;
    private int empNumber;
    private String uName;
    private String uPassword;
    private static int uId;

    public TblUserMaster getTblUserMaster() {
        return tblUserMaster;
    }

    public void setTblUserMaster(TblUserMaster tblUserMaster) {
        this.tblUserMaster = tblUserMaster;
    }

    public List<TblUserMaster> getListTblUserMaster() {
        return listTblUserMaster;
    }

    public void setListTblUserMaster(List<TblUserMaster> listTblUserMaster) {
        this.listTblUserMaster = listTblUserMaster;
    }

    public List<TblEmpMaster> getListTblEmpMaster() {
        return listTblEmpMaster;
    }

    public void setListTblEmpMaster(List<TblEmpMaster> listTblEmpMaster) {
        this.listTblEmpMaster = listTblEmpMaster;
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

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuPassword() {
        return uPassword;
    }

    public void setuPassword(String uPassword) {
        this.uPassword = uPassword;
    }

    public static int getuId() {
        return uId;
    }

    public static void setuId(int uId) {
        UserAction.uId = uId;
    }

    public UserAction() throws Exception {
        tblUserMaster = new TblUserMaster();
        listTblUserMaster = new ArrayList<TblUserMaster>();
        listTblEmpMaster = new ArrayList<TblEmpMaster>();
    }

    public String getList() {
        listTblUserMaster = (ArrayList<TblUserMaster>) userDao.getList("where userStatus=1");
        if (!listTblUserMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void getEmployeeList() {
        listTblEmpMaster = employeeDao.getList("where empStatus=1");
    }

    public String addUser() {
        uId = 0;
        setuName("");
        setuPassword("");
        getEmployeeList();
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (empNumber == 0) {
            addFieldError("empNumber", "Please Select Employee.");
            clear = false;
        }
        if (uName.length() == 0 && uName != null) {
            addFieldError("uName", "Please Enter User Name.");
            clear = false;
        }
        if (uPassword.length() == 0 && uPassword != null) {
            addFieldError("uPassword", "Please Enter User Password.");
            clear = false;
        }
        return clear;
    }

    public String saveUser() throws ParseException {
        if (validation()) {
            listTblUserMaster = (ArrayList<TblUserMaster>) userDao.getList("where userStatus=1 and userName='" + uName + "' and tblEmpMasterByEmpNumber.empNumber=" + empNumber + "");
            if (listTblUserMaster.isEmpty()) {
                if (uId != 0) {
                    tblUserMaster = (TblUserMaster) userDao.getById(uId);
                } else {
                    tblUserMaster = new TblUserMaster();
                }
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblUserMaster.setTblEmpMasterByEmpNumber(empMaster);
                tblUserMaster.setUserName(uName);
                tblUserMaster.setUserPassword(utils.getEncript(uPassword));
                tblUserMaster.setUserLoginIp(this.session.get("ip").toString());
                tblUserMaster.setUserStatus(1);
                tblUserMaster.setTblEmpMasterByUserLmu(empMaster);
                tblUserMaster.setUserLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = userDao.save(tblUserMaster);
                if (result) {
                    setTblUserMaster(new TblUserMaster());
                    setEmpNumber(0);
                    setuName("");
                    setuPassword("");
                    setMessage("User Inserted Successfully");
                    return SUCCESS;
                } else {
                    getList();
                    getEmployeeList();
                    return INPUT;
                }
            } else {
                addFieldError("empNumber", "Duplicate Employee or User Name.");
                getList();
                getEmployeeList();
                return INPUT;
            }
        } else {
            getList();
            getEmployeeList();
            return INPUT;
        }
    }

    public String viewUser() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("userId");
        getEmployeeList();
        try {
            if (editId.length == 1) {
                this.tblUserMaster = (TblUserMaster) userDao.getById(Integer.parseInt(editId[0]));
                setEmpNumber(tblUserMaster.getTblEmpMasterByEmpNumber().getEmpNumber());
                setuName(tblUserMaster.getUserName());
                setuPassword(tblUserMaster.getUserPassword());
                setuId(tblUserMaster.getUserId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateUser() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("userId");
        getEmployeeList();
        try {
            if (editId.length == 1) {
                this.tblUserMaster = (TblUserMaster) userDao.getById(Integer.parseInt(editId[0]));
                setEmpNumber(tblUserMaster.getTblEmpMasterByEmpNumber().getEmpNumber());
                setuName(tblUserMaster.getUserName());
                setuPassword(tblUserMaster.getUserPassword());
                setuId(tblUserMaster.getUserId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteUser() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("userId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblUserMaster = (TblUserMaster) userDao.getById(Integer.parseInt(a));
                this.tblUserMaster.setUserStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblUserMaster.setTblEmpMasterByUserLmu(empMaster);
                this.tblUserMaster.setUserLmd(utils.getDateFormat(utils.DateIn()));
                userDao.save(this.tblUserMaster);
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
    public TblUserMaster getModel() {
        return this.tblUserMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
