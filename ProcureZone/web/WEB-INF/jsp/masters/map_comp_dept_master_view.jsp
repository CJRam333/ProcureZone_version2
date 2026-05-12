<%-- 
    Document   : map_comp_dept_master
    Created on : Oct 1, 2019, 11:40:38 AM
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
                Company Department Map Master
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Masters</a></li>
                <li class="active">Company Department Map</li>
            </ol>
        </section>

        <section class="content">
            <div class="row">
                <!-- left column -->
                <div class="col-md-12">
                    <!-- general form elements -->
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">Company Department Map Details</h3>
                        </div>
                        <!-- /.box-header -->
                        <!-- form start -->
                        <form action="#" method="post" class="form-horizontal">
                            <div class="box-body">                                         
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Company</label>
                                    <div class="col-sm-10">
                                        <s:select name="compId" disabled="true" list="listTblCompanyMaster" headerKey="0" headerValue="--- Select ---" listKey="compId" listValue="compName" cssClass="form-control select2"/>

                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Department</label>
                                    <div class="col-sm-10">
                                        <s:select name="deptId" disabled="true" list="listTblDepartmentMaster" headerKey="0" headerValue="--- Select ---" listKey="deptId" listValue="deptName" cssClass="form-control select2"/>
                                    </div>
                                </div> 

                            </div>
                            <!-- /.box-body -->
                            <div class="box-footer">
                                <a href="<s:url action="compDeptMasterList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <!-- /.box -->
        </section>
        <!-- /.content -->
    </div>
    <%@include file="../header/footer.jsp" %>