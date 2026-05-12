/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.action;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import pojo.TblCompanyMaster;
import pojo.TblEmpMaster;
import pojo.TblMapCompanyPlantMaterial;
import pojo.TblMaterialMaster;
import pojo.TblPlantMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.CompPlantMaterialDaoImpl;
import seeds.masters.daoImpl.CompanyDaoImpl;
import seeds.masters.daoImpl.MaterialDaoImpl;
import seeds.masters.daoImpl.PlantDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class CompPlantMaterialAction extends ActionSupport implements ModelDriven<TblMapCompanyPlantMaterial>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblMapCompanyPlantMaterial tblMapCompanyPlantMaterial;
    private List<TblMapCompanyPlantMaterial> listTblMapCompanyPlantMaterial;
    private final CompPlantMaterialDaoImpl compPlantMaterialDao = DaoFactory.getDao(CompPlantMaterialDaoImpl.class);
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private List<TblPlantMaster> listTblPlantMaster;
    private final PlantDaoImpl plantDao = DaoFactory.getDao(PlantDaoImpl.class);
    private List<TblMaterialMaster> listTblMaterialMaster;
    private final MaterialDaoImpl materialDao = DaoFactory.getDao(MaterialDaoImpl.class);

    private String message;
    private int compId;
    private int plantId;
    private int matId;
    private BigDecimal materialQty;
    private static int genId;

    public TblMapCompanyPlantMaterial getTblMapCompanyPlantMaterial() {
        return tblMapCompanyPlantMaterial;
    }

    public void setTblMapCompanyPlantMaterial(TblMapCompanyPlantMaterial tblMapCompanyPlantMaterial) {
        this.tblMapCompanyPlantMaterial = tblMapCompanyPlantMaterial;
    }

    public List<TblMapCompanyPlantMaterial> getListTblMapCompanyPlantMaterial() {
        return listTblMapCompanyPlantMaterial;
    }

    public void setListTblMapCompanyPlantMaterial(List<TblMapCompanyPlantMaterial> listTblMapCompanyPlantMaterial) {
        this.listTblMapCompanyPlantMaterial = listTblMapCompanyPlantMaterial;
    }

    public List<TblCompanyMaster> getListTblCompanyMaster() {
        return listTblCompanyMaster;
    }

    public void setListTblCompanyMaster(List<TblCompanyMaster> listTblCompanyMaster) {
        this.listTblCompanyMaster = listTblCompanyMaster;
    }

    public List<TblPlantMaster> getListTblPlantMaster() {
        return listTblPlantMaster;
    }

    public void setListTblPlantMaster(List<TblPlantMaster> listTblPlantMaster) {
        this.listTblPlantMaster = listTblPlantMaster;
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

    public int getPlantId() {
        return plantId;
    }

    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public int getMatId() {
        return matId;
    }

    public void setMatId(int matId) {
        this.matId = matId;
    }

    public BigDecimal getMaterialQty() {
        return materialQty;
    }

    public void setMaterialQty(BigDecimal materialQty) {
        this.materialQty = materialQty;
    }

    public static int getGenId() {
        return genId;
    }

    public static void setGenId(int genId) {
        CompPlantMaterialAction.genId = genId;
    }

    public CompPlantMaterialAction() throws Exception {
        tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
        listTblMapCompanyPlantMaterial = new ArrayList<TblMapCompanyPlantMaterial>();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
        listTblPlantMaster = new ArrayList<TblPlantMaster>();
        listTblMaterialMaster = new ArrayList<TblMaterialMaster>();
    }

    public String getList() {
        listTblMapCompanyPlantMaterial = (ArrayList<TblMapCompanyPlantMaterial>) compPlantMaterialDao.getList("where mapStatus in (0,1)");
        if (!listTblMapCompanyPlantMaterial.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void getCompPlantMaterialList() {
        listTblCompanyMaster = companyDao.getList("where compStatus=1");
        listTblPlantMaster = plantDao.getList("where plantStatus=1");
        listTblMaterialMaster = materialDao.getList("where materialStatus=1");
    }

    public String addCompPlantMaterial() {
        genId = 0;
        getCompPlantMaterialList();
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (compId == 0) {
            addFieldError("compId", "Please Select Company.");
            clear = false;
        }
        if (plantId == 0) {
            addFieldError("plantId", "Please Select Plant.");
            clear = false;
        }
        if (matId == 0) {
            addFieldError("matId", "Please Select Material.");
            clear = false;
        }
        return clear;
    }

    public String saveCompPlantMaterial() throws ParseException {
        if (validation()) {
            //listTblMapCompanyPlantMaterial = (ArrayList<TblMapCompanyPlantMaterial>) compPlantMaterialDao.getList("where mapStatus in (0,1) and tblCompanyMaster.compId=" + compId + " and tblPlantMaster.plantId =" + plantId + " and tblMaterialMaster.materialId=" + matId + " and mapQuantityStores=" + materialQty + "");
            listTblMapCompanyPlantMaterial = (ArrayList<TblMapCompanyPlantMaterial>) compPlantMaterialDao.getList("where mapStatus in (0,1) and tblCompanyMaster.compId=" + compId + " and tblPlantMaster.plantId =" + plantId + " and tblMaterialMaster.materialId=" + matId + "");
            if (genId != 0) {
                tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getById(genId);
            } else {
                if (listTblMapCompanyPlantMaterial.isEmpty()) {
                    tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
                } else {
                    addFieldError("compId", "Duplicate Company or Plant or Material.");
                    getList();
                    getCompPlantMaterialList();
                    return INPUT;
                }
            }
            TblEmpMaster empMaster = new TblEmpMaster();
            empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
            TblCompanyMaster compMaster = new TblCompanyMaster();
            compMaster.setCompId(compId);
            tblMapCompanyPlantMaterial.setTblCompanyMaster(compMaster);
            TblPlantMaster plantMaster = new TblPlantMaster();
            plantMaster.setPlantId(plantId);
            tblMapCompanyPlantMaterial.setTblPlantMaster(plantMaster);
            TblMaterialMaster materialMaster = new TblMaterialMaster();
            materialMaster.setMaterialId(matId);
            tblMapCompanyPlantMaterial.setTblMaterialMaster(materialMaster);
            if (!materialQty.toString().isEmpty() && materialQty.toString().length() != 0) {
                tblMapCompanyPlantMaterial.setMapQuantityStores(materialQty);
            } else {
                tblMapCompanyPlantMaterial.setMapQuantityStores(new BigDecimal(BigInteger.ZERO));
            }
            tblMapCompanyPlantMaterial.setMapStatus(1);
            tblMapCompanyPlantMaterial.setTblEmpMaster(empMaster);
            tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
            boolean result = compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
            if (result) {
                setTblMapCompanyPlantMaterial(new TblMapCompanyPlantMaterial());
                setCompId(0);
                setPlantId(0);
                setMatId(0);
                setMessage("Company Plant Material Map Inserted Successfully");
                return SUCCESS;
            } else {
                getList();
                getCompPlantMaterialList();
                return INPUT;
            }
        } else {
            getList();
            getCompPlantMaterialList();
            return INPUT;
        }
    }

    public String viewCompPlantMaterial() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("compPlantMaterialId");
        getCompPlantMaterialList();
        try {
            if (editId.length == 1) {
                this.tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getById(Integer.parseInt(editId[0]));
                setCompId(tblMapCompanyPlantMaterial.getTblCompanyMaster().getCompId());
                setPlantId(tblMapCompanyPlantMaterial.getTblPlantMaster().getPlantId());
                setMatId(tblMapCompanyPlantMaterial.getTblMaterialMaster().getMaterialId());
                setMaterialQty(tblMapCompanyPlantMaterial.getMapQuantityStores());
                setGenId(tblMapCompanyPlantMaterial.getMapId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateCompPlantMaterial() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("compPlantMaterialId");
        getCompPlantMaterialList();
        try {
            if (editId.length == 1) {
                this.tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getById(Integer.parseInt(editId[0]));
                setCompId(tblMapCompanyPlantMaterial.getTblCompanyMaster().getCompId());
                setPlantId(tblMapCompanyPlantMaterial.getTblPlantMaster().getPlantId());
                setMatId(tblMapCompanyPlantMaterial.getTblMaterialMaster().getMaterialId());
                setMaterialQty(tblMapCompanyPlantMaterial.getMapQuantityStores());
                setGenId(tblMapCompanyPlantMaterial.getMapId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteCompPlantMaterial() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("compPlantMaterialId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getById(Integer.parseInt(a));
                this.tblMapCompanyPlantMaterial.setMapStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblMapCompanyPlantMaterial.setTblEmpMaster(empMaster);
                this.tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn2()));
                compPlantMaterialDao.save(this.tblMapCompanyPlantMaterial);
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
    public TblMapCompanyPlantMaterial getModel() {
        return this.tblMapCompanyPlantMaterial;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
