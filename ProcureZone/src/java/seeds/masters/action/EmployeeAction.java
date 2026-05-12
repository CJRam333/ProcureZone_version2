/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.action;

import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import pojo.TblCompanyMaster;
import pojo.TblDepartmentMaster;
import pojo.TblEmpMaster;
import pojo.TblLocationMaster;
import pojo.TblMapCompanyEmp;
import pojo.TblMapEmpReporting;
import pojo.TblMapEmpRoles;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.CompEmpDaoImpl;
import seeds.masters.daoImpl.CompanyDaoImpl;
import seeds.masters.daoImpl.DepartmentDaoImpl;
import seeds.masters.daoImpl.EmployeeDaoImpl;
import seeds.masters.daoImpl.EmployeeRolesDaoImpl;
import seeds.masters.daoImpl.LocationDaoImpl;
import seeds.masters.daoImpl.ReportToDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class EmployeeAction extends ActionSupport implements ModelDriven<TblEmpMaster>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblEmpMaster tblEmpMaster;
    private List<TblEmpMaster> listTblEmpMaster;
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private List<TblLocationMaster> listTblLocationMaster;
    private final LocationDaoImpl locationDao = DaoFactory.getDao(LocationDaoImpl.class);
    private List<TblDepartmentMaster> listTblDepartmentMaster;
    private final DepartmentDaoImpl departmentDao = DaoFactory.getDao(DepartmentDaoImpl.class);
    private TblMapCompanyEmp tblMapCompanyEmp;
    private List<TblMapCompanyEmp> listTblMapCompanyEmp;
    private final CompEmpDaoImpl compEmpDao = DaoFactory.getDao(CompEmpDaoImpl.class);
    private List<TblMapEmpRoles> listTblMapEmpRoles;
    private final EmployeeRolesDaoImpl employeeRolesDao = DaoFactory.getDao(EmployeeRolesDaoImpl.class);
    private List<TblMapEmpReporting> listTblMapEmpReporting;
    private final ReportToDaoImpl reportToDao = DaoFactory.getDao(ReportToDaoImpl.class);

    private String message;
    private String employeeId;
    private String employeeName;
    private String employeeEmail;
    private String employeeJoinDate;
    private int[] compId;
    private int locId;
    private int deptId;
    private String employeeDesig;
    private static int eId;
    private File fileUpload;
    private String fileUploadFileName;
    private String fileUploadContentType;
    private String employeeCost;

    public TblEmpMaster getTblEmpMaster() {
        return tblEmpMaster;
    }

    public void setTblEmpMaster(TblEmpMaster tblEmpMaster) {
        this.tblEmpMaster = tblEmpMaster;
    }

    public List<TblEmpMaster> getListTblEmpMaster() {
        return listTblEmpMaster;
    }

    public void setListTblEmpMaster(List<TblEmpMaster> listTblEmpMaster) {
        this.listTblEmpMaster = listTblEmpMaster;
    }

    public List<TblCompanyMaster> getListTblCompanyMaster() {
        return listTblCompanyMaster;
    }

    public void setListTblCompanyMaster(List<TblCompanyMaster> listTblCompanyMaster) {
        this.listTblCompanyMaster = listTblCompanyMaster;
    }

    public List<TblLocationMaster> getListTblLocationMaster() {
        return listTblLocationMaster;
    }

    public void setListTblLocationMaster(List<TblLocationMaster> listTblLocationMaster) {
        this.listTblLocationMaster = listTblLocationMaster;
    }

    public List<TblDepartmentMaster> getListTblDepartmentMaster() {
        return listTblDepartmentMaster;
    }

    public void setListTblDepartmentMaster(List<TblDepartmentMaster> listTblDepartmentMaster) {
        this.listTblDepartmentMaster = listTblDepartmentMaster;
    }

    public TblMapCompanyEmp getTblMapCompanyEmp() {
        return tblMapCompanyEmp;
    }

    public void setTblMapCompanyEmp(TblMapCompanyEmp tblMapCompanyEmp) {
        this.tblMapCompanyEmp = tblMapCompanyEmp;
    }

    public List<TblMapCompanyEmp> getListTblMapCompanyEmp() {
        return listTblMapCompanyEmp;
    }

    public void setListTblMapCompanyEmp(List<TblMapCompanyEmp> listTblMapCompanyEmp) {
        this.listTblMapCompanyEmp = listTblMapCompanyEmp;
    }

    public List<TblMapEmpRoles> getListTblMapEmpRoles() {
        return listTblMapEmpRoles;
    }

    public void setListTblMapEmpRoles(List<TblMapEmpRoles> listTblMapEmpRoles) {
        this.listTblMapEmpRoles = listTblMapEmpRoles;
    }

    public List<TblMapEmpReporting> getListTblMapEmpReporting() {
        return listTblMapEmpReporting;
    }

    public void setListTblMapEmpReporting(List<TblMapEmpReporting> listTblMapEmpReporting) {
        this.listTblMapEmpReporting = listTblMapEmpReporting;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getEmployeeJoinDate() {
        return employeeJoinDate;
    }

    public void setEmployeeJoinDate(String employeeJoinDate) {
        this.employeeJoinDate = employeeJoinDate;
    }

    public int[] getCompId() {
        return compId;
    }

    public void setCompId(int[] compId) {
        this.compId = compId;
    }

    public int getLocId() {
        return locId;
    }

    public void setLocId(int locId) {
        this.locId = locId;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getEmployeeDesig() {
        return employeeDesig;
    }

    public void setEmployeeDesig(String employeeDesig) {
        this.employeeDesig = employeeDesig;
    }

    public static int geteId() {
        return eId;
    }

    public static void seteId(int eId) {
        EmployeeAction.eId = eId;
    }

    public File getFileUpload() {
        return fileUpload;
    }

    public void setFileUpload(File fileUpload) {
        this.fileUpload = fileUpload;
    }

    public String getFileUploadFileName() {
        return fileUploadFileName;
    }

    public void setFileUploadFileName(String fileUploadFileName) {
        this.fileUploadFileName = fileUploadFileName;
    }

    public String getFileUploadContentType() {
        return fileUploadContentType;
    }

    public void setFileUploadContentType(String fileUploadContentType) {
        this.fileUploadContentType = fileUploadContentType;
    }

    public String getEmployeeCost() {
        return employeeCost;
    }

    public void setEmployeeCost(String employeeCost) {
        this.employeeCost = employeeCost;
    }

    public EmployeeAction() throws Exception {
        tblEmpMaster = new TblEmpMaster();
        listTblEmpMaster = new ArrayList<TblEmpMaster>();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
        listTblLocationMaster = new ArrayList<TblLocationMaster>();
        listTblDepartmentMaster = new ArrayList<TblDepartmentMaster>();
        tblMapCompanyEmp = new TblMapCompanyEmp();
        listTblMapCompanyEmp = new ArrayList<TblMapCompanyEmp>();
        listTblMapEmpRoles = new ArrayList<TblMapEmpRoles>();
        listTblMapEmpReporting = new ArrayList<TblMapEmpReporting>();
    }

    public String getList() {
        listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus in  (0,1)");
        if (!listTblEmpMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void getCompLocDeptList() {
        listTblCompanyMaster = companyDao.getList("where compStatus=1");
        listTblLocationMaster = locationDao.getList("where locStatus=1");
        listTblDepartmentMaster = departmentDao.getList("where deptStatus=1");
    }

    public String addEmployee() {
        eId = 0;
        getCompLocDeptList();
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (employeeId.length() == 0 && employeeId != null) {
            addFieldError("employeeId", "Please Enter Employee Id.");
            clear = false;
        }
        if (employeeName.length() == 0 && employeeName != null) {
            addFieldError("employeeName", "Please Enter Employee Name.");
            clear = false;
        }
        if (employeeEmail.length() == 0 && employeeEmail != null) {
            addFieldError("employeeEmail", "Please Enter Employee Email.");
            clear = false;
        }
        if (employeeJoinDate.length() == 0 && employeeJoinDate != null) {
            addFieldError("employeeJoinDate", "Please Enter Employee Joining Date.");
            clear = false;
        }
        if (compId.length == 0) {
            addFieldError("compId", "Please Select Company.");
            clear = false;
        }
        if (locId == 0) {
            addFieldError("locId", "Please Select Location.");
            clear = false;
        }
        if (deptId == 0) {
            addFieldError("deptId", "Please Select Department.");
            clear = false;
        }
        if (employeeDesig.length() == 0 && employeeDesig != null) {
            addFieldError("employeeDesig", "Please Enter Designation.");
            clear = false;
        }
        if (employeeCost.length() == 0 && employeeCost != null) {
            addFieldError("employeeCost", "Please Enter Cost Center.");
            clear = false;
        }
        return clear;
    }

    public String saveEmployee() throws ParseException, IOException {
        if (validation()) {
            if (eId != 0) {
                tblEmpMaster = (TblEmpMaster) employeeDao.getById(eId);
            } else {
                listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus in (0,1) and empId='" + employeeId + "' and empName='" + employeeName + "' and empEmail='" + employeeEmail + "'");
                if (listTblEmpMaster.isEmpty()) {
                    tblEmpMaster = new TblEmpMaster();
                } else {
                    addFieldError("employeeId", "Duplicate Employee Id or Employee Name or Employee Email.");
                    getList();
                    getCompLocDeptList();
                    return INPUT;
                }
            }
            tblEmpMaster.setEmpId(employeeId);
            tblEmpMaster.setEmpName(employeeName);
            tblEmpMaster.setEmpEmail(employeeEmail);
            tblEmpMaster.setEmpJoinDate(utils.getDateFormat(employeeJoinDate));
            TblLocationMaster locMaster = new TblLocationMaster();
            locMaster.setLocId(locId);
            tblEmpMaster.setTblLocationMaster(locMaster);
            TblDepartmentMaster deptMaster = new TblDepartmentMaster();
            deptMaster.setDeptId(deptId);
            tblEmpMaster.setTblDepartmentMaster(deptMaster);
            tblEmpMaster.setEmpDesignation(employeeDesig);
            tblEmpMaster.setEmpStatus(1);
            tblEmpMaster.setEmpLmd(utils.getDateFormat(utils.DateIn()));
            tblEmpMaster.setEmpPassword(utils.getEncript(employeeId));
            tblEmpMaster.setEmpCostCenter(employeeCost);
            HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get("com.opensymphony.xwork2.dispatcher.HttpServletRequest");
            String filePath = request.getRealPath("./images/emp");
            String filePath1 = "./images/emp";
            String strDirectoy = filePath;
            new File(strDirectoy).mkdirs();
            if (fileUpload != null) {
                File fileToCreate = new File(filePath, this.fileUploadFileName);
                FileUtils.copyFile(this.fileUpload, fileToCreate);
                filePath1 = filePath1 + "/" + this.fileUploadFileName;
                tblEmpMaster.setEmpPath(filePath1);
            }
            boolean result = employeeDao.save(tblEmpMaster);
            if (result) {
                listTblMapCompanyEmp = (ArrayList<TblMapCompanyEmp>) compEmpDao.getList("where mapStatus=1 and tblEmpMasterByMapEmp.empNumber=" + tblEmpMaster.getEmpNumber() + "");
                if (listTblMapCompanyEmp.isEmpty()) {
                    for (int i = 0; i < compId.length; i++) {
                        tblMapCompanyEmp = new TblMapCompanyEmp();
                        TblCompanyMaster compMaster = new TblCompanyMaster();
                        compMaster.setCompId(compId[i]);
                        tblMapCompanyEmp.setTblCompanyMaster(compMaster);
                        tblMapCompanyEmp.setTblEmpMasterByMapEmp(tblEmpMaster);
                        tblMapCompanyEmp.setMapStatus(1);
                        tblMapCompanyEmp.setTblEmpMasterByMapLmu(tblEmpMaster);
                        tblMapCompanyEmp.setMapLmd(utils.getDateFormat(utils.DateIn()));
                        compEmpDao.save(tblMapCompanyEmp);
                    }
                } else {
                    try {
                        int i = 0;
                        if (listTblMapCompanyEmp.size() == compId.length) {
                            for (TblMapCompanyEmp compEmp : listTblMapCompanyEmp) {
                                TblCompanyMaster compMaster = new TblCompanyMaster();
                                compMaster.setCompId(compId[i]);
                                compEmp.setTblCompanyMaster(compMaster);
                                compEmp.setTblEmpMasterByMapLmu(tblEmpMaster);
                                compEmp.setMapLmd(utils.getDateFormat(utils.DateIn()));
                                if (compEmp.getMapStatus() == 0) {
                                    compEmp.setMapStatus(1);
                                }
                                compEmpDao.save(compEmp);
                                i++;
                            }
                        }
                        if (listTblMapCompanyEmp.size() > compId.length) {
                            Iterator it = listTblMapCompanyEmp.iterator();
                            int j = 0;
                            while (it.hasNext()) {
                                tblMapCompanyEmp = (TblMapCompanyEmp) it.next();
                                try {
                                    TblCompanyMaster compMaster = new TblCompanyMaster();
                                    compMaster.setCompId(compId[j]);
                                    tblMapCompanyEmp.setTblCompanyMaster(compMaster);
                                    tblMapCompanyEmp.setTblEmpMasterByMapLmu(tblEmpMaster);
                                    tblMapCompanyEmp.setMapLmd(utils.getDateFormat(utils.DateIn()));
                                    if (tblMapCompanyEmp.getMapStatus() == 0) {
                                        tblMapCompanyEmp.setMapStatus(1);
                                    }
                                    compEmpDao.save(tblMapCompanyEmp);
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    tblMapCompanyEmp.setMapStatus(0);
                                    tblMapCompanyEmp.setTblEmpMasterByMapLmu(tblEmpMaster);
                                    tblMapCompanyEmp.setMapLmd(utils.getDateFormat(utils.DateIn()));
                                    compEmpDao.save(tblMapCompanyEmp);
                                }
                                j++;
                            }
                        }
                        if (listTblMapCompanyEmp.size() < compId.length) {
                            int k = 0;
                            for (TblMapCompanyEmp compEmp : listTblMapCompanyEmp) {
                                TblCompanyMaster compMaster = new TblCompanyMaster();
                                compMaster.setCompId(compId[k]);
                                compEmp.setTblCompanyMaster(compMaster);
                                compEmp.setTblEmpMasterByMapLmu(tblEmpMaster);
                                compEmp.setMapLmd(utils.getDateFormat(utils.DateIn()));
                                if (compEmp.getMapStatus() == 0) {
                                    compEmp.setMapStatus(1);
                                }
                                compEmpDao.save(compEmp);
                                k++;
                            }
                            TblCompanyMaster compMaster1 = new TblCompanyMaster();
                            compMaster1.setCompId(compId[k]);
                            tblMapCompanyEmp.setTblCompanyMaster(compMaster1);
                            tblMapCompanyEmp.setTblEmpMasterByMapEmp(tblEmpMaster);
                            tblMapCompanyEmp.setMapStatus(1);
                            tblMapCompanyEmp.setTblEmpMasterByMapLmu(tblEmpMaster);
                            tblMapCompanyEmp.setMapLmd(utils.getDateFormat(utils.DateIn()));
                            compEmpDao.save(tblMapCompanyEmp);

                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        tblMapCompanyEmp.setMapStatus(0);
                        tblMapCompanyEmp.setTblEmpMasterByMapLmu(tblEmpMaster);
                        tblMapCompanyEmp.setMapLmd(utils.getDateFormat(utils.DateIn()));
                        compEmpDao.save(tblMapCompanyEmp);
                    }

                    /*Iterator it = listTblMapCompanyEmp.iterator();
                     int i = 0;
                     while (it.hasNext()) {
                     tblMapCompanyEmp = (TblMapCompanyEmp) it.next();
                     try {
                     TblCompanyMaster compMaster = new TblCompanyMaster();
                     compMaster.setCompId(compId[i]);
                     tblMapCompanyEmp.setTblCompanyMaster(compMaster);
                     tblMapCompanyEmp.setTblEmpMasterByMapLmu(tblEmpMaster);
                     tblMapCompanyEmp.setMapLmd(utils.getDateFormat(utils.DateIn()));
                     if (tblMapCompanyEmp.getMapStatus() == 0) {
                     tblMapCompanyEmp.setMapStatus(1);
                     }
                     compEmpDao.save(tblMapCompanyEmp);
                     } catch (ArrayIndexOutOfBoundsException e) {
                     tblMapCompanyEmp.setMapStatus(0);
                     tblMapCompanyEmp.setTblEmpMasterByMapLmu(tblEmpMaster);
                     tblMapCompanyEmp.setMapLmd(utils.getDateFormat(utils.DateIn()));
                     compEmpDao.save(tblMapCompanyEmp);
                     }
                     i++;
                     }*/
                }
                setTblEmpMaster(new TblEmpMaster());
                setEmployeeId("");
                setEmployeeName("");
                setEmployeeEmail("");
                setEmployeeJoinDate("");
                setLocId(0);
                setDeptId(0);
                setEmployeeDesig("");
                setEmployeeCost("");
                setMessage("Employee Inserted Successfully");
                return SUCCESS;
            } else {
                getList();
                getCompLocDeptList();
                return INPUT;
            }
        } else {
            getList();
            getCompLocDeptList();
            return INPUT;
        }
    }

    public String updateEmployee() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("empId");
        int[] comEmp = new int[10];
        getCompLocDeptList();
        try {
            if (editId.length == 1) {
                this.tblEmpMaster = (TblEmpMaster) employeeDao.getById(Integer.parseInt(editId[0]));
                setEmployeeId(tblEmpMaster.getEmpId());
                setEmployeeName(tblEmpMaster.getEmpName());
                setEmployeeEmail(tblEmpMaster.getEmpEmail());
                setEmployeeJoinDate(tblEmpMaster.getEmpJoinDate().toString());
                setLocId(tblEmpMaster.getTblLocationMaster().getLocId());
                setDeptId(tblEmpMaster.getTblDepartmentMaster().getDeptId());
                setEmployeeDesig(tblEmpMaster.getEmpDesignation());
                setEmployeeCost(tblEmpMaster.getEmpCostCenter());
                seteId(tblEmpMaster.getEmpNumber());
                listTblMapCompanyEmp = (ArrayList<TblMapCompanyEmp>) compEmpDao.getList("where mapStatus=1 and tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(editId[0]) + "");
                Iterator it = listTblMapCompanyEmp.iterator();
                int i = 0;
                while (it.hasNext()) {
                    tblMapCompanyEmp = (TblMapCompanyEmp) it.next();
                    comEmp[i] = tblMapCompanyEmp.getTblCompanyMaster().getCompId();
                    i++;
                }
                setCompId(comEmp);
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteEmployee() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("empId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblEmpMaster = (TblEmpMaster) employeeDao.getById(Integer.parseInt(a));
                this.tblEmpMaster.setEmpStatus(0);
                this.tblEmpMaster.setEmpLmd(utils.getDateFormat(utils.DateIn()));
                employeeDao.save(this.tblEmpMaster);
                listTblMapCompanyEmp = (ArrayList<TblMapCompanyEmp>) compEmpDao.getList("where mapStatus=1 and tblEmpMasterByMapEmp.empNumber=" + tblEmpMaster.getEmpNumber() + "");
                
                for (TblMapCompanyEmp CompanyEmp : listTblMapCompanyEmp) {
                    CompanyEmp.setMapStatus(0);
                    CompanyEmp.setMapLmd(utils.getDateFormat(utils.DateIn()));
                    compEmpDao.save(CompanyEmp);
                }
                listTblMapEmpRoles = (ArrayList<TblMapEmpRoles>) employeeRolesDao.getList("where empRolesStatus=1 and tblEmpMasterByEmpNumber.empNumber= " + tblEmpMaster.getEmpNumber() + "");
                for (TblMapEmpRoles empRoles : listTblMapEmpRoles) {
                    empRoles.setEmpRolesStatus(0);
                    empRoles.setTblEmpMasterByEmpRolesLmu(tblEmpMaster);
                    empRoles.setEmpRolesLmd(utils.getDateFormat(utils.DateIn()));
                    employeeRolesDao.save(empRoles);
                }
                listTblMapEmpReporting = (ArrayList<TblMapEmpReporting>) reportToDao.getList("where reportStatus=1 and tblEmpMasterByReportSub.empNumber= " + tblEmpMaster.getEmpNumber() + "");
                for (TblMapEmpReporting empReporting : listTblMapEmpReporting) {
                    empReporting.setReportStatus(0);
                    empReporting.setTblEmpMasterByReportLmu(tblEmpMaster);
                    empReporting.setReportLmd(utils.getDateFormat(utils.DateIn()));
                    empReporting.setReportEnd(utils.getDateFormat(utils.DateIn()));
                    reportToDao.save(empReporting);
                }
            }
        }
        getList();
        return SUCCESS;
    }

    public String viewEmployee() {
        getCompLocDeptList();
        int[] comEmp = new int[10];
        try {
            this.tblEmpMaster = (TblEmpMaster) employeeDao.getById(Integer.parseInt(this.session.get("empNumber").toString()));
            setEmployeeId(tblEmpMaster.getEmpId());
            setEmployeeName(tblEmpMaster.getEmpName());
            setEmployeeEmail(tblEmpMaster.getEmpEmail());
            setEmployeeJoinDate(tblEmpMaster.getEmpJoinDate().toString());
            setLocId(tblEmpMaster.getTblLocationMaster().getLocId());
            setDeptId(tblEmpMaster.getTblDepartmentMaster().getDeptId());
            setEmployeeDesig(tblEmpMaster.getEmpDesignation());
            seteId(tblEmpMaster.getEmpNumber());
            setEmployeeCost(tblEmpMaster.getEmpCostCenter());
            listTblMapCompanyEmp = (ArrayList<TblMapCompanyEmp>) compEmpDao.getList("where mapStatus=1 and tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(this.session.get("empNumber").toString()) + "");
            Iterator it = listTblMapCompanyEmp.iterator();
            int i = 0;
            while (it.hasNext()) {
                tblMapCompanyEmp = (TblMapCompanyEmp) it.next();
                comEmp[i] = tblMapCompanyEmp.getTblCompanyMaster().getCompId();
                i++;
            }
            setCompId(comEmp);
            return SUCCESS;

        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String viewEmployee1() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("empId");
        getCompLocDeptList();
        int[] comEmp = new int[10];
        try {
            if (editId.length == 1) {
                this.tblEmpMaster = (TblEmpMaster) employeeDao.getById(Integer.parseInt(editId[0]));
                setEmployeeId(tblEmpMaster.getEmpId());
                setEmployeeName(tblEmpMaster.getEmpName());
                setEmployeeEmail(tblEmpMaster.getEmpEmail());
                setEmployeeJoinDate(tblEmpMaster.getEmpJoinDate().toString());
                setLocId(tblEmpMaster.getTblLocationMaster().getLocId());
                setDeptId(tblEmpMaster.getTblDepartmentMaster().getDeptId());
                setEmployeeDesig(tblEmpMaster.getEmpDesignation());
                seteId(tblEmpMaster.getEmpNumber());
                setEmployeeCost(tblEmpMaster.getEmpCostCenter());
                listTblMapCompanyEmp = (ArrayList<TblMapCompanyEmp>) compEmpDao.getList("where mapStatus=1 and tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(editId[0]) + "");
                Iterator it = listTblMapCompanyEmp.iterator();
                int i = 0;
                while (it.hasNext()) {
                    tblMapCompanyEmp = (TblMapCompanyEmp) it.next();
                    comEmp[i] = tblMapCompanyEmp.getTblCompanyMaster().getCompId();
                    i++;
                }
                setCompId(comEmp);
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    public TblEmpMaster getModel() {
        return this.tblEmpMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
