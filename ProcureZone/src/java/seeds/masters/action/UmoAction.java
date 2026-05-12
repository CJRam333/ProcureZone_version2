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
import pojo.TblUmoMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.UmoDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class UmoAction extends ActionSupport implements ModelDriven<TblUmoMaster>, SessionAware{
    
    private Map session;
    private final Utils utils = new Utils();
    private TblUmoMaster tblUmoMaster;
    private List<TblUmoMaster> listTblUmoMaster;
    private final UmoDaoImpl umoDao = DaoFactory.getDao(UmoDaoImpl.class);

    private String message;
    private String umCode;
    private String umName;
    private static int umoId;

    public TblUmoMaster getTblUmoMaster() {
        return tblUmoMaster;
    }

    public void setTblUmoMaster(TblUmoMaster tblUmoMaster) {
        this.tblUmoMaster = tblUmoMaster;
    }

    public List<TblUmoMaster> getListTblUmoMaster() {
        return listTblUmoMaster;
    }

    public void setListTblUmoMaster(List<TblUmoMaster> listTblUmoMaster) {
        this.listTblUmoMaster = listTblUmoMaster;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }    

    public String getUmCode() {
        return umCode;
    }

    public void setUmCode(String umCode) {
        this.umCode = umCode;
    }

    public String getUmName() {
        return umName;
    }

    public void setUmName(String umName) {
        this.umName = umName;
    }

    public static int getUmoId() {
        return umoId;
    }

    public static void setUmoId(int umoId) {
        UmoAction.umoId = umoId;
    }
    
    public UmoAction() throws Exception{
        tblUmoMaster=new TblUmoMaster();
        listTblUmoMaster=new ArrayList<TblUmoMaster>();
    }
    
    public String getList() {
        listTblUmoMaster = (ArrayList<TblUmoMaster>) umoDao.getList("where umoStatus in (0,1)");
        if (!listTblUmoMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String addUmo() {
        umoId = 0;
        setUmCode("");
        setUmName("");
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (umCode.length() == 0 && umCode != null) {
            addFieldError("umCode", "Please Enter UMO Code.");
            clear = false;
        }
        if (umName.length() == 0 && umName != null) {
            addFieldError("umName", "Please Enter UMO Name.");
            clear = false;
        }
        return clear;
    }

    public String saveUmo() throws ParseException {
        if (validation()) {
            listTblUmoMaster = (ArrayList<TblUmoMaster>) umoDao.getList("where (umoStatus in (0,1) and umoCode='" + umCode + "') or (umoStatus in (0,1) and umoName='" + umName + "')");
                if (umoId != 0) {
                    tblUmoMaster = (TblUmoMaster) umoDao.getById(umoId);
                } else {
                    if (listTblUmoMaster.isEmpty()) {
                        tblUmoMaster = new TblUmoMaster();
                    } else {
                        addFieldError("umCode", "Duplicate UMO Code or UMO Name.");
                        return INPUT;
                    }
                }
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblUmoMaster.setUmoCode(umCode);
                tblUmoMaster.setUmoName(umName);
                tblUmoMaster.setUmoStatus(1);
                tblUmoMaster.setTblEmpMaster(empMaster);
                tblUmoMaster.setUmoLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = umoDao.save(tblUmoMaster);
                if (result) {
                    setTblUmoMaster(new TblUmoMaster());
                    setUmCode("");
                    setUmName("");
                    setMessage("UMO Inserted Successfully");
                    return SUCCESS;
                } else {
                    return INPUT;
                }
        } else {
            return INPUT;
        }
    }

    public String viewUmo() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("umoId");
        try {
            if (editId.length == 1) {
                this.tblUmoMaster = (TblUmoMaster) umoDao.getById(Integer.parseInt(editId[0]));
                setUmCode(tblUmoMaster.getUmoCode());
                setUmName(tblUmoMaster.getUmoName());
                setUmoId(tblUmoMaster.getUmoId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateUmo() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("umoId");
        try {
            if (editId.length == 1) {
                this.tblUmoMaster = (TblUmoMaster) umoDao.getById(Integer.parseInt(editId[0]));
                 setUmCode(tblUmoMaster.getUmoCode());
                 setUmName(tblUmoMaster.getUmoName());
                 setUmoId(tblUmoMaster.getUmoId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteUmo() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("umoId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblUmoMaster = (TblUmoMaster) umoDao.getById(Integer.parseInt(a));
                this.tblUmoMaster.setUmoStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblUmoMaster.setTblEmpMaster(empMaster);
                this.tblUmoMaster.setUmoLmd(utils.getDateFormat(utils.DateIn()));
                umoDao.save(this.tblUmoMaster);
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
    public TblUmoMaster getModel() {
        return this.tblUmoMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session=map;
    }
    
}
