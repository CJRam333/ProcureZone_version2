<%-- 
    Document   : employee_roles_master
    Created on : Oct 3, 2019, 11:23:39 AM
    Author     : ramesh.avv
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
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
                        Employee Roles Master
                        <small></small>
                    </h1>
                    <ol class="breadcrumb">
                        <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                        <li><a href="#">Masters</a></li>
                        <li class="active">Employee Roles</li>
                    </ol>
                </section>

                <section class="content">
                    <div class="row">
                        <!-- left column -->
                        <div class="col-md-12">
                            <!-- general form elements -->
                            <div class="box box-primary">
                                <div class="box-header with-border">
                                    <h3 class="box-title">Employee Roles Details</h3>
                                </div>
                                <!-- /.box-header -->
                                <!-- form start -->
                                <form action="#" method="post" class="form-horizontal">
                                    <div class="box-body">                                         
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Employee</label>
                                            <div class="col-sm-10">
                                                <s:select disabled="true" name="empNumber" list="listTblEmpMaster" headerKey="0" headerValue="--- Select ---" listKey="empNumber" listValue="empName" cssClass="form-control select2"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Role</label>
                                            <div class="col-sm-10">
                                                <s:select disabled="true" name="roleId" list="listTblRolesMaster" headerKey="0" headerValue="--- Select ---" listKey="roleId" listValue="roleName" cssClass="form-control select2"/>
                                            </div>
                                        </div>                                                                                
                                    </div>
                                    <!-- /.box-body -->
                                    <div class="box-footer">
                                        <a href="<s:url action="empRolesMasterList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>
                                    </div>
                                </form>

                            </div>
                        </div>
                    </div>
                    <!-- /.box -->
                </section>
                <!-- /.content -->
            </div>
            <!-- /.content-wrapper -->
             <%@include file="../header/footer.jsp" %>