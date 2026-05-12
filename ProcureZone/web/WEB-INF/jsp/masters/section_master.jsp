<%-- 
    Document   : section_master
    Created on : Oct 1, 2019, 11:23:13 AM
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
                        Section Master
                        <small></small>
                    </h1>
                    <ol class="breadcrumb">
                        <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                        <li><a href="#">Masters</a></li>
                        <li class="active">Section</li>
                    </ol>
                </section>

                <section class="content">
                    <div class="row">
                        <!-- left column -->
                        <div class="col-md-12">
                            <!-- general form elements -->
                            <div class="box box-primary">
                                <div class="box-header with-border">
                                    <h3 class="box-title">Section Details</h3>
                                </div>
                                <!-- /.box-header -->
                                <!-- form start -->
                                <form action="saveSection.action" method="post" class="form-horizontal">
                                    <div class="box-body">                                         
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Section Code</label>
                                            <div class="col-sm-10">
                                                <s:textfield name="sectionCode" cssClass="form-control"/>

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Section Name</label>
                                            <div class="col-sm-10">
                                                <s:textfield name="sectionName" cssClass="form-control"/>
                                            </div>
                                        </div> 

                                    </div>
                                    <!-- /.box-body -->
                                    <div class="box-footer">
                                        <button type="submit" class="btn btn-primary">Submit</button>
                                        <a href="<s:url action="sectionMasterList.action"/>"> <span class="btn btn-danger pull-right">Back to List</span></a>
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