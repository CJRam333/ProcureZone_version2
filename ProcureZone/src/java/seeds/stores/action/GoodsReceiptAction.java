/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.stores.action;

import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import pojo.TblEmpMaster;
import pojo.TblGoodsReceipt;
import pojo.TblIndentDetails;
import pojo.TblIndentMaster;
import pojo.TblIndentStatus;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.indent.daoImpl.IndentDaoImpl;
import seeds.indent.daoImpl.IndentDetailsDaoImpl;
import seeds.indent.daoImpl.IndentStatusDaoImpl;
import seeds.masters.daoImpl.EmployeeDaoImpl;
import seeds.stores.daoImpl.GoodsReceiptDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class GoodsReceiptAction extends ActionSupport implements ModelDriven<TblGoodsReceipt>, SessionAware {

    private Map session;
    private final Utils utils = new Utils();
    private TblGoodsReceipt tblGoodsReceipt;
    private ArrayList<TblGoodsReceipt> listTblGoodsReceipt;
    private final GoodsReceiptDaoImpl goodsReceiptDao = DaoFactory.getDao(GoodsReceiptDaoImpl.class);
    private TblIndentMaster tblIndentMaster;
    private List<TblIndentMaster> listTblIndentMaster;
    private final IndentDaoImpl indentDao = DaoFactory.getDao(IndentDaoImpl.class);
    private TblIndentDetails tblIndentDetails;
    private List<TblIndentDetails> listTblIndentDetails;
    private final IndentDetailsDaoImpl indentDetailsDao = DaoFactory.getDao(IndentDetailsDaoImpl.class);
    private List<TblEmpMaster> listTblEmpMaster;
    private List<TblEmpMaster> listTblEmpMaster1;
    private final EmployeeDaoImpl employeeDao = DaoFactory.getDao(EmployeeDaoImpl.class);
    private List<TblIndentStatus> listTblIndentStatus;
    private final IndentStatusDaoImpl indentStatusDao = DaoFactory.getDao(IndentStatusDaoImpl.class);

    private int statusId;
    private int indentId;
    private String indentNo;
    private String empId;
    private String empName;
    private String compName;
    private List md;
    private List md1;
    private List reqqty;
    private List balqty;
    private String remarks;
    private String[] qty;
    private String[] bal;
    private String[] md2;
    private static int iId;
    private static int gId;
    private int indentStatusId;
    private String rmremarks;
    private List openqty;
    private List goodsreceiptqty;
    private List goodsissuedqty;
    private List balinventory;
    private String[] oqty;
    private String[] grqty;
    private String[] giqty;
    private String[] balinv;
    private String storesremarks;

    public TblGoodsReceipt getTblGoodsReceipt() {
        return tblGoodsReceipt;
    }

    public void setTblGoodsReceipt(TblGoodsReceipt tblGoodsReceipt) {
        this.tblGoodsReceipt = tblGoodsReceipt;
    }

    public ArrayList<TblGoodsReceipt> getListTblGoodsReceipt() {
        return listTblGoodsReceipt;
    }

    public void setListTblGoodsReceipt(ArrayList<TblGoodsReceipt> listTblGoodsReceipt) {
        this.listTblGoodsReceipt = listTblGoodsReceipt;
    }

    public TblIndentMaster getTblIndentMaster() {
        return tblIndentMaster;
    }

    public void setTblIndentMaster(TblIndentMaster tblIndentMaster) {
        this.tblIndentMaster = tblIndentMaster;
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

    public List<TblIndentStatus> getListTblIndentStatus() {
        return listTblIndentStatus;
    }

    public void setListTblIndentStatus(List<TblIndentStatus> listTblIndentStatus) {
        this.listTblIndentStatus = listTblIndentStatus;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getIndentId() {
        return indentId;
    }

    public void setIndentId(int indentId) {
        this.indentId = indentId;
    }

    public String getIndentNo() {
        return indentNo;
    }

    public void setIndentNo(String indentNo) {
        this.indentNo = indentNo;
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

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List getMd() {
        return md;
    }

    public void setMd(List md) {
        this.md = md;
    }

    public List getMd1() {
        return md1;
    }

    public void setMd1(List md1) {
        this.md1 = md1;
    }

    public List getReqqty() {
        return reqqty;
    }

    public void setReqqty(List reqqty) {
        this.reqqty = reqqty;
    }

    public List getBalqty() {
        return balqty;
    }

    public void setBalqty(List balqty) {
        this.balqty = balqty;
    }

    public String[] getQty() {
        return qty;
    }

    public void setQty(String[] qty) {
        this.qty = qty;
    }

    public String[] getBal() {
        return bal;
    }

    public void setBal(String[] bal) {
        this.bal = bal;
    }

    public String[] getMd2() {
        return md2;
    }

    public void setMd2(String[] md2) {
        this.md2 = md2;
    }

    public static int getiId() {
        return iId;
    }

    public static int getgId() {
        return gId;
    }

    public static void setgId(int gId) {
        GoodsReceiptAction.gId = gId;
    }

    public static void setiId(int iId) {
        GoodsReceiptAction.iId = iId;
    }

    public int getIndentStatusId() {
        return indentStatusId;
    }

    public void setIndentStatusId(int indentStatusId) {
        this.indentStatusId = indentStatusId;
    }

    public String getRmremarks() {
        return rmremarks;
    }

    public void setRmremarks(String rmremarks) {
        this.rmremarks = rmremarks;
    }

    public List getOpenqty() {
        return openqty;
    }

    public void setOpenqty(List openqty) {
        this.openqty = openqty;
    }

    public List getGoodsreceiptqty() {
        return goodsreceiptqty;
    }

    public void setGoodsreceiptqty(List goodsreceiptqty) {
        this.goodsreceiptqty = goodsreceiptqty;
    }

    public List getGoodsissuedqty() {
        return goodsissuedqty;
    }

    public void setGoodsissuedqty(List goodsissuedqty) {
        this.goodsissuedqty = goodsissuedqty;
    }

    public List getBalinventory() {
        return balinventory;
    }

    public void setBalinventory(List balinventory) {
        this.balinventory = balinventory;
    }

    public String[] getOqty() {
        return oqty;
    }

    public void setOqty(String[] oqty) {
        this.oqty = oqty;
    }

    public String[] getGrqty() {
        return grqty;
    }

    public void setGrqty(String[] grqty) {
        this.grqty = grqty;
    }

    public String[] getGiqty() {
        return giqty;
    }

    public void setGiqty(String[] giqty) {
        this.giqty = giqty;
    }

    public String[] getBalinv() {
        return balinv;
    }

    public void setBalinv(String[] balinv) {
        this.balinv = balinv;
    }

    public String getStoresremarks() {
        return storesremarks;
    }

    public void setStoresremarks(String storesremarks) {
        this.storesremarks = storesremarks;
    }

    public GoodsReceiptAction() throws Exception {
        tblGoodsReceipt = new TblGoodsReceipt();
        listTblGoodsReceipt = new ArrayList<TblGoodsReceipt>();
        tblIndentMaster = new TblIndentMaster();
        listTblIndentMaster = new ArrayList<TblIndentMaster>();
        tblIndentDetails = new TblIndentDetails();
        listTblIndentDetails = new ArrayList<TblIndentDetails>();
        listTblEmpMaster = new ArrayList<TblEmpMaster>();
        listTblEmpMaster1 = new ArrayList<TblEmpMaster>();
        listTblIndentStatus = new ArrayList<TblIndentStatus>();
    }

    public String getList() {
        setStatusId(1);
        getStatusDetails();
        listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId=10 and tblIndentMaster.tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
        return SUCCESS;
    }

    public String getList1() {
        getStatusDetails();
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] receiptId = request.getParameterValues("receiptId");
        if (receiptId.length == 1) {
            if (Integer.parseInt(receiptId[0]) == 1) {
                listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=1 and tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId=10 and tblEmpMasterByGoodsReceiptCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
            } else if (Integer.parseInt(receiptId[0]) == 2) {
                listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=2 and tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId=10 and tblEmpMasterByGoodsReceiptCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
            } else if (Integer.parseInt(receiptId[0]) == 3) {
                listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=3 and tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId=10 and tblEmpMasterByGoodsReceiptCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
            }
        }
        getStatusDetails();
        if (!listTblGoodsReceipt.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public void getDetails() {
        //listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblEmpMasterByIndentCreatedby.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + "");
        listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblEmpMasterByIndentCreatedby.empNumber=10");

    }

    public void getStatusDetails() {
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (1,2,3)");
    }

    public String addIssueNoteRequest() {
        iId = 0;
        getDetails();
        return SUCCESS;
    }

    public String getIndent() throws ParseException {
        tblIndentMaster = (TblIndentMaster) indentDao.getById(indentId);
        setEmpId(tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId());
        setEmpName(tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName());
        setCompName(tblIndentMaster.getTblCompanyMaster().getCompName());
        listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId= " + tblIndentMaster.getIndentId() + "");
        listTblGoodsReceipt=(ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId=10 and tblIndentMaster.indentId= " + tblIndentMaster.getIndentId() + "");
        getDetails();
        return SUCCESS;
    }

    public boolean validation() {
        boolean clear = true;
        if (reqqty.isEmpty()) {
            addFieldError("reqqty", "Please Enter Requested Quantity.");
            clear = false;
        }
        if (balqty.isEmpty()) {
            addFieldError("balqty", "Please Enter Balance Quantity in Stores.");
            clear = false;
        }
        return clear;
    }

    public String saveIssueNoteRequest1() throws ParseException {
        System.out.println("rama " + md.toString() + " rama " + md1.toString());
        return SUCCESS;
    }

    public String saveIssueNoteRequest() throws ParseException {
        boolean res = false;
        if (validation()) {
            qty = new String[reqqty.size()];
            reqqty.toArray(qty);
            bal = new String[balqty.size()];
            balqty.toArray(bal);
            md2 = new String[md.size()];
            md.toArray(md2);
            if (((Integer) this.session.get("Supervisor")) != null && ((Integer) this.session.get("Supervisor")) == 4) {
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                for (int i = 0; i < qty.length; i++) {
                    if (!qty[i].equals("") && !qty[i].isEmpty() && !bal[i].equals("") && !bal[i].isEmpty()) {
                        tblGoodsReceipt = new TblGoodsReceipt();
                        tblIndentMaster.setIndentId(indentId);
                        tblGoodsReceipt.setTblIndentMaster(tblIndentMaster);
                        tblIndentDetails = (TblIndentDetails) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + " and tblMaterialMaster.materialId=" + md2[i] + " ").get(0);
                        tblGoodsReceipt.setTblIndentDetails(tblIndentDetails);
                        tblGoodsReceipt.setTblEmpMasterByGoodsReceiptCreatedby(empMaster);
                        tblGoodsReceipt.setGoodsReceiptCreatedDate(utils.DateIn());
                        tblGoodsReceipt.setGoodsReceiptCreatedRemarks(remarks);
                        tblGoodsReceipt.setTblEmpMasterByGoodsReceiptApprovedby(empMaster);
                        TblIndentStatus indentStatus = new TblIndentStatus();
                        indentStatus.setIndentStatusId(3);
                        tblGoodsReceipt.setTblIndentStatusByGoodsReceiptApprovedStatus(indentStatus);                        
                        tblGoodsReceipt.setGoodsReceiptApprovedbyDate(utils.DateIn());
                        tblGoodsReceipt.setGoodsReceiptApprovedbyRemarks(remarks);
                        tblGoodsReceipt.setGoodsReceiptRequestedQuantity(new BigDecimal(qty[i]));
                        tblGoodsReceipt.setGoodsReceiptBalanceQuantityStores(new BigDecimal(bal[i]));
                        tblGoodsReceipt.setGoodsReceiptStatus(1);
                        tblGoodsReceipt.setGoodsReceiptLmd(utils.DateIn());
                        tblGoodsReceipt.setTblEmpMasterByGoodsReceiptLmu(empMaster);
                        res = goodsReceiptDao.save(tblGoodsReceipt);
                    }
                }
                if (res) {
                    getIndent();
                    return SUCCESS;
                } else {
                    getIndent();
                    return INPUT;
                }
            } else if (((Integer) this.session.get("role")) != null && ((Integer) this.session.get("role")) == 3) {
                TblEmpMaster empMaster = new TblEmpMaster();
                empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                for (int i = 0; i < qty.length; i++) {
                    if (!qty[i].equals("") && !qty[i].isEmpty() && !bal[i].equals("") && !bal[i].isEmpty()) {
                        tblGoodsReceipt = new TblGoodsReceipt();
                        tblIndentMaster.setIndentId(indentId);
                        tblGoodsReceipt.setTblIndentMaster(tblIndentMaster);
                        tblIndentDetails = (TblIndentDetails) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + " and tblMaterialMaster.materialId=" + md2[i] + " ").get(0);
                        tblGoodsReceipt.setTblIndentDetails(tblIndentDetails);
                        tblGoodsReceipt.setTblEmpMasterByGoodsReceiptCreatedby(empMaster);
                        tblGoodsReceipt.setGoodsReceiptCreatedDate(utils.DateIn());
                        tblGoodsReceipt.setGoodsReceiptCreatedRemarks(remarks);
                        TblIndentStatus indentStatus = new TblIndentStatus();
                        indentStatus.setIndentStatusId(1);
                        tblGoodsReceipt.setTblIndentStatusByGoodsReceiptApprovedStatus(indentStatus);
                        tblGoodsReceipt.setGoodsReceiptRequestedQuantity(new BigDecimal(qty[i]));
                        tblGoodsReceipt.setGoodsReceiptBalanceQuantityStores(new BigDecimal(bal[i]));
                        tblGoodsReceipt.setGoodsReceiptStatus(1);
                        tblGoodsReceipt.setGoodsReceiptLmd(utils.DateIn());
                        tblGoodsReceipt.setTblEmpMasterByGoodsReceiptLmu(empMaster);
                        res = goodsReceiptDao.save(tblGoodsReceipt);
                    }
                }
                if (res) {
                    getIndent();
                    return SUCCESS;
                } else {
                    getIndent();
                    return INPUT;
                }
            } else {
                getIndent();
                return INPUT;
            }
        } else {
            getIndent();
            return INPUT;
        }
    }

    public String getSelfIssueNoteDetails() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("receiptId");
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblGoodsReceipt = (TblGoodsReceipt) goodsReceiptDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setIndentNo(tblGoodsReceipt.getTblIndentMaster().getIndentNo());
            setEmpName(tblGoodsReceipt.getTblEmpMasterByGoodsReceiptCreatedby().getEmpName());
            setEmpId(tblGoodsReceipt.getTblEmpMasterByGoodsReceiptCreatedby().getEmpId());
            setCompName(tblGoodsReceipt.getTblIndentMaster().getTblCompanyMaster().getCompName());
            setRemarks(tblGoodsReceipt.getGoodsReceiptCreatedRemarks());
            listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentMaster.indentId=" + tblGoodsReceipt.getTblIndentMaster().getIndentId() + "");
        }
        return SUCCESS;
    }

    public String getRmList() {
        setStatusId(1);
        getStatusDetails();
        listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=1 and tblEmpMasterByGoodsReceiptCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
        if (!listTblGoodsReceipt.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String getRmList1() {
        getStatusDetails();
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] receiptId = request.getParameterValues("receiptId");
        if (receiptId.length == 1) {
            if (Integer.parseInt(receiptId[0]) == 1) {
                listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=1 and tblEmpMasterByGoodsReceiptCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            } else if (Integer.parseInt(receiptId[0]) == 2) {
                listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=2 and tblEmpMasterByGoodsReceiptCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            } else if (Integer.parseInt(receiptId[0]) == 3) {
                listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=3 and tblEmpMasterByGoodsReceiptCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + Integer.parseInt(session.get("empNumber").toString()) + ")");
            }
        }
        if (!listTblGoodsReceipt.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String getRmIssueNoteDetails() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("receiptId");
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblGoodsReceipt = (TblGoodsReceipt) goodsReceiptDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setIndentNo(tblGoodsReceipt.getTblIndentMaster().getIndentNo());
            setEmpName(tblGoodsReceipt.getTblEmpMasterByGoodsReceiptCreatedby().getEmpName());
            setEmpId(tblGoodsReceipt.getTblEmpMasterByGoodsReceiptCreatedby().getEmpId());
            setCompName(tblGoodsReceipt.getTblIndentMaster().getTblCompanyMaster().getCompName());
            setRemarks(tblGoodsReceipt.getGoodsReceiptCreatedRemarks());
            listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentMaster.indentId=" + tblGoodsReceipt.getTblIndentMaster().getIndentId() + "");
            listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (2,3)");
        }
        return SUCCESS;
    }

    public boolean rmvalidation() {
        boolean clear = true;
        if (reqqty.isEmpty()) {
            addFieldError("reqqty", "Please Enter Requested Quantity.");
            clear = false;
        }
        if (balqty.isEmpty()) {
            addFieldError("balqty", "Please Enter Balance Quantity in Stores.");
            clear = false;
        }
        return clear;
    }

    public String saveRmIssueNoteRequest() throws ParseException {
        boolean res = false;
        if (rmvalidation()) {
            qty = new String[reqqty.size()];
            reqqty.toArray(qty);
            bal = new String[balqty.size()];
            balqty.toArray(bal);
            md2 = new String[md.size()];
            md.toArray(md2);
            for (int i = 0; i < qty.length; i++) {
                if (!qty[i].equals("") && !qty[i].isEmpty() && !bal[i].equals("") && !bal[i].isEmpty()) {
                    this.tblGoodsReceipt = (TblGoodsReceipt) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentDetails.tblMaterialMaster.materialId=" + md2[i] + "").get(0);
                    TblEmpMaster empMaster = new TblEmpMaster();
                    empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                    tblGoodsReceipt.setTblEmpMasterByGoodsReceiptApprovedby(empMaster);
                    TblIndentStatus indentStatus = new TblIndentStatus();
                    indentStatus.setIndentStatusId(indentStatusId);
                    tblGoodsReceipt.setTblIndentStatusByGoodsReceiptApprovedStatus(indentStatus);
                    tblGoodsReceipt.setGoodsReceiptApprovedbyDate(utils.DateIn());
                    tblGoodsReceipt.setGoodsReceiptApprovedbyRemarks(rmremarks);
                    tblGoodsReceipt.setGoodsReceiptRequestedQuantity(new BigDecimal(qty[i]));
                    tblGoodsReceipt.setGoodsReceiptBalanceQuantityStores(new BigDecimal(bal[i]));
                    tblGoodsReceipt.setGoodsReceiptStatus(1);
                    tblGoodsReceipt.setGoodsReceiptLmd(utils.DateIn());
                    tblGoodsReceipt.setTblEmpMasterByGoodsReceiptLmu(empMaster);
                    res = goodsReceiptDao.save(tblGoodsReceipt);
                }
            }
            if (res) {
                return SUCCESS;
            } else {
                return INPUT;
            }
        } else {
            return INPUT;
        }
    }

    public String getRmIssueNoteDetailsView() {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("receiptId");
        if (editId == null) {
            editId = new String[]{String.valueOf(iId)};
        }
        if (editId.length == 1) {
            iId = Integer.parseInt(editId[0]);
            this.tblGoodsReceipt = (TblGoodsReceipt) goodsReceiptDao.getById(Integer.parseInt(editId[0]));
            if (iId == 0) {
                iId = Integer.parseInt(editId[0]);
            }
            setIndentNo(tblGoodsReceipt.getTblIndentMaster().getIndentNo());
            setEmpName(tblGoodsReceipt.getTblEmpMasterByGoodsReceiptCreatedby().getEmpName());
            setEmpId(tblGoodsReceipt.getTblEmpMasterByGoodsReceiptCreatedby().getEmpId());
            setCompName(tblGoodsReceipt.getTblIndentMaster().getTblCompanyMaster().getCompName());
            setRemarks(tblGoodsReceipt.getGoodsReceiptCreatedRemarks());
            setRmremarks(tblGoodsReceipt.getGoodsReceiptApprovedbyRemarks());
            listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentMaster.indentId=" + tblGoodsReceipt.getTblIndentMaster().getIndentId() + "");
            listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (2,3)");
            setIndentStatusId(tblGoodsReceipt.getTblIndentStatusByGoodsReceiptApprovedStatus().getIndentStatusId());
        }
        return SUCCESS;
    }

    public String getStoresList() {
        setStatusId(3);
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (3)");
        listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=3");
        if (!listTblGoodsReceipt.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String getStoresList1() {
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (3)");
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] receiptId = request.getParameterValues("receiptId");
        if (receiptId.length == 1) {
            if (Integer.parseInt(receiptId[0]) == 1) {
                listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=1");
            } else if (Integer.parseInt(receiptId[0]) == 2) {
                listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=2");
            } else if (Integer.parseInt(receiptId[0]) == 3) {
                listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId=3");
            }
        }
        if (!listTblGoodsReceipt.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    

    public String getGoodsReceiptList() {
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId=10");
        setStatusId(10);
        listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId=10");
        if (!listTblGoodsReceipt.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }
    
     public String getGoodsReceiptList1() {
        listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId in (10)");
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] receiptId = request.getParameterValues("receiptId");
        if (receiptId.length == 1) {
            if (Integer.parseInt(receiptId[0]) == 10) {
                listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId=10");
            }
        }
        if (!listTblGoodsReceipt.isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    public String addGoodsReceiptRequest() {
        gId = 0;
        listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentProcurementStatus.indentStatusId=7");
        return SUCCESS;
    }

    public String getIndentDetails() throws ParseException {
        tblIndentMaster = (TblIndentMaster) indentDao.getById(indentId);
        setEmpId(tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId());
        setEmpName(tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName());
        setCompName(tblIndentMaster.getTblCompanyMaster().getCompName());
        listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId= " + tblIndentMaster.getIndentId() + "");
        listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentProcurementStatus.indentStatusId=7");
        return SUCCESS;
    }

    public boolean goodsReceiptValidation() {
        boolean clear = true;
        if (openqty.isEmpty()) {
            addFieldError("openqty", "Please Enter Opening Quantity.");
            clear = false;
        }
        if (goodsreceiptqty.isEmpty()) {
            addFieldError("goodsreceiptqty", "Please Enter Goods Receipt Quantity.");
            clear = false;
        }
        if (balinventory.isEmpty()) {
            addFieldError("balinventory", "Please Enter Balance Inventory.");
            clear = false;
        }
        return clear;
    }

    public String saveGoodsReceiptRequest() {
        boolean res = false;
        if (goodsReceiptValidation()) {
            oqty = new String[openqty.size()];
            openqty.toArray(oqty);
            grqty = new String[goodsreceiptqty.size()];
            goodsreceiptqty.toArray(grqty);
            balinv = new String[balinventory.size()];
            balinventory.toArray(balinv);
            md2 = new String[md.size()];
            md.toArray(md2);

            for (int i = 0; i < oqty.length; i++) {                
                if (!oqty[i].equals("") && !oqty[i].isEmpty() && !grqty[i].equals("") && !grqty[i].isEmpty() && !balinv[i].equals("") && !balinv[i].isEmpty()) {
                    tblGoodsReceipt = new TblGoodsReceipt();
                    tblIndentMaster.setIndentId(indentId);
                    tblGoodsReceipt.setTblIndentMaster(tblIndentMaster);
                    tblIndentDetails = (TblIndentDetails) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + " and tblMaterialMaster.materialId=" + md2[i] + " ").get(0);
                    tblGoodsReceipt.setTblIndentDetails(tblIndentDetails);
                    TblEmpMaster empMaster = new TblEmpMaster();
                    empMaster.setEmpNumber(Integer.parseInt(this.session.get("empNumber").toString()));
                    tblGoodsReceipt.setTblEmpMasterByGoodsReceiptStoresby(empMaster);
                    TblIndentStatus indentStatus = new TblIndentStatus();
                    indentStatus.setIndentStatusId(10);
                    tblGoodsReceipt.setTblIndentStatusByGoodsReceiptStoresbyStatus(indentStatus);
                    tblGoodsReceipt.setGoodsReceiptStoresbyDate(utils.DateIn());
                    tblGoodsReceipt.setGoodsReceiptStoresbyRemarks(storesremarks);
                    tblGoodsReceipt.setGoodsReceiptOpeningQuantity(new BigDecimal(oqty[i]));
                    tblGoodsReceipt.setGoodsReceiptQuantity(new BigDecimal(grqty[i]));
                    tblGoodsReceipt.setGoodsReceiptBalanceInventory(new BigDecimal(balinv[i]));
                    tblGoodsReceipt.setGoodsReceiptStatus(1);
                    tblGoodsReceipt.setGoodsReceiptLmd(utils.DateIn());
                    tblGoodsReceipt.setTblEmpMasterByGoodsReceiptLmu(empMaster);
                    res = goodsReceiptDao.save(tblGoodsReceipt);
                }
            }
            if (res) {
                return SUCCESS;
            } else {
                return INPUT;
            }
        } else {
            return INPUT;
        }
    }
    
    public String goodsReceiptDetails() throws ParseException {
        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String[] editId = request.getParameterValues("receiptId");
        if (editId == null) {
            editId = new String[]{String.valueOf(gId)};
        }
        if (editId.length == 1) {
            gId = Integer.parseInt(editId[0]);
            this.tblGoodsReceipt = (TblGoodsReceipt) goodsReceiptDao.getById(Integer.parseInt(editId[0]));
            if (gId == 0) {
                gId = Integer.parseInt(editId[0]);
            }
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentProcurementStatus.indentStatusId=7");
            setIndentId(tblGoodsReceipt.getTblIndentMaster().getIndentId());
            setEmpName(tblGoodsReceipt.getTblIndentMaster().getTblEmpMasterByIndentCreatedby().getEmpName());
            setEmpId(tblGoodsReceipt.getTblIndentMaster().getTblEmpMasterByIndentCreatedby().getEmpId());
            setCompName(tblGoodsReceipt.getTblIndentMaster().getTblCompanyMaster().getCompName());
            setStoresremarks(tblGoodsReceipt.getGoodsReceiptStoresbyRemarks());
            listTblGoodsReceipt = (ArrayList<TblGoodsReceipt>) goodsReceiptDao.getList("where goodsReceiptStatus=1 and tblIndentMaster.indentId=" + tblGoodsReceipt.getTblIndentMaster().getIndentId() + "");
            listTblIndentStatus = indentStatusDao.getList("where indentStatus=1 and indentStatusId=10");
            
        }
        return SUCCESS;
    }

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    public TblGoodsReceipt getModel() {
        return this.tblGoodsReceipt;
    }

    @Override
    public void setSession(Map map
    ) {
        this.session = map;
    }

}
