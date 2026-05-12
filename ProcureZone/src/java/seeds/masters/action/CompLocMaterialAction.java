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
import pojo.TblMapCompanyLocationMaterial;
import pojo.TblMaterialMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.CompLocMaterialDaoImpl;
import seeds.masters.daoImpl.CompanyDaoImpl;
import seeds.masters.daoImpl.LocationDaoImpl;
import seeds.masters.daoImpl.MaterialDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class CompLocMaterialAction extends ActionSupport implements ModelDriven<TblMapCompanyLocationMaterial>, SessionAware{
    private Map session;
    private final Utils utils = new Utils();
    private TblMapCompanyLocationMaterial tblMapCompanyLocationMaterial;
    private List<TblMapCompanyLocationMaterial> listTblMapCompanyLocationMaterial;
    private final CompLocMaterialDaoImpl compLocMaterialDao = DaoFactory.getDao(CompLocMaterialDaoImpl.class);
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private List<TblLocationMaster> listTblLocationMaster;
    private final LocationDaoImpl locationDao = DaoFactory.getDao(LocationDaoImpl.class);
    private List<TblMaterialMaster> listTblMaterialMaster;
    private final MaterialDaoImpl materialDao=DaoFactory.getDao(MaterialDaoImpl.class);

    private String message;
    private int compId;
    private int locId;
    private int matId;
    private static int genId;

    public TblMapCompanyLocationMaterial getTblMapCompanyLocationMaterial() {
        return tblMapCompanyLocationMaterial;
    }

    public void setTblMapCompanyLocationMaterial(TblMapCompanyLocationMaterial tblMapCompanyLocationMaterial) {
        this.tblMapCompanyLocationMaterial = tblMapCompanyLocationMaterial;
    }

    public List<TblMapCompanyLocationMaterial> getListTblMapCompanyLocationMaterial() {
        return listTblMapCompanyLocationMaterial;
    }

    public void setListTblMapCompanyLocationMaterial(List<TblMapCompanyLocationMaterial> listTblMapCompanyLocationMaterial) {
        this.listTblMapCompanyLocationMaterial = listTblMapCompanyLocationMaterial;
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

    public List<TblMaterialMaster> getListTblMaterialMaster() {
        return listTblMaterialMaster;
    }

    public void setListTblMaterialMaster(List<TblMaterialMaster> listTblMaterialMaster) {
        this.listTblMaterialMaster = listTblMaterialMaster;
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

    public int getMatId() {
        return matId;
    }

    public void setMatId(int matId) {
        this.matId = matId;
    }

    public static int getGenId() {
        return genId;
    }

    public static void setGenId(int genId) {
        CompLocMaterialAction.genId = genId;
    }

    public CompLocMaterialAction() throws Exception {
        tblMapCompanyLocationMaterial = new TblMapCompanyLocationMaterial();
        listTblMapCompanyLocationMaterial = new ArrayList<TblMapCompanyLocationMaterial>();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
        listTblLocationMaster = new ArrayList<TblLocationMaster>();
        listTblMaterialMaster=new ArrayList<TblMaterialMaster>();
    }

    public String getList() {
        listTblMapCompanyLocationMaterial = (ArrayList<TblMapCompanyLocationMaterial>) compLocMaterialDao.getList("where mapStatus=1");
        if (!listTblMapCompanyLocationMaterial.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void getCompLocMaterialList() {
        listTblCompanyMaster = companyDao.getList("where compStatus=1");
        listTblLocationMaster = locationDao.getList("where locStatus=1");
        listTblMaterialMaster=materialDao.getList("where materialStatus=1");
    }

    public String addCompLocMaterial() {
        genId = 0;
        getCompLocMaterialList();
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
        if (matId == 0) {
            addFieldError("matId", "Please Select Material.");
            clear = false;
        }
        return clear;
    }

    public String saveCompLocMaterial() throws ParseException {
        if (validation()) {
            listTblMapCompanyLocationMaterial = (ArrayList<TblMapCompanyLocationMaterial>) compLocMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + compId + " and tblLocationMaster.locId =" + locId + " and tblMaterialMaster.materialId=" + matId + "");
            if (listTblMapCompanyLocationMaterial.isEmpty()) {
                if (genId != 0) {
                    tblMapCompanyLocationMaterial = (TblMapCompanyLocationMaterial) compLocMaterialDao.getById(genId);
                } else {
                    tblMapCompanyLocationMaterial = new TblMapCompanyLocationMaterial();
                }
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                TblCompanyMaster compMaster = new TblCompanyMaster();
                compMaster.setCompId(compId);
                tblMapCompanyLocationMaterial.setTblCompanyMaster(compMaster);
                TblLocationMaster locMaster = new TblLocationMaster();
                locMaster.setLocId(locId);                
                tblMapCompanyLocationMaterial.setTblLocationMaster(locMaster);
                TblMaterialMaster materialMaster=new TblMaterialMaster();
                materialMaster.setMaterialId(matId);
                tblMapCompanyLocationMaterial.setTblMaterialMaster(materialMaster);
                tblMapCompanyLocationMaterial.setMapStatus(1);
                tblMapCompanyLocationMaterial.setTblEmpMaster(empMaster);
                tblMapCompanyLocationMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = compLocMaterialDao.save(tblMapCompanyLocationMaterial);
                if (result) {
                    setTblMapCompanyLocationMaterial(new TblMapCompanyLocationMaterial());
                    setCompId(0);
                    setLocId(0);
                    setMatId(0);
                    setMessage("Company Location Material Map Inserted Successfully");
                    return SUCCESS;
                } else {
                    getList();
                    getCompLocMaterialList();
                    return INPUT;
                }
            } else {
                addFieldError("compId", "Duplicate Company or Location or Material.");
                getList();
                getCompLocMaterialList();
                return INPUT;
            }
        } else {
            getList();
            getCompLocMaterialList();
            return INPUT;
        }
    }

    public String viewCompLocMaterial() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("compLocMaterialId");
        getCompLocMaterialList();
        try {
            if (editId.length == 1) {
                this.tblMapCompanyLocationMaterial = (TblMapCompanyLocationMaterial) compLocMaterialDao.getById(Integer.parseInt(editId[0]));
                setCompId(tblMapCompanyLocationMaterial.getTblCompanyMaster().getCompId());
                setLocId(tblMapCompanyLocationMaterial.getTblLocationMaster().getLocId());
                setMatId(tblMapCompanyLocationMaterial.getTblMaterialMaster().getMaterialId());
                setGenId(tblMapCompanyLocationMaterial.getMapId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateCompLocMaterial() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("compLocMaterialId");
        getCompLocMaterialList();
        try {
            if (editId.length == 1) {
                this.tblMapCompanyLocationMaterial = (TblMapCompanyLocationMaterial) compLocMaterialDao.getById(Integer.parseInt(editId[0]));
                setCompId(tblMapCompanyLocationMaterial.getTblCompanyMaster().getCompId());
                setLocId(tblMapCompanyLocationMaterial.getTblLocationMaster().getLocId());
                setMatId(tblMapCompanyLocationMaterial.getTblMaterialMaster().getMaterialId());
                setGenId(tblMapCompanyLocationMaterial.getMapId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteCompLocMaterial() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("compLocMaterialId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblMapCompanyLocationMaterial = (TblMapCompanyLocationMaterial) compLocMaterialDao.getById(Integer.parseInt(a));
                this.tblMapCompanyLocationMaterial.setMapStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblMapCompanyLocationMaterial.setTblEmpMaster(empMaster);
                this.tblMapCompanyLocationMaterial.setMapLmd(utils.getDateFormat(utils.DateIn2()));
                compLocMaterialDao.save(this.tblMapCompanyLocationMaterial);
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
    public TblMapCompanyLocationMaterial getModel() {
        return this.tblMapCompanyLocationMaterial;
    }

    @Override
    public void setSession(Map map) {
        this.session=map;
    }
    
}
