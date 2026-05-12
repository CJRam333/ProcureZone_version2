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
        <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                Indent Request 
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Indent</a></li>
                <li class="active">Indent Request</li>
            </ol>
        </section>

        <section class="content">
            <div class="row">
                <!-- left column -->
                <div class="col-md-12">
                    <!-- general form elements -->
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">Indent Request </h3>
                        </div>
                        <!-- /.box-header -->
                        <!-- form start -->
                        <form action="saveIndentRequest.action" method="post" class="form-horizontal">
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
                                                <s:textfield name="indNo" readonly="true" cssClass="form-control"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="col-sm-3 control-label">Company</label>
                                            <div class="col-xs-6">                                                                                                       
                                                <s:select name="compId" id="compId" list="listTblCompanyMaster" onchange="this.form.action='getIndentNo.action'; this.form.submit();" headerKey="0" headerValue="--- Select ---" listKey="compId" listValue="compName" cssClass="form-control select2"/>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="col-sm-3 control-label">Section</label>
                                            <div class="col-xs-6">                                                                                                       
                                                <s:select name="secId" list="listTblSectionMaster" headerKey="0" headerValue="--- Select ---" listKey="secId" listValue="secName" cssClass="form-control select2"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="col-sm-3 control-label">Department</label> 
                                            <div class="col-xs-6">
                                                <s:select name="deptId" list="listTblDepartmentMaster" headerKey="0" headerValue="--- Select ---" listKey="deptId" listValue="deptName" cssClass="form-control select2"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-md-3 control-label">Plant</label>
                                            <div class="col-xs-6">
                                                <s:select name="plantId" list="listTblPlantMaster" headerKey="0" headerValue="--- Select ---" listKey="plantId" listValue="plantName" cssClass="form-control select2"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6"> 
                                        <div class="form-group">
                                            <label class="col-md-12 control-label">Please arrange the following items as per the Specification/Details</label> 
                                            <div class="col-xs-6">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row"  style="overflow:scroll;">
                                    <table id="example1" class="table table-bordered table-striped">
                                        <thead>
                                            <tr>
                                                <th>Material Description</th>
                                                <th>UOM</th>
                                                <th>Project</th>
                                                <th>Vendor</th>
                                                <th>Quantity Requested</th>
                                                <th>Available Stock</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">                                                      
                                                            <select id="md[]" name="md"  class="form-control select2">
                                                                <%--<%
                                                                    try {                                                               
                                                                        List<TblMaterialMaster> listTblMaterialMaster = DaoFactory.getDao(MaterialDaoImpl.class).getMaterial();
                                                                        for (TblMaterialMaster materialMaster : listTblMaterialMaster) {
                                                                            int materialId = materialMaster.getMaterialId();
                                                                            String materialName = materialMaster.getMaterialName();
                                                                %>
                                                                <option value="<%=materialId%>"><%=materialName%></option>
                                                                <%
                                                                        }
                                                                    } catch (Exception e) {
                                                                    }
                                                                %>--%>
                                                                <s:iterator value="listTblMaterialMaster">
                                                                    <option value="<s:property value="materialId "/>"><s:property value="materialDesc"/> - <s:property value="materialName"/></option>
                                                                </s:iterator>
                                                            </select>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <select id="umo[]" name="umo"  class="form-control select2" style="width: 100px;">
                                                                <%--<%
                                                                    try {
                                                                        List<TblUmoMaster> listTblUmoMaster = DaoFactory.getDao(UmoDaoImpl.class).getUmo();
                                                                        for (TblUmoMaster umoMaster : listTblUmoMaster) {
                                                                            int umoId = umoMaster.getUmoId();
                                                                            String umoName = umoMaster.getUmoCode();
                                                                %>
                                                                <option value="<%=umoId%>"><%=umoName%></option>
                                                                <%
                                                                        }
                                                                    } catch (Exception e) {
                                                                    }
                                                                %>--%>
                                                                <s:iterator value="listTblUmoMaster">
                                                                    <option value="<s:property value="umoId"/>"><s:property value="umoCode"/></option>
                                                                </s:iterator>
                                                            </select>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input id="pur[]" name="pur" type="text" class="form-control">
                                                        </div>
                                                    </div>  
                                                </td>
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input id="ven[]" name="ven" type="text" class="form-control">
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input id="qty[]" name="qty" type="text" class="form-control">
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="form-group">
                                                        <div class="col-md-12">
                                                            <input readonly id="stock[]" name="stock"  type="text" class="form-control">
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <div class="col-xs-2">
                                                <!--<a href="javascript:void(0);" id="addCF"><span class="btn btn-primary">Add</span></a>-->
                                                <input type="button" value="Add" class="btn btn-primary" onclick="getAddRow();">
                                            </div>
                                            <!-- /.col -->
                                            <div class="col-xs-2">                                                        
                                                <input type="button" value="Remove" class="btn btn-danger" onclick="getRemoveRow();"> 
                                            </div>
                                        </div>
                                        <!-- /.col -->
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
                                <div class="box-footer">
                                    <button type="submit" class="btn btn-primary">Submit</button>
                                    <a href="<s:url action="indentRequestList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>
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
                for (var i = 0; i < q.length; i++) {
                    if (parseFloat(s[i].value, 10) >= parseFloat(q[i].value, 10)) {
                        alert("Avaliable stock should be less than Quantity Requested");
                        s[i].value = "";
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

            $(document).on('change', 'select[name="md"]', function () {
                var matId = 0;
                matId = parseInt($(this).val());
                var comp = $('#compId option:selected').val();
                var plant = $('#plantId').val();

                $.ajax({
                    type: "POST",
                    url: "getAvailableStock.action",
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
