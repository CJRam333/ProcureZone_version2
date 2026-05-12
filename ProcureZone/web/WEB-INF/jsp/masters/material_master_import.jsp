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
                Material Master
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Masters</a></li>
                <li class="active">Material</li>
            </ol>
        </section>
        <section class="content">
            <div class="row">
                <!-- left column -->
                <div class="col-md-12">
                    <!-- general form elements -->
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">Material Details</h3>
                        </div>
                        <!-- /.box-header -->
                        <!-- form start -->
                        <form action="importMaterialMaster.action" method="post" class="form-horizontal" enctype="multipart/form-data">
                            <div><p class="message"><span style="font-size: 5; color: green;"><s:property value="message"/></span></p></div>
                            <div class="box-body">                                         
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">Material</label>
                                    <div class="col-sm-10">
                                        <s:file name="fileUpload" id="fileUpload1"/>
                                    </div>
                                </div>                                  
                            </div>
                            <!-- /.box-body -->
                            <div class="box-footer">
                                <button type="submit" class="btn btn-primary">Import</button>
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

