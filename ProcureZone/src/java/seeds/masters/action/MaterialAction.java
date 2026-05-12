/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.action;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import pojo.TblEmpMaster;
import pojo.TblMaterialMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.report.MaterialMasterImport;
import seeds.masters.daoImpl.MaterialDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class MaterialAction extends ActionSupport implements ModelDriven<TblMaterialMaster>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblMaterialMaster tblMaterialMaster;
    private List<TblMaterialMaster> listTblMaterialMaster;
    private final MaterialDaoImpl materialDao = DaoFactory.getDao(MaterialDaoImpl.class);

    private String message;
    private String matCode;
    private String matName;
    private String matDesc;
    private static int materialId;
    private File fileUpload;
    private String fileUploadFileName;
    private String fileUploadContentType;

    public TblMaterialMaster getTblMaterialMaster() {
        return tblMaterialMaster;
    }

    public void setTblMaterialMaster(TblMaterialMaster tblMaterialMaster) {
        this.tblMaterialMaster = tblMaterialMaster;
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

    public String getMatCode() {
        return matCode;
    }

    public void setMatCode(String matCode) {
        this.matCode = matCode;
    }

    public String getMatName() {
        return matName;
    }

    public void setMatName(String matName) {
        this.matName = matName;
    }

    public String getMatDesc() {
        return matDesc;
    }

    public void setMatDesc(String matDesc) {
        this.matDesc = matDesc;
    }

    public static int getMaterialId() {
        return materialId;
    }

    public static void setMaterialId(int materialId) {
        MaterialAction.materialId = materialId;
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

    public MaterialAction() throws Exception {
        tblMaterialMaster = new TblMaterialMaster();
        listTblMaterialMaster = new ArrayList<TblMaterialMaster>();
    }

    public String getList() {
        listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus in (0,1)");
        if (!listTblMaterialMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String addMaterial() {
        materialId = 0;
        setMatCode("");
        setMatName("");
        setMatDesc("");
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (matCode.length() == 0 && matCode != null) {
            addFieldError("matCode", "Please Enter Material Code.");
            clear = false;
        }
        if (matName.length() == 0 && matName != null) {
            addFieldError("matName", "Please Enter Material Name.");
            clear = false;
        }
        if (matDesc.length() == 0 && matDesc != null) {
            addFieldError("matDesc", "Please Enter Material Description.");
            clear = false;
        }
        return clear;
    }

    public String saveMaterial() throws ParseException {
        if (validation()) {
            listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where (materialStatus in (0,1)  and materialCode='" + matCode + "') or ( materialStatus in (0,1) and materialName='" + matName + "')");
           // if (listTblMaterialMaster.isEmpty()) {
                if (materialId != 0) {
                    tblMaterialMaster = (TblMaterialMaster) materialDao.getById(materialId);
                } else {
                    if (listTblMaterialMaster.isEmpty()) {
                            tblMaterialMaster = new TblMaterialMaster();
                    } else {
                            addFieldError("matCode", "Duplicate Material Code or Material Name.");
                            return INPUT;
                    }
                }
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblMaterialMaster.setMaterialCode(matCode);
                tblMaterialMaster.setMaterialName(matName);
                tblMaterialMaster.setMaterialDesc(matDesc);
                tblMaterialMaster.setMaterialStatus(1);
                tblMaterialMaster.setTblEmpMaster(empMaster);
                tblMaterialMaster.setMaterialLmd(utils.getDateFormat(utils.DateIn()));
                boolean result = materialDao.save(tblMaterialMaster);
                if (result) {
                    setTblMaterialMaster(new TblMaterialMaster());
                    setMatCode("");
                    setMatName("");
                    setMatDesc("");
                    setMessage("Material Inserted Successfully");
                    return SUCCESS;
                } else {
                    return INPUT;
                }
           /* } else {
                addFieldError("matCode", "Duplicate Material Code or Material Name.");
                return INPUT;
            }*/
        } else {
            return INPUT;
        }
    }

    public String viewMaterial() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("materialId");
        try {
            if (editId.length == 1) {
                this.tblMaterialMaster = (TblMaterialMaster) materialDao.getById(Integer.parseInt(editId[0]));
                setMatCode(tblMaterialMaster.getMaterialCode());
                setMatName(tblMaterialMaster.getMaterialName());
                setMatDesc(tblMaterialMaster.getMaterialDesc());
                setMaterialId(tblMaterialMaster.getMaterialId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String updateMaterial() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("materialId");
        try {
            if (editId.length == 1) {
                this.tblMaterialMaster = (TblMaterialMaster) materialDao.getById(Integer.parseInt(editId[0]));
                setMatCode(tblMaterialMaster.getMaterialCode());
                setMatName(tblMaterialMaster.getMaterialName());
                setMatDesc(tblMaterialMaster.getMaterialDesc());
                setMaterialId(tblMaterialMaster.getMaterialId());
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch (NumberFormatException e) {
            return INPUT;
        }
    }

    public String deleteMaterial() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] deleteId = request.getParameterValues("materialId");
        if (deleteId != null) {
            for (String a : deleteId) {
                this.tblMaterialMaster = (TblMaterialMaster) materialDao.getById(Integer.parseInt(a));
                this.tblMaterialMaster.setMaterialStatus(0);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                this.tblMaterialMaster.setTblEmpMaster(empMaster);
                this.tblMaterialMaster.setMaterialLmd(utils.getDateFormat(utils.DateIn()));
                materialDao.save(this.tblMaterialMaster);
            }
        }
        getList();
        return SUCCESS;
    }

    public String importMaterial() {
        return SUCCESS;
    }

    public String importMaterialMaster() throws IOException, Exception {
        /*HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get("com.opensymphony.xwork2.dispatcher.HttpServletRequest");
         String filePath = request.getRealPath("./images/emp");
         String filePath1 = "./images/emp";
         String strDirectoy = filePath;
         new File(strDirectoy).mkdirs();*/
        if (fileUpload != null) {
            File fileToCreate = new File(this.fileUploadFileName);
            FileUtils.copyFile(this.fileUpload, fileToCreate);
            //filePath1 = filePath1 + "/" + this.fileUploadFileName;
            MaterialMasterImport mmi = new MaterialMasterImport();
           message= mmi.InsertMaterialMaster(Integer.parseInt(this.session.get("empNumber").toString()), fileUpload.getAbsolutePath());
            //setMessage("Import Success");
            return SUCCESS;
        } else {
            getList();
            setMessage("Import Failed");
            return INPUT;
        }
    }

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    public TblMaterialMaster getModel() {
        return this.tblMaterialMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
