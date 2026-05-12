<%-- 
    Document   : company_master
    Created on : Sep 30, 2019, 9:19:38 AM
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
                Employee Plant Role Map
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Masters</a></li>
                <li class="active">Employee Plant Role Map</li>
            </ol>
        </section>
        <section class="content">
            <div class="row">
                <!-- left column -->
                <div class="col-md-12">
                    <!-- general form elements -->
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">Employee Plant Role Map</h3>
                        </div>
                        <!-- /.box-header -->
                        <!-- form start -->
                        <form action="saveEmpPlantRoleMap.action" method="post" class="form-horizontal">
                            <div class="box-body">
                                
                                
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Company</label>
                                    <div class="col-sm-10">
                                        <s:select name="compCodeMap" id="compCode1" list="listTblPzCompanyMaster" onchange="this.form.action='getoutputPlantDetails1.action'; this.form.submit();"  headerKey="0" headerValue="--- Select ---" listKey="compCode" listValue="compName" cssClass="form-control select1"/>

                                    </div>
                                </div>
                                        
                                        
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Plant Code</label>
                                    <div class="col-sm-10">
                                        <s:select name="plantCodeMap" list="listTblPlantMaster" headerKey="0" headerValue="--- Select ---" listKey="plantCode" listValue="{PlantCode} +' - '+  plantName"  cssClass="form-control select2"/>
                                    </div>
                                </div> 
                                
                                
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Employee</label>
                                    <div class="col-sm-10">
                                        <s:select name="empcodemap" id="compCode11" list="listTblEmpMaster"  headerKey="0" headerValue="--- Select ---" listKey="empNumber" listValue="empName +' - '+  empId" cssClass="form-control select2"/>

                                    </div>
                                </div>
                                        
                                        
                                        
                                        
                                
                                    
<!--                                    <div class="form-group">
                                    <label class="col-sm-2 control-label">Plant Role</label>
                                    <div class="col-sm-10">
                                        <s:select name="plantrolemap" id ="myoutputmata" list="listTblRolesMaster"  headerKey="0" headerValue="--- Select ---" listKey="roleId" listValue="roleName" cssClass="form-control select2"/>
                                    </div>
                                </div> -->
                            </div>
                            <!-- /.box-body -->
                            <div class="box-footer">
                                <button type="submit" class="btn btn-primary">Submit</button>
                                <!--<a href="<s:url action="materialMasterList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>-->
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

