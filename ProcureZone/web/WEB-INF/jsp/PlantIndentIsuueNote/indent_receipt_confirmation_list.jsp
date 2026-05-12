<%-- 
    Document   : seeds_indent_request_list
    Created on : Oct 3, 2019, 12:17:54 PM
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
        <title>Plant Inventory</title>
         <link href="images/Nsllogo.png" rel="icon">
        <!-- Content Wrapper. Contains page content -->      

    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                Indent Receipt Confirmation
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Masters</a></li>
                <li class="active">Indent Receipt Confirmation List </li>
            </ol>
        </section>
        <!-- Main content -->
        <section class="content">
            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <div class="box-header">
                            <h3 class="box-title">Indent Receipt Confirmation </h3>
                        </div>

                        <!-- /.box-header -->
                        <script>
                            function getSubmit(val)
                            {
                                document.getElementById("indent_Id").value = document.getElementById("statusId").value;
                                var frm = document.getElementById("indStatus");
                                frm.submit();
                            }
                            function getSubmit1(val)
                            {
                                document.getElementById("indent_Id1").value = val;
                                var frm = document.getElementById("approveInd");
                                frm.submit();
                            }

                        </script>
                        <s:form action="indentRequestList1.action" id="indStatus" method="post"> 
                            <s:hidden id="indent_Id" name="indId" />
                        </s:form>
                        <s:form action="getIssueReceiptConfirmation.action" id="approveInd" method="post"> 
                            <s:hidden id="indent_Id1" name="indId" />
                        </s:form>

                        <div class="box-body">
                            <div class="box-footer">
                                <label class="col-sm-2 headline text-yellow" style="font-size:20px ">Filter Status</label>
<!--                                <div class="col-xs-2">
                                    <s:select onchange="getSubmit();" name="statusId" id="statusId" list="listTblIndentStatus" headerKey="0" headerValue="--- Select ---" listKey="indentStatusId" listValue="indentStatusName" cssClass="form-control select2"/>
                                </div>-->
                                    <a href="<s:url action="userPzindentCreatedReport.action"/>"> <span class="box-title btn btn-success pull-right">Export</span></a> &emsp;
                                <!--<a href="<s:url action="addFloorInchargeforplant.action"/>"> <span class="box-title btn btn-success pull-right">Check Indents</span></a>-->
                            </div>
                            <table id="example1" class="table table-bordered table-striped">
                                <thead>
                                    <tr>
                                        <th>Indent No.</th>
                                        <th>Year</th>
                                        <th>Employee Name</th>
                                        <th>Company Id</th>
					<th>Plant Id</th>
                                        <th>Date & Time</th>
                                        <th>Modified User Email</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <s:iterator value="listTblpzIndentMaster">
                                        <tr>
                                            <td><s:property value="IndentFinalNumber"/></td>
                                            <td><s:property value="indentYear"/></td>
                                            <td><s:property value="tblEmpMasterByIndentLmu.empName"/></td>
                                            <td><s:property value="tblCompanyMaster.compId"/></td>
				            <td><s:property value="tblPlantMaster.plantId"/></td>  
                                            <td><s:property value="indentDate"/></td>
                                            <td><s:property value="tblEmpMasterByIndentLmu.empEmail"/></td>                                       
                                            <td><s:if test="%{tblIndentStatusByIndentApprovedStatus.indentStatusId==2 && tblIndentStatusByIndentFinalStatus.indentStatusId==1 && tblIndentStatusByIndentProcurementStatus.indentStatusId==10}">Inventory Issue Pending</s:if>
                                                <s:if test="%{tblIndentStatusByIndentApprovedStatus.indentStatusId==3 && tblIndentStatusByIndentFinalStatus.indentStatusId==1 && tblIndentStatusByIndentProcurementStatus.indentStatusId==10}">Inventory issue Confirmation   </s:if>
                                                <s:if test="%{tblIndentStatusByIndentApprovedStatus.indentStatusId==1 && tblIndentStatusByIndentFinalStatus.indentStatusId==1&& tblIndentStatusByIndentProcurementStatus.indentStatusId==10}">Deo Order Pending</s:if>
                                                <s:if test="%{tblIndentStatusByIndentApprovedStatus.indentStatusId==4 && tblIndentStatusByIndentFinalStatus.indentStatusId==1&& tblIndentStatusByIndentProcurementStatus.indentStatusId==10}">Inventory Receipt Pending</s:if>
                                                <s:if test="%{tblIndentStatusByIndentApprovedStatus.indentStatusId==5 && tblIndentStatusByIndentFinalStatus.indentStatusId==1 && tblIndentStatusByIndentProcurementStatus.indentStatusId==10}">GRN Receipt pending</s:if>
                                                <s:if test="%{tblIndentStatusByIndentApprovedStatus.indentStatusId==6 && tblIndentStatusByIndentFinalStatus.indentStatusId==1 && tblIndentStatusByIndentProcurementStatus.indentStatusId==10}"><s:a href="javascript:void(0)" onclick="getSubmit1(%{indentId})" >Receipt Confirmation pending</s:a></s:if>                                                   
                                                <s:if test="%{tblIndentStatusByIndentApprovedStatus.indentStatusId==7 && tblIndentStatusByIndentFinalStatus.indentStatusId==1 && tblIndentStatusByIndentProcurementStatus.indentStatusId==10}">Completed</s:if>
                                                
                                                    </td>
                                                </tr>
                                    </s:iterator>
                                </tbody>
                            </table>  
                        </div>
                    </div>
                    <!-- /.box -->
                </div>
                <!-- /.col -->
            </div>
            <!-- /.row -->
        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->
    <footer class="main-footer">
        <div class="pull-right hidden-xs">
            <b>Version</b> 1.0.0
        </div>
        <strong>Copyright &copy; 2019 <a href="http://www.nuziveeduseeds.com/">Nuziveedu Seeds</a>.</strong> All rights reserved.
    </footer>

    <!-- jQuery 3 -->
    <script src="Seeds/assets/jquery/dist/jquery.min.js"></script>    
    <script>
                            $.widget.bridge('uibutton', $.ui.button);
    </script>
    <!-- Bootstrap 3.3.7 -->
    <script src="Seeds/assets/bootstrap/dist/js/bootstrap.min.js"></script>    
    <!-- Slimscroll -->
    <script src="Seeds/assets/jquery-slimscroll/jquery.slimscroll.min.js"></script>
    <!-- FastClick -->
    <script src="Seeds/assets/fastclick/lib/fastclick.js"></script>
    <!-- AdminLTE App -->
    <script src="Seeds/dist/js/seeds.min.js"></script>

    <!-- AdminLTE for demo purposes -->
    <script src="Seeds/dist/js/demo.js"></script>    <!-- iCheck 1.0.1 -->
    <script src="Seeds/plugins/iCheck/icheck.min.js"></script>
    <!-- DataTables -->
    <script src="Seeds/assets/datatables.net/js/jquery.dataTables.min.js"></script>
    <script src="Seeds/assets/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>    
    <script>
                            $(function () {
                                $('#example1').DataTable()
                                $('#example2').DataTable({
                                    'background': red,
                                    'paging': true,
                                    'lengthChange': false,
                                    'searching': false,
                                    'ordering': true,
                                    'info': true,
                                    'autoWidth': false
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
