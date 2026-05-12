/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.action;

import static com.opensymphony.xwork2.Action.SUCCESS;
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
import pojo.TblEmpMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.CompanyDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class CompanyAction extends ActionSupport implements ModelDriven<TblCompanyMaster>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblCompanyMaster tblCompanyMaster;
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);

    private String message;
    private String companyCode;
    private String companyName;
    private static int compId;

    public TblCompanyMaster getTblCompanyMaster() {
        return tblCompanyMaster;
    }

    public void setTblCompanyMaster(TblCompanyMaster tblCompanyMaster) {
        this.tblCompanyMaster = tblCompanyMaster;
    }

    public List<TblCompanyMaster> getListTblCompanyMaster() {
        return listTblCompanyMaster;
    }

    public void setListTblCompanyMaster(List<TblCompanyMaster> listTblCompanyMaster) {
        this.listTblCompanyMaster = listTblCompanyMaster;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public static int getCompId() {
        return compId;
    }

    public static void setCompId(int compId) {
        CompanyAction.compId = compId;
    }

    public CompanyAction() throws Exception {
        tblCompanyMaster = new TblCompanyMaster();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
    }

    public String getList() {
        listTblCompanyMaster = (ArrayList<TblCompanyMaster>) companyDao.getList("where compStatus in (0,1)");
        if (!listTblCompanyMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String addCompany() {
        compId = 0;
        setCompanyCode("");
        setCompanyName("");
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (companyCode.length() == 0 && companyCode != null) {
            addFieldError("companyCode", "Please Enter Company Code.");
            clear = false;
        }
        if (companyName.length() == 0 && companyName != null) {
            addFieldError("companyName", "Please Enter Company Name.");
            clear = false;
        }
        return clear;
    }

    public String saveCompany() throws ParseException {
        if (validation()) {
            listTblCompanyMaster = (ArrayList<TblCompanyMaster>) companyDao.getList("where (compStatus in (0,1) and compCode='" + companyCode + "') or ( compStatus in (0,1) and compName='" + companyName + "')");
            //if (listTblCompanyMaster.isEmpty()) {
                if (compId != 0) {
                    tblCompanyMaster = (TblCompanyMaster) companyDao.getById(compId);
                } else {
                    if (listTblCompanyMaster.isEmpty()) {
                        tblCompanyMaster = new TblCompanyMaster();
                    }else {
                        addFieldError("companyCode", "Duplicate Company Code or Company Name.");
                        return INPUT;
                    }                    
                }
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblCompanyMaster.setCompCode(companyCode);
                tblCompanyMaster.setCompName(companyName);
                tblCompanyMaster.setCompStatus(1);
                tblCompanyMaster.setTblEmpMaster(empMaster);
                tblCompanyMaster.setCompLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = companyDao.save(tblCompanyMaster);
                if (result) {
                    setTblCompanyMaster(new TblCompanyMaster());
                    setCompanyCode("");
                    setCompanyName("");
                    setMessage("Company Inserted Successfully");
                    return SUCCESS;
                } else {
                    return INPUT;
                }
            /*} else {
                addFieldError("companyCode", "Duplicate Company Code or Company Name.");
                return INPUT;
            }*/
        } else {
            return INPUT;
        }
    }

    public String viewCompany() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("compId");
        try {
            if (editId.length == 1) {
                this.tblCompanyMaster = (TblCompanyMaster) companyDao.getById(Integer.parseInt(editId[0]));
                setCompanyCode(tblCompanyMaster.getCompCode());
                setCompanyName(tblCompanyMaster.getCompName());
                setCompId(tblCompanyMaster.getCompId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateCompany() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("compId");
        try {
            if (editId.length == 1) {
                this.tblCompanyMaster = (TblCompanyMaster) companyDao.getById(Integer.parseInt(editId[0]));
                setCompanyCode(tblCompanyMaster.getCompCode());
                setCompanyName(tblCompanyMaster.getCompName());
                setCompId(tblCompanyMaster.getCompId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteCompany() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("compId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblCompanyMaster = (TblCompanyMaster) companyDao.getById(Integer.parseInt(a));
                this.tblCompanyMaster.setCompStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblCompanyMaster.setTblEmpMaster(empMaster);
                this.tblCompanyMaster.setCompLmd(utils.getDateFormat(utils.DateIn()));
                companyDao.save(this.tblCompanyMaster);
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
    public TblCompanyMaster getModel() {
        return this.tblCompanyMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
