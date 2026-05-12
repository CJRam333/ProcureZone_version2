/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.indent.action;

//import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
//import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opensymphony.xwork2.ActionContext;
//import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.ServletActionContext;
import pojo.TblIndentDetails;
import pojo.TblIndentMaster;
import seeds.dbconfig.Util.Utils;
import seeds.global.service.DaoFactory;
import seeds.indent.daoImpl.IndentDaoImpl;
import seeds.indent.daoImpl.IndentDetailsDaoImpl;

/**
 *
 * @author ramesh.avv
 */
public class IndentPdfDetails {

    private final Utils utils = new Utils();
    private TblIndentMaster tblIndentMaster;
    private final IndentDaoImpl indentDao = DaoFactory.getDao(IndentDaoImpl.class);
    private List<TblIndentDetails> listTblIndentDetails;
    private final IndentDetailsDaoImpl indentDetailsDao = DaoFactory.getDao(IndentDetailsDaoImpl.class);

    public TblIndentMaster getTblIndentMaster() {
        return tblIndentMaster;
    }

    public void setTblIndentMaster(TblIndentMaster tblIndentMaster) {
        this.tblIndentMaster = tblIndentMaster;
    }

    public List<TblIndentDetails> getListTblIndentDetails() {
        return listTblIndentDetails;
    }

    public void setListTblIndentDetails(List<TblIndentDetails> listTblIndentDetails) {
        this.listTblIndentDetails = listTblIndentDetails;
    }

    public IndentPdfDetails() throws Exception {
        tblIndentMaster = new TblIndentMaster();
        listTblIndentDetails = new ArrayList<TblIndentDetails>();
    }

    /*public ByteArrayOutputStream exportToPdf(int req) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            HttpServletResponse response = (HttpServletResponse) ActionContext.getContext().get(ServletActionContext.HTTP_RESPONSE);
            //Rectangle pageSize = new Rectangle(PageSize.A4);
            //pageSize.setBackgroundColor(new BaseColor(84, 141, 212));
            Document document = new Document(PageSize.A4, 36, 36, 64, 64);

            PdfWriter.getInstance(document, baos);
            document.open();

            document.open();
            document.setMarginMirroring(true);
            document.setMarginMirroringTopBottom(true);

            tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
            listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");

            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(110f);
            Font xf = new Font();
            //xf.setColor(BaseColor.RED);
            xf.setSize(8f);
            Chunk x = new Chunk("Indent Request Details");
            xf.setStyle("font-weight:bold; color:red;");
            //xf.isUnderlined();
            x.setFont(xf);
            Paragraph title = new Paragraph(x);
            PdfPCell cell = new PdfPCell(title);
            cell.setColspan(10);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(10.0f);
            table.addCell(cell);

            Font xf3 = new Font();
            xf3.setSize(8f);
            xf3.setColor(BaseColor.RED);
            x = new Chunk("Year : " + tblIndentMaster.getIndentYear());
            xf3.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(5);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table.addCell(cell);

            xf3.setSize(8f);
            x = new Chunk("Date: " + tblIndentMaster.getIndentDate());
            xf3.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(5);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPadding(10.0f);
            table.addCell(cell);

            xf3.setSize(8f);
            x = new Chunk("Indent No.: " + tblIndentMaster.getIndentNo());
            xf3.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(5);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table.addCell(cell);

            xf3.setSize(8f);
            x = new Chunk("Employee : " + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId() + ")");
            xf3.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(5);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPadding(10.0f);
            table.addCell(cell);

            xf3.setSize(8f);
            x = new Chunk("Company : " + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getTblCompanyMaster().getCompName());
            xf3.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(5);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table.addCell(cell);

            xf3.setSize(8f);
            x = new Chunk("Department : " + tblIndentMaster.getTblDepartmentMaster().getDeptName());
            xf3.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(5);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPadding(10.0f);
            table.addCell(cell);

            xf3.setSize(8f);
            x = new Chunk("Section : " + tblIndentMaster.getTblSectionMaster().getSecName());
            xf3.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(10);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table.addCell(cell);

            xf3.setSize(8f);
            x = new Chunk("Please arrange the following items as per the Specification/Details.");
            xf3.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(10);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table.addCell(cell);

            document.add(table);

            PdfPTable table1 = new PdfPTable(5);
            table1.setWidthPercentage(110f);
            Font xf1 = new Font();
            xf1.setSize(6f);
            x = new Chunk("S.No");
            xf1.setStyle("font-weight:bold; text-decoration:none");
            //xf1.isUnderlined();
            x.setFont(xf1);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            //cell.setPadding (10.0f);
            table1.addCell(cell);

            xf1.setSize(6f);
            x = new Chunk("Material Description");
            xf1.setStyle("font-weight:bold; text-decoration:none");
            //xf1.isUnderlined();
            x.setFont(xf1);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            //cell.setPadding (10.0f);
            table1.addCell(cell);

            xf1.setSize(6f);
            x = new Chunk("UMO");
            xf1.setStyle("font-weight:bold; text-decoration:none");
            //xf1.isUnderlined();
            x.setFont(xf1);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            //cell.setPadding (10.0f);
            table1.addCell(cell);

            xf1.setSize(6f);
            x = new Chunk("Qty Required");
            xf1.setStyle("font-weight:bold; text-decoration:none");
            //xf1.isUnderlined();
            x.setFont(xf1);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            //cell.setPadding (10.0f);
            table1.addCell(cell);

            xf1.setSize(6f);
            x = new Chunk("Available Stock");
            xf1.setStyle("font-weight:bold; text-decoration:none");
            //xf1.isUnderlined();
            x.setFont(xf1);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            //cell.setPadding (10.0f);
            table1.addCell(cell);

            Iterator it = listTblIndentDetails.iterator();
            int k = 1;
            while (it.hasNext()) {
                TblIndentDetails indentDetails = (TblIndentDetails) it.next();
                xf1.setSize(6f);
                x = new Chunk(Integer.toString(k));
                xf1.setStyle("font-weight:bold; text-decoration:none");
                //xf1.isUnderlined();
                x.setFont(xf1);
                title = new Paragraph(x);
                cell = new PdfPCell(title);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table1.addCell(cell);

                xf1.setSize(6f);
                x = new Chunk(indentDetails.getIndentDetailsMaterial());
                xf1.setStyle("font-weight:bold; text-decoration:none");
                //xf1.isUnderlined();
                x.setFont(xf1);
                title = new Paragraph(x);
                cell = new PdfPCell(title);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table1.addCell(cell);

                xf1.setSize(6f);
                x = new Chunk(indentDetails.getIndentDetailsUmo());
                xf1.setStyle("font-weight:bold; text-decoration:none");
                //xf1.isUnderlined();
                x.setFont(xf1);
                title = new Paragraph(x);
                cell = new PdfPCell(title);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table1.addCell(cell);

                xf1.setSize(6f);
                x = new Chunk(String.valueOf(indentDetails.getIndentDetailsQty()));
                xf1.setStyle("font-weight:bold; text-decoration:none");
                //xf1.isUnderlined();
                x.setFont(xf1);
                title = new Paragraph(x);
                cell = new PdfPCell(title);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table1.addCell(cell);

                xf1.setSize(6f);
                x = new Chunk(String.valueOf(indentDetails.getIndentDetailsStockAval()));
                xf1.setStyle("font-weight:bold; text-decoration:none");
                //xf1.isUnderlined();
                x.setFont(xf1);
                title = new Paragraph(x);
                cell = new PdfPCell(title);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table1.addCell(cell);

                k++;
            }

            document.add(table1);

            PdfPTable table2 = new PdfPTable(12);
            table2.setWidthPercentage(110f);

            Font xf4 = new Font();
            xf4.setColor(BaseColor.RED);
            xf4.setSize(8f);
            x = new Chunk("Purpose : " + tblIndentMaster.getIndentPurpose());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(12);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("Suggested Vendor 1: " + tblIndentMaster.getIndentVendor());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(12);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("2 : " + tblIndentMaster.getIndentVendor1());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(12);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("3 : " + tblIndentMaster.getIndentVendor2());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(12);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("4 : " + tblIndentMaster.getIndentVendor3());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(12);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("5 : " + tblIndentMaster.getIndentVendor4());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(12);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("Approved By : " + tblIndentMaster.getTblEmpMasterByIndentFinalApprovedby().getEmpName());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(6);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("Approved Date : " + tblIndentMaster.getIndentApprovedbyDate());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(6);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("Approved Remarks : " + tblIndentMaster.getIndentRemarks());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(12);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("Final Approved By : " + tblIndentMaster.getTblEmpMasterByIndentFinalApprovedby().getEmpName());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(6);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("Final Approved Date : " + tblIndentMaster.getIndentFinalDate());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(6);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            xf4.setSize(8f);
            x = new Chunk("Final Approved Remarks : " + tblIndentMaster.getIndentFinalRemarks());
            xf4.setStyle("font-weight:bold; color:red;");
            //xf3.isUnderlined();
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(12);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(10.0f);
            table2.addCell(cell);

            document.add(table2);
            document.close();

            OutputStream outputStream = response.getOutputStream();
            baos.writeTo(outputStream);
            response.setHeader("Pragma", "public");
            response.setContentType("application/pdf");
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            //catch
        }
        return baos;
    }*/

    public String exportToPdf1(int req) throws DocumentException, FileNotFoundException {

        String filePath ;
        String filepath1;

        try {
            HttpServletRequest request1 = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
            filePath = request1.getRealPath("/uploads/indent/");
            filepath1 = "uploads/indent/";
            String strDirectoy = filePath;
            (new File(strDirectoy)).mkdirs();
        } catch (Exception e) {
        }
        Document document = new Document(PageSize.A4, 36, 36, 64, 64);

        PdfWriter.getInstance(document, new FileOutputStream("Indent(" + utils.DateIn() + ").pdf"));
        String Path = "Indent(" + utils.DateIn() + ").pdf";
        document.open();
        document.setMarginMirroring(true);
        document.setMarginMirroringTopBottom(true);
        tblIndentMaster = (TblIndentMaster) indentDao.getById(req);
        listTblIndentDetails = (ArrayList<TblIndentDetails>) indentDetailsDao.getList("where indentDetailsStatus=1 and tblIndentMaster.indentId=" + tblIndentMaster.getIndentId() + "");
        try {
         HttpServletRequest request2 = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
         String filePath1 = request2.getRealPath("uploads/company/");
         Image img;
         int i;
         i = tblIndentMaster.getTblCompanyMaster().getCompId();
         if (i == 1) {
         img = Image.getInstance(filePath1 + "/seed.png");
         } else if (i == 2) {
         img = Image.getInstance(filePath1 + "/nslgroup.png");
         } else if (i == 3) {
         img = Image.getInstance(filePath1 + "/nsltextiles.png");
         } else if (i == 4) {
         img = Image.getInstance(filePath1 + "/nslsugars.png");
         } else if (i == 5) {
         img = Image.getInstance(filePath1 + "/nslinfratech.png");
         } else if (i == 6) {
         img = Image.getInstance(filePath1 + "/nslcotton.png");
         } else if (i == 7) {
         img = Image.getInstance(filePath1 + "/nslpower.png");
         } else {
         img = Image.getInstance(filePath1 + "/nsltextiles.png");
         }
         img.scalePercent(80, 80);            
         img.setAbsolutePosition(50, 780);
         document.add(img);

         } catch (DocumentException de) {
         System.err.println(de.getMessage());
         } catch (IOException ioe) {
         System.err.println(ioe.getMessage());
         }

        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(110f);

        Font xf = new Font();
        xf.setSize(20f);
        Chunk x = new Chunk("Nuziveedu Seeds Pvt. Limited");
        xf.setStyle("font-weight:bold;");
        x.setFont(xf);
        Paragraph title = new Paragraph(x);
        PdfPCell cell = new PdfPCell(title);
        cell.setColspan(10);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4.0f);
        table.addCell(cell);

        Font xf1 = new Font();
        xf1.setSize(10f);
        x = new Chunk("KANDLAKOYA, SECUNDERABAD.");
        x.setFont(xf1);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(10);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4.0f);
        table.addCell(cell);

        Font xf2 = new Font();
        xf2.setSize(12f);
        x = new Chunk("MATERIAL PURCHASE REQUISITION");
        xf2.setStyle("font-weight:bold;");
        x.setFont(xf2);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(10);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5.0f);
        table.addCell(cell);

        Font xf3 = new Font();
        xf3.setSize(8f);
        x = new Chunk("Year : " + tblIndentMaster.getIndentYear());
        xf3.setStyle(Font.NORMAL);
        x.setFont(xf3);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(6);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4.0f);
        table.addCell(cell);

        x = new Chunk("Date: " + tblIndentMaster.getIndentDate());
        x.setFont(xf3);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(5);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4.0f);
        table.addCell(cell);

        x = new Chunk("Indent No.: " + tblIndentMaster.getIndentNo());
        x.setFont(xf3);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(5);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4.0f);
        table.addCell(cell);

        x = new Chunk("Employee : " + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpName() + "(" + tblIndentMaster.getTblEmpMasterByIndentCreatedby().getEmpId() + ")");
        x.setFont(xf3);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(5);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4.0f);
        table.addCell(cell);

        x = new Chunk("Company : " + tblIndentMaster.getTblCompanyMaster().getCompName());
        x.setFont(xf3);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(5);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4.0f);
        table.addCell(cell);

        x = new Chunk("Department : " + tblIndentMaster.getTblDepartmentMaster().getDeptName());
        x.setFont(xf3);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(5);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4.0f);
        table.addCell(cell);

        x = new Chunk("Section : " + tblIndentMaster.getTblSectionMaster().getSecName());
        x.setFont(xf3);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(5);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4.0f);
        table.addCell(cell);
        
        x = new Chunk("Plant : " + tblIndentMaster.getTblPlantMaster().getPlantName());
        x.setFont(xf3);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(5);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4.0f);
        table.addCell(cell);

        x = new Chunk("Please arrange the following items as per the Specification/Details.");
        xf3.setStyle("font-weight:bold; color:red;");
        x.setFont(xf3);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(10);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5.0f);
        table.addCell(cell);

        document.add(table);

        PdfPTable table1 = new PdfPTable(9);
        table1.setWidthPercentage(110f);
        Font xf4 = new Font();
        xf4.setSize(8f);
        xf4.setStyle("font-weight:bold;");
        x = new Chunk("S.No");
        x.setFont(xf4);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table1.addCell(cell);

        x = new Chunk("Material Description");
        x.setFont(xf4);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table1.addCell(cell);

        x = new Chunk("UOM");
        x.setFont(xf4);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table1.addCell(cell);
        
        x = new Chunk("Purpose");
        x.setFont(xf4);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table1.addCell(cell);
        
        x = new Chunk("Vendor");
        x.setFont(xf4);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table1.addCell(cell);

        x = new Chunk("Quantity Requested");
        x.setFont(xf4);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table1.addCell(cell);
        
        x = new Chunk("Rm Quantity Requested");
        x.setFont(xf4);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table1.addCell(cell);
        
        x = new Chunk("Dept Head Quantity Requested");
        x.setFont(xf4);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table1.addCell(cell);

        x = new Chunk("Available Stock");
        x.setFont(xf4);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table1.addCell(cell);

        Iterator it = listTblIndentDetails.iterator();
        int k = 1;
        while (it.hasNext()) {
            TblIndentDetails indentDetails = (TblIndentDetails) it.next();
            x = new Chunk(Integer.toString(k));
            xf4.setSize(8f);
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(cell);

            x = new Chunk(indentDetails.getTblMaterialMaster().getMaterialName());
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(cell);

            x = new Chunk(indentDetails.getTblUmoMaster().getUmoName());
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(cell);
            
            x = new Chunk(indentDetails.getIndentDetailsPurpose());
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(cell);
            
            x = new Chunk(indentDetails.getIndentDetailsVendor());
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(cell);

            x = new Chunk(String.valueOf(indentDetails.getIndentDetailsQty()));
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(cell);
            
            x = new Chunk(String.valueOf(indentDetails.getIndentDetailsRmQty()));
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(cell);
            
            x = new Chunk(String.valueOf(indentDetails.getIndentDetailsDeptQty()));
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(cell);

            x = new Chunk(String.valueOf(indentDetails.getIndentDetailsStockAval()));
            x.setFont(xf4);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(cell);

            k++;
        }

        document.add(table1);

        PdfPTable table2 = new PdfPTable(6);
        table2.setWidthPercentage(110f);

        Font xf5 = new Font();
        xf5.setSize(8f);
        xf5.setStyle("font-weight:bold;");
        x = new Chunk("Comments/Remarks : " + tblIndentMaster.getIndentComments());
        x.setFont(xf5);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(3);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4.0f);
        table2.addCell(cell);

        
        document.add(table2);

        PdfPTable table3 = new PdfPTable(6);
        table3.setWidthPercentage(110f);
        Font xf6 = new Font();
        xf6.setSize(8f);
        xf6.setStyle("font-weight:bold;");
        x = new Chunk("Approved By : " + tblIndentMaster.getTblEmpMasterByIndentFinalApprovedby().getEmpName());
        x.setFont(xf6);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(3);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4.0f);
        table3.addCell(cell);

        x = new Chunk("Approved Date : " + tblIndentMaster.getIndentApprovedbyDate());
        x.setFont(xf6);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(3);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4.0f);
        table3.addCell(cell);

        if (tblIndentMaster.getIndentRemarks()!=null && !tblIndentMaster.getIndentRemarks().isEmpty()) {
            x = new Chunk("Approved Remarks : " + tblIndentMaster.getIndentRemarks());
            x.setFont(xf6);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(6);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(4.0f);
            table3.addCell(cell);
        }
        x = new Chunk("Final Approved By : " + tblIndentMaster.getTblEmpMasterByIndentFinalApprovedby().getEmpName());
        x.setFont(xf6);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(3);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4.0f);
        table3.addCell(cell);

        x = new Chunk("Final Approved Date : " + tblIndentMaster.getIndentFinalDate());
        x.setFont(xf6);
        title = new Paragraph(x);
        cell = new PdfPCell(title);
        cell.setColspan(3);
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4.0f);
        table3.addCell(cell);

        if (tblIndentMaster.getIndentFinalRemarks()!=null && !tblIndentMaster.getIndentFinalRemarks().isEmpty()) {
            x = new Chunk("Final Approved Remarks : " + tblIndentMaster.getIndentFinalRemarks());
            x.setFont(xf6);
            title = new Paragraph(x);
            cell = new PdfPCell(title);
            cell.setColspan(6);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(4.0f);
            table3.addCell(cell);
        }
        document.add(table3);        
        document.close();

        return Path;

    }

}
