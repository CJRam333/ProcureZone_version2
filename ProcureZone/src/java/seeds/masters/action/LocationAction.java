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
import pojo.TblLocationMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.LocationDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class LocationAction extends ActionSupport implements ModelDriven<TblLocationMaster>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblLocationMaster tblLocationMaster;
    private List<TblLocationMaster> listTblLocationMaster;
    private final LocationDaoImpl locationDao = DaoFactory.getDao(LocationDaoImpl.class);

    private String message;
    private String locationCode;
    private String locationName;
    private static int locId;

    public TblLocationMaster getTblLocationMaster() {
        return tblLocationMaster;
    }

    public void setTblLocationMaster(TblLocationMaster tblLocationMaster) {
        this.tblLocationMaster = tblLocationMaster;
    }

    public List<TblLocationMaster> getListTblLocationMaster() {
        return listTblLocationMaster;
    }

    public void setListTblLocationMaster(List<TblLocationMaster> listTblLocationMaster) {
        this.listTblLocationMaster = listTblLocationMaster;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public static int getLocId() {
        return locId;
    }

    public static void setLocId(int locId) {
        LocationAction.locId = locId;
    }

    public LocationAction() throws Exception {
        tblLocationMaster = new TblLocationMaster();
        listTblLocationMaster = new ArrayList<TblLocationMaster>();
    }

    public String getList() {
        listTblLocationMaster = (ArrayList<TblLocationMaster>) locationDao.getList("where locStatus in (0,1)");
        if (!listTblLocationMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String addLocation() {
        locId = 0;
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (locationCode.length() == 0 && locationCode != null) {
            addFieldError("locationCode", "Please Enter Location Code.");
            clear = false;
        }
        if (locationName.length() == 0 && locationName != null) {
            addFieldError("locationName", "Please Enter Location Name.");
            clear = false;
        }
        return clear;
    }

    public String saveLocation() throws ParseException {
        if (validation()) {
            listTblLocationMaster = (ArrayList<TblLocationMaster>) locationDao.getList("where (locStatus in (0,1) and locCode='" + locationCode + "') or (locStatus in (0,1) and locName='" + locationName + "')");
            //if (listTblLocationMaster.isEmpty()) {
                if (locId != 0) {
                    tblLocationMaster = (TblLocationMaster) locationDao.getById(locId);
                } else {
                    if (listTblLocationMaster.isEmpty()) {
                        tblLocationMaster = new TblLocationMaster();
                    } else {
                        addFieldError("locationCode", "Duplicate Location Code or Location Name.");
                        return INPUT;
                    }
                }
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblLocationMaster.setLocCode(locationCode);
                tblLocationMaster.setLocName(locationName);
                tblLocationMaster.setLocStatus(1);
                tblLocationMaster.setTblEmpMaster(empMaster);
                tblLocationMaster.setLocLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = locationDao.save(tblLocationMaster);
                if (result) {
                    setTblLocationMaster(new TblLocationMaster());
                    setLocationCode("");
                    setLocationName("");
                    setMessage("Location Inserted Successfully");
                    return SUCCESS;
                } else {
                    return INPUT;
                }
            /*} else {
                addFieldError("locationCode", "Duplicate Location Code or Location Name.");
                return INPUT;
            }*/
        } else {
            return INPUT;
        }
    }

    public String viewLocation() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("locId");
        try {
            if (editId.length == 1) {
                this.tblLocationMaster = (TblLocationMaster) locationDao.getById(Integer.parseInt(editId[0]));
                setLocationCode(tblLocationMaster.getLocCode());
                setLocationName(tblLocationMaster.getLocName());
                setLocId(tblLocationMaster.getLocId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateLocation() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("locId");
        try {
            if (editId.length == 1) {
                this.tblLocationMaster = (TblLocationMaster) locationDao.getById(Integer.parseInt(editId[0]));
                setLocationCode(tblLocationMaster.getLocCode());
                setLocationName(tblLocationMaster.getLocName());
                setLocId(tblLocationMaster.getLocId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteLocation() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("locId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblLocationMaster = (TblLocationMaster) locationDao.getById(Integer.parseInt(a));
                this.tblLocationMaster.setLocStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblLocationMaster.setTblEmpMaster(empMaster);
                this.tblLocationMaster.setLocLmd(utils.getDateFormat(utils.DateIn()));
                locationDao.save(this.tblLocationMaster);
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
    public TblLocationMaster getModel() {
        return this.tblLocationMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
