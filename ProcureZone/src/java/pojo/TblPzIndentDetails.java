/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author ramesh.a
 */
public class TblPzIndentDetails implements java.io.Serializable  {
    
     private Integer indentDetailsId;
     private TblEmpMaster tblEmpMaster;
     private TbPzlIndentMastera tblIndentMaster;
//     private TblPzScheduleMaterialmaster tblMaterialMaster;
     private TblPzUmoMaster tblUmoMaster;
     private BigDecimal indentDetailsQty;
     private BigDecimal indentDetailsRmQty;
     private BigDecimal indentDetailsDeptQty;
     private BigDecimal indentDetailsStockAval;
     private BigDecimal indentDetailsPricing;
     private String indentDetailsPurpose;
     private String indentDetailsVendor;
     private int indentDetailsStatus;
     private Date indentDetailsLmd;
     private String indentMatel;
     private TblPzScheduleMaterialmaster indentDetailsMaterial;
     
     private String indentLoteNum;
     private String indentMateDesc;
     private String indentStoLoc;
     
     
     
     private String matStl;
     private String matOdv;
     private String matGot;
     private String matElisa;
     
     
     
     private String sDCLS;
     private String sTATS;
     private String sKIPD;
     private String iNSPDT;
     private String mOISTURE;
     private String PURESEED;
     
     private String INERTMATTER;
     private String OCSCOUNT;
     private String WEEDSEEDCOUNT;
     private String gRAIN;
     private String BLACKSEEDS;
     private String PINHOLESEEDS;
     
     private String ODVRES;
     private String BULKDENSITY;
     private String tHSW;
     private String COLDVIGOURGERMNORMAL;
     private String FIRSTCOUNTNORMAL;
     private String GERMNORMAL;
     
     private String FETNORMAL;
     private String SOILCOUNTDAYS;
     private String AAVGERMNORMAL;
     private String GOTGP;
     private String GOTFEMALE;
     private String GOTOTHERS;
     
     private String bG1;
     private String bG2;
     private String hT;
     private String fQR;
     private String q1;
     private String q2;
     private String q3;
     private String q4;
     private String q5;
     private String q6;
     private String q7;
     private String q8;
     private String q9;
     
     

    public TblPzIndentDetails() {
    }

    public TblPzIndentDetails(Integer indentDetailsId, TblEmpMaster tblEmpMaster, TbPzlIndentMastera tblIndentMaster, TblPzUmoMaster tblUmoMaster, BigDecimal indentDetailsQty, BigDecimal indentDetailsRmQty, BigDecimal indentDetailsDeptQty, BigDecimal indentDetailsStockAval, BigDecimal indentDetailsPricing, String indentDetailsPurpose, String indentDetailsVendor, int indentDetailsStatus, Date indentDetailsLmd, String indentMatel, TblPzScheduleMaterialmaster indentDetailsMaterial, String indentLoteNum, String indentMateDesc, String indentStoLoc, String matStl, String matOdv, String matGot, String matElisa, String sDCLS, String sTATS, String sKIPD, String iNSPDT, String mOISTURE, String PURESEED, String INERTMATTER, String OCSCOUNT, String WEEDSEEDCOUNT, String gRAIN, String BLACKSEEDS, String PINHOLESEEDS, String ODVRES, String BULKDENSITY, String tHSW, String COLDVIGOURGERMNORMAL, String FIRSTCOUNTNORMAL, String GERMNORMAL, String FETNORMAL, String SOILCOUNTDAYS, String AAVGERMNORMAL, String GOTGP, String GOTFEMALE, String GOTOTHERS, String bG1, String bG2, String hT, String fQR, String q1, String q2, String q3, String q4, String q5, String q6, String q7, String q8, String q9) {
        this.indentDetailsId = indentDetailsId;
        this.tblEmpMaster = tblEmpMaster;
        this.tblIndentMaster = tblIndentMaster;
        this.tblUmoMaster = tblUmoMaster;
        this.indentDetailsQty = indentDetailsQty;
        this.indentDetailsRmQty = indentDetailsRmQty;
        this.indentDetailsDeptQty = indentDetailsDeptQty;
        this.indentDetailsStockAval = indentDetailsStockAval;
        this.indentDetailsPricing = indentDetailsPricing;
        this.indentDetailsPurpose = indentDetailsPurpose;
        this.indentDetailsVendor = indentDetailsVendor;
        this.indentDetailsStatus = indentDetailsStatus;
        this.indentDetailsLmd = indentDetailsLmd;
        this.indentMatel = indentMatel;
        this.indentDetailsMaterial = indentDetailsMaterial;
        this.indentLoteNum = indentLoteNum;
        this.indentMateDesc = indentMateDesc;
        this.indentStoLoc = indentStoLoc;
        this.matStl = matStl;
        this.matOdv = matOdv;
        this.matGot = matGot;
        this.matElisa = matElisa;
        this.sDCLS = sDCLS;
        this.sTATS = sTATS;
        this.sKIPD = sKIPD;
        this.iNSPDT = iNSPDT;
        this.mOISTURE = mOISTURE;
        this.PURESEED = PURESEED;
        this.INERTMATTER = INERTMATTER;
        this.OCSCOUNT = OCSCOUNT;
        this.WEEDSEEDCOUNT = WEEDSEEDCOUNT;
        this.gRAIN = gRAIN;
        this.BLACKSEEDS = BLACKSEEDS;
        this.PINHOLESEEDS = PINHOLESEEDS;
        this.ODVRES = ODVRES;
        this.BULKDENSITY = BULKDENSITY;
        this.tHSW = tHSW;
        this.COLDVIGOURGERMNORMAL = COLDVIGOURGERMNORMAL;
        this.FIRSTCOUNTNORMAL = FIRSTCOUNTNORMAL;
        this.GERMNORMAL = GERMNORMAL;
        this.FETNORMAL = FETNORMAL;
        this.SOILCOUNTDAYS = SOILCOUNTDAYS;
        this.AAVGERMNORMAL = AAVGERMNORMAL;
        this.GOTGP = GOTGP;
        this.GOTFEMALE = GOTFEMALE;
        this.GOTOTHERS = GOTOTHERS;
        this.bG1 = bG1;
        this.bG2 = bG2;
        this.hT = hT;
        this.fQR = fQR;
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
        this.q5 = q5;
        this.q6 = q6;
        this.q7 = q7;
        this.q8 = q8;
        this.q9 = q9;
    }

    public TblPzIndentDetails(TblEmpMaster tblEmpMaster, TbPzlIndentMastera tblIndentMaster, TblPzUmoMaster tblUmoMaster, BigDecimal indentDetailsQty, BigDecimal indentDetailsRmQty, BigDecimal indentDetailsDeptQty, BigDecimal indentDetailsStockAval, BigDecimal indentDetailsPricing, String indentDetailsPurpose, String indentDetailsVendor, int indentDetailsStatus, Date indentDetailsLmd, String indentMatel, TblPzScheduleMaterialmaster indentDetailsMaterial, String indentLoteNum, String indentMateDesc, String indentStoLoc, String matStl, String matOdv, String matGot, String matElisa, String sDCLS, String sTATS, String sKIPD, String iNSPDT, String mOISTURE, String PURESEED, String INERTMATTER, String OCSCOUNT, String WEEDSEEDCOUNT, String gRAIN, String BLACKSEEDS, String PINHOLESEEDS, String ODVRES, String BULKDENSITY, String tHSW, String COLDVIGOURGERMNORMAL, String FIRSTCOUNTNORMAL, String GERMNORMAL, String FETNORMAL, String SOILCOUNTDAYS, String AAVGERMNORMAL, String GOTGP, String GOTFEMALE, String GOTOTHERS, String bG1, String bG2, String hT, String fQR, String q1, String q2, String q3, String q4, String q5, String q6, String q7, String q8, String q9) {
        this.tblEmpMaster = tblEmpMaster;
        this.tblIndentMaster = tblIndentMaster;
        this.tblUmoMaster = tblUmoMaster;
        this.indentDetailsQty = indentDetailsQty;
        this.indentDetailsRmQty = indentDetailsRmQty;
        this.indentDetailsDeptQty = indentDetailsDeptQty;
        this.indentDetailsStockAval = indentDetailsStockAval;
        this.indentDetailsPricing = indentDetailsPricing;
        this.indentDetailsPurpose = indentDetailsPurpose;
        this.indentDetailsVendor = indentDetailsVendor;
        this.indentDetailsStatus = indentDetailsStatus;
        this.indentDetailsLmd = indentDetailsLmd;
        this.indentMatel = indentMatel;
        this.indentDetailsMaterial = indentDetailsMaterial;
        this.indentLoteNum = indentLoteNum;
        this.indentMateDesc = indentMateDesc;
        this.indentStoLoc = indentStoLoc;
        this.matStl = matStl;
        this.matOdv = matOdv;
        this.matGot = matGot;
        this.matElisa = matElisa;
        this.sDCLS = sDCLS;
        this.sTATS = sTATS;
        this.sKIPD = sKIPD;
        this.iNSPDT = iNSPDT;
        this.mOISTURE = mOISTURE;
        this.PURESEED = PURESEED;
        this.INERTMATTER = INERTMATTER;
        this.OCSCOUNT = OCSCOUNT;
        this.WEEDSEEDCOUNT = WEEDSEEDCOUNT;
        this.gRAIN = gRAIN;
        this.BLACKSEEDS = BLACKSEEDS;
        this.PINHOLESEEDS = PINHOLESEEDS;
        this.ODVRES = ODVRES;
        this.BULKDENSITY = BULKDENSITY;
        this.tHSW = tHSW;
        this.COLDVIGOURGERMNORMAL = COLDVIGOURGERMNORMAL;
        this.FIRSTCOUNTNORMAL = FIRSTCOUNTNORMAL;
        this.GERMNORMAL = GERMNORMAL;
        this.FETNORMAL = FETNORMAL;
        this.SOILCOUNTDAYS = SOILCOUNTDAYS;
        this.AAVGERMNORMAL = AAVGERMNORMAL;
        this.GOTGP = GOTGP;
        this.GOTFEMALE = GOTFEMALE;
        this.GOTOTHERS = GOTOTHERS;
        this.bG1 = bG1;
        this.bG2 = bG2;
        this.hT = hT;
        this.fQR = fQR;
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
        this.q5 = q5;
        this.q6 = q6;
        this.q7 = q7;
        this.q8 = q8;
        this.q9 = q9;
    }

    public Integer getIndentDetailsId() {
        return indentDetailsId;
    }

    public void setIndentDetailsId(Integer indentDetailsId) {
        this.indentDetailsId = indentDetailsId;
    }

    public TblEmpMaster getTblEmpMaster() {
        return tblEmpMaster;
    }

    public void setTblEmpMaster(TblEmpMaster tblEmpMaster) {
        this.tblEmpMaster = tblEmpMaster;
    }

    public TbPzlIndentMastera getTblIndentMaster() {
        return tblIndentMaster;
    }

    public void setTblIndentMaster(TbPzlIndentMastera tblIndentMaster) {
        this.tblIndentMaster = tblIndentMaster;
    }

    public TblPzUmoMaster getTblUmoMaster() {
        return tblUmoMaster;
    }

    public void setTblUmoMaster(TblPzUmoMaster tblUmoMaster) {
        this.tblUmoMaster = tblUmoMaster;
    }

    public BigDecimal getIndentDetailsQty() {
        return indentDetailsQty;
    }

    public void setIndentDetailsQty(BigDecimal indentDetailsQty) {
        this.indentDetailsQty = indentDetailsQty;
    }

    public BigDecimal getIndentDetailsRmQty() {
        return indentDetailsRmQty;
    }

    public void setIndentDetailsRmQty(BigDecimal indentDetailsRmQty) {
        this.indentDetailsRmQty = indentDetailsRmQty;
    }

    public BigDecimal getIndentDetailsDeptQty() {
        return indentDetailsDeptQty;
    }

    public void setIndentDetailsDeptQty(BigDecimal indentDetailsDeptQty) {
        this.indentDetailsDeptQty = indentDetailsDeptQty;
    }

    public BigDecimal getIndentDetailsStockAval() {
        return indentDetailsStockAval;
    }

    public void setIndentDetailsStockAval(BigDecimal indentDetailsStockAval) {
        this.indentDetailsStockAval = indentDetailsStockAval;
    }

    public BigDecimal getIndentDetailsPricing() {
        return indentDetailsPricing;
    }

    public void setIndentDetailsPricing(BigDecimal indentDetailsPricing) {
        this.indentDetailsPricing = indentDetailsPricing;
    }

    public String getIndentDetailsPurpose() {
        return indentDetailsPurpose;
    }

    public void setIndentDetailsPurpose(String indentDetailsPurpose) {
        this.indentDetailsPurpose = indentDetailsPurpose;
    }

    public String getIndentDetailsVendor() {
        return indentDetailsVendor;
    }

    public void setIndentDetailsVendor(String indentDetailsVendor) {
        this.indentDetailsVendor = indentDetailsVendor;
    }

    public int getIndentDetailsStatus() {
        return indentDetailsStatus;
    }

    public void setIndentDetailsStatus(int indentDetailsStatus) {
        this.indentDetailsStatus = indentDetailsStatus;
    }

    public Date getIndentDetailsLmd() {
        return indentDetailsLmd;
    }

    public void setIndentDetailsLmd(Date indentDetailsLmd) {
        this.indentDetailsLmd = indentDetailsLmd;
    }

    public String getIndentMatel() {
        return indentMatel;
    }

    public void setIndentMatel(String indentMatel) {
        this.indentMatel = indentMatel;
    }

    public TblPzScheduleMaterialmaster getIndentDetailsMaterial() {
        return indentDetailsMaterial;
    }

    public void setIndentDetailsMaterial(TblPzScheduleMaterialmaster indentDetailsMaterial) {
        this.indentDetailsMaterial = indentDetailsMaterial;
    }

    public String getIndentLoteNum() {
        return indentLoteNum;
    }

    public void setIndentLoteNum(String indentLoteNum) {
        this.indentLoteNum = indentLoteNum;
    }

    public String getIndentMateDesc() {
        return indentMateDesc;
    }

    public void setIndentMateDesc(String indentMateDesc) {
        this.indentMateDesc = indentMateDesc;
    }

    public String getIndentStoLoc() {
        return indentStoLoc;
    }

    public void setIndentStoLoc(String indentStoLoc) {
        this.indentStoLoc = indentStoLoc;
    }

    public String getMatStl() {
        return matStl;
    }

    public void setMatStl(String matStl) {
        this.matStl = matStl;
    }

    public String getMatOdv() {
        return matOdv;
    }

    public void setMatOdv(String matOdv) {
        this.matOdv = matOdv;
    }

    public String getMatGot() {
        return matGot;
    }

    public void setMatGot(String matGot) {
        this.matGot = matGot;
    }

    public String getMatElisa() {
        return matElisa;
    }

    public void setMatElisa(String matElisa) {
        this.matElisa = matElisa;
    }

    public String getsDCLS() {
        return sDCLS;
    }

    public void setsDCLS(String sDCLS) {
        this.sDCLS = sDCLS;
    }

    public String getsTATS() {
        return sTATS;
    }

    public void setsTATS(String sTATS) {
        this.sTATS = sTATS;
    }

    public String getsKIPD() {
        return sKIPD;
    }

    public void setsKIPD(String sKIPD) {
        this.sKIPD = sKIPD;
    }

    public String getiNSPDT() {
        return iNSPDT;
    }

    public void setiNSPDT(String iNSPDT) {
        this.iNSPDT = iNSPDT;
    }

    public String getmOISTURE() {
        return mOISTURE;
    }

    public void setmOISTURE(String mOISTURE) {
        this.mOISTURE = mOISTURE;
    }

    public String getPURESEED() {
        return PURESEED;
    }

    public void setPURESEED(String PURESEED) {
        this.PURESEED = PURESEED;
    }

    public String getINERTMATTER() {
        return INERTMATTER;
    }

    public void setINERTMATTER(String INERTMATTER) {
        this.INERTMATTER = INERTMATTER;
    }

    public String getOCSCOUNT() {
        return OCSCOUNT;
    }

    public void setOCSCOUNT(String OCSCOUNT) {
        this.OCSCOUNT = OCSCOUNT;
    }

    public String getWEEDSEEDCOUNT() {
        return WEEDSEEDCOUNT;
    }

    public void setWEEDSEEDCOUNT(String WEEDSEEDCOUNT) {
        this.WEEDSEEDCOUNT = WEEDSEEDCOUNT;
    }

    public String getgRAIN() {
        return gRAIN;
    }

    public void setgRAIN(String gRAIN) {
        this.gRAIN = gRAIN;
    }

    public String getBLACKSEEDS() {
        return BLACKSEEDS;
    }

    public void setBLACKSEEDS(String BLACKSEEDS) {
        this.BLACKSEEDS = BLACKSEEDS;
    }

    public String getPINHOLESEEDS() {
        return PINHOLESEEDS;
    }

    public void setPINHOLESEEDS(String PINHOLESEEDS) {
        this.PINHOLESEEDS = PINHOLESEEDS;
    }

    public String getODVRES() {
        return ODVRES;
    }

    public void setODVRES(String ODVRES) {
        this.ODVRES = ODVRES;
    }

    public String getBULKDENSITY() {
        return BULKDENSITY;
    }

    public void setBULKDENSITY(String BULKDENSITY) {
        this.BULKDENSITY = BULKDENSITY;
    }

    public String gettHSW() {
        return tHSW;
    }

    public void settHSW(String tHSW) {
        this.tHSW = tHSW;
    }

    public String getCOLDVIGOURGERMNORMAL() {
        return COLDVIGOURGERMNORMAL;
    }

    public void setCOLDVIGOURGERMNORMAL(String COLDVIGOURGERMNORMAL) {
        this.COLDVIGOURGERMNORMAL = COLDVIGOURGERMNORMAL;
    }

    public String getFIRSTCOUNTNORMAL() {
        return FIRSTCOUNTNORMAL;
    }

    public void setFIRSTCOUNTNORMAL(String FIRSTCOUNTNORMAL) {
        this.FIRSTCOUNTNORMAL = FIRSTCOUNTNORMAL;
    }

    public String getGERMNORMAL() {
        return GERMNORMAL;
    }

    public void setGERMNORMAL(String GERMNORMAL) {
        this.GERMNORMAL = GERMNORMAL;
    }

    public String getFETNORMAL() {
        return FETNORMAL;
    }

    public void setFETNORMAL(String FETNORMAL) {
        this.FETNORMAL = FETNORMAL;
    }

    public String getSOILCOUNTDAYS() {
        return SOILCOUNTDAYS;
    }

    public void setSOILCOUNTDAYS(String SOILCOUNTDAYS) {
        this.SOILCOUNTDAYS = SOILCOUNTDAYS;
    }

    public String getAAVGERMNORMAL() {
        return AAVGERMNORMAL;
    }

    public void setAAVGERMNORMAL(String AAVGERMNORMAL) {
        this.AAVGERMNORMAL = AAVGERMNORMAL;
    }

    public String getGOTGP() {
        return GOTGP;
    }

    public void setGOTGP(String GOTGP) {
        this.GOTGP = GOTGP;
    }

    public String getGOTFEMALE() {
        return GOTFEMALE;
    }

    public void setGOTFEMALE(String GOTFEMALE) {
        this.GOTFEMALE = GOTFEMALE;
    }

    public String getGOTOTHERS() {
        return GOTOTHERS;
    }

    public void setGOTOTHERS(String GOTOTHERS) {
        this.GOTOTHERS = GOTOTHERS;
    }

    public String getbG1() {
        return bG1;
    }

    public void setbG1(String bG1) {
        this.bG1 = bG1;
    }

    public String getbG2() {
        return bG2;
    }

    public void setbG2(String bG2) {
        this.bG2 = bG2;
    }

    public String gethT() {
        return hT;
    }

    public void sethT(String hT) {
        this.hT = hT;
    }

    public String getfQR() {
        return fQR;
    }

    public void setfQR(String fQR) {
        this.fQR = fQR;
    }

    public String getQ1() {
        return q1;
    }

    public void setQ1(String q1) {
        this.q1 = q1;
    }

    public String getQ2() {
        return q2;
    }

    public void setQ2(String q2) {
        this.q2 = q2;
    }

    public String getQ3() {
        return q3;
    }

    public void setQ3(String q3) {
        this.q3 = q3;
    }

    public String getQ4() {
        return q4;
    }

    public void setQ4(String q4) {
        this.q4 = q4;
    }

    public String getQ5() {
        return q5;
    }

    public void setQ5(String q5) {
        this.q5 = q5;
    }

    public String getQ6() {
        return q6;
    }

    public void setQ6(String q6) {
        this.q6 = q6;
    }

    public String getQ7() {
        return q7;
    }

    public void setQ7(String q7) {
        this.q7 = q7;
    }

    public String getQ8() {
        return q8;
    }

    public void setQ8(String q8) {
        this.q8 = q8;
    }

    public String getQ9() {
        return q9;
    }

    public void setQ9(String q9) {
        this.q9 = q9;
    }

    

   
    
}
