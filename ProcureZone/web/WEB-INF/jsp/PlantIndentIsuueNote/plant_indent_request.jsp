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
<%@page import="pojo.TblPzMaterialMaster"%>
<%@page import="pojo.TblCropMaster"%>
<%@page import="pojo.TblPzScheduleMaterialmaster"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib uri="/WEB-INF/displaytag.tld" prefix="display" %>
<!--<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>-->
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="../header/header.jsp" %>    
         <title>Plant Inventory</title>
         <link href="images/Nsllogo.png" rel="icon">
        <!-- Content Wrapper. Contains page content -->
    </head>
    <body>
        <div class="content-wrapper">
            <!-- Content Header (Page header) -->
            <section class="content-header">
                <h1>
                    Indent from Plant Manager 
                    <small></small>
                </h1>
                <ol class="breadcrumb">
                    <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                    <li><a href="#">Plant Indent</a></li>
                    <li class="active">Indent from Plant Manager</li>
                </ol>
            </section>

            <section class="content">
                <div class="row">
                    <!-- left column -->
                    <div class="col-md-12">
                        <!-- general form elements -->
                        <div class="box box-primary">
                            <div class="box-header with-border">
                                <h3 class="box-title">Indent from Plant Manager</h3>
                            </div>
                            <!-- /.box-header -->
                            <!-- form start -->
                            <form action="saveIndentPzRequest.action" method="post" name="myForm" onsubmit="return validateForm()" class="form-horizontal">
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
                                                <div class="col-xs-6">
                                                    <s:textfield name="isndNo"  readonly="true" cssClass="form-control" />
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="form-group">
                                                <label class="col-sm-3 control-label">Company</label>
                                                <div class="col-xs-6">                                                                                                       
                                                    <s:select name="compCode" id="compCode" list="listTblPzCompanyMaster" onchange="this.form.action='getPlantDetails.action'; this.form.submit();"  headerKey="0" headerValue="--- Select ---" listKey="compCode" listValue="compName" cssClass="form-control select2"/>
                                                </div>
                                            </div>


                                            <div class="form-group">
                                                <label class="col-md-3 control-label">Crop Type</label>
                                                <div class="col-xs-6">                                               
                                                    <s:select name="divisionId" id="divisionId" list="listTblPzCropType" onchange="this.form.action='getCropGroup.action'; this.form.submit();" headerKey="0" headerValue="--- Select ---" listKey="divisionCode" listValue="cropName" cssClass="form-control select2"/>

                                                </div>
                                            </div>

                                            <div class="form-group">
                                                <label class="col-sm-3 control-label">Department</label> 
                                                <div class="col-xs-6">
                                                    <s:select name="deptId" id ="deptId" list="listTblDepartmentMaster" headerKey="0" headerValue="--- Select ---" listKey="deptId" listValue="deptName" cssClass="form-control select2"/>
                                                </div>
                                            </div>



                                            <div class="form-group">
                                                <label class="col-md-3 control-label">Output Materials</label>
                                                <div class="col-xs-6">
                                                    <s:select name="myoutputmata" id ="myoutputmata" list="listPzTblMaterialMaster" onchange="myFunctiona()" headerKey="0" headerValue="--- Select ---" listKey="materialCode" listValue="materialCode" cssClass="form-control select2"/>
                                                </div>
                                            </div>




                                            <div class="form-group">
                                                <div class="col-md-12" style="display: none">                                                      
                                                    <select id="matoutpumatabc" name="md"  class="form-control select2"  >

                                                        <s:iterator value="listPzTblMaterialMaster">                                                                   

                                                            <option value="<s:property value="materialId "/>"> <s:property value="materialDesc"/></option>
                                                        </s:iterator>


                                                    </select>
                                                </div>
                                                <label class="col-md-3 control-label">Output Desc</label>
                                                <div class="col-xs-6">
                                                    <s:textfield id ="myoutputmatab" readonly="true" name="myoutputmatab"  cssClass="form-control"/>
                                                </div>
                                            </div>
                                                
                                                
                                                
                                                <div class="form-group">
                                                <div class="col-md-12" style="display: none">                                                      
                                                    <select id="matumoabc" name="umo"  class="form-control select2"  >

                                                        <s:iterator value="listPzTblMaterialMaster">                                                                   

                                                            <option value="<s:property value="materialId "/>"> <s:property value="materialUom"/></option>
                                                        </s:iterator>


                                                    </select>
                                                </div>

                                                <label class="col-md-3 control-label">UOM</label>
                                                <div class="col-xs-6">
                                                    <s:textfield name="matumoa" readonly="true" id = "matumoa"  cssClass="form-control"/>
                                                </div>
                                            </div>
                                                
                                                <div class="form-group">
                                                <label class="col-md-3 control-label">Order Type</label>
                                                <div class="col-xs-6">
                                                    <s:select name="orderNoid" id = "orderNoid" list="listTblplantOrderTypea" onchange="myFunctionb();" headerKey="0" headerValue="--- Select ---" listKey="orderNoid" listValue="orderNoid +' - '+ orderDesc" cssClass="form-control select2"/>
                                                </div>
                                            </div>
                                                
                                                <div class="form-group">
                                                <div class="col-md-12" style="display: none">                                                      
                                                    <select id="orderIdpl" name="orderId"  class="form-control select2"  >

                                                        <s:iterator value="listTblplantOrderTypea">                                                                   

                                                            <option value="<s:property value="orderId "/>"> <s:property value="orderDesc"/></option>
                                                        </s:iterator>


                                                    </select>
                                                </div>

                                                <label class="col-md-3 control-label">Order Description</label>
                                                <div class="col-xs-6">
                                                    <s:textfield name="ordedescid" readonly="true" id = "ordedescid"  cssClass="form-control"/>
                                                     
                                                </div>
                                            </div>
                                       
                                        </div>
                                        <div class="col-md-6">

                                            <div class="form-group">
                                                <label class="col-md-3 control-label">Plant</label>
                                                <div class="col-xs-6">
                                                    <s:select name="plantId" list="listTblPlantMaster" headerKey="0" headerValue="--- Select ---" listKey="plantCode" listValue="{PlantCode} +' - '+  plantName" onchange="this.form.action='getLineCodea.action'; this.form.submit();" cssClass="form-control select2"/>
                                                </div>
                                            </div>


                                            <div class="form-group">
                                                <label class="col-md-3 control-label">Crop</label>
                                                <div class="col-xs-6">                                               
                                                    <s:select name="crospId" id="crospId" list="listTblCropMaster"  headerKey="0" headerValue="--- Select ---"  onchange="this.form.action='getCropwiseMat.action'; this.form.submit();" listKey="cropId"  listValue="cropName" cssClass="form-control select2"/>

                                                </div>
                                            </div>



                                            <div class="form-group">
                                                <label class="col-sm-3 control-label">Processing/Packing</label> 
                                                <div class="col-xs-6">

                                                    <s:select name="packandprocessing" id = "packandprocessing" list="ListTblProcPack" onchange="this.form.action='getCropTypepacproc.action'; this.form.submit();"  headerKey="0" headerValue="--- Select ---" listKey="colName" listValue="colName" cssClass="form-control select2"/>
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
                                                    
                                            <div class="form-group">
                                                <label class="col-md-3 control-label">Line Code</label>
                                                <div class="col-xs-6">
                                                    <s:select name="lincodabc" id = "lincodabc" list="ListTblPzLineCode" onchange="myFunctionc();" headerKey="0" headerValue="--- Select ---" listKey="lineCode" listValue="lineCode +' - '+ lineDesc +' - '+ lineActDesc " cssClass="form-control select2"/>
                                                </div>
                                            </div>
 
                                                <div class="form-group">
                                                <div class="col-md-12" style="display: none">                                                      
                                                    <select id="lineDesca" name="lineDesca"  class="form-control select2"  >

                                                        <s:iterator value="ListTblPzLineCode">                                                                   

                                                            <option value="<s:property value="lineId "/>"> <s:property value="lineDesc"/>-<s:property value="lineActivtyType"/></option>
                                                        </s:iterator>


                                                    </select>
                                                </div>

                                                <label class="col-md-3 control-label">Line Description</label>
                                                <div class="col-xs-6">
                                                    <s:textfield name="linedesca" readonly="true" id = "linedesca"  cssClass="form-control"/>
                                                     
                                                </div>
                                            </div>
                                              
                                            <div class="form-group">
                                                <label class="col-md-3 control-label">Expected Output Qty</label>
                                                <div class="col-xs-6">
                                                    <s:textfield name="expoutPut"  id="expoutPut" onblur="myFunction()" cssClass="form-control"/>
                                                </div>
                                            </div>
                                                
                                                <div class="form-group">
                                                <label class="col-md-3 control-label">Batch Number</label>
                                                <div class="col-xs-6">
                                                    <s:textfield name="batchNumberIndent" id="batchNumberIndent" cssClass="form-control"/>
                                                </div>
                                            </div>
                                                
<!--                                                <div class="form-group">
                                                <label class="col-md-3 control-label">Batch Number</label>
                                                <div class="col-xs-6">
                                                    <s:textfield name="batchNumberIndent" id="batchNumberIndent" cssClass="form-control"/>
                                                </div>
                                            </div>-->
                                                
                                      
                                        </div>
                                                
                                    </div>

                                    <div class="row" >
                                        <div  style="overflow-y: hidden;overflow-x: auto;">
                                            <table id="example1" class="table table-bordered table-striped">
                                                <thead>
                                                    <tr>
                                                        <th>Material Code</th>                                                
                                                        <th>Description</th>
                                                        <th>Lot Number</th>
                                                        <th>Storage Location</th>
                                                        <th>Variety</th>
                                                        <th>UOM</th>
                                                        <th>Available Stock</th>
                                                        <th>Issue Stock</th>
                                                        <th>STL</th>
                                                        <th>ODV</th>
                                                        <th>GOT</th>
                                                        <th>ELISA</th>
                                                        <th>SDCLS</th>
                                                        <th>SKIPD</th>
                                                       
                                                        <th>INSPDT</th> 
                                                        <th>MOISTURE</th> 
                                                        <th>PURE SEED</th> 
                                                        <th>INERT MATTER</th> 
                                                        <th>OCS COUNT</th> 
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
                                            <tr>
                                                <td>
                                                  
                                                        <div class="form-group">
                                                            <div class="col-md-12">                                                      
<!--                                                                <select id="companyId" name="materialaCode" class="form-control select2" onchange="this.form.action = 'getmaterialsgroup.action'; javascript:submit();"  >-->
                                                                    <select id="md" name="md"  class="form-control select2" style="width: 200px;">

                                                                    <s:iterator value="listPzTblPzScheduleMaterialmaster">
                                                                        <option  value="<s:property value="companyId"/>" ><s:property value="materialaCode" /> - <s:property value="matBatch" /> - <s:property value="storageLocation" /></option>
                                                                    </s:iterator>
                                                                    </select>
                                                            </div>
                                                        </div>
                                                </td>
                                                
<!--                                                <td>
                                                    <div>
                                                        <div class="form-group">
                                                            <div class="col-md-12">                                                      
                                                                <select id="companyId" name="materialaCode" class="form-control select2" onchange="this.form.action = 'getmaterialsgroup.action'; javascript:submit();"  >
                                                                    <select id="mb" name="mb"  class="form-control select2" style="width: 200px;">

                                                                    <s:iterator value="listPzTblPzScheduleMaterialmaster">
                                                                        <option value="<s:property value="matBatch"/>" ><s:property value="matBatch" /> </option>
                                                                        

                                                                    </s:iterator>



                                                                </select>
                                                            </div>
                                                        </div>
                                                </td>-->
                                                
 


                                                <td>
                                                    <div class="form-group">


                                                <div class="col-md-12">
<!--                                                    <input id="createais" name="qtyaa" type="text" class="form-control" style="width: 120px;">-->
                                                    <input id="des[]" readonly name="des" type="text" class="form-control" style="width: 200px;" >
                                                </div>
                                                        </div>
                                                </td>
                                                
                                                <td>
                                        <div class="form-group">
                                            
                                            <div class="form-group">
                                                <div class="col-md-12">
<!--                                                    <input id="createais" name="qtyaa" type="text" class="form-control" style="width: 120px;">-->
                                                    <input id="lot[]" readonly name="lot" type="text" class="form-control" style="width: 100px;">
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    
                                    <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="stgLoc[]" name="stgLoc"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                    
                                    <td>
                                        <div class="form-group">
                                            
                                            <div class="form-group">
                                                <div class="col-md-12">
<!--                                                    <input id="createais" name="qtyaa" type="text" class="form-control" style="width: 120px;">-->
                                                    <input id="pur[]" readonly name="pur" type="text" class="form-control">
                                                </div>
                                            </div>
                                            
                                            
                                            
                                            <div class="form-group" style="display: none">
                                                <div class="col-md-12">
<!--                                                    <input id="createais" name="qtyaa" type="text" class="form-control" style="width: 120px;">-->
                                                    <input id="hidmat[]" readonly name="hidmat" type="text" class="form-control">
                                                </div>
                                            </div>
                                        </div>
                                    </td>


                                    <td>
                                        <div class="form-group">
                                            
                                            <div class="col-md-12">
<!--                                                <input id="myuom" name="qtysaa" type="text" class="form-control" style="width: 60px;">-->
                                                <input id="ven[]" readonly name="ven" type="text" class="form-control" style="width: 50px;">
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="stock[]" name="stock"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                    <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input id="qty[]"  onchange="CheckQty();" name="qty" type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                              <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="stl[]" name="stl"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="odv[]" name="odv"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="got[]" name="got"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="elisa[]" name="elisa"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="sdcls[]" name="sdcls"  type="text" class="form-control" style="width: 110px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="skipd[]" name="skipd"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
<!--                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input type="button" value="Quality" class="btn btn-warning" onclick="getRemoveRow();"> 
                                                            <a href="">Quality Check</a>
                                                        </div>
                                                    </div>
                                                </td>-->
                                                
                                                
                                                
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input  readonly id="iNSPDT[]" name="iNSPDT"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="mOISTURE[]" name="mOISTURE"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="PURESEED[]" name="PURESEED"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="INERTMATTER[]" name="INERTMATTER"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly  id="OCSCOUNT[]" name="OCSCOUNT"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="WEEDSEEDCOUNT[]" name="WEEDSEEDCOUNT"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="gRAIN[]" name="gRAIN"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="BLACKSEEDS[]" name="BLACKSEEDS"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="PINHOLESEEDS[]" name="PINHOLESEEDS"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="ODVRES[]" name="ODVRES"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="BULKDENSITY[]" name="BULKDENSITY"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="tHSW[]" name="tHSW"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="COLDVIGOURGERMNORMAL[]" name="COLDVIGOURGERMNORMAL"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="FIRSTCOUNTNORMAL[]" name="FIRSTCOUNTNORMAL"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="GERMNORMAL[]" name="GERMNORMAL"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                
                                                <td > 
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="FETNORMAL[]" name="FETNORMAL"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="SOILCOUNTDAYS[]" name="SOILCOUNTDAYS"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="AAVGERMNORMAL[]" name="AAVGERMNORMAL"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td >
                                                
                                                
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="GOTGP[]" name="GOTGP"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="GOTFEMALE[]" name="GOTFEMALE"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="GOTOTHERS[]" name="GOTOTHERS"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="bG1[]" name="bG1"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="bG2[]" name="bG2"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="hT[]" name="hT"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                <td >
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="fQR[]" name="fQR"  type="text" class="form-control" style="width: 60px;">
                                                        </div>
                                                    </div>
                                                </td>
                                                
                                                
                                                
                                                
                                                
                                                
                                                
                                                <!--
-->                                                
<!--<td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input   readonly id="stl[]" name="stl"  type="text" class="form-control">
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly  id="stl[]" name="stl"   type="text" class="form-control">
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input   readonly  id="stl[]" name="stl"  type="text" class="form-control">
                                                        </div>
                                                    </div>
                                                </td>
                                                -->
                                                
                                                
                                                
                                            </tr>
                                            
                                              
                                        </tbody>
                                    </table>
                                </div>

<!--                                                    </form>-->
                                <div class="row" style="padding-left:20px">
                                    <div class="col-md-4">
                                        <div class="form-group">
                                            <div class="col-xs-2">
                                                <!--                                                <a href="javascript:void(0);" id="addCF"><span class="btn btn-primary">Add</span></a>-->
                                                <input type="button" value="Add" class="btn btn-primary" onclick="getAddRow();">
                                            </div>

                                            <div class="col-xs-2">                                                        
                                                <input type="button" value="Remove" class="btn btn-danger" onclick="getRemoveRow();"> 
                                            </div>
                                        </div>

                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group">
                                        <label class="col-sm-2 control-label">Comments/Remarks</label> 
                                        <div class="col-xs-9">
                                            <s:textarea name="comments"  cssClass="form-control"/>
                                        </div>
                                    </div>
                                </div>                                   
                                <div class="box-footer" >
                                    <button type="submit"  class="btn btn-primary pull-right">Submit</button>
<!--                                    <a href="<s:url action="indentRequestList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>-->
                                </div>
                        </div>
                      </div>
                                </form> 
                                 
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
        
        <script src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.3/js/select2.min.js"/>
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
       
        <script>
            function myFunctiona() {
                var x = document.getElementById("myoutputmata").selectedIndex;
                var y = document.getElementById("myoutputmata").options;

                aset = y[x].index;
                
                var a = document.getElementById("matoutpumatabc").selectedIndex;
                var b = document.getElementById("matoutpumatabc").options;
                var c = document.getElementById("matumoabc").options;
                

                //alert(b[a].index) ;

                bset = b[a].index;

                if (aset != "abcd") {
                    // alert("hi");
                    //alert(b[x].text);
                    create = b[x - 1].text;
                    umocreate = c[x - 1].text;
                    
                    // alert(create);

                    document.getElementById("myoutputmatab").value = create;
                    document.getElementById("matumoa").value = umocreate;
                    


                }


            }
        </script> 
        
        <script>
            function myFunctionb() {
                var x1 = document.getElementById("orderNoid").selectedIndex;
                var y1 = document.getElementById("orderNoid").options;

                aset = y1[x1].index;
                //alert(aset);
                
                var a1 = document.getElementById("orderIdpl").selectedIndex;
                var b1 = document.getElementById("orderIdpl").options;
               // var c1 = document.getElementById("matumoabc").options;
                

                //alert(b[a].index) ;

                bset = b1[a1].index;

                if (aset != "abcd") {
                    // alert("hi");
                    //alert(b[x].text);
                    create = b1[x1 - 1].text;
                    //umocreate = c1[x1 - 1].text;
                    
                     //alert(create);

                    document.getElementById("ordedescid").value = create;
                   // document.getElementById("ordedescid").value = umocreate;
                    


                }


            }
        </script> 
        
        <script>
            function myFunctionc() {
                var x2 = document.getElementById("lincodabc").selectedIndex;
                var y2 = document.getElementById("lincodabc").options;

                aset = y2[x2].index;
                
                //alert(aset);
                
                var a2 = document.getElementById("lineDesca").selectedIndex;
                var b2 = document.getElementById("lineDesca").options;
               // var c1 = document.getElementById("matumoabc").options;
                

                //alert(b[a].index) ;

                bset = b2[a2].index;

                if (aset != "abcd") {
                     //alert("hi");
                    //alert(b[x].text);
                    create = b2[x2 - 1].text;
                    //umocreate = c1[x1 - 1].text;
                    
                     //alert(create);

                    document.getElementById("linedesca").value = create;
                   // document.getElementById("ordedescid").value = umocreate;
                    


                }


            }
        </script>  
        
        
        
        
        
<!--        <script>

//                                                    var s = document.createElement("script");
//                                                    s.type = "text/javascript";
//                                                    s.src = "Seeds/assets/select2/dist/js/select2.full.min.js";
//                                                    var i = 2;
//                                                    $("#addCF").click(function () {
//                                                        $("#example1").append('<tr>"+ i +"<td><div class="form-group"><div class="col-md-12"><select id="materialaCode" name="materialaCode" class="form-control select2"><s:iterator value="TblPzScheduleMaterialmaster"><option value="<s:property value="companyId "/>"><s:property value="materialaCode"/></option></s:iterator></select></div></div></td><td><div class="form-group"><div class="col-md-12"><select id="matBatch" name="matBatch"  class="form-control select2"><s:iterator value="listPzTblPzScheduleMaterialmaster"><option value="<s:property value="materialaCode"/>"><s:property value="materialaCode"/></option></s:iterator></select></div></div></td><td><div class="form-group"><div class="col-md-12"><input id="pur[]"  name="pur" type="text" class="form-control"></div></div></td><td><div class="form-group"><div class="col-md-12"><input id="ven[]"  name="ven" type="text" class="form-control"></div></div></td> <td><div class="form-group"><div class="col-md-12"><input id="qty[]"  name="qty" type="text" class="form-control"></div></div></td><td><div class="form-group"><div class="col-md-12"><input id="matBatch"  name="matBath" type="text" onblur="CheckQty();" class="form-control"></div></div><a href="javascript:void(0);" class="remCF"><span class="btn btn-danger">Remove</span></a></td>  </tr>');
//                                                        $.getScript("Seeds/assets/select2/dist/js/select2.full.min.js");
//                                                        i++;
//                                                        $(".remCF").on('click', function () {
//                                                            $(this).parent().parent().remove();
//                                                        });
//                                                    });


        </script>-->
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
                var last_child = $('#example1 tr:last select');
                last_child.select2("destroy");
                
                //$('#example1 tr:last select').select2("destroy");   //    
                
                var $str = $('#example1 tr:last').html();
                $('#example1 tr:last').after("<tr>" + $str + "</tr>");

                var tr = $('#example1').find('tr').last();

                tr.find('select[name=md]').attr('id', 'md' + (row + 1));
               // tr.find('select[name=md]').val($("'select[name=md] option:first").val());
                $('#md' + (row + 1)).val("");

//                tr.find('select[name=umo]').attr('id', 'umo' + (row + 1));
//                //tr.find('select[name=umo]').val($("'select[name=umo] option:first").val());
//                $('#umo' + (row + 1)).val("");
//
//                tr.find('input[name=pur]').attr('id', 'pur' + (row + 1));
//                $('#pur' + (row + 1)).val("");
//
//                tr.find('input[name=ven]').attr('id', 'ven' + (row + 1));
//                $('#ven' + (row + 1)).val("");
//
//                tr.find('input[name=qty]').attr('id', 'qty' + (row + 1));
//                $('#qty' + (row + 1)).val("");
//
//                tr.find('input[name=stock]').attr('id', 'stock' + (row + 1));
//                
//                
//                
//                tr.find('input[name=des]').attr('id', 'des' + (row + 1));
//                $('#des' + (row + 1)).val("");
//                
//                tr.find('input[name=lot]').attr('id', 'lot' + (row + 1));
//                $('#lot' + (row + 1)).val("");
//                
//                tr.find('input[name=hidmat]').attr('id', 'hidmat' + (row + 1));
//                $('#hidmat' + (row + 1)).val("");
                
             
                
                
                last_child.select2();
                $('#md' + (row + 1)).select2();
              

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
                //this.onchangeevent();
            }

            $(document).on('change', 'select[name="md"]', function () {
                
                //$('#matBatch').empty();
                
                //$('#lotab').empty();
                var matId = 0;
                matId = parseInt($(this).val());
                //alert(matId);
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
                        tr.find('input[name="stock"]').attr('id', 'stock' + (row + 1));
                        $('#stock' + (row + 1)).val(m.qty);
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
                        tr.find('input[name="hidmat"]').attr('id', 'hidmat' + (row + 1));
                        $('#hidmat' + (row + 1)).val(m.qty5);
                        tr.find('input[name="stgLoc"]').attr('id', 'stgLoc' + (row + 1));
                        $('#stgLoc' + (row + 1)).val(m.qty6);
                        
                        
                        a= tr.find('input[name="stl"]').attr('id', 'stl' + (row + 1));                        
                        $('#stl' + (row + 1)).val(m.qty10);
                        
                        
                         tr.find('input[name="odv"]').attr('id', 'odv' + (row + 1));
                        $('#odv' + (row + 1)).val(m.qty11);
                        
                        tr.find('input[name="got"]').attr('id', 'got' + (row + 1));
                        $('#got' + (row + 1)).val(m.qty12);
                        
                        
                        tr.find('input[name="elisa"]').attr('id', 'elisa' + (row + 1));
                        $('#elisa' + (row + 1)).val(m.qty13);
                        
                        tr.find('input[name="sdcls"]').attr('id', 'sdcls' + (row + 1));
                        $('#sdcls' + (row + 1)).val(m.qty14);
                        
                        tr.find('input[name="skipd"]').attr('id', 'skipd' + (row + 1));
                        $('#skipd' + (row + 1)).val(m.qty15);
                        
                        
                        tr.find('input[name="iNSPDT"]').attr('id', 'iNSPDT' + (row + 1));
                        $('#iNSPDT' + (row + 1)).val(m.qty16);
                        
                        tr.find('input[name="mOISTURE"]').attr('id', 'mOISTURE' + (row + 1));
                        $('#mOISTURE' + (row + 1)).val(m.qty17);
                        
                        tr.find('input[name="PURESEED"]').attr('id', 'PURESEED' + (row + 1));
                        $('#PURESEED' + (row + 1)).val(m.qty18);
                        
                        tr.find('input[name="INERTMATTER"]').attr('id', 'INERTMATTER' + (row + 1));
                        $('#INERTMATTER' + (row + 1)).val(m.qty19);
                        
                        tr.find('input[name="OCSCOUNT"]').attr('id', 'OCSCOUNT' + (row + 1));
                        $('#OCSCOUNT' + (row + 1)).val(m.qty20);
                        
                        tr.find('input[name="WEEDSEEDCOUNT"]').attr('id', 'WEEDSEEDCOUNT' + (row + 1));
                        $('#WEEDSEEDCOUNT' + (row + 1)).val(m.qty21);
                        
                        tr.find('input[name="gRAIN"]').attr('id', 'gRAIN' + (row + 1));
                        $('#gRAIN' + (row + 1)).val(m.qty22);
                        
                        tr.find('input[name="BLACKSEEDS"]').attr('id', 'BLACKSEEDS' + (row + 1));
                        $('#BLACKSEEDS' + (row + 1)).val(m.qty23);
                        
                        tr.find('input[name="PINHOLESEEDS"]').attr('id', 'PINHOLESEEDS' + (row + 1));
                        $('#PINHOLESEEDS' + (row + 1)).val(m.qty24);
                        
                        tr.find('input[name="ODVRES"]').attr('id', 'ODVRES' + (row + 1));
                        $('#ODVRES' + (row + 1)).val(m.qty25);
                        
                        tr.find('input[name="BULKDENSITY"]').attr('id', 'BULKDENSITY' + (row + 1));
                        $('#BULKDENSITY' + (row + 1)).val(m.qty26);
                        
                        tr.find('input[name="tHSW"]').attr('id', 'tHSW' + (row + 1));
                        $('#tHSW' + (row + 1)).val(m.qty27);
                        
                        tr.find('input[name="COLDVIGOURGERMNORMAL"]').attr('id', 'COLDVIGOURGERMNORMAL' + (row + 1));
                        $('#COLDVIGOURGERMNORMAL' + (row + 1)).val(m.qty28);
                        
                        tr.find('input[name="FIRSTCOUNTNORMAL"]').attr('id', 'FIRSTCOUNTNORMAL' + (row + 1));
                        $('#FIRSTCOUNTNORMAL' + (row + 1)).val(m.qty29);
                        
                        tr.find('input[name="GERMNORMAL"]').attr('id', 'GERMNORMAL' + (row + 1));
                        $('#GERMNORMAL' + (row + 1)).val(m.qty30);
                        
                        tr.find('input[name="FETNORMAL"]').attr('id', 'FETNORMAL' + (row + 1));
                        $('#FETNORMAL' + (row + 1)).val(m.qty31);
                        
                        tr.find('input[name="SOILCOUNTDAYS"]').attr('id', 'SOILCOUNTDAYS' + (row + 1));
                        $('#SOILCOUNTDAYS' + (row + 1)).val(m.qty32);
                        
                        tr.find('input[name="AAVGERMNORMAL"]').attr('id', 'AAVGERMNORMAL' + (row + 1));
                        $('#AAVGERMNORMAL' + (row + 1)).val(m.qty33);
                    
                        
                        
                        
                        
                        
                        tr.find('input[name="GOTGP"]').attr('id', 'GOTGP' + (row + 1));
                        $('#GOTGP' + (row + 1)).val(m.qty34);
                        
                        tr.find('input[name="GOTFEMALE"]').attr('id', 'GOTFEMALE' + (row + 1));
                        $('#GOTFEMALE' + (row + 1)).val(m.qty35);
                        
                        tr.find('input[name="GOTOTHERS"]').attr('id', 'GOTOTHERS' + (row + 1));
                        $('#GOTOTHERS' + (row + 1)).val(m.qty36);
                        
                        tr.find('input[name="bG1"]').attr('id', 'bG1' + (row + 1));
                        $('#bG1' + (row + 1)).val(m.qty37);
                        
                        tr.find('input[name="bG2"]').attr('id', 'bG2' + (row + 1));
                        $('#bG2' + (row + 1)).val(m.qty38);
                        
                        tr.find('input[name="hT"]').attr('id', 'hT' + (row + 1));
                        $('#hT' + (row + 1)).val(m.qty39);
                        
                        tr.find('input[name="fQR"]').attr('id', 'fQR' + (row + 1));
                        $('#fQR' + (row + 1)).val(m.qty40);
                        


                    }
                
            
                });
            });


        </script>
        
<!--        <script type="text/javascript">$( '#myoutputmata').select2(); </script>-->
        
        <script>
            $(function () {
                //Initialize Select2 Elements
                $('.select2').select2();
                
                
                

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
                    format: 'yyyy-mm-dd',
                    startDate: new Date()
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
     
        
        
        
        
<!--        <script>
function myFunction() {
  var x = document.getElementById("expoutPut");
  x.value = x.value.toUpperCase();
  
  if(x.value == ''){
      alert("NULL VALUE");
      x.value = "Error Occured";
      x.style.backgroundColor="red";
  }else if(x.value != ''){
      x.style.backgroundColor="";
      
  }
}
</script>-->
        
        
        
        
        
        <script>
function validateForm() {
  let x = document.forms["myForm"]["fromDate"].value;
  let y = document.forms["myForm"]["expoutPut"].value;
  let z = document.forms["myForm"]["batchNumberIndent"].value;
  let z1 = document.forms["myForm"]["comments"].value;
  
  
  
var e = document.getElementById("compCode");
var strUser = e.options[e.selectedIndex].value;
var f = document.getElementById("divisionId");
var strUser1 = f.options[f.selectedIndex].value;
var g = document.getElementById("deptId");
var strUser2 = g.options[g.selectedIndex].value;
var h = document.getElementById("myoutputmata");
var strUser3 = h.options[h.selectedIndex].value;
var i = document.getElementById("plantId");
var strUser4 = i.options[i.selectedIndex].value;
var j = document.getElementById("crospId");
var strUser5 = j.options[j.selectedIndex].value;
var k = document.getElementById("packandprocessing");
var strUser6 = k.options[k.selectedIndex].value;
var l = document.getElementById("lincodabc");
var strUser7 = l.options[l.selectedIndex].value;
var m = document.getElementById("orderNoid");
var strUser8 = m.options[m.selectedIndex].value;


var n = document.getElementById("des[]").value;
//var strUser9 = n.options[n.selectedIndex].value;
//alert ("name is"+n);


if(strUser==0)
{
alert("Please select a Company");
return false;

}else if(strUser1==0){
alert("Please select a Crop type");
return false;

}
else if(strUser2==0){
alert("Please select a Department");
return false;

}

else if(strUser4==0){
alert("Please select a Plant");
return false;

}
else if(strUser5==0){
alert("Please select a Crop");
return false;

}
else if(strUser6==0){
alert("Please select a packing or Processing");
return false;

}else if(strUser3==0){
alert("Please select a Output Material");
return false;

}


else if (x == "") {
    alert("Start Date must be filled out.");
    return false;
  }
  else if (y == 0) {
    alert("Expected Output Quantity must be filled out.");
    return false;
  }else if (z == "") {
    alert("Batch Number must be filled out.");
    return false;
  }else if (z1 == "") {
    alert("Comments must be filled out.");
    return false;
  }


else if(strUser7==0){
alert("Please select a Line Code");
return false;

}
else if(strUser8==0){
alert("Please select a Order Type");
return false;

}
else if(n == ""){
alert("Please select a Input Material");
return false;

}
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

    </body>
</html>
