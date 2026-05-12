/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.issue.mailService;

/**
 *
 * @author ramesh.avv
 */
import com.opensymphony.xwork2.Action;
import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;
import pojo.TblMaterialMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.MaterialDaoImpl;

public class SapCsvImport2 implements Action{

    private final Utils utils = new Utils();  
    private TblMaterialMaster tblMaterialMaster;
    private List<TblMaterialMaster> listTblMaterialMaster;
    private final MaterialDaoImpl materialDao = DaoFactory.getDao(MaterialDaoImpl.class);
   
    private InputStream inputStream;
    private String file;

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

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public SapCsvImport2() throws Exception {
        tblMaterialMaster = new TblMaterialMaster();
        listTblMaterialMaster = new ArrayList<TblMaterialMaster>();        
    }

    public void InsertMaterialQuantityDetails() {
        try {
            FileInputStream input = new FileInputStream(new File("E:\\MaterialMaster.xls"));
            //POIFSFileSystem fs = new POIFSFileSystem(input);
            HSSFWorkbook wb = new HSSFWorkbook(input);
            HSSFSheet sheet = wb.getSheetAt(0);
             
            HSSFRow row;
          
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {               
                tblMaterialMaster = new TblMaterialMaster();               
                row = sheet.getRow(i);
                tblMaterialMaster = (TblMaterialMaster) materialDao.getList("where materialStatus=1 and materialCode='" + row.getCell(0) + "' and materialDesc='" + row.getCell(2) + "'").get(0);
                //tblMaterialMaster.setMaterialQuantityStores(new BigDecimal(row.getCell(3).toString()));
                materialDao.save(tblMaterialMaster);
                
                System.out.println("Imported rows " + i);
            }

            input.close();
            System.out.println("Successfully imported excel Data to Material Quantity mysql table");
        } catch (IOException e) {
        }
    }
    
    public void MaterialQuantityDetailsReport() throws FileNotFoundException, IOException {
        ByteArrayInputStream bis = null;
        //String filename = "./uploads/Excel/MaterialQuantityDetails(" + utils.DateIn() + ").xls";
        String filename = "E:/MaterialQuantityDetails.xls";
        try {
            listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1");
            HSSFWorkbook hwb = new HSSFWorkbook();
            int i = 1;
            HSSFSheet sheet = hwb.createSheet("MaterialQuantityDetails");
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
                Iterator it = listTblMaterialMaster.iterator();
                while (it.hasNext()) {
                    TblMaterialMaster Material = (TblMaterialMaster) it.next();              
                    sheet.addMergedRegion(new Region(1, (short) 0, 1, (short) 30));
                    sheet.addMergedRegion(new Region(2, (short) 0, 2, (short) 30));
                    sheet.addMergedRegion(new Region(3, (short) 0, 3, (short) 30));
                    cell.setCellValue("Material Quantity Details");
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

                cell1.setCellValue("Material Code");
                cell2.setCellValue("Material Name");
                cell3.setCellValue("Material Description");
                cell4.setCellValue("Quantity in Stores");

                cell1.setCellStyle(TableHdr);
                cell2.setCellStyle(TableHdr);
                cell3.setCellStyle(TableHdr);
                cell4.setCellStyle(TableHdr);
                
                i = 5;
                Iterator it1 = listTblMaterialMaster.iterator();
                while (it1.hasNext()) {
                    TblMaterialMaster Material = (TblMaterialMaster) it1.next();
                    row = sheet.createRow(i);
                    cell1 = row.createCell((short) 0);
                    cell2 = row.createCell((short) 1);
                    cell3 = row.createCell((short) 2);
                    cell4 = row.createCell((short) 3);

                    try {
                        cell1.setCellValue(Material.getMaterialCode());
                        cell2.setCellValue(Material.getMaterialName());
                        cell3.setCellValue(Material.getMaterialDesc());
                        //cell4.setCellValue(Material.getMaterialQuantityStores().toString());
                        
                        
                    } catch (NullPointerException e) {
                    }

                    cell1.setCellStyle(TableData1);
                    cell2.setCellStyle(TableData1);
                    cell3.setCellStyle(TableData1);
                    cell4.setCellStyle(TableData1);

                    i++;
                }
            }
            
            sheet.autoSizeColumn((short) 0);
            sheet.autoSizeColumn((short) 1);
            sheet.autoSizeColumn((short) 2);
            sheet.autoSizeColumn((short) 3);
            sheet.autoSizeColumn((short) 4);

            FileOutputStream fileOut = new FileOutputStream(filename);
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                hwb.write(fileOut);
                //hwb.write(baos);
                //bis = new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException e) {
            }
            fileOut.close();
        } catch (Exception e) {
        }

        //return bis;
    }    

    @Override
    public String execute() throws Exception {
         try {
                //inputStream = MaterialQuantityDetailsReport();
                file = "MaterialQuantityDetails(" + utils.DateIn() + ").xls";
                setFile("MaterialQuantityDetails(" + utils.DateIn() + ").xls");
            } catch (Exception e) {
                return INPUT;
            }
         return SUCCESS;
    }
    
    public static void main(String[] args) throws Exception {
        SapCsvImport2 sap = new SapCsvImport2();
        sap.execute();
    }
    
}
