/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.indent.action;


import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import plant.sap.cronScheduler.SapSchedulerListenerPlant;
import pojo.TbPzlIndentMaster;
import pojo.TbPzlIndentMastera;
import pojo.TblCompanyMaster;
import pojo.TblCropMaster;
import pojo.TblDepartmentMaster;
import pojo.TblEmpMaster;
import pojo.TblIndentDetails;
import pojo.TblIndentMaster;
import pojo.TblIndentProcurementLogs;
import pojo.TblIndentStatus;
import pojo.TblMapCompanyPlantMaterial;
import pojo.TblMaterialMaster;
import pojo.TblPlantLineCode;
import pojo.TblPlantMaster;
import pojo.TblPlantOrderType;
import pojo.TblPzCompanyMaster;
import pojo.TblPzCropsType;
import pojo.TblPzIndentDetails;
import pojo.TblPzMapCompanyPlantMaterial;
import pojo.TblPzMaterialMaster;
import pojo.TblPzPlantStoLoction;
import pojo.TblPzProcPack;
import pojo.TblPzScheduleMaterialmaster;
import pojo.TblPzTblEmpPlantMap;
import pojo.TblPzUmoMaster;
import pojo.TblRolesMaster;
import pojo.TblSectionMaster;
import pojo.TblUmoMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.indent.action.IndentAction;
import seeds.indent.daoImpl.IndentDaoImpl;
import seeds.indent.daoImpl.IndentDetailsDaoImpl;
import seeds.indent.daoImpl.IndentProcurementDaoImpl;
import seeds.indent.daoImpl.IndentStatusDaoImpl;
import seeds.indent.mailService.IndentNotification;
import seeds.indent.report.IndentDetailsReport;
import seeds.indent.report.IndentReportUser;
import seeds.masters.daoImpl.CompPlantMaterialDaoImpl;
import seeds.masters.daoImpl.CompanyDaoImpl;
import seeds.masters.daoImpl.DepartmentDaoImpl;
import seeds.masters.daoImpl.EmployeeDaoImpl;
import seeds.masters.daoImpl.MaterialDaoImpl;
import seeds.masters.daoImpl.PlantDaoImpl;
import seeds.masters.daoImpl.RolesDaoImpl;
import seeds.masters.daoImpl.SectionDaoImpl;
import seeds.masters.daoImpl.UmoDaoImpl;



/**
 *
 * @author ramesh.a
 */
public class PlantIndentAction extends ActionSupport implements ModelDriven<TbPzlIndentMaster>, SessionAware {
    
    private Map session;
    
    private SapSchedulerListenerPlant sln;
    private final Utils utils = new Utils();
    private TbPzlIndentMastera tblIndentMaster;
    private TblPzTblEmpPlantMap tblEmpPlantMap;
    private List<TblPzTblEmpPlantMap> listtbEmpPlantMaps;
    private final plantEmpMapImpl plantEmpMapImplDao = DaoFactory.getDao(plantEmpMapImpl.class);
    private TbPzlIndentMaster tblpzIndentMaster;
    private TblIndentMaster indentMaster;
    private List<TblIndentMaster> listTblIndentMaster;
    private List<TbPzlIndentMastera> listTblpzIndentMaster;
    private List<TbPzlIndentMastera> listTblpzIndentMasterQcrejected;
    private List<TbPzlIndentMastera> listTblpzIndentMastera;
    private final IndentDaoImpl indentDao = DaoFactory.getDao(IndentDaoImpl.class);
    private final IndentPzDaoImpl indentpzDao = DaoFactory.getDao(IndentPzDaoImpl.class);
    private final IndentOrderTypeDaoImpl indentpzorderTypeDao = DaoFactory.getDao(IndentOrderTypeDaoImpl.class);
    private TblIndentDetails tblIndentDetails;
    private TblPzIndentDetails tblpzIndentDetails;
    private List<TblIndentDetails> listTblIndentDetails;
    private List<TblPzIndentDetails> listTblPzIndentDetails;
    private List<TblPzPlantStoLoction> listTblPzStoLoc;
    private final IndentDetailsDaoImpl indentDetailsDao = DaoFactory.getDao(IndentDetailsDaoImpl.class);
    private final IndentPzDetailsDaoImpl indentPzDetailsDao = DaoFactory.getDao(IndentPzDetailsDaoImpl.class);
    private final PzStoLocImpl instoLocDao = DaoFactory.getDao(PzStoLocImpl.class);
    private List<TblEmpMaster> listTblEmpMaster;
    private List<TblEmpMaster> listTblEmpMaster1;
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);
    private List<TblDepartmentMaster> listTblDepartmentMaster;
    private final DepartmentDaoImpl departmentDao = DaoFactory.getDao(DepartmentDaoImpl.class);
    private List<TblSectionMaster> listTblSectionMaster;
    private final SectionDaoImpl sectionDao = DaoFactory.getDao(SectionDaoImpl.class);
    private List<TblIndentStatus> listTblIndentStatus;
    private List<TblIndentStatus> listTblIndentStatus1;
    private List<TblIndentStatus> listTblIndentStatus2;
    private List<TblIndentStatus> listTblIndentStatus3;
    private final IndentStatusDaoImpl indentStatusDao = DaoFactory.getDao(IndentStatusDaoImpl.class);
    private final IndentNotification in = new IndentNotification();
    private List<TblPlantMaster> listTblPlantMaster;
    private List<TblPlantOrderType> listTblplantOrderType;
    private final PlantDaoImpl plantDao = DaoFactory.getDao(PlantDaoImpl.class);
    private List<TblMaterialMaster> listTblMaterialMaster;
    private List<TblPzMaterialMaster> listPzTblMaterialMaster;
    private List<TblPzMaterialMaster> listPzTblMaterialMasterdesc;
    private List<TblPzScheduleMaterialmaster> listPzTblPzScheduleMaterialmaster;
    private TblPzScheduleMaterialmaster tblschedulematmaster;
    private List<TblPzScheduleMaterialmaster> listPzTblPzScheduleMaterialmasterBatch;
    private final PzSchedulematerialMasterImpl pzschedulematerialDao = DaoFactory.getDao(PzSchedulematerialMasterImpl.class);
    private final ScheduleMaterialDaoImpl schedulematerialDao = DaoFactory.getDao(ScheduleMaterialDaoImpl.class);
    private final MaterialDaoImpl materialDao = DaoFactory.getDao(MaterialDaoImpl.class);
    private final PzMaterialDaoImpl pzmaterialDao = DaoFactory.getDao(PzMaterialDaoImpl.class);
    private List<TblUmoMaster> listTblUmoMaster;
    private List<TblPzUmoMaster> listTblPzUmoMaster;
    private final UmoDaoImpl umoDao = DaoFactory.getDao(UmoDaoImpl.class);
    private List<TblPzProcPack> ListTblProcPack;    
    private final PzProcPackDaoImpl pzprocpackDao = DaoFactory.getDao(PzProcPackDaoImpl.class);
    private final PzUmoDaoImpl pzumoDao = DaoFactory.getDao(PzUmoDaoImpl.class);
    private TblIndentProcurementLogs tblIndentProcurementLogs;
    private List<TblIndentProcurementLogs> listTblIndentProcurementLogs;
    private final IndentProcurementDaoImpl indentProcurementDao = DaoFactory.getDao(IndentProcurementDaoImpl.class);
    private List<TblCompanyMaster> listTblCompanyMaster;
    private List<TblPzCompanyMaster> listTblPzCompanyMaster;
    private List<TblRolesMaster> listTblRolesMaster;
    private final RolesDaoImpl rolesDao = DaoFactory.getDao(RolesDaoImpl.class);
    private final PzCompanyDaoImpl pzcompanyDao = DaoFactory.getDao(PzCompanyDaoImpl.class);
    private List<TblPzCropsType> listTblPzCropType;
    private final PzCropTypeDaoImpl pzcroptypeDao = DaoFactory.getDao(PzCropTypeDaoImpl.class);
    private List<TblCropMaster> listTblCropMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private TblCropMaster tblCropMaster;
    private final CropDaoImpl cropDao = DaoFactory.getDao(CropDaoImpl.class);
    private HttpServletResponse response;
    private TblMapCompanyPlantMaterial tblMapCompanyPlantMaterial;
    private TblPzMapCompanyPlantMaterial tblPzMapCompanyPlantMaterial;
    private final CompPzPlantMaterialDaoImpl compPzPlantMaterialDao = DaoFactory.getDao(CompPzPlantMaterialDaoImpl.class);
    private TblPzScheduleMaterialmaster tblPzScheduleMaterialmaster;
    private TbPzlIndentMastera tblPzIndentmastera;
    private List<TblPlantOrderType> listTblplantOrderTypea;
    private List<TblPlantLineCode> ListTblPzLineCode; 
    private TblPlantLineCode tblplantLinecode;
    private final PzTblLineCodeDaoImpl pzLineCodeDao = DaoFactory.getDao(PzTblLineCodeDaoImpl.class);
    private List<TblMapCompanyPlantMaterial> listTblMapCompanyPlantMaterial;
    private final CompPlantMaterialDaoImpl compPlantMaterialDao = DaoFactory.getDao(CompPlantMaterialDaoImpl.class);
    private final companyOutputMaterialMaster cOutputMaterialMaster = DaoFactory.getDao(companyOutputMaterialMaster.class);
    private String isndNo;
    private String myoutputmata;
    private String batchnumber;
    private String matumoa;
    private String crospId;
    private String colId;
    private String lineDesc;
    private String expoutput;
    private String message;
    private String finYear;
    private String indNo;
    private String empId;
    private String date;
    private int compId;
    private int deptId;
    private int secId;
    private int plantId;
    private String comments;
    private String Rejectcomments;
    private int[] md;
    private int[] umo;
    private String[] lot;
    private BigDecimal[] qty;
    private BigDecimal[] rmQty;
    private BigDecimal[] deptQty;
    private BigDecimal[] stock;
    private String[] pur;
    private String[] des;
    private String[] ven;
    private String[] stgLoc;
    private BigDecimal[] pricing;
    private int statusId;
    private static int iId;
    private String remarks;
    private int indentStatusId;
    private String finalRemarks;
    private String procurementRemarks;
    private String deliveryDate;
    private String poNumber;
    private int[] detailsCount;
    private String file;
    private InputStream inputStream;
    private int empNumber;
    private int empNumber1;
    private int materialId;
    private int umoId;
    private String fromDate;
    private String toDate;
    private int matId;
    private String comp;
    private String plant;
    private static BigDecimal mId;
    public String result;
    public String PlantName;
    private String linecode;
    private String lincodabc;
    private String linedesca;
    private BigDecimal mSd;
    private String mSd1;
    private String mSd2;
    private int mSp2;
    private String mSd3;
    private String mSd4;
    private String mSd5;
    private String mSd6;
    
    private String mSd10;
    private String mSd11;
    private String mSd12;
    
    private String mSd13;
    
    private String mSd14;
    private String mSd15;
    
    
    private String mSd16;
    private String mSd17;
    private String mSd18;
    private String mSd19;
    private String mSd20;
    private String mSd21;
    private String mSd22;
    private String mSd23;
    private String mSd24;
    private String mSd25;
    private String mSd26;
    private String mSd27;
    private String mSd28;
    private String mSd29;
    private String mSd30;
    
    private String mSd31;
    private String mSd32;
    private String mSd33;
    
    private String mSd34;
    private String mSd35;
    private String mSd36;
    private String mSd37;
    private String mSd38;
    private String mSd39;
    private String mSd40;
    
    
    private String stl[];
    private String odv[];
    private String got[];
    private String elisa[];
    
    private String sdcls[];
    private String skipd[];
    
    
     private String iNSPDT[];
     private String mOISTURE[];
     private String PURESEED[];
     
     private String INERTMATTER[];
     private String OCSCOUNT[];
     private String WEEDSEEDCOUNT[];
     private String gRAIN[];
     private String BLACKSEEDS[];
     private String PINHOLESEEDS[];
     
     private String ODVRES[];
     private String BULKDENSITY[];
     private String tHSW[];
     private String COLDVIGOURGERMNORMAL[];
     private String FIRSTCOUNTNORMAL[];
     private String GERMNORMAL[];
     
     private String FETNORMAL[];
     private String SOILCOUNTDAYS[];
     private String AAVGERMNORMAL[];
     private String GOTGP[];
     private String GOTFEMALE[];
     private String GOTOTHERS[];
     
     private String bG1[];
     private String bG2[];
     private String hT[];
     private String fQR[];
     private String q1[];
     private String q2[];
     private String q3[];
     private String q4[];
     private String q5[];
     private String q6[];
     private String q7[];
     private String q8[];
     private String q9[];
    
    
    private List<TblPzScheduleMaterialmaster> mSd7;
    private String mSd8;
    private String grnreceiptnumber;
    private String stoLocationa;
    private String colCode;
    private String matCodeout;
    private int plantCodeout;
    private String matDescout;
    private String matTypeout;
    private String matGroupout;
    private String matUomout;
    private int matpacktypeout;
    private TblDepartmentMaster mSp4;    
    private BigDecimal mSda;    
    private int indentId;    
    private int indentsId;    
    private String batchNumberIndent;    
    private String packandprocessing;    
    private String indentFmComments;    
    private String indentDeoComments;    
    private String indentFlInchargeCommants;
    private String indentOrderNumbera;    
    private BigDecimal atualOutputQty;    
    private String DeptName;    
    private String CompName;
    private String IndentCrop;
    private int cropId;    
    private String indentFlinchargeComments;
    private int flcompanyid;

    public String getMatCodeout() {
        return matCodeout;
    }

    public void setMatCodeout(String matCodeout) {
        this.matCodeout = matCodeout;
    }

    public int getPlantCodeout() {
        return plantCodeout;
    }

    public void setPlantCodeout(int plantCodeout) {
        this.plantCodeout = plantCodeout;
    }

    public String getMatDescout() {
        return matDescout;
    }

    public void setMatDescout(String matDescout) {
        this.matDescout = matDescout;
    }

    public String getMatTypeout() {
        return matTypeout;
    }

    public void setMatTypeout(String matTypeout) {
        this.matTypeout = matTypeout;
    }

    public String getMatGroupout() {
        return matGroupout;
    }

    public void setMatGroupout(String matGroupout) {
        this.matGroupout = matGroupout;
    }

    public String getMatUomout() {
        return matUomout;
    }

    public void setMatUomout(String matUomout) {
        this.matUomout = matUomout;
    }

    public int getMatpacktypeout() {
        return matpacktypeout;
    }

    public void setMatpacktypeout(int matpacktypeout) {
        this.matpacktypeout = matpacktypeout;
    }

    public static Log getLOG() {
        return LOG;
    }

    public static void setLOG(Log LOG) {
        ActionSupport.LOG = LOG;
    }
    
    public String getColCode() {
        return colCode;
    }

    public void setColCode(String colCode) {
        this.colCode = colCode;
    }

    public String getStoLocationa() {
        return stoLocationa;
    }

    public void setStoLocationa(String stoLocationa) {
        this.stoLocationa = stoLocationa;
    }
    
    public String[] getStgLoc() {
        return stgLoc;
    }

    public void setStgLoc(String[] stgLoc) {
        this.stgLoc = stgLoc;
    }

    public String getmSd8() {
        return mSd8;
    }

    public void setmSd8(String mSd8) {
        this.mSd8 = mSd8;
    }

    public TblPzMapCompanyPlantMaterial getTblPzMapCompanyPlantMaterial() {
        return tblPzMapCompanyPlantMaterial;
    }

    public void setTblPzMapCompanyPlantMaterial(TblPzMapCompanyPlantMaterial tblPzMapCompanyPlantMaterial) {
        this.tblPzMapCompanyPlantMaterial = tblPzMapCompanyPlantMaterial;
    }
    
    public List<TblPzScheduleMaterialmaster> getmSd7() {
        return mSd7;
    }

    public void setmSd7(List<TblPzScheduleMaterialmaster> mSd7) {
        this.mSd7 = mSd7;
    }
    private String indentGrnReceiptComments;

    public String getIndentGrnReceiptComments() {
        return indentGrnReceiptComments;
    }

    public void setIndentGrnReceiptComments(String indentGrnReceiptComments) {
        this.indentGrnReceiptComments = indentGrnReceiptComments;
    }
    
    public String getmSd6() {
        return mSd6;
    }

    public void setmSd6(String mSd6) {
        this.mSd6 = mSd6;
    }
    
    public String getmSd5() {
        return mSd5;
    }

    public void setmSd5(String mSd5) {
        this.mSd5 = mSd5;
    }

    public String getGrnreceiptnumber() {
        return grnreceiptnumber;
    }

    public void setGrnreceiptnumber(String grnreceiptnumber) {
        this.grnreceiptnumber = grnreceiptnumber;
    }

    public String getIndentFlInchargeCommants() {
        return indentFlInchargeCommants;
    }

    public void setIndentFlInchargeCommants(String indentFlInchargeCommants) {
        this.indentFlInchargeCommants = indentFlInchargeCommants;
    }

    public String getIndentFlinchargeComments() {
        return indentFlinchargeComments;
    }

    public void setIndentFlinchargeComments(String indentFlinchargeComments) {
        this.indentFlinchargeComments = indentFlinchargeComments;
    }
    
    private String[] hidmat;

    public String[] getHidmat() {
        return hidmat;
    }

    public void setHidmat(String[] hidmat) {
        this.hidmat = hidmat;
    }
    

    public int getCropId() {
        return cropId;
    }

    public void setCropId(int cropId) {
        this.cropId = cropId;
    }
    
    public String getIndentCrop() {
        return IndentCrop;
    }

    public void setIndentCrop(String IndentCrop) {
        this.IndentCrop = IndentCrop;
    }

    public String getPlantName() {
        return PlantName;
    }

    public void setPlantName(String PlantName) {
        this.PlantName = PlantName;
    }
    
    public String getCompName() {
        return CompName;
    }

    public void setCompName(String CompName) {
        this.CompName = CompName;
    }
    
    public String getDeptName() {
        return DeptName;
    }

    public void setDeptName(String DeptName) {
        this.DeptName = DeptName;
    }
    
    public BigDecimal getAtualOutputQty() {
        return atualOutputQty;
    }

    public void setAtualOutputQty(BigDecimal atualOutputQty) {
        this.atualOutputQty = atualOutputQty;
    }
    

    public String getIndentDeoComments() {
        return indentDeoComments;
    }

    public void setIndentDeoComments(String indentDeoComments) {
        this.indentDeoComments = indentDeoComments;
    }
    
    public String getIndentOrderNumbera() {
        return indentOrderNumbera;
    }

    public void setIndentOrderNumbera(String indentOrderNumbera) {
        this.indentOrderNumbera = indentOrderNumbera;
    }
    
    public String getIndentFmComments() {
        return indentFmComments;
    }

    public void setIndentFmComments(String indentFmComments) {
        this.indentFmComments = indentFmComments;
    }
    
    public String getPackandprocessing() {
        return packandprocessing;
    }

    public void setPackandprocessing(String packandprocessing) {
        this.packandprocessing = packandprocessing;
    }

    public String[] getDes() {
        return des;
    }

    public void setDes(String[] des) {
        this.des = des;
    }

    public String getBatchNumberIndent() {
        return batchNumberIndent;
    }

    public void setBatchNumberIndent(String batchNumberIndent) {
        this.batchNumberIndent = batchNumberIndent;
    }
    
    public String getLinedesca() {
        return linedesca;
    }

    public void setLinedesca(String linedesca) {
        this.linedesca = linedesca;
    }

    public String getLincodabc() {
        return lincodabc;
    }

    public void setLincodabc(String lincodabc) {
        this.lincodabc = lincodabc;
    }

    public TblDepartmentMaster getmSp4() {
        return mSp4;
    }

    public void setmSp4(TblDepartmentMaster mSp4) {
        this.mSp4 = mSp4;
    }

    public int getmSp2() {
        return mSp2;
    }

    public void setmSp2(int mSp2) {
        this.mSp2 = mSp2;
    }

    public BigDecimal getmSda() {
        return mSda;
    }

    public void setmSda(BigDecimal mSda) {
        this.mSda = mSda;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMastera() {
        return listTblpzIndentMastera;
    }

    public void setListTblpzIndentMastera(List<TbPzlIndentMastera> listTblpzIndentMastera) {
        this.listTblpzIndentMastera = listTblpzIndentMastera;
    }

    public List<TblPlantOrderType> getListTblplantOrderType() {
        return listTblplantOrderType;
    }

    public void setListTblplantOrderType(List<TblPlantOrderType> listTblplantOrderType) {
        this.listTblplantOrderType = listTblplantOrderType;
    }

    public TbPzlIndentMastera getTblPzIndentmastera() {
        return tblPzIndentmastera;
    }

    public void setTblPzIndentmastera(TbPzlIndentMastera tblPzIndentmastera) {
        this.tblPzIndentmastera = tblPzIndentmastera;
    }

    public int getIndentId() {
        return indentId;
    }

    public void setIndentId(int indentId) {
        this.indentId = indentId;
    }

    public int getIndentsId() {
        return indentsId;
    }

    public void setIndentsId(int indentsId) {
        this.indentsId = indentsId;
    }

    public String[] getLot() {
        return lot;
    }

    public void setLot(String[] lot) {
        this.lot = lot;
    }
   

    public int getFlcompanyid() {
        return flcompanyid;
    }

    public void setFlcompanyid(int flcompanyid) {
        this.flcompanyid = flcompanyid;
    }

    public List<TblPzIndentDetails> getListTblPzIndentDetails() {
        return listTblPzIndentDetails;
    }

    public void setListTblPzIndentDetails(List<TblPzIndentDetails> listTblPzIndentDetails) {
        this.listTblPzIndentDetails = listTblPzIndentDetails;
    }

    
    
    
    

    public String getmSd4() {
        return mSd4;
    }

    public void setmSd4(String mSd4) {
        this.mSd4 = mSd4;
    }
    
    

    public String getmSd3() {
        return mSd3;
    }

    public void setmSd3(String mSd3) {
        this.mSd3 = mSd3;
    }
    
    

    public String getmSd2() {
        return mSd2;
    }

    public void setmSd2(String mSd2) {
        this.mSd2 = mSd2;
    }
    
    

    public String getmSd1() {
        return mSd1;
    }

    public void setmSd1(String mSd1) {
        this.mSd1 = mSd1;
    }

    public BigDecimal getmSd() {
        return mSd;
    }

    public void setmSd(BigDecimal mSd) {
        this.mSd = mSd;
    }

    
    
    

    

    public String getLinecode() {
        return linecode;
    }

    public void setLinecode(String linecode) {
        this.linecode = linecode;
    }
    
    
    
    
    
    
    
    
    
    //private int materialId;
    
    private String materialaCode;
    
    private int compCode;
    private int compCodeMap;
    
    
    private int plantCodeMap;
    
    private int empcodemap;
    
    private int plantrolemap;
    
    
    
    
    private int plantGroup;
    
    private int divisionId;

    private String matCd;
    
    
    private String matBatch;
    
    private int expoutPut;
    private String orderNoid;
    private String ordedescid;
    
    private String myoutputmatab;

    public String getOrderNoid() {
        return orderNoid;
    }

    public void setOrderNoid(String orderNoid) {
        this.orderNoid = orderNoid;
    }

    public String getOrdedescid() {
        return ordedescid;
    }

    public void setOrdedescid(String ordedescid) {
        this.ordedescid = ordedescid;
    }
    
    

    public String getMyoutputmatab() {
        return myoutputmatab;
    }

    public void setMyoutputmatab(String myoutputmatab) {
        this.myoutputmatab = myoutputmatab;
    }
    
    

    public int getExpoutPut() {
        return expoutPut;
    }

    public void setExpoutPut(int expoutPut) {
        this.expoutPut = expoutPut;
    }
    
    

    public TbPzlIndentMastera getTblIndentMaster() {
        return tblIndentMaster;
    }

    public void setTblIndentMaster(TbPzlIndentMastera tblIndentMaster) {
        this.tblIndentMaster = tblIndentMaster;
    }

    

    public String getIsndNo() {
        return isndNo;
    }

    public void setIsndNo(String isndNo) {
        this.isndNo = isndNo;
    }

   

    

    public String getMyoutputmata() {
        return myoutputmata;
    }

    public void setMyoutputmata(String myoutputmata) {
        this.myoutputmata = myoutputmata;
    }

    public String getBatchnumber() {
        return batchnumber;
    }

    public void setBatchnumber(String batchnumber) {
        this.batchnumber = batchnumber;
    }

    public String getMatumoa() {
        return matumoa;
    }

    public void setMatumoa(String matumoa) {
        this.matumoa = matumoa;
    }

    public String getCrospId() {
        return crospId;
    }

    public void setCrospId(String crospId) {
        this.crospId = crospId;
    }

    

    

    public String getColId() {
        return colId;
    }

    public void setColId(String colId) {
        this.colId = colId;
    }

    public String getLineDesc() {
        return lineDesc;
    }

    public void setLineDesc(String lineDesc) {
        this.lineDesc = lineDesc;
    }

    public String getExpoutput() {
        return expoutput;
    }

    public void setExpoutput(String expoutput) {
        this.expoutput = expoutput;
    }
    
    
    
    
    
    

    public String getMatBatch() {
        return matBatch;
    }

    public void setMatBatch(String matBatch) {
        this.matBatch = matBatch;
    }
    
    
    
    

    public String getMatCd() {
        return matCd;
    }

    public void setMatCd(String matCd) {
        this.matCd = matCd;
    }

    public String getMaterialaCode() {
        return materialaCode;
    }

    public void setMaterialaCode(String materialaCode) {
        this.materialaCode = materialaCode;
    }

    

    public int getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(int divisionId) {
        this.divisionId = divisionId;
    }
    

    public int getPlantGroup() {
        return plantGroup;
    }

    public void setPlantGroup(int plantGroup) {
        this.plantGroup = plantGroup;
    }

    public TbPzlIndentMaster getTblpzIndentMaster() {
        return tblpzIndentMaster;
    }

    public void setTblpzIndentMaster(TbPzlIndentMaster tblpzIndentMaster) {
        this.tblpzIndentMaster = tblpzIndentMaster;
    }
    
    
    
    

    

    

    public TblIndentMaster getIndentMaster() {
        return indentMaster;
    }

    public void setIndentMaster(TblIndentMaster indentMaster) {
        this.indentMaster = indentMaster;
    }

    public List<TblIndentMaster> getListTblIndentMaster() {
        return listTblIndentMaster;
    }

    public void setListTblIndentMaster(List<TblIndentMaster> listTblIndentMaster) {
        this.listTblIndentMaster = listTblIndentMaster;
    }   

    public TblIndentDetails getTblIndentDetails() {
        return tblIndentDetails;
    }

    public void setTblIndentDetails(TblIndentDetails tblIndentDetails) {
        this.tblIndentDetails = tblIndentDetails;
    }

    public List<TblIndentDetails> getListTblIndentDetails() {
        return listTblIndentDetails;
    }

    public void setListTblIndentDetails(List<TblIndentDetails> listTblIndentDetails) {
        this.listTblIndentDetails = listTblIndentDetails;
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

    public List<TblDepartmentMaster> getListTblDepartmentMaster() {
        return listTblDepartmentMaster;
    }

    public void setListTblDepartmentMaster(List<TblDepartmentMaster> listTblDepartmentMaster) {
        this.listTblDepartmentMaster = listTblDepartmentMaster;
    }

    public List<TblSectionMaster> getListTblSectionMaster() {
        return listTblSectionMaster;
    }

    public void setListTblSectionMaster(List<TblSectionMaster> listTblSectionMaster) {
        this.listTblSectionMaster = listTblSectionMaster;
    }

    public List<TblIndentStatus> getListTblIndentStatus() {
        return listTblIndentStatus;
    }

    public void setListTblIndentStatus(List<TblIndentStatus> listTblIndentStatus) {
        this.listTblIndentStatus = listTblIndentStatus;
    }

    public List<TblIndentStatus> getListTblIndentStatus1() {
        return listTblIndentStatus1;
    }

    public void setListTblIndentStatus1(List<TblIndentStatus> listTblIndentStatus1) {
        this.listTblIndentStatus1 = listTblIndentStatus1;
    }

    public List<TblIndentStatus> getListTblIndentStatus2() {
        return listTblIndentStatus2;
    }

    public void setListTblIndentStatus2(List<TblIndentStatus> listTblIndentStatus2) {
        this.listTblIndentStatus2 = listTblIndentStatus2;
    }

    public List<TblIndentStatus> getListTblIndentStatus3() {
        return listTblIndentStatus3;
    }

    public void setListTblIndentStatus3(List<TblIndentStatus> listTblIndentStatus3) {
        this.listTblIndentStatus3 = listTblIndentStatus3;
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

    public List<TblUmoMaster> getListTblUmoMaster() {
        return listTblUmoMaster;
    }

    public void setListTblUmoMaster(List<TblUmoMaster> listTblUmoMaster) {
        this.listTblUmoMaster = listTblUmoMaster;
    }

    public TblIndentProcurementLogs getTblIndentProcurementLogs() {
        return tblIndentProcurementLogs;
    }

    public void setTblIndentProcurementLogs(TblIndentProcurementLogs tblIndentProcurementLogs) {
        this.tblIndentProcurementLogs = tblIndentProcurementLogs;
    }

    public List<TblIndentProcurementLogs> getListTblIndentProcurementLogs() {
        return listTblIndentProcurementLogs;
    }

    public void setListTblIndentProcurementLogs(List<TblIndentProcurementLogs> listTblIndentProcurementLogs) {
        this.listTblIndentProcurementLogs = listTblIndentProcurementLogs;
    }

    public List<TblCompanyMaster> getListTblCompanyMaster() {
        return listTblCompanyMaster;
    }

    public void setListTblCompanyMaster(List<TblCompanyMaster> listTblCompanyMaster) {
        this.listTblCompanyMaster = listTblCompanyMaster;
    }

    public List<TblCropMaster> getListTblCropMaster() {
        return listTblCropMaster;
    }

    public void setListTblCropMaster(List<TblCropMaster> listTblCropMaster) {
        this.listTblCropMaster = listTblCropMaster;
    }

    public List<TblPzCompanyMaster> getListTblPzCompanyMaster() {
        return listTblPzCompanyMaster;
    }

    public void setListTblPzCompanyMaster(List<TblPzCompanyMaster> listTblPzCompanyMaster) {
        this.listTblPzCompanyMaster = listTblPzCompanyMaster;
    }

    public TblPzScheduleMaterialmaster getTblPzScheduleMaterialmaster() {
        return tblPzScheduleMaterialmaster;
    }

    public void setTblPzScheduleMaterialmaster(TblPzScheduleMaterialmaster tblPzScheduleMaterialmaster) {
        this.tblPzScheduleMaterialmaster = tblPzScheduleMaterialmaster;
    }
    
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFinYear() {
        return finYear;
    }

    public void setFinYear(String finYear) {
        this.finYear = finYear;
    }

    public String getIndNo() {
        return indNo;
    }

    public void setIndNo(String indNo) {
        this.indNo = indNo;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCompId() {
        return compId;
    }

    public void setCompId(int compId) {
        this.compId = compId;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public int getSecId() {
        return secId;
    }

    public void setSecId(int secId) {
        this.secId = secId;
    }

    public int getPlantId() {
        return plantId;
    }

    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int[] getMd() {
        return md;
    }

    public void setMd(int[] md) {
        this.md = md;
    }

   

   

    public int[] getUmo() {
        return umo;
    }

    public void setUmo(int[] umo) {
        this.umo = umo;
    }

    public String[] getPur() {
        return pur;
    }

    public void setPur(String[] pur) {
        this.pur = pur;
    }

    public String[] getVen() {
        return ven;
    }

    public void setVen(String[] ven) {
        this.ven = ven;
    }

    public BigDecimal[] getQty() {
        return qty;
    }

    public void setQty(BigDecimal[] qty) {
        this.qty = qty;
    }

    public BigDecimal[] getRmQty() {
        return rmQty;
    }

    public void setRmQty(BigDecimal[] rmQty) {
        this.rmQty = rmQty;
    }

    public BigDecimal[] getDeptQty() {
        return deptQty;
    }

    public void setDeptQty(BigDecimal[] deptQty) {
        this.deptQty = deptQty;
    }

    public BigDecimal[] getStock() {
        return stock;
    }

    public void setStock(BigDecimal[] stock) {
        this.stock = stock;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public static int getiId() {
        return iId;
    }

    public static void setiId(int iId) {
        IndentAction.iId = iId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getIndentStatusId() {
        return indentStatusId;
    }

    public void setIndentStatusId(int indentStatusId) {
        this.indentStatusId = indentStatusId;
    }

    public String getFinalRemarks() {
        return finalRemarks;
    }

    public void setFinalRemarks(String finalRemarks) {
        this.finalRemarks = finalRemarks;
    }

    public String getProcurementRemarks() {
        return procurementRemarks;
    }

    public void setProcurementRemarks(String procurementRemarks) {
        this.procurementRemarks = procurementRemarks;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public int[] getDetailsCount() {
        return detailsCount;
    }

    public void setDetailsCount(int[] detailsCount) {
        this.detailsCount = detailsCount;
    }

    public BigDecimal[] getPricing() {
        return pricing;
    }

    public void setPricing(BigDecimal[] pricing) {
        this.pricing = pricing;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
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

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public int getUmoId() {
        return umoId;
    }

    public void setUmoId(int umoId) {
        this.umoId = umoId;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public int getMatId() {
        return matId;
    }

    public void setMatId(int matId) {
        this.matId = matId;
    }
    
    

    public static BigDecimal getmId() {
        return mId;
    }

    public String getComp() {
        return comp;
    }

    public void setComp(String comp) {
        this.comp = comp;
    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    public static void setmId(BigDecimal mId) {
        IndentAction.mId = mId;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<TblPzMaterialMaster> getListPzTblMaterialMaster() {
        return listPzTblMaterialMaster;
    }

    public void setListPzTblMaterialMaster(List<TblPzMaterialMaster> listPzTblMaterialMaster) {
        this.listPzTblMaterialMaster = listPzTblMaterialMaster;
    }

    public List<TblPzUmoMaster> getListTblPzUmoMaster() {
        return listTblPzUmoMaster;
    }

    public void setListTblPzUmoMaster(List<TblPzUmoMaster> listTblPzUmoMaster) {
        this.listTblPzUmoMaster = listTblPzUmoMaster;
    }

    public List<TblPzMaterialMaster> getListPzTblMaterialMasterdesc() {
        return listPzTblMaterialMasterdesc;
    }

    public void setListPzTblMaterialMasterdesc(List<TblPzMaterialMaster> listPzTblMaterialMasterdesc) {
        this.listPzTblMaterialMasterdesc = listPzTblMaterialMasterdesc;
    }

    public List<TblPzScheduleMaterialmaster> getListPzTblPzScheduleMaterialmaster() {
        return listPzTblPzScheduleMaterialmaster;
    }

    public void setListPzTblPzScheduleMaterialmaster(List<TblPzScheduleMaterialmaster> listPzTblPzScheduleMaterialmaster) {
        this.listPzTblPzScheduleMaterialmaster = listPzTblPzScheduleMaterialmaster;
    }

    public int getCompCode() {
        return compCode;
    }

    public void setCompCode(int compCode) {
        this.compCode = compCode;
    }

    public List<TblPzCropsType> getListTblPzCropType() {
        return listTblPzCropType;
    }

    public void setListTblPzCropType(List<TblPzCropsType> listTblPzCropType) {
        this.listTblPzCropType = listTblPzCropType;
    }

    public List<TblPzProcPack> getListTblProcPack() {
        return ListTblProcPack;
    }

    public void setListTblProcPack(List<TblPzProcPack> ListTblProcPack) {
        this.ListTblProcPack = ListTblProcPack;
    }

    public List<TblPzScheduleMaterialmaster> getListPzTblPzScheduleMaterialmasterBatch() {
        return listPzTblPzScheduleMaterialmasterBatch;
    }

    public void setListPzTblPzScheduleMaterialmasterBatch(List<TblPzScheduleMaterialmaster> listPzTblPzScheduleMaterialmasterBatch) {
        this.listPzTblPzScheduleMaterialmasterBatch = listPzTblPzScheduleMaterialmasterBatch;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMaster() {
        return listTblpzIndentMaster;
    }

    public void setListTblpzIndentMaster(List<TbPzlIndentMastera> listTblpzIndentMaster) {
        this.listTblpzIndentMaster = listTblpzIndentMaster;
    }

    

    public TblPzIndentDetails getTblpzIndentDetails() {
        return tblpzIndentDetails;
    }

    public void setTblpzIndentDetails(TblPzIndentDetails tblpzIndentDetails) {
        this.tblpzIndentDetails = tblpzIndentDetails;
    }

    public List<TblPlantOrderType> getListTblplantOrderTypea() {
        return listTblplantOrderTypea;
    }

    public void setListTblplantOrderTypea(List<TblPlantOrderType> listTblplantOrderTypea) {
        this.listTblplantOrderTypea = listTblplantOrderTypea;
    }

    public List<TblPlantLineCode> getListTblPzLineCode() {
        return ListTblPzLineCode;
    }

    public void setListTblPzLineCode(List<TblPlantLineCode> ListTblPzLineCode) {
        this.ListTblPzLineCode = ListTblPzLineCode;
    }

    public TblPzScheduleMaterialmaster getTblschedulematmaster() {
        return tblschedulematmaster;
    }

    public void setTblschedulematmaster(TblPzScheduleMaterialmaster tblschedulematmaster) {
        this.tblschedulematmaster = tblschedulematmaster;
    }

    public String getRejectcomments() {
        return Rejectcomments;
    }

    public void setRejectcomments(String Rejectcomments) {
        this.Rejectcomments = Rejectcomments;
    }

    public List<TblPzPlantStoLoction> getListTblPzStoLoc() {
        return listTblPzStoLoc;
    }

    public void setListTblPzStoLoc(List<TblPzPlantStoLoction> listTblPzStoLoc) {
        this.listTblPzStoLoc = listTblPzStoLoc;
    }

    public List<TblRolesMaster> getListTblRolesMaster() {
        return listTblRolesMaster;
    }

    public void setListTblRolesMaster(List<TblRolesMaster> listTblRolesMaster) {
        this.listTblRolesMaster = listTblRolesMaster;
    }

    public TblPzTblEmpPlantMap getTblEmpPlantMap() {
        return tblEmpPlantMap;
    }

    public void setTblEmpPlantMap(TblPzTblEmpPlantMap tblEmpPlantMap) {
        this.tblEmpPlantMap = tblEmpPlantMap;
    }

    public int getCompCodeMap() {
        return compCodeMap;
    }

    public void setCompCodeMap(int compCodeMap) {
        this.compCodeMap = compCodeMap;
    }

    public int getPlantCodeMap() {
        return plantCodeMap;
    }

    public void setPlantCodeMap(int plantCodeMap) {
        this.plantCodeMap = plantCodeMap;
    }

    public int getEmpcodemap() {
        return empcodemap;
    }

    public void setEmpcodemap(int empcodemap) {
        this.empcodemap = empcodemap;
    }

    public int getPlantrolemap() {
        return plantrolemap;
    }

    public void setPlantrolemap(int plantrolemap) {
        this.plantrolemap = plantrolemap;
    }

    public List<TblPzTblEmpPlantMap> getListtbEmpPlantMaps() {
        return listtbEmpPlantMaps;
    }

    public void setListtbEmpPlantMaps(List<TblPzTblEmpPlantMap> listtbEmpPlantMaps) {
        this.listtbEmpPlantMaps = listtbEmpPlantMaps;
        
        
    }

    public SapSchedulerListenerPlant getSln() {
        return sln;
    }

    public void setSln(SapSchedulerListenerPlant sln) {
        this.sln = sln;
    }

    public TblCropMaster getTblCropMaster() {
        return tblCropMaster;
    }

    public void setTblCropMaster(TblCropMaster tblCropMaster) {
        this.tblCropMaster = tblCropMaster;
    }

    public TblPlantLineCode getTblplantLinecode() {
        return tblplantLinecode;
    }

    public void setTblplantLinecode(TblPlantLineCode tblplantLinecode) {
        this.tblplantLinecode = tblplantLinecode;
    }

    public String getmSd10() {
        return mSd10;
    }

    public void setmSd10(String mSd10) {
        this.mSd10 = mSd10;
    }

    public String getmSd11() {
        return mSd11;
    }

    public void setmSd11(String mSd11) {
        this.mSd11 = mSd11;
    }

    public String getmSd12() {
        return mSd12;
    }

    public void setmSd12(String mSd12) {
        this.mSd12 = mSd12;
    }

    public String getmSd13() {
        return mSd13;
    }

    public void setmSd13(String mSd13) {
        this.mSd13 = mSd13;
    }

    public String getmSd14() {
        return mSd14;
    }

    public void setmSd14(String mSd14) {
        this.mSd14 = mSd14;
    }

    public String getmSd15() {
        return mSd15;
    }

    public void setmSd15(String mSd15) {
        this.mSd15 = mSd15;
    }

    public String[] getStl() {
        return stl;
    }

    public void setStl(String[] stl) {
        this.stl = stl;
    }

    public String[] getOdv() {
        return odv;
    }

    public void setOdv(String[] odv) {
        this.odv = odv;
    }

    public String[] getGot() {
        return got;
    }

    public void setGot(String[] got) {
        this.got = got;
    }

    public String[] getElisa() {
        return elisa;
    }

    public void setElisa(String[] elisa) {
        this.elisa = elisa;
    }

    public String[] getSdcls() {
        return sdcls;
    }

    public void setSdcls(String[] sdcls) {
        this.sdcls = sdcls;
    }

    public String[] getSkipd() {
        return skipd;
    }

    public void setSkipd(String[] skipd) {
        this.skipd = skipd;
    }

    public String getmSd16() {
        return mSd16;
    }

    public void setmSd16(String mSd16) {
        this.mSd16 = mSd16;
    }

    public String getmSd17() {
        return mSd17;
    }

    public void setmSd17(String mSd17) {
        this.mSd17 = mSd17;
    }

    public String getmSd18() {
        return mSd18;
    }

    public void setmSd18(String mSd18) {
        this.mSd18 = mSd18;
    }

    public String getmSd19() {
        return mSd19;
    }

    public void setmSd19(String mSd19) {
        this.mSd19 = mSd19;
    }

    public String getmSd20() {
        return mSd20;
    }

    public void setmSd20(String mSd20) {
        this.mSd20 = mSd20;
    }

    public String getmSd21() {
        return mSd21;
    }

    public void setmSd21(String mSd21) {
        this.mSd21 = mSd21;
    }

    public String getmSd22() {
        return mSd22;
    }

    public void setmSd22(String mSd22) {
        this.mSd22 = mSd22;
    }

    public String getmSd23() {
        return mSd23;
    }

    public void setmSd23(String mSd23) {
        this.mSd23 = mSd23;
    }

    public String getmSd24() {
        return mSd24;
    }

    public void setmSd24(String mSd24) {
        this.mSd24 = mSd24;
    }

    public String getmSd25() {
        return mSd25;
    }

    public void setmSd25(String mSd25) {
        this.mSd25 = mSd25;
    }

    public String getmSd26() {
        return mSd26;
    }

    public void setmSd26(String mSd26) {
        this.mSd26 = mSd26;
    }

    public String getmSd27() {
        return mSd27;
    }

    public void setmSd27(String mSd27) {
        this.mSd27 = mSd27;
    }

    public String getmSd28() {
        return mSd28;
    }

    public void setmSd28(String mSd28) {
        this.mSd28 = mSd28;
    }

    public String getmSd29() {
        return mSd29;
    }

    public void setmSd29(String mSd29) {
        this.mSd29 = mSd29;
    }

    public String getmSd30() {
        return mSd30;
    }

    public void setmSd30(String mSd30) {
        this.mSd30 = mSd30;
    }

    public String[] getiNSPDT() {
        return iNSPDT;
    }

    public void setiNSPDT(String[] iNSPDT) {
        this.iNSPDT = iNSPDT;
    }

    public String[] getmOISTURE() {
        return mOISTURE;
    }

    public void setmOISTURE(String[] mOISTURE) {
        this.mOISTURE = mOISTURE;
    }

    public String[] getPURESEED() {
        return PURESEED;
    }

    public void setPURESEED(String[] PURESEED) {
        this.PURESEED = PURESEED;
    }

    public String[] getINERTMATTER() {
        return INERTMATTER;
    }

    public void setINERTMATTER(String[] INERTMATTER) {
        this.INERTMATTER = INERTMATTER;
    }

    public String[] getOCSCOUNT() {
        return OCSCOUNT;
    }

    public void setOCSCOUNT(String[] OCSCOUNT) {
        this.OCSCOUNT = OCSCOUNT;
    }

    public String[] getWEEDSEEDCOUNT() {
        return WEEDSEEDCOUNT;
    }

    public void setWEEDSEEDCOUNT(String[] WEEDSEEDCOUNT) {
        this.WEEDSEEDCOUNT = WEEDSEEDCOUNT;
    }

    public String[] getgRAIN() {
        return gRAIN;
    }

    public void setgRAIN(String[] gRAIN) {
        this.gRAIN = gRAIN;
    }

    public String[] getBLACKSEEDS() {
        return BLACKSEEDS;
    }

    public void setBLACKSEEDS(String[] BLACKSEEDS) {
        this.BLACKSEEDS = BLACKSEEDS;
    }

    public String[] getPINHOLESEEDS() {
        return PINHOLESEEDS;
    }

    public void setPINHOLESEEDS(String[] PINHOLESEEDS) {
        this.PINHOLESEEDS = PINHOLESEEDS;
    }

    public String[] getODVRES() {
        return ODVRES;
    }

    public void setODVRES(String[] ODVRES) {
        this.ODVRES = ODVRES;
    }

    public String[] getBULKDENSITY() {
        return BULKDENSITY;
    }

    public void setBULKDENSITY(String[] BULKDENSITY) {
        this.BULKDENSITY = BULKDENSITY;
    }

    public String[] gettHSW() {
        return tHSW;
    }

    public void settHSW(String[] tHSW) {
        this.tHSW = tHSW;
    }

    public String[] getCOLDVIGOURGERMNORMAL() {
        return COLDVIGOURGERMNORMAL;
    }

    public void setCOLDVIGOURGERMNORMAL(String[] COLDVIGOURGERMNORMAL) {
        this.COLDVIGOURGERMNORMAL = COLDVIGOURGERMNORMAL;
    }

    public String[] getFIRSTCOUNTNORMAL() {
        return FIRSTCOUNTNORMAL;
    }

    public void setFIRSTCOUNTNORMAL(String[] FIRSTCOUNTNORMAL) {
        this.FIRSTCOUNTNORMAL = FIRSTCOUNTNORMAL;
    }

    public String[] getGERMNORMAL() {
        return GERMNORMAL;
    }

    public void setGERMNORMAL(String[] GERMNORMAL) {
        this.GERMNORMAL = GERMNORMAL;
    }

    public String[] getFETNORMAL() {
        return FETNORMAL;
    }

    public void setFETNORMAL(String[] FETNORMAL) {
        this.FETNORMAL = FETNORMAL;
    }

    public String[] getSOILCOUNTDAYS() {
        return SOILCOUNTDAYS;
    }

    public void setSOILCOUNTDAYS(String[] SOILCOUNTDAYS) {
        this.SOILCOUNTDAYS = SOILCOUNTDAYS;
    }

    public String[] getAAVGERMNORMAL() {
        return AAVGERMNORMAL;
    }

    public void setAAVGERMNORMAL(String[] AAVGERMNORMAL) {
        this.AAVGERMNORMAL = AAVGERMNORMAL;
    }

    public String[] getGOTGP() {
        return GOTGP;
    }

    public void setGOTGP(String[] GOTGP) {
        this.GOTGP = GOTGP;
    }

    public String[] getGOTFEMALE() {
        return GOTFEMALE;
    }

    public void setGOTFEMALE(String[] GOTFEMALE) {
        this.GOTFEMALE = GOTFEMALE;
    }

    public String[] getGOTOTHERS() {
        return GOTOTHERS;
    }

    public void setGOTOTHERS(String[] GOTOTHERS) {
        this.GOTOTHERS = GOTOTHERS;
    }

    public String[] getbG1() {
        return bG1;
    }

    public void setbG1(String[] bG1) {
        this.bG1 = bG1;
    }

    public String[] getbG2() {
        return bG2;
    }

    public void setbG2(String[] bG2) {
        this.bG2 = bG2;
    }

    public String[] gethT() {
        return hT;
    }

    public void sethT(String[] hT) {
        this.hT = hT;
    }

    public String[] getfQR() {
        return fQR;
    }

    public void setfQR(String[] fQR) {
        this.fQR = fQR;
    }

    public String[] getQ1() {
        return q1;
    }

    public void setQ1(String[] q1) {
        this.q1 = q1;
    }

    public String[] getQ2() {
        return q2;
    }

    public void setQ2(String[] q2) {
        this.q2 = q2;
    }

    public String[] getQ3() {
        return q3;
    }

    public void setQ3(String[] q3) {
        this.q3 = q3;
    }

    public String[] getQ4() {
        return q4;
    }

    public void setQ4(String[] q4) {
        this.q4 = q4;
    }

    public String[] getQ5() {
        return q5;
    }

    public void setQ5(String[] q5) {
        this.q5 = q5;
    }

    public String[] getQ6() {
        return q6;
    }

    public void setQ6(String[] q6) {
        this.q6 = q6;
    }

    public String[] getQ7() {
        return q7;
    }

    public void setQ7(String[] q7) {
        this.q7 = q7;
    }

    public String[] getQ8() {
        return q8;
    }

    public void setQ8(String[] q8) {
        this.q8 = q8;
    }

    public String[] getQ9() {
        return q9;
    }

    public void setQ9(String[] q9) {
        this.q9 = q9;
    }

    public String getmSd31() {
        return mSd31;
    }

    public void setmSd31(String mSd31) {
        this.mSd31 = mSd31;
    }

    public String getmSd32() {
        return mSd32;
    }

    public void setmSd32(String mSd32) {
        this.mSd32 = mSd32;
    }

    public String getmSd33() {
        return mSd33;
    }

    public void setmSd33(String mSd33) {
        this.mSd33 = mSd33;
    }

    public String getmSd34() {
        return mSd34;
    }

    public void setmSd34(String mSd34) {
        this.mSd34 = mSd34;
    }

    public String getmSd35() {
        return mSd35;
    }

    public void setmSd35(String mSd35) {
        this.mSd35 = mSd35;
    }

    public String getmSd36() {
        return mSd36;
    }

    public void setmSd36(String mSd36) {
        this.mSd36 = mSd36;
    }

    public String getmSd37() {
        return mSd37;
    }

    public void setmSd37(String mSd37) {
        this.mSd37 = mSd37;
    }

    public String getmSd38() {
        return mSd38;
    }

    public void setmSd38(String mSd38) {
        this.mSd38 = mSd38;
    }

    public String getmSd39() {
        return mSd39;
    }

    public void setmSd39(String mSd39) {
        this.mSd39 = mSd39;
    }

    public String getmSd40() {
        return mSd40;
    }

    public void setmSd40(String mSd40) {
        this.mSd40 = mSd40;
    }

    public List<TbPzlIndentMastera> getListTblpzIndentMasterQcrejected() {
        return listTblpzIndentMasterQcrejected;
    }

    public void setListTblpzIndentMasterQcrejected(List<TbPzlIndentMastera> listTblpzIndentMasterQcrejected) {
        this.listTblpzIndentMasterQcrejected = listTblpzIndentMasterQcrejected;
    }
    
    
    
    
    
    public String getoutputmatList(){
        listPzTblMaterialMaster = (ArrayList<TblPzMaterialMaster>) pzmaterialDao.getList("where materialStatus = 1 ORDER BY materialId ");
        listTblPlantMaster = plantDao.getList("where plantStatus = "+compCode+"");
        listTblPzCompanyMaster = (ArrayList<TblPzCompanyMaster>) pzcompanyDao.getList("where compStatus = 1");
        return SUCCESS;
    }
    
    
    public String getPlantRoleMapMasters(){
        listTblEmpMaster = employeeDao.getList("where empStatus=1 ");
        listPzTblMaterialMaster = (ArrayList<TblPzMaterialMaster>) pzmaterialDao.getList("where materialStatus = 1 ORDER BY materialId ");
        listTblPlantMaster = plantDao.getList("where plantStatus = "+compCode+"");
        listTblPzCompanyMaster = (ArrayList<TblPzCompanyMaster>) pzcompanyDao.getList("where compStatus = 1");
        listTblRolesMaster = rolesDao.getList("where roleStatus =1");
        listtbEmpPlantMaps = (ArrayList<TblPzTblEmpPlantMap>) plantEmpMapImplDao.getList("where Status=1");
        return SUCCESS;
    }
    public PlantIndentAction() throws Exception {
        tblIndentMaster = new TbPzlIndentMastera();
        indentMaster= new TblIndentMaster();
        listTblIndentMaster = new ArrayList<TblIndentMaster>();        
        tblIndentDetails = new TblIndentDetails();
        listTblIndentDetails = new ArrayList<TblIndentDetails>();
        listTblEmpMaster = new ArrayList<TblEmpMaster>();
        listTblEmpMaster1 = new ArrayList<TblEmpMaster>();
        listTblDepartmentMaster = new ArrayList<TblDepartmentMaster>();
        listTblSectionMaster = new ArrayList<TblSectionMaster>();
        listTblIndentStatus = new ArrayList<TblIndentStatus>();
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (12)");
        listTblIndentStatus1 = new ArrayList<TblIndentStatus>();
        listTblIndentStatus2 = new ArrayList<TblIndentStatus>();
        listTblIndentStatus2 = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (4,5,6,7,8,9)");
        listTblPlantMaster = new ArrayList<TblPlantMaster>();
        listTblMaterialMaster = new ArrayList<TblMaterialMaster>();        
        listPzTblMaterialMaster = new ArrayList<TblPzMaterialMaster>();        
        listTblUmoMaster = new ArrayList<TblUmoMaster>();        
        listTblPzUmoMaster = new ArrayList<TblPzUmoMaster>();        
        listTblPzCompanyMaster = new ArrayList<TblPzCompanyMaster>();
        tblIndentProcurementLogs = new TblIndentProcurementLogs();
        listTblIndentProcurementLogs = new ArrayList<TblIndentProcurementLogs>();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();        
        listTblCropMaster = new ArrayList<TblCropMaster>();
        listTblPzCropType = new ArrayList<TblPzCropsType>();
        ListTblProcPack = new ArrayList<TblPzProcPack>();
        listTblpzIndentMaster = new ArrayList<TbPzlIndentMastera>();
        listTblplantOrderTypea =  new ArrayList<TblPlantOrderType>();
        ListTblPzLineCode = new ArrayList<TblPlantLineCode>();
        listTblPzStoLoc = new ArrayList<TblPzPlantStoLoction>();
    }

    public String getList() {
        setStatusId(1);
        TblEmpMaster empMaster = new TblEmpMaster();
        empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
        listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (1,2,3,4,5,6,7,8,9,20)  and tblPlantMaster.plantId in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where tblEmpMaster.empNumber ="+this.session.get("empNumber")+")");
        if (!listTblpzIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
   
    
    public String getIMList() {
        setStatusId(1);
        listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus=3 and tblPlantMaster.plantId in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where tblEmpMaster.empNumber ="+this.session.get("empNumber")+")");
             if (!listTblpzIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
    
    
    
    public String getQualityMgrList() {
        setStatusId(1);
     listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus=20 ");
     listTblpzIndentMasterQcrejected = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus = 0 and tblEmpMasterByIndentLmu.empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId = 14)");
         if (!listTblpzIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
    public String getDeoList() {
        setStatusId(1);
     listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus=2 and tblPlantMaster.plantId in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where tblEmpMaster.empNumber ="+this.session.get("empNumber")+")");
         if (!listTblpzIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
    
    
    
    public String getIssueConfList() {
        setStatusId(1);
        listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus=4 and tblPlantMaster.plantId in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where tblEmpMaster.empNumber ="+this.session.get("empNumber")+")");
         if (!listTblpzIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
    
    public String getFlinchargeList() {
        setStatusId(1);
    listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus=5 and tblPlantMaster.plantId in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where tblEmpMaster.empNumber ="+this.session.get("empNumber")+")");
       if (!listTblpzIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
    public String getRecConfList() {
        setStatusId(1);   listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus=6 and tblPlantMaster.plantId in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where tblEmpMaster.empNumber ="+this.session.get("empNumber")+")");
          if (!listTblpzIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
    
    public String getGRPList(){
        setStatusId(1);
        listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus=7 and tblPlantMaster.plantId in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where tblEmpMaster.empNumber ="+this.session.get("empNumber")+")");
        if (!listTblpzIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    
    }

    public String getList1() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] indId = request.getParameterValues("indId");
        if (indId.length == 1) {
            if (Integer.parseInt(indId[0]) == 1) {
                listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where tblIndentStatusByIndentFinalStatus.indentStatus=1 ");
                    } 
        }
        if (!listTblpzIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
    

    public void getDetails() {
        listTblEmpMaster = employeeDao.getList("where empStatus=1");
        listTblDepartmentMaster = departmentDao.getList("where deptStatus=2");
        listTblSectionMaster = sectionDao.getList("where secStatus=1"); listTblUmoMaster = (ArrayList<TblUmoMaster>) umoDao.getList("where umoStatus=1");
        listTblPzUmoMaster = (ArrayList<TblPzUmoMaster>)pzumoDao.getList("where umoStatus = 1");
        listTblPzCropType = pzcroptypeDao.getList("where divisionStatus = 1");
        ListTblProcPack = pzprocpackDao.getList("where colStatus = 1");
        listTblplantOrderType = indentpzorderTypeDao.getList("where orderStatus = 1");
        listPzTblPzScheduleMaterialmasterBatch  =(ArrayList<TblPzScheduleMaterialmaster>) schedulematerialDao.getList("where matcodeId ="+ matId+"");
    }

    public String addIndentRequest() throws ParseException {
        iId = 0;
        String i;
        getDetails();
        getPlantDetails();
        getCropGroup();
        setCompId(1);
        setPlantId(1);
        setDeptId(1);
        setSecId(1);
        TblEmpMaster empMaster = (TblEmpMaster) employeeDao.getList("where empStatus=1 and empNumber=" + Integer.parseInt(this.session.get("empNumber").toString()) + "").get(0);
        setEmpId(empMaster.getEmpName() + "(" + empMaster.getEmpId() + ")");
        int year = Integer.parseInt(utils.YearIn()) + 1;
        String start = utils.YearIn() + "-04" + "-01";
        String end = String.valueOf(year) + "-03" + "-31";
        Date currDate = utils.getDateFormat(utils.DateIn());
        Date start1 = utils.getDateFormat(start);
        Date end1 = utils.getDateFormat(end);
        setFinYear(utils.YearIn() + "-" + year);
        setDate(utils.DateIn2());
        listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId = " + compId + ") order by materialDesc");
        listTblUmoMaster = (ArrayList<TblUmoMaster>) umoDao.getList("where umoStatus=1");
        return SUCCESS;
    }
    
    public String getoutputPlantDetails(){
        getPlantDetails();
        ListTblProcPack = pzprocpackDao.getList("where colStatus = 1");
        return SUCCESS;
    }
    
    
    
    public String getListEmpMap(){
        listtbEmpPlantMaps = (ArrayList<TblPzTblEmpPlantMap>) plantEmpMapImplDao.getList("where Status in (1,0)");
        return SUCCESS;
    }
    
    
    public String getoutputPlantDetails1(){
        listTblPlantMaster = plantDao.getList("where plantStatus = "+compCodeMap+"");
        listTblRolesMaster = rolesDao.getList("where roleStatus =1");
        listTblEmpMaster = employeeDao.getList("where empStatus=1 ");
        listTblPzCompanyMaster = (ArrayList<TblPzCompanyMaster>) pzcompanyDao.getList("where compStatus = 1");
        return SUCCESS;
    }
    
    public String getPlantDetails() {
        getDetails();
        listTblPzCompanyMaster = companyDao.getList("where compStatus = 1 and compCode in (select tblCompanyMaster.compId from TblPzTblEmpPlantMap where tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
        listTblDepartmentMaster = departmentDao.getList("where deptStatus=2");
        listPzTblMaterialMasterdesc = pzmaterialDao.getList("where materialStatus= 1");
        listTblplantOrderTypea =  indentpzorderTypeDao.getList("where orderStatus = 1");
        if(compCode != 0){
           listTblplantOrderTypea =  indentpzorderTypeDao.getList("where orderCompany = "+compCode);
           listTblPlantMaster = plantDao.getList("where plantStatus = "+compCode+" and plantCode in (select tblPlantMaster.plantId from TblPzTblEmpPlantMap where tblEmpMaster.empNumber = "+ this.session.get("empNumber")+")");
            return SUCCESS;
        }
        else{
            return INPUT;
        }
    }
    
    
    public String getLineCodea() {
        getCropGroup();
        getPlantDetails();
        ListTblPzLineCode = pzLineCodeDao.getList("where lineStatus =1 and linePlant ="+plantId+" ");
        return SUCCESS;
    }
    
    public String getCropGroup() {
    listTblPzCropType = pzcroptypeDao.getList("where divisionStatus = 1");
    if(divisionId != 0){
        listTblCropMaster = cropDao.getList("where divisionId = "+divisionId+"");
        return SUCCESS;
    }else{
        return INPUT;
    }
    }
    
    public String getCropwiseMat(){
    getDetails();
    getPlantDetails();
    getCropGroup();
    getLineCodea();
    listPzTblPzScheduleMaterialmaster = schedulematerialDao.getList("where matStatus = 1 and companyCode = "+compCode+" and plantCode = "+plantId+" and matcodeId="+crospId+"");
    return SUCCESS;
    }
    
    
    public String getCropTypepacproc(){
    getDetails();
    getPlantDetails();
    getCropGroup();
    getLineCodea();
    getCropwiseMat();
    if(packandprocessing.equals("Packing")){
            listPzTblMaterialMaster = pzmaterialDao.getList("where materialGroup="+crospId +"and  materialPlant = "+plantId+" and materialPackType =2" );
    }else if(packandprocessing.equals("Processing")){
            listPzTblMaterialMaster = pzmaterialDao.getList("where materialGroup="+crospId +"and  materialPlant = "+plantId+" and materialPackType =1" );
    }return SUCCESS;
    }
    
   
     public String getmaterialsgroup() throws IOException,NullPointerException {
        tblPzScheduleMaterialmaster = (TblPzScheduleMaterialmaster) schedulematerialDao.getList("where companyId ="+ matId+" and matStatus = 1 and matStl = 'A' " ).get(0);
        mSd = tblPzScheduleMaterialmaster.getMatQuantity();
        mSd1 = tblPzScheduleMaterialmaster.getMaterialaDesc();
        mSd2 = tblPzScheduleMaterialmaster.getVarietyType();
        mSd3 = tblPzScheduleMaterialmaster.getMateriaaUom();
        mSd4 = tblPzScheduleMaterialmaster.getMatBatch();
        mSd5 = tblPzScheduleMaterialmaster.getMaterialaCode();
        mSd6 = tblPzScheduleMaterialmaster.getStorageLocation();
        mSd10 = tblPzScheduleMaterialmaster.getMatStl();
        
//        if(tblPzScheduleMaterialmaster.getMatOdv().isEmpty()){
//        mSd11 = "-";
//        }else{
            mSd11 = tblPzScheduleMaterialmaster.getMatOdv();
//        }
        
        
//        mSd11 = tblPzScheduleMaterialmaster.getMatOdv();
//        if(tblPzScheduleMaterialmaster.getMatGot().isEmpty()){
//        mSd12 = "-";
//        }else{
            mSd12 = tblPzScheduleMaterialmaster.getMatGot();
//        }
        
        
//        if(tblPzScheduleMaterialmaster.getMatElisa().isEmpty()){
//        mSd12 = "-";
//        }else{
            mSd12 = tblPzScheduleMaterialmaster.getMatElisa();
//        }
        
//        if(tblPzScheduleMaterialmaster.getMatGot().isEmpty()){
//        mSd13 = "-";
//        }else{
            mSd13 = tblPzScheduleMaterialmaster.getMatGot();
//        }
        
//       if(tblPzScheduleMaterialmaster.getsDCLS().isEmpty()){
//        mSd14 = "-";
//        }else{
            mSd14 = tblPzScheduleMaterialmaster.getsDCLS();
//        }
//        mSd14 = tblPzScheduleMaterialmaster.getsDCLS();
//         System.out.println("Skipd value :"+tblPzScheduleMaterialmaster.getsKIPD());
        
//         if(tblPzScheduleMaterialmaster.getsKIPD() == null){
//             mSd15 = "-";
//         }else{
              mSd15 = tblPzScheduleMaterialmaster.getsKIPD();
//         }
         
         
//         if(tblPzScheduleMaterialmaster.getiNSPDT() == null){
//             mSd16 = "-";
//         }else{
              mSd16 = tblPzScheduleMaterialmaster.getiNSPDT();
//         }
//       
       
        
        
        
        
        mSd17 = tblPzScheduleMaterialmaster.getmOISTURE();
        mSd18 = tblPzScheduleMaterialmaster.getPURESEED();
        mSd19 = tblPzScheduleMaterialmaster.getINERTMATTER();
        mSd20 = tblPzScheduleMaterialmaster.getOCSCOUNT();
        mSd21 = tblPzScheduleMaterialmaster.getWEEDSEEDCOUNT();
        mSd22 = tblPzScheduleMaterialmaster.getgRAIN();
        mSd23 = tblPzScheduleMaterialmaster.getBLACKSEEDS();
        mSd24 = tblPzScheduleMaterialmaster.getPINHOLESEEDS();
        mSd25 = tblPzScheduleMaterialmaster.getODVRES();
        mSd26 = tblPzScheduleMaterialmaster.getBULKDENSITY();
        mSd27 = tblPzScheduleMaterialmaster.gettHSW();
        mSd28 = tblPzScheduleMaterialmaster.getCOLDVIGOURGERMNORMAL();
        mSd29 = tblPzScheduleMaterialmaster.getFIRSTCOUNTNORMAL();
        mSd30 = tblPzScheduleMaterialmaster.getGERMNORMAL();
        
        mSd31 = tblPzScheduleMaterialmaster.getFETNORMAL();
        mSd32 = tblPzScheduleMaterialmaster.getSOILCOUNTDAYS();
        
        mSd33 = tblPzScheduleMaterialmaster.getAAVGERMNORMAL();
         System.out.println("tblPzScheduleMaterialmaster.getAAVGERMNORMAL(); Is : "+tblPzScheduleMaterialmaster.getAAVGERMNORMAL());
        
         
//         if(tblPzScheduleMaterialmaster.getGOTGP() == null){
//             mSd34 = "-";
//         }else{
              mSd34 = tblPzScheduleMaterialmaster.getGOTGP();
//         }
//        mSd34 = tblPzScheduleMaterialmaster.getGOTGP();

//        if(tblPzScheduleMaterialmaster.getGOTFEMALE() == null){
//             mSd35 = "-";
//         }else{
              mSd35 = tblPzScheduleMaterialmaster.getGOTFEMALE();
//         }
//        mSd35 = tblPzScheduleMaterialmaster.getGOTFEMALE();
        
//        if(tblPzScheduleMaterialmaster.getGOTOTHERS() == null){
//             mSd36 = "-";
//         }else{
              mSd36 = tblPzScheduleMaterialmaster.getGOTOTHERS();
//         }
//        mSd36 = tblPzScheduleMaterialmaster.getGOTOTHERS();
        

//        if(tblPzScheduleMaterialmaster.getbG1() == null){
//             mSd37 = "-";
//         }else{
              mSd37 = tblPzScheduleMaterialmaster.getbG1();
//         }
//        mSd37 = tblPzScheduleMaterialmaster.getbG1();
//        if(tblPzScheduleMaterialmaster.getbG2() == null){
//             mSd38 = "-";
//         }else{
              mSd38 = tblPzScheduleMaterialmaster.getbG2();
//         }
//        mSd38 = tblPzScheduleMaterialmaster.getbG2();
//        if(tblPzScheduleMaterialmaster.gethT() == null){
//             mSd39 = "-";
//         }else{
              mSd39 = tblPzScheduleMaterialmaster.gethT();
//         }
//        mSd39 = tblPzScheduleMaterialmaster.gethT();
//        if(tblPzScheduleMaterialmaster.getfQR() == null){
//             mSd40 = "-";
//         }else{
              mSd40 = tblPzScheduleMaterialmaster.getfQR();
//         }
//        mSd40 = tblPzScheduleMaterialmaster.getfQR();
        
        
        JSONObject json = new JSONObject();
        json.put("qty", mSd);
        json.put("qty1", mSd1);
        json.put("qty2", mSd2);
        json.put("qty3", mSd3);
        json.put("qty4", mSd4);
        json.put("qty5", mSd5);
        json.put("qty6", mSd6);
        json.put("qty10", mSd10);
        json.put("qty11", mSd11);
        json.put("qty12", mSd12);
        
        json.put("qty13", mSd13);
        json.put("qty14", mSd14);
        json.put("qty15", mSd15);
        
        
        json.put("qty16", mSd16);
        json.put("qty17", mSd17);
        json.put("qty18", mSd18);
        json.put("qty19", mSd19);
        json.put("qty20", mSd20);
        json.put("qty21", mSd21);
        json.put("qty22", mSd22);
        json.put("qty23", mSd23);
        json.put("qty24", mSd24);
        json.put("qty25", mSd25);
        json.put("qty26", mSd26);
        json.put("qty27", mSd27);
        json.put("qty28", mSd28);
        json.put("qty29", mSd29);
        json.put("qty30", mSd30);
        
        json.put("qty31", mSd31);
        json.put("qty32", mSd32);
        json.put("qty33", mSd33);
        
        json.put("qty34", mSd34);
        json.put("qty35", mSd35);
        json.put("qty36", mSd36);
        json.put("qty37", mSd37);
        json.put("qty38", mSd38);
        json.put("qty39", mSd39);
        json.put("qty40", mSd40);
        
        
        result = json.toString();
        return SUCCESS;
     }
     
    public void getIndentNo1() throws ParseException {
        String i;
        getPlantDetails();
        int year = Integer.parseInt(utils.YearIn()) + 1;
        String start = utils.YearIn() + "-04" + "-01";
        String end = String.valueOf(year) + "-03" + "-31";
        Date currDate = utils.getDateFormat(utils.DateIn());
        Date start1 = utils.getDateFormat(start);
        Date end1 = utils.getDateFormat(end);
//        TblPzCompanyMaster companyMaster = (TblPzCompanyMaster) pzcompanyDao.getList("where compStatus=1").get(0);
//        if (currDate.after(start1) && currDate.before(end1)) {
//            listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where tblIndentStatusByIndentFinalStatus.indentStatus=10");
//        if (!listTblpzIndentMaster.isEmpty()) {
//                tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getList("where tblIndentStatusByIndentFinalStatus.indentStatus=10 order by indentId DESC LIMIT 1").get(0);
//                i = (tblIndentMaster.getIndentNo() + 1);
//                setIsndNo(i);
//        try {
//                tblIndentMaster=(TbPzlIndentMastera)indentpzDao.getList("where indentStatus=20 and indentNo='" + i + "'").get(0);
//        } catch (IndexOutOfBoundsException e) {}
//                if(tblIndentMaster.getIndentNo()==null || tblIndentMaster.getIndentNo().length()==0 || tblIndentMaster.getIndentNo().isEmpty()){
//                    setIsndNo(i);
//        }else{
//                    i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
//                    setIsndNo(i);
//        }
//                
//            } else {
//                i = "00001";
//                setIsndNo(utils.YearIn() + i);
//        }
//        } else {
//            listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus=20");
//            if (!listTblpzIndentMaster.isEmpty()) {
//                tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getList("where indentStatus=20 order by indentId DESC LIMIT 1").get(0);
//                i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
//                try {
//                tblIndentMaster=(TbPzlIndentMastera)indentpzDao.getList("where indentStatus=20 and indentNo='" + i + "'").get(0);
//                } catch (IndexOutOfBoundsException e) {}
//                if(tblIndentMaster.getIndentNo()==null || tblIndentMaster.getIndentNo().length()==0 || tblIndentMaster.getIndentNo().isEmpty()){
//                    setIsndNo(i);
//                }else{
//                    i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
//                    setIsndNo(i);
//                }
//                //setIndNo(i);
//            } else {
//                i = "00001";
//                setIsndNo( utils.YearIn() + i);
//            }
//        }
        
        tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getList("where tblIndentStatusByIndentProcurementStatus.indentStatusId=10 order by indentId DESC LIMIT 1").get(0);
        System.out.println("tblIndentMaster"+tblIndentMaster);
                i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                setIsndNo(i);
        
    }
     	

    public String getIndentnumbers(){
        listTblPzCompanyMaster = pzcompanyDao.getList("where compStatus = 1");
        listTblpzIndentMaster =indentpzDao.getList("where indentStatus = 2");
        switch (compCode) {
            case 1100:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 2");
                return SUCCESS;
            case 1200:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 2");
                return SUCCESS;
            case 1300: 
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 2");
                return SUCCESS;
            case 1400:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 2");
                return SUCCESS;
            case 1500:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 2");
                return SUCCESS;
            case 1600:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 2");
                return SUCCESS;
            default:
                boolean clear = true;
                addFieldError("flcompanyid", "No Indents Available for this company.");
                clear = false;
                return INPUT;
        }
    }
    
    
    
    public String getDeoIndentnumbers(){
        listTblPzCompanyMaster = pzcompanyDao.getList("where compStatus = 1");
        listTblpzIndentMaster =indentpzDao.getList("where indentStatus = 3");
        switch (compCode) {
            case 1100:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 3");
                return SUCCESS;
            case 1200:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 3");
                return SUCCESS;
            case 1300:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 3");
                return SUCCESS;
            case 1400:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 3");
                return SUCCESS;
            case 1500:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 3");
                return SUCCESS;
            case 1600:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 3");
                return SUCCESS;
            default:
                boolean clear = true;
                addFieldError("flcompanyid", "No Indents Available for this company.");
                clear = false;
                return INPUT;
        }
    }
    
    
    public String getFlInchargeIndentnumbers(){
        listTblPzCompanyMaster = pzcompanyDao.getList("where compStatus = 1");
        listTblpzIndentMaster =indentpzDao.getList("where indentStatus = 4");
        switch (compCode) {
            case 1100:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 4");
                return SUCCESS;
            case 1200:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 4");
                return SUCCESS;
            case 1300:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 4");
                return SUCCESS;
            case 1400:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 4");
                return SUCCESS;
            case 1500:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 4");
                return SUCCESS;
            case 1600:
                listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"and indentStatus = 4");
                return SUCCESS;
            default:
                boolean clear = true;
                addFieldError("flcompanyid", "No Indents Available for this company.");
                clear = false;
                return INPUT;
        }
    }
    
    
    public String getAllIndentDetails() throws IOException , ParseException{
        listTblPzCompanyMaster = pzcompanyDao.getList("where compStatus = 1");
        listTblPzCompanyMaster = pzcompanyDao.getList("where compStatus = 1");
        listTblpzIndentMaster =indentpzDao.getList("where tblCompanyMaster = "+ compCode +"");
        listTblIndentDetails = indentPzDetailsDao.getList("where tblIndentMaster = "+indentId);
        tblpzIndentDetails = (TblPzIndentDetails) indentPzDetailsDao.getList("where indentDetailsStatus = 1" ).get(0);
        mSda = tblpzIndentDetails.getIndentDetailsQty();
        tblpzIndentDetails.setIndentDetailsQty(mSda);
        
    return INPUT;
    }
    
    
    public String getflindentdetails() {
         getIndentnumbers();
         
         listTblPzCompanyMaster = pzcompanyDao.getList("where compStatus = 1");
         listTblpzIndentMastera = indentpzDao.getList("where indentId="+indentsId);
         listTblIndentDetails = (ArrayList<TblIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster="+indentsId);
         return SUCCESS;
     }
    
    
    public String getDeoindentdetails() {
         getDeoIndentnumbers();
         listTblPzCompanyMaster = pzcompanyDao.getList("where compStatus = 1");
         listTblpzIndentMastera = indentpzDao.getList("where indentId="+indentsId);
         listTblIndentDetails = (ArrayList<TblIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster="+indentsId);
         return SUCCESS;
     }
    
    public String getFlInchargeindentdetails() {
         getFlInchargeIndentnumbers();
         listTblPzCompanyMaster = pzcompanyDao.getList("where compStatus = 1");
         listTblpzIndentMastera = indentpzDao.getList("where indentId="+indentsId);
         listTblIndentDetails = (ArrayList<TblIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster="+indentsId);
         return SUCCESS;
     }
    
    

    public boolean validation() {
        boolean clear = true;
        if (deptId == 0) {
            addFieldError("deptId", "Please Select Department.");
            clear = false;
        }
        if (secId == 0) {
            addFieldError("secId", "Please Select Section.");
            clear = false;
        }
        if (plantId == 0) {
            addFieldError("plantId", "Please Select plant.");
            clear = false;
        }
        if  (qty == null || Arrays.toString(qty).isEmpty() || qty.length == 0) {
            addFieldError("qty", "Please Enter Quantity Requested.");
            clear = false;
        }
       if  (stock == null || Arrays.toString(stock).isEmpty() || stock.length == 0) {
            addFieldError("stock", "Please Enter Available Stock.");
            clear = false;
        }
       
        return clear;
    }
    
    
    
    
    public String saveEmpPlantRoleMap()  throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        getListEmpMap();
    //System.out.println("Saving Started here");
    if (((Integer) this.session.get("PlantManager")) != null && ((Integer) this.session.get("PlantManager")) == 7) {
        tblEmpPlantMap = new TblPzTblEmpPlantMap();
        
        TblEmpMaster empMaster = new TblEmpMaster();
        empMaster.setEmpNumber(empcodemap);
        tblEmpPlantMap.setTblEmpMaster(empMaster);
        
        TblCompanyMaster companyMaster = new TblCompanyMaster();
        companyMaster.setCompId(compCodeMap);
        tblEmpPlantMap.setTblCompanyMaster(companyMaster);
        
        TblPlantMaster plantMaster = new TblPlantMaster();
        plantMaster.setPlantId(plantCodeMap);
        tblEmpPlantMap.setTblPlantMaster(plantMaster);
        
        TblRolesMaster rolesMaster = new TblRolesMaster();
        rolesMaster.setRoleId(plantrolemap);
        tblEmpPlantMap.setTblRolesMaster(rolesMaster);
        
        tblEmpPlantMap.setStatus(1);
        tblEmpPlantMap.setRoleStatus("ACTIVE");
        
        boolean res = plantEmpMapImplDao.save(tblEmpPlantMap);
        return SUCCESS;
    }else {
        return INPUT;
    }
    }
    
    public String saveIndentPzRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
     String ac;
            getIndentNo1();
            if (((Integer) this.session.get("PlantManager")) != null && ((Integer) this.session.get("PlantManager")) == 7) {
                tblIndentMaster=new TbPzlIndentMastera();
                tblIndentMaster.setIndentNo(isndNo);
                tblIndentMaster.setIndentFinalNumber(compCode+isndNo);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentEmp(empMaster);
                
                TblPzCompanyMaster companyMaster = new TblPzCompanyMaster();
                companyMaster.setCompId(compCode);
             
                tblIndentMaster.setTblCompanyMaster(companyMaster);
                
                TblDepartmentMaster deptMaster = new TblDepartmentMaster();
                deptMaster.setDeptId(deptId);
                tblIndentMaster.setTblDepartmentMaster(deptMaster);
                TblSectionMaster secMaster = new TblSectionMaster();
                secMaster.setSecId(secId);
                tblIndentMaster.setTblSectionMaster(secMaster);
                TblPlantMaster plantMaster = new TblPlantMaster();
                plantMaster.setPlantId(plantId);
                tblIndentMaster.setTblPlantMaster(plantMaster);
                tblIndentMaster.setIndentYear(finYear);
                tblIndentMaster.setIndentDate(utils.DateIn2());
                tblIndentMaster.setTblEmpMasterByIndentCreatedby(empMaster);
                tblIndentMaster.setTblEmpMasterByIndentApprovedby(empMaster);
                TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(10);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                tblIndentMaster.setIndentApprovedbyDate(utils.DateIn2());
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(10);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                tblIndentMaster.setTblIndentStatusByIndentProcurementStatus(indentStatus);
                tblIndentMaster.setIndentComments(comments);
                tblIndentMaster.setIndentStatus(20);
                tblIndentMaster.setIndentLmd(utils.getDateFormat(utils.DateIn2()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                tblIndentMaster.setIndentCroptype(divisionId);
                tblIndentMaster.setIndentCrop(crospId);
                
                tblIndentMaster.setIndentProcpack(colId);
                tblIndentMaster.setIndentOutmaterial(myoutputmata);
                tblIndentMaster.setIndentStartdate(fromDate);
                tblIndentMaster.setIndentOutdesc(myoutputmatab);
                tblIndentMaster.setIndentBatchnumber(batchnumber);
                tblIndentMaster.setIndentUom(matumoa);
                tblIndentMaster.setIndentLinecode(lincodabc);
                tblIndentMaster.setIndentLinedesc(linedesca);
                tblIndentMaster.setIndentOutqty(expoutPut);
                tblIndentMaster.setIndentOrderTypea(orderNoid);
                tblIndentMaster.setIndentOrderTpDesc(ordedescid);
                tblIndentMaster.setIndentPackProcessa(packandprocessing);
                tblIndentMaster.setIndentBatchNumbera(batchNumberIndent);
                
                boolean res = indentpzDao.save(tblIndentMaster);
                if (res) {
                    for (int i = 0; i < qty.length; i++) {
                        tblpzIndentDetails = new TblPzIndentDetails();
                        tblpzIndentDetails.setTblIndentMaster(tblIndentMaster);
                        TblPzScheduleMaterialmaster materialMasterav = new TblPzScheduleMaterialmaster();
                        materialMasterav.setCompanyId(md[i]);
                        tblpzIndentDetails.setIndentDetailsMaterial(materialMasterav);
                        tblpzIndentDetails.setIndentMatel(hidmat[i]);
                        tblpzIndentDetails.setIndentDetailsPurpose(pur[i]);
                        tblpzIndentDetails.setIndentDetailsVendor(ven[i]);
                        tblpzIndentDetails.setIndentDetailsQty(qty[i]);
                        tblpzIndentDetails.setIndentDetailsRmQty(qty[i]);
                        tblpzIndentDetails.setIndentDetailsStockAval(stock[i]);
                        tblpzIndentDetails.setIndentLoteNum(lot[i]);
                        tblpzIndentDetails.setIndentStoLoc(stgLoc[i]);
                        tblpzIndentDetails.setIndentMateDesc(des[i]);
                        tblpzIndentDetails.setIndentDetailsStatus(10);
                        tblpzIndentDetails.setTblEmpMaster(empMaster);
                        tblpzIndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn2()));
                        tblpzIndentDetails.setMatStl(stl[i]);
                        tblpzIndentDetails.setMatOdv(odv[i]);
                        tblpzIndentDetails.setMatGot(got[i]);
                        tblpzIndentDetails.setMatElisa(elisa[i]);
                        tblpzIndentDetails.setsDCLS(sdcls[i]);
                        tblpzIndentDetails.setsKIPD(skipd[i]);
                        
                        tblpzIndentDetails.setiNSPDT(iNSPDT[i]);
                        tblpzIndentDetails.setmOISTURE(mOISTURE[i]);
                        tblpzIndentDetails.setPURESEED(PURESEED[i]);
                        tblpzIndentDetails.setINERTMATTER(INERTMATTER[i]);
                        tblpzIndentDetails.setOCSCOUNT(OCSCOUNT[i]);
                        tblpzIndentDetails.setWEEDSEEDCOUNT(WEEDSEEDCOUNT[i]);
                        tblpzIndentDetails.setgRAIN(gRAIN[i]);
                        tblpzIndentDetails.setBLACKSEEDS(BLACKSEEDS[i]);
                        tblpzIndentDetails.setPINHOLESEEDS(PINHOLESEEDS[i]);
                        tblpzIndentDetails.setODVRES(ODVRES[i]);
                        tblpzIndentDetails.setBULKDENSITY(BULKDENSITY[i]);
                        tblpzIndentDetails.settHSW(tHSW[i]);
                        tblpzIndentDetails.setCOLDVIGOURGERMNORMAL(COLDVIGOURGERMNORMAL[i]);
                        tblpzIndentDetails.setFIRSTCOUNTNORMAL(FIRSTCOUNTNORMAL[i]);
                        tblpzIndentDetails.setGERMNORMAL(GERMNORMAL[i]);
                        tblpzIndentDetails.setFETNORMAL(FETNORMAL[i]);
                        tblpzIndentDetails.setSOILCOUNTDAYS(SOILCOUNTDAYS[i]);
                        tblpzIndentDetails.setAAVGERMNORMAL(AAVGERMNORMAL[i]);
//                        tblpzIndentDetails.setGOTGP(GOTGP[i]);
//                        tblpzIndentDetails.setGOTFEMALE(GOTFEMALE[i]);
//                        tblpzIndentDetails.setGOTOTHERS(GOTOTHERS[i]);
//                        tblpzIndentDetails.setbG1(bG1[i]);
//                        tblpzIndentDetails.setbG2(bG2[i]);
//                        tblpzIndentDetails.sethT(hT[i]);
//                        tblpzIndentDetails.setfQR(fQR[i]);
//                        tblpzIndentDetails.setQ1(q1[i]);
//                        tblpzIndentDetails.setQ2(q2[i]);
//                        tblpzIndentDetails.setQ3(q3[i]);
//                        tblpzIndentDetails.setQ4(q4[i]);
//                        tblpzIndentDetails.setQ5(q5[i]);
//                        tblpzIndentDetails.setQ6(q6[i]);
//                        tblpzIndentDetails.setQ7(q7[i]);
//                        tblpzIndentDetails.setQ8(q8[i]);
//                        tblpzIndentDetails.setQ9(q9[i]);
                        
                        
                        
                   
                        indentPzDetailsDao.save(tblpzIndentDetails);
                        listPzTblPzScheduleMaterialmaster = pzschedulematerialDao.getList("where matBatch=" + tblpzIndentDetails.getIndentLoteNum() +"and storageLocation ="+tblpzIndentDetails.getIndentStoLoc()+""); 
                        int j = 0;
                        for(TblPzScheduleMaterialmaster schedulematerialmaster : listPzTblPzScheduleMaterialmaster){
                       BigDecimal a = tblpzIndentDetails.getIndentDetailsRmQty();
                       BigDecimal b = (tblpzIndentDetails.getIndentDetailsQty()); 
                       BigDecimal c = (a.subtract(b));
                       schedulematerialmaster.setMatQuantity(c);
                       BigDecimal bd = schedulematerialmaster.getMatQuantity();
                       if(bd.intValue() == 0){
                           schedulematerialmaster.setMatStatus(20);
                       }
                       pzschedulematerialDao.save(schedulematerialmaster);
                        }
                        j++;
                    }
                    in.NewPzIndentRequestNotification(tblIndentMaster.getIndentId()); 
//                    in.NewPzIndentRequestNotificationRaghu(tblIndentMaster.getIndentId());
                    //in.test(tblIndentMaster.getIndentId());
                    return SUCCESS;
                } else {
                    getDetails();
                    return INPUT;
                }
            } else {
                getDetails();
                getPlantDetails();
                getCropGroup();
                return INPUT;
            }
    }
    
    
    
    public String saveQialityManagerIndentPzRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String ac;
        if (((Integer) this.session.get("QualityManager")) != null && ((Integer) this.session.get("QualityManager")) == 14) {
               this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(iId);
                TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);
                
                int aaa = tblIndentMaster.getIndentStatus();
                tblIndentMaster.setIndentPoNumber(indentOrderNumbera);
                tblIndentMaster.setIndentFinalRemarks(indentDeoComments);
                tblIndentMaster.setIndentStatus(2);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                tblIndentMaster.setTblEmpMasterByIndentApprovedby(empMaster);
                tblIndentMaster.setTblEmpMasterByIndentFinalApprovedby(empMaster);
                boolean res = indentpzDao.save(tblIndentMaster);
                if (res) {
                    listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                    int i = 0;
                    for (TblPzIndentDetails IndentDetails : listTblPzIndentDetails) {
                        IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        IndentDetails.setIndentDetailsStatus(1);
                        indentPzDetailsDao.save(IndentDetails);
                        i++;
                    }
                    in.NewPzQualityMgrNotification(tblIndentMaster.getIndentId());
                    
                    return SUCCESS;
                } else {
                    getDetails();
                    return INPUT;
                }
            } else {
            return INPUT;
            }
    }
    
    
    public String saveDeoIndentPzRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String ac;
        if (((Integer) this.session.get("DataEntryOperator")) != null && ((Integer) this.session.get("DataEntryOperator")) == 9) {
               this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(iId);
                TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(2);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);
                
                int aaa = tblIndentMaster.getIndentStatus();
                tblIndentMaster.setIndentPoNumber(indentOrderNumbera);
                tblIndentMaster.setIndentFinalRemarks(indentDeoComments);
                tblIndentMaster.setIndentStatus(3);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                tblIndentMaster.setTblEmpMasterByIndentApprovedby(empMaster);
                tblIndentMaster.setTblEmpMasterByIndentFinalApprovedby(empMaster);
                boolean res = indentpzDao.save(tblIndentMaster);
                if (res) {
                    listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                    int i = 0;
                    for (TblPzIndentDetails IndentDetails : listTblPzIndentDetails) {
                        IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        IndentDetails.setIndentDetailsStatus(2);
                        indentPzDetailsDao.save(IndentDetails);
                        i++;
                    }
                    in.NewPzDEONotification(tblIndentMaster.getIndentId());
                    
                    return SUCCESS;
                } else {
                    getDetails();
                    return INPUT;
                }
            } else {
            return INPUT;
            }
    }
    
    
    public String savefmIndentPzRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
      String ac;
     if (((Integer) this.session.get("FloorIncharge")) != null && ((Integer) this.session.get("FloorIncharge")) == 8) {
               this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(iId);
                tblIndentMaster.setIndentRemarks(remarks);             
                TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(3);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                tblIndentMaster.setIndentApprovedbyDate(utils.DateIn2());
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);                
                int aaa = tblIndentMaster.getIndentStatus();
                tblIndentMaster.setIndentRemarks(indentFmComments);
                tblIndentMaster.setIndentStatus(4);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                tblIndentMaster.setTblEmpMasterByIndentApprovedby(empMaster);
                boolean res = indentpzDao.save(tblIndentMaster);
                if (res) {
                    listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                    int i = 0;
                    for (TblPzIndentDetails IndentDetails : listTblPzIndentDetails) {
                        
                    IndentDetails.setIndentDetailsQty(qty[i]);
                    IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                    IndentDetails.setIndentDetailsStatus(3);
                    indentPzDetailsDao.save(IndentDetails);
                    String lote = IndentDetails.getIndentLoteNum();
                    String detailsid= IndentDetails.getIndentStoLoc();
                    listPzTblPzScheduleMaterialmaster =  (ArrayList<TblPzScheduleMaterialmaster>) pzschedulematerialDao.getList("where matBatch=" + IndentDetails.getIndentLoteNum() +"and matStatus = 2");
                    for(TblPzScheduleMaterialmaster schedulematerialmaster : listPzTblPzScheduleMaterialmaster){
                       BigDecimal a = IndentDetails.getIndentDetailsRmQty();
                       BigDecimal b = (IndentDetails.getIndentDetailsQty());
                       BigDecimal c = (a.subtract(b));
                       schedulematerialmaster.setMatQuantity(c);
                       BigDecimal bd = schedulematerialmaster.getMatQuantity();
                       if(bd.intValue() == 0){
                           schedulematerialmaster.setMatStatus(2);
                       }
                       pzschedulematerialDao.save(schedulematerialmaster);
                    }
                    i++;
                    }
                    in.NewPzFlMgrNotification(tblIndentMaster.getIndentId());
                    return SUCCESS;
                } else {
                    getDetails();
                    return INPUT;
                }
     } else {
         return INPUT;
     }
    }
    
//    DONE
    public String savePzIssueConfirmation() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String ac;
        if (((Integer) this.session.get("IissueConfirm")) != null && ((Integer) this.session.get("IissueConfirm")) == 12) {
               this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(iId);
               TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(4);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);
                int aaa = tblIndentMaster.getIndentStatus();
                tblIndentMaster.setIndentPoNumber(indentOrderNumbera);
                tblIndentMaster.setIndentGrnCommants(indentGrnReceiptComments);
                tblIndentMaster.setIndentStatus(5);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                boolean res = indentpzDao.save(tblIndentMaster);
                if (res) {
                    listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");

                    int i = 0;
                    for (TblPzIndentDetails IndentDetails : listTblPzIndentDetails) {
                        IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        IndentDetails.setIndentDetailsStatus(4);
                        indentPzDetailsDao.save(IndentDetails);
                        i++;
                    }
                    in.NewPzIssueConfirmation(tblIndentMaster.getIndentId());
                    return SUCCESS;
                } else {
                    getDetails();
                    return INPUT;
                }
        } else {
            return INPUT;
            }
    }
    
    public String savePzReceiptConfirmation() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String ac;
        if (((Integer) this.session.get("ReceiptConfirm")) != null && ((Integer) this.session.get("ReceiptConfirm")) == 13) {
               this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(iId);
               TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(5);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);
                int aaa = tblIndentMaster.getIndentStatus();
                tblIndentMaster.setIndentStatus(7);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                boolean res = indentpzDao.save(tblIndentMaster);
                if (res) {
                    listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                    int i = 0;
                    for (TblPzIndentDetails IndentDetails : listTblPzIndentDetails) {
                        IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        IndentDetails.setIndentDetailsStatus(6);
                        indentPzDetailsDao.save(IndentDetails);
                        i++;
                    }
                    in.NewPzReceiptConfirmation(tblIndentMaster.getIndentId());
                    return SUCCESS;
                } else {
                    getDetails();
                    return INPUT;
                }
        } else {
            return INPUT;
        }
    }
    
    
    
    
    public String saveFlInchargeIndentPzRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String ac;
        if (((Integer) this.session.get("GoodsIncharge")) != null && ((Integer) this.session.get("GoodsIncharge")) == 10) {
            this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(iId);
            TblIndentStatus indentStatus = new TblIndentStatus();
            indentStatus.setIndentStatusId(6);
            tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
            indentStatus = new TblIndentStatus();
            indentStatus.setIndentStatusId(1);
            int aaa = tblIndentMaster.getIndentStatus();
            tblIndentMaster.setIndentActOutQty(atualOutputQty);
            tblIndentMaster.setIndentFlInchargeCommants(indentFlinchargeComments);
            tblIndentMaster.setIndentStoLocation(stoLocationa);
            tblIndentMaster.setIndentStatus(6);
            tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
            TblEmpMaster empMaster = new TblEmpMaster();
            empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
            tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
            boolean res = indentpzDao.save(tblIndentMaster);
            if (res) {
                listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                int i = 0;
                for (TblPzIndentDetails IndentDetails : listTblPzIndentDetails) {
                    IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                    IndentDetails.setIndentDetailsStatus(5);
                    indentPzDetailsDao.save(IndentDetails);
                    i++;
                }
                in.NewPzFlInchargeNotification(tblIndentMaster.getIndentId());
                return SUCCESS;
            }else{
                getDetails();
                return INPUT;
            }
        }else{
            return INPUT;
        }
    }
    
    public String saveOutputMaterial()throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        getoutputmatList();
        if (((Integer) this.session.get("SuperAdmin")) == 1 && ((Integer) this.session.get("Admin")) == 2) {
            TblPzMaterialMaster tblPzMaterialMaster = new TblPzMaterialMaster();
            tblPzMaterialMaster.setMaterialCode(matCodeout);
            tblPzMaterialMaster.setMaterialPlant(plantCodeout);
            tblPzMaterialMaster.setMaterialDesc(matDescout);
            tblPzMaterialMaster.setMaterialVariety(matTypeout);
            tblPzMaterialMaster.setMaterialGroup(matGroupout);
            tblPzMaterialMaster.setMaterialUom(matUomout);
            tblPzMaterialMaster.setMaterialPackType(matpacktypeout);
            tblPzMaterialMaster.setMaterialStatus(1);
            cOutputMaterialMaster.save(tblPzMaterialMaster);
            return SUCCESS;
        }
        return INPUT;
    }
    
    public String savePzGrnRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String ac;
        if (((Integer) this.session.get("GRNIncharge")) != null && ((Integer) this.session.get("GRNIncharge")) == 11) {
               this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(iId);
               TblIndentStatus indentStatus = new TblIndentStatus();
               indentStatus.setIndentStatusId(7);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);
                int aaa = tblIndentMaster.getIndentStatus();
                tblIndentMaster.setIndentGrnNumber(grnreceiptnumber);
                tblIndentMaster.setIndentGrnCommants(indentGrnReceiptComments);
                tblIndentMaster.setIndentStatus(8);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                boolean res = indentpzDao.save(tblIndentMaster);
                if (res) {
                    listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                    int i = 0;
                    for (TblPzIndentDetails IndentDetails : listTblPzIndentDetails) {
                        IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        IndentDetails.setIndentDetailsStatus(7);
                        indentPzDetailsDao.save(IndentDetails);
                        i++;
                    }
                    in.NewPzGRNRequestNotification(tblIndentMaster.getIndentId());
                    return SUCCESS;
                }else{
                    getDetails();
                    return INPUT;
                }
        }else{
            return INPUT;
        }
    }  
    
    public String deletePlantIndent() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException , NullPointerException{
        String ac;
            getIndentNo1();
            if (((Integer) this.session.get("PlantManager")) != null && ((Integer) this.session.get("PlantManager")) == 7)  {
                this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(iId);
                TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(0);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(0);
                int aaa = tblIndentMaster.getIndentStatus();
//                System.out.println("indentDeoComments"+indentDeoComments);
//                if(!Rejectcomments.isEmpty() ){
//                     tblIndentMaster.setIndentComments(indentDeoComments);
//                
//                }else{
//                    tblIndentMaster.setIndentComments(Rejectcomments);
//                }
                tblIndentMaster.setIndentComments(Rejectcomments);
                tblIndentMaster.setIndentStatus(0);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                boolean res = indentpzDao.save(tblIndentMaster);
                if (res) {
                    listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                    int i = 0;
                    for (TblPzIndentDetails IndentDetails : listTblPzIndentDetails) {
                        IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        IndentDetails.setIndentDetailsStatus(0);
                        indentPzDetailsDao.save(IndentDetails);
                        i++;
                    }
                    in.NewPzDeleteNotification(tblIndentMaster.getIndentId());
                    return SUCCESS;
                }else{
                    getDetails();
                    return INPUT;
                }
            }else{
                return INPUT;
            }
    }
    
    
    public String deleteQmPlantIndent() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException , NullPointerException{
        String ac;
            getIndentNo1();
            if (((Integer) this.session.get("QualityManager")) != null && ((Integer) this.session.get("QualityManager")) == 14)  {
                this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(iId);
                TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(0);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(0);
                int aaa = tblIndentMaster.getIndentStatus();
//                System.out.println("indentDeoComments"+indentDeoComments);
//                if(!Rejectcomments.isEmpty() ){
//                     tblIndentMaster.setIndentComments(indentDeoComments);
//                
//                }else{
//                    tblIndentMaster.setIndentComments(Rejectcomments);
//                }
                tblIndentMaster.setIndentComments(indentDeoComments);
                tblIndentMaster.setIndentStatus(0);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                boolean res = indentpzDao.save(tblIndentMaster);
                if (res) {
                    listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                    int i = 0;
                    for (TblPzIndentDetails IndentDetails : listTblPzIndentDetails) {
                        IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        IndentDetails.setIndentDetailsStatus(0);
                        indentPzDetailsDao.save(IndentDetails);
                        i++;
                    }
                    in.NewPzQualityDeleteNotification(tblIndentMaster.getIndentId());
                    return SUCCESS;
                }else{
                    getDetails();
                    return INPUT;
                }
            }else{
                return INPUT;
            }
    }
    
    
  
    public String getSelfIndentDetailsPz() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentFinalNumber()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            //setCompName(tblIndentMaster.getTblCompanyMaster().getCompName());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setDeptName(tblIndentMaster.getTblDepartmentMaster().getDeptName());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            
            tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblIndentMaster.getIndentCrop()).get(0);
            System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
            setIndentCrop(tblCropMaster.getCropName());
           
            setMyoutputmata(tblIndentMaster.getIndentOutmaterial());
            setPackandprocessing(tblIndentMaster.getIndentPackProcessa());
            setOrderNoid(tblIndentMaster.getIndentOrderTypea()+"----"+tblIndentMaster.getIndentOrderTpDesc());
            setLincodabc(tblIndentMaster.getIndentLinecode());
            setBatchNumberIndent(tblIndentMaster.getIndentBatchNumbera());
            setMatumoa(tblIndentMaster.getIndentUom());
            setFromDate(tblIndentMaster.getIndentStartdate());
            setExpoutPut(tblIndentMaster.getIndentOutqty());
            setIndentOrderNumbera(tblIndentMaster.getIndentPoNumber());
            setIndentDeoComments(tblIndentMaster.getIndentFinalRemarks());
            listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where indentDetailsStatus=2 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");            
        }
        return SUCCESS;
    }
    
   public String getSelfIndentDetailsforDelete() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentFinalNumber()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            //setCompName(tblIndentMaster.getTblCompanyMaster().getCompName());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setDeptName(tblIndentMaster.getTblDepartmentMaster().getDeptName());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            setIndentCrop(tblIndentMaster.getIndentCrop());
            setMyoutputmata(tblIndentMaster.getIndentOutmaterial());
            setPackandprocessing(tblIndentMaster.getIndentPackProcessa());
            setOrderNoid(tblIndentMaster.getIndentOrderTypea()+"----"+tblIndentMaster.getIndentOrderTpDesc());
            setLincodabc(tblIndentMaster.getIndentLinecode());
            setBatchNumberIndent(tblIndentMaster.getIndentBatchNumbera());
            setIndentOrderNumbera(tblIndentMaster.getIndentPoNumber());
            setMatumoa(tblIndentMaster.getIndentUom());
            setFromDate(tblIndentMaster.getIndentStartdate());
            setExpoutPut(tblIndentMaster.getIndentOutqty());
            setComments(tblIndentMaster.getIndentComments());
            listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where indentDetailsStatus=10 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");            
        }
        return SUCCESS;
    }
    
   
   public String getQualityCheckMgrDetails() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentFinalNumber()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setDeptName(tblIndentMaster.getTblDepartmentMaster().getDeptName());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            
            tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblIndentMaster.getIndentCrop()).get(0);
            System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());            
            
            setIndentCrop(tblCropMaster.getCropName());
            setMyoutputmata(tblIndentMaster.getIndentOutmaterial());
            setPackandprocessing(tblIndentMaster.getIndentPackProcessa());
            setOrderNoid(tblIndentMaster.getIndentOrderTypea()+"----"+tblIndentMaster.getIndentOrderTpDesc());
            setLincodabc(tblIndentMaster.getIndentLinecode());
            setBatchNumberIndent(tblIndentMaster.getIndentBatchNumbera());
            setIndentOrderNumbera(tblIndentMaster.getIndentPoNumber());
            setMatumoa(tblIndentMaster.getIndentUom());
            setFromDate(tblIndentMaster.getIndentStartdate());
            setExpoutPut(tblIndentMaster.getIndentOutqty());
            setComments(tblIndentMaster.getIndentComments());
            listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where indentDetailsStatus=10 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");            
        }
        return SUCCESS;
    }
   
   
   
   
   public String getSelfIndentDetailsDeoMger() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentFinalNumber()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setDeptName(tblIndentMaster.getTblDepartmentMaster().getDeptName());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            
            tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblIndentMaster.getIndentCrop()).get(0);
            System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());            
            
            setIndentCrop(tblCropMaster.getCropName());
            setMyoutputmata(tblIndentMaster.getIndentOutmaterial());
            setPackandprocessing(tblIndentMaster.getIndentPackProcessa());
            setOrderNoid(tblIndentMaster.getIndentOrderTypea()+"----"+tblIndentMaster.getIndentOrderTpDesc());
            setLincodabc(tblIndentMaster.getIndentLinecode());
            setBatchNumberIndent(tblIndentMaster.getIndentBatchNumbera());
            setIndentOrderNumbera(tblIndentMaster.getIndentPoNumber());
            setMatumoa(tblIndentMaster.getIndentUom());
            setFromDate(tblIndentMaster.getIndentStartdate());
            setExpoutPut(tblIndentMaster.getIndentOutqty());
            setComments(tblIndentMaster.getIndentFinalRemarks());
            listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");            
        }
        return SUCCESS;
    }
   
   
    
    
    
    public String getSelfIndentDetailsFlMger() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentFinalNumber()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            //setCompName(tblIndentMaster.getTblCompanyMaster().getCompName());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setDeptName(tblIndentMaster.getTblDepartmentMaster().getDeptName());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblIndentMaster.getIndentCrop()).get(0);
            System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
            setIndentCrop(tblCropMaster.getCropName());
            setMyoutputmata(tblIndentMaster.getIndentOutmaterial());
            setPackandprocessing(tblIndentMaster.getIndentPackProcessa());
            setOrderNoid(tblIndentMaster.getIndentOrderTypea()+"----"+tblIndentMaster.getIndentOrderTpDesc());
            setLincodabc(tblIndentMaster.getIndentLinecode());
            setBatchNumberIndent(tblIndentMaster.getIndentBatchNumbera());
            setMatumoa(tblIndentMaster.getIndentUom());
            setFromDate(tblIndentMaster.getIndentStartdate());
            setExpoutPut(tblIndentMaster.getIndentOutqty());
            setIndentOrderNumbera(tblIndentMaster.getIndentPoNumber());
            setComments(tblIndentMaster.getIndentComments());
            listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where indentDetailsStatus=3 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");            
        }
        return SUCCESS;
    }
    
    
    
    
    public String getSelfIndentIssueConfirmation() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentFinalNumber()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            //setCompName(tblIndentMaster.getTblCompanyMaster().getCompName());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setDeptName(tblIndentMaster.getTblDepartmentMaster().getDeptName());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblIndentMaster.getIndentCrop()).get(0);
            System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
            setIndentCrop(tblCropMaster.getCropName());
            setMyoutputmata(tblIndentMaster.getIndentOutmaterial());
            setPackandprocessing(tblIndentMaster.getIndentPackProcessa());
            setOrderNoid(tblIndentMaster.getIndentOrderTypea()+"----"+tblIndentMaster.getIndentOrderTpDesc());
            setLincodabc(tblIndentMaster.getIndentLinecode());
            setBatchNumberIndent(tblIndentMaster.getIndentBatchNumbera());
            setAtualOutputQty(tblIndentMaster.getIndentActOutQty());
            setIndentOrderNumbera(tblIndentMaster.getIndentPoNumber());
            setMatumoa(tblIndentMaster.getIndentUom());
            setFromDate(tblIndentMaster.getIndentStartdate());
            setExpoutPut(tblIndentMaster.getIndentOutqty());
            setGrnreceiptnumber(tblIndentMaster.getIndentGrnNumber());
            setComments(tblIndentMaster.getIndentRemarks());
            listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where indentDetailsStatus=3 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");            
        }
        return SUCCESS;
    }
    
    
    
    
    
    public String getSelfIndentIssueReceiptConfirmation() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentFinalNumber()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            //setCompName(tblIndentMaster.getTblCompanyMaster().getCompName());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setDeptName(tblIndentMaster.getTblDepartmentMaster().getDeptName());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblIndentMaster.getIndentCrop()).get(0);
            System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
            setIndentCrop(tblCropMaster.getCropName());
            setMyoutputmata(tblIndentMaster.getIndentOutmaterial());
            setPackandprocessing(tblIndentMaster.getIndentPackProcessa());
            setOrderNoid(tblIndentMaster.getIndentOrderTypea()+"----"+tblIndentMaster.getIndentOrderTpDesc());
            setLincodabc(tblIndentMaster.getIndentLinecode());
            setBatchNumberIndent(tblIndentMaster.getIndentBatchNumbera());
            setAtualOutputQty(tblIndentMaster.getIndentActOutQty());
            setIndentOrderNumbera(tblIndentMaster.getIndentPoNumber());
            setStoLocationa(tblIndentMaster.getIndentStoLocation());
            setMatumoa(tblIndentMaster.getIndentUom());
            setFromDate(tblIndentMaster.getIndentStartdate());
            setExpoutPut(tblIndentMaster.getIndentOutqty());
            setGrnreceiptnumber(tblIndentMaster.getIndentGrnNumber());
            setComments(tblIndentMaster.getIndentRemarks());
            listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where indentDetailsStatus=5 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");            
        }
        return SUCCESS;
    }
    
    
    
    
    
    public String getSelfIndentDetailsFlIncharge() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentFinalNumber()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            //setCompName(tblIndentMaster.getTblCompanyMaster().getCompName());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setDeptName(tblIndentMaster.getTblDepartmentMaster().getDeptName());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblIndentMaster.getIndentCrop()).get(0);
            System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
            
            
            setIndentCrop(tblCropMaster.getCropName());
            setMyoutputmata(tblIndentMaster.getIndentOutmaterial());
            setPackandprocessing(tblIndentMaster.getIndentPackProcessa());
            setOrderNoid(tblIndentMaster.getIndentOrderTypea()+"----"+tblIndentMaster.getIndentOrderTpDesc());
            setLincodabc(tblIndentMaster.getIndentLinecode());
            setBatchNumberIndent(tblIndentMaster.getIndentBatchNumbera());
            setMatumoa(tblIndentMaster.getIndentUom());
            setFromDate(tblIndentMaster.getIndentStartdate());
            setExpoutPut(tblIndentMaster.getIndentOutqty());
            setIndentOrderNumbera(tblIndentMaster.getIndentPoNumber());
            setComments(tblIndentMaster.getIndentFinalRemarks());
            listPzTblPzScheduleMaterialmaster = schedulematerialDao.getList("where matStatus = 1 and companyCode = "+tblIndentMaster.getTblCompanyMaster().getCompId()+" and plantCode = "+tblIndentMaster.getTblPlantMaster().getPlantId()+"");
            listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where indentDetailsStatus=4 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");   
            listTblPzStoLoc = (ArrayList<TblPzPlantStoLoction>) instoLocDao.getList("where plantStatus = 1 and plantCode="+tblIndentMaster.getTblPlantMaster().getPlantId()+"");
        }
        return SUCCESS;
    }
    
   
    public String getGRNReceipt() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TbPzlIndentMastera) indentpzDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentFinalNumber()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            //setCompName(tblIndentMaster.getTblCompanyMaster().getCompName());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setDeptName(tblIndentMaster.getTblDepartmentMaster().getDeptName());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            tblCropMaster = (TblCropMaster) cropDao.getList("where cropId ="+tblIndentMaster.getIndentCrop()).get(0);
            System.out.println("Crop Id New Id :"+tblCropMaster.getCropName());
            
            
            setIndentCrop(tblCropMaster.getCropName());
            setMyoutputmata(tblIndentMaster.getIndentOutmaterial());
            setPackandprocessing(tblIndentMaster.getIndentPackProcessa());
            setOrderNoid(tblIndentMaster.getIndentOrderTypea()+"----"+tblIndentMaster.getIndentOrderTpDesc());
            setLincodabc(tblIndentMaster.getIndentLinecode());
            setBatchNumberIndent(tblIndentMaster.getIndentBatchNumbera());
            setAtualOutputQty(tblIndentMaster.getIndentActOutQty());
            setStoLocationa(tblIndentMaster.getIndentStoLocation());
            setMatumoa(tblIndentMaster.getIndentUom());
            setFromDate(tblIndentMaster.getIndentStartdate());
            setExpoutPut(tblIndentMaster.getIndentOutqty());
            setIndentOrderNumbera(tblIndentMaster.getIndentPoNumber());
            setComments(tblIndentMaster.getIndentFlInchargeCommants());
            listTblPzIndentDetails = (ArrayList<TblPzIndentDetails>) indentPzDetailsDao.getList("where indentDetailsStatus=6 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");            
        }
        return SUCCESS;
    }
    
    
    
    public String addoutputMaterial(){
        listTblPzCompanyMaster = (ArrayList<TblPzCompanyMaster>) pzcompanyDao.getList("where compStatus = 1");
        listTblPlantMaster = plantDao.getList("where plantStatus = "+compCode+"");
        return SUCCESS;
    }


    public String IndentReportUser() throws Exception {
        try {
            IndentReportUser report = new IndentReportUser();
            inputStream = report.UserPzIndentProcurementReport(Integer.parseInt(session.get("empNumber").toString()));
            System.out.println("inputstream"+inputStream);
            file = "PlantindentReport(" + utils.DateIn() + ").xls";
            setFile("PlantindentReport(" + utils.DateIn() + ").xls");
        } catch (Exception e) {
        }
        return SUCCESS;
    }

    public String IndentReportRm() throws Exception {
        try {
            IndentReportUser report = new IndentReportUser();
            inputStream = report.RmProcurementReport(Integer.parseInt(session.get("empNumber").toString()));
            file = "ProcurementReport(" + utils.DateIn() + ").xls";
            setFile("ProcurementReport(" + utils.DateIn() + ").xls");
        } catch (Exception e) {
        }
        return SUCCESS;
    }

    public String IndentReportDept() throws Exception {
        try {
            IndentReportUser report = new IndentReportUser();
            inputStream = report.DeptProcurementReport(Integer.parseInt(session.get("empNumber").toString()));
            file = "ProcurementReport(" + utils.DateIn() + ").xls";
            setFile("ProcurementReport(" + utils.DateIn() + ").xls");
        } catch (Exception e) {
        }
        return SUCCESS;
    }

    public String IndentReportProcurement() throws Exception {
        try {
            IndentReportUser report = new IndentReportUser();
            inputStream = report.ProcurementReport(Integer.parseInt(session.get("empNumber").toString()));
            file = "ProcurementReport(" + utils.DateIn() + ").xls";
            setFile("ProcurementReport(" + utils.DateIn() + ").xls");
        } catch (Exception e) {
        }
        return SUCCESS;
    }

    public String IndentReport() throws Exception {
        listTblEmpMaster = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select a.tblEmpMasterByReportSub.empNumber from TblMapEmpReporting  as a where a.tblEmpMasterByReportSup.empNumber in (select b.tblEmpMasterByReportSub.empNumber from TblMapEmpReporting as b where b.tblEmpMasterByReportSup.empNumber =" + Integer.parseInt(session.get("empNumber").toString()) + ")) and empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where empRolesStatus=1 and tblRolesMaster.roleId=3)");
        listTblEmpMaster1 = (ArrayList<TblEmpMaster>) employeeDao.getList("where empStatus=1 and empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ") and empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where empRolesStatus=1 and tblRolesMaster.roleId=4)");
        listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId in (select tblCompanyMaster.compId from TblMapCompanyEmp where tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(this.session.get("empNumber").toString()) + " and tblCompanyMaster.compId=1)) order by materialDesc");
        listTblUmoMaster = (ArrayList<TblUmoMaster>) umoDao.getList("where umoStatus=1");
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId not in (10,11)");
        return SUCCESS;
    }

    public String detailedIndentReport() throws Exception {
        try {
            IndentDetailsReport report = new IndentDetailsReport();
            inputStream = report.DetailsReport(empNumber, fromDate, toDate, empNumber1, materialId, indentStatusId,Integer.parseInt(session.get("empNumber").toString()),Integer.parseInt(session.get("Admin").toString()),Integer.parseInt(session.get("deptId").toString()));
            file = "ProcurementReport(" + utils.DateIn() + ").xls";
            setFile("ProcurementReport(" + utils.DateIn() + ").xls");

        } catch (Exception e) {
        }
        IndentReport();
        setEmpNumber(0);
        setFromDate("");
        setToDate("");
        setEmpNumber1(0);
        setMaterialId(0);
        setIndentStatusId(0);
        return SUCCESS;
    }

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    public TbPzlIndentMaster getModel() {
        return this.tblpzIndentMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }   
}
