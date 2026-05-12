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
import pojo.TblEmpMaster;
import pojo.TblLocationMaster;
import pojo.TblMapCompanyLocation;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.CompLocDaoImpl;
import seeds.masters.daoImpl.CompanyDaoImpl;
import seeds.masters.daoImpl.LocationDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class CompLocAction extends ActionSupport implements ModelDriven<TblMapCompanyLocation>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblMapCompanyLocation tblMapCompanyLocation;
    private List<TblMapCompanyLocation> listTblMapCompanyLocation;
    private final CompLocDaoImpl compLocDao = DaoFactory.getDao(CompLocDaoImpl.class);
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private List<TblLocationMaster> listTblLocationMaster;
    private final LocationDaoImpl locationDao = DaoFactory.getDao(LocationDaoImpl.class);

    private String message;
    private int compId;
    private int locId;
    private static int genId;

    public TblMapCompanyLocation getTblMapCompanyLocation() {
        return tblMapCompanyLocation;
    }

    public void setTblMapCompanyLocation(TblMapCompanyLocation tblMapCompanyLocation) {
        this.tblMapCompanyLocation = tblMapCompanyLocation;
    }

    public List<TblMapCompanyLocation> getListTblMapCompanyLocation() {
        return listTblMapCompanyLocation;
    }

    public void setListTblMapCompanyLocation(List<TblMapCompanyLocation> listTblMapCompanyLocation) {
        this.listTblMapCompanyLocation = listTblMapCompanyLocation;
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

    public int getLocId() {
        return locId;
    }

    public void setLocId(int locId) {
        this.locId = locId;
    }

    public static int getGenId() {
        return genId;
    }

    public static void setGenId(int genId) {
        CompLocAction.genId = genId;
    }

    public CompLocAction() throws Exception {
        tblMapCompanyLocation = new TblMapCompanyLocation();
        listTblMapCompanyLocation = new ArrayList<TblMapCompanyLocation>();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
        listTblLocationMaster = new ArrayList<TblLocationMaster>();
    }

    public String getList() {
        listTblMapCompanyLocation = (ArrayList<TblMapCompanyLocation>) compLocDao.getList("where mapStatus in (0,1)");
        if (!listTblMapCompanyLocation.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void getCompLocList() {
        listTblCompanyMaster = companyDao.getList("where compStatus=1");
        listTblLocationMaster = locationDao.getList("where locStatus=1");
    }

    public String addCompLoc() {
        genId = 0;
        getCompLocList();
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (compId == 0) {
            addFieldError("compId", "Please Select Company.");
            clear = false;
        }
        if (locId == 0) {
            addFieldError("locId", "Please Select Location.");
            clear = false;
        }
        return clear;
    }

    public String saveCompLoc() throws ParseException {
        if (validation()) {
            listTblMapCompanyLocation = (ArrayList<TblMapCompanyLocation>) compLocDao.getList("where mapStatus in (0,1) and tblCompanyMaster.compId=" + compId + " and tblLocationMaster.locId =" + locId + "");

            if (genId != 0) {
                tblMapCompanyLocation = (TblMapCompanyLocation) compLocDao.getById(genId);
            } else {
                if (listTblMapCompanyLocation.isEmpty()) {
                    tblMapCompanyLocation = new TblMapCompanyLocation();
                } else {
                    addFieldError("compId", "Duplicate Company or Location.");
                    getList();
                    getCompLocList();
                    return INPUT;
                }
            }
            TblEmpMaster empMaster = new TblEmpMaster();
            empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
            TblCompanyMaster compMaster = new TblCompanyMaster();
            compMaster.setCompId(compId);
            tblMapCompanyLocation.setTblCompanyMaster(compMaster);
            TblLocationMaster locMaster = new TblLocationMaster();
            locMaster.setLocId(locId);
            tblMapCompanyLocation.setTblLocationMaster(locMaster);
            tblMapCompanyLocation.setMapStatus(1);
            tblMapCompanyLocation.setTblEmpMaster(empMaster);
            tblMapCompanyLocation.setMapLmd(utils.getDateFormat(utils.DateIn()));
            boolean result = compLocDao.save(tblMapCompanyLocation);
            if (result) {
                setTblMapCompanyLocation(new TblMapCompanyLocation());
                setCompId(0);
                setLocId(0);
                setMessage("Company Location Map Inserted Successfully");
                return SUCCESS;
            } else {
                getList();
                getCompLocList();
                return INPUT;
            }
        } else {
            getList();
            getCompLocList();
            return INPUT;
        }
    }

    public String viewCompLoc() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("compLocId");
        getCompLocList();
        try {
            if (editId.length == 1) {
                this.tblMapCompanyLocation = (TblMapCompanyLocation) compLocDao.getById(Integer.parseInt(editId[0]));
                setCompId(tblMapCompanyLocation.getTblCompanyMaster().getCompId());
                setLocId(tblMapCompanyLocation.getTblLocationMaster().getLocId());
                setGenId(tblMapCompanyLocation.getMapId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateCompLoc() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("compLocId");
        getCompLocList();
        try {
            if (editId.length == 1) {
                this.tblMapCompanyLocation = (TblMapCompanyLocation) compLocDao.getById(Integer.parseInt(editId[0]));
                setCompId(tblMapCompanyLocation.getTblCompanyMaster().getCompId());
                setLocId(tblMapCompanyLocation.getTblLocationMaster().getLocId());
                setGenId(tblMapCompanyLocation.getMapId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteCompLoc() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("compLocId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblMapCompanyLocation = (TblMapCompanyLocation) compLocDao.getById(Integer.parseInt(a));
                this.tblMapCompanyLocation.setMapStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblMapCompanyLocation.setTblEmpMaster(empMaster);
                this.tblMapCompanyLocation.setMapLmd(utils.getDateFormat(utils.DateIn()));
                compLocDao.save(this.tblMapCompanyLocation);
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
    public TblMapCompanyLocation getModel() {
        return this.tblMapCompanyLocation;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
