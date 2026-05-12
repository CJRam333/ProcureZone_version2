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
import pojo.TblRolesMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.RolesDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class RolesAction extends ActionSupport implements ModelDriven<TblRolesMaster>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblRolesMaster tblRolesMaster;
    private List<TblRolesMaster> listTblRolesMaster;
    private final RolesDaoImpl rolesDao = DaoFactory.getDao(RolesDaoImpl.class);

    private String message;
    private String rCode;
    private String rName;
    private String rView;
    private String rAdd;
    private String rEdit;
    private String rDelete;
    private static int rId;

    public TblRolesMaster getTblRolesMaster() {
        return tblRolesMaster;
    }

    public void setTblRolesMaster(TblRolesMaster tblRolesMaster) {
        this.tblRolesMaster = tblRolesMaster;
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

    public String getrCode() {
        return rCode;
    }

    public void setrCode(String rCode) {
        this.rCode = rCode;
    }

    public String getrName() {
        return rName;
    }

    public void setrName(String rName) {
        this.rName = rName;
    }

    public String getrView() {
        return rView;
    }

    public void setrView(String rView) {
        this.rView = rView;
    }

    public String getrAdd() {
        return rAdd;
    }

    public void setrAdd(String rAdd) {
        this.rAdd = rAdd;
    }

    public String getrEdit() {
        return rEdit;
    }

    public void setrEdit(String rEdit) {
        this.rEdit = rEdit;
    }

    public String getrDelete() {
        return rDelete;
    }

    public void setrDelete(String rDelete) {
        this.rDelete = rDelete;
    }

    public static int getrId() {
        return rId;
    }

    public static void setrId(int rId) {
        RolesAction.rId = rId;
    }

    public RolesAction() throws Exception {
        tblRolesMaster = new TblRolesMaster();
        listTblRolesMaster = new ArrayList<TblRolesMaster>();
    }

    public String getList() {
        listTblRolesMaster = (ArrayList<TblRolesMaster>) rolesDao.getList("where roleStatus in (0,1)");
        if (!listTblRolesMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String addRole() {
        rId = 0;
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (rCode.length() == 0 && rCode != null) {
            addFieldError("rCode", "Please Enter Role Code.");
            clear = false;
        }
        if (rName.length() == 0 && rName != null) {
            addFieldError("rName", "Please Enter Role Name.");
            clear = false;
        }
        return clear;
    }

    public String saveRole() throws ParseException {
        if (validation()) {
            listTblRolesMaster = (ArrayList<TblRolesMaster>) rolesDao.getList("where (roleStatus in (0,1) and roleCode='" + rCode + "') or (roleStatus in (0,1) and roleName='" + rName + "')");
            
                if (rId != 0) {
                    tblRolesMaster = (TblRolesMaster) rolesDao.getById(rId);
                } else {
                    if (listTblRolesMaster.isEmpty()) {
                        tblRolesMaster = new TblRolesMaster();
                    } else {
                        addFieldError("rCode", "Duplicate Role Code or Role Name.");
                        return INPUT;
                    }
                }
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblRolesMaster.setRoleCode(rCode);
                tblRolesMaster.setRoleName(rName);
                tblRolesMaster.setRoleView(rView);
                tblRolesMaster.setRoleAdd(rAdd);
                tblRolesMaster.setRoleEdit(rEdit);
                tblRolesMaster.setRoleDelete(rDelete);
                tblRolesMaster.setRoleStatus(1);
                tblRolesMaster.setTblEmpMaster(empMaster);
                tblRolesMaster.setRoleLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = rolesDao.save(tblRolesMaster);
                if (result) {
                    setTblRolesMaster(new TblRolesMaster());
                    setrCode("");
                    setrName("");
                    setMessage("Role Inserted Successfully");
                    return SUCCESS;
                } else {
                    return INPUT;
                }            
        } else {
            return INPUT;
        }
    }

    public String viewRole() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("roleId");
        try {
            if (editId.length == 1) {
                this.tblRolesMaster = (TblRolesMaster) rolesDao.getById(Integer.parseInt(editId[0]));
                setrCode(tblRolesMaster.getRoleCode());
                setrName(tblRolesMaster.getRoleName());
                setrView(tblRolesMaster.getRoleView());
                setrAdd(tblRolesMaster.getRoleAdd());
                setrEdit(tblRolesMaster.getRoleEdit());
                setrDelete(tblRolesMaster.getRoleDelete());
                setrId(tblRolesMaster.getRoleId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateRole() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("roleId");
        try {
            if (editId.length == 1) {
                this.tblRolesMaster = (TblRolesMaster) rolesDao.getById(Integer.parseInt(editId[0]));
                setrCode(tblRolesMaster.getRoleCode());
                setrName(tblRolesMaster.getRoleName());
                setrView(tblRolesMaster.getRoleView());
                setrAdd(tblRolesMaster.getRoleAdd());
                setrEdit(tblRolesMaster.getRoleEdit());
                setrDelete(tblRolesMaster.getRoleDelete());
                setrId(tblRolesMaster.getRoleId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteRole() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("roleId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblRolesMaster = (TblRolesMaster) rolesDao.getById(Integer.parseInt(a));
                this.tblRolesMaster.setRoleStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblRolesMaster.setTblEmpMaster(empMaster);
                this.tblRolesMaster.setRoleLmd(utils.getDateFormat(utils.DateIn()));
                rolesDao.save(this.tblRolesMaster);
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
    public TblRolesMaster getModel() {
        return this.tblRolesMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
