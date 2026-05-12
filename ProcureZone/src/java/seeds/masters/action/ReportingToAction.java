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
import pojo.TblMapEmpReporting;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.EmployeeDaoImpl;
import seeds.masters.daoImpl.ReportToDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class ReportingToAction extends ActionSupport implements ModelDriven<TblMapEmpReporting>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblMapEmpReporting tblMapEmpReporting;
    private List<TblMapEmpReporting> listTblMapEmpReporting;
    private final ReportToDaoImpl reportToDao = DaoFactory.getDao(ReportToDaoImpl.class);
    private List<TblEmpMaster> listTblEmpMaster;
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);
    private List<TblEmpMaster> listTblEmpMaster1;

    private String message;
    private int empNumber;
    private int empNumber1;
    private String startDate;
    private String endDate;
    private static int rtId;

    public TblMapEmpReporting getTblMapEmpReporting() {
        return tblMapEmpReporting;
    }

    public void setTblMapEmpReporting(TblMapEmpReporting tblMapEmpReporting) {
        this.tblMapEmpReporting = tblMapEmpReporting;
    }

    public List<TblMapEmpReporting> getListTblMapEmpReporting() {
        return listTblMapEmpReporting;
    }

    public void setListTblMapEmpReporting(List<TblMapEmpReporting> listTblMapEmpReporting) {
        this.listTblMapEmpReporting = listTblMapEmpReporting;
    }

    public List<TblEmpMaster> getListTblEmpMaster() {
        return listTblEmpMaster;
    }

    public void setListTblEmpMaster(List<TblEmpMaster> listTblEmpMaster) {
        this.listTblEmpMaster = listTblEmpMaster;
    }

    public List<TblEmpMaster> getListTblEmpMaster1() {
        return listTblEmpMaster1;
    }

    public void setListTblEmpMaster1(List<TblEmpMaster> listTblEmpMaster1) {
        this.listTblEmpMaster1 = listTblEmpMaster1;
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

    public int getEmpNumber1() {
        return empNumber1;
    }

    public void setEmpNumber1(int empNumber1) {
        this.empNumber1 = empNumber1;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public static int getRtId() {
        return rtId;
    }

    public static void setRtId(int rtId) {
        ReportingToAction.rtId = rtId;
    }

    public ReportingToAction() throws Exception {
        tblMapEmpReporting = new TblMapEmpReporting();
        listTblMapEmpReporting = new ArrayList<TblMapEmpReporting>();
        listTblEmpMaster = new ArrayList<TblEmpMaster>();
        listTblEmpMaster1 = new ArrayList<TblEmpMaster>();
    }

    public String getList() {
        listTblMapEmpReporting = (ArrayList<TblMapEmpReporting>) reportToDao.getList("where reportStatus in (0,1)");
        if (!listTblMapEmpReporting.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void getEmployeeList() {
        listTblEmpMaster = employeeDao.getList("where empStatus=1 and empNumber not in (1)");
        listTblEmpMaster1 = employeeDao.getList("where empStatus=1 and empNumber not in (1)");
    }

    public String addReportingTo() {
        rtId = 0;
        getEmployeeList();
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (empNumber == 0) {
            addFieldError("empNumber", "Please Select Employee.");
            clear = false;
        }
        if (empNumber1 == 0) {
            addFieldError("empNumber1", "Please Select Reportingto.");
            clear = false;
        }
        if (startDate.length() == 0 && startDate != null) {
            addFieldError("startDate", "Please Enter Start Date.");
            clear = false;
        }
        if (endDate.length() == 0 && endDate != null) {
            addFieldError("endDate", "Please Enter End Date.");
            clear = false;
        }
        return clear;
    }

    public String saveReportingTo() throws ParseException {
        if (validation()) {
            listTblMapEmpReporting = (ArrayList<TblMapEmpReporting>) reportToDao.getList("where reportStatus in (0,1) and tblEmpMasterByReportSub.empNumber=" + empNumber + " and tblEmpMasterByReportSup.empNumber=" + empNumber1 + "");
                if (rtId != 0) {
                    tblMapEmpReporting = (TblMapEmpReporting) reportToDao.getById(rtId);
                } else {
                    if (listTblMapEmpReporting.isEmpty()) {
                        tblMapEmpReporting = new TblMapEmpReporting();
                    } else {
                        addFieldError("empNumber", "Duplicate Employee or Reportingto.");
                        getList();
                        getEmployeeList();
                        return INPUT;
                    }
                }

                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(empNumber);
                tblMapEmpReporting.setTblEmpMasterByReportSub(empMaster);
                TblEmpMaster empMaster1 = new TblEmpMaster();
                empMaster1.setEmpNumber(empNumber1);
                tblMapEmpReporting.setTblEmpMasterByReportSup(empMaster1);
                tblMapEmpReporting.setReportStart(utils.getDateFormat(startDate));
                tblMapEmpReporting.setReportEnd(utils.getDateFormat(endDate));
                tblMapEmpReporting.setReportStatus(1);
                TblEmpMaster empMaster2 = new TblEmpMaster();
                empMaster2.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblMapEmpReporting.setTblEmpMasterByReportLmu(empMaster2);
                tblMapEmpReporting.setReportLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = reportToDao.save(tblMapEmpReporting);
                if (result) {
                    setTblMapEmpReporting(new TblMapEmpReporting());
                    setEmpNumber(0);
                    setEmpNumber1(0);
                    setStartDate("");
                    setEndDate("");
                    setMessage("User Inserted Successfully");
                    return SUCCESS;
                } else {
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

    public String viewReportingTo() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("reporttoId");
        getEmployeeList();
        try {
            if (editId.length == 1) {
                this.tblMapEmpReporting = (TblMapEmpReporting) reportToDao.getById(Integer.parseInt(editId[0]));
                setEmpNumber(tblMapEmpReporting.getTblEmpMasterByReportSub().getEmpNumber());
                setEmpNumber1(tblMapEmpReporting.getTblEmpMasterByReportSup().getEmpNumber());
                setStartDate(tblMapEmpReporting.getReportStart().toString());
                setEndDate(tblMapEmpReporting.getReportEnd().toString());
                setRtId(tblMapEmpReporting.getReportId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateReportingTo() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("reporttoId");
        getEmployeeList();
        try {
            if (editId.length == 1) {
                this.tblMapEmpReporting = (TblMapEmpReporting) reportToDao.getById(Integer.parseInt(editId[0]));
                setEmpNumber(tblMapEmpReporting.getTblEmpMasterByReportSub().getEmpNumber());
                setEmpNumber1(tblMapEmpReporting.getTblEmpMasterByReportSup().getEmpNumber());
                setStartDate(tblMapEmpReporting.getReportStart().toString());
                setEndDate(tblMapEmpReporting.getReportEnd().toString());
                setRtId(tblMapEmpReporting.getReportId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteReportingTo() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("reporttoId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblMapEmpReporting = (TblMapEmpReporting) reportToDao.getById(Integer.parseInt(a));
                this.tblMapEmpReporting.setReportStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblMapEmpReporting.setTblEmpMasterByReportLmu(empMaster);
                this.tblMapEmpReporting.setReportLmd(utils.getDateFormat(utils.DateIn()));
                reportToDao.save(this.tblMapEmpReporting);
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
    public TblMapEmpReporting getModel() {
        return this.tblMapEmpReporting;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
