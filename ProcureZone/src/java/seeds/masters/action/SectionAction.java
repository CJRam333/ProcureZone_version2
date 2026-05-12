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
import pojo.TblSectionMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.SectionDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class SectionAction extends ActionSupport implements ModelDriven<TblSectionMaster>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblSectionMaster tblSectionMaster;
    private List<TblSectionMaster> listTblSectionMaster;
    private final SectionDaoImpl sectionDao = DaoFactory.getDao(SectionDaoImpl.class);

    private String message;
    private String sectionCode;
    private String sectionName;
    private static int secId;

    public TblSectionMaster getTblSectionMaster() {
        return tblSectionMaster;
    }

    public void setTblSectionMaster(TblSectionMaster tblSectionMaster) {
        this.tblSectionMaster = tblSectionMaster;
    }

    public List<TblSectionMaster> getListTblSectionMaster() {
        return listTblSectionMaster;
    }

    public void setListTblSectionMaster(List<TblSectionMaster> listTblSectionMaster) {
        this.listTblSectionMaster = listTblSectionMaster;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public static int getSecId() {
        return secId;
    }

    public static void setSecId(int secId) {
        SectionAction.secId = secId;
    }

    public SectionAction() throws Exception {
        tblSectionMaster = new TblSectionMaster();
        listTblSectionMaster = new ArrayList<TblSectionMaster>();
    }

    public String getList() {
        listTblSectionMaster = (ArrayList<TblSectionMaster>) sectionDao.getList("where secStatus in (0,1)");
        if (!listTblSectionMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String addSection() {
        secId = 0;
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (sectionCode.length() == 0 && sectionCode != null) {
            addFieldError("sectionCode", "Please Enter Section Code.");
            clear = false;
        }
        if (sectionName.length() == 0 && sectionName != null) {
            addFieldError("sectionName", "Please Enter Section Name.");
            clear = false;
        }
        return clear;
    }

    public String saveSection() throws ParseException {
        if (validation()) {
            listTblSectionMaster = (ArrayList<TblSectionMaster>) sectionDao.getList("where (secStatus in (0,1) and secCode='" + sectionCode + "') or ( secStatus in (0,1) and secName='" + sectionName + "')");
            
                if (secId != 0) {
                    tblSectionMaster = (TblSectionMaster) sectionDao.getById(secId);
                } else {
                    if (listTblSectionMaster.isEmpty()) {
                        tblSectionMaster = new TblSectionMaster();
                    } else {
                        addFieldError("sectionCode", "Duplicate Section Code or Section Name.");
                        return INPUT;
                    }
                }
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblSectionMaster.setSecCode(sectionCode);
                tblSectionMaster.setSecName(sectionName);
                tblSectionMaster.setSecStatus(1);
                tblSectionMaster.setTblEmpMaster(empMaster);
                tblSectionMaster.setSecLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = sectionDao.save(tblSectionMaster);
                if (result) {
                    setTblSectionMaster(new TblSectionMaster());
                    setSectionCode("");
                    setSectionName("");
                    setMessage("Section Inserted Successfully");
                    return SUCCESS;
                } else {
                    return INPUT;
                }
        } else {
            return INPUT;
        }
    }

    public String viewSection() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("secId");
        try {
            if (editId.length == 1) {
                this.tblSectionMaster = (TblSectionMaster) sectionDao.getById(Integer.parseInt(editId[0]));
                setSectionCode(tblSectionMaster.getSecCode());
                setSectionName(tblSectionMaster.getSecName());
                setSecId(tblSectionMaster.getSecId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateSection() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("secId");
        try {
            if (editId.length == 1) {
                this.tblSectionMaster = (TblSectionMaster) sectionDao.getById(Integer.parseInt(editId[0]));
                setSectionCode(tblSectionMaster.getSecCode());
                setSectionName(tblSectionMaster.getSecName());
                setSecId(tblSectionMaster.getSecId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteSection() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("secId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblSectionMaster = (TblSectionMaster) sectionDao.getById(Integer.parseInt(a));
                this.tblSectionMaster.setSecStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblSectionMaster.setTblEmpMaster(empMaster);
                this.tblSectionMaster.setSecLmd(utils.getDateFormat(utils.DateIn()));
                sectionDao.save(this.tblSectionMaster);
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
    public TblSectionMaster getModel() {
        return this.tblSectionMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
