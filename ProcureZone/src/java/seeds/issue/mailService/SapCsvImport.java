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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;
import pojo.TblCompanyMaster;
import pojo.TblEmpMaster;
import pojo.TblMapCompanyPlantMaterial;
import pojo.TblMaterialMaster;
import pojo.TblPlantMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.CompPlantMaterialDaoImpl;
import seeds.masters.daoImpl.CompanyDaoImpl;
import seeds.masters.daoImpl.MaterialDaoImpl;
import seeds.masters.daoImpl.PlantDaoImpl;

public class SapCsvImport implements Action {

    private final Utils utils = new Utils();
    private TblMaterialMaster tblMaterialMaster;
    private List<TblMaterialMaster> listTblMaterialMaster;
    private final MaterialDaoImpl materialDao = DaoFactory.getDao(MaterialDaoImpl.class);
    private TblMapCompanyPlantMaterial tblMapCompanyPlantMaterial;
    private List<TblMapCompanyPlantMaterial> listTblMapCompanyPlantMaterial;
    private final CompPlantMaterialDaoImpl compPlantMaterialDao = DaoFactory.getDao(CompPlantMaterialDaoImpl.class);
    private TblCompanyMaster tblCompanyMaster;
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private TblPlantMaster tblPlantMaster;
    private List<TblPlantMaster> listTblPlantMaster;
    private final PlantDaoImpl plantDao = DaoFactory.getDao(PlantDaoImpl.class);
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

    public TblCompanyMaster getTblCompanyMaster() {
        return tblCompanyMaster;
    }

    public void setTblCompanyMaster(TblCompanyMaster tblCompanyMaster) {
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

    public SapCsvImport() throws Exception {
        tblMaterialMaster = new TblMaterialMaster();
        listTblMaterialMaster = new ArrayList<TblMaterialMaster>();
        tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
        listTblMapCompanyPlantMaterial = new ArrayList<TblMapCompanyPlantMaterial>();
        tblCompanyMaster = new TblCompanyMaster();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
        tblPlantMaster = new TblPlantMaster();
        listTblPlantMaster = new ArrayList<TblPlantMaster>();
    }

//    public void InsertMaterialQuantityDetails() throws ParseException {
//        try {           
//            //File destinationFile = new File("E:\\Material("+utils.DateIn()+").csv");
//            //BufferedReader bf = new BufferedReader(new FileReader(destinationFile));
//
//            //File destinationFile = new File("/opt/tomcat7/webapps/ProcureZone/uploads/issue/Material("+utils.DateIn()+").csv");
//            //String destinationFile = "/opt/tomcat7/webapps/Procure/uploakkkds/issue/Material.CSV";
//            //File file = new File(destinationFile).getAbsoluteFile();
//            //file.getParentFile().mkdirs(); 
//            //System.out.println("rama "+file.getPath());
////            BufferedReader bf = new BufferedReader(new FileReader("/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issue/Material(" + utils.DateIn() + ").CSV"));
////            BufferedReader bf = new BufferedReader(new FileReader("/opt/tomcat/apache-tomcat-8.5.57/webapps/ProcureZone/uploads/issue/Material(" + utils.DateIn() + ").CSV"));
//                // Initialize mapQuantityStores to 0.00 for all entries before reading data from the CSV file
//        List<TblMapCompanyPlantMaterial> allMapCompanyPlantMaterials = compPlantMaterialDao.getList("where mapStatus = 1"); // Assuming getAll() method fetches all entries
//        for (TblMapCompanyPlantMaterial material : allMapCompanyPlantMaterials) {
//            material.setMapQuantityStores(BigDecimal.ZERO);
//            compPlantMaterialDao.save(material); // Save the initialized value
//            System.out.println("ESTEABLISDE 0.00");
//        }
//
//                
//            BufferedReader bf = new BufferedReader(new FileReader("E:\\Material(" + utils.DateIn() + ").CSV"));
//            String line = null;
//            Scanner scanner = null;
//            int index = 0;
//            while ((line = bf.readLine()) != null) {
//                scanner = new Scanner(line);
//                scanner.useDelimiter(",");
//                try {
//                    
//                    while (scanner.hasNext()) {
//                    String data = scanner.next();
//                    switch (index) {
//                        case 0:
//                            tblCompanyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compCode='" + data + "'").get(0);
//                            break;
//                        case 1:
//                            tblPlantMaster = (TblPlantMaster) plantDao.getList("where plantStatus=1 and plantCode='" + data + "'").get(0);
//                            break;
//                        case 2:
//                            listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialCode='" + data + "'");
//                            if (!listTblMaterialMaster.isEmpty()) {
//                                tblMaterialMaster = (TblMaterialMaster) materialDao.getList("where materialStatus=1 and materialCode='" + data + "'").get(0);
//                            }   System.out.println("data " + data );
//                            break;
//                        case 4:
//                            if (!listTblMaterialMaster.isEmpty()) {
//                                tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + tblCompanyMaster.getCompId() + " and tblPlantMaster.plantId=" + tblPlantMaster.getPlantId() + " and tblMaterialMaster.materialId=" + tblMaterialMaster.getMaterialId() + "").get(0);
//                                //if (!tblMapCompanyPlantMaterial.getMapId().toString() .isEmpty() && tblMapCompanyPlantMaterial.getMapId().toString() != null) {
//                                tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
//                                //String str=data.substring(0, data.length()-1);
//                                Double d = new Double(data);
//                                BigDecimal b = BigDecimal.valueOf(d);
//                                tblMapCompanyPlantMaterial.setMapQuantityStores(b);
//                                System.out.println("Id " + tblMapCompanyPlantMaterial.getMapId() + " qty " + b);
//                                compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
//                                //}
//                            }   break;
//                        default:
//                            break;
//                    }
//                    index++;
//                }
//                    
//                } catch (NumberFormatException e) {
//                } catch (ParseException e) {
//            }
//                
//                index = 0;
//            }
//            bf.close();
//        } catch (IOException ex) {
//        }
//    }
    
    
//    public void InsertMaterialQuantityDetails() throws ParseException {
//    try {           
////        List<TblMapCompanyPlantMaterial> allMapCompanyPlantMaterials = compPlantMaterialDao.getList("where mapStatus = 1"); 
////        for (TblMapCompanyPlantMaterial material : allMapCompanyPlantMaterials) {
////            material.setMapQuantityStores(BigDecimal.ZERO);
////            compPlantMaterialDao.save(material); 
////            System.out.println("ESTABLISHED 0.00");
////        }
//        
//        
////        BufferedReader bf = new BufferedReader(new FileReader("/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issuebck/Material(" + utils.DateIn() + ").CSV"));
//        BufferedReader bf = new BufferedReader(new FileReader("E:\\Material(" + utils.DateIn() + ").CSV"));
//
//        String line = null;
//        Scanner scanner = null;
//        int index = 0;
//        while ((line = bf.readLine()) != null) {
//            scanner = new Scanner(line);
//            scanner.useDelimiter(",");
//            try {
//                while (scanner.hasNext()) {
//                    String data = scanner.next();
//                    switch (index) {
//                        case 0:
//                            tblCompanyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compCode='" + data + "'").get(0);
//                            break;
//                        case 1:
//                            tblPlantMaster = (TblPlantMaster) plantDao.getList("where plantCode='" + data + "'").get(0);
//                            break;
//                        case 2:
//                            listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialCode='" + data + "'");
//                            if (!listTblMaterialMaster.isEmpty()) {
//                                tblMaterialMaster = (TblMaterialMaster) materialDao.getList("where materialStatus=1 and materialCode='" + data + "'").get(0);
//                            }else{
//                                
//                                System.out.println("Data not instered as material "+data);
//                            
//                                
//           
//                TblEmpMaster empMaster = new TblEmpMaster();
//                empMaster.setEmpNumber(1);
//                tblMaterialMaster.setMaterialCode(data);
//                tblMaterialMaster.setMaterialName(data);
//                tblMaterialMaster.setMaterialDesc(data);
//                tblMaterialMaster.setMaterialStatus(1);
//                tblMaterialMaster.setTblEmpMaster(empMaster);
//                tblMaterialMaster.setMaterialLmd(utils.getDateFormat(utils.DateIn()));
//                                System.out.println("Mat Insteredt perfectly" +data);
//                boolean result = materialDao.save(tblMaterialMaster);
//                if(result){
//                     System.out.println("Done Inserting");
////                     listTblMapCompanyPlantMaterial = (ArrayList<TblMapCompanyPlantMaterial>) compPlantMaterialDao.getList("where materialStatus=1 and tblMaterialMaster.materialId='" + tblMaterialMaster.getMaterialId() + "'");
////                     if(listTblMapCompanyPlantMaterial.equals(0)){
//                         TblMapCompanyPlantMaterial  tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
//                               TblCompanyMaster tblCompanyMaster1 = new TblCompanyMaster();
//                               tblCompanyMaster1.setCompId(1);
//                               tblMapCompanyPlantMaterial.setTblCompanyMaster(tblCompanyMaster1);
//                               TblPlantMaster plantMaster = new TblPlantMaster();
//                               plantMaster.setPlantId(1);
//                               tblMapCompanyPlantMaterial.setTblPlantMaster(plantMaster);
//                               TblMaterialMaster materialMaster = new TblMaterialMaster();
//                               materialMaster.setMaterialId(tblMaterialMaster.getMaterialId());
//                               tblMapCompanyPlantMaterial.setTblMaterialMaster(materialMaster);                           
//                               tblMapCompanyPlantMaterial.setMapQuantityStores(BigDecimal.ZERO);
//                               tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
//                               tblMapCompanyPlantMaterial.setMapStatus(1);
//                               tblMapCompanyPlantMaterial.setTblEmpMaster(empMaster);
//                               compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
//                                System.out.println("Material Mapping also Done");
//                     
////                     }else{
////                         System.out.println("Already Mapped");
////                     }                           
//                               
//                
//                    
//                    
//                }else{
//                
//                    System.out.println("Failed in inserting");
//                
//                }
//                               
//                          }   
//                            System.out.println("data " + data );
//                            break;
//                        case 4:
//                            if (!listTblMaterialMaster.isEmpty()) {
//                                try {
//                                    tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + tblCompanyMaster.getCompId() + " and tblPlantMaster.plantId=" + tblPlantMaster.getPlantId() + " and tblMaterialMaster.materialId=" + tblMaterialMaster.getMaterialId() + "").get(0);
//                                    tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
//                                Double d = new Double(data);
//                                BigDecimal b = BigDecimal.valueOf(d);
//                                tblMapCompanyPlantMaterial.setMapQuantityStores(b);
//                                System.out.println("Id " + tblMapCompanyPlantMaterial.getMapId() + " qty " + b);
//                                compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
//                                    
//                                } catch (NumberFormatException e) {
//                                    System.out.println("Material Not found");
//                                    continue;
//                                } catch (ParseException e) {
//                                    System.out.println("Material Not found");
//                                    continue;
//                        }
//                                
//                                
//                            }else{
//                                System.out.println("Not Availble");
//                            }   
//                            break;
//                        default:
//                            break;
//                    }
//                    index++;
//                    
//                }
//                
//            }catch (NumberFormatException e) {
//                // Skip to the next line in case of NumberFormatException
//                System.out.println("Skipped nextline");
//                continue;
//            }
//            // Skip to the next line in case of ParseException
//            
//            index = 0; // Reset index for the next line
//        }
//        System.out.println("All entered Succesfully");
//        bf.close();
//    } catch (IOException ex) {
//        // Handle IOException
//    }
//}
    
    public void InsertMaterialQuantityDetails() throws ParseException {
    try {      
     //   BufferedReader bf = new BufferedReader(new FileReader("/home/sapuser/sapf/Material(" + utils.DateIn() + ").CSV"));
      //  BufferedReader bf = new BufferedReader(new FileReader("/opt/apache-tomcat-9.0.6/webapps/ProcureZone/uploads/issuebck/Material(" + utils.DateIn() + ").CSV"));
        BufferedReader bf = new BufferedReader(new FileReader("E:\\Material(" + utils.DateIn() + ").CSV"));
        System.out.println("Bufer reader 1");

        String line = null;
        Scanner scanner = null;
        int index = 0;
        while ((line = bf.readLine()) != null) {
            System.out.println("Bufer reader 2");
            scanner = new Scanner(line);
            scanner.useDelimiter(",");
            try {
                while (scanner.hasNext()) {
                    String data = scanner.next();
                    switch (index) {
                        case 0:
                            tblCompanyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compCode='" + data + "'").get(0);
                            System.out.println("Bufer reader 3");
                            break;
                        case 1:
                            tblPlantMaster = (TblPlantMaster) plantDao.getList("where plantCode='" + data + "'").get(0);
                            System.out.println("Bufer reader 4");
                            break;
                        case 2:
                            listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialCode='" + data + "'");
                            if (!listTblMaterialMaster.isEmpty()) {
                                tblMaterialMaster = listTblMaterialMaster.get(0);
                            } else {
                                System.out.println("Bufer reader 5");
                                // Material does not exist, create it
                                tblMaterialMaster = new TblMaterialMaster();
                                tblMaterialMaster.setMaterialCode(data);
                                tblMaterialMaster.setMaterialName(data);
                                tblMaterialMaster.setMaterialDesc(data);
                                tblMaterialMaster.setMaterialStatus(1);
                                TblEmpMaster empMaster = new TblEmpMaster();
                                empMaster.setEmpNumber(1);
                                tblMaterialMaster.setTblEmpMaster(empMaster);
                                tblMaterialMaster.setMaterialLmd(utils.getDateFormat(utils.DateIn()));
                                // Save the new material
                                boolean materialResult = materialDao.save(tblMaterialMaster);
                                if (materialResult) {
                                    System.out.println("Material Inserted: " + data);
                                    // Material mapping
                                    TblMapCompanyPlantMaterial tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
                                    tblMapCompanyPlantMaterial.setTblCompanyMaster(tblCompanyMaster);
                                    tblMapCompanyPlantMaterial.setTblPlantMaster(tblPlantMaster);
                                    tblMapCompanyPlantMaterial.setTblMaterialMaster(tblMaterialMaster);
                                    tblMapCompanyPlantMaterial.setMapQuantityStores(BigDecimal.ZERO);
                                    tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
                                    tblMapCompanyPlantMaterial.setMapStatus(1);
                                    tblMapCompanyPlantMaterial.setTblEmpMaster(empMaster);
                                    System.out.println("Bufer reader 6");
                                    boolean mappingResult = compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
                                    if (mappingResult) {
                                        System.out.println("Material Mapping Done: " + data);
                                    } else {
                                        System.out.println("Failed to map material: " + data);
                                    }
                                } else {
                                    System.out.println("Failed to insert material: " + data);
                                }
                            }
                            break;
                        case 4:
                            if (!listTblMaterialMaster.isEmpty()) {
                                // Check if material is mapped
                                try {
                                    tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + tblCompanyMaster.getCompId() + " and tblPlantMaster.plantId=" + tblPlantMaster.getPlantId() + " and tblMaterialMaster.materialId=" + tblMaterialMaster.getMaterialId() + "").get(0);
                                } catch (Exception e) {
                                    // Material not mapped, map it
                                    tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
                                    tblMapCompanyPlantMaterial.setTblCompanyMaster(tblCompanyMaster);
                                    tblMapCompanyPlantMaterial.setTblPlantMaster(tblPlantMaster);
                                    tblMapCompanyPlantMaterial.setTblMaterialMaster(tblMaterialMaster);
                                    tblMapCompanyPlantMaterial.setMapQuantityStores(BigDecimal.ZERO);
                                    tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
                                    tblMapCompanyPlantMaterial.setMapStatus(1);
                                    TblEmpMaster empMaster = new TblEmpMaster();
                                    empMaster.setEmpNumber(1);
                                    tblMapCompanyPlantMaterial.setTblEmpMaster(empMaster);
                                    boolean mappingResult = compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
                                    if (mappingResult) {
                                        System.out.println("Material Mapping Done: " + data);
                                    } else {
                                        System.out.println("Failed to map material: " + data);
                                    }
                                }
                                // Update quantity
                                tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
                                Double d = new Double(data);
                                BigDecimal b = BigDecimal.valueOf(d);
                                tblMapCompanyPlantMaterial.setMapQuantityStores(b);
                                System.out.println("Id " + tblMapCompanyPlantMaterial.getMapId() + " qty " + b);
                                compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
                            }
                            break;
                        default:
                            
                            break;
                    }
                    index++;
                }
            } catch (NumberFormatException e) {
                // Skip to the next line in case of NumberFormatException
                System.out.println("Skipped next line");
                continue;
            }
            index = 0; // Reset index for the next line
        }
        System.out.println("All entered successfully");
        bf.close();
    } catch (IOException ex) {
        // Handle IOException
    }
}


    
    
    
    
//    public void InsertMaterialQuantityDetails() throws ParseException {
//    try {           
//        List<TblMapCompanyPlantMaterial> allMapCompanyPlantMaterials = compPlantMaterialDao.getList("where mapStatus = 1"); // Assuming getAll() method fetches all entries
//        for (TblMapCompanyPlantMaterial material : allMapCompanyPlantMaterials) {
//            material.setMapQuantityStores(BigDecimal.ZERO);
//            compPlantMaterialDao.save(material); // Save the initialized value
//            System.out.println("ESTEABLISDE 0.00");
//        }
//
//        BufferedReader bf = new BufferedReader(new FileReader("E:\\Material(" + utils.DateIn() + ").CSV"));
//        String line = null;
//        Scanner scanner = null;
//        int index = 0;
//        while ((line = bf.readLine()) != null) {
//            scanner = new Scanner(line);
//            scanner.useDelimiter(",");
//            
//            try {
//                while (scanner.hasNext()) {
//                    String data = scanner.next();
//                    switch (index) {
//                        case 0:
//                            tblCompanyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compCode='" + data + "'").get(0);
//                            break;
//                        case 1:
//                            tblPlantMaster = (TblPlantMaster) plantDao.getList("where plantCode='" + data + "'").get(0);
//                            break;
//                        case 2:
//                            listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialCode='" + data + "'");
//                            if (!listTblMaterialMaster.isEmpty()) {
//                                tblMaterialMaster = (TblMaterialMaster) materialDao.getList("where materialStatus=1 and materialCode='" + data + "'").get(0);
//                            } else {
//                                // Skip processing this line if material is not found
//                                continue;
//                            }
//                            break;
//                        case 4:
//                            if (!listTblMaterialMaster.isEmpty()) {
//                                tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + tblCompanyMaster.getCompId() + " and tblPlantMaster.plantId=" + tblPlantMaster.getPlantId() + " and tblMaterialMaster.materialId=" + tblMaterialMaster.getMaterialId() + "").get(0);
//                                tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
//                                Double d = new Double(data);
////                                BigDecimal b = BigDecimal.valueOf(d);
//                                BigDecimal b = new BigDecimal(d);
//                                tblMapCompanyPlantMaterial.setMapQuantityStores(b);
//                                System.out.println("Id " + tblMapCompanyPlantMaterial.getMapId() + " qty " + b);
//                                compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
//                            }
//                            break;
//                        default:
//                            break;
//                    }
//                    index++;
//                }  
//            } catch (NumberFormatException nfe) {
//                // Skip processing this line
//                continue;
//            } catch (ParseException pe) {
//                // Skip processing this line
//                continue;
//            } catch (IndexOutOfBoundsException ioobe) {
//                // Skip processing this line
//                continue;
//            }
//            index = 0;
//        }
//        bf.close();
//    } catch (IOException ex) {
//        // Handle IOException
//    }
//}
    
    
//    public void InsertMaterialQuantityDetails() throws ParseException {
//    try {
////         BufferedReader bf = new BufferedReader(new FileReader("/opt/tomcat/apache-tomcat-8.5.57/webapps/ProcureZone/uploads/issue/Material(" + utils.DateIn() + ").CSV"));
//        BufferedReader bf = new BufferedReader(new FileReader("E:\\Material(" + utils.DateIn() + ").CSV"));
//        String line = null;
//
//        while ((line = bf.readLine()) != null) {
//            String[] parts = line.split(",");
//
//            // Assuming the CSV structure is: CompanyCode, PlantCode, MaterialCode, Description, Quantity
//            String companyCode = parts[0];
//            String plantCode = parts[1];
//            String materialCode = parts[2];
//            BigDecimal quantity = new BigDecimal(parts[4]);
//
//            try {
//                 tblCompanyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compCode='" + companyCode + "'").get(0);
//                 tblPlantMaster = (TblPlantMaster) plantDao.getList("where plantCode='" + plantCode + "'").get(0);
//                 System.out.println("Plant Code is  :"+tblPlantMaster.getPlantName());
//                 listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialStatus=1 and materialCode='" + materialCode + "'");
//
//                if (!listTblMaterialMaster.isEmpty()) {
//                    tblMaterialMaster = listTblMaterialMaster.get(0);
//                    tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + tblCompanyMaster.getCompId() + " and tblPlantMaster.plantId=" + tblPlantMaster.getPlantId() + " and tblMaterialMaster.materialId=" + tblMaterialMaster.getMaterialId() + "").get(0);
//
//                    tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
//                    tblMapCompanyPlantMaterial.setMapQuantityStores(quantity);
//
//                    System.out.println("Company: " + companyCode + ", Plant: " + plantCode + ", Material: " + materialCode + ", Quantity: " + quantity);
//                    compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
//                }
//            } catch (NumberFormatException e) {
//                // Handle NumberFormatException
//            } catch (ParseException e) {
//                // Handle ParseException
//            } catch (IndexOutOfBoundsException e) {
//                // Handle IndexOutOfBoundsException
//            }
//        }
//
//        bf.close();
//    } catch (IOException ex) {
//        // Handle IOException
//    }
//}



    public void InsertMaterialQuantityDetails1() {
        try {
            
            FileInputStream input = new FileInputStream(new File("E:\\Material(" + utils.DateIn() + ").CSV"));
            HSSFWorkbook wb = new HSSFWorkbook(input);
            HSSFSheet sheet = wb.getSheetAt(0);

            HSSFRow row;

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {

                tblCompanyMaster = new TblCompanyMaster();
                tblPlantMaster = new TblPlantMaster();
                tblMaterialMaster = new TblMaterialMaster();
                tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();

                row = sheet.getRow(i);
                tblCompanyMaster = (TblCompanyMaster) companyDao.getList("where compStatus=1 and compCode='" + row.getCell(0) + "'").get(0);
                tblPlantMaster = (TblPlantMaster) plantDao.getList("where plantStatus=1 and plantCode='" + row.getCell(1) + "'").get(0);
                tblMaterialMaster = (TblMaterialMaster) materialDao.getList("where materialStatus=1 and materialCode='" + row.getCell(2) + "' and materialDesc='" + row.getCell(3) + "'").get(0);
                tblMapCompanyPlantMaterial = (TblMapCompanyPlantMaterial) compPlantMaterialDao.getList("where mapStatus=1 and tblCompanyMaster.compId=" + tblCompanyMaster.getCompId() + " and tblPlantMaster.plantId=" + tblPlantMaster.getPlantId() + " and tblMaterialMaster.materialId=" + tblMaterialMaster.getMaterialId() + "").get(0);
                tblMapCompanyPlantMaterial.setMapQuantityStores(new BigDecimal(row.getCell(4).toString()));
                compPlantMaterialDao.save(tblMapCompanyPlantMaterial);

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

    public static void main(String[] args) throws Exception {
        SapCsvImport sap = new SapCsvImport();
        sap.InsertMaterialQuantityDetails();
    }

}
