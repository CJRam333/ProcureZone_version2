/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.indent.action;

import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import pojo.TblCompanyMaster;
import pojo.TblDepartmentMaster;
import pojo.TblEmpMaster;
import pojo.TblIndentDetails;
import pojo.TblIndentMaster;
import pojo.TblIndentProcurementLogs;
import pojo.TblIndentStatus;
import pojo.TblMapCompanyPlantMaterial;
import pojo.TblMaterialMaster;
import pojo.TblPlantMaster;
import pojo.TblSectionMaster;
import pojo.TblUmoMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
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
import seeds.masters.daoImpl.SectionDaoImpl;
import seeds.masters.daoImpl.UmoDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class IndentAction extends ActionSupport implements ModelDriven<TblIndentMaster>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblIndentMaster tblIndentMaster;
    private TblIndentMaster indentMaster;
    private List<TblIndentMaster> listTblIndentMaster;
    private final IndentDaoImpl indentDao = DaoFactory.getDao(IndentDaoImpl.class);
    private TblIndentDetails tblIndentDetails;
    private List<TblIndentDetails> listTblIndentDetails;
    private final IndentDetailsDaoImpl indentDetailsDao = DaoFactory.getDao(IndentDetailsDaoImpl.class);
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
    private final PlantDaoImpl plantDao = DaoFactory.getDao(PlantDaoImpl.class);
    private List<TblMaterialMaster> listTblMaterialMaster;
    private final MaterialDaoImpl materialDao = DaoFactory.getDao(MaterialDaoImpl.class);
    private List<TblUmoMaster> listTblUmoMaster;
    private final UmoDaoImpl umoDao = DaoFactory.getDao(UmoDaoImpl.class);
    private TblIndentProcurementLogs tblIndentProcurementLogs;
    private List<TblIndentProcurementLogs> listTblIndentProcurementLogs;
    private final IndentProcurementDaoImpl indentProcurementDao = DaoFactory.getDao(IndentProcurementDaoImpl.class);
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private HttpServletResponse response;
    private TblMapCompanyPlantMaterial tblMapCompanyPlantMaterial;
    private List<TblMapCompanyPlantMaterial> listTblMapCompanyPlantMaterial;
    private final CompPlantMaterialDaoImpl compPlantMaterialDao = DaoFactory.getDao(CompPlantMaterialDaoImpl.class);

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

    private int[] md;
    private int[] umo;
    private BigDecimal[] qty;
    private BigDecimal[] rmQty;
    private BigDecimal[] deptQty;
    private BigDecimal[] stock;
    private String[] pur;
    private String[] ven;
    private BigDecimal[] pricing;
    private int statusId;
    public static int iId;
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
    private String matId;
    private String comp;
    private String plant;
    public static BigDecimal mId;
    public String result;

    public TblIndentMaster getTblIndentMaster() {
        return tblIndentMaster;
    }

    public void setTblIndentMaster(TblIndentMaster tblIndentMaster) {
        this.tblIndentMaster = tblIndentMaster;
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
    
    public String getMatId() {
        return matId;
    }

    public void setMatId(String matId) {
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

    public IndentAction() throws Exception {
        tblIndentMaster = new TblIndentMaster();
        indentMaster= new TblIndentMaster();
        listTblIndentMaster = new ArrayList<TblIndentMaster>();        
        tblIndentDetails = new TblIndentDetails();
        listTblIndentDetails = new ArrayList<TblIndentDetails>();
        listTblEmpMaster = new ArrayList<TblEmpMaster>();
        listTblEmpMaster1 = new ArrayList<TblEmpMaster>();
        listTblDepartmentMaster = new ArrayList<TblDepartmentMaster>();
        listTblSectionMaster = new ArrayList<TblSectionMaster>();
        listTblIndentStatus = new ArrayList<TblIndentStatus>();
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (1,2,3,4)");
        listTblIndentStatus1 = new ArrayList<TblIndentStatus>();
        listTblIndentStatus2 = new ArrayList<TblIndentStatus>();
        listTblIndentStatus2 = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (4,5,6,7,8,9)");
        listTblPlantMaster = new ArrayList<TblPlantMaster>();
        listTblMaterialMaster = new ArrayList<TblMaterialMaster>();
        listTblUmoMaster = new ArrayList<TblUmoMaster>();
        tblIndentProcurementLogs = new TblIndentProcurementLogs();
        listTblIndentProcurementLogs = new ArrayList<TblIndentProcurementLogs>();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
        
    }

    public String getList() {
        setStatusId(1);
        listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 and tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
        if (!listTblIndentMaster.isEmpty()) {
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
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 and tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
            } else if (Integer.parseInt(indId[0]) == 2) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where  (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=2 and tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ") or (indentStatus=1 and tblIndentStatusByIndentFinalStatus.indentStatusId=2 and tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            } else if (Integer.parseInt(indId[0]) == 3) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=1 and tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
            } else if (Integer.parseInt(indId[0]) == 4) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
            }
        }
        if (!listTblIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void getDetails() {
        listTblEmpMaster = employeeDao.getList("where empStatus=1");
        listTblDepartmentMaster = departmentDao.getList("where deptStatus=1");
        listTblSectionMaster = sectionDao.getList("where secStatus=1");
        listTblPlantMaster = plantDao.getList("where plantStatus=1");
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (2,3)");
        listTblIndentStatus1 = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (2,4)");
        listTblIndentStatus3 = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (5,6,7,8,9)");
        listTblCompanyMaster = companyDao.getList("where compStatus=1 and compId in (select tblCompanyMaster.compId from TblMapCompanyEmp where tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(this.session.get("empNumber").toString()) + ")");
        //listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId in (select tblCompanyMaster.compId from TblMapCompanyEmp where tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(this.session.get("empNumber").toString()) + " and tblCompanyMaster.compId=1)) order by materialDesc");
        listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId in (select tblCompanyMaster.compId from TblMapCompanyEmp where tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(this.session.get("empNumber").toString()) + ")) order by materialDesc");
        listTblUmoMaster = (ArrayList<TblUmoMaster>) umoDao.getList("where umoStatus=1");
        
        
        
        
        
        
        
        
    }

    public String addIndentRequest() throws ParseException {
        iId = 0;
        String i;
        getDetails();
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
        
        /*TblCompanyMaster companyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compId = " + compId + "").get(0);
        
        
        if (currDate.after(start1) && currDate.before(end1)) {
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1");
            if (!listTblIndentMaster.isEmpty()) {
                tblIndentMaster = (TblIndentMaster) indentDao.getList("where indentStatus=1 order by indentId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                //setIndNo(i);
                try {
                indentMaster=(TblIndentMaster)indentDao.getList("where indentStatus=1 and indentNo='" + i + "'").get(0);
                } catch (IndexOutOfBoundsException e) {}
                if(indentMaster.getIndentNo()==null || indentMaster.getIndentNo().length()==0 || indentMaster.getIndentNo().isEmpty()){
                    setIndNo(i);
                }else{
                    i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                    setIndNo(i);
                }
            } else {
                i = "00001";
                setIndNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
        } else {
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1");
            if (!listTblIndentMaster.isEmpty()) {
                tblIndentMaster = (TblIndentMaster) indentDao.getList("where indentStatus=1 order by indentId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                //setIndNo(i);
                try {
                indentMaster=(TblIndentMaster)indentDao.getList("where indentStatus=1 and indentNo='" + i + "'").get(0);
                } catch (IndexOutOfBoundsException e) {}
                if(indentMaster.getIndentNo()==null || indentMaster.getIndentNo().length()==0 || indentMaster.getIndentNo().isEmpty()){
                    setIndNo(i);
                }else{
                    i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                    setIndNo(i);
                }
            } else {
                i = "00001";
                setIndNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
            //i = "00001";
            setIndNo(companyMaster.getCompCode() + utils.YearIn() + i);//
        }*/
        listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId = " + compId + ") order by materialDesc");
        listTblUmoMaster = (ArrayList<TblUmoMaster>) umoDao.getList("where umoStatus=1");
        return SUCCESS;
    }

    public String getIndentNo() throws ParseException {
        String i;
        getDetails();
        /*int year = Integer.parseInt(utils.YearIn()) + 1;
        String start = utils.YearIn() + "-04" + "-01";
        String end = String.valueOf(year) + "-03" + "-31";
        Date currDate = utils.getDateFormat(utils.DateIn());
        Date start1 = utils.getDateFormat(start);
        Date end1 = utils.getDateFormat(end);
        TblCompanyMaster companyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compId = " + compId + "").get(0);
        if (currDate.after(start1) && currDate.before(end1)) {
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1");
            if (!listTblIndentMaster.isEmpty()) {
                tblIndentMaster = (TblIndentMaster) indentDao.getList("where indentStatus=1 order by indentId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                try {
                indentMaster=(TblIndentMaster)indentDao.getList("where indentStatus=1 and indentNo='" + i + "'").get(0);
                } catch (IndexOutOfBoundsException e) {}
                if(indentMaster.getIndentNo()==null || indentMaster.getIndentNo().length()==0 || indentMaster.getIndentNo().isEmpty()){
                    setIndNo(i);
                }else{
                    i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                    setIndNo(i);
                }
                
            } else {
                i = "00001";
                setIndNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
        } else {
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1");
            if (!listTblIndentMaster.isEmpty()) {
                tblIndentMaster = (TblIndentMaster) indentDao.getList("where indentStatus=1 order by indentId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                try {
                indentMaster=(TblIndentMaster)indentDao.getList("where indentStatus=1 and indentNo='" + i + "'").get(0);
                } catch (IndexOutOfBoundsException e) {}
                if(indentMaster.getIndentNo()==null || indentMaster.getIndentNo().length()==0 || indentMaster.getIndentNo().isEmpty()){
                    setIndNo(i);
                }else{
                    i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                    setIndNo(i);
                }
                //setIndNo(i);
            } else {
                i = "00001";
                setIndNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
            //i = "00001";
            //setIndNo(companyMaster.getCompCode() + utils.YearIn() + i);
        }*/
        listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId = " + compId + ") order by materialDesc");
        listTblUmoMaster = (ArrayList<TblUmoMaster>) umoDao.getList("where umoStatus=1");
        return SUCCESS;
    }
    	



    public void getIndentNo1() throws ParseException {
        String i;
        int year = Integer.parseInt(utils.YearIn()) + 1;
        String start = utils.YearIn() + "-04" + "-01";
        String end = String.valueOf(year) + "-03" + "-31";
        Date currDate = utils.getDateFormat(utils.DateIn());
        Date start1 = utils.getDateFormat(start);
        Date end1 = utils.getDateFormat(end);
        TblCompanyMaster companyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compId = " + compId + "").get(0);
        if (currDate.after(start1) && currDate.before(end1)) {
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1");
            if (!listTblIndentMaster.isEmpty()) {
                tblIndentMaster = (TblIndentMaster) indentDao.getList("where indentStatus=1 order by indentId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                try {
                indentMaster=(TblIndentMaster)indentDao.getList("where indentStatus=1 and indentNo='" + i + "'").get(0);
                } catch (IndexOutOfBoundsException e) {}
                if(indentMaster.getIndentNo()==null || indentMaster.getIndentNo().length()==0 || indentMaster.getIndentNo().isEmpty()){
                    setIndNo(i);
                }else{
                    i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                    setIndNo(i);
                }
                
            } else {
                i = "00001";
                setIndNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
        } else {
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1");
            if (!listTblIndentMaster.isEmpty()) {
                tblIndentMaster = (TblIndentMaster) indentDao.getList("where indentStatus=1 order by indentId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                try {
                indentMaster=(TblIndentMaster)indentDao.getList("where indentStatus=1 and indentNo='" + i + "'").get(0);
                } catch (IndexOutOfBoundsException e) {}
                if(indentMaster.getIndentNo()==null || indentMaster.getIndentNo().length()==0 || indentMaster.getIndentNo().isEmpty()){
                    setIndNo(i);
                }else{
                    i = String.valueOf(Long.parseLong(tblIndentMaster.getIndentNo()) + 1);
                    setIndNo(i);
                }
                //setIndNo(i);
            } else {
                i = "00001";
                setIndNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
            //i = "00001";
            //setIndNo(companyMaster.getCompCode() + utils.YearIn() + i);
        }
    }
    
    public String getAvailableStock() throws IOException {
        tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + Integer.parseInt(comp) + " and tblPlantMaster.plantId=" + Integer.parseInt(plant) + " and tblMaterialMaster.materialId=" + Integer.parseInt(matId) + "").get(0);
        
        System.out.println(tblMapCompanyPlantMaterial);
        mId = tblMapCompanyPlantMaterial.getMapQuantityStores();
        System.out.println(mId);
        setCompId(Integer.parseInt(comp));
        setPlantId(Integer.parseInt(plant));
        JSONObject json = new JSONObject();
        json.put("qty", mId.toString());
        result = json.toString();
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
        if  (qty == null || qty.toString().isEmpty() || qty.length == 0) {
            addFieldError("qty", "Please Enter Quantity Requested.");
            clear = false;
        }
       if  (stock == null || stock.toString().isEmpty() || stock.length == 0) {
            addFieldError("stock", "Please Enter Available Stock.");
            clear = false;
        }
        return clear;
    }

    public String saveIndentRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        if (validation()) {
            getIndentNo1();
            if (((Integer) this.session.get("Supervisor")) != null && ((Integer) this.session.get("Supervisor")) == 4) {
                tblIndentMaster=new TblIndentMaster();
                tblIndentMaster.setIndentNo(indNo);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentEmp(empMaster);
                TblCompanyMaster companyMaster = new TblCompanyMaster();
                companyMaster.setCompId(compId);
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
                indentStatus.setIndentStatusId(3);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                tblIndentMaster.setIndentApprovedbyDate(utils.DateIn2());
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                tblIndentMaster.setTblIndentStatusByIndentProcurementStatus(indentStatus);
                tblIndentMaster.setIndentComments(comments);
                tblIndentMaster.setIndentStatus(1);
                tblIndentMaster.setIndentLmd(utils.getDateFormat(utils.DateIn()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                boolean res = indentDao.save(tblIndentMaster);
                if (res) {
                    for (int i = 0; i < qty.length; i++) {
                        tblIndentDetails = new TblIndentDetails();
                        tblIndentDetails.setTblIndentMaster(tblIndentMaster);
                        TblMaterialMaster materialMaster = new TblMaterialMaster();
                        materialMaster.setMaterialId(md[i]);
                        tblIndentDetails.setTblMaterialMaster(materialMaster);
                        TblUmoMaster umoMaster = new TblUmoMaster();
                        umoMaster.setUmoId(umo[i]);
                        tblIndentDetails.setTblUmoMaster(umoMaster);
                        tblIndentDetails.setIndentDetailsPurpose(pur[i]);
                        tblIndentDetails.setIndentDetailsVendor(ven[i]);
                        tblIndentDetails.setIndentDetailsQty(qty[i]);
                        tblIndentDetails.setIndentDetailsRmQty(qty[i]);
                        tblIndentDetails.setIndentDetailsStockAval(stock[i]);
                        tblIndentDetails.setIndentDetailsStatus(1);
                        tblIndentDetails.setTblEmpMaster(empMaster);
                        tblIndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        indentDetailsDao.save(tblIndentDetails);
                    }
                    in.NewIndentRequestNotification(tblIndentMaster.getIndentId());
                    return SUCCESS;
                } else {
                    getDetails();
                    getRmIndentDetails();
                    return INPUT;
                }
            } else if (((Integer) this.session.get("role")) != null && ((Integer) this.session.get("role")) == 3) {
                tblIndentMaster=new TblIndentMaster();
                tblIndentMaster.setIndentNo(indNo);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentEmp(empMaster);
                TblCompanyMaster companyMaster = new TblCompanyMaster();
                companyMaster.setCompId(compId);
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
                TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);
                tblIndentMaster.setTblIndentStatusByIndentProcurementStatus(indentStatus);
                tblIndentMaster.setIndentComments(comments);
                tblIndentMaster.setIndentStatus(1);
                tblIndentMaster.setIndentLmd(utils.getDateFormat(utils.DateIn()));
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                boolean res = indentDao.save(tblIndentMaster);
                if (res) {
                    for (int i = 0; i < qty.length; i++) {
                        tblIndentDetails = new TblIndentDetails();
                        tblIndentDetails.setTblIndentMaster(tblIndentMaster);
                        TblMaterialMaster materialMaster = new TblMaterialMaster(); 
                        materialMaster.setMaterialId(md[i]);
                        tblIndentDetails.setTblMaterialMaster(materialMaster);
                        TblUmoMaster umoMaster = new TblUmoMaster();
                        umoMaster.setUmoId(umo[i]);
                        tblIndentDetails.setTblUmoMaster(umoMaster);
                        tblIndentDetails.setIndentDetailsPurpose(pur[i]);
                        tblIndentDetails.setIndentDetailsVendor(ven[i]);
                        tblIndentDetails.setIndentDetailsQty(qty[i]);
                        tblIndentDetails.setIndentDetailsStockAval(stock[i]);
                        tblIndentDetails.setIndentDetailsStatus(1);
                        tblIndentDetails.setTblEmpMaster(empMaster);
                        tblIndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        indentDetailsDao.save(tblIndentDetails);
                    }
                    in.NewIndentRequestNotification(tblIndentMaster.getIndentId());
                    return SUCCESS;
                } else {
                    getDetails();
                    getSelfIndentDetails();
                    return INPUT;
                }
            } else {
                getDetails();
                return INPUT;
            }
        } else {
            getDetails();
            return INPUT;
        }
    }

    public String getSelfIndentDetails() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentNo()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            setComments(tblIndentMaster.getIndentComments());
            listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");            
        }
        return SUCCESS;
    }

    public String getRmList() {
        setStatusId(1);
        listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 and tblEmpMasterByIndentCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
        if (!listTblIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String getRmList1() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] indId = request.getParameterValues("indId");
        if (indId.length == 1) {
            if (Integer.parseInt(indId[0]) == 1) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 and tblEmpMasterByIndentCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            } else if (Integer.parseInt(indId[0]) == 2) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=2 and tblEmpMasterByIndentCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (indentStatus=1 and tblIndentStatusByIndentFinalStatus.indentStatusId=2 and tblEmpMasterByIndentCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSub.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=2 and tblEmpMasterByIndentApprovedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            } else if (Integer.parseInt(indId[0]) == 3) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=1 and tblEmpMasterByIndentCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=1 and tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            } else if (Integer.parseInt(indId[0]) == 4) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblEmpMasterByIndentCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            }
        }
        if (!listTblIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public boolean rmValidation() {
        boolean clear = true;
        if (indentStatusId == 2) {
            if (remarks.length() == 0 && remarks != null) {
                addFieldError("remarks", "Please Enter Remarks.");
                clear = false;
            }
        }
        if (indentStatusId == 0) {
            addFieldError("indentStatusId", "Please Select Status.");
            clear = false;
        }
        return clear;
    }

    public String saveRmIndentRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {

        if (rmValidation()) {
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(iId);
            tblIndentMaster.setIndentRemarks(remarks);
            TblIndentStatus idstatus = new TblIndentStatus();
            idstatus.setIndentStatusId(indentStatusId);
            TblEmpMaster empMaster = new TblEmpMaster();
            empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
            tblIndentMaster.setTblEmpMasterByIndentApprovedby(empMaster);
            tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(idstatus);
            tblIndentMaster.setIndentApprovedbyDate(utils.DateIn2());
            tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
            tblIndentMaster.setIndentLmd(utils.getDateFormat(utils.DateIn()));
            boolean res = indentDao.save(tblIndentMaster);
            if (res) {
                listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                int i = 0;
                for (TblIndentDetails IndentDetails : listTblIndentDetails) {
                    //Arrays.sort(detailsCount);
                    // if (IndentDetails.getIndentDetailsId() == detailsCount[i]) {
                    IndentDetails.setIndentDetailsRmQty(rmQty[i]);
                    IndentDetails.setTblEmpMaster(empMaster);
                    IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                    indentDetailsDao.save(IndentDetails);
                    //}
                    i++;
                }
                if (indentStatusId == 2) {
                    in.RejectedIndentRequestNotification(tblIndentMaster.getIndentId());
                } else {
                    in.ApprovedIndentRequestNotification(tblIndentMaster.getIndentId());
                }
                return SUCCESS;
            } else {
                getDetails();
                getRmIndentDetails();
                return INPUT;
            }
        } else {
            getDetails();
            getRmIndentDetails();
            return INPUT;
        }
    }

    public String getRmIndentDetails() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentNo()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            setComments(tblIndentMaster.getIndentComments());
            listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");
        }
        return SUCCESS;
    }

    public String getRmIndentDetailsView() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentNo()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            setComments(tblIndentMaster.getIndentComments());
            if (tblIndentMaster.getIndentRemarks() == null || tblIndentMaster.getIndentRemarks().isEmpty()) {
                setRemarks("");
            } else {
                setRemarks(tblIndentMaster.getIndentRemarks());
            }
            setIndentStatusId(tblIndentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId());
            listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");
        }
        return SUCCESS;
    }

    public String getRejectedIndentDetails() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentNo()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            setComments(tblIndentMaster.getIndentComments());
            if (tblIndentMaster.getIndentRemarks() == null || tblIndentMaster.getIndentRemarks().isEmpty()) {
                setRemarks("");
            } else {
                setRemarks(tblIndentMaster.getIndentRemarks());
            }
            setIndentStatusId(tblIndentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId());
            listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");
        }
        return SUCCESS;
    }

    public String getDeptHeadList() {
        setStatusId(3);
        //listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 and tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
        listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=1 and tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
        if (!listTblIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String getDeptHeadList1() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] indId = request.getParameterValues("indId");
        if (indId.length == 1) {
            if (Integer.parseInt(indId[0]) == 1) {
                //listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 and tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1)");
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 and tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 and tblEmpMasterByIndentCreatedby.empNumber in (select a.tblEmpMasterByReportSub.empNumber from TblMapEmpReporting  as a where a.tblEmpMasterByReportSup.empNumber in (select b.tblEmpMasterByReportSub.empNumber from TblMapEmpReporting as b where b.tblEmpMasterByReportSup.empNumber =" + Integer.parseInt(session.get("empNumber").toString()) + ")))");
            } else if (Integer.parseInt(indId[0]) == 2) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where  (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=2 and tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (indentStatus=1 and tblIndentStatusByIndentFinalStatus.indentStatusId=2 and tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=2 and tblIndentStatusByIndentFinalStatus.indentStatusId=2 and tblEmpMasterByIndentFinalApprovedby.empNumber =" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            } else if (Integer.parseInt(indId[0]) == 3) {
                //listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=1 and tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=1 and tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=1 and tblEmpMasterByIndentApprovedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
                //listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=1");
            } else if (Integer.parseInt(indId[0]) == 4) {
                //listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblEmpMasterByIndentFinalApprovedby.empNumber =" + Integer.parseInt(session.get("empNumber").toString()) + "");
                //listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 ");
            }
        }
        if (!listTblIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String getFinalIndentDetails() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentNo()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            setComments(tblIndentMaster.getIndentComments());
            if (tblIndentMaster.getIndentRemarks() == null || tblIndentMaster.getIndentRemarks().isEmpty()) {
                setRemarks("");
            } else {
                setRemarks(tblIndentMaster.getIndentRemarks());
            }
            listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");
        }
        return SUCCESS;
    }

    public boolean deptHeadValidation() {
        boolean clear = true;
        if (indentStatusId == 2) {
            if (finalRemarks.length() == 0 && finalRemarks != null) {
                addFieldError("finalRemarks", "Please Enter Final Remarks.");
                clear = false;
            }
        }
        if (indentStatusId == 0) {
            addFieldError("indentStatusId", "Please Select Status.");
            clear = false;
        }
        return clear;
    }

    public String saveFinalIndentRequest() throws ParseException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, FileNotFoundException, Exception {
        if (deptHeadValidation()) {
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(iId);
            if (tblIndentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId() == 1) {
                tblIndentMaster.setIndentFinalRemarks(finalRemarks);
                tblIndentMaster.setIndentRemarks(finalRemarks);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentApprovedby(empMaster);
                tblIndentMaster.setTblEmpMasterByIndentFinalApprovedby(empMaster);
                TblIndentStatus idstatus = new TblIndentStatus();
                if (indentStatusId == 2) {
                    idstatus.setIndentStatusId(2);
                } else {
                    idstatus.setIndentStatusId(3);
                }
                tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(idstatus);
                idstatus = new TblIndentStatus();
                idstatus.setIndentStatusId(indentStatusId);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(idstatus);
                tblIndentMaster.setIndentApprovedbyDate(utils.DateIn2());
                tblIndentMaster.setIndentFinalDate(utils.DateIn2());
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                tblIndentMaster.setIndentLmd(utils.getDateFormat(utils.DateIn()));
                tblIndentMaster.setTblIndentStatusByIndentProcurementStatus(idstatus);
                boolean res = indentDao.save(tblIndentMaster);
                if (res) {
                    listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                    int i = 0;
                    for (TblIndentDetails IndentDetails : listTblIndentDetails) {
                        // if (IndentDetails.getIndentDetailsId() == detailsCount[i]) {
                        IndentDetails.setIndentDetailsDeptQty(deptQty[i]);
                        IndentDetails.setIndentDetailsRmQty(deptQty[i]);
                        IndentDetails.setTblEmpMaster(empMaster);
                        IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        indentDetailsDao.save(IndentDetails);
                        //}
                        i++;
                    }
                    if (indentStatusId == 2) {
                        in.FinalRejectedIndentRequestNotification(tblIndentMaster.getIndentId());
                    } else {
                        in.FinalApprovedIndentRequestNotification(tblIndentMaster.getIndentId());
                    }
                    return SUCCESS;
                } else {
                    getDetails();
                    getRmIndentDetails();
                    return INPUT;
                }
            } else {
                tblIndentMaster.setIndentFinalRemarks(finalRemarks);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                tblIndentMaster.setTblEmpMasterByIndentFinalApprovedby(empMaster);
                TblIndentStatus idstatus = new TblIndentStatus();
                idstatus.setIndentStatusId(indentStatusId);
                tblIndentMaster.setTblIndentStatusByIndentFinalStatus(idstatus);
                tblIndentMaster.setIndentFinalDate(utils.DateIn2());
                tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
                tblIndentMaster.setIndentLmd(utils.getDateFormat(utils.DateIn()));
                tblIndentMaster.setTblIndentStatusByIndentProcurementStatus(idstatus);
                boolean res = indentDao.save(tblIndentMaster);
                if (res) {
                    listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                    int i = 0;
                    for (TblIndentDetails IndentDetails : listTblIndentDetails) {
                        //if (IndentDetails.getIndentDetailsId() == detailsCount[i]) {
                        IndentDetails.setIndentDetailsDeptQty(deptQty[i]);
                        IndentDetails.setTblEmpMaster(empMaster);
                        IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                        indentDetailsDao.save(IndentDetails);
                        // }
                        i++;
                    }
                    if (indentStatusId == 2) {
                        in.FinalRejectedIndentRequestNotification(tblIndentMaster.getIndentId());
                    } else {
                        in.FinalApprovedIndentRequestNotification(tblIndentMaster.getIndentId());
                    }
                    return SUCCESS;
                } else {
                    getDetails();
                    getFinalIndentDetails();
                    return INPUT;
                }
            }

        } else {
            getDetails();
            getFinalIndentDetails();
            return INPUT;
        }
    }

    public String getFinalIndentDetailsView() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentNo()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            setComments(tblIndentMaster.getIndentComments());
            if (tblIndentMaster.getIndentRemarks() == null || tblIndentMaster.getIndentRemarks().isEmpty()) {
                setRemarks("");
            } else {
                setRemarks(tblIndentMaster.getIndentRemarks());
            }
            if (tblIndentMaster.getIndentFinalRemarks() == null || tblIndentMaster.getIndentFinalRemarks().isEmpty()) {
                setFinalRemarks("");
            } else {
                setFinalRemarks(tblIndentMaster.getIndentFinalRemarks());
            }
            setIndentStatusId(tblIndentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId());
            listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");
        }
        return SUCCESS;
    }

    public String getRmRejectedIndentDetails() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentNo()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            setComments(tblIndentMaster.getIndentComments());
            if (tblIndentMaster.getIndentRemarks() == null || tblIndentMaster.getIndentRemarks().isEmpty()) {
                setRemarks("");
            } else {
                setRemarks(tblIndentMaster.getIndentRemarks());
            }
            setIndentStatusId(tblIndentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId());
            listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");
        }
        return SUCCESS;
    }

    public String getProcurementList() {
        setStatusId(4);
        listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblIndentStatusByIndentProcurementStatus.indentStatusId=4");
        if (!listTblIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String getProcurementList1() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] indId = request.getParameterValues("indId");
        if (indId.length == 1) {
            if (Integer.parseInt(indId[0]) == 4) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblIndentStatusByIndentProcurementStatus.indentStatusId=4");
            } else if (Integer.parseInt(indId[0]) == 5) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblEmpMasterByIndentProcurementby.empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId=6) and tblIndentStatusByIndentProcurementStatus.indentStatusId=5");
            } else if (Integer.parseInt(indId[0]) == 6) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblEmpMasterByIndentProcurementby.empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId=6) and tblIndentStatusByIndentProcurementStatus.indentStatusId=6");
            } else if (Integer.parseInt(indId[0]) == 7) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblEmpMasterByIndentProcurementby.empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId=6) and tblIndentStatusByIndentProcurementStatus.indentStatusId=7");
            } else if (Integer.parseInt(indId[0]) == 8) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblEmpMasterByIndentProcurementby.empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId=6) and tblIndentStatusByIndentProcurementStatus.indentStatusId=8");
            } else if (Integer.parseInt(indId[0]) == 9) {
                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblEmpMasterByIndentProcurementby.empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId=6) and tblIndentStatusByIndentProcurementStatus.indentStatusId=9");
            }
        }
        if (!listTblIndentMaster.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
    public String getProcurementIndentDetails() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentNo()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            setComments(tblIndentMaster.getIndentComments());
            if (tblIndentMaster.getIndentRemarks() == null || tblIndentMaster.getIndentRemarks().isEmpty()) {
                setRemarks("");
            } else {
                setRemarks(tblIndentMaster.getIndentRemarks());
            }
            if (tblIndentMaster.getIndentFinalRemarks() == null || tblIndentMaster.getIndentFinalRemarks().isEmpty()) {
                setFinalRemarks("");
            } else {
                setFinalRemarks(tblIndentMaster.getIndentFinalRemarks());
            }
            setIndentStatusId(tblIndentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId());
            listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");
        }
        return SUCCESS;
    }

    public boolean procurementValidation() {
        boolean clear = true;
        if (indentStatusId == 8) {
            if (procurementRemarks.length() == 0 && procurementRemarks != null) {
                addFieldError("procurementRemarks", "Please Enter Procurement Remarks.");
                clear = false;
            }
        }
        if (indentStatusId == 7) {
            if (poNumber.length() == 0 && poNumber != null) {
                addFieldError("poNumber", "Please Enter PO Number.");
                clear = false;
            }
            if (deliveryDate.length() == 0 && deliveryDate != null) {
                addFieldError("deliveryDate", "Please Enter Delivery Date.");
                clear = false;
            }
        }
        if (indentStatusId == 9) {
            if (deliveryDate.length() == 0 && deliveryDate != null) {
                addFieldError("deliveryDate", "Please Enter Delivery Date.");
                clear = false;
            }
        }
        if (indentStatusId == 0) {
            addFieldError("indentStatusId", "Please Select Status.");
            clear = false;
        }
        return clear;
    }

    public String saveProcurementIndentRequest() throws ParseException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, FileNotFoundException, Exception {
        if (procurementValidation()) {
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(iId);
            TblEmpMaster empMaster = new TblEmpMaster();
            empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
            tblIndentMaster.setTblEmpMasterByIndentProcurementby(empMaster);
            TblIndentStatus idstatus = new TblIndentStatus();
            idstatus.setIndentStatusId(indentStatusId);
            tblIndentMaster.setTblIndentStatusByIndentProcurementStatus(idstatus);
            if (indentStatusId == 7) {
                tblIndentMaster.setIndentDeliveryDate(deliveryDate);
                tblIndentMaster.setIndentPoNumber(poNumber);
            }
            if (indentStatusId == 9) {
                tblIndentMaster.setIndentDeliveryDate(deliveryDate);
            }
            tblIndentMaster.setTblEmpMasterByIndentLmu(empMaster);
            tblIndentMaster.setIndentLmd(utils.getDateFormat(utils.DateIn()));
            boolean res = indentDao.save(tblIndentMaster);
            if (res) {
                listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
                int i = 0;
                for (TblIndentDetails IndentDetails : listTblIndentDetails) {
                    IndentDetails.setIndentDetailsPricing(pricing[i]);
                    IndentDetails.setTblEmpMaster(empMaster);
                    IndentDetails.setIndentDetailsLmd(utils.getDateFormat(utils.DateIn()));
                    indentDetailsDao.save(IndentDetails);
                    i++;
                }
                tblIndentProcurementLogs = new TblIndentProcurementLogs();
                tblIndentProcurementLogs.setTblIndentMaster(tblIndentMaster);
                tblIndentProcurementLogs.setIndentProcurementDate(utils.DateIn2());
                tblIndentProcurementLogs.setIndentProcurementRemarks(procurementRemarks);
                tblIndentProcurementLogs.setTblIndentStatus(idstatus);
                tblIndentProcurementLogs.setTblEmpMaster(empMaster);
                tblIndentProcurementLogs.setIndentProcurementStatus(1);
                tblIndentProcurementLogs.setIndentProcurementLmd(utils.getDateFormat(utils.DateIn()));
                indentProcurementDao.save(tblIndentProcurementLogs);
                /*if (indentStatusId == 5) {
                    in.ProcurementIndentRequestNotification(tblIndentMaster.getIndentId());
                } else if (indentStatusId == 6) {
                    in.ProcurementIndentRequestNotification(tblIndentMaster.getIndentId());
                } else if (indentStatusId == 7) {
                    in.ProcurementIndentRequestNotification(tblIndentMaster.getIndentId());
                } else if (indentStatusId == 8) {
                    in.ProcurementIndentRequestNotification(tblIndentMaster.getIndentId());
                } else if (indentStatusId == 9) {
                    in.ProcurementIndentRequestNotification(tblIndentMaster.getIndentId());
                }*/
                return SUCCESS;
            } else {
                getDetails();
                getProcurementIndentDetails();
                return INPUT;
            }
        } else {
            getDetails();
            getProcurementIndentDetails();
            return INPUT;
        }
    }

    public String getProcurementIndentDetailsView() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("indId");
        getDetails();
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIndentMaster = (TblIndentMaster) indentDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setFinYear(tblIndentMaster.getIndentYear());
            setDate(tblIndentMaster.getIndentDate());
            setIndNo(String.valueOf(tblIndentMaster.getIndentNo()));
            setEmpId(tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentEmp().getEmpId() + ")");
            setCompId(tblIndentMaster.getTblCompanyMaster().getCompId());
            setDeptId(tblIndentMaster.getTblDepartmentMaster().getDeptId());
            setSecId(tblIndentMaster.getTblSectionMaster().getSecId());
            setPlantId(tblIndentMaster.getTblPlantMaster().getPlantId());
            setComments(tblIndentMaster.getIndentComments());
            if (tblIndentMaster.getIndentDeliveryDate() == null || tblIndentMaster.getIndentDeliveryDate().isEmpty()) {
                setDeliveryDate("");
            } else {
                setDeliveryDate(tblIndentMaster.getIndentDeliveryDate());
            }
            if (tblIndentMaster.getIndentPoNumber() == null || tblIndentMaster.getIndentPoNumber().isEmpty()) {
                setPoNumber("");
            } else {
                setPoNumber(tblIndentMaster.getIndentPoNumber());
            }
            if (tblIndentMaster.getIndentRemarks() == null || tblIndentMaster.getIndentRemarks().isEmpty()) {
                setRemarks("");
            } else {
                setRemarks(tblIndentMaster.getIndentRemarks());
            }
            if (tblIndentMaster.getIndentFinalRemarks() == null || tblIndentMaster.getIndentFinalRemarks().isEmpty()) {
                setFinalRemarks("");
            } else {
                setFinalRemarks(tblIndentMaster.getIndentFinalRemarks());
            }
            setIndentStatusId(tblIndentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId());
            listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + Integer.parseInt(editId[0]) + "");
        }
        return SUCCESS;
    }

    public String IndentReportUser() throws Exception {
        try {
            IndentReportUser report = new IndentReportUser();
            inputStream = report.UserProcurementReport(Integer.parseInt(session.get("empNumber").toString()));
            file = "ProcurementReport(" + utils.DateIn() + ").xls";
            setFile("ProcurementReport(" + utils.DateIn() + ").xls");
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
    public TblIndentMaster getModel() {
        return this.tblIndentMaster;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

}
