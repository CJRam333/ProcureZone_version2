/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this tindentlate file, choose Tools | Tindentlates
 * and open the tindentlate in the editor.
 */
package seeds.indent.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;
import pojo.TblIndentMaster;
import pojo.TblIndentProcurementLogs;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.indent.daoImpl.IndentDaoImpl;
import seeds.indent.daoImpl.IndentProcurementDaoImpl;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;

/**
 *
 * @author ramesh.avv
 */
public class IndentDetailsReport {

    private final Utils utils = new Utils();
    private TblIndentMaster tblIndentMaster;
    private List<TblIndentMaster> listTblIndentMaster;
    private final IndentDaoImpl indentDao = DaoFactory.getDao(IndentDaoImpl.class);
    private TblIndentProcurementLogs tblIndentProcurementLogs;
    private List<TblIndentProcurementLogs> listTblIndentProcurementLogs;
    private final IndentProcurementDaoImpl indentProcurementDao = DaoFactory.getDao(IndentProcurementDaoImpl.class);

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

    public IndentDetailsReport() throws Exception {
        tblIndentMaster = new TblIndentMaster();
        listTblIndentMaster = new ArrayList<TblIndentMaster>();
        tblIndentProcurementLogs = new TblIndentProcurementLogs();
        listTblIndentProcurementLogs = new ArrayList<TblIndentProcurementLogs>();
    }

    public ByteArrayInputStream DetailsReport(int empNumber, String fromDate, String toDate, int empNumber1, int materialId, int indentStatusId, int deptEmpNumber, int admin,int dept) throws FileNotFoundException, IOException {
        ByteArrayInputStream bis = null;
        String filename = "ProcurementReport(" + utils.DateIn() + ").xls";
        String query = "", query1, query2 = "";
        List listIndent = new ArrayList();
        try {
            if (empNumber != 0) {
                query = " a.indent_createdby=" + empNumber + " and a.indent_dept=" + dept + "";
            }
            if (fromDate.length() != 0 && toDate.length() != 0) {
                if (!query.isEmpty()) {
                    if (admin == 2) {
                        query = query + " and DATE(a.indent_date) >='" + fromDate + "' and DATE(a.indent_date) <='" + toDate + "'";
                    } else {
                        query = query + " and (DATE(a.indent_date) >='" + fromDate + "' and DATE(a.indent_date) <='" + toDate + "' and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (DATE(a.indent_date) >='" + fromDate + "' and DATE(a.indent_date) <='" + toDate + "' and a.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup =" + deptEmpNumber + "))";
                    }
                } else {
                    if (admin == 2) {
                        query = query + " DATE(a.indent_date) >='" + fromDate + "' and DATE(a.indent_date) <='" + toDate + "' and a.indent_dept=" + dept + "";
                    } else {
                        query = query + " (DATE(a.indent_date) >='" + fromDate + "' and DATE(a.indent_date) <='" + toDate + "' and a.indent_dept=" + dept + " and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (DATE(a.indent_date) >='" + fromDate + "' and DATE(a.indent_date) <='" + toDate + "' and a.indent_dept=" + dept + " and a.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup =" + deptEmpNumber + "))";
                    }
                }
            } else if (fromDate.length() != 0 && toDate.length() == 0) {
                if (!query.isEmpty()) {
                    if (admin == 2) {
                        query = query + " and DATE(a.indent_date) ='" + fromDate + "'";
                    } else {
                        query = query + " and (DATE(a.indent_date) ='" + fromDate + "' and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (DATE(a.indent_date) >='" + fromDate + "' and a.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup =" + deptEmpNumber + "))";
                    }
                } else {
                    if (admin == 2) {
                        query = query + " DATE(a.indent_date) ='" + fromDate + "' and a.indent_dept=" + dept + " ";
                    } else {
                        query = query + " (DATE(a.indent_date) ='" + fromDate + "' and a.indent_dept=" + dept + " and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (DATE(a.indent_date) >='" + fromDate + "' and a.indent_dept=" + dept + " and a.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup =" + deptEmpNumber + "))";
                    }
                }
            } else if (fromDate.length() == 0 && toDate.length() != 0) {
                if (!query.isEmpty()) {
                    if (admin == 2) {
                        query = query + " and DATE(a.indent_date) ='" + toDate + "'";
                    } else {
                        query = query + " and (DATE(a.indent_date) ='" + toDate + "' and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (DATE(a.indent_date) <='" + toDate + "' and a.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup =" + deptEmpNumber + "))";
                    }
                } else {
                    if (admin == 2) {
                        query = query + " DATE(a.indent_date) ='" + toDate + "' and a.indent_dept=" + dept + "";
                    } else {
                        query = query + " (DATE(a.indent_date) ='" + toDate + "' and a.indent_dept=" + dept + " and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (DATE(a.indent_date) <='" + toDate + "' and a.indent_dept=" + dept + " and a.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup =" + deptEmpNumber + "))";
                    }
                }
            }
            if (empNumber1 != 0) {
                if (!query.isEmpty()) {
                    query = query + " and a.indent_approvedby=" + empNumber1 + " and a.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting as g where g.report_sup =" + deptEmpNumber + ")";
                } else {
                    query = query + " a.indent_approvedby=" + empNumber1 + " and a.indent_dept=" + dept + " and a.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting as g where g.report_sup =" + deptEmpNumber + ")";
                }
            }
            if (materialId != 0) {
                if (!query.isEmpty()) {
                    query = query + " and (b.indent_details_material=" + materialId + " and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (b.indent_details_material=" + materialId + " and a.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting as g where g.report_sup =" + deptEmpNumber + "))";
                } else {
                    query = query + " (b.indent_details_material=" + materialId + " and a.indent_dept=" + dept + " and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (b.indent_details_material=" + materialId + " and a.indent_dept=" + dept + " and a.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting as g where g.report_sup =" + deptEmpNumber + "))";
                }
            }

            if (indentStatusId != 0) {
                if (indentStatusId == 1 || indentStatusId == 2 || indentStatusId == 3) {
                    if (!query.isEmpty()) {
                        if (indentStatusId == 2) {
                            query = query + " and ((a.indent_approved_status=" + indentStatusId + " and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + ")))or (a.indent_approved_status=" + indentStatusId + " anda.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting as g where g.report_sup =" + deptEmpNumber + "))) or ((a.indent_final_status=" + indentStatusId + " and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (a.indent_approved_status=" + indentStatusId + " anda.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting as g where g.report_sup =" + deptEmpNumber + ")))";
                        } else {
                            query = query + " and (a.indent_approved_status=" + indentStatusId + " and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (a.indent_approved_status=" + indentStatusId + " anda.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting as g where g.report_sup =" + deptEmpNumber + "))";
                        }
                    } else {
                        query = query + " a.indent_approved_status=" + indentStatusId + " and a.indent_dept=" + dept + " and a.indent_createdby in (select g.report_sub from tbl_map_emp_reporting  as g where g.report_sup in (select h.report_sub from tbl_map_emp_reporting as h where h.report_sup =" + deptEmpNumber + "))) or (a.indent_approved_status=" + indentStatusId + " anda.indent_approvedby in (select g.report_sub from tbl_map_emp_reporting as g where g.report_sup =" + deptEmpNumber + "))";
                    }
                }
                if (indentStatusId == 4) {
                    if (!query.isEmpty()) {
                        query = query + " and a.indent_final_status=" + indentStatusId + " and indent_final_approvedby= " + deptEmpNumber + "";
                    } else {
                        query = query + " a.indent_final_status=" + indentStatusId + " and a.indent_dept=" + dept + " and indent_final_approvedby= " + deptEmpNumber + "";
                    }
                }
                if (indentStatusId == 5 || indentStatusId == 6 || indentStatusId == 7 || indentStatusId == 8 || indentStatusId == 9) {
                    if (!query.isEmpty()) {
                        query = query + " and a.indent_procurement_status=" + indentStatusId + " and indent_final_approvedby= " + deptEmpNumber + "";
                    } else {
                        query = query + " a.indent_procurement_status=" + indentStatusId + " and a.indent_dept=" + dept + " and indent_final_approvedby= " + deptEmpNumber + "";
                    }
                }
            }

            if (!query.isEmpty()) {
                query2 = " where " + query;
            } else {
                query2 = "";
            }            
            query1 = "select a.indent_no,a.indent_date,c.emp_name,d.emp_name,e.material_name,f.umo_name,b.indent_details_dept_qty,b.indent_details_stock_aval,b.indent_details_vendor,b.indent_details_pricing,a.indent_po_number,a.indent_delivery_date,a.indent_approved_status,a.indent_final_status,a.indent_procurement_status from tbl_indent_master a "
                    + "left join tbl_indent_details b on a.indent_id=b.indent_id "
                    + "left join tbl_emp_master c on a.indent_createdby=c.emp_number "
                    + "left join tbl_emp_master d on a.indent_approvedby=d.emp_number "
                    + "left join tbl_material_master e on b.indent_details_material=e.material_id "
                    + "left join tbl_umo_master f on b.indent_details_umo=f.umo_id " + query2 + "";
            System.out.println("rama " + query1);
            listIndent = indentDao.getSqlQuery1(query1);
            HSSFWorkbook hwb = new HSSFWorkbook();
            int i = 1;
            HSSFSheet sheet = hwb.createSheet(filename);
            sheet.autoSizeColumn((short) 1);
            HSSFCellStyle MainHead = hwb.createCellStyle();
            HSSFCellStyle MainHead1 = hwb.createCellStyle();
            HSSFCellStyle MainHead2 = hwb.createCellStyle();
            HSSFCellStyle TableHdr = hwb.createCellStyle();
            HSSFCellStyle TableData = hwb.createCellStyle();
            HSSFCellStyle TableDataLt = hwb.createCellStyle();
            HSSFCellStyle TableData1 = hwb.createCellStyle();
            HSSFCellStyle TableData2 = hwb.createCellStyle();
            HSSFDataFormat format = hwb.createDataFormat();
            HSSFCellStyle TableData3 = hwb.createCellStyle();
            TableData3.setDataFormat(format.getFormat("#######.00"));
            TableHdr.setAlignment((short) 1);
            TableData.setAlignment((short) 1);
            TableDataLt.setAlignment((short) 1);
            TableData1.setAlignment((short) 1);
            TableData2.setAlignment((short) 1);
            TableData3.setAlignment((short) 1);

            MainHead.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
            HSSFFont font = hwb.createFont();
            font.setFontName("calibri");
            font.setFontHeightInPoints((short) 13);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font.setColor(HSSFColor.BLACK.index);
            MainHead.setFont(font);
            HSSFFont font5 = hwb.createFont();
            font5.setFontName("calibri");

            font5.setFontHeightInPoints((short) 10);
            font5.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font5.setColor(HSSFColor.BLACK.index);
            MainHead1.setFont(font5);
            HSSFFont font6 = hwb.createFont();
            font6.setFontName("calibri");

            font6.setFontHeightInPoints((short) 8);
            font6.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font6.setColor(HSSFColor.BLACK.index);
            MainHead2.setFont(font6);
            MainHead2.setBorderBottom((short) 1);
            MainHead2.setBorderTop((short) 1);
            MainHead2.setBorderLeft((short) 1);
            MainHead2.setBorderRight((short) 1);
            MainHead2.setBorderRight((short) 1);

            HSSFFont font1 = hwb.createFont();
            font1.setFontName("calibri");
            font1.setFontHeightInPoints((short) 10);
            font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font1.setColor(HSSFColor.BLACK.index);
            TableHdr.setFont(font1);
            TableHdr.setBorderBottom((short) 1);
            TableHdr.setBorderTop((short) 1);
            TableHdr.setBorderLeft((short) 1);
            TableHdr.setBorderRight((short) 1);
            TableHdr.setBorderRight((short) 1);

            HSSFFont font2 = hwb.createFont();
            font2.setFontName("calibri");
            font2.setFontHeightInPoints((short) 8);
            font2.setColor(HSSFColor.BLACK.index);
            TableData.setFont(font2);
            TableData.setBorderBottom((short) 1);
            TableData.setBorderTop((short) 1);
            TableData.setBorderLeft((short) 1);
            TableData.setBorderRight((short) 1);

            HSSFFont fontLt = hwb.createFont();
            fontLt.setFontName("calibri");
            fontLt.setFontHeightInPoints((short) 8);
            fontLt.setColor(HSSFColor.RED.index);
            TableDataLt.setFont(fontLt);
            TableDataLt.setBorderBottom((short) 1);
            TableDataLt.setBorderTop((short) 1);
            TableDataLt.setBorderLeft((short) 1);
            TableDataLt.setBorderRight((short) 1);
            HSSFFont font3 = hwb.createFont();
            font3.setFontName("calibri");
            font3.setFontHeightInPoints((short) 10);
            font3.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font3.setColor(HSSFColor.BLACK.index);
            TableData1.setFont(font3);
            TableData1.setBorderBottom((short) 1);
            TableData1.setBorderTop((short) 1);
            TableData1.setBorderLeft((short) 1);
            TableData1.setBorderRight((short) 1);

            HSSFFont fontlt1 = hwb.createFont();
            fontlt1.setFontName("calibri");
            fontlt1.setFontHeightInPoints((short) 10);
            fontlt1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            fontlt1.setColor(HSSFColor.BLACK.index);
            TableData3.setFont(font3);
            TableData3.setBorderBottom((short) 1);
            TableData3.setBorderTop((short) 1);
            TableData3.setBorderLeft((short) 1);
            TableData3.setBorderRight((short) 1);

            HSSFRow row = sheet.createRow((short) 1);
            sheet.setDefaultColumnWidth((short) 3);
            sheet.setDisplayGridlines(false);
            sheet.setVerticallyCenter(true);
            sheet.setDefaultRowHeight((short) 250);
            HSSFCell cell = row.createCell((short) 0);

            if (i == 1) {
                Iterator it = listIndent.iterator();
                while (it.hasNext()) {
                    Object obj = it.next();
                    sheet.addMergedRegion(new Region(1, (short) 0, 1, (short) 30));
                    sheet.addMergedRegion(new Region(2, (short) 0, 2, (short) 30));
                    sheet.addMergedRegion(new Region(3, (short) 0, 3, (short) 30));
                    cell.setCellValue("Indent Request Details");
                    cell.setCellStyle(MainHead);
                    row = sheet.createRow((short) 2);
                    cell = row.createCell((short) 0);
                    cell.setCellValue("Report Genarated Date : " + utils.DateIn());
                    cell.setCellStyle(MainHead);
                    row = sheet.createRow((short) 3);
                    cell = row.createCell((short) 0);
                    cell.setCellStyle(MainHead);
                }
                row = sheet.createRow((short) 4);

                HSSFCell cell1 = row.createCell((short) 0);
                HSSFCell cell2 = row.createCell((short) 1);
                HSSFCell cell3 = row.createCell((short) 2);
                HSSFCell cell4 = row.createCell((short) 3);
                HSSFCell cell5 = row.createCell((short) 4);
                HSSFCell cell6 = row.createCell((short) 5);
                HSSFCell cell7 = row.createCell((short) 6);
                HSSFCell cell8 = row.createCell((short) 7);
                HSSFCell cell9 = row.createCell((short) 8);
                HSSFCell cell10 = row.createCell((short) 9);
                HSSFCell cell11 = row.createCell((short) 10);
                HSSFCell cell12 = row.createCell((short) 11);
                HSSFCell cell13 = row.createCell((short) 12);
                HSSFCell cell14 = row.createCell((short) 13);
                HSSFCell cell15 = row.createCell((short) 14);

                cell1.setCellValue("Indent No.");
                cell2.setCellValue("Indent Date");
                cell3.setCellValue("Indenter");
                cell4.setCellValue("Reporting Manager");
                cell5.setCellValue("Material");
                cell6.setCellValue("Uom");
                cell7.setCellValue("Final Quantity Requested");
                cell8.setCellValue("Available Stock");
                cell9.setCellValue("Vendor");
                cell10.setCellValue("Pricing");
                cell11.setCellValue("Indent Status");
                cell12.setCellValue("PO Number");
                cell13.setCellValue("PO Date");
                cell14.setCellValue("Expected Delivery Date");
                cell15.setCellValue("Procurement Remarks");

                cell1.setCellStyle(TableHdr);
                cell2.setCellStyle(TableHdr);
                cell3.setCellStyle(TableHdr);
                cell4.setCellStyle(TableHdr);
                cell5.setCellStyle(TableHdr);
                cell6.setCellStyle(TableHdr);
                cell7.setCellStyle(TableHdr);
                cell8.setCellStyle(TableHdr);
                cell9.setCellStyle(TableHdr);
                cell10.setCellStyle(TableHdr);
                cell11.setCellStyle(TableHdr);
                cell12.setCellStyle(TableHdr);
                cell13.setCellStyle(TableHdr);
                cell14.setCellStyle(TableHdr);
                cell15.setCellStyle(TableHdr);

                i = 5;

                Iterator it1 = listIndent.iterator();
                while (it1.hasNext()) {
                    Object[] indent = (Object[]) it1.next();

                    row = sheet.createRow((short) i);
                    cell1 = row.createCell((short) 0);
                    cell2 = row.createCell((short) 1);
                    cell3 = row.createCell((short) 2);
                    cell4 = row.createCell((short) 3);
                    cell5 = row.createCell((short) 4);
                    cell6 = row.createCell((short) 5);
                    cell7 = row.createCell((short) 6);
                    cell8 = row.createCell((short) 7);
                    cell9 = row.createCell((short) 8);
                    cell10 = row.createCell((short) 9);
                    cell11 = row.createCell((short) 10);
                    cell12 = row.createCell((short) 11);
                    cell13 = row.createCell((short) 12);
                    cell14 = row.createCell((short) 13);
                    cell15 = row.createCell((short) 14);

                    try {
                        try {
                            if (indent[0] == null || indent[0].toString().length() == 0) {
                                cell1.setCellValue("-");
                            } else {
                                cell1.setCellValue(indent[0].toString());
                            }
                        } catch (Exception e) {
                            cell1.setCellValue("-");
                        }
                        try {
                            if (indent[1] == null || indent[1].toString().length() == 0) {
                                cell2.setCellValue("-");
                            } else {
                                cell2.setCellValue(utils.getDateFormat3(indent[1].toString()));
                            }
                        } catch (Exception e) {
                            cell2.setCellValue("-");
                        }
                        try {
                            if (indent[2] == null || indent[2].toString().length() == 0) {
                                cell3.setCellValue("-");
                            } else {
                                cell3.setCellValue(indent[2].toString());
                            }
                        } catch (Exception e) {
                            cell3.setCellValue("-");
                        }
                        try {
                            if (indent[3] == null || indent[3].toString().length() == 0) {
                                cell4.setCellValue("-");
                            } else {
                                cell4.setCellValue(indent[3].toString());
                            }
                        } catch (Exception e) {
                            cell4.setCellValue("-");
                        }
                        try {
                            if (indent[4] == null || indent[4].toString().length() == 0) {
                                cell5.setCellValue("-");
                            } else {
                                cell5.setCellValue(indent[4].toString());
                            }
                        } catch (Exception e) {
                            cell5.setCellValue("-");
                        }
                        try {
                            if (indent[5] == null || indent[5].toString().length() == 0) {
                                cell6.setCellValue("-");
                            } else {
                                cell6.setCellValue(indent[5].toString());
                            }
                        } catch (Exception e) {
                            cell6.setCellValue("-");
                        }
                        try {
                            if (indent[6] == null || indent[6].toString().length() == 0) {
                                cell7.setCellValue("-");
                            } else {
                                cell7.setCellValue(new Double(indent[6].toString()));

                            }
                        } catch (Exception e) {
                            cell7.setCellValue("-");
                        }
                        try {
                            if (indent[7] == null || indent[7].toString().length() == 0) {
                                cell8.setCellValue("-");
                            } else {
                                cell8.setCellValue(new Double(indent[7].toString()));
                            }
                        } catch (Exception e) {
                            cell8.setCellValue("-");
                        }
                        try {
                            if (indent[8] == null || indent[8].toString().length() == 0) {
                                cell9.setCellValue("-");
                            } else {
                                cell9.setCellValue(indent[8].toString());
                            }
                        } catch (Exception e) {
                            cell9.setCellValue("-");
                        }
                        try {
                            if (indent[9] == null || indent[9].toString().length() == 0) {
                                cell10.setCellValue("-");
                            } else {
                                cell10.setCellValue(new Double(indent[9].toString()));
                            }
                        } catch (Exception e) {
                            cell10.setCellValue("-");
                        }
                        try {
                            if (indent[12].toString().equals(String.valueOf(1)) && indent[13].toString().equals(String.valueOf(1)) && indent[14].toString().equals(String.valueOf(1))) {
                                cell11.setCellValue("Pending");
                            } else if (indent[12].toString().equals(String.valueOf(2)) && indent[13].toString().equals(String.valueOf(1)) && indent[14].toString().equals(String.valueOf(1))) {
                                cell11.setCellValue("RM Rejected");
                            } else if (indent[12].toString().equals(String.valueOf(3)) && indent[13].toString().equals(String.valueOf(1)) && indent[14].toString().equals(String.valueOf(1))) {
                                cell11.setCellValue("RM Approved");
                            } else if (indent[12].toString().equals(String.valueOf(3)) && indent[13].toString().equals(String.valueOf(2)) && indent[14].toString().equals(String.valueOf(2))) {
                                cell11.setCellValue("Dept. Head Rejected");
                            } else if (indent[12].toString().equals(String.valueOf(2)) && indent[13].toString().equals(String.valueOf(2)) && indent[14].toString().equals(String.valueOf(2))) {
                                cell11.setCellValue("Dept. Head Rejected");
                            } else if (indent[12].toString().equals(String.valueOf(3)) && indent[13].toString().equals(String.valueOf(4)) && indent[14].toString().equals(String.valueOf(4))) {
                                cell11.setCellValue("Dept. Head Approved");
                            } else if (indent[12].toString().equals(String.valueOf(3)) && indent[13].toString().equals(String.valueOf(4)) && indent[14].toString().equals(String.valueOf(5))) {
                                cell11.setCellValue("Quotations Collected");
                            } else if (indent[12].toString().equals(String.valueOf(3)) && indent[13].toString().equals(String.valueOf(4)) && indent[14].toString().equals(String.valueOf(6))) {
                                cell11.setCellValue("Negotiation Done");
                            } else if (indent[12].toString().equals(String.valueOf(3)) && indent[13].toString().equals(String.valueOf(4)) && indent[14].toString().equals(String.valueOf(7))) {
                                cell11.setCellValue("PO Released");
                            } else if (indent[12].toString().equals(String.valueOf(3)) && indent[13].toString().equals(String.valueOf(4)) && indent[14].toString().equals(String.valueOf(8))) {
                                cell11.setCellValue("Hold");
                            } else if (indent[12].toString().equals(String.valueOf(3)) && indent[13].toString().equals(String.valueOf(4)) && indent[14].toString().equals(String.valueOf(9))) {
                                cell11.setCellValue("Cash Buy");
                            }

                        } catch (Exception e) {
                            cell11.setCellValue("-");
                        }
                        try {
                            if (indent[11] == null || indent[11].toString().length() == 0) {
                                cell12.setCellValue("-");
                            } else {
                                cell12.setCellValue(indent[10].toString());
                            }
                        } catch (Exception e) {
                            cell12.setCellValue("-");
                        }
                        try {
                            if (indent[12] == null || indent[12].toString().length() == 0) {
                                cell13.setCellValue("-");
                            } else {
                                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentNo='" + indent[0].toString() + "' and indentStatus=1");
                                if (!listTblIndentMaster.isEmpty()) {
                                    for (TblIndentMaster indentMaster : listTblIndentMaster) {
                                        listTblIndentProcurementLogs = (ArrayList<TblIndentProcurementLogs>) indentProcurementDao.getList("where tblIndentMaster.indentId=" + indentMaster.getIndentId() + " and tblIndentStatus.indentStatusId=" + indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId() + " and indentProcurementStatus=1");
                                        if (!listTblIndentProcurementLogs.isEmpty()) {
                                            for (TblIndentProcurementLogs indentProcurementLogs : listTblIndentProcurementLogs) {
                                                cell13.setCellValue(utils.getDateFormat3(indentProcurementLogs.getIndentProcurementDate()));
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            cell13.setCellValue("-");
                        }
                        try {
                            if (indent[13] == null || indent[13].toString().length() == 0) {
                                cell14.setCellValue("-");
                            } else {
                                cell14.setCellValue(utils.getDateFormat3(indent[11].toString()));
                            }
                        } catch (Exception e) {
                            cell14.setCellValue("-");
                        }
                        try {
                            if (indent[14] == null || indent[14].toString().length() == 0) {
                                cell15.setCellValue("-");
                            } else {
                                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentNo='" + indent[0].toString() + "' and indentStatus=1");
                                if (!listTblIndentMaster.isEmpty()) {
                                    for (TblIndentMaster indentMaster : listTblIndentMaster) {
                                        listTblIndentProcurementLogs = (ArrayList<TblIndentProcurementLogs>) indentProcurementDao.getList("where tblIndentMaster.indentId=" + indentMaster.getIndentId() + " and tblIndentStatus.indentStatusId=" + indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId() + " and indentProcurementStatus=1");
                                        if (!listTblIndentProcurementLogs.isEmpty()) {
                                            for (TblIndentProcurementLogs indentProcurementLogs : listTblIndentProcurementLogs) {
                                                cell15.setCellValue(indentProcurementLogs.getIndentProcurementRemarks());
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            cell15.setCellValue("-");
                        }
                    } catch (NullPointerException e) {
                    }

                    cell1.setCellStyle(TableData1);
                    cell2.setCellStyle(TableData1);
                    cell3.setCellStyle(TableData1);
                    cell4.setCellStyle(TableData1);
                    cell5.setCellStyle(TableData1);
                    cell6.setCellStyle(TableData1);
                    cell7.setCellStyle(TableData3);
                    cell8.setCellStyle(TableData3);
                    cell9.setCellStyle(TableData1);
                    cell10.setCellStyle(TableData3);
                    cell11.setCellStyle(TableData1);
                    cell12.setCellStyle(TableData1);
                    cell13.setCellStyle(TableData1);
                    cell14.setCellStyle(TableData1);
                    cell15.setCellStyle(TableData1);

                    i++;
                }
            }
            sheet.autoSizeColumn((short) 0);
            sheet.autoSizeColumn((short) 1);
            sheet.autoSizeColumn((short) 2);
            sheet.autoSizeColumn((short) 3);
            sheet.autoSizeColumn((short) 4);
            sheet.autoSizeColumn((short) 5);
            sheet.autoSizeColumn((short) 6);
            sheet.autoSizeColumn((short) 7);
            sheet.autoSizeColumn((short) 8);
            sheet.autoSizeColumn((short) 9);
            sheet.autoSizeColumn((short) 10);
            sheet.autoSizeColumn((short) 11);
            sheet.autoSizeColumn((short) 12);
            sheet.autoSizeColumn((short) 12);
            sheet.autoSizeColumn((short) 13);
            sheet.autoSizeColumn((short) 14);
            sheet.autoSizeColumn((short) 15);

            FileOutputStream fileOut = new FileOutputStream(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                hwb.write(fileOut);
                hwb.write(baos);
                bis = new ByteArrayInputStream(baos.toByteArray());

            } catch (FileNotFoundException e) {
            }
            fileOut.close();
        } catch (IOException e) {
        }

        return bis;
    }

    public ByteArrayInputStream DetailsReport1(int indentNumber, String fromDate, String toDate, int indentNumber1, int materialId, int indentStatusId) throws FileNotFoundException, IOException {
        ByteArrayInputStream bis = null;
        String filename = "ProcurementReport(" + utils.DateIn() + ").xls";
        String query = "", query1, query2 = "";
        List listIndent = new ArrayList();
        try {
            if (indentNumber != 0) {
                query = " a.indent_createdby=" + indentNumber + "";
            }
            if (fromDate.length() != 0 && toDate.length() != 0) {
                if (!query.isEmpty()) {
                    query = query + " and DATE(a.indent_date) >='" + fromDate + "' and DATE(a.indent_date) <='" + toDate + "'";
                } else {
                    query = query + " DATE(a.indent_date) >='" + fromDate + "' and DATE(a.indent_date) <='" + toDate + "'";
                }
            }
            if (indentNumber1 != 0) {
                if (!query.isEmpty()) {
                    query = query + " and a.indent_approvedby=" + indentNumber1 + "";
                } else {
                    query = query + " a.indent_approvedby=" + indentNumber1 + "";
                }
            }
            if (materialId != 0) {
                if (!query.isEmpty()) {
                    query = query + " and b.indent_details_material=" + materialId + "";
                } else {
                    query = query + " b.indent_details_material=" + materialId + "";
                }
            }
            /*if (umoId != 0) {
             if (!query.isEmpty()) {
             query = query + " and b.indent_details_umo=" + umoId + "";
             } else {
             query = query + " b.indent_details_umo=" + umoId + "";
             }
             }*/
            if (indentStatusId != 0) {
                if (indentStatusId == 1 || indentStatusId == 2 || indentStatusId == 3) {
                    if (!query.isEmpty()) {
                        if (indentStatusId == 2) {
                            query = query + " and (a.indent_approved_status=" + indentStatusId + " or a.indent_final_status=" + indentStatusId + ")";
                        } else {
                            query = query + " and a.indent_approved_status=" + indentStatusId + "";
                        }
                    } else {
                        query = query + " a.indent_approved_status=" + indentStatusId + "";
                    }
                }
                if (indentStatusId == 4) {
                    if (!query.isEmpty()) {
                        query = query + " and a.indent_final_status=" + indentStatusId + "";
                    } else {
                        query = query + " a.indent_final_status=" + indentStatusId + "";
                    }
                }
                if (indentStatusId == 5 || indentStatusId == 6 || indentStatusId == 7 || indentStatusId == 7 || indentStatusId == 8) {
                    if (!query.isEmpty()) {
                        query = query + " and a.indent_procurement_status=" + indentStatusId + "";
                    } else {
                        query = query + " a.indent_procurement_status=" + indentStatusId + "";
                    }
                }
            }
            if (!query.isEmpty()) {
                query2 = " where " + query;
            } else {
                query2 = "";
            }

            /*query1 = "select a.indent_no,a.indent_year,a.indent_date,d.emp_id,e.comp_name,f.dept_name,g.sec_name,h.plant_name,a.indent_comments,d.emp_name,i.emp_name,a.indent_approvedby_date,a.indent_remarks,j.emp_name,a.indent_final_date,a.indent_final_remarks,k.material_name,l.umo_name,b.indent_details_purpose,b.indent_details_vendor,b.indent_details_qty,b.indent_details_rm_qty,b.indent_details_dept_qty,b.indent_details_stock_aval,b.indent_details_pricing,c.indent_procurement_remarks,c.indent_procurement_date,a.indent_delivery_date,a.indent_po_number,a.indent_approved_status,a.indent_final_status,a.indent_procurement_status from tbl_indent_master a "
             + "left join tbl_indent_details b on a.indent_id=b.indent_id "
             + "left join tbl_indent_procurement_logs c on a.indent_id=c.indent_procurement_indent "
             + "left join tbl_emp_master d on a.indent_createdby=d.emp_number "
             + "left join tbl_company_master e on a.indent_company=e.comp_id "
             + "left join tbl_department_master f on a.indent_dept=f.dept_id "
             + "left join tbl_section_master g on a.indent_sec=g.sec_id "
             + "left join tbl_plant_master h on a.indent_plant=h.plant_id "
             + "left join tbl_emp_master i on a.indent_approvedby=i.emp_number "
             + "left join tbl_emp_master j on a.indent_final_approvedby=j.emp_number "
             + "left join tbl_material_master k on b.indent_details_material=k.material_id "
             + "left join tbl_umo_master l on b.indent_details_umo=l.umo_id " + query2 + "";*/
            query1 = "select a.indent_no,a.indent_year,a.indent_date,c.emp_id,d.comp_name,e.dept_name,f.sec_name,g.plant_name,a.indent_comments,c.emp_name,h.emp_name,a.indent_approvedby_date,a.indent_remarks,i.emp_name,a.indent_final_date,a.indent_final_remarks,j.material_name,k.umo_name,b.indent_details_purpose,b.indent_details_vendor,b.indent_details_qty,b.indent_details_rm_qty,b.indent_details_dept_qty,b.indent_details_stock_aval,b.indent_details_pricing,a.indent_delivery_date,a.indent_po_number,a.indent_approved_status,a.indent_final_status,a.indent_procurement_status from tbl_indent_master a "
                    + "left join tbl_indent_details b on a.indent_id=b.indent_id "
                    + "left join tbl_emp_master c on a.indent_createdby=c.emp_number "
                    + "left join tbl_company_master d on a.indent_company=d.comp_id "
                    + "left join tbl_department_master e on a.indent_dept=e.dept_id "
                    + "left join tbl_section_master f on a.indent_sec=f.sec_id "
                    + "left join tbl_plant_master g on a.indent_plant=g.plant_id "
                    + "left join tbl_emp_master h on a.indent_approvedby=h.emp_number "
                    + "left join tbl_emp_master i on a.indent_final_approvedby=i.emp_number "
                    + "left join tbl_material_master j on b.indent_details_material=j.material_id "
                    + "left join tbl_umo_master k on b.indent_details_umo=k.umo_id " + query2 + "";
            listIndent = indentDao.getSqlQuery1(query1);
            HSSFWorkbook hwb = new HSSFWorkbook();
            int i = 1;
            HSSFSheet sheet = hwb.createSheet(filename);
            sheet.autoSizeColumn((short) 1);
            HSSFCellStyle MainHead = hwb.createCellStyle();
            HSSFCellStyle MainHead1 = hwb.createCellStyle();
            HSSFCellStyle MainHead2 = hwb.createCellStyle();
            HSSFCellStyle TableHdr = hwb.createCellStyle();
            HSSFCellStyle TableData = hwb.createCellStyle();
            HSSFCellStyle TableDataLt = hwb.createCellStyle();
            HSSFCellStyle TableData1 = hwb.createCellStyle();
            HSSFCellStyle TableData2 = hwb.createCellStyle();
            HSSFCellStyle TableData3 = hwb.createCellStyle();
            TableHdr.setAlignment((short) 1);
            TableData.setAlignment((short) 1);
            TableDataLt.setAlignment((short) 1);
            TableData1.setAlignment((short) 1);
            TableData2.setAlignment((short) 1);
            TableData3.setAlignment((short) 1);

            MainHead.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
            HSSFFont font = hwb.createFont();
            font.setFontName("calibri");
            font.setFontHeightInPoints((short) 13);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font.setColor(HSSFColor.BLACK.index);
            MainHead.setFont(font);
            HSSFFont font5 = hwb.createFont();
            font5.setFontName("calibri");

            font5.setFontHeightInPoints((short) 10);
            font5.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font5.setColor(HSSFColor.BLACK.index);
            MainHead1.setFont(font5);
            HSSFFont font6 = hwb.createFont();
            font6.setFontName("calibri");

            font6.setFontHeightInPoints((short) 8);
            font6.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font6.setColor(HSSFColor.BLACK.index);
            MainHead2.setFont(font6);
            MainHead2.setBorderBottom((short) 1);
            MainHead2.setBorderTop((short) 1);
            MainHead2.setBorderLeft((short) 1);
            MainHead2.setBorderRight((short) 1);
            MainHead2.setBorderRight((short) 1);

            HSSFFont font1 = hwb.createFont();
            font1.setFontName("calibri");
            font1.setFontHeightInPoints((short) 10);
            font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font1.setColor(HSSFColor.BLACK.index);
            TableHdr.setFont(font1);
            TableHdr.setBorderBottom((short) 1);
            TableHdr.setBorderTop((short) 1);
            TableHdr.setBorderLeft((short) 1);
            TableHdr.setBorderRight((short) 1);
            TableHdr.setBorderRight((short) 1);

            HSSFFont font2 = hwb.createFont();
            font2.setFontName("calibri");
            font2.setFontHeightInPoints((short) 8);
            font2.setColor(HSSFColor.BLACK.index);
            TableData.setFont(font2);
            TableData.setBorderBottom((short) 1);
            TableData.setBorderTop((short) 1);
            TableData.setBorderLeft((short) 1);
            TableData.setBorderRight((short) 1);

            HSSFFont fontLt = hwb.createFont();
            fontLt.setFontName("calibri");
            fontLt.setFontHeightInPoints((short) 8);
            fontLt.setColor(HSSFColor.RED.index);
            TableDataLt.setFont(fontLt);
            TableDataLt.setBorderBottom((short) 1);
            TableDataLt.setBorderTop((short) 1);
            TableDataLt.setBorderLeft((short) 1);
            TableDataLt.setBorderRight((short) 1);
            HSSFFont font3 = hwb.createFont();
            font3.setFontName("calibri");
            font3.setFontHeightInPoints((short) 10);
            font3.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font3.setColor(HSSFColor.BLACK.index);
            TableData1.setFont(font3);
            TableData1.setBorderBottom((short) 1);
            TableData1.setBorderTop((short) 1);
            TableData1.setBorderLeft((short) 1);
            TableData1.setBorderRight((short) 1);

            HSSFRow row = sheet.createRow((short) 1);
            sheet.setDefaultColumnWidth((short) 3);
            sheet.setDisplayGridlines(false);
            sheet.setVerticallyCenter(true);
            sheet.setDefaultRowHeight((short) 250);
            HSSFCell cell = row.createCell((short) 0);

            if (i == 1) {
                Iterator it = listIndent.iterator();
                while (it.hasNext()) {
                    Object obj = it.next();
                    sheet.addMergedRegion(new Region(1, (short) 0, 1, (short) 30));
                    sheet.addMergedRegion(new Region(2, (short) 0, 2, (short) 30));
                    sheet.addMergedRegion(new Region(3, (short) 0, 3, (short) 30));
                    cell.setCellValue("Indent Request Details");
                    cell.setCellStyle(MainHead);
                    row = sheet.createRow((short) 2);
                    cell = row.createCell((short) 0);
                    cell.setCellValue("Report Genarated Date : " + utils.DateIn());
                    cell.setCellStyle(MainHead);
                    row = sheet.createRow((short) 3);
                    cell = row.createCell((short) 0);
                    cell.setCellStyle(MainHead);
                }
                row = sheet.createRow((short) 4);

                HSSFCell cell1 = row.createCell((short) 0);
                HSSFCell cell2 = row.createCell((short) 1);
                HSSFCell cell3 = row.createCell((short) 2);
                HSSFCell cell4 = row.createCell((short) 3);
                HSSFCell cell5 = row.createCell((short) 4);
                HSSFCell cell6 = row.createCell((short) 5);
                HSSFCell cell7 = row.createCell((short) 6);
                HSSFCell cell8 = row.createCell((short) 7);
                HSSFCell cell9 = row.createCell((short) 8);
                HSSFCell cell10 = row.createCell((short) 9);
                HSSFCell cell11 = row.createCell((short) 10);
                HSSFCell cell12 = row.createCell((short) 11);
                HSSFCell cell13 = row.createCell((short) 12);
                HSSFCell cell14 = row.createCell((short) 13);
                HSSFCell cell15 = row.createCell((short) 14);
                HSSFCell cell16 = row.createCell((short) 15);
                HSSFCell cell17 = row.createCell((short) 16);
                HSSFCell cell18 = row.createCell((short) 17);
                HSSFCell cell19 = row.createCell((short) 18);
                HSSFCell cell20 = row.createCell((short) 19);
                HSSFCell cell21 = row.createCell((short) 20);
                HSSFCell cell22 = row.createCell((short) 21);
                HSSFCell cell23 = row.createCell((short) 22);
                HSSFCell cell24 = row.createCell((short) 23);
                HSSFCell cell25 = row.createCell((short) 24);
                HSSFCell cell26 = row.createCell((short) 25);
                HSSFCell cell27 = row.createCell((short) 26);
                HSSFCell cell28 = row.createCell((short) 27);
                HSSFCell cell29 = row.createCell((short) 28);
                HSSFCell cell30 = row.createCell((short) 29);

                cell1.setCellValue("Indent No.");
                cell2.setCellValue("Year");
                cell3.setCellValue("Indent Date");
                cell4.setCellValue("Employee Id");
                cell5.setCellValue("Company");
                cell6.setCellValue("Department");
                cell7.setCellValue("Section");
                cell8.setCellValue("Plant");
                cell9.setCellValue("Indent Comments");
                cell10.setCellValue("Indenter");
                cell11.setCellValue("Rm ");
                cell12.setCellValue("Rm Approved Date");
                cell13.setCellValue("Rm Remarks");
                cell14.setCellValue("Dept.Head");
                cell15.setCellValue("Dept.Head Approved Date");
                cell16.setCellValue("Dept.Head Remarks");
                cell17.setCellValue("Material");
                cell18.setCellValue("Uom");
                cell19.setCellValue("Project");
                cell20.setCellValue("Vendor");
                cell21.setCellValue("Quantity Requested");
                cell22.setCellValue("Rm Quantity Requested");
                cell23.setCellValue("Dept.Head Quantity Requested");
                cell24.setCellValue("Available Stock");
                cell25.setCellValue("Pricing");
                cell26.setCellValue("Delivery Date");
                cell27.setCellValue("PO Number");
                cell28.setCellValue("Procurement Remarks");
                cell29.setCellValue("Procurement Date");
                cell30.setCellValue("Indent Status");

                cell1.setCellStyle(TableHdr);
                cell2.setCellStyle(TableHdr);
                cell3.setCellStyle(TableHdr);
                cell4.setCellStyle(TableHdr);
                cell5.setCellStyle(TableHdr);
                cell6.setCellStyle(TableHdr);
                cell7.setCellStyle(TableHdr);
                cell8.setCellStyle(TableHdr);
                cell9.setCellStyle(TableHdr);
                cell10.setCellStyle(TableHdr);
                cell11.setCellStyle(TableHdr);
                cell12.setCellStyle(TableHdr);
                cell13.setCellStyle(TableHdr);
                cell14.setCellStyle(TableHdr);
                cell15.setCellStyle(TableHdr);
                cell16.setCellStyle(TableHdr);
                cell17.setCellStyle(TableHdr);
                cell18.setCellStyle(TableHdr);
                cell19.setCellStyle(TableHdr);
                cell20.setCellStyle(TableHdr);
                cell21.setCellStyle(TableHdr);
                cell22.setCellStyle(TableHdr);
                cell23.setCellStyle(TableHdr);
                cell24.setCellStyle(TableHdr);
                cell25.setCellStyle(TableHdr);
                cell26.setCellStyle(TableHdr);
                cell27.setCellStyle(TableHdr);
                cell28.setCellStyle(TableHdr);
                cell29.setCellStyle(TableHdr);
                cell30.setCellStyle(TableHdr);

                i = 5;

                Iterator it1 = listIndent.iterator();
                while (it1.hasNext()) {
                    Object[] indent = (Object[]) it1.next();

                    row = sheet.createRow((short) i);
                    cell1 = row.createCell((short) 0);
                    cell2 = row.createCell((short) 1);
                    cell3 = row.createCell((short) 2);
                    cell4 = row.createCell((short) 3);
                    cell5 = row.createCell((short) 4);
                    cell6 = row.createCell((short) 5);
                    cell7 = row.createCell((short) 6);
                    cell8 = row.createCell((short) 7);
                    cell9 = row.createCell((short) 8);
                    cell10 = row.createCell((short) 9);
                    cell11 = row.createCell((short) 10);
                    cell12 = row.createCell((short) 11);
                    cell13 = row.createCell((short) 12);
                    cell14 = row.createCell((short) 13);
                    cell15 = row.createCell((short) 14);
                    cell16 = row.createCell((short) 15);
                    cell17 = row.createCell((short) 16);
                    cell18 = row.createCell((short) 17);
                    cell19 = row.createCell((short) 18);
                    cell20 = row.createCell((short) 19);
                    cell21 = row.createCell((short) 20);
                    cell22 = row.createCell((short) 21);
                    cell23 = row.createCell((short) 22);
                    cell24 = row.createCell((short) 23);
                    cell25 = row.createCell((short) 24);
                    cell26 = row.createCell((short) 25);
                    cell27 = row.createCell((short) 26);
                    cell28 = row.createCell((short) 27);
                    cell29 = row.createCell((short) 28);
                    cell30 = row.createCell((short) 29);

                    try {
                        try {
                            if (indent[0] == null || indent[0].toString().length() == 0) {
                                cell1.setCellValue("-");
                            } else {
                                cell1.setCellValue(indent[0].toString());
                            }
                        } catch (Exception e) {
                            cell1.setCellValue("-");
                        }
                        try {
                            if (indent[1] == null || indent[1].toString().length() == 0) {
                                cell2.setCellValue("-");
                            } else {
                                cell2.setCellValue(indent[1].toString());
                            }
                        } catch (Exception e) {
                            cell2.setCellValue("-");
                        }
                        try {
                            if (indent[2] == null || indent[2].toString().length() == 0) {
                                cell3.setCellValue("-");
                            } else {
                                cell3.setCellValue(utils.getDateFormat3(indent[2].toString()));
                            }
                        } catch (Exception e) {
                            cell3.setCellValue("-");
                        }
                        try {
                            if (indent[3] == null || indent[3].toString().length() == 0) {
                                cell4.setCellValue("-");
                            } else {
                                cell4.setCellValue(indent[3].toString());
                            }
                        } catch (Exception e) {
                            cell4.setCellValue("-");
                        }
                        try {
                            if (indent[4] == null || indent[4].toString().length() == 0) {
                                cell5.setCellValue("-");
                            } else {
                                cell5.setCellValue(indent[4].toString());
                            }
                        } catch (Exception e) {
                            cell5.setCellValue("-");
                        }
                        try {
                            if (indent[5] == null || indent[5].toString().length() == 0) {
                                cell6.setCellValue("-");
                            } else {
                                cell6.setCellValue(indent[5].toString());
                            }
                        } catch (Exception e) {
                            cell6.setCellValue("-");
                        }
                        try {
                            if (indent[6] == null || indent[6].toString().length() == 0) {
                                cell7.setCellValue("-");
                            } else {
                                cell7.setCellValue(indent[6].toString());
                            }
                        } catch (Exception e) {
                            cell7.setCellValue("-");
                        }
                        try {
                            if (indent[7] == null || indent[7].toString().length() == 0) {
                                cell8.setCellValue("-");
                            } else {
                                cell8.setCellValue(indent[7].toString());
                            }
                        } catch (Exception e) {
                            cell8.setCellValue("-");
                        }
                        try {
                            if (indent[8] == null || indent[8].toString().length() == 0) {
                                cell9.setCellValue("-");
                            } else {
                                cell9.setCellValue(indent[8].toString());
                            }
                        } catch (Exception e) {
                            cell9.setCellValue("-");
                        }
                        try {
                            if (indent[9] == null || indent[9].toString().length() == 0) {
                                cell10.setCellValue("-");
                            } else {
                                cell10.setCellValue(indent[9].toString());
                            }
                        } catch (Exception e) {
                            cell10.setCellValue("-");
                        }
                        try {
                            if (indent[10] == null || indent[10].toString().length() == 0) {
                                cell11.setCellValue("-");
                            } else {
                                cell11.setCellValue(indent[10].toString());
                            }
                        } catch (Exception e) {
                            cell11.setCellValue("-");
                        }
                        try {
                            if (indent[11] == null || indent[11].toString().length() == 0) {
                                cell12.setCellValue("-");
                            } else {
                                cell12.setCellValue(indent[11].toString());
                            }
                        } catch (Exception e) {
                            cell12.setCellValue("-");
                        }
                        try {
                            if (indent[12] == null || indent[12].toString().length() == 0) {
                                cell13.setCellValue("-");
                            } else {
                                cell13.setCellValue(indent[12].toString());
                            }
                        } catch (Exception e) {
                            cell13.setCellValue("-");
                        }
                        try {
                            if (indent[13] == null || indent[13].toString().length() == 0) {
                                cell14.setCellValue("-");
                            } else {
                                cell14.setCellValue(indent[13].toString());
                            }
                        } catch (Exception e) {
                            cell14.setCellValue("-");
                        }
                        try {
                            if (indent[14] == null || indent[14].toString().length() == 0) {
                                cell15.setCellValue("-");
                            } else {
                                cell15.setCellValue(indent[14].toString());
                            }
                        } catch (Exception e) {
                            cell15.setCellValue("-");
                        }
                        try {
                            if (indent[15] == null || indent[15].toString().length() == 0) {
                                cell16.setCellValue("-");
                            } else {
                                cell16.setCellValue(indent[15].toString());
                            }
                        } catch (Exception e) {
                            cell16.setCellValue("-");
                        }
                        try {
                            if (indent[16] == null || indent[16].toString().length() == 0) {
                                cell17.setCellValue("-");
                            } else {
                                cell17.setCellValue(indent[16].toString());
                            }
                        } catch (Exception e) {
                            cell17.setCellValue("-");
                        }
                        try {
                            if (indent[17] == null || indent[17].toString().length() == 0) {
                                cell18.setCellValue("-");
                            } else {
                                cell18.setCellValue(indent[17].toString());
                            }
                        } catch (Exception e) {
                            cell18.setCellValue("-");
                        }
                        try {
                            if (indent[18] == null || indent[18].toString().length() == 0) {
                                cell19.setCellValue("-");
                            } else {
                                cell19.setCellValue(indent[18].toString());
                            }
                        } catch (Exception e) {
                            cell19.setCellValue("-");
                        }
                        try {
                            if (indent[19] == null || indent[19].toString().length() == 0) {
                                cell20.setCellValue("-");
                            } else {
                                cell20.setCellValue(indent[19].toString());
                            }
                        } catch (Exception e) {
                            cell20.setCellValue("-");
                        }
                        try {
                            if (indent[20] == null || indent[20].toString().length() == 0) {
                                cell21.setCellValue("-");
                            } else {
                                cell21.setCellValue(indent[20].toString());
                            }
                        } catch (Exception e) {
                            cell21.setCellValue("-");
                        }
                        try {
                            if (indent[21] == null || indent[21].toString().length() == 0) {
                                cell22.setCellValue("-");
                            } else {
                                cell22.setCellValue(indent[21].toString());
                            }
                        } catch (Exception e) {
                            cell22.setCellValue("-");
                        }
                        try {
                            if (indent[22] == null || indent[22].toString().length() == 0) {
                                cell23.setCellValue("-");
                            } else {
                                cell23.setCellValue(indent[22].toString());
                            }
                        } catch (Exception e) {
                            cell23.setCellValue("-");
                        }
                        try {
                            if (indent[23] == null || indent[23].toString().length() == 0) {
                                cell24.setCellValue("-");
                            } else {
                                cell24.setCellValue(indent[23].toString());
                            }
                        } catch (Exception e) {
                            cell24.setCellValue("-");
                        }
                        try {
                            if (indent[24] == null || indent[24].toString().length() == 0) {
                                cell25.setCellValue("-");
                            } else {
                                cell25.setCellValue(indent[24].toString());
                            }
                        } catch (Exception e) {
                            cell25.setCellValue("-");
                        }
                        try {
                            if (indent[25] == null || indent[25].toString().length() == 0) {
                                cell26.setCellValue("-");
                            } else {
                                cell26.setCellValue(indent[25].toString());

                            }
                        } catch (Exception e) {
                            cell26.setCellValue("-");
                        }
                        try {
                            if (indent[26] == null || indent[26].toString().length() == 0) {
                                cell27.setCellValue("-");
                            } else {
                                cell27.setCellValue(indent[26].toString());
                            }
                        } catch (Exception e) {
                            cell27.setCellValue("-");
                        }
                        try {
                            if (indent[27] == null || indent[27].toString().length() == 0) {
                                cell28.setCellValue("-");
                            } else {
                                //cell28.setCellValue(indent[27].toString());
                                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentNo='" + indent[0].toString() + "' and indentStatus=1");
                                if (!listTblIndentMaster.isEmpty()) {
                                    for (TblIndentMaster indentMaster : listTblIndentMaster) {
                                        listTblIndentProcurementLogs = (ArrayList<TblIndentProcurementLogs>) indentProcurementDao.getList("where tblIndentMaster.indentId=" + indentMaster.getIndentId() + " and tblIndentStatus.indentStatusId=" + indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId() + " and indentProcurementStatus=1");
                                        if (!listTblIndentProcurementLogs.isEmpty()) {
                                            for (TblIndentProcurementLogs indentProcurementLogs : listTblIndentProcurementLogs) {
                                                cell28.setCellValue(indentProcurementLogs.getIndentProcurementRemarks());
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            cell28.setCellValue("-");
                        }
                        try {
                            if (indent[28] == null || indent[28].toString().length() == 0) {
                                cell29.setCellValue("-");
                            } else {
                                //cell29.setCellValue(indent[28].toString());
                                listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentNo='" + indent[0].toString() + "' and indentStatus=1");
                                if (!listTblIndentMaster.isEmpty()) {
                                    for (TblIndentMaster indentMaster : listTblIndentMaster) {
                                        listTblIndentProcurementLogs = (ArrayList<TblIndentProcurementLogs>) indentProcurementDao.getList("where tblIndentMaster.indentId=" + indentMaster.getIndentId() + " and tblIndentStatus.indentStatusId=" + indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId() + " and indentProcurementStatus=1");
                                        if (!listTblIndentProcurementLogs.isEmpty()) {
                                            for (TblIndentProcurementLogs indentProcurementLogs : listTblIndentProcurementLogs) {
                                                cell29.setCellValue(indentProcurementLogs.getIndentProcurementDate());
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            cell29.setCellValue("-");
                        }
                        try {
                            if (indent[29] == null || indent[29].toString().length() == 0) {
                                cell30.setCellValue("-");
                            } else {
                                //cell30.setCellValue(indent[30].toString());
                                if (indent[27].toString().equals(String.valueOf(1)) && indent[28].toString().equals(String.valueOf(1)) && indent[29].toString().equals(String.valueOf(1))) {
                                    cell30.setCellValue("Pending");
                                } else if (indent[27].toString().equals(String.valueOf(2)) && indent[28].toString().equals(String.valueOf(1)) && indent[29].toString().equals(String.valueOf(1))) {
                                    cell30.setCellValue("RM Rejected");
                                } else if (indent[27].toString().equals(String.valueOf(3)) && indent[28].toString().equals(String.valueOf(1)) && indent[29].toString().equals(String.valueOf(1))) {
                                    cell30.setCellValue("RM Approved");
                                } else if (indent[27].toString().equals(String.valueOf(3)) && indent[28].toString().equals(String.valueOf(2)) && indent[29].toString().equals(String.valueOf(2))) {
                                    cell30.setCellValue("Dept. Head Rejected");
                                } else if (indent[27].toString().equals(String.valueOf(2)) && indent[28].toString().equals(String.valueOf(2)) && indent[29].toString().equals(String.valueOf(2))) {
                                    cell30.setCellValue("Dept. Head Rejected");
                                } else if (indent[27].toString().equals(String.valueOf(3)) && indent[28].toString().equals(String.valueOf(4)) && indent[29].toString().equals(String.valueOf(4))) {
                                    cell30.setCellValue("Dept. Head Approved");
                                } else if (indent[27].toString().equals(String.valueOf(3)) && indent[28].toString().equals(String.valueOf(4)) && indent[29].toString().equals(String.valueOf(5))) {
                                    cell30.setCellValue("Quotations Collected");
                                } else if (indent[27].toString().equals(String.valueOf(3)) && indent[28].toString().equals(String.valueOf(4)) && indent[29].toString().equals(String.valueOf(6))) {
                                    cell30.setCellValue("Negotiation Done");
                                } else if (indent[27].toString().equals(String.valueOf(3)) && indent[28].toString().equals(String.valueOf(4)) && indent[29].toString().equals(String.valueOf(7))) {
                                    cell30.setCellValue("PO Released");
                                } else if (indent[27].toString().equals(String.valueOf(3)) && indent[28].toString().equals(String.valueOf(4)) && indent[29].toString().equals(String.valueOf(8))) {
                                    cell30.setCellValue("Hold");
                                } else if (indent[27].toString().equals(String.valueOf(3)) && indent[28].toString().equals(String.valueOf(4)) && indent[29].toString().equals(String.valueOf(9))) {
                                    cell30.setCellValue("Cash Buy");
                                }
                            }
                        } catch (Exception e) {
                            cell30.setCellValue("-");
                        }
                        /*if (indent[29].toString() == 1 && indent[30].toString() == 1 && indent[31].toString() == 1) {
                         cell30.setCellValue("Pending");
                         } else if (indent[29].toString() == 2 && indent[30].toString() == 1 && indent[31].toString() == 1) {
                         cell30.setCellValue("RM Rejected");
                         } else if (indent[29].toString() == 3 && indent[30].toString() == 1 && indent[31].toString() == 1) {
                         cell30.setCellValue("RM Approved");
                         } else if (indent[29].toString() == 3 && indent[30].toString() == 2 && indent[31].toString() == 2) {
                         cell30.setCellValue("Dept. Head Rejected");
                         } else if (indent[29].toString() == 2 && indent[30].toString() == 2 && indent[31].toString() == 2) {
                         cell30.setCellValue("Dept. Head Rejected");
                         } else if (indent[29].toString() == 3 && indent[30].toString() == 4 && indent[31].toString() == 4) {
                         cell30.setCellValue("Dept. Head Approved");
                         } else if (indent[29].toString() == 3 && indent[30].toString() == 4 && indent[31].toString() == 5) {
                         cell30.setCellValue("Quotations Collected");
                         } else if (indent[29].toString() == 3 && indent[30].toString() == 4 && indent[31].toString() == 6) {
                         cell30.setCellValue("Negotiation Done");
                         } else if (indent[29].toString() == 3 && indent[30].toString() == 4 && indent[31].toString() == 7) {
                         cell30.setCellValue("PO Released");
                         } else if (indent[29].toString() == 3 && indent[30].toString() == 4 && indent[31].toString() == 8) {
                         cell30.setCellValue("Hold");
                         } else if (indent[29].toString() == 3 && indent[30].toString() == 4 && indent[31].toString() == 9) {
                         cell30.setCellValue("Cash Buy");
                         }*/
                    } catch (NullPointerException e) {
                    }

                    cell1.setCellStyle(TableData1);
                    cell2.setCellStyle(TableData1);
                    cell3.setCellStyle(TableData1);
                    cell4.setCellStyle(TableData1);
                    cell5.setCellStyle(TableData1);
                    cell6.setCellStyle(TableData1);
                    cell7.setCellStyle(TableData1);
                    cell8.setCellStyle(TableData1);
                    cell9.setCellStyle(TableData1);
                    cell10.setCellStyle(TableData1);
                    cell11.setCellStyle(TableData1);
                    cell12.setCellStyle(TableData1);
                    cell13.setCellStyle(TableData1);
                    cell14.setCellStyle(TableData1);
                    cell15.setCellStyle(TableData1);
                    cell16.setCellStyle(TableData1);
                    cell17.setCellStyle(TableData1);
                    cell18.setCellStyle(TableData1);
                    cell19.setCellStyle(TableData1);
                    cell20.setCellStyle(TableData1);
                    cell21.setCellStyle(TableData1);
                    cell22.setCellStyle(TableData1);
                    cell23.setCellStyle(TableData1);
                    cell24.setCellStyle(TableData1);
                    cell25.setCellStyle(TableData1);
                    cell26.setCellStyle(TableData1);
                    cell27.setCellStyle(TableData1);
                    cell28.setCellStyle(TableData1);
                    cell29.setCellStyle(TableData1);
                    cell30.setCellStyle(TableData1);

                    i++;
                }
            }
            sheet.autoSizeColumn((short) 0);
            sheet.autoSizeColumn((short) 1);
            sheet.autoSizeColumn((short) 2);
            sheet.autoSizeColumn((short) 3);
            sheet.autoSizeColumn((short) 4);
            sheet.autoSizeColumn((short) 5);
            sheet.autoSizeColumn((short) 6);
            sheet.autoSizeColumn((short) 7);
            sheet.autoSizeColumn((short) 8);
            sheet.autoSizeColumn((short) 9);
            sheet.autoSizeColumn((short) 10);
            sheet.autoSizeColumn((short) 11);
            sheet.autoSizeColumn((short) 12);
            sheet.autoSizeColumn((short) 12);
            sheet.autoSizeColumn((short) 13);
            sheet.autoSizeColumn((short) 14);
            sheet.autoSizeColumn((short) 15);
            sheet.autoSizeColumn((short) 16);
            sheet.autoSizeColumn((short) 17);
            sheet.autoSizeColumn((short) 18);
            sheet.autoSizeColumn((short) 19);
            sheet.autoSizeColumn((short) 20);
            sheet.autoSizeColumn((short) 21);
            sheet.autoSizeColumn((short) 22);
            sheet.autoSizeColumn((short) 23);
            sheet.autoSizeColumn((short) 24);
            sheet.autoSizeColumn((short) 25);
            sheet.autoSizeColumn((short) 26);
            sheet.autoSizeColumn((short) 27);
            sheet.autoSizeColumn((short) 28);
            sheet.autoSizeColumn((short) 29);
            sheet.autoSizeColumn((short) 30);

            FileOutputStream fileOut = new FileOutputStream(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                hwb.write(fileOut);
                hwb.write(baos);
                bis = new ByteArrayInputStream(baos.toByteArray());

            } catch (FileNotFoundException e) {
            }
            fileOut.close();
        } catch (IOException e) {
        }

        return bis;
    }

}
