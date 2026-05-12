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
import pojo.TblPlantMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.PlantDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class PlantAction extends ActionSupport implements ModelDriven<TblPlantMaster>, SessionAware{
    
    private Map session;
    private final Utils utils = new Utils();
    private TblPlantMaster tblPlantMaster;
    private List<TblPlantMaster> listTblPlantMaster;
    private final PlantDaoImpl plantDao = DaoFactory.getDao(PlantDaoImpl.class);

    private String message;
    private String pCode;
    private String pName;
    private static int plantId;

    public TblPlantMaster getTblPlantMaster() {
        return tblPlantMaster;
    }

    public void setTblPlantMaster(TblPlantMaster tblPlantMaster) {
        this.tblPlantMaster = tblPlantMaster;
    }

    public List<TblPlantMaster> getListTblPlantMaster() {
        return listTblPlantMaster;
    }

    public void setListTblPlantMaster(List<TblPlantMaster> listTblPlantMaster) {
        this.listTblPlantMaster = listTblPlantMaster;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getpCode() {
        return pCode;
    }

    public void setpCode(String pCode) {
        this.pCode = pCode;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public static int getPlantId() {
        return plantId;
    }

    public static void setPlantId(int plantId) {
        PlantAction.plantId = plantId;
    }
    
    public PlantAction() throws Exception{
        tblPlantMaster=new TblPlantMaster();
        listTblPlantMaster=new ArrayList<TblPlantMaster>();
    }
    
    public String getList() {
        listTblPlantMaster = (ArrayList<TblPlantMaster>) plantDao.getList("where plantStatus in (0,1)");
        if (!listTblPlantMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String addPlant() {
        plantId = 0;
        setpCode("");
        setpName("");
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (pCode.length() == 0 && pCode != null) {
            addFieldError("pCode", "Please Enter Plant Code.");
            clear = false;
        }
        if (pName.length() == 0 && pName != null) {
            addFieldError("pName", "Please Enter Plant Name.");
            clear = false;
        }
        return clear;
    }

    public String savePlant() throws ParseException {
        if (validation()) {
            listTblPlantMaster = (ArrayList<TblPlantMaster>) plantDao.getList("where (plantStatus in (0,1) and plantCode='" + pCode + "') or (plantStatus in (0,1) and plantName='" + pName + "')");
                if (plantId != 0) {
                    tblPlantMaster = (TblPlantMaster) plantDao.getById(plantId);
                } else {
                    if (listTblPlantMaster.isEmpty()) {
                        tblPlantMaster = new TblPlantMaster();
                    } else {
                        addFieldError("pCode", "Duplicate Plant Code or Plant Name.");
                        return INPUT;
                    }
                }
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblPlantMaster.setPlantCode(pCode);
                tblPlantMaster.setPlantName(pName);
                tblPlantMaster.setPlantStatus(1);
                tblPlantMaster.setTblEmpMaster(empMaster);
                tblPlantMaster.setPlantLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = plantDao.save(tblPlantMaster);
                if (result) {
                    setTblPlantMaster(new TblPlantMaster());
                    setpCode("");
                    setpName("");
                    setMessage("Plant Inserted Successfully");
                    return SUCCESS;
                } else {
                    return INPUT;
                }
        } else {
            return INPUT;
        }
    }

    public String viewPlant() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("plantId");
        try {
            if (editId.length == 1) {
                this.tblPlantMaster = (TblPlantMaster) plantDao.getById(Integer.parseInt(editId[0]));
                setpCode(tblPlantMaster.getPlantCode());
                setpName(tblPlantMaster.getPlantName());
                setPlantId(tblPlantMaster.getPlantId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updatePlant() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("plantId");
        try {
            if (editId.length == 1) {
                this.tblPlantMaster = (TblPlantMaster) plantDao.getById(Integer.parseInt(editId[0]));
                setpCode(tblPlantMaster.getPlantCode());
                setpName(tblPlantMaster.getPlantName());
                setPlantId(tblPlantMaster.getPlantId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deletePlant() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("plantId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblPlantMaster = (TblPlantMaster) plantDao.getById(Integer.parseInt(a));
                this.tblPlantMaster.setPlantStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblPlantMaster.setTblEmpMaster(empMaster);
                this.tblPlantMaster.setPlantLmd(utils.getDateFormat(utils.DateIn()));
                plantDao.save(this.tblPlantMaster);
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
    public TblPlantMaster getModel() {
        return this.tblPlantMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session=map;
    }
    
}
