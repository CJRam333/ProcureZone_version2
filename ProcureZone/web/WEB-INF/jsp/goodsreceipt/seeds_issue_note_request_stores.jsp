<%-- 
    Document   : seeds_issue_note_request
    Created on : Feb 19, 2020, 10:17:01 AM
    Author     : ramesh.avv
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib uri="/WEB-INF/displaytag.tld" prefix="display" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="../header/header.jsp" %>
    </head>
    <body>
        <div class="content-wrapper">
            <!-- Content Header (Page header) -->
            <section class="content-header">
                <h1>
                    Goods Receipt
                    <small></small>
                </h1>
                <ol class="breadcrumb">
                    <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                    <li><a href="#">Issue Note</a></li>
                    <li class="active">Goods Receipt</li>
                </ol>
            </section>
            <section class="content">
                <div class="row">
                    <!-- left column -->
                    <div class="col-md-12">
                        <!-- general form elements -->
                        <div class="box box-primary">
                            <div class="box-header with-border">
                                <h3 class="box-title">Goods Receipt</h3>
                            </div>
                            <script>
                            function getBalInv()
                            {
                                var theForm = document.getElementById("myForm");
                               
                               for(var i = 0; i < theForm.openqty.length; i++){
                                   var bal=parseFloat(theForm.openqty[i].value)+parseFloat(theForm.goodsreceiptqty[i].value);
                                   theForm.balinventory[i].value=bal;
                               }
                            }
                            </script>
                            <!-- /.box-header -->
                            <!-- form start -->
                            <form id="myForm" action="saveGoodsReceiptRequest.action" method="post" class="form-horizontal">
                                <div class="box-body">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <div class="form-group">
                                                <label class="col-sm-3 control-label">PO Number</label>
                                                <div class="col-xs-6">                                                                                                       
                                                    <s:select disabled="true" name="indentId" id="indentId" list="listTblIndentMaster" onchange="this.form.action='getIndentDetails.action'; this.form.submit();" headerKey="0" headerValue="--- Select ---" listKey="indentId" listValue="indentPoNumber" cssClass="form-control select2"/>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-sm-3 control-label">Emp Name</label>
                                                <div class="col-xs-6">
                                                    <s:textfield name="empName" readonly="true" cssClass="form-control"/>                                                
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="form-group">
                                                <label class="col-sm-3 control-label">Emp Id</label>
                                                <div class="col-xs-6">                                                                                                       
                                                    <s:textfield name="empId" readonly="true" cssClass="form-control"/>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-sm-3 control-label">Company</label>
                                                <div class="col-xs-6">                                                                                                       
                                                    <s:textfield name="compName" readonly="true" cssClass="form-control"/>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <table id="example1" class="table table-bordered table-striped">
                                            <thead>
                                                <tr>
                                                    <th>Material Description</th>        
                                                    <th>Opening Quantity</th>
                                                    <th>Goods Receipt Quantity</th>
                                                    <!--<th>Goods Issued Quantity</th>-->
                                                    <th>Balance Inventory</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <s:set name="sno" value="0"/>
                                                <s:iterator value="listTblIndentDetails">
                                                    <tr>                                                    
                                                        <td>                                                        
                                                            <div class="form-group">
                                                                <div class="col-md-12">                                                                   
                                                                    <input name="md"  type="hidden" value="<s:property value="%{tblMaterialMaster.materialId}"/>" class="form-control">
                                                                    <input name="md1"  readonly type="text" value="<s:property value="%{tblMaterialMaster.materialDesc}"/> - <s:property value="%{tblMaterialMaster.materialName}"/>" class="form-control">                                                                
                                                                </div>
                                                            </div>
                                                        </td>                                                        
                                                        <td>
                                                            <div class="form-group">
                                                                <div class="col-md-12">
                                                                    <input name="openqty" readonly  value="<s:property value="indentDetailsDeptQty"/>" type="text" class="form-control">
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <div class="form-group">
                                                                <div class="col-md-12">
                                                                    <input onblur="getBalInv();" name="goodsreceiptqty" type="text" class="form-control">
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <%--<td>
                                                            <div class="form-group">
                                                                <div class="col-md-12">
                                                                    <input name="goodsissuedqty" type="text" class="form-control">
                                                                </div>
                                                            </div>
                                                        </td>--%>
                                                        <td>
                                                            <div class="form-group">
                                                                <div class="col-md-12">
                                                                    <input name="balinventory" type="text" class="form-control">
                                                                </div>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                    <s:set name="sno" value="%{#sno+1}"/>
                                                </s:iterator>
                                            </tbody>
                                        </table>
                                    </div>                                    
                                    <div class="row">
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Remarks</label> 
                                            <div class="col-xs-9">
                                                <s:textarea name="storesremarks"  cssClass="form-control"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="submit" class="btn btn-primary">Submit</button>
                                        <a href="<s:url action="goodsReceiptRequestList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>
                                    </div>
                                </div>                                            
                            </form>
                        </div>                                
                    </div>
                </div>
            </section>
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
            $(function () {
                //Initialize Select2 Elements
                $('.select2').select2()

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
    </body>
</html>
