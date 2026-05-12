<%-- 
    Document   : user_master
    Created on : Oct 1, 2019, 4:41:03 PM
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
                User Master
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Masters</a></li>
                <li class="active">User</li>
            </ol>
        </section>

        <section class="content">
            <div class="row">
                <!-- left column -->
                <div class="col-md-12">
                    <!-- general form elements -->
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">User Details</h3>
                        </div>
                        <!-- /.box-header -->
                        <!-- form start -->
                        <form action="#" method="post" class="form-horizontal">
                            <div class="box-body">
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Employee</label>
                                    <div class="col-sm-10">
                                        <s:select name="empNumber" disabled="true" list="listTblEmpMaster" headerKey="0" headerValue="--- Select ---" listKey="empNumber" listValue="empName" cssClass="form-control select2"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">User Name</label>
                                    <div class="col-sm-10">
                                        <s:textfield name="uName" readonly="true" cssClass="form-control"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">User Password</label>
                                    <div class="col-sm-10">
                                        <s:password name="uPassword" readonly="true" cssClass="form-control"/>
                                    </div>
                                </div> 

                            </div>
                            <!-- /.box-body -->
                            <div class="box-footer">
                                <a href="<s:url action="userMasterList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>
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