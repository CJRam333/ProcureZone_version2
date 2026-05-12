<%-- 
    Document   : roles_master_list
    Created on : Oct 1, 2019, 1:38:26 PM
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
        <!-- Content Wrapper. Contains page content -->      

    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
               Employee Plant Map Master
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Masters</a></li>
                <li class="active">Plant Mapping List</li>
            </ol>
        </section>
        <!-- Main content -->
        <section class="content">
            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <div class="box-header">
                            <h3 class="box-title">Plant Mapping  Master List</h3>
                            <s:if test="#session.add=='true'">
                                <a href="<s:url action="empPlantRoleMap.action"/>"> <span class="box-title btn btn-success pull-right">Map Plant to Employee</span></a>
                            </s:if>
                        </div>

                        <!-- /.box-header -->
                        <script>
                            function getSubmit(val)
                            {
                                document.getElementById("roles_Id").value = val;
                                var frm = document.getElementById("rolesedit");
                                frm.submit();
                            }
                            function getSubmit1(val)
                            {
                                document.getElementById("roles_Id1").value = val;
                                var frm = document.getElementById("rolesdel");
                                frm.submit();
                            }
                            function getSubmit2(val)
                            {
                                document.getElementById("roles_Id2").value = val;
                                var frm = document.getElementById("rolesview");
                                frm.submit();
                            }
                        </script>
                        <s:form action="updateRole.action" id="rolesedit" method="post" > 
                            <s:hidden id="roles_Id" name="roleId" />
                        </s:form>  
                        <s:form action="deleteRole.action" id="rolesdel" method="post"> 
                            <s:hidden id="roles_Id1" name="roleId" />
                        </s:form>
                        <s:form action="viewRole.action" id="rolesview" method="post"> 
                            <s:hidden id="roles_Id2" name="roleId" />
                        </s:form>
                        <div class="box-body">
                            <table id="example1" class="table table-bordered table-striped">
                                <thead>
                                    <tr>
                                        <th>Id</th>
                                        <th>Company</th>
                                        <th>Plant Code</th>
                                        <th>Employee Name</th>
                                        <th>Employee Email</th>
                                        <th>Role Status</th>
                                        
                                       
                                        
                                            
                                    </tr>
                                </thead>
                                <tbody>
                                    <s:iterator value="listtbEmpPlantMaps">
                                        <tr>
                                            <td><s:property value="Id"/></td>
                                            <td><s:property value="tblCompanyMaster.compId"/></td>
                                            <td><s:property value="tblPlantMaster.plantId"/></td>
                                            <td><s:property value="tblEmpMaster.empName "/> - [ - <s:property value="tblEmpMaster.empId" /> - ]</td>
                                            <td><s:property value="tblEmpMaster.empEmail "/></td>
                                            <td><s:property value="roleStatus"/></td>
                                            
                                            
                                            
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
</div>
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
