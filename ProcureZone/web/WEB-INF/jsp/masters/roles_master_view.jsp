<%-- 
    Document   : roles_master
    Created on : Oct 1, 2019, 1:38:09 PM
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
                        Roles Master
                        <small></small>
                    </h1>
                    <ol class="breadcrumb">
                        <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                        <li><a href="#">Masters</a></li>
                        <li class="active">Roles</li>
                    </ol>
                </section>

                <section class="content">
                    <div class="row">
                        <!-- left column -->
                        <div class="col-md-12">
                            <!-- general form elements -->
                            <div class="box box-primary">
                                <div class="box-header with-border">
                                    <h3 class="box-title">Roles Details</h3>
                                </div>
                                <!-- /.box-header -->
                                <!-- form start -->
                                <form action="#" method="post" class="form-horizontal">
                                    <div class="box-body">                                         
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Role Code</label>
                                            <div class="col-sm-10">
                                                <s:textfield readonly="true" name="rCode" cssClass="form-control"/>

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Role Name</label>
                                            <div class="col-sm-10">
                                                <s:textfield readonly="true" name="rName" cssClass="form-control"/>
                                            </div>
                                        </div> 
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">View Option</label>
                                            <div class="col-sm-10">
                                                <s:checkbox disabled="true" name="rView" cssClass="checkbox"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Add Option</label>
                                            <div class="col-sm-10">
                                                <s:checkbox disabled="true" name="rAdd" cssClass="checkbox"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Edit Option</label>
                                            <div class="col-sm-10">
                                                <s:checkbox disabled="true" name="rEdit" cssClass="checkbox"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Delete Option</label>
                                            <div class="col-sm-10">
                                                <s:checkbox disabled="true" name="rDelete" cssClass="checkbox"/>
                                            </div>
                                        </div>

                                    </div>
                                    <!-- /.box-body -->
                                    <div class="box-footer">
                                        <a href="<s:url action="rolesMasterList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>
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
