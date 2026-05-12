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
        

<div class="loader"></div>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="../header/header.jsp" %>
        <!-- Content Wrapper. Contains page content -->      

    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                Output material List
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Masters</a></li>
                <li class="active">Output material List </li>
            </ol>
        </section>
        <!-- Main content -->
        <section class="content">
            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <div class="box-header">
                            <h3 class="box-title">Output material List </h3>
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
                            
                            function getSubmit2(val)
                            {
                                document.getElementById("indent_Id2").value = val;
                                var frm = document.getElementById("deleteInd");
                                frm.submit();
                            }

                        </script>
                        <s:form action="indentRequestPzList1.action" id="indStatus" method="post"> 
                            <s:hidden id="indent_Id" name="indId" />
                        </s:form>
                        <s:form action="getSelfIndentDetailsforDelete.action" id="approveInd" method="post"> 
                            <s:hidden id="indent_Id1" name="indId" />
                        </s:form>
                        <s:form action="deletePlantIndent.action" id="deleteInd" method="post"> 
                            <s:hidden id="indent_Id2" name="indId" />
                        </s:form>

                        <div class="box-body">
                            <div class="box-footer">
                                <label class="col-sm-2 headline text-yellow" style="font-size:20px ">Filter </label>
<!--                                <div class="col-xs-2">
                                    <s:select onchange="getSubmit();" name="statusId" id="statusId" list="listTblIndentStatus" headerKey="0" headerValue="--- Select ---" listKey="indentStatusId" listValue="indentStatusName" cssClass="form-control select2"/>
                                </div>-->
<!--                                    <a href="<s:url action="userPzindentCreatedReport.action"/>"> <span class="box-title btn btn-success pull-right">Export</span></a> &emsp;-->
                                <a href="<s:url action="addoutputMaterial.action"/>"> <span class="box-title btn btn-success pull-right">Add OutPut Material</span></a>
                            </div>
                            <table id="example1" class="table table-bordered table-striped" data-toggle="table" data-url="/examples/bootstrap_table/data" data-height="400" data-side-pagination="server" data-pagination="true" data-page-list="[5, 10, 20, 50, 100, 200]" data-search="true">
                                <thead>
                                    <tr>
                                        <th data-checkbox="true">Id</th>
                                        <th>Material Code.</th>
                                        <th>Material Desc</th>
                                        <th>material UOM</th>
                                        <th>Material Variety</th>
                                        <th>Material Plant</th>
                                        
<!--                                        <th>Action</th>-->
                                    </tr>
                                </thead>
                                <tbody>
                                    <s:iterator value="listPzTblMaterialMaster">
                                        <tr>
                                            <td><s:property value="materialId" /></td>
                                            <td><s:property value="materialCode" /></td>
                                            <td><s:property value="materialDesc"/></td>
                                            <td><s:property value="materialUom"/></td>
                                            <td><s:property value="materialVariety"/></td>
                                            <td><s:property value="materialPlant"/></td>                                       
                                            

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
