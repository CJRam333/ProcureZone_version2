/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.sap.cronScheduler;

/**
 *
 * @author ramesh.avv
 */
import com.opensymphony.xwork2.Action;
import com.sun.xml.internal.ws.wsdl.DispatchException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import javax.mail.MessagingException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;
import plant.indent.action.CompPzPlantMaterialDaoImpl;
import plant.indent.action.PzCompanyDaoImpl;
import plant.indent.action.PzSchedulematerialMasterImpl;
import pojo.TblCompanyMaster;
import pojo.TblMapCompanyPlantMaterial;
import pojo.TblMaterialMaster;
import pojo.TblPlantMaster;
import pojo.TblPzCompanyMaster;
import pojo.TblPzMapCompanyPlantMaterial;
import pojo.TblPzScheduleMaterialmaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.CompPlantMaterialDaoImpl;
import seeds.masters.daoImpl.CompanyDaoImpl;
import seeds.masters.daoImpl.MaterialDaoImpl;
import seeds.masters.daoImpl.PlantDaoImpl;

public class SapCsvImport implements Action {

    private final Utils utils = new Utils();
    private TblPzScheduleMaterialmaster tblMaterialMaster;
    private List<TblPzScheduleMaterialmaster> listTblMaterialMaster;
    private final MaterialDaoImpl materialDao = DaoFactory.getDao(MaterialDaoImpl.class);
    private TblMapCompanyPlantMaterial tblMapCompanyPlantMaterial;
    private List<TblMapCompanyPlantMaterial> listTblMapCompanyPlantMaterial;
    private final CompPlantMaterialDaoImpl compPlantMaterialDao = DaoFactory.getDao(CompPlantMaterialDaoImpl.class);
    private TblPzCompanyMaster tblCompanyMaster;
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private final PzCompanyDaoImpl pzcompanyDao = DaoFactory.getDao(PzCompanyDaoImpl.class);
    
    private TblPlantMaster tblPlantMaster;
    private List<TblPlantMaster> listTblPlantMaster;
    private final PlantDaoImpl plantDao = DaoFactory.getDao(PlantDaoImpl.class);
    private InputStream inputStream;
    private String file;
     private TblPzScheduleMaterialmaster tblPzScheduleMaterialmaster;
     private TblPzMapCompanyPlantMaterial tblPzcompanyplantMaterialmaster;
    private List<TblPzScheduleMaterialmaster> listPzTblPzScheduleMaterialmaster;
    
    private List<TblPzScheduleMaterialmaster> listPzTblPzScheduleMaterialmasterBatch;
    private final PzSchedulematerialMasterImpl pzschedulematerialDao = DaoFactory.getDao(PzSchedulematerialMasterImpl.class);
    private final CompPzPlantMaterialDaoImpl compPzPlantMaterialDao = DaoFactory.getDao(CompPzPlantMaterialDaoImpl.class);
    private final CompPzPlantMatcronDaoImpl compPzPlantMaterialcronDao = DaoFactory.getDao(CompPzPlantMatcronDaoImpl.class);
    
    
    
                private String companyId1;
                private String companyCode1;
                private String plantCode1;
                private String storageLocation1;
                private String materialaCode1;
                private String materialDesc1;
                private String materiaaUom1;
                private String matBatch1;
                private String matQuantity1;
                private String materialType1;
                private String materialGroup1;
                private String matgroupDesc1;
                private String varietyType1;
                private String matVariety1;
                private String cropType1;
                private String cropGroup1;
                private String matStl1;
                private String matOdv1;
                private String matGot1;
                private String matElisa1;
                private String matcodeId1;
    

    public TblPzMapCompanyPlantMaterial getTblPzcompanyplantMaterialmaster() {
        return tblPzcompanyplantMaterialmaster;
    }

    public void setTblPzcompanyplantMaterialmaster(TblPzMapCompanyPlantMaterial tblPzcompanyplantMaterialmaster) {
        this.tblPzcompanyplantMaterialmaster = tblPzcompanyplantMaterialmaster;
    }
    

    public TblPzScheduleMaterialmaster getTblPzScheduleMaterialmaster() {
        return tblPzScheduleMaterialmaster;
    }

    public void setTblPzScheduleMaterialmaster(TblPzScheduleMaterialmaster tblPzScheduleMaterialmaster) {
        this.tblPzScheduleMaterialmaster = tblPzScheduleMaterialmaster;
    }

    public List<TblPzScheduleMaterialmaster> getListPzTblPzScheduleMaterialmaster() {
        return listPzTblPzScheduleMaterialmaster;
    }

    public void setListPzTblPzScheduleMaterialmaster(List<TblPzScheduleMaterialmaster> listPzTblPzScheduleMaterialmaster) {
        this.listPzTblPzScheduleMaterialmaster = listPzTblPzScheduleMaterialmaster;
    }

    public List<TblPzScheduleMaterialmaster> getListPzTblPzScheduleMaterialmasterBatch() {
        return listPzTblPzScheduleMaterialmasterBatch;
    }

    public void setListPzTblPzScheduleMaterialmasterBatch(List<TblPzScheduleMaterialmaster> listPzTblPzScheduleMaterialmasterBatch) {
        this.listPzTblPzScheduleMaterialmasterBatch = listPzTblPzScheduleMaterialmasterBatch;
    }

    public TblPzScheduleMaterialmaster getTblMaterialMaster() {
        return tblMaterialMaster;
    }

    public void setTblMaterialMaster(TblPzScheduleMaterialmaster tblMaterialMaster) {
        this.tblMaterialMaster = tblMaterialMaster;
    }
    
    
    

    

    public List<TblPzScheduleMaterialmaster> getListTblMaterialMaster() {
        return listTblMaterialMaster;
    }

    public void setListTblMaterialMaster(List<TblPzScheduleMaterialmaster> listTblMaterialMaster) {
        this.listTblMaterialMaster = listTblMaterialMaster;
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

    public TblPzCompanyMaster getTblCompanyMaster() {
        return tblCompanyMaster;
    }

    public void setTblCompanyMaster(TblPzCompanyMaster tblCompanyMaster) {
        this.tblCompanyMaster = tblCompanyMaster;
    }

    

    

    

    public List<TblCompanyMaster> getListTblCompanyMaster() {
        return listTblCompanyMaster;
    }

    public void setListTblCompanyMaster(List<TblCompanyMaster> listTblCompanyMaster) {
        this.listTblCompanyMaster = listTblCompanyMaster;
    }

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

    public String getCompanyId1() {
        return companyId1;
    }

    public void setCompanyId1(String companyId1) {
        this.companyId1 = companyId1;
    }

    public String getCompanyCode1() {
        return companyCode1;
    }

    public void setCompanyCode1(String companyCode1) {
        this.companyCode1 = companyCode1;
    }

    public String getPlantCode1() {
        return plantCode1;
    }

    public void setPlantCode1(String plantCode1) {
        this.plantCode1 = plantCode1;
    }

    public String getStorageLocation1() {
        return storageLocation1;
    }

    public void setStorageLocation1(String storageLocation1) {
        this.storageLocation1 = storageLocation1;
    }

    public String getMaterialaCode1() {
        return materialaCode1;
    }

    public void setMaterialaCode1(String materialaCode1) {
        this.materialaCode1 = materialaCode1;
    }

    public String getMaterialDesc1() {
        return materialDesc1;
    }

    public void setMaterialDesc1(String materialDesc1) {
        this.materialDesc1 = materialDesc1;
    }

    public String getMateriaaUom1() {
        return materiaaUom1;
    }

    public void setMateriaaUom1(String materiaaUom1) {
        this.materiaaUom1 = materiaaUom1;
    }

    public String getMatBatch1() {
        return matBatch1;
    }

    public void setMatBatch1(String matBatch1) {
        this.matBatch1 = matBatch1;
    }

    public String getMatQuantity1() {
        return matQuantity1;
    }

    public void setMatQuantity1(String matQuantity1) {
        this.matQuantity1 = matQuantity1;
    }

    public String getMaterialType1() {
        return materialType1;
    }

    public void setMaterialType1(String materialType1) {
        this.materialType1 = materialType1;
    }

    public String getMaterialGroup1() {
        return materialGroup1;
    }

    public void setMaterialGroup1(String materialGroup1) {
        this.materialGroup1 = materialGroup1;
    }

    public String getMatgroupDesc1() {
        return matgroupDesc1;
    }

    public void setMatgroupDesc1(String matgroupDesc1) {
        this.matgroupDesc1 = matgroupDesc1;
    }

    public String getVarietyType1() {
        return varietyType1;
    }

    public void setVarietyType1(String varietyType1) {
        this.varietyType1 = varietyType1;
    }

    public String getMatVariety1() {
        return matVariety1;
    }

    public void setMatVariety1(String matVariety1) {
        this.matVariety1 = matVariety1;
    }

    public String getCropType1() {
        return cropType1;
    }

    public void setCropType1(String cropType1) {
        this.cropType1 = cropType1;
    }

    public String getCropGroup1() {
        return cropGroup1;
    }

    public void setCropGroup1(String cropGroup1) {
        this.cropGroup1 = cropGroup1;
    }

    public String getMatStl1() {
        return matStl1;
    }

    public void setMatStl1(String matStl1) {
        this.matStl1 = matStl1;
    }

    public String getMatOdv1() {
        return matOdv1;
    }

    public void setMatOdv1(String matOdv1) {
        this.matOdv1 = matOdv1;
    }

    public String getMatGot1() {
        return matGot1;
    }

    public void setMatGot1(String matGot1) {
        this.matGot1 = matGot1;
    }

    public String getMatElisa1() {
        return matElisa1;
    }

    public void setMatElisa1(String matElisa1) {
        this.matElisa1 = matElisa1;
    }

    public String getMatcodeId1() {
        return matcodeId1;
    }

    public void setMatcodeId1(String matcodeId1) {
        this.matcodeId1 = matcodeId1;
    }

    
    
    
    
    

    public SapCsvImport() throws Exception {
        tblMaterialMaster = new TblPzScheduleMaterialmaster();
        listTblMaterialMaster = new ArrayList<TblPzScheduleMaterialmaster>();
        tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
        listTblMapCompanyPlantMaterial = new ArrayList<TblMapCompanyPlantMaterial>();
        tblCompanyMaster = new TblPzCompanyMaster();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
        tblPlantMaster = new TblPlantMaster();
        listTblPlantMaster = new ArrayList<TblPlantMaster>();
    }

    public void InsertCronMaterialsUpdated() throws ParseException, MessagingException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException ,DispatchException {
        try {           
            //File destinationFile = new File("E:\\Material("+utils.DateIn()+").csv");
            //BufferedReader bf = new BufferedReader(new FileReader(destinationFile));

            //File destinationFile = new File("/opt/tomcat7/webapps/ProcureZone/uploads/issue/Material("+utils.DateIn()+").csv");
            //String destinationFile = "/opt/tomcat7/webapps/Procure/uploads/issue/Material.CSV";
            //File file = new File(destinationFile).getAbsoluteFile();
            //file.getParentFile().mkdirs(); 
            //System.out.println("rama "+file.getPath());
//            BufferedReader bf = new BufferedReader(new FileReader("/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/Material(" + utils.DateIn() + ").CSV"));
            //BufferedReader bf = new BufferedReader(new FileReader("/opt/tomcat/apache-tomcat-8.5.57/webapps/ProcureZone/uploads/issue/Material(" + utils.DateIn() + ").CSV"));
            BufferedReader bf = new BufferedReader(new FileReader("E:\\Plant_Indent(" + utils.DateIn() + ").CSV"));
            String line = null;
            Scanner scanner = null;
            int index = 0;
            while ((line = bf.readLine()) != null) {
                scanner = new Scanner(line);
                scanner.useDelimiter(",");
                while (scanner.hasNext()) {
                    String[] data = scanner.next().split(line);
                      tblMapCompanyPlantMaterial.setMapId(1);
                            compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
                            //}
                   
                    index++;
                }
                index = 0;
            }
            bf.close();
        } catch (IOException ex) {
        }
    }
    
    
    

    public void InsertMaterialQuantityDetails1() {
        try {
            FileInputStream input = new FileInputStream(new File("E:\\Plant_Indent(" + utils.DateIn() + ").CSV"));
            HSSFWorkbook wb = new HSSFWorkbook(input);
            HSSFSheet sheet = wb.getSheetAt(0);

            HSSFRow row;
            System.out.println("Reading file");
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {

//                tblCompanyMaster = new TblPzCompanyMaster();
//                tblPlantMaster = new TblPlantMaster();
//                tblMaterialMaster = new TblPzScheduleMaterialmaster();
//                tblPzcompanyplantMaterialmaster = new TblPzMapCompanyPlantMaterial();

                row = sheet.getRow(i);
                
                System.out.println("row1"+row.getCell(0));
//                tblCompanyMaster = (TblPzCompanyMaster) companyDao.getList("where compStatus=1 and compCode='" + row.getCell(1) + "'").get(0);
//                tblPlantMaster = (TblPlantMaster) plantDao.getList("where plantStatus=1 and plantCode='" + row.getCell(2) + "'").get(0);
//                tblMaterialMaster = (TblPzScheduleMaterialmaster) materialDao.getList("where matStatus=1 and materialaCode='" + row.getCell(4) + "' and materialDesc='" + row.getCell(5) + "'").get(0);
//                tblPzcompanyplantMaterialmaster = (TblPzMapCompanyPlantMaterial) compPzPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + tblCompanyMaster.getCompId() + " and tblPlantMaster.plantId=" + tblPlantMaster.getPlantId() + " and tblMaterialMaster.materialId=" + tblMaterialMaster.getCompanyId() + "").get(0);
//                tblPzcompanyplantMaterialmaster.setMapQuantityStores(new BigDecimal(row.getCell(8).toString()));
//                
//                
//                tblPzScheduleMaterialmaster.setCompanyCode(i);
//                
//                
//                pzschedulematerialDao.save(tblPzScheduleMaterialmaster);
                
                

                System.out.println("Imported rogws " + i);
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
            listTblMapCompanyPlantMaterial = (ArrayList<TblMapCompanyPlantMaterial>) compPlantMaterialDao.getList("where mapStatus=1");
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
                for (TblMapCompanyPlantMaterial MapCompanyPlantMaterial : listTblMapCompanyPlantMaterial) {
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
                HSSFCell cell5 = row.createCell((short) 4);

                cell1.setCellValue("Company Code");
                cell2.setCellValue("Plant Code");
                cell3.setCellValue("Material Code");
                cell4.setCellValue("Material Description");
                cell5.setCellValue("Quantity in Stores");

                cell1.setCellStyle(TableHdr);
                cell2.setCellStyle(TableHdr);
                cell3.setCellStyle(TableHdr);
                cell4.setCellStyle(TableHdr);
                cell5.setCellStyle(TableHdr);

                i = 5;
                for (TblMapCompanyPlantMaterial MapCompanyPlantMaterial : listTblMapCompanyPlantMaterial) {
                    row = sheet.createRow(i);
                    cell1 = row.createCell((short) 0);
                    cell2 = row.createCell((short) 1);
                    cell3 = row.createCell((short) 2);
                    cell4 = row.createCell((short) 3);
                    cell5 = row.createCell((short) 4);

                    try {
                        cell1.setCellValue(MapCompanyPlantMaterial.getTblCompanyMaster().getCompCode());
                        cell2.setCellValue(MapCompanyPlantMaterial.getTblPlantMaster().getPlantCode());
                        cell3.setCellValue(MapCompanyPlantMaterial.getTblMaterialMaster().getMaterialCode());
                        cell4.setCellValue(MapCompanyPlantMaterial.getTblMaterialMaster().getMaterialDesc());
                        cell5.setCellValue(MapCompanyPlantMaterial.getMapQuantityStores().toString());

                    } catch (NullPointerException e) {
                    }

                    cell1.setCellStyle(TableData1);
                    cell2.setCellStyle(TableData1);
                    cell3.setCellStyle(TableData1);
                    cell4.setCellStyle(TableData1);
                    cell5.setCellStyle(TableData1);

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
        } catch (IOException e) {
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
    
    
    
    public void ExcelToDatabase() throws FileNotFoundException, IOException{
        
        mergeCSVFiles();
        
        // Connection parameters for your SQL database
        String url = "jdbc:mysql://localhost:3306/seeds_indent";
        String user = "root";
//        String password = "ezone160@172169160";
        String password = "password";
// Set read, write, and execute permissions for the folder
        
        // Path to the Excel file to be read
//        String excelFilePath = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/Plant_Indent(" + utils.DateIn() + ").CSV";
//        String excelFilePath = "E:/Plant_Indent(" + utils.DateIn() + ").csv";
//            String excelFilePath = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/merged(" + utils.DateIn() + ").csv";
        String excelFilePath = "E:/merged(" + utils.DateIn() + ").csv";
         // Set read, write, and execute permissions for the folder
        setFolderPermissions(excelFilePath, true, true, true);

        try {
            // Create a JDBC connection to the database
            Connection conn = DriverManager.getConnection(url, user, password);
            
            Statement statement1 = conn.createStatement();
            String truncateSql = "TRUNCATE TABLE pz_schedule_sap_material_master";
            statement1.executeUpdate(truncateSql);

            // Create a SQL statement to insert data into the database
            

            
            String sql = "INSERT INTO pz_schedule_sap_material_master (company_code, plant_code, storage_location,material_code,"
                    + "material_desc,material_uom,batch,quantity,material_type,"
                    + "material_group,material_group_desc,variety_type,variety,"
                    + "crop_type,crop_group,stl,odv,got,elisa,status,"
                    + "mat_code_id,SDCLS,STATS,SKIPD,"
                    + "INSPDT,MOISTURE,PURE_SEED,INERT_MATTER,OCS_COUNT,WEED_SEED_COUNT,GRAIN,"
                    + "BLACK_SEEDS,PINHOLE_SEEDS,ODV_RES,BULK_DENSITY,THSW,COLD_VIGOUR_GERM_NORMAL,"
                    + "FIRST_COUNT_NORMAL,GERM_NORMAL,FET_NORMAL,SOIL_COUNT_DAYS,AAV_GERM_NORMAL,"
                    + "GOT_GP,GOT_FEMALE,GOT_OTHERS,BG1,BG2,HT,FQR,"
                    + "stp_one,lmd) VALUES (?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,"
                    + "?,?)";
            
            
            
            PreparedStatement statement = conn.prepareStatement(sql);

            // Open the CSV file for reading
            BufferedReader reader = new BufferedReader(new FileReader(excelFilePath));

            // Loop through each line in the CSV file
            String line;
            while ((line = reader.readLine()) != null) {
                
                
                // Parse the line into separate fields
                String[] fields = line.split(",");
                
            
//                // Get the values from the fields
                int col1 = Integer.parseInt(fields[0]);
                int col2 = Integer.parseInt(fields[1]);
                String  col3 = fields[2];
                String  col4 = fields[3];
                String  col5 = fields[4];
                String  col6 = fields[5];
                String  col7 = fields[6];
                Double  col8 =  Double.valueOf(fields[7]);
                String  col9 =  fields[8];
                int  col10 = Integer.parseInt(fields[9]);
                String  col11 = fields[10];
                String  col12 = fields[11];
                String  col13 = fields[12];
                String  col14 = fields[13];
                String  col15 = fields[14];
                String  col16 = fields[15];
                String  col17 = fields[16];
                String  col18 = fields[17];
                String  col19 = fields[18];
                int col20 = 1;
                int col21 = Integer.parseInt(fields[9]);
                String  col22 = fields.length > 19 && fields[19] != null && !fields[19].isEmpty() ? fields[19] : null;
                String  col23 = fields.length > 20 && fields[20] != null && !fields[20].isEmpty() ? fields[20] : null;
                String  col24 = fields.length > 21 && fields[21] != null && !fields[21].isEmpty() ? fields[21] : null;
                String  col25 = fields.length > 26 && fields[26] != null && !fields[26].isEmpty() ? fields[26] : null;
                String  col26 = fields.length > 27 && fields[27] != null && !fields[27].isEmpty() ? fields[27] : null;
                String  col27 = fields.length > 28 && fields[28] != null && !fields[28].isEmpty() ? fields[28] : null;
                String  col28 = fields.length > 29 && fields[29] != null && !fields[29].isEmpty() ? fields[29] : null;
                String  col29 = fields.length > 30 && fields[30] != null && !fields[30].isEmpty() ? fields[30] : null;
                String  col30 = fields.length > 31 && fields[31] != null && !fields[31].isEmpty() ? fields[31] : null;
                String  col31 = fields.length > 32 && fields[32] != null && !fields[32].isEmpty() ? fields[32] : null;
                String  col32 = fields.length > 33 && fields[33] != null && !fields[33].isEmpty() ? fields[33] : null;
                String  col33 = fields.length > 34 && fields[34] != null && !fields[34].isEmpty() ? fields[34] : null;
                String  col34 = fields.length > 35 && fields[35] != null && !fields[35].isEmpty() ? fields[35] : null;
                String  col35 = fields.length > 36 && fields[36] != null && !fields[36].isEmpty() ? fields[36] : null;
                String  col36 = fields.length > 37 && fields[37] != null && !fields[37].isEmpty() ? fields[37] : null;
                String  col37 = fields.length > 38 && fields[38] != null && !fields[38].isEmpty() ? fields[38] : null;
                String  col38 = fields.length > 39 && fields[39] != null && !fields[39].isEmpty() ? fields[39] : null;
                String  col39 = fields.length > 40 && fields[40] != null && !fields[40].isEmpty() ? fields[40] : null;
                String  col40 = fields.length > 41 && fields[41] != null && !fields[41].isEmpty() ? fields[41] : null;
                String  col41 = fields.length > 42 && fields[42] != null && !fields[42].isEmpty() ? fields[42] : null;
                String  col42 = fields.length > 43 && fields[43] != null && !fields[43].isEmpty() ? fields[43] : null;
                
                String  col43 = fields.length > 44 && fields[44] != null && !fields[44].isEmpty() ? fields[44] : null;
                String  col44 = fields.length > 45 && fields[45] != null && !fields[45].isEmpty() ? fields[45] : null;
                String  col45 = fields.length > 46 && fields[46] != null && !fields[46].isEmpty() ? fields[46] : null;
                String  col46 = fields.length > 47 && fields[47] != null && !fields[47].isEmpty() ? fields[47] : null;
                String  col47 = fields.length > 48 && fields[48] != null && !fields[48].isEmpty() ? fields[48] : null;
                String  col48 = fields.length > 45 && fields[45] != null && !fields[45].isEmpty() ? fields[45] : null;
                String  col49 = fields.length > 46 && fields[46] != null && !fields[46].isEmpty() ? fields[46] : null;
                
                String  col50 = fields.length > 47 && fields[47] != null && !fields[47].isEmpty() ? fields[47] : null;
                String  col51 = utils.DateIn()+" -- "+utils.TimeIn();
//                String  col60 = fields[57];
//                String  col61 = fields[58];

//                // Set the values in the SQL statement
                statement.setInt(1, col1);
                statement.setInt(2, col2);
                statement.setString(3, col3);
                statement.setString(4, col4);
                statement.setString(5, col5);
                statement.setString(6, col6);
                statement.setString(7, col7);
                statement.setDouble(8, col8);
                statement.setString(9, col9);
                statement.setInt(10, col10);
                statement.setString(11, col11);
                statement.setString(12, col12);
                statement.setString(13, col13);
                statement.setString(14, col14);
                statement.setString(15, col15);
                statement.setString(16, col16);
                statement.setString(17, col17);
                statement.setString(18, col18);
                statement.setString(19, col19);
                statement.setInt(20, col20);
                statement.setInt(21, col21);
                statement.setString(22, col22);
                statement.setString(23, col23);
                statement.setString(24, col24);
                statement.setString(25, col25);
                statement.setString(26, col26);
                statement.setString(27, col27);
                statement.setString(28, col28);
                statement.setString(29, col29);
                statement.setString(30, col30);
                statement.setString(31, col31);
                statement.setString(32, col32);
                statement.setString(33, col33);
                statement.setString(34, col34);
                statement.setString(35, col35);
                statement.setString(36, col36);
                statement.setString(37, col37);
                statement.setString(38, col38);
                statement.setString(39, col39);
                statement.setString(40, col40);
                statement.setString(41, col41);
                statement.setString(42, col42);
                
                statement.setString(43, col43);
                statement.setString(44, col44);
                statement.setString(45, col45);
                statement.setString(46, col46);
                statement.setString(47, col47);
                statement.setString(48, col48);
                statement.setString(49, col49);
                statement.setString(50, col50);
                statement.setString(51, col51);
//                
                

                // Execute the SQL statement to insert the row
                statement.executeUpdate();
            }

            // Close the JDBC connection and the CSV file reader
            statement.close();
            conn.close();
            reader.close();

            System.out.println("Data imported successfully.");

        } catch (SQLException e) {
            
//            e.printStackTrace();
        }
    
    
    }
    
    public String mergeCSVFiles() {
        
        
        String csvFile1 = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/Plant_Indent(" + utils.DateIn() + ").csv";
        String csvFile2 = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/Quality_Info(" + utils.DateIn() + ").csv";
        String mergedFile = "/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/merged(" + utils.DateIn() + ").csv";

        FileReader fileReader1 = null;
        FileReader fileReader2 = null;
        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;

        try {
            fileReader1 = new FileReader(csvFile1);
            fileReader2 = new FileReader(csvFile2);
            fileWriter = new FileWriter(mergedFile);
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

            Iterable<CSVRecord> records1 = CSVFormat.DEFAULT.parse(fileReader1);
            Iterable<CSVRecord> records2 = CSVFormat.DEFAULT.parse(fileReader2);

            // Read data from the first CSV file
            List<String[]> data1 = new ArrayList<String[]>();
            for (CSVRecord record : records1) {
                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    row[i] = record.get(i);
                }
                data1.add(row);
            }

            // Read data from the second CSV file
            List<String[]> data2 = new ArrayList<String[]>();
            for (CSVRecord record : records2) {
                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    row[i] = record.get(i);
                }
                data2.add(row);
            }

            // Merge the data side-by-side
            List<String[]> mergedData = new ArrayList<String[]>();
            Iterator<String[]> iterator1 = data1.iterator();
            Iterator<String[]> iterator2 = data2.iterator();
            while (iterator1.hasNext() && iterator2.hasNext()) {
                String[] row1 = iterator1.next();
                String[] row2 = iterator2.next();
                String[] mergedRow = new String[row1.length + row2.length];
                System.arraycopy(row1, 0, mergedRow, 0, row1.length);
                System.arraycopy(row2, 0, mergedRow, row1.length, row2.length);
                mergedData.add(mergedRow);
            }

            // Write merged data to the new CSV file
            for (String[] row : mergedData) {
                csvPrinter.printRecord((Object[]) row);
            }

            return "CSV files merged side-by-side successfully!";
        } catch (IOException e) {
            return "Failed to merge CSV files side-by-side.";
        } finally {
            try {
                if (csvPrinter != null) {
                    csvPrinter.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (fileReader2 != null) {
                    fileReader2.close();
                }
                if (fileReader1 != null) {
                    fileReader1.close();
                }
            } catch (IOException e) {
            }
        }
        
    }
    

    public static void main(String[] args) throws Exception {
        SapCsvImport sap = new SapCsvImport();
        sap.ExcelToDatabase();
    }
    
    
    public static void setFolderPermissions(String folderPath, boolean readable, boolean writable, boolean executable) {
        File folder = new File(folderPath);
        
        if (folder.exists()) {
            folder.setReadable(readable);
            folder.setWritable(writable);
            folder.setExecutable(executable);
            System.out.println("Folder permissions set successfully.");
        } else {
            System.out.println("Folder does not exist.");
        }
    }
    
    public static void yourMethodThatUsesFolder(String folderPath) {
        // Your code that requires access to the folder
    }

}
