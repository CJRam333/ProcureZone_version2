<%-- 
    Document   : employee_master
    Created on : Oct 1, 2019, 2:43:08 PM
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
                Employee Master
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Masters</a></li>
                <li class="active">Employee</li>
            </ol>
        </section>

        <section class="content">
            <div class="row">
                <!-- left column -->
                <div class="col-md-12">
                    <!-- general form elements -->
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">Employee Details</h3>
                        </div>
                        <!-- /.box-header -->
                        <!-- form start -->
                        <form action="saveEmployee.action" method="post" class="form-horizontal" enctype="multipart/form-data">
                            <div class="box-body">                                         
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Employee ID</label>
                                    <div class="col-sm-10">
                                        <s:textfield name="employeeId" cssClass="form-control"/>

                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Employee Name</label>
                                    <div class="col-sm-10">
                                        <s:textfield name="employeeName" cssClass="form-control"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Employee Email</label>
                                    <div class="col-sm-10">
                                        <s:textfield name="employeeEmail" cssClass="form-control"/>
                                    </div>
                                </div>    
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Joining Date</label>
                                    <div class="date col-sm-10">                                                
                                        <s:textfield name="employeeJoinDate" cssClass="form-control pull-right" id="datepicker"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Company</label>
                                    <div class="col-sm-10">
                                            <s:select name="compId" list="listTblCompanyMaster" headerKey="0" headerValue="--- Select ---" listKey="compId" listValue="compName" cssClass="form-control select2" multiple="true"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Location</label>
                                    <div class="col-sm-10">
                                        <s:select name="locId" list="listTblLocationMaster" headerKey="0" headerValue="--- Select ---" listKey="locId" listValue="locName" cssClass="form-control select2"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Department</label>
                                    <div class="col-sm-10">
                                        <s:select name="deptId" list="listTblDepartmentMaster" headerKey="0" headerValue="--- Select ---" listKey="deptId" listValue="deptName" cssClass="form-control select2"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Designation</label>
                                    <div class="col-sm-10">
                                        <s:textfield name="employeeDesig" cssClass="form-control"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Cost Center</label>
                                    <div class="col-sm-10">
                                        <s:textfield name="employeeCost" cssClass="form-control"/>
                                    </div>
                                </div>    
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Employee Image</label>
                                    <div class="col-sm-10">
                                        <s:file name="fileUpload" id="fileUpload"/> 
                                    </div>
                                </div>
                            </div>
                            <!-- /.box-body -->
                            <div class="box-footer">
                                <button type="submit" class="btn btn-primary">Submit</button>
                                <a href="<s:url action="employeeMasterList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>
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