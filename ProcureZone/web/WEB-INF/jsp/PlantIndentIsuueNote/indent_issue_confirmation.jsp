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
                Indent for Inventory Manager 
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Plant Indent</a></li>
                <li class="active">Indent for Inventory Manager</li>
            </ol>
        </section>

        <section class="content">
            <div class="row">
                <!-- left column -->
                <div class="col-md-12">
                    <!-- general form elements -->
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">Indent for Inventory Manager</h3>
                        </div>
                        <!-- /.box-header -->
                        <!-- form start -->
                        <form action="savePzIssueConfirmation.action" method="post" name="myForm" onsubmit="return validateForm()" class="form-horizontal">
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
                                            
<!--                                            <div class="form-group">
                                         
                                                    
                                                <label class="col-md-3 control-label">Actual Output Qty</label>
                                                <div class="col-xs-6">
                                                    <span class="field">
                                                        <s:textfield readonly="true" name="atualOutputQty"  onchange="chackactualQty();" cssClass="form-control" />                        
                                                    </span>	

                                                </div> 
                                          
                                                
                                            </div>-->
                                                    
<!--                                                    <div class="form-group">
                                         
                                                    
                                                <label class="col-md-3 control-label">GRN Receipt Number</label>
                                                <div class="col-xs-6">
                                                    <span class="field">
                                                        <s:textfield name="grnreceiptnumber"  readonly="true" cssClass="form-control" />                        
                                                    </span>	

                                                </div> 
                                          
                                                
                                            </div>-->
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
                                                    
                                                    <div class="form-group">
                                                
                                                    
                                                <label class="col-md-3 control-label">Order Number</label>
                                                <div class="col-xs-6">
                                                    <span class="field">
                                                        <s:textfield name="indentOrderNumbera" readonly ="true" cssClass="form-control pull-right"  id="datepicker"/>                        
                                                    </span>	

                                                </div> 
                                               
                                            </div>
                                    
                                </div>
                                </div>
                                <div class="row">
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
                                                        <th>Issued Stock</th>                                                        
                                                        
                                                        <th>STL</th>
                                                        <th>ODV</th>
                                                        <th>GOT</th>
                                                        <th>ELISA</th>
                                                        <th>SDCLS</th>
                                                        <th>Confirmed</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <s:set name="sno" value="0"/>
                                                <s:iterator value="listTblPzIndentDetails">
                                                    <tr>
                                                        
                                                        <td><s:property value="indentMatel"/></td>
                                                        <td><s:property value="indentMateDesc"/></td>
                                                        <td><s:property value="indentLoteNum"/></td>
                                                        <td><s:property value="indentStoLoc"/></td>
                                                        <td><s:property value="indentDetailsPurpose"/></td>
                                                        <td><s:property value="indentDetailsVendor"/></td>
                                                        <td><input  id="recstock[]" value="<s:property value="indentDetailsStockAval"/>" name="stock" type="text" readonly="readonly" class="form-control" style="width: 90px;"></td>
                                                        <td><input  id="recqty[]" value="<s:property value="indentDetailsQty"/>"  onchange="CheckQty();" name="qty" readonly="readonly" type="text" class="form-control" style="width: 90px;"></td>                                                  
                                                        
                                                        
                                                          <td><s:property value="matStl"/></td>
                                                        <td><s:property value="matOdv"/></td>
                                                        <td><s:property value="matGot"/></td>
                                                        <td><s:property value="matElisa"/></td>
                                                        <td><s:property value="sDCLS"/></td>  
                                                        <td><input type="checkbox" name="checkMe" value="true" checked="checked" id="issueconfirmed" /></td>
                                                         
                                                         
                                                        
                                                        
                                                        <%--<td>
                                                            <div class="form-group">
                                                                <div class="col-md-12">
                                                                    <input  id="recqty[]" name="recqty" type="text" class="form-control">
                                                                </div>
                                                            </div>
                                                        </td>--%>
                                                       
                                                                                                          
                                                    </tr>
                                                    <s:set name="sno" value="%{#sno+1}"/>
                                                </s:iterator>
                                            </tbody>
                                        </table>
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
                                        
                                        <label class="col-sm-2 control-label">Comments/Remarks</label> 
                                        <div class="col-xs-9">
                                            <s:textarea readonly="true" name="comments"  cssClass="form-control"/>
                                        </div>
                                        
                                    </div>
                                    
                                    <div class="form-group">
                                        
                                        
                                        <label class="col-sm-2 control-label">Inventory Manager Comments/Remarks</label> 
                                        <div class="col-xs-9">
                                            <s:textarea name="indentFmComments"  cssClass="form-control"/>
                                        </div>
                                        
                                        
                                    </div>
                                </div>                                   
                                <div class="box-footer" style="padding-left:20px">
                                    <button id= "sbtbtn" type="submit" class="btn btn-primary" >Submit</button>
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
  let x = document.forms["myForm"]["indentFmComments"].value;
  
  if (x == "") {
    alert("Comments must be filled out.");
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

</body>
</html>
