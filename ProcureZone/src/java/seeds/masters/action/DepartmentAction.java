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
import pojo.TblDepartmentMaster;
import pojo.TblEmpMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.DepartmentDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class DepartmentAction extends ActionSupport implements ModelDriven<TblDepartmentMaster>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblDepartmentMaster tblDepartmentMaster;
    private List<TblDepartmentMaster> listTblDepartmentMaster;
    private final DepartmentDaoImpl departmentDao = DaoFactory.getDao(DepartmentDaoImpl.class);

    private String message;
    private String departmentCode;
    private String departmentName;
    private static int deptId;

    public TblDepartmentMaster getTblDepartmentMaster() {
        return tblDepartmentMaster;
    }

    public void setTblDepartmentMaster(TblDepartmentMaster tblDepartmentMaster) {
        this.tblDepartmentMaster = tblDepartmentMaster;
    }

    public List<TblDepartmentMaster> getListTblDepartmentMaster() {
        return listTblDepartmentMaster;
    }

    public void setListTblDepartmentMaster(List<TblDepartmentMaster> listTblDepartmentMaster) {
        this.listTblDepartmentMaster = listTblDepartmentMaster;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public static int getDeptId() {
        return deptId;
    }

    public static void setDeptId(int deptId) {
        DepartmentAction.deptId = deptId;
    }

    public DepartmentAction() throws Exception {
        tblDepartmentMaster = new TblDepartmentMaster();
        listTblDepartmentMaster = new ArrayList<TblDepartmentMaster>();
    }

    public String getList() {
        listTblDepartmentMaster = (ArrayList<TblDepartmentMaster>) departmentDao.getList("where deptStatus in (0,1)");
        if (!listTblDepartmentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String addDepartment() {
        deptId = 0;
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (departmentCode.length() == 0 && departmentCode != null) {
            addFieldError("departmentCode", "Please Enter Department Code.");
            clear = false;
        }
        if (departmentName.length() == 0 && departmentName != null) {
            addFieldError("departmentName", "Please Enter Department Name.");
            clear = false;
        }
        return clear;
    }

    public String saveDepartment() throws ParseException {
        if (validation()) {
            listTblDepartmentMaster = (ArrayList<TblDepartmentMaster>) departmentDao.getList("where (deptStatus in (0,1) and deptCode='" + departmentCode + "') or (deptStatus in (0,1) and deptName='" + departmentName + "')");
            
                if (deptId != 0) {
                    tblDepartmentMaster = (TblDepartmentMaster) departmentDao.getById(deptId);
                } else {
                    if (listTblDepartmentMaster.isEmpty()) {
                        tblDepartmentMaster = new TblDepartmentMaster();
                    } else {
                        addFieldError("departmentCode", "Duplicate Department Code or Department Name.");
                        return INPUT;
                    }
                }
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblDepartmentMaster.setDeptCode(departmentCode);
                tblDepartmentMaster.setDeptName(departmentName);
                tblDepartmentMaster.setDeptStatus(1);
                tblDepartmentMaster.setTblEmpMaster(empMaster);
                tblDepartmentMaster.setDeptLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = departmentDao.save(tblDepartmentMaster);
                if (result) {
                    setTblDepartmentMaster(new TblDepartmentMaster());
                    setDepartmentCode("");
                    setDepartmentName("");
                    setMessage("Department Inserted Successfully");
                    return SUCCESS;
                } else {
                    return INPUT;
                }
            
        } else {
            return INPUT;
        }
    }

    public String viewDepartment() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("deptId");
        try {
            if (editId.length == 1) {
                this.tblDepartmentMaster = (TblDepartmentMaster) departmentDao.getById(Integer.parseInt(editId[0]));
                setDepartmentCode(tblDepartmentMaster.getDeptCode());
                setDepartmentName(tblDepartmentMaster.getDeptName());
                setDeptId(tblDepartmentMaster.getDeptId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateDepartment() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("deptId");
        try {
            if (editId.length == 1) {
                this.tblDepartmentMaster = (TblDepartmentMaster) departmentDao.getById(Integer.parseInt(editId[0]));
                setDepartmentCode(tblDepartmentMaster.getDeptCode());
                setDepartmentName(tblDepartmentMaster.getDeptName());
                setDeptId(tblDepartmentMaster.getDeptId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteDepartment() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("deptId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblDepartmentMaster = (TblDepartmentMaster) departmentDao.getById(Integer.parseInt(a));
                this.tblDepartmentMaster.setDeptStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblDepartmentMaster.setTblEmpMaster(empMaster);
                this.tblDepartmentMaster.setDeptLmd(utils.getDateFormat(utils.DateIn()));
                departmentDao.save(this.tblDepartmentMaster);
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
    public TblDepartmentMaster getModel() {
        return this.tblDepartmentMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
