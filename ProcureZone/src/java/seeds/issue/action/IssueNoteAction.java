/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.issue.action;

import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import pojo.TblCompanyMaster;
import pojo.TblDepartmentMaster;
import pojo.TblSectionMaster;
import pojo.TblEmpMaster;
import pojo.TblIndentStatus;
import pojo.TblIssueNote;
import pojo.TblIssueNoteDetails;
import pojo.TblMapCompanyPlantMaterial;
import pojo.TblMaterialMaster;
import pojo.TblPlantMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.indent.daoImpl.IndentStatusDaoImpl;
import seeds.issue.daoImpl.IssueNoteDaoImpl;
import seeds.issue.daoImpl.IssueNoteDetailsDaoImpl;
import seeds.issue.mailService.IssueNotification;
import seeds.masters.daoImpl.CompPlantMaterialDaoImpl;
import seeds.masters.daoImpl.CompanyDaoImpl;
import seeds.masters.daoImpl.DepartmentDaoImpl;
import seeds.masters.daoImpl.SectionDaoImpl;
import seeds.masters.daoImpl.EmployeeDaoImpl;
import seeds.masters.daoImpl.MaterialDaoImpl;
import seeds.masters.daoImpl.PlantDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class IssueNoteAction extends ActionSupport implements ModelDriven<TblIssueNote>, SessionAware, ServletResponseAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblIssueNote tblIssueNote;
    private TblIssueNote issueNote;
    private ArrayList<TblIssueNote> listTblIssueNote;
    private final IssueNoteDaoImpl issueNoteDao = DaoFactory.getDao(IssueNoteDaoImpl.class);
    private TblIssueNoteDetails tblIssueNoteDetails;
    private ArrayList<TblIssueNoteDetails> listTblIssueNoteDetails;
    private final IssueNoteDetailsDaoImpl issueNoteDetailsDao = DaoFactory.getDao(IssueNoteDetailsDaoImpl.class);
    private TblEmpMaster tblEmpMaster;
    private List<TblEmpMaster> listTblEmpMaster;
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);
    private List<TblIndentStatus> listTblIndentStatus;
    private final IndentStatusDaoImpl indentStatusDao = DaoFactory.getDao(IndentStatusDaoImpl.class);
    private TblMaterialMaster tblMaterialMaster;
    private List<TblMaterialMaster> listTblMaterialMaster;
    private final MaterialDaoImpl materialDao = DaoFactory.getDao(MaterialDaoImpl.class);
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private List<TblDepartmentMaster> listTblDepartmentMaster;
    private final DepartmentDaoImpl departmentDao = DaoFactory.getDao(DepartmentDaoImpl.class);
    private List<TblSectionMaster> listTblSectionMaster;
    private final SectionDaoImpl sectionDao = DaoFactory.getDao(SectionDaoImpl.class);
    private final IssueNotification in = new IssueNotification();
    private List<TblPlantMaster> listTblPlantMaster;
    private final PlantDaoImpl plantDao = DaoFactory.getDao(PlantDaoImpl.class);
    private HttpServletResponse response;
    private TblMapCompanyPlantMaterial tblMapCompanyPlantMaterial;
    private List<TblMapCompanyPlantMaterial> listTblMapCompanyPlantMaterial;
    private final CompPlantMaterialDaoImpl compPlantMaterialDao = DaoFactory.getDao(CompPlantMaterialDaoImpl.class);

    private static int iId;
    private String issueNo;
    private String finYear;
    private int statusId;
    private int compId;
    private int deptId;
    private int secId;
    private int plantId;
    private String empId;
    private String empName;
    private int[] md;
    private BigDecimal[] reqqty;
    private BigDecimal[] balqty;
    private BigDecimal[] issqty;
    private BigDecimal[] recqty;
    private BigDecimal[] openqty;
    private int[] temp;
    private BigDecimal[] temp1;
    private String remarks;
    private String rmRemarks;
    private String storesRemarks;
    private int indentStatusId;
    private String matId;
    private String comp;
    private String plant;
    private static BigDecimal mId;
    public String result;

    public TblIssueNote getTblIssueNote() {
        return tblIssueNote;
    }

    public void setTblIssueNote(TblIssueNote tblIssueNote) {
        this.tblIssueNote = tblIssueNote;
    }

    public TblIssueNote getIssueNote() {
        return issueNote;
    }

    public void setIssueNote(TblIssueNote issueNote) {
        this.issueNote = issueNote;
    }

    public ArrayList<TblIssueNote> getListTblIssueNote() {
        return listTblIssueNote;
    }

    public void setListTblIssueNote(ArrayList<TblIssueNote> listTblIssueNote) {
        this.listTblIssueNote = listTblIssueNote;
    }

    public TblIssueNoteDetails getTblIssueNoteDetails() {
        return tblIssueNoteDetails;
    }

    public void setTblIssueNoteDetails(TblIssueNoteDetails tblIssueNoteDetails) {
        this.tblIssueNoteDetails = tblIssueNoteDetails;
    }

    public ArrayList<TblIssueNoteDetails> getListTblIssueNoteDetails() {
        return listTblIssueNoteDetails;
    }

    public void setListTblIssueNoteDetails(ArrayList<TblIssueNoteDetails> listTblIssueNoteDetails) {
        this.listTblIssueNoteDetails = listTblIssueNoteDetails;
    }

    public TblEmpMaster getTblEmpMaster() {
        return tblEmpMaster;
    }

    public void setTblEmpMaster(TblEmpMaster tblEmpMaster) {
        this.tblEmpMaster = tblEmpMaster;
    }

    public List<TblEmpMaster> getListTblEmpMaster() {
        return listTblEmpMaster;
    }

    public void setListTblEmpMaster(List<TblEmpMaster> listTblEmpMaster) {
        this.listTblEmpMaster = listTblEmpMaster;
    }

    public List<TblIndentStatus> getListTblIndentStatus() {
        return listTblIndentStatus;
    }

    public void setListTblIndentStatus(List<TblIndentStatus> listTblIndentStatus) {
        this.listTblIndentStatus = listTblIndentStatus;
    }

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

    public List<TblCompanyMaster> getListTblCompanyMaster() {
        return listTblCompanyMaster;
    }

    public void setListTblCompanyMaster(List<TblCompanyMaster> listTblCompanyMaster) {
        this.listTblCompanyMaster = listTblCompanyMaster;
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

    public List<TblPlantMaster> getListTblPlantMaster() {
        return listTblPlantMaster;
    }

    public void setListTblPlantMaster(List<TblPlantMaster> listTblPlantMaster) {
        this.listTblPlantMaster = listTblPlantMaster;
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

    public static int getiId() {
        return iId;
    }

    public static void setiId(int iId) {
        IssueNoteAction.iId = iId;
    }

    public String getIssueNo() {
        return issueNo;
    }

    public void setIssueNo(String issueNo) {
        this.issueNo = issueNo;
    }

    public String getFinYear() {
        return finYear;
    }

    public void setFinYear(String finYear) {
        this.finYear = finYear;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
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

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public int[] getMd() {
        return md;
    }

    public void setMd(int[] md) {
        this.md = md;
    }

    public BigDecimal[] getReqqty() {
        return reqqty;
    }

    public void setReqqty(BigDecimal[] reqqty) {
        this.reqqty = reqqty;
    }

    public BigDecimal[] getBalqty() {
        return balqty;
    }

    public void setBalqty(BigDecimal[] balqty) {
        this.balqty = balqty;
    }

    public BigDecimal[] getIssqty() {
        return issqty;
    }

    public void setIssqty(BigDecimal[] issqty) {
        this.issqty = issqty;
    }

    public BigDecimal[] getRecqty() {
        return recqty;
    }

    public void setRecqty(BigDecimal[] recqty) {
        this.recqty = recqty;
    }

    public BigDecimal[] getOpenqty() {
        return openqty;
    }

    public void setOpenqty(BigDecimal[] openqty) {
        this.openqty = openqty;
    }

    public int[] getTemp() {
        return temp;
    }

    public void setTemp(int[] temp) {
        this.temp = temp;
    }

    public BigDecimal[] getTemp1() {
        return temp1;
    }

    public void setTemp1(BigDecimal[] temp1) {
        this.temp1 = temp1;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRmRemarks() {
        return rmRemarks;
    }

    public void setRmRemarks(String rmRemarks) {
        this.rmRemarks = rmRemarks;
    }

    public String getStoresRemarks() {
        return storesRemarks;
    }

    public void setStoresRemarks(String storesRemarks) {
        this.storesRemarks = storesRemarks;
    }

    public int getIndentStatusId() {
        return indentStatusId;
    }

    public void setIndentStatusId(int indentStatusId) {
        this.indentStatusId = indentStatusId;
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
        IssueNoteAction.mId = mId;
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

    public IssueNoteAction() throws Exception {
        issueNote = new TblIssueNote();
        tblIssueNote = new TblIssueNote();
        listTblIssueNote = new ArrayList<TblIssueNote>();
        tblIssueNoteDetails = new TblIssueNoteDetails();
        listTblIssueNoteDetails = new ArrayList<TblIssueNoteDetails>();
        tblEmpMaster = new TblEmpMaster();
        listTblEmpMaster = new ArrayList<TblEmpMaster>();
        listTblIndentStatus = new ArrayList<TblIndentStatus>();
        tblMaterialMaster = new TblMaterialMaster();
        listTblMaterialMaster = new ArrayList<TblMaterialMaster>();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
        listTblDepartmentMaster = new ArrayList<TblDepartmentMaster>();
        listTblSectionMaster = new ArrayList<TblSectionMaster>();
        listTblPlantMaster = new ArrayList<TblPlantMaster>();
        tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
        listTblMapCompanyPlantMaterial = new ArrayList<TblMapCompanyPlantMaterial>();
    }

    public void getStatusDetails() {
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (1,2,3)");
    }

    public String getList() {
        setStatusId(1);
        getStatusDetails();
        listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=1 and tblEmpMasterByIssueNoteCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
        return SUCCESS;
    }

    public String getList1() {
        getStatusDetails();
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] noteId = request.getParameterValues("noteId");
        if (noteId.length == 1 && noteId != null) {
            if (Integer.parseInt(noteId[0]) == 1) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=1 and tblEmpMasterByIssueNoteCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
            } else if (Integer.parseInt(noteId[0]) == 2) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=2 and tblEmpMasterByIssueNoteCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
            } else if (Integer.parseInt(noteId[0]) == 3) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=3 and tblEmpMasterByIssueNoteCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
            }
        }
        getStatusDetails();
        if (!listTblIssueNote.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void issueNoteDetails() {
        //listTblCompanyMaster = companyDao.getList("where compStatus=1");
        listTblCompanyMaster = companyDao.getList("where compStatus=1 and compId in (select tblCompanyMaster.compId from TblMapCompanyEmp where tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(this.session.get("empNumber").toString()) + ")");
        listTblDepartmentMaster = departmentDao.getList("where deptStatus=1");
        listTblSectionMaster = sectionDao.getList("where secStatus=1");
        listTblPlantMaster = (ArrayList<TblPlantMaster>) plantDao.getList("where plantStatus=1");
        listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId in (select tblCompanyMaster.compId from TblMapCompanyEmp where tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(this.session.get("empNumber").toString()) + " )) order by materialDesc) order by materialDesc");
    }

    public String addIssueNoteRequest() throws ParseException {
        iId = 0;
        String i;
        issueNoteDetails();
        //listTblCompanyMaster = companyDao.getList("where compStatus=1 and compId in (select tblCompanyMaster.compId from TblMapCompanyEmp where tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(this.session.get("empNumber").toString()) + ")");
        //listTblCompanyMaster = companyDao.getList("where compStatus=1");
        //listTblDepartmentMaster = departmentDao.getList("where deptStatus=1");
        //listTblSectionMaster = sectionDao.getList("where secStatus=1");
        setCompId(1);
        setDeptId(1);
        setSecId(1);
        setPlantId(1);

        
        int year = Integer.parseInt(utils.YearIn()) + 1;
        String start = utils.YearIn() + "-04" + "-01";
        String end = String.valueOf(year) + "-03" + "-31";
        Date currDate = utils.getDateFormat(utils.DateIn());
        Date start1 = utils.getDateFormat(start);
        Date end1 = utils.getDateFormat(end);
        setFinYear(utils.YearIn() + "-" + year);
        /*TblCompanyMaster companyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compId = " + compId + "").get(0);
        if (currDate.after(start1) && currDate.before(end1)) {
            listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1");
            if (!listTblIssueNote.isEmpty()) {
                tblIssueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 order by issueNoteId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                try {
                    issueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 and issueNoteNo='" + i + "'").get(0);                    
                } catch (IndexOutOfBoundsException e) {}
                if (issueNote.getIssueNoteNo() == null || issueNote.getIssueNoteNo().length() == 0 || issueNote.getIssueNoteNo().isEmpty()) {
                        setIssueNo(i);
                    } else {
                        i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                        setIssueNo(i);
                    }
            } else {
                i = "00001";
                setIssueNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
        } else {
            listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1");
            if (!listTblIssueNote.isEmpty()) {
                tblIssueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 order by issueNoteId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                //setIssueNo(i);
                try {
                issueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 and issueNoteNo='" + i + "'").get(0);
                } catch (IndexOutOfBoundsException e) {}
                if (issueNote.getIssueNoteNo() == null || issueNote.getIssueNoteNo().length() == 0 || issueNote.getIssueNoteNo().isEmpty()) {
                    setIssueNo(i);
                } else {
                    i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                    setIssueNo(i);
                }
            } else {
                i = "00001";
                setIssueNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
        }*/
        tblEmpMaster = (TblEmpMaster) employeeDao.getById(Integer.parseInt(this.session.get("empNumber").toString()));
        setEmpId(tblEmpMaster.getEmpId());
        setEmpName(tblEmpMaster.getEmpName());
        //listTblPlantMaster = (ArrayList<TblPlantMaster>) plantDao.getList("where plantStatus=1");
        //listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId = 1) order by materialDesc");
        listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId = " + compId + ") order by materialDesc");
        return SUCCESS;
    }

    public String getIssueNoteNo() throws ParseException {
        String i;
        issueNoteDetails();
        listTblCompanyMaster = companyDao.getList("where compStatus=1 and compId in (select tblCompanyMaster.compId from TblMapCompanyEmp where tblEmpMasterByMapEmp.empNumber=" + Integer.parseInt(this.session.get("empNumber").toString()) + ")");
        //listTblCompanyMaster = companyDao.getList("where compStatus=1");
        //setCompId(1);
//        setDeptId(1);
//        setPlantId(1);
//        setSecId(1);
       /* int year = Integer.parseInt(utils.YearIn()) + 1;
        String start = utils.YearIn() + "-04" + "-01";
        String end = String.valueOf(year) + "-03" + "-31";
        Date currDate = utils.getDateFormat(utils.DateIn());
        Date start1 = utils.getDateFormat(start);
        Date end1 = utils.getDateFormat(end);
        TblCompanyMaster companyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compId = " + compId + "").get(0);
        if (currDate.after(start1) && currDate.before(end1)) {
            listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1");
            if (!listTblIssueNote.isEmpty()) {
                tblIssueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 order by issueNoteId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                //setIssueNo(i);
                try {
                issueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 and issueNoteNo='" + i + "'").get(0);
                } catch (IndexOutOfBoundsException e) {}
                if (issueNote.getIssueNoteNo() == null || issueNote.getIssueNoteNo().length() == 0 || issueNote.getIssueNoteNo().isEmpty()) {
                    setIssueNo(i);
                } else {
                    i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                    setIssueNo(i);
                }
            } else {
                i = "00001";
                setIssueNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
        } else {
            listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1");
            if (!listTblIssueNote.isEmpty()) {
                tblIssueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 order by issueNoteId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                //setIssueNo(i);
                try {
                issueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 and issueNoteNo='" + i + "'").get(0);
                } catch (IndexOutOfBoundsException e) {}
                if (issueNote.getIssueNoteNo() == null || issueNote.getIssueNoteNo().length() == 0 || issueNote.getIssueNoteNo().isEmpty()) {
                    setIssueNo(i);
                } else {
                    i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                    setIssueNo(i);
                }
            } else {
                i = "00001";
                setIssueNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
        }*/
        System.out.println("company id is : "+compId);
        tblEmpMaster = (TblEmpMaster) employeeDao.getById(Integer.parseInt(this.session.get("empNumber").toString()));
        setEmpId(tblEmpMaster.getEmpId());
        setEmpName(tblEmpMaster.getEmpName());
        //listTblPlantMaster = (ArrayList<TblPlantMaster>) plantDao.getList("where plantStatus=1");
        listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId = " + compId + ") order by materialDesc");
        return SUCCESS;
    }
    
    public void getIssueNoteNo1() throws ParseException {
        String i;
        //issueNoteDetails();
        TblCompanyMaster companyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compId = " + compId + "").get(0);
        int year = Integer.parseInt(utils.YearIn()) + 1;
        String start = utils.YearIn() + "-04" + "-01";
        String end = String.valueOf(year) + "-03" + "-31";
        Date currDate = utils.getDateFormat(utils.DateIn());
        Date start1 = utils.getDateFormat(start);
        Date end1 = utils.getDateFormat(end);
        setFinYear(utils.YearIn() + "-" + year);
        if (currDate.after(start1) && currDate.before(end1)) {
            listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1");
            if (!listTblIssueNote.isEmpty()) {
                tblIssueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 order by issueNoteId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                try {
                    issueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 and issueNoteNo='" + i + "'").get(0);                    
                } catch (IndexOutOfBoundsException e) {}
                if (issueNote.getIssueNoteNo() == null || issueNote.getIssueNoteNo().length() == 0 || issueNote.getIssueNoteNo().isEmpty()) {
                        setIssueNo(i);
                    } else {
                        i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                        setIssueNo(i);
                    }
            } else {
                i = "00001";
                setIssueNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
        } else {
            listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1");
            if (!listTblIssueNote.isEmpty()) {
                tblIssueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 order by issueNoteId DESC LIMIT 1").get(0);
                i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                //setIssueNo(i);
                try {
                issueNote = (TblIssueNote) issueNoteDao.getList("where issueNoteStatus=1 and issueNoteNo='" + i + "'").get(0);
                } catch (IndexOutOfBoundsException e) {}
                if (issueNote.getIssueNoteNo() == null || issueNote.getIssueNoteNo().length() == 0 || issueNote.getIssueNoteNo().isEmpty()) {
                    setIssueNo(i);
                } else {
                    i = String.valueOf(Long.parseLong(tblIssueNote.getIssueNoteNo()) + 1);
                    setIssueNo(i);
                }
            } else {
                i = "00001";
                setIssueNo(companyMaster.getCompCode() + utils.YearIn() + i);
            }
        }
    }

    public String getQuantity() throws IOException {
        tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + Integer.parseInt(comp) + " and tblPlantMaster.plantId=" + Integer.parseInt(plant) + " and tblMaterialMaster.materialId=" + Integer.parseInt(matId) + "").get(0);
        mId = tblMapCompanyPlantMaterial.getMapQuantityStores();
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
        if (reqqty == null || reqqty.length == 0) {
            addFieldError("reqqty", "Please Enter Requested Quantity.");
            clear = false;
        }
        if (balqty == null || balqty.length == 0) {
            addFieldError("balqty", "Please Enter Quantity in Stores.");
            clear = false;
        }
        return clear;
    }

    public String saveIssueNoteRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        boolean res;
        if (validation()) {
            getIssueNoteNo1();
            if (((Integer) this.session.get("Supervisor")) != null && ((Integer) this.session.get("Supervisor")) == 4) {  
                tblIssueNote=new TblIssueNote();
                tblIssueNote.setIssueNoteNo(issueNo);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                TblCompanyMaster companyMaster = new TblCompanyMaster();
                companyMaster.setCompId(compId);
                tblIssueNote.setTblCompanyMaster(companyMaster);
                TblDepartmentMaster deptMaster = new TblDepartmentMaster();
                deptMaster.setDeptId(deptId);
                tblIssueNote.setTblDepartmentMaster(deptMaster);
                TblSectionMaster secMaster = new TblSectionMaster();
                secMaster.setSecId(secId);
                tblIssueNote.setTblSectionMaster(secMaster);
                TblPlantMaster plantMaster = new TblPlantMaster();
                plantMaster.setPlantId(plantId);
                tblIssueNote.setTblPlantMaster(plantMaster);
                tblIssueNote.setTblEmpMasterByIssueNoteCreatedby(empMaster);
                tblIssueNote.setTblEmpMasterByIssueNoteApprovedby(empMaster);
                TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(3);
                tblIssueNote.setTblIndentStatusByIssueNoteApprovedStatus(indentStatus);
                tblIssueNote.setIssueNoteCreatedDate(utils.DateIn2());
                tblIssueNote.setIssueNoteApprovedbyDate(utils.DateIn2());
                tblIssueNote.setIssueNoteCreatedRemarks(remarks);
                tblIssueNote.setIssueNoteApprovedbyRemarks(remarks);
                indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);
                tblIssueNote.setTblIndentStatusByIssueNoteStoresbyStatus(indentStatus);
                tblIssueNote.setIssueNoteStatus(1);
                tblIssueNote.setIssueNoteLmd(utils.DateIn());
                tblIssueNote.setTblEmpMasterByIssueNoteLmu(empMaster);
                tblIssueNote.setIssueNoteYear(finYear);
                res = issueNoteDao.save(tblIssueNote);
                if (res) {
                    for (int i = 0; i < reqqty.length; i++) {
                        tblIssueNoteDetails = new TblIssueNoteDetails();
                        tblIssueNoteDetails.setTblIssueNote(tblIssueNote);
                        TblMaterialMaster materialMaster = new TblMaterialMaster();
                        materialMaster.setMaterialId(md[i]);
                        tblIssueNoteDetails.setTblMaterialMaster(materialMaster);
                        tblIssueNoteDetails.setIssueNoteDetailsRequestedQuantity(reqqty[i]);
                        tblIssueNoteDetails.setIssueNoteDetailsQuantityStores(balqty[i]);
                        tblIssueNoteDetails.setIssueNoteDetailsStatus(1);
                        tblIssueNoteDetails.setIssueNoteDetailsLmd(utils.DateIn());
                        tblIssueNoteDetails.setTblEmpMaster(empMaster);
                        issueNoteDetailsDao.save(tblIssueNoteDetails);
                    }
                   
                    in.NewIssueNoteRequestNotification(tblIssueNote.getIssueNoteId(), Integer.parseInt(this.session.get("Supervisor").toString()));
                    return SUCCESS;
                } else {
                    issueNoteDetails();
                    return INPUT;
                }
            } else if (((Integer) this.session.get("role")) != null && ((Integer) this.session.get("role")) == 3) { 
                tblIssueNote=new TblIssueNote();
                tblIssueNote.setIssueNoteNo(issueNo);
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                TblCompanyMaster companyMaster = new TblCompanyMaster();
                companyMaster.setCompId(compId);
                tblIssueNote.setTblCompanyMaster(companyMaster);
                TblDepartmentMaster deptMaster = new TblDepartmentMaster();
                deptMaster.setDeptId(deptId);
                tblIssueNote.setTblDepartmentMaster(deptMaster);
                TblSectionMaster secMaster = new TblSectionMaster();
                secMaster.setSecId(secId);
                tblIssueNote.setTblSectionMaster(secMaster);
                TblPlantMaster plantMaster = new TblPlantMaster();
                plantMaster.setPlantId(plantId);
                tblIssueNote.setTblPlantMaster(plantMaster);
                tblIssueNote.setTblEmpMasterByIssueNoteCreatedby(empMaster);
                tblIssueNote.setIssueNoteCreatedDate(utils.DateIn());
                tblIssueNote.setIssueNoteCreatedRemarks(remarks);
                TblIndentStatus indentStatus = new TblIndentStatus();
                indentStatus.setIndentStatusId(1);
                tblIssueNote.setTblIndentStatusByIssueNoteApprovedStatus(indentStatus);
                tblIssueNote.setTblIndentStatusByIssueNoteStoresbyStatus(indentStatus);
                tblIssueNote.setIssueNoteStatus(1);
                tblIssueNote.setIssueNoteLmd(utils.DateIn());
                tblIssueNote.setTblEmpMasterByIssueNoteLmu(empMaster);
                tblIssueNote.setIssueNoteYear(finYear);
                res = issueNoteDao.save(tblIssueNote);
                if (res) {
                    for (int i = 0; i < reqqty.length; i++) {
                        tblIssueNoteDetails = new TblIssueNoteDetails();
                        tblIssueNoteDetails.setTblIssueNote(tblIssueNote);
                        TblMaterialMaster materialMaster = new TblMaterialMaster();
                        materialMaster.setMaterialId(md[i]);
                        tblIssueNoteDetails.setTblMaterialMaster(materialMaster);
                        tblIssueNoteDetails.setIssueNoteDetailsRequestedQuantity(reqqty[i]);
                        tblIssueNoteDetails.setIssueNoteDetailsQuantityStores(balqty[i]);
                        tblIssueNoteDetails.setIssueNoteDetailsStatus(1);
                        tblIssueNoteDetails.setIssueNoteDetailsLmd(utils.DateIn());
                        tblIssueNoteDetails.setTblEmpMaster(empMaster);
                        issueNoteDetailsDao.save(tblIssueNoteDetails);
                    }                   
                    in.NewIssueNoteRequestNotification(tblIssueNote.getIssueNoteId(), Integer.parseInt(this.session.get("role").toString()));
                    return SUCCESS;
                } else {
                    issueNoteDetails();
                    return INPUT;
                }
            } else {
                issueNoteDetails();
                return INPUT;
            }
        } else {
            issueNoteDetails();
            return INPUT;
        }
        
    }

    public String getIssueNoteDetails() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("noteId");
        issueNoteDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIssueNote = (TblIssueNote) issueNoteDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            //listTblCompanyMaster = (ArrayList<TblCompanyMaster>) companyDao.getList("where compStatus=1");
            //listTblPlantMaster = (ArrayList<TblPlantMaster>) plantDao.getList("where plantStatus=1");
            setFinYear(tblIssueNote.getIssueNoteYear());
            setPlantId(tblIssueNote.getTblPlantMaster().getPlantId());
            setIssueNo(tblIssueNote.getIssueNoteNo());
            setEmpName(tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpName());
            setEmpId(tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId());
            setCompId(tblIssueNote.getTblCompanyMaster().getCompId());
            setDeptId(tblIssueNote.getTblDepartmentMaster().getDeptId());
            setSecId(tblIssueNote.getTblSectionMaster().getSecId());
            setRemarks(tblIssueNote.getIssueNoteCreatedRemarks());
            listTblIssueNoteDetails = (ArrayList<TblIssueNoteDetails>) issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + tblIssueNote.getIssueNoteId() + "");
        }
        return SUCCESS;
    }

    public String getRmList() {
        setStatusId(1);
        getStatusDetails();
        listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=1 and tblEmpMasterByIssueNoteCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
        if (!listTblIssueNote.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String getRmList1() {
        getStatusDetails();
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] noteId = request.getParameterValues("noteId");
        if (noteId.length == 1) {
            if (Integer.parseInt(noteId[0]) == 1) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=1 and tblEmpMasterByIssueNoteCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            } else if (Integer.parseInt(noteId[0]) == 2) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where (issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=2 and tblEmpMasterByIssueNoteCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=2 and tblEmpMasterByIssueNoteApprovedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            } else if (Integer.parseInt(noteId[0]) == 3) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where (issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=3 and tblEmpMasterByIssueNoteCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")) or (issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=3 and tblEmpMasterByIssueNoteApprovedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            }
        }
        if (!listTblIssueNote.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String getRmIssueNoteDetails() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("noteId");
        issueNoteDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIssueNote = (TblIssueNote) issueNoteDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            //listTblCompanyMaster = companyDao.getList("where compStatus=1");
            //listTblPlantMaster = (ArrayList<TblPlantMaster>) plantDao.getList("where plantStatus=1");
            setIssueNo(tblIssueNote.getIssueNoteNo());
            setEmpName(tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpName());
            setEmpId(tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId());
            setCompId(tblIssueNote.getTblCompanyMaster().getCompId());
            setFinYear(tblIssueNote.getIssueNoteYear());
            setSecId(tblIssueNote.getTblSectionMaster().getSecId());
            setDeptId(tblIssueNote.getTblDepartmentMaster().getDeptId());
            setPlantId(tblIssueNote.getTblPlantMaster().getPlantId());
            setRemarks(tblIssueNote.getIssueNoteCreatedRemarks());
            listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId = " + compId + ") order by materialDesc");
            listTblIssueNoteDetails = (ArrayList<TblIssueNoteDetails>) issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + tblIssueNote.getIssueNoteId() + "");
            listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (2,3)");
        }
        return SUCCESS;
    }

    public boolean rmValidation() {
        boolean clear = true;
        if (reqqty == null || reqqty.length == 0) {
            addFieldError("reqqty", "Please Enter Requested Quantity.");
            clear = false;
        }
        if (balqty == null || balqty.length == 0) {
            addFieldError("balqty", "Please Enter Quantity in Stores.");
            clear = false;
        }
        if (indentStatusId == 0) {
            addFieldError("indentStatusId", "Please Select Status.");
            clear = false;
        }
        return clear;
    }

    public String saveRmIssueNoteRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        if (rmValidation()) {
            this.tblIssueNote = (TblIssueNote) issueNoteDao.getById(iId);
            TblIndentStatus idstatus = new TblIndentStatus();
            idstatus.setIndentStatusId(indentStatusId);
            TblEmpMaster empMaster = new TblEmpMaster();
            empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
            tblIssueNote.setTblEmpMasterByIssueNoteApprovedby(empMaster);
            tblIssueNote.setTblIndentStatusByIssueNoteApprovedStatus(idstatus);
            tblIssueNote.setIssueNoteApprovedbyRemarks(rmRemarks);
            tblIssueNote.setIssueNoteApprovedbyDate(utils.DateIn2());
            tblIssueNote.setTblEmpMasterByIssueNoteLmu(empMaster);
            tblIssueNote.setIssueNoteLmd(utils.DateIn());
            boolean res = issueNoteDao.save(tblIssueNote);
            if (res) {
                listTblIssueNoteDetails = (ArrayList<TblIssueNoteDetails>) issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + tblIssueNote.getIssueNoteId() + "");
                int i = 0;
                for (TblIssueNoteDetails IssueNoteDetails : listTblIssueNoteDetails) {
                    IssueNoteDetails.setIssueNoteDetailsRequestedQuantity(reqqty[i]);
                    IssueNoteDetails.setTblEmpMaster(empMaster);
                    IssueNoteDetails.setIssueNoteDetailsLmd(utils.DateIn());
                    issueNoteDetailsDao.save(IssueNoteDetails);
                    i++;
                }
                getStatusDetails();
                if (indentStatusId == 2) {
                    in.RejectedIssueNoteRequestNotification(tblIssueNote.getIssueNoteId());
                } else {
                    in.ApprovedIssueNoteRequestNotification(tblIssueNote.getIssueNoteId());
                }
                return SUCCESS;
            } else {
                getStatusDetails();
                getRmIssueNoteDetails();
                return INPUT;
            }
        } else {
            getStatusDetails();
            getRmIssueNoteDetails();
            return INPUT;
        }
    }

    public String issueNoteDetailsRmView() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("noteId");
        issueNoteDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIssueNote = (TblIssueNote) issueNoteDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            //listTblCompanyMaster = (ArrayList<TblCompanyMaster>) companyDao.getList("where compStatus=1");
            //listTblPlantMaster = (ArrayList<TblPlantMaster>) plantDao.getList("where plantStatus=1");
            setIssueNo(tblIssueNote.getIssueNoteNo());
            setEmpName(tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpName());
            setEmpId(tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId());
            setCompId(tblIssueNote.getTblCompanyMaster().getCompId());
            setFinYear(tblIssueNote.getIssueNoteYear());
            setSecId(tblIssueNote.getTblSectionMaster().getSecId());
            setDeptId(tblIssueNote.getTblDepartmentMaster().getDeptId());
            setPlantId(tblIssueNote.getTblPlantMaster().getPlantId());
            setRemarks(tblIssueNote.getIssueNoteCreatedRemarks());
            setRmRemarks(tblIssueNote.getIssueNoteApprovedbyRemarks());
            listTblIssueNoteDetails = (ArrayList<TblIssueNoteDetails>) issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + tblIssueNote.getIssueNoteId() + "");
            listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (2,3)");
            setIndentStatusId(tblIssueNote.getTblIndentStatusByIssueNoteApprovedStatus().getIndentStatusId());
        }
        return SUCCESS;
    }

    public void getStoresStatusDetails() {
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (3,2,11)");
    }

    public String getStoresList() {
        getStoresStatusDetails();
        setStatusId(3);
        listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=3 and tblIndentStatusByIssueNoteStoresbyStatus.indentStatusId=1");
        if (!listTblIssueNote.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String getStoresList1() {
        getStoresStatusDetails();
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] noteId = request.getParameterValues("noteId");
        if (noteId.length == 1) {
            if (Integer.parseInt(noteId[0]) == 3) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=3 and tblIndentStatusByIssueNoteStoresbyStatus.indentStatusId=1");
            } else if (Integer.parseInt(noteId[0]) == 2) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=3 and tblIndentStatusByIssueNoteStoresbyStatus.indentStatusId =2");
            } else if (Integer.parseInt(noteId[0]) == 11) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=3 and tblIndentStatusByIssueNoteStoresbyStatus.indentStatusId=11 and tblEmpMasterByIssueNoteStoresby.empNumber in (select tblEmpMasterByEmpNumber.empNumber from TblMapEmpRoles where tblRolesMaster.roleId=6)");
            }
        }
        if (!listTblIssueNote.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
    /*public String getStoresList1() {
        getStoresStatusDetails();
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] noteId = request.getParameterValues("noteId");
        if (noteId.length == 1) {
            if (Integer.parseInt(noteId[0]) == 3) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=3 and tblIndentStatusByIssueNoteStoresbyStatus.indentStatusId=1");
            } else if (Integer.parseInt(noteId[0]) == 2) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=3 and tblIndentStatusByIssueNoteStoresbyStatus.indentStatusId =2");
            } else if (Integer.parseInt(noteId[0]) == 11) {
                listTblIssueNote = (ArrayList<TblIssueNote>) issueNoteDao.getList("where issueNoteStatus=1 and tblIndentStatusByIssueNoteApprovedStatus.indentStatusId=3 and tblIndentStatusByIssueNoteStoresbyStatus.indentStatusId=11 and tblEmpMasterByIssueNoteStoresby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
            }
        }
        if (!listTblIssueNote.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }*/

    public String getStoresIssueNoteDetails() throws ParseException {
        try {
            HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
            String[] editId = request.getParameterValues("noteId");
            issueNoteDetails();
            if (editId == null) {
                editId = new String[]{String.valueOf(iId)};
            }
            if (editId.length == 1) {
                iId = Integer.parseInt(editId[0]);
                this.tblIssueNote = (TblIssueNote) issueNoteDao.getById(Integer.parseInt(editId[0]));
                if (iId == 0) {
                    iId = Integer.parseInt(editId[0]);
                }
                //listTblCompanyMaster = companyDao.getList("where compStatus=1");
                //listTblPlantMaster = (ArrayList<TblPlantMaster>) plantDao.getList("where plantStatus=1");
                setIssueNo(tblIssueNote.getIssueNoteNo());
                setEmpName(tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpName());
                setEmpId(tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId());
                setCompId(tblIssueNote.getTblCompanyMaster().getCompId());
                setFinYear(tblIssueNote.getIssueNoteYear());
                setSecId(tblIssueNote.getTblSectionMaster().getSecId());
                setDeptId(tblIssueNote.getTblDepartmentMaster().getDeptId());
                setPlantId(tblIssueNote.getTblPlantMaster().getPlantId());
                setRemarks(tblIssueNote.getIssueNoteCreatedRemarks());
                setRmRemarks(tblIssueNote.getIssueNoteApprovedbyRemarks());
                listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialId in (select tblMaterialMaster.materialId from TblMapCompanyPlantMaterial where tblCompanyMaster.compId = " + compId + " and tblPlantMaster.plantId=" + plantId + ") order by materialDesc");
                listTblIssueNoteDetails = (ArrayList<TblIssueNoteDetails>) issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + tblIssueNote.getIssueNoteId() + "");
                for (TblIssueNoteDetails issnotDet : listTblIssueNoteDetails) {
                    tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getList(" where mapStatus=1 and tblMaterialMaster.materialId=" + issnotDet.getTblMaterialMaster().getMaterialId() + "").get(0);
                    issnotDet.setIssueNoteDetailsQuantityStores(tblMapCompanyPlantMaterial.getMapQuantityStores());
                    compPlantMaterialDao.save(issnotDet);

                }
                listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (2,11)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    public boolean storesValidation() {
        boolean clear = true;
        if (openqty == null || openqty.length == 0) {
            addFieldError("openqty", "Please Enter Opening Quantity.");
            clear = false;
        }
        if (reqqty == null || reqqty.length == 0) {
            addFieldError("reqqty", "Please Enter Requested Quantity.");
            clear = false;
        }
        /*if (recqty == null || recqty.length == 0) {
         addFieldError("recqty", "Please Enter Receipt Quantity.");
         clear = false;
         }*/
        if (issqty == null || issqty.length == 0) {
            addFieldError("issqty", "Please Enter Issued Quantity.");
            clear = false;
        }
        if (balqty == null || balqty.length == 0) {
            addFieldError("balqty", "Please Enter Quantity in Stores.");
            clear = false;
        }
        if (indentStatusId == 0) {
            addFieldError("indentStatusId", "Please Select Status.");
            clear = false;
        }

        return clear;
    }

    public String saveStoresIssueNoteRequest() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        if (storesValidation()) {
            this.tblIssueNote = (TblIssueNote) issueNoteDao.getById(iId);
            TblIndentStatus idstatus = new TblIndentStatus();
            idstatus.setIndentStatusId(indentStatusId);
            TblEmpMaster empMaster = new TblEmpMaster();
            empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
            tblIssueNote.setTblEmpMasterByIssueNoteStoresby(empMaster);
            tblIssueNote.setTblIndentStatusByIssueNoteStoresbyStatus(idstatus);
            tblIssueNote.setIssueNoteStoresbyRemarks(storesRemarks);
            tblIssueNote.setIssueNoteStoresbyDate(utils.DateIn2());
            tblIssueNote.setTblEmpMasterByIssueNoteLmu(empMaster);
            tblIssueNote.setIssueNoteLmd(utils.DateIn());
            boolean res = issueNoteDao.save(tblIssueNote);
            if (res) {
                if (indentStatusId != 2) {
                    listTblIssueNoteDetails = (ArrayList<TblIssueNoteDetails>) issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + tblIssueNote.getIssueNoteId() + "");
                    int i = 0;
                    for (TblIssueNoteDetails IssueNoteDetails : listTblIssueNoteDetails) {
                        listTblMapCompanyPlantMaterial = (ArrayList<TblMapCompanyPlantMaterial>) compPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + tblIssueNote.getTblCompanyMaster().getCompId() + " and tblMaterialMaster.materialId=" + md[i] + " and tblPlantMaster.plantId=" + tblIssueNote.getTblPlantMaster().getPlantId() + "");
                        for (TblMapCompanyPlantMaterial CompanyPlantMaterial : listTblMapCompanyPlantMaterial) {
                            balqty[i] = CompanyPlantMaterial.getMapQuantityStores().subtract(issqty[i]);
                            if (balqty[i].compareTo(BigDecimal.ZERO) <= 0) {
                                balqty[i] = new BigDecimal(BigInteger.ZERO);
                            }
                            CompanyPlantMaterial.setMapQuantityStores(balqty[i]);
                            compPlantMaterialDao.save(CompanyPlantMaterial);
                        }
                        //TblMapCompanyPlantMaterial CompPltMat =(TblMapCompanyPlantMaterial)compPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + tblIssueNote.getTblCompanyMaster().getCompId() + " and tblMaterialMaster.materialId=" + md[i] + " and tblPlantMaster.plantId=" + tblIssueNote.getTblPlantMaster().getPlantId() + "");
                        IssueNoteDetails.setIssueNoteDetailsOpeningQuantity(openqty[i]);
                        //IssueNoteDetails.setIssueNoteDetailsQuantity(recqty[i]);
                        IssueNoteDetails.setIssueNoteDetailsIssuedQuantity(issqty[i]);
                        IssueNoteDetails.setIssueNoteDetailsQuantityStores(balqty[i]);
                        IssueNoteDetails.setIssueNoteDetailsBalanceInventory(balqty[i]);
                        IssueNoteDetails.setTblEmpMaster(empMaster);
                        IssueNoteDetails.setIssueNoteDetailsLmd(utils.DateIn());
                        issueNoteDetailsDao.save(IssueNoteDetails);

                        i++;
                    }
                }
                getStoresStatusDetails();
                getStoresIssueNoteDetails();
                if (indentStatusId == 2) {
                    in.StoresRejectedIssueNoteRequestNotification(tblIssueNote.getIssueNoteId());
                } else {
                    in.StoresIssueNoteRequestNotification(tblIssueNote.getIssueNoteId());
                }
                return SUCCESS;
            } else {
                getStoresIssueNoteDetails();
                getStoresStatusDetails();
                return INPUT;
            }
        } else {
            getStoresIssueNoteDetails();
            getStoresStatusDetails();
            return INPUT;
        }

    }

    public String issueNoteDetailsStoresView() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("noteId");
        issueNoteDetails();
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblIssueNote = (TblIssueNote) issueNoteDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            //listTblCompanyMaster = (ArrayList<TblCompanyMaster>) companyDao.getList("where compStatus=1");
            //listTblPlantMaster = (ArrayList<TblPlantMaster>) plantDao.getList("where plantStatus=1");
            setIssueNo(tblIssueNote.getIssueNoteNo());
            setEmpName(tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpName());
            setEmpId(tblIssueNote.getTblEmpMasterByIssueNoteCreatedby().getEmpId());
            setCompId(tblIssueNote.getTblCompanyMaster().getCompId());
            setFinYear(tblIssueNote.getIssueNoteYear());
            setSecId(tblIssueNote.getTblSectionMaster().getSecId());
            setDeptId(tblIssueNote.getTblDepartmentMaster().getDeptId());
            setPlantId(tblIssueNote.getTblPlantMaster().getPlantId());
            setRemarks(tblIssueNote.getIssueNoteCreatedRemarks());
            setRmRemarks(tblIssueNote.getIssueNoteApprovedbyRemarks());
            setStoresRemarks(tblIssueNote.getIssueNoteStoresbyRemarks());
            listTblIssueNoteDetails = (ArrayList<TblIssueNoteDetails>) issueNoteDetailsDao.getList("where issueNoteDetailsStatus=1 and tblIssueNote.issueNoteId=" + tblIssueNote.getIssueNoteId() + "");
            listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (3,2,11)");
            setIndentStatusId(tblIssueNote.getTblIndentStatusByIssueNoteStoresbyStatus().getIndentStatusId());
        }
        return SUCCESS;
    }

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    public TblIssueNote getModel() {
        return tblIssueNote;
    }

    @Override
    public void setSession(Map map) {
        this.session = map;
    }

    @Override
    public void setServletResponse(HttpServletResponse hsr) {
        this.response = hsr;
    }

}
