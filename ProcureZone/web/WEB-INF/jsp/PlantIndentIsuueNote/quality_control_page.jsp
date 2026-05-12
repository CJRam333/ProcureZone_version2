<%-- 
    Document   : seeds_indent_request
    Created on : Oct 3, 2019, 12:17:34 PM
    Author     : ramesh.avv
--%>

<%@page import="seeds.masters.daoImpl.UmoDaoImpl"%>
<%@page import="pojo.TblUmoMaster"%>
<%@page import="seeds.global.service.DaoFactory"%>
<%@page import="seeds.masters.daoImpl.MaterialDaoImpl"%>
<%@page import="java.util.List"%>
<%@page import="pojo.TblMaterialMaster"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib uri="/WEB-INF/displaytag.tld" prefix="display" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="../header/header.jsp" %>    
        <title>Plant Inventory</title>
         <link href="images/Nsllogo.png" rel="icon">
        <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                Quality Control Check
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Plant Indent</a></li>
                <li class="active">Quality Control Check</li>
            </ol>
        </section>
        
       <s:form action="deleteQmPlantIndent.action" id="upload_form" method="post"> 
                            <s:hidden id="indent_Id1" name="indNo" />
                            <s:hidden id="indent_Id1212" name="indentDeoComments" />
                        </s:form>     

        <section class="content">
            <div class="row">
                
                
                <!-- left column -->
                <div class="col-md-12">
                    <!-- general form elements -->
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">Quality Control Check</h3>
                        </div>
                        <!-- /.box-header -->
                        <!-- form start -->
                        <form action="saveQialityManagerIndentPzRequest.action" method="post" name="myForm" onsubmit="return validateForm()" class="form-horizontal">
                            <div class="box-body"> 
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="col-sm-3 control-label">Year</label> 
                                            <div class="col-xs-6">
                                                <s:textfield name="finYear" readonly="true" cssClass="form-control"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-3 control-label">Employee Id</label> 
                                            <div class="col-xs-6">
                                                <s:textfield name="empId" readonly="true" cssClass="form-control"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="col-sm-3 control-label">Date</label>
                                            <div class="col-xs-6">
                                                <s:textfield name="date" readonly="true" cssClass="form-control"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-3 control-label">Indent No.</label>
<!--                                            <div class="col-xs-6">
                                                
                                                <s:select name="indentsId"  id="indentNo" list="listTblpzIndentMaster" onchange="this.form.action='getflindentdetails.action'; this.form.submit();" headerKey="0" headerValue="--- Select ---" listKey="indentId" listValue="indentNo" cssClass="form-control select2"/>
                                            </div>-->
                                                <div class="col-xs-6">
                                               <s:textfield name="indNo" readonly="true" cssClass="form-control"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="col-sm-3 control-label">Company</label>
<!--                                            <div class="col-xs-6">                                                                                                       
                                               <s:select name="compCode" id="compName" list="listTblPzCompanyMaster" onchange="this.form.action='getIndentnumbers.action'; this.form.submit();"  headerKey="0" headerValue="--- Select ---" listKey="compCode" listValue="compName" cssClass="form-control select2"/>
                                            </div>-->
                                               <div class="col-xs-6">
                                               <s:textfield name="CompId" readonly="true" cssClass="form-control"/>
                                            </div>
                                        </div>

<!--                                        <div class="form-group">
                                            <label class="col-sm-3 control-label">Section</label> 
                                            <div class="col-xs-6">
                                               <s:textfield name="indNo" readonly="true" cssClass="form-control"/>
                                            </div>
                                        </div>-->
                                            
                                            <div class="form-group">
                                                
                                            <label class="col-sm-3 control-label">Department</label> 
                                            <div class="col-xs-6">
                                               <s:textfield name="DeptName" readonly="true" cssClass="form-control"/> 
                                            </div>
                                            
                                        </div>
                                               
                                               <div class="form-group">
                                                   
                                                <label class="col-sm-3 control-label">Processing/Packing</label> 
                                                <div class="col-xs-6">

                                                    <s:textfield name="packandprocessing" readonly="true" cssClass="form-control"/> 
                                                </div>
                                                
                                            </div>
                                               
                                               
                                               <div class="form-group">
                                                   
                                                <label class="col-md-3 control-label">Order Type</label>
                                                <div class="col-xs-6">
                                                    <s:textfield name="orderNoid" readonly="true" cssClass="form-control"/> 
                                                </div>
                                                
                                            </div>
                                            
                                            
                                            
                                            <div class="form-group">
                                                
                                            <label class="col-md-3 control-label">Line Code</label>
                                            <div class="col-xs-6">
                                                <s:textfield name="lincodabc" readonly="true" cssClass="form-control"/> 
                                            </div>
                                           
                                        </div>
                                               
                                               <div class="form-group">
                                               
                                            <label class="col-md-3 control-label">Batch Number</label>
                                            <div class="col-xs-6">
                                                <s:textfield name="batchNumberIndent" readonly="true" cssClass="form-control"/> 
                                            </div>
                                            
                                        </div>
                                    </div>
                                            
                                            
                                    <div class="col-md-6">
                                        
                                            
                                        
                                        <div class="form-group">
                                              
                                            <label class="col-md-3 control-label">Plant</label>
                                            <div class="col-xs-6">
                                                
                                                <s:textfield name="PlantId" readonly="true" cssClass="form-control"/> 
                                            </div>
                                           
                                        </div>
                                        
                                        <div class="form-group">
                                                 
                                            <label class="col-md-3 control-label">Crop</label>
                                            <div class="col-xs-6">
                                                
                                               
                                                <s:textfield name="indentCrop" readonly="true" cssClass="form-control"/>
                                                
                                            </div>
                                              
                                        </div>
                                            
                                            <div class="form-group">
                                                
                                            <label class="col-md-3 control-label">Material Code</label>
                                            <div class="col-xs-6">
                                                
                                                <s:textfield name="myoutputmata" readonly="true" cssClass="form-control"/>
                                                
                                            </div>
                                                
                                        </div>
                                            
<!--                                            <div class="form-group">
                                               
                                            <label class="col-md-3 control-label">Quantity Receipt</label>
                                            <div class="col-xs-6">
                                                
                                               <s:textfield name="indNo" readonly="true" cssClass="form-control"/>
                                               
                                            </div>
                                             
                                        </div>-->
                                        <div class="form-group">
                                       
                                            <label class="col-md-3 control-label">UOM</label>
                                            <div class="col-xs-6">
                                               <s:textfield name="matumoa" readonly="true" cssClass="form-control"/> 
                                            </div>
                                        
                                        </div>
                                            
                                        <div class="form-group">
                                     
                                            <label class="col-md-3 control-label">Expected output Qty</label>
                                            <div class="col-xs-6">
                                                <span class="field">
                                                            <s:textfield name="expoutPut" readonly="true" cssClass="form-control"/>                       
                                                </span>
                                            </div>
                                           
                                        </div>

                                                <div class="form-group">
                                                
                                                <label class="col-md-3 control-label">Start Date</label>
                                                <div class="col-xs-6">
                                                    <span class="field">
                                                        <s:textfield name="fromDate" readonly="true" cssClass="form-control pull-right"  id="datepicker"/>                        
                                                    </span>	

                                                </div> 
                                                </div>

<!--                                            <div class="form-group">
                                                
                                                    
                                                <label class="col-md-3 control-label">Order Number</label>
                                                <div class="col-xs-6">
                                                    <span class="field">
                                                        <s:textfield name="indentOrderNumbera" cssClass="form-control pull-right"  id="datepicker"/>                        
                                                    </span>	

                                                </div> 
                                                   
                                                   



                                            </div>-->
                                    
                                </div>
                                </div>
                                <div class="row">
                                    <div  class="col-md-12" style="overflow-y: hidden;overflow-x: auto;">
                                        <table id="example1" class="table table-bordered table-striped" style="overflow-y: hidden;overflow-x: auto;padding-bottom: 10px">
                                            <thead>
                                                <tr>
                                                    <th>Material Code</th>
                                                    <th>Description</th>
                                                    <th>Lot Number</th>
                                                    <th>Storage Location</th>
                                                    <th>Variety</th>
                                                    <th>UOM</th>
                                                    <th>Available Stock</th>
                                                    <th>Issued Stock</th>
                                                    <th>STL</th>
                                                    <th>ODV</th>
                                                    <th>GOT</th>
                                                    <th>ELISA</th>
                                                    <th>SKIPD</th>
                                                    
                                                    <th>STATS</th>
                                                    <th>SKIPD</th>
                                                    <th>INSPDT</th>
                                                    <th>MOISTURE</th>
                                                    <th>PURE SEED </th>
                                                    
                                                    <th>INERT MATTER </th>
                                                    <th>OCS COUNT </th>
                                                    <th>WEED SEED COUNT</th>
                                                    
                                                    <th>GRAIN</th>
                                                    
                                                    <th>BLACK SEEDS</th>
                                                    <th>PIN HOLE SEEDS</th>
                                                    <th>ODV RES</th>
                                                    <th>BULK DENSITY</th>
                                                    <th>THSW</th>
                                                    <th>COLD VIGOUR GERM NORMAL</th>
                                                    <th>FIRST COUNT NORMAL</th>
                                                    <th>GERM NORMAL </th>
                                                    <th>FET NORMAL </th>
                                                    
                                                    <th>SOIL COUNTDAYS </th>
                                                    <th>AAV GERM NORMAL </th>
                                                    <th>GOT GP</th>
                                                    <th>GOT FEMALE  </th>
                                                    <th>GOT OTHERS  </th>
                                                    <th>BG1 </th>
                                                    <th>BG2</th>
                                                    <th>HT</th>
                                                    <th>FQR </th>
                                                    
                                                    
                                                    <!--<th>Action</th>-->
                                                </tr>
                                            </thead>
                                            <tbody>
                                                
                                                <s:iterator value="listTblPzIndentDetails">
                                                    
                                                    <tr>
                                                        <td><s:property value="indentMatel"/></td>
                                                        <td><s:property value="indentMateDesc"/></td>
                                                        <td><s:property value="indentLoteNum"/></td>
                                                        <td><s:property value="indentStoLoc"/></td>
                                                        <td><s:property value="indentDetailsPurpose"/></td>
                                                        <td><s:property value="indentDetailsVendor"/></td>
                                                        <td><input id="recstock[]" value="<s:property value="indentDetailsStockAval"/>" name="stock" type="text" readonly="readonly" class="form-control" style="width: 90px;"></td>
                                                        <td><input id="recqty[]" value="<s:property value="indentDetailsQty"/>" readonly="readonly"  name="qty" type="text" class="form-control" style="width: 90px;"></td>
                                                        <td><s:property value="matStl"/></td>
                                                        <td><s:property value="matOdv"/></td>
                                                        <td><s:property value="matGot"/></td>
                                                        <td><s:property value="matElisa"/></td>
                                                        <td><s:property value="sKIPD"/></td>
                                                        
                                                        <td><s:property value="sTATS"/></td>
                                                        <td><s:property value="sKIPD"/></td>
                                                        <td><s:property value="iNSPDT"/></td>
                                                        <td><s:property value="mOISTURE"/></td>
                                                        <td><s:property value="PURESEED"/></td>
                                                        
                                                        <td><s:property value="INERTMATTER"/></td>
                                                        <td><s:property value="OCSCOUNT"/></td>
                                                        <td><s:property value="WEEDSEEDCOUNT"/></td>
                                                        <td><s:property value="gRAIN"/></td>
                                                        
                                                        
                                                        <td><s:property value="BLACKSEEDS"/></td>
                                                        <td><s:property value="PINHOLESEEDS"/></td>
                                                        <td><s:property value="ODVRES"/></td>
                                                        <td><s:property value="BULKDENSITY"/></td>
                                                        <td><s:property value="tHSW"/></td>
                                                        <td><s:property value="COLDVIGOURGERMNORMAL"/></td>
                                                        <td><s:property value="FIRSTCOUNTNORMAL"/></td>
                                                        <td><s:property value="GERMNORMAL"/></td>
                                                        <td><s:property value="FETNORMAL"/></td>
                                                        
                                                        
                                                        <td><s:property value="SOILCOUNTDAYS"/></td>
                                                        <td><s:property value="AAVGERMNORMAL"/></td>
                                                        <td><s:property value="GOTGP"/></td>
                                                        <td><s:property value="GOTFEMALE"/></td>
                                                        <td><s:property value="GOTOTHERS"/></td>
                                                        <td><s:property value="bG1"/></td>
                                                        <td><s:property value="bG2"/></td>
                                                        
                                                        <td><s:property value="hT"/></td>
                                                        <td><s:property value="fQR"/></td>
                                                        <!--<td><input type="button" value="View Quality Results" onclick="OpenLoginPopup()"/></td>-->
                                                    </tr>
                                                    
                                                    
                                                
                                            <div id="loginDiv" style="display: none;text-align: justify;padding: 20px">
                                                <div class="col-md-4" style=" border-right: 3px solid gray;">          
                                    <label style="width:40%">STL  </label>:    <s:property value="matStl"/><br />
                                    <label style="width:40%">ODV  </label>:     <s:property value="matOdv"/><br />
                                    <label style="width:40%">GOT  </label>:     <s:property value="matGot"/><br />
                                    <label style="width:40%">ELISA  </label>:  <s:property value="matElisa"/><br />
                                    <label style="width:40%">SDCLS </label>:  <s:property value="sDCLS"/><br />
                                    
                                    <label style="width:40%">STATS  </label>:  <s:property value="sTATS"/><br />
                                    <label style="width:40%">SKIPD  </label>:  <s:property value="sKIPD"/><br />
                                    <label style="width:40%">INSPDT </label>: <s:property value="iNSPDT"/><br />
                                    <label style="width:40%">MOISTURE  </label>:   <s:property value="mOISTURE"/><br />
                                    <label style="width:40%">PURE SEED </label>:  <s:property value="PURESEED"/><br />
                                    <label style="width:40%">INERT MATTER  </label>:   <s:property value="INERTMATTER"/><br />
                                    <label style="width:40%">OCS COUNT  </label>:  <s:property value="OCSCOUNT"/><br />
                                    <label style="width:50%">WEED SEED COUNT  </label>: <s:property value="WEEDSEEDCOUNT"/><br />
                                    <label style="width:40%">GRAIN  </label>:   <s:property value="gRAIN"/><br />
                                    
                                    
                                    
                                    </div>
                                    <div class="col-md-4" style=" border-right: 3px solid gray;">
                                    <label style="width:40%">BLACK SEEDS</label>:   <s:property value="BLACKSEEDS"/><br />
                                    <label style="width:40%">PIN HOLE SEEDS </label>:   <s:property value="PINHOLESEEDS"/><br />
                                    <label style="width:40%">ODV RES </label>:   <s:property value="ODVRES"/><br />
                                    <label style="width:40%">BULK DENSITY</label>:   <s:property value="BULKDENSITY"/><br />
                                    <label style="width:40%">THSW  </label>:   <s:property value="tHSW"/><br />
                                    <label style="width:70%">COLD VIGOUR GERM NORMAL</label>:   <s:property value="COLDVIGOURGERMNORMAL"/><br />
                                    <label style="width:60%">FIRST COUNT NORMAL </label>:   <s:property value="FIRSTCOUNTNORMAL"/><br />
                                    <label style="width:40%">GERM NORMAL </label>:   <s:property value="GERMNORMAL"/><br />
                                    <label style="width:40%">FET NORMAL </label>:   <s:property value="FETNORMAL"/><br />
                                    <label style="width:40%">SOIL COUNTDAYS </label>:   <s:property value="SOILCOUNTDAYS"/><br />
                                    <label style="width:50%">AAV GERM NORMAL</label>:   <s:property value="AAVGERMNORMAL"/><br />
                                    <label style="width:40%">GOT GP</label>:   <s:property value="GOTGP"/><br />
                                    <label style="width:40%">GOT FEMALE </label>:   <s:property value="GOTFEMALE"/><br />
                                    <label style="width:40%">GOT OTHERS </label>:   <s:property value="GOTOTHERS"/><br />
                                    
                                    
                                    </div>
                                    
                                    <div class="col-md-4">
                                        
                                        <label style="width:40%">BG1  </label>:   <s:property value="bG1"/><br />
                                    <label style="width:40%">BG2  </label>:   <s:property value="bG2"/><br />
                                    <label style="width:40%">HT  </label>:   <s:property value="hT"/><br />
                                    <label style="width:40%">FQR  </label>:   <s:property value="fQR"/><br />
                                    <!--<label style="width:40%">Q1  </label>:   <s:property value="q1"/><br />-->
                                    <!--<label style="width:40%">Q2  </label>:   <s:property value="q2"/><br />-->
<!--                                    <label style="width:40%">Q3  </label>:   <s:property value="q3"/><br />
                                    <label style="width:40%">Q4  </label>:   <s:property value="q4"/><br />
                                    <label style="width:40%">Q5  </label>:   <s:property value="q5"/><br />
                                    <label style="width:40%">Q6  </label>:   <s:property value="q6"/><br />
                                    <label style="width:40%">Q7  </label>:   <s:property value="q7"/><br />
                                    <label style="width:40%">Q8  </label>:   <s:property value="q8"/><br />
                                    <label style="width:40%">Q9  </label>:   <s:property value="q9"/><br />-->
                                        
                                        
                                        
                                    </div>
                                    <div class="col-sm-12" style="text-align:center;padding-top: 20px;">

                                        <input type="button" value="Close" style="background-color: #3366ff;color:white;border-radius: 12px;text-align: center;width: 100px" onclick="ClosePopupDiv('loginDiv')" /><br />
                                        </div>
                                    </div>
                                                   
                                                </s:iterator>
                                                     
                                            </tbody>
                                        </table>
                                        </div>
                                    </div>
<!--                                <div class="row" style="padding-left:20px">
                                    <div class="col-md-4">
                                        <div class="form-group">
                                            <div class="col-xs-2">
                                                <a href="javascript:void(0);" id="addCF"><span class="btn btn-primary">Add</span></a>
                                                <input type="button" value="Add" class="btn btn-primary" onclick="getAddRow();">
                                            </div>
                                             /.col 
                                            <div class="col-xs-2">                                                        
                                                <input type="button" value="Remove" class="btn btn-danger" onclick="getRemoveRow();"> 
                                            </div>
                                        </div>
                                         /.col 
                                    </div>
                                </div>-->
                                <div class="row">
                                    <div class="form-group">
                                        
                                        <label class="col-sm-2 control-label">Plant Manager Comments/Remarks</label> 
                                        <div class="col-xs-9">
                                            <s:textarea readonly="true" name="comments"  cssClass="form-control"/>
                                        </div>
                                        
                                    </div>
                                    
                                    <div class="form-group">
                                       
                                        
                                        <label class="col-sm-2 control-label">Quality Manager Comments</label> 
                                        <div class="col-xs-9">
                                            <s:textarea name="indentDeoComments"   id = "valueone" cssClass="form-control"/>
                                        </div>
                                        
                                        
                                    </div>
                                </div>                                   
                                <div class="box-footer" style="padding-left:20px;text-align: center">
                                    <button id= "sbtbtn" type="submit" class="btn btn-primary" >Submit</button>
<!--                                    <a href="<s:url action="indentRequestList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>-->
                                    
                                    <button id= "sbtbtn" type="submit" onclick="confirmDelete(); return false;" class="btn btn-danger" >Reject Indent</button>
<!--                                    <a href="<s:url action="indentRequestList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>-->
                                
                                </div>
                                
                            </div>
                                
                            <!-- /.box-body -->
                        </form>
                    </div>
                </div>
            </div>
            <!-- /.box -->
        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->
    <footer class="main-footer">
        <div class="pull-right hidden-xs">
            <b>Version</b> 1.0.0
        </div>
        <strong>Copyright &copy; 2019 <a href="http://www.nuziveeduseeds.com/">Nuziveedu Seeds</a>.</strong> All rights reserved.
    </footer><!-- jQuery 3 -->
    <script src="Seeds/assets/jquery/dist/jquery.min.js"></script>
    <!-- jQuery UI 1.11.4 -->
    <script src="Seeds/assets/jquery-ui/jquery-ui.min.js"></script>
    <!-- Resolve conflict in jQuery UI tooltip with Bootstrap tooltip -->
    <script>
                                                    $.widget.bridge('uibutton', $.ui.button);
    </script>
    <!-- Bootstrap 3.3.7 -->
    <script src="Seeds/assets/bootstrap/dist/js/bootstrap.min.js"></script>
    <!-- Morris.js charts -->
    <script src="Seeds/assets/raphael/raphael.min.js"></script>
    <script src="Seeds/assets/morris.js/morris.min.js"></script>
    <!-- Sparkline -->
    <script src="Seeds/assets/jquery-sparkline/dist/jquery.sparkline.min.js"></script>
    <!-- jvectormap -->
    <script src="Seeds/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
    <script src="Seeds/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
    <!-- jQuery Knob Chart -->
    <script src="Seeds/assets/jquery-knob/dist/jquery.knob.min.js"></script>
    <!-- daterangepicker -->
    <script src="Seeds/assets/moment/min/moment.min.js"></script>
    <script src="Seeds/assets/bootstrap-daterangepicker/daterangepicker.js"></script>
    <!-- datepicker -->
    <script src="Seeds/assets/bootstrap-datepicker/dist/js/bootstrap-datepicker.min.js"></script>
    <!-- Bootstrap WYSIHTML5 -->
    <script src="Seeds/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
    <!-- Slimscroll -->
    <script src="Seeds/assets/jquery-slimscroll/jquery.slimscroll.min.js"></script>
    <!-- FastClick -->
    <script src="Seeds/assets/fastclick/lib/fastclick.js"></script>
    <!-- AdminLTE App -->
    <script src="Seeds/dist/js/seeds.min.js"></script>
    <!-- AdminLTE dashboard demo (This is only for demo purposes) -->
    <script src="Seeds/dist/js/pages/dashboard.js"></script>
    <!-- AdminLTE for demo purposes -->
    <script src="Seeds/dist/js/demo.js"></script>
    <!-- Select2 -->
    <script src="Seeds/assets/select2/dist/js/select2.full.min.js"></script>
    <!-- InputMask -->
    <script src="Seeds/plugins/input-mask/jquery.inputmask.js"></script>
    <script src="Seeds/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
    <script src="Seeds/plugins/input-mask/jquery.inputmask.extensions.js"></script>
    <!-- bootstrap color picker -->
    <script src="Seeds/assets/bootstrap-colorpicker/dist/js/bootstrap-colorpicker.min.js"></script>
    <!-- bootstrap time picker -->
    <script src="Seeds/plugins/timepicker/bootstrap-timepicker.min.js"></script>
    <!-- iCheck 1.0.1 -->
    <script src="Seeds/plugins/iCheck/icheck.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    
    
    <script>
            function confirmDelete(){
                a = document.getElementById("valueone").value;
//                alert(a);
                Swal.fire({
  title: 'Are you sure?',
  text: "Are You Sure! ,You want to delete the Indent",
  icon: 'success',
  showCancelButton: true,
  confirmButtonColor: '#3085d6',
  cancelButtonColor: '#d33',
  confirmButtonText: 'Yes, Delete It!',
 
}).then((result) => {
    
  if (result.isConfirmed) {
      document.getElementById("indent_Id1212").value = a;
      document.getElementById("upload_form").submit();
      
  } 
});           
                        }

                        
    
        </script>
    
    
    
    <script>

                                                    var s = document.createElement("script");
                                                    s.type = "text/javascript";
                                                    s.src = "Seeds/assets/select2/dist/js/select2.full.min.js";
                                                    var i = 2;
                                                    $("#addCF").click(function () {
                                                        $("#example1").append('<tr>"+ i +"<td><div class="form-group"><div class="col-md-12"><select id="md[]" name="md" class="form-control select2"><s:iterator value="listTblMaterialMaster"><option value="<s:property value="materialId "/>"><s:property value="materialName"/></option></s:iterator></select></div></div></td><td><div class="form-group"><div class="col-md-12"><select id="umo[]" name="umo"  class="form-control select2"><s:iterator value="listTblUmoMaster"><option value="<s:property value="umoId"/>"><s:property value="umoCode"/></option></s:iterator></select></div></div></td><td><div class="form-group"><div class="col-md-12"><input id="pur[]"  name="pur" type="text" class="form-control"></div></div></td><td><div class="form-group"><div class="col-md-12"><input id="ven[]"  name="ven" type="text" class="form-control"></div></div></td> <td><div class="form-group"><div class="col-md-12"><input id="qty[]"  name="qty" type="text" class="form-control"></div></div></td><td><div class="form-group"><div class="col-md-12"><input id="stock[]"  name="stock" type="text" onblur="CheckQty();" class="form-control"></div></div><a href="javascript:void(0);" class="remCF"><span class="btn btn-danger">Remove</span></a></td>  </tr>');
                                                        $.getScript("Seeds/assets/select2/dist/js/select2.full.min.js");
                                                        i++;
                                                        $(".remCF").on('click', function () {
                                                            $(this).parent().parent().remove();
                                                        });
                                                    });


        </script>
        
        
        <script>
   
            
            
            
            function CheckQty() {
                var q = document.getElementsByName("qty");
                var s = document.getElementsByName("stock");
                var r = document.getElementsByName("sbtbtn");
                
                for (var i = 0; i < q.length; i++) {
                    //alert(s[i].value);
                    if (parseFloat(s[i].value, 10) < parseFloat(q[i].value, 10)) {
                        
                        alert("Avaliable stock should be less than Quantity Requested");
                        q[i].value = "";
                       
                    }
                }
            }

            function getAddRow() {

                var row = $('#example1').find('tr').length - 2;
                var $str = $('#example1 tr:last').html();

                $('#example1 tr:last').after("<tr>" + $str + "</tr>");

                var tr = $('#example1').find('tr').last();

                tr.find('select[name=md]').attr('id', 'md' + (row + 1));
                tr.find('select[name=md]').val($("'select[name=md] option:first").val());
                $('#md' + (row + 1)).val("");

                tr.find('select[name=umo]').attr('id', 'umo' + (row + 1));
                tr.find('select[name=umo]').val($("'select[name=umo] option:first").val());
                $('#umo' + (row + 1)).val("");

                tr.find('input[name=pur]').attr('id', 'pur' + (row + 1));
                $('#pur' + (row + 1)).val("");

                tr.find('input[name=ven]').attr('id', 'ven' + (row + 1));
                $('#ven' + (row + 1)).val("");

                tr.find('input[name=qty]').attr('id', 'qty' + (row + 1));
                $('#qty' + (row + 1)).val("");

                tr.find('input[name=stock]').attr('id', 'stock' + (row + 1));
                /*tr.find('input[name=stock]').blur(function() {
                 var q = document.getElementsByName("qty");
                 var s = document.getElementsByName("stock");
                 for (var i = 0; i < q.length; i++) {
                 if (parseFloat(s[i].value, 10) >= parseFloat(q[i].value, 10)) {
                 alert("Avaliable stock should be less than Quantity Requested");
                 s[i].value = "";
                 }
                 }
                 });
                 $('#stock' + (row + 1)).val("");*/

                this.onchangeevent();

            }

            function getRemoveRow() {

                try {
                    var row = $('#example1').find('tr').length - 2;
                    if (row > 0) {
                        $('#example1 tr:last').remove();
                    }
                } catch (e) {
                    alert(e);
                }
                this.onchangeevent();
            }

            $(document).on('change', 'select[name="indentsId"]', function () {
                //$('#matBatch').empty();
                
                //$('#lotab').empty();
                var matId = 0;
                matId = parseInt($(this).val());
                var comp = $('#compId option:selected').val();
                var plant = $('#plantId').val();
                

                $.ajax({
                    type: "POST",
                    url: "getmaterialsgroup.action",
                     dataType: "json",
                    data: {matId: matId, comp: comp, plant: plant},
                    //data: "matId=" + matId+",
                    async: true,
                    success: function (data) {
                        
                       
                        data: JSON.stringify(data);
                        var m = JSON.parse(data);
                        var row = $('#example1').find('tr').length - 2;
                        var tr = $('#example1').find('tr').last();
                        tr.find('input[name="crosspId"]').attr('id', 'crssopId' + (row + 1));
                        $('#cssropId' + (row + 1)).val(m.qty);
                        tr.find('input[name="pur"]').attr('id', 'pur' + (row + 1));
                        $('#pur' + (row + 1)).val(m.qty2);
                        tr.find('input[name="ven"]').attr('id', 'ven' + (row + 1));
                        $('#ven' + (row + 1)).val(m.qty3);
                        tr.find('input[name="qty"]').attr('id', 'qty' + (row + 1));
                        $('#qty' + (row + 1)).val(m.qty);
                        tr.find('input[name="des"]').attr('id', 'des' + (row + 1));
                        $('#des' + (row + 1)).val(m.qty1);
                        tr.find('input[name="lot"]').attr('id', 'lot' + (row + 1));
                        $('#lot' + (row + 1)).val(m.qty4);
                        
                        tr.find('select[name="lotab"]').attr('id', 'lotab' + (row + 1));
                        tr.find('select[name="lotab"]').val($("'select[name='lotab'] option:first").val());
                        $('#lotab').empty();
                        $('#lotab' + (row + 1)).val(m.qty4);
                        


//console.log(data);
//            var selOpts = "";
//            for (i=0;i<data.length;i++)
//            console.log(data.length);
//            {
//                data: JSON.stringify(data);
//                var m = JSON.parse(data);
//                console.log(" value is : "+ m.qty);
//                var id = m.qty1;
//                var val = m.qty1;
//                var row = $('#example1').find('tr').length - 2;
//                var tr = $('#example1').find('tr').last();
//                console.log("the row is :"+row);
//                console.log("the table is :"+tr);
//                selOpts += "<option value='"+id+"'>"+val+"</option>";
//                console.log("the select ps is : "+selOpts);
//                console.log("this is log"+data);
//                
//            }$('#matBatch').append(selOpts);
                      



                    }
                
            
                });
            });


        </script>
        <script>
            $(function () {
                //Initialize Select2 Elements
                //$('.select2').select2()

                //Datemask dd/mm/yyyy
                $('#datemask').inputmask('dd/mm/yyyy', {'placeholder': 'dd/mm/yyyy'})
                //Datemask2 mm/dd/yyyy
                $('#datemask2').inputmask('mm/dd/yyyy', {'placeholder': 'mm/dd/yyyy'})
                //Money Euro
                $('[data-mask]').inputmask()

                //Date range picker
                $('#reservation').daterangepicker()
                //Date range picker with time picker
                $('#reservationtime').daterangepicker({timePicker: true, timePickerIncrement: 30, locale: {format: 'MM/DD/YYYY hh:mm A'}})
                //Date range as a button
                $('#daterange-btn').daterangepicker(
                        {
                            ranges: {
                                'Today': [moment(), moment()],
                                'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                                'Last 7 Days': [moment().subtract(6, 'days'), moment()],
                                'Last 30 Days': [moment().subtract(29, 'days'), moment()],
                                'This Month': [moment().startOf('month'), moment().endOf('month')],
                                'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
                            },
                            startDate: moment().subtract(29, 'days'),
                            endDate: moment()
                        },
                function (start, end) {
                    $('#daterange-btn span').html(start.format('MMMM D, YYYY') + ' - ' + end.format('MMMM D, YYYY'))
                }
                )

                //Date picker
                $('#datepicker').datepicker({
                    autoclose: true,
                    format: 'yyyy-mm-dd'
                })

                //iCheck for checkbox and radio inputs
                $('input[type="checkbox"].minimal, input[type="radio"].minimal').iCheck({
                    checkboxClass: 'icheckbox_minimal-blue',
                    radioClass: 'iradio_minimal-blue'
                })
                //Red color scheme for iCheck
                $('input[type="checkbox"].minimal-red, input[type="radio"].minimal-red').iCheck({
                    checkboxClass: 'icheckbox_minimal-red',
                    radioClass: 'iradio_minimal-red'
                })
                //Flat red color scheme for iCheck
                $('input[type="checkbox"].flat-red, input[type="radio"].flat-red').iCheck({
                    checkboxClass: 'icheckbox_flat-green',
                    radioClass: 'iradio_flat-green'
                })

                //Colorpicker
                $('.my-colorpicker1').colorpicker()
                //color picker with addon
                $('.my-colorpicker2').colorpicker()

                //Timepicker
                $('.timepicker').timepicker({
                    showInputs: false
                })
            })
        </script>
        <script>
            $(function () {
                $('input').iCheck({
                    checkboxClass: 'icheckbox_square-blue',
                    radioClass: 'iradio_square-blue',
                    increaseArea: '20%' /* optional */
                });
            });
        </script>
        
        <script>
function validateForm() {
  let x = document.forms["myForm"]["indentOrderNumbera"].value;
  let y = document.forms["myForm"]["indentDeoComments"].value;
  
  if (x == "") {
    alert("Order Number must be filled out.");
    return false;
  }else if (y == "") {
    alert("Comments must be filled out.");
    return false;
  }
  
}
</script>

<script>
    var hide1 = document.getElementById("hide1");
    function hideshow(){
        hide1.style.display = 'block';
    }
</script>

    <%--<script>
        var i = 2;
        $("#addCF").click(function () {
            $("#example1").append('<tr>"+ i +"<td><div class="col-xs-20"><select id="md[]" name="md" class="form-control select2"><%List<TblMaterialMaster> listTblMaterialMaster1 = DaoFactory.getDao(MaterialDaoImpl.class).getMaterial();
                for (TblMaterialMaster materialMaster1 : listTblMaterialMaster1) {
                    int mId = materialMaster1.getMaterialId();
                    String mName = materialMaster1.getMaterialName();%><option value="<%=mId%>"><%=mName%></option><%}%> </select></div></td><td><div class="col-xs-30"><select id="umo[]" name="umo"  class="form-control select2"><%List<TblUmoMaster> listTblUmoMaster1 = DaoFactory.getDao(UmoDaoImpl.class).getUmo();
                        for (TblUmoMaster umoMaster1 : listTblUmoMaster1) {
                            int uId = umoMaster1.getUmoId();
                            String uName = umoMaster1.getUmoName();%><option value="<%=uId%>"><%=uName%></option><%}%></select></div></td><td><div class="col-xs-10"><input id="pur[]"  name="pur" type="text" class="form-control"></div></td><td><div class="col-xs-10"><input id="ven[]"  name="ven" type="text" class="form-control"></div></td> <td><div class="col-xs-7"><input id="qty[]"  name="qty" type="text" class="form-control"></div></td><td><div class="col-xs-6"><input id="stock[]"  name="stock" type="text" onblur="CheckQty();" class="form-control"></div><a href="javascript:void(0);" class="remCF"><span class="btn btn-danger">Remove</span></a></td>  </tr>');
            i++;
            $(".remCF").on('click', function () {
                $(this).parent().parent().remove();
            });
        });
    </script>--%>
<script type="text/javascript">
        function OpenLoginPopup() {
            var divToOpen = "loginDiv";
            var popupSetting = { width: '1000', height: '500', title: 'QUALITY RESULTS',isFixed:true };
            ShowPopup(divToOpen, popupSetting);
        }
        // Function to Show Div Popup
        function ShowPopup(divId, popupSetting) {
            var divElt = document.getElementById(divId);
            divElt.style.display = 'block';
            var element = divElt.parentElement;
            popupSetting = popupSetting || {};
            if (!popupSetting.width) { popupSetting.width = divElt.offsetWidth; };
            if (!popupSetting.height) { popupSetting.height = divElt.offsetHeight; };
            if (!popupSetting.title) { popupSetting.title = 'Dialog' };
            var table = document.createElement('table');
            table.setAttribute('id', 'table' + divId);table.setAttribute('cellspacing', '0');table.setAttribute('cellpadding', '0');
            var tr1 = document.createElement('tr'); tr1.className = 'PopupHeader';
            var td1 = document.createElement('td'); td1.setAttribute('style', 'width: 90%; padding: 5px;');
            var span = document.createElement('span'); span.innerHTML = popupSetting.title;
            span.setAttribute('style', 'font-size: 14px; font-weight: bold;');
            td1.appendChild(span); tr1.appendChild(td1); table.appendChild(tr1);
            var tr2 = document.createElement('tr');
            var tdDynamic = document.createElement('td');
            tdDynamic.setAttribute('align', 'center');
            tdDynamic.setAttribute('style', 'padding-top: 10px; vertical-align:top;');
            var tempElt = document.createElement('div');
            tempElt.setAttribute('id', 'tempElt' + divElt.id);
            divElt.parentElement.insertBefore(tempElt, divElt);
            tdDynamic.appendChild(divElt);
            tr2.appendChild(tdDynamic);
            table.appendChild(tr2);
            var cssText = ' border:1px solid black;  z-index:92000; background-color:white; top:50%; left:50%;';
            cssText += 'width: ' + popupSetting.width + 'px; height: ' + popupSetting.height + 'px; margin-left: -' + Math.round(popupSetting.width / 2) + 'px; margin-top: -' + Math.round(popupSetting.height / 2) + 'px;';
            if (popupSetting.isFixed === true) { cssText += 'position: fixed;';}
            else { cssText += 'position: absolute;'; }
            table.setAttribute('style', cssText);
            element.appendChild(table);
            var shadeElt = document.createElement('div');
            shadeElt.id = "ShadedBG";shadeElt.className = "ShadedBG";
            tempElt.appendChild(shadeElt);
        }
 
        // Function to Close Div Popup
        function ClosePopupDiv(divId) {
            
            var table = document.getElementById('table' + divId);
            var element = table.parentElement;
            var divElt = document.getElementById(divId);
//            alert(divElt);
            divElt.style.display = 'none';
            
            var tempElt = document.getElementById('tempElt' + divId);
            
            
            tempElt.parentElement.insertBefore(divElt, tempElt);
            table.parentElement.removeChild(table);
            table.setAttribute('style', 'display: none');
            tempElt.parentElement.removeChild(tempElt);
            tempElt.parentElement.insertBefore(divElt, tempElt);
            tempElt.remove();
            
            
            
     
        }
 
    </script>
    
    <style type="text/css">
        .PopupHeader
        {
            color: white;
            background-color:#4d79ff;
            text-align: center;
        }
        .ShadedBG
        {
            width: 100%;
            height: 100%;
            position: fixed;
            z-index: 10000;
            opacity: 0.5;
            filter: alpha(opacity=80);
            -moz-opacity: 0.8;
            background-color: #B8C6B8;
            top: 0;
            left: 0;
            margin: 0;
            padding: 0;
            
        }
    </style>
    
    
    <script>
                            $(function () {
                                $('#example1').DataTable();
                                $('#example2').DataTable({
                                    'background': red,
                                    'paging': true,
                                    'lengthChange': true,
                                    'searching': true,
                                    'ordering': true,
                                    'info': true,
                                    'autoWidth': true
                                })
                            })

    </script>
    
<!--    <script>
        
        function confirmDelete()
{
var r=confirm("Are you sure you want to Delete This Indent");
if (r==true)
{
    r.submit();
//User Pressed okay. Delete

}
else
{
//user pressed cancel. Do nothing
    }
 }
        
    </script>-->

</body>
</html>
