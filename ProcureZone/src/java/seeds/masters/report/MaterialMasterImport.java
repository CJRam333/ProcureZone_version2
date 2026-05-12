/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.masters.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
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

/**
 *
 * @author ramesh.avv
 */
public class MaterialMasterImport {

    private final Utils utils = new Utils();
    private String message;
    private TblMaterialMaster tblMaterialMaster;
    private List<TblMaterialMaster> listTblMaterialMaster;
    private final MaterialDaoImpl materialDao = DaoFactory.getDao(MaterialDaoImpl.class);
    private TblMapCompanyPlantMaterial tblMapCompanyPlantMaterial;
    private final CompPlantMaterialDaoImpl compPlantMaterialDao = DaoFactory.getDao(CompPlantMaterialDaoImpl.class);
    private List<TblMapCompanyPlantMaterial> listTblMapCompanyPlantMaterial;
    private TblCompanyMaster tblCompanyMaster;
    private List<TblCompanyMaster> listTblCompanyMaster;
    private final CompanyDaoImpl companyDao = DaoFactory.getDao(CompanyDaoImpl.class);
    private TblPlantMaster tblPlantMaster;
    private List<TblPlantMaster> listTblPlantMaster;
    private final PlantDaoImpl plantDao = DaoFactory.getDao(PlantDaoImpl.class);
    private TblEmpMaster tblEmpMaster;

    public TblEmpMaster getTblEmpMaster() {
        return tblEmpMaster;
    }

    public void setTblEmpMaster(TblEmpMaster tblEmpMaster) {
        this.tblEmpMaster = tblEmpMaster;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MaterialMasterImport() throws Exception {
        tblMaterialMaster = new TblMaterialMaster();
        listTblMaterialMaster = new ArrayList<TblMaterialMaster>();
        tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
        listTblMapCompanyPlantMaterial = new ArrayList<TblMapCompanyPlantMaterial>();
        tblCompanyMaster = new TblCompanyMaster();
        listTblCompanyMaster = new ArrayList<TblCompanyMaster>();
        tblPlantMaster = new TblPlantMaster();
        listTblPlantMaster = new ArrayList<TblPlantMaster>();
        tblEmpMaster = new TblEmpMaster();
    }

    public String InsertMaterialMaster(int empNumber, String fileName) throws ParseException, NullPointerException {
        try {

            //FileInputStream input = new FileInputStream(new File("C:\\Users\\ramesh.avv.CORPNSL\\Desktop\\material.xls"));
            FileInputStream input = new FileInputStream(new File(fileName));
            POIFSFileSystem fs = new POIFSFileSystem(input);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row;
            //int material = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                tblMaterialMaster = new TblMaterialMaster();
                tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
                tblCompanyMaster = new TblCompanyMaster();
                tblPlantMaster = new TblPlantMaster();
                row = sheet.getRow(i);
                listTblMaterialMaster = (ArrayList<TblMaterialMaster>) materialDao.getList("where materialName='" + row.getCell(0).getStringCellValue() + "'");
                if (listTblMaterialMaster.isEmpty()) {
                    tblMaterialMaster = new TblMaterialMaster();
                    tblMaterialMaster.setMaterialCode(row.getCell(0).getStringCellValue());
                    System.out.println("Val 1 : "+row.getCell(0).getStringCellValue());
                    tblMaterialMaster.setMaterialName(row.getCell(0).getStringCellValue());
                    tblMaterialMaster.setMaterialDesc(row.getCell(1).getStringCellValue());
                    tblMaterialMaster.setMaterialStatus(1);
                    tblMaterialMaster.setMaterialLmd(utils.getDateFormat(utils.DateIn()));
                    //tblEmpMaster=new TblEmpMaster();     
                    tblEmpMaster.setEmpNumber(empNumber);
                    //tblEmpMaster.setEmpNumber(1);
                    tblMaterialMaster.setTblEmpMaster(tblEmpMaster);
                    materialDao.save(tblMaterialMaster);
                    //material=tblMaterialMaster.getMaterialId();
                    try {
                        listTblMapCompanyPlantMaterial = (ArrayList<TblMapCompanyPlantMaterial>) compPlantMaterialDao.getList("where tblMaterialMaster.materialId=" + tblMaterialMaster.getMaterialId() + "");
                    } catch (Exception e) {
                    }
                    if (listTblMapCompanyPlantMaterial.isEmpty()) {
                        tblMapCompanyPlantMaterial = new TblMapCompanyPlantMaterial();
                        //tblMaterialMaster.setMaterialId(material);
                        tblMapCompanyPlantMaterial.setTblMaterialMaster(tblMaterialMaster);
                        tblCompanyMaster = (TblCompanyMaster) companyDao.getList("where compName='" + row.getCell(2).getStringCellValue() + "'").get(0);
                        tblMapCompanyPlantMaterial.setTblCompanyMaster(tblCompanyMaster);
                        tblPlantMaster = (TblPlantMaster) plantDao.getList("where plantName='" + row.getCell(3).getStringCellValue() + "'").get(0);
                        tblMapCompanyPlantMaterial.setTblPlantMaster(tblPlantMaster);
                        tblMapCompanyPlantMaterial.setMapQuantityStores(new BigDecimal(row.getCell(4).getNumericCellValue()));
                        tblMapCompanyPlantMaterial.setMapStatus(1);
                        //tblEmpMaster.setEmpNumber(empNumber);
                        tblMapCompanyPlantMaterial.setTblEmpMaster(tblEmpMaster);
                        tblMapCompanyPlantMaterial.setMapLmd(utils.getDateFormat(utils.DateIn()));
                        compPlantMaterialDao.save(tblMapCompanyPlantMaterial);
                    }
                }
                /*else {
                    setMessage("Import Failed");
                    return message;
                }*/

                System.out.println("Imported rows " + i);
            }

            input.close();
            setMessage("Import Success");
            return message;
        } catch (IOException e) {
            setMessage("Import Failed");
            return message;
        } catch (NullPointerException e) {
            return message;
        }
    }

    public static void main(String[] args) throws Exception {
        MaterialMasterImport mmi = new MaterialMasterImport();
        //mmi.InsertMaterialMaster();
    }
}
