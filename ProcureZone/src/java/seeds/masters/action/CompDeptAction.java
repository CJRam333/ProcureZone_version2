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
import pojo.TblCompanyMaster;
import pojo.TblDepartmentMaster;
import pojo.TblEmpMaster;
import pojo.TblMapCompanyDepartment;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.CompDeptDaoImpl;
import seeds.masters.daoImpl.CompanyDaoImpl;
import seeds.masters.daoImpl.DepartmentDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class CompDeptAction extends ActionSupport implements ModelDriven<TblMapCompanyDepartment>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblMapCompanyDepartment tblMapCompanyDepartment;
    private List<TblMapCompanyDepartment> listTblMapCompanyDepartment;
    private final CompDeptDaoImpl compDeptDao = DaoFactory.getDao(CompDeptDaoImpl.class);
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private List<TblDepartmentMaster> listTblDepartmentMaster;
    private final DepartmentDaoImpl departmentDao = DaoFactory.getDao(DepartmentDaoImpl.class);

    private String message;
    private int compId;
    private int deptId;
    private static int genId;

    public TblMapCompanyDepartment getTblMapCompanyDepartment() {
        return tblMapCompanyDepartment;
    }

    public void setTblMapCompanyDepartment(TblMapCompanyDepartment tblMapCompanyDepartment) {
        this.tblMapCompanyDepartment = tblMapCompanyDepartment;
    }

    public List<TblMapCompanyDepartment> getListTblMapCompanyDepartment() {
        return listTblMapCompanyDepartment;
    }

    public void setListTblMapCompanyDepartment(List<TblMapCompanyDepartment> listTblMapCompanyDepartment) {
        this.listTblMapCompanyDepartment = listTblMapCompanyDepartment;
    }

    public List<TblCompanyMaster> getListTblCompanyMaster() {
        return listTblCompanyMaster;
    }

    public void setListTblCompanyMaster(List<TblCompanyMaster> listTblCompanyMaster) {
        this.listTblCompanyMaster = listTblCompanyMaster;
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

    public int getCompId() {
        return compId;
    }

    public void setCompId(int compId) {
        this.compId = compId;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public static int getGenId() {
        return genId;
    }

    public static void setGenId(int genId) {
        CompDeptAction.genId = genId;
    }

    public CompDeptAction() throws Exception {
        tblMapCompanyDepartment = new TblMapCompanyDepartment();
        listTblMapCompanyDepartment = new ArrayList<TblMapCompanyDepartment>();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
        listTblDepartmentMaster = new ArrayList<TblDepartmentMaster>();
    }

    public String getList() {
        listTblMapCompanyDepartment = (ArrayList<TblMapCompanyDepartment>) compDeptDao.getList("where mapStatus in (0,1)");
        if (!listTblMapCompanyDepartment.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void getCompDeptList() {
        listTblCompanyMaster = companyDao.getList("where compStatus=1");
        listTblDepartmentMaster = departmentDao.getList("where deptStatus=1");
    }

    public String addCompDept() {
        genId = 0;
        getCompDeptList();
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (compId == 0) {
            addFieldError("compId", "Please Select Company.");
            clear = false;
        }
        if (deptId == 0) {
            addFieldError("deptId", "Please Select Department.");
            clear = false;
        }
        return clear;
    }

    public String saveCompDept() throws ParseException {
        if (validation()) {
            listTblMapCompanyDepartment = (ArrayList<TblMapCompanyDepartment>) compDeptDao.getList("where mapStatus in (0,1) and tblCompanyMaster.compId=" + compId + " and tblDepartmentMaster.deptId=" + deptId + "");
            if (genId != 0) {
                tblMapCompanyDepartment = (TblMapCompanyDepartment) compDeptDao.getById(genId);
            } else {
                if (listTblMapCompanyDepartment.isEmpty()) {
                    tblMapCompanyDepartment = new TblMapCompanyDepartment();
                } else {
                    addFieldError("compId", "Duplicate Company or Department.");
                    getList();
                    getCompDeptList();
                    return INPUT;
                }
            }
            TblEmpMaster empMaster = new TblEmpMaster();
            empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
            TblCompanyMaster compMaster = new TblCompanyMaster();
            compMaster.setCompId(compId);
            tblMapCompanyDepartment.setTblCompanyMaster(compMaster);
            TblDepartmentMaster deptMaster = new TblDepartmentMaster();
            deptMaster.setDeptId(deptId);
            tblMapCompanyDepartment.setTblDepartmentMaster(deptMaster);
            tblMapCompanyDepartment.setMapStatus(1);
            tblMapCompanyDepartment.setTblEmpMaster(empMaster);
            tblMapCompanyDepartment.setMapLmd(utils.getDateFormat(utils.DateIn()));
            boolean result = compDeptDao.save(tblMapCompanyDepartment);
            if (result) {
                setTblMapCompanyDepartment(new TblMapCompanyDepartment());
                setCompId(0);
                setDeptId(0);
                setMessage("Company Department Map Inserted Successfully");
                return SUCCESS;
            } else {
                getList();
                getCompDeptList();
                return INPUT;
            }
        } else {
            getList();
            getCompDeptList();
            return INPUT;
        }
    }

    public String viewCompDept() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("compDeptId");
        getCompDeptList();
        try {
            if (editId.length == 1) {
                this.tblMapCompanyDepartment = (TblMapCompanyDepartment) compDeptDao.getById(Integer.parseInt(editId[0]));
                setCompId(tblMapCompanyDepartment.getTblCompanyMaster().getCompId());
                setDeptId(tblMapCompanyDepartment.getTblDepartmentMaster().getDeptId());
                setGenId(tblMapCompanyDepartment.getMapId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateCompDept() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("compDeptId");
        getCompDeptList();
        try {
            if (editId.length == 1) {
                this.tblMapCompanyDepartment = (TblMapCompanyDepartment) compDeptDao.getById(Integer.parseInt(editId[0]));
                setCompId(tblMapCompanyDepartment.getTblCompanyMaster().getCompId());
                setDeptId(tblMapCompanyDepartment.getTblDepartmentMaster().getDeptId());
                setGenId(tblMapCompanyDepartment.getMapId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteCompDept() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("compDeptId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblMapCompanyDepartment = (TblMapCompanyDepartment) compDeptDao.getById(Integer.parseInt(a));
                this.tblMapCompanyDepartment.setMapStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblMapCompanyDepartment.setTblEmpMaster(empMaster);
                this.tblMapCompanyDepartment.setMapLmd(utils.getDateFormat(utils.DateIn()));
                compDeptDao.save(this.tblMapCompanyDepartment);
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
    public TblMapCompanyDepartment getModel() {
        return this.tblMapCompanyDepartment;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
