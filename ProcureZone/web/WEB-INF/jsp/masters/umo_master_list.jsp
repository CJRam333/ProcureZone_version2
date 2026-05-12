<%-- 
    Document   : company_master_list
    Created on : Sep 30, 2019, 9:19:18 AM
    Author     : ramesh.avv
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib uri="/WEB-INF/displaytag.tld" prefix="display" %>
<!DOCTYPE html>
<html>   
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="../header/header.jsp" %>
    <!-- Content Wrapper. Contains page content -->      

    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                UOM Master
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Masters</a></li>
                <li class="active">UOM List</li>
            </ol>
        </section>
        <!-- Main content -->
        <section class="content">
            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <div class="box-header">
                            <h3 class="box-title">UOM Master List</h3>
                            <s:if test="#session.add=='true'">
                                <a href="<s:url action="addUmo.action"/>"> <span class="box-title btn btn-success pull-right">Add UOM</span></a>
                            </s:if>
                        </div>

                        <!-- /.box-header -->
                        <script>
                            function getSubmit(val)
                            {
                                document.getElementById("umo_Id").value = val;
                                var frm = document.getElementById("umoedit");
                                frm.submit();
                            }
                            function getSubmit1(val)
                            {
                                document.getElementById("umo_Id1").value = val;
                                var frm = document.getElementById("umodel");
                                frm.submit();
                            }
                            function getSubmit2(val)
                            {
                                document.getElementById("umo_Id2").value = val;
                                var frm = document.getElementById("umoview");
                                frm.submit();
                            }
                        </script>
                        <s:form action="updateUmo.action" id="umoedit" method="post" > 
                            <s:hidden id="umo_Id" name="umoId" />
                        </s:form>  
                        <s:form action="deleteUmo.action" id="umodel" method="post"> 
                            <s:hidden id="umo_Id1" name="umoId" />
                        </s:form>
                        <s:form action="viewUmo.action" id="umoview" method="post"> 
                            <s:hidden id="umo_Id2" name="umoId" />
                        </s:form>
                        <div class="box-body">
                            <table id="example1" class="table table-bordered table-striped">
                                <thead>
                                    <tr>
                                        <th>UOM Code</th>
                                        <th>UOM Name</th>
                                        <th>Modified Date</th>
                                        <th>Modified User</th>
                                        <th>Status</th>
                                            <s:if test="#session.view=='true'">
                                            <th>&nbsp;</th>
                                            </s:if>
                                            <s:if test="#session.edit=='true'">
                                            <th>&nbsp;</th>
                                            </s:if>
                                            <s:if test="#session.delete=='true'">
                                            <th>&nbsp;</th>
                                            </s:if>
                                    </tr>
                                </thead>
                                <tbody>
                                    <s:iterator value="listTblUmoMaster">
                                        <tr>
                                            <td><s:property value="umoCode"/></td>
                                            <td><s:property value="umoName"/></td>
                                            <td><s:date format="yyyy-MM-dd" name="umoLmd"/></td>                                            
                                            <td><s:property value="tblEmpMaster.empName"/></td>
                                            <td> 
                                                <s:if test="%{umoStatus==1}">                            
                                                    Active
                                                </s:if>
                                                <s:else>
                                                    Inactive
                                                </s:else>    
                                            </td>
                                            <s:if test="#session.view=='true'">
                                                <td><s:a href="javascript:void(0)" cssClass="btn btn-default" onclick="getSubmit2(%{umoId})"  title="View"> <span class="fa fa-eye"></span></s:a></td>
                                                </s:if>
                                                <s:if test="#session.edit=='true'">
                                                <td><s:a href="javascript:void(0)" cssClass="btn btn-default" onclick="getSubmit(%{umoId})" title="Edit"> <span class="fa fa-edit"></span></s:a></td>                                                  
                                                </s:if>
                                                <s:if test="#session.delete=='true'">
                                                <td><s:a href="javascript:void(0)" cssClass="btn btn-default" onclick="getSubmit1(%{umoId})" title="Delete"> <span class="fa fa-trash"></span></s:a></td>
                                                </s:if>
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
            <b>Version</b> 2.4.18
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
