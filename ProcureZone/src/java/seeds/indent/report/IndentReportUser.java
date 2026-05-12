/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import plant.indent.action.IndentPzDaoImpl;
import pojo.TbPzlIndentMastera;
import pojo.TblEmpMaster;
import pojo.TblIndentMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.indent.daoImpl.IndentDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class IndentReportUser {

    private final Utils utils = new Utils();
    private List<TblIndentMaster> listTblIndentMaster;
    private TbPzlIndentMastera tblIndentMaster;
    private List<TbPzlIndentMastera> listTblpzIndentMaster;
    private final IndentPzDaoImpl indentpzDao = DaoFactory.getDao(IndentPzDaoImpl.class);
    
    private final IndentDaoImpl indentDao = DaoFactory.getDao(IndentDaoImpl.class);

    public List<TblIndentMaster> getListTblIndentMaster() {
        return listTblIndentMaster;
    }

    public void setListTblIndentMaster(List<TblIndentMaster> listTblIndentMaster) {
        this.listTblIndentMaster = listTblIndentMaster;
    }

    public IndentReportUser() throws Exception {
        listTblIndentMaster = new ArrayList<TblIndentMaster>();
    }

    public ByteArrayInputStream UserProcurementReport(int empNumber) throws FileNotFoundException, IOException {
        ByteArrayInputStream bis = null;
        String filename = "ProcurementReport(" + utils.DateIn() + ").xls";
        try {
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblEmpMasterByIndentCreatedby.empNumber=" + empNumber + "");

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
                Iterator it = listTblIndentMaster.iterator();
                while (it.hasNext()) {
                    TblIndentMaster indentMast = (TblIndentMaster) it.next();
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

                cell1.setCellValue("Indent No.");
                cell2.setCellValue("Year");
                cell3.setCellValue("Indent Date");
                cell4.setCellValue("Employee Id");
                cell5.setCellValue("Employee Name");
                cell6.setCellValue("Company");
                cell7.setCellValue("Department");
                cell8.setCellValue("Section");
                cell9.setCellValue("Plant");
                cell10.setCellValue("Indent Comments");
                cell11.setCellValue("Indent Status");

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
                i = 5;

                Iterator it1 = listTblIndentMaster.iterator();
                while (it1.hasNext()) {
                    TblIndentMaster indentMaster = (TblIndentMaster) it1.next();

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

                    try {
                        cell1.setCellValue(indentMaster.getIndentNo());
                        cell2.setCellValue(indentMaster.getIndentYear());
                        cell3.setCellValue(utils.getDateFormat3(indentMaster.getIndentDate()));
                        cell4.setCellValue(indentMaster.getTblEmpMasterByIndentCreatedby().getEmpId());
                        cell5.setCellValue(indentMaster.getTblEmpMasterByIndentCreatedby().getEmpName());
                        cell6.setCellValue(indentMaster.getTblCompanyMaster().getCompName());
                        cell7.setCellValue(indentMaster.getTblDepartmentMaster().getDeptName());
                        cell8.setCellValue(indentMaster.getTblSectionMaster().getSecName());
                        cell9.setCellValue(indentMaster.getTblPlantMaster().getPlantName());
                        cell10.setCellValue(indentMaster.getIndentComments());
                        if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("Pending");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("RM Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("RM Approved");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==2){
                            cell11.setCellValue("Dept. Head Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==2){
                            cell11.setCellValue("Dept. Head Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==4){
                            cell11.setCellValue("Dept. Head Approved");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==5){
                            cell11.setCellValue("Quotations Collected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==6){
                            cell11.setCellValue("Negotiation Done");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==7){
                            cell11.setCellValue("PO Released");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==8){
                            cell11.setCellValue("Hold");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==9){
                            cell11.setCellValue("Cash Buy");
                        }
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

            FileOutputStream fileOut = new FileOutputStream(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                hwb.write(fileOut);
                hwb.write(baos);
                bis = new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException e) {
            }
            fileOut.close();
        } catch (Exception e) {
        }

        return bis;
    }
    
 
    public ByteArrayInputStream UserPzIndentProcurementReport(int empNumber) throws FileNotFoundException, IOException {
        ByteArrayInputStream bis = null;
        String filename = "PlantindentReport(" + utils.DateIn() + ").xls";
        try {
            TblEmpMaster empmaster= new TblEmpMaster();
            System.out.println("emp Number is :"+empmaster.getEmpId());
            //listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where tblIndentStatusByIndentFinalStatus.indentStatus=1 and tblEmpMasterByIndentCreatedby.empNumber=" + empNumber + "");
            listTblpzIndentMaster = (ArrayList<TbPzlIndentMastera>) indentpzDao.getList("where indentStatus in (1,2,3,4,5,6,7,8,9,0,20) ");
            System.out.println("indent master table data : "+listTblpzIndentMaster);

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
                Iterator it = listTblpzIndentMaster.iterator();
                while (it.hasNext()) {
                    TbPzlIndentMastera indentMast = (TbPzlIndentMastera) it.next();
                    sheet.addMergedRegion(new Region(1, (short) 0, 1, (short) 30));
                    sheet.addMergedRegion(new Region(2, (short) 0, 2, (short) 30));
                    sheet.addMergedRegion(new Region(3, (short) 0, 3, (short) 30));
                    cell.setCellValue("Indent Details");
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

                cell1.setCellValue("Indent No.");
                cell2.setCellValue("Year");
                cell3.setCellValue("Indent Date");
                cell4.setCellValue("Employee Id");
                cell5.setCellValue("Employee Name");
                cell6.setCellValue("Company");
                cell7.setCellValue("Plant");
                cell8.setCellValue("Department");
                
                cell9.setCellValue("Crop");
                cell10.setCellValue("Processing/Packing");
                cell11.setCellValue("Output Material");
                cell12.setCellValue("Output Material Description");
                
                
                cell13.setCellValue("UOM");
                cell14.setCellValue("Expected Output Quantity");
                cell15.setCellValue("Order Type");
                cell16.setCellValue("Order Description");
                cell17.setCellValue("Start Date");
                cell18.setCellValue("Line Code");
                cell19.setCellValue("Line Description");
                cell20.setCellValue("Batch Number");
                cell21.setCellValue("Comment");
                cell22.setCellValue("Status");
                

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
                
                
                i = 5;

                Iterator it1 = listTblpzIndentMaster.iterator();
                while (it1.hasNext()) {
                    TbPzlIndentMastera indentMaster = (TbPzlIndentMastera) it1.next();

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

                    try {
                        cell1.setCellValue(indentMaster.getIndentFinalNumber());
                        cell2.setCellValue(indentMaster.getIndentYear());
                        cell3.setCellValue(utils.getDateFormat3(indentMaster.getIndentDate()));
                        cell4.setCellValue(indentMaster.getTblEmpMasterByIndentCreatedby().getEmpId());
                        //cell5.setCellValue(indentMaster.getTblEmpMasterByIndentCreatedby().getEmpName());
                        cell5.setCellValue(indentMaster.getTblEmpMasterByIndentLmu().getEmpName());
                        cell6.setCellValue(indentMaster.getTblCompanyMaster().getCompId());
                        cell7.setCellValue(indentMaster.getTblCompanyMaster().getCompId());
                        cell8.setCellValue(indentMaster.getTblDepartmentMaster().getDeptName());
                        
                        cell9.setCellValue(indentMaster.getIndentCrop());
                        cell10.setCellValue(indentMaster.getIndentPackProcessa());
                        cell11.setCellValue(indentMaster.getIndentOutmaterial());
                        cell12.setCellValue(indentMaster.getIndentOutdesc());
                        cell13.setCellValue(indentMaster.getIndentUom());
                        cell14.setCellValue(indentMaster.getIndentOutqty());
                        cell15.setCellValue(indentMaster.getIndentOrderTypea());
                        cell16.setCellValue(indentMaster.getIndentOrderTpDesc());
                        cell17.setCellValue(indentMaster.getIndentStartdate());
                        cell18.setCellValue(indentMaster.getIndentLinecode());
                        cell19.setCellValue(indentMaster.getIndentLinedesc());
                        cell20.setCellValue(indentMaster.getIndentBatchNumbera());
                        cell21.setCellValue(indentMaster.getIndentComments());
                        if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==10){
                            cell22.setCellValue("Inventory Issue Pending");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==10){
                            cell22.setCellValue("Inventory Issue Confirmation");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==10){
                            cell22.setCellValue("Deo Order Pending");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==10){
                            cell22.setCellValue("Inventory Receipt Pending");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==6 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==10){
                            cell22.setCellValue("Receipt Confirmation Pending");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==5 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==10){
                            cell22.setCellValue("GRN Receipt pending");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==7 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==10){
                            cell22.setCellValue("Completed");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==0 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==0 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==10){
                            cell22.setCellValue("Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==10 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==10 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==10){
                            cell22.setCellValue("Quality Control Pending");
                        }
//                        else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==5){
//                            cell20.setCellValue("Quotations Collected");
//                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==6){
//                            cell20.setCellValue("Negotiation Done");
//                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==7){
//                            cell20.setCellValue("PO Released");
//                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==8){
//                            cell20.setCellValue("Hold");
//                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==9){
//                            cell20.setCellValue("Cash Buy");
//                        }
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
            sheet.autoSizeColumn((short) 13);
            sheet.autoSizeColumn((short) 14);
            sheet.autoSizeColumn((short) 15);
            sheet.autoSizeColumn((short) 16);
            sheet.autoSizeColumn((short) 17);
            sheet.autoSizeColumn((short) 18);
            sheet.autoSizeColumn((short) 19);
            sheet.autoSizeColumn((short) 20);
            sheet.autoSizeColumn((short) 21);

            FileOutputStream fileOut = new FileOutputStream(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                hwb.write(fileOut);
                hwb.write(baos);
                bis = new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException e) {
            }
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bis;
    }
    
   
    public ByteArrayInputStream RmProcurementReport(int empNumber) throws FileNotFoundException, IOException {
        ByteArrayInputStream bis = null;
        String filename = "ProcurementReport(" + utils.DateIn() + ").xls";
        try {
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where (indentStatus=1 and tblEmpMasterByIndentCreatedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + empNumber + ")) or (indentStatus=1 and tblEmpMasterByIndentCreatedby.empNumber =" + empNumber + ")");

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
                Iterator it = listTblIndentMaster.iterator();
                while (it.hasNext()) {
                    TblIndentMaster indentMast = (TblIndentMaster) it.next();
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

                cell1.setCellValue("Indent No.");
                cell2.setCellValue("Year");
                cell3.setCellValue("Indent Date");
                cell4.setCellValue("Employee Id");
                cell5.setCellValue("Employee Name");
                cell6.setCellValue("Company");
                cell7.setCellValue("Department");
                cell8.setCellValue("Section");
                cell9.setCellValue("Plant");
                cell10.setCellValue("Indent Comments");
                cell11.setCellValue("Indent Status");

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
                i = 5;

                Iterator it1 = listTblIndentMaster.iterator();
                while (it1.hasNext()) {
                    TblIndentMaster indentMaster = (TblIndentMaster) it1.next();

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

                    try {
                        cell1.setCellValue(indentMaster.getIndentNo());
                        cell2.setCellValue(indentMaster.getIndentYear());
                        cell3.setCellValue(utils.getDateFormat3(indentMaster.getIndentDate()));
                        cell4.setCellValue(indentMaster.getTblEmpMasterByIndentCreatedby().getEmpId());
                        cell5.setCellValue(indentMaster.getTblEmpMasterByIndentCreatedby().getEmpName());
                        cell6.setCellValue(indentMaster.getTblCompanyMaster().getCompName());
                        cell7.setCellValue(indentMaster.getTblDepartmentMaster().getDeptName());
                        cell8.setCellValue(indentMaster.getTblSectionMaster().getSecName());
                        cell9.setCellValue(indentMaster.getTblPlantMaster().getPlantName());
                        cell10.setCellValue(indentMaster.getIndentComments());
                        if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("Pending");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("RM Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("RM Approved");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==2){
                            cell11.setCellValue("Dept. Head Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==2){
                            cell11.setCellValue("Dept. Head Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==4){
                            cell11.setCellValue("Dept. Head Approved");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==5){
                            cell11.setCellValue("Quotations Collected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==6){
                            cell11.setCellValue("Negotiation Done");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==7){
                            cell11.setCellValue("PO Released");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==8){
                            cell11.setCellValue("Hold");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==9){
                            cell11.setCellValue("Cash Buy");
                        }
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

            FileOutputStream fileOut = new FileOutputStream(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                hwb.write(fileOut);
                hwb.write(baos);
                bis = new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException e) {
            }
            fileOut.close();
        } catch (Exception e) {
        }

        return bis;
    }
    
    public ByteArrayInputStream DeptProcurementReport(int empNumber) throws FileNotFoundException, IOException {
        ByteArrayInputStream bis = null;
        String filename = "ProcurementReport(" + utils.DateIn() + ").xls";
        try {
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblEmpMasterByIndentFinalApprovedby.empNumber =" + empNumber + " and tblIndentStatusByIndentFinalStatus.indentStatusId in (2,3,4) or (tblEmpMasterByIndentApprovedby.empNumber=" + empNumber + ") or (tblEmpMasterByIndentApprovedby.empNumber in (select tblEmpMasterByReportSub.empNumber from TblMapEmpReporting where tblEmpMasterByReportSup.empNumber=" + empNumber + " and reportStatus=1))");

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
                Iterator it = listTblIndentMaster.iterator();
                while (it.hasNext()) {
                    TblIndentMaster indentMast = (TblIndentMaster) it.next();
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

                cell1.setCellValue("Indent No.");
                cell2.setCellValue("Year");
                cell3.setCellValue("Indent Date");
                cell4.setCellValue("Employee Id");
                cell5.setCellValue("Employee Name");
                cell6.setCellValue("Company");
                cell7.setCellValue("Department");
                cell8.setCellValue("Section");
                cell9.setCellValue("Plant");
                cell10.setCellValue("Indent Comments");
                cell11.setCellValue("Indent Status");

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
                i = 5;

                Iterator it1 = listTblIndentMaster.iterator();
                while (it1.hasNext()) {
                    TblIndentMaster indentMaster = (TblIndentMaster) it1.next();

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

                    try {
                        cell1.setCellValue(indentMaster.getIndentNo());
                        cell2.setCellValue(indentMaster.getIndentYear());
                        cell3.setCellValue(utils.getDateFormat3(indentMaster.getIndentDate()));
                        cell4.setCellValue(indentMaster.getTblEmpMasterByIndentCreatedby().getEmpId());
                        cell5.setCellValue(indentMaster.getTblEmpMasterByIndentCreatedby().getEmpName());
                        cell6.setCellValue(indentMaster.getTblCompanyMaster().getCompName());
                        cell7.setCellValue(indentMaster.getTblDepartmentMaster().getDeptName());
                        cell8.setCellValue(indentMaster.getTblSectionMaster().getSecName());
                        cell9.setCellValue(indentMaster.getTblPlantMaster().getPlantName());
                        cell10.setCellValue(indentMaster.getIndentComments());
                        if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("Pending");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("RM Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("RM Approved");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==2){
                            cell11.setCellValue("Dept. Head Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==2){
                            cell11.setCellValue("Dept. Head Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==4){
                            cell11.setCellValue("Dept. Head Approved");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==5){
                            cell11.setCellValue("Quotations Collected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==6){
                            cell11.setCellValue("Negotiation Done");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==7){
                            cell11.setCellValue("PO Released");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==8){
                            cell11.setCellValue("Hold");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==9){
                            cell11.setCellValue("Cash Buy");
                        }
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

            FileOutputStream fileOut = new FileOutputStream(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                hwb.write(fileOut);
                hwb.write(baos);
                bis = new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException e) {
            }
            fileOut.close();
        } catch (Exception e) {
        }

        return bis;
    }
    
    public ByteArrayInputStream ProcurementReport(int empNumber) throws FileNotFoundException, IOException {
        ByteArrayInputStream bis = null;
        String filename = "ProcurementReport(" + utils.DateIn() + ").xls";
        try {
            listTblIndentMaster = (ArrayList<TblIndentMaster>) indentDao.getList("where indentStatus=1 and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 and tblIndentStatusByIndentFinalStatus.indentStatusId=4 and tblIndentStatusByIndentProcurementStatus.indentStatusId in (4,5,6,7,8,9)");

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
                Iterator it = listTblIndentMaster.iterator();
                while (it.hasNext()) {
                    TblIndentMaster indentMast = (TblIndentMaster) it.next();
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

                cell1.setCellValue("Indent No.");
                cell2.setCellValue("Year");
                cell3.setCellValue("Indent Date");
                cell4.setCellValue("Employee Id");
                cell5.setCellValue("Employee Name");
                cell6.setCellValue("Company");
                cell7.setCellValue("Department");
                cell8.setCellValue("Section");
                cell9.setCellValue("Plant");
                cell10.setCellValue("Indent Comments");
                cell11.setCellValue("Indent Status");

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
                i = 5;

                Iterator it1 = listTblIndentMaster.iterator();
                while (it1.hasNext()) {
                    TblIndentMaster indentMaster = (TblIndentMaster) it1.next();

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

                    try {
                        cell1.setCellValue(indentMaster.getIndentNo());
                        cell2.setCellValue(indentMaster.getIndentYear());
                        cell3.setCellValue(utils.getDateFormat3(indentMaster.getIndentDate()));
                        cell4.setCellValue(indentMaster.getTblEmpMasterByIndentCreatedby().getEmpId());
                        cell5.setCellValue(indentMaster.getTblEmpMasterByIndentCreatedby().getEmpName());
                        cell6.setCellValue(indentMaster.getTblCompanyMaster().getCompName());
                        cell7.setCellValue(indentMaster.getTblDepartmentMaster().getDeptName());
                        cell8.setCellValue(indentMaster.getTblSectionMaster().getSecName());
                        cell9.setCellValue(indentMaster.getTblPlantMaster().getPlantName());
                        cell10.setCellValue(indentMaster.getIndentComments());
                        if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("Pending");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("RM Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==1 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==1){
                            cell11.setCellValue("RM Approved");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==2){
                            cell11.setCellValue("Dept. Head Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==2 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==2){
                            cell11.setCellValue("Dept. Head Rejected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==4){
                            cell11.setCellValue("Dept. Head Approved");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==5){
                            cell11.setCellValue("Quotations Collected");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==6){
                            cell11.setCellValue("Negotiation Done");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==7){
                            cell11.setCellValue("PO Released");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==8){
                            cell11.setCellValue("Hold");
                        }else if(indentMaster.getTblIndentStatusByIndentApprovedStatus().getIndentStatusId()==3 && indentMaster.getTblIndentStatusByIndentFinalStatus().getIndentStatusId()==4 && indentMaster.getTblIndentStatusByIndentProcurementStatus().getIndentStatusId()==9){
                            cell11.setCellValue("Cash Buy");
                        }
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

            FileOutputStream fileOut = new FileOutputStream(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                hwb.write(fileOut);
                hwb.write(baos);
                bis = new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException e) {
            }
            fileOut.close();
        } catch (Exception e) {
        }

        return bis;
    }
}
