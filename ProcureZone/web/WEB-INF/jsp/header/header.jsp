<%-- 
    Document   : headermain
    Created on : Sep 27, 2019, 11:52:49 AM
    Author     : ramesh.avv
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<%@taglib uri="/WEB-INF/displaytag.tld" prefix="display" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="Seeds/assets/bootstrap/dist/css/bootstrap.min.css">
        <!-- Font Awesome -->
        <link rel="stylesheet" href="Seeds/assets/font-awesome/css/font-awesome.min.css">
        <!-- Ionicons -->
        <link rel="stylesheet" href="Seeds/assets/Ionicons/css/ionicons.min.css">
        <!-- Theme style -->
        <link rel="stylesheet" href="Seeds/dist/css/seeds.min.css">
        <!-- AdminLTE Skins. Choose a skin from the css/skins
             folder instead of downloading all of them to reduce the load. -->
        <link rel="stylesheet" href="Seeds/dist/css/skins/_all-skins.min.css">
        <!-- Morris chart -->
        <link rel="stylesheet" href="Seeds/assets/morris.js/morris.css">
        <!-- jvectormap -->
        <link rel="stylesheet" href="Seeds/assets/jvectormap/jquery-jvectormap.css">
        <!-- Date Picker -->
        <link rel="stylesheet" href="Seeds/assets/bootstrap-datepicker/dist/css/bootstrap-datepicker.min.css">
        <!-- Daterange picker -->
        <link rel="stylesheet" href="Seeds/assets/bootstrap-daterangepicker/daterangepicker.css">
        <!-- bootstrap wysihtml5 - text editor -->
        <link rel="stylesheet" href="Seeds/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
        <!-- iCheck for checkboxes and radio inputs -->
        <link rel="stylesheet" href="Seeds/plugins/iCheck/all.css">
        <!-- Bootstrap Color Picker -->
        <link rel="stylesheet" href="Seeds/assets/bootstrap-colorpicker/dist/css/bootstrap-colorpicker.min.css">
        <!-- Bootstrap time Picker -->
        <link rel="stylesheet" href="Seeds/plugins/timepicker/bootstrap-timepicker.min.css">
        <!-- Select2 -->
        <link rel="stylesheet" href="Seeds/assets/select2/dist/css/select2.min.css">
        <!-- DataTables -->
        <link rel="stylesheet" href="Seeds/assets/datatables.net-bs/css/dataTables.bootstrap.min.css">
        <!-- iCheck -->
        <link rel="stylesheet" href="Seeds/plugins/iCheck/square/blue.css">
        <!-- Google Font -->
        <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,300italic,400italic,600italic">
    </head>
    <body class="hold-transition skin-blue sidebar-mini">
        <div class="wrapper">
            <header class="main-header">
                <a href="#" class="logo">
                    <!-- mini logo for sidebar mini 50x50 pixels -->
                    <s:if test="#session.SuperAdmin==1 || #session.role == 3 || #session.Procurement == 6 || #session.Admin==2 || #session.Supervisor==4 || #session.DepartmentHead==5">
                    <span class="logo-mini"><b>Procure Zone</b></span>
                    
                    <!-- logo for regular state and mobile devices -->
                    <span class="logo-lg"><b>Procure Zone</b></span>
                    </s:if>
                    
                    <s:if test="#session.SuperAdmin==1 || #session.PlantManager == 7 || #session.FloorIncharge==8 || #session.DataEntryOperator == 9 || #session.GoodsIncharge==10 || #session.GRNIncharge==11 || #session.QualityManager==14">
                    <span class="logo-mini"><b>NSLDAKSH</b></span>
                    
                    <!-- logo for regular state and mobile devices -->
                    <span class="logo-lg"><b>NSLDAKSH</b></span>
                    </s:if>
                </a>
                <nav class="navbar navbar-static-top">
                    <!-- Sidebar toggle button-->
                    <a href="#" class="sidebar-toggle" data-toggle="push-menu" role="button">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </a>
                    
                    <s:if test="#session.PlantManager==7 || #session.FloorIncharge==8 || #session.DataEntryOperator==9 || #session.GoodsIncharge==10 || #session.GRNIncharge==11 || #session.IissueConfirm==12 || #session.ReceiptConfirm==13 || #session.QualityManager==14">
<a href="MyDashboard.action" class="btn btn-primary btn-lg " style="border: none;">My Dashboard</a>
<!--<a href="#" class="btn btn-primary btn-lg " style="border: none;align-content: center">Seeds Plant Inventory Module System</a>-->
        </s:if>

                    <div class="navbar-custom-menu">
                        <ul class="nav navbar-nav">
                            <!-- User Account: style can be found in dropdown.less -->
                            <li class="dropdown user user-menu">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                    <img src="<s:property value='#session.empImage'/>" class="user-image" alt="User Image">
                                    <span class="hidden-xs"><s:property value="#session.user"/></span>
                                </a>
                                <ul class="dropdown-menu">
                                    <!-- User image -->
                                    <li class="user-header">
                                        <img src="<s:property value='#session.empImage'/>" class="img-circle" alt="User Image">

                                        <p>
                                            Hello,<s:property value="#session.user"/>
                                            <small>Member since <s:property value="#session.joinDate"/></small>
                                        </p>
                                    </li>
                                    <!-- Menu Body -->
                                    <li class="user-body">
                                        <div class="row">
                                            <div class="col-xs-4 text-center">
                                                <a href="#"></a>
                                            </div>
                                            <div class="col-xs-4 text-center">
                                                <a href="#"></a>
                                            </div>
                                            <div class="col-xs-4 text-center">
                                                <a href="#"></a>
                                            </div>
                                        </div>
                                        <!-- /.row -->
                                    </li>
                                    <!-- Menu Footer-->
                                    <li class="user-footer">
                                        <div class="pull-left">
                                            <a href="<s:url action="viewEmployee.action"/>" class="btn btn-default btn-flat">Profile</a>
                                        </div>
                                        <div class="pull-right">
                                            <a href="<s:url action="logOutAction.action"/>" class="btn btn-default btn-flat">Sign out</a>
                                        </div>
                                    </li>
                                </ul>
                            </li>                            
                        </ul>
                    </div>
                </nav>
            </header>
            <!-- =============================================== -->
            <!-- Left side column. contains the sidebar -->
            <aside class="main-sidebar">
                <!-- sidebar: style can be found in sidebar.less -->
                <section class="sidebar">
                    <!-- Sidebar user panel -->
                    <div class="user-panel">
                        <div class="pull-left image">
                            <img src="<s:property value='#session.empImage'/>" class="img-circle" alt="User Image">
                        </div>
                        <div class="pull-left info">
                            <p><s:property value="#session.user"/></p>
                            <a href="#"><i class="fa fa-circle text-success"></i> Online</a>
                        </div>
                    </div>
                    <!-- search form -->
                    <form action="#" method="get" class="sidebar-form">
                        <div class="input-group">
                            <input type="text" name="q" class="form-control" placeholder="Search...">
                            <span class="input-group-btn">
                                <button type="submit" name="search" id="search-btn" class="btn btn-flat"><i class="fa fa-search"></i>
                                </button>
                            </span>
                        </div>
                    </form>
                    <!-- /.search form -->
                    <!-- sidebar menu: : style can be found in sidebar.less -->
                    <ul class="sidebar-menu" data-widget="tree">
                        <li class="header">MAIN NAVIGATION</li>
                            <s:if test="#session.SuperAdmin==1 || #session.Admin==2">
                            <li class="treeview">
                                <a href="#">
                                    <i class="fa fa-folder"></i> <span>Masters</span>
                                    <span class="pull-right-container">
                                        <i class="fa fa-angle-left pull-right"></i>
                                    </span>
                                </a>
                                <ul class="treeview-menu">
                                    <li><a href="<s:url action="companyMasterList.action"/>"><i class="fa fa-circle-o"></i>Company</a></li>
                                    <li><a href="<s:url action="locationMasterList.action"/>"><i class="fa fa-circle-o"></i>Location</a></li>
                                    <li><a href="<s:url action="departmentMasterList.action"/>"><i class="fa fa-circle-o"></i>Department</a></li>
                                    <li><a href="<s:url action="sectionMasterList.action"/>"><i class="fa fa-circle-o"></i>Section</a></li>
                                    <li><a href="<s:url action="compLocMasterList.action"/>"><i class="fa fa-circle-o"></i>Company Location Map</a></li>
                                    <li><a href="<s:url action="compDeptMasterList.action"/>"><i class="fa fa-circle-o"></i>Company Department Map</a></li>
                                    <li><a href="<s:url action="employeeMasterList.action"/>"><i class="fa fa-circle-o"></i>Employee</a></li>
                                        <%--<li><a href="<s:url action="userMasterList.action"/>"><i class="fa fa-circle-o"></i>User</a></li>--%>
                                    <li><a href="<s:url action="rolesMasterList.action"/>"><i class="fa fa-circle-o"></i>roles</a></li>
                                    <li><a href="<s:url action="reportingToMasterList.action"/>"><i class="fa fa-circle-o"></i>Reporting To</a></li>
                                    <li><a href="<s:url action="empRolesMasterList.action"/>"><i class="fa fa-circle-o"></i>Employee Roles</a></li>
                                    <li><a href="<s:url action="materialMasterList.action"/>"><i class="fa fa-circle-o"></i>Material</a></li>
                                        <%--<li><a href="<s:url action="compLocMaterialMasterList.action"/>"><i class="fa fa-circle-o"></i>Company Location Material Map</a></li>--%>
                                    <li><a href="<s:url action="umoMasterList.action"/>"><i class="fa fa-circle-o"></i>UOM</a></li>
                                    <li><a href="<s:url action="plantMasterList.action"/>"><i class="fa fa-circle-o"></i>Plant</a></li>
                                    <li><a href="<s:url action="compPlantMaterialMasterList.action"/>"><i class="fa fa-circle-o"></i>Company Plant Material Map</a></li>
                                    <li><a href="<s:url action="outputmateriallist.action"/>"><i class="fa fa-circle-o"></i>Output Material Update</a></li>
                                    <li><a href="<s:url action="getListEmpMap.action"/>"><i class="fa fa-circle-o"></i>Employee Plant&Role Map</a></li>
                                   
                                </ul>
                            </li>
                        </s:if>
                        <li class="treeview active">
                            <s:if test="#session.SuperAdmin==1 || #session.role == 3 || #session.Procurement == 6 || #session.Admin==2 || #session.Supervisor==4 || #session.DepartmentHead==5">
                            <a href="<s:url action="indentRequestList.action"/>">
                                <i class="fa fa-folder"></i> <span>Indent</span>
                                <span class="pull-right-container">
                                    <i class="fa fa-angle-left pull-right"></i>
                                </span>
                            </a></s:if>
                            <ul class="treeview-menu">
                                <s:if test="#session.SuperAdmin==1 || #session.role==3">
                                    <li><a href="<s:url action="indentRequestList.action"/>"><i class="fa fa-circle-o"></i>Indent Request</a></li>
                                    </s:if>
                                    <s:if test="#session.SuperAdmin==1 || #session.Admin==2 || #session.Supervisor==4 ">
                                    <li><a href="<s:url action="rmIndentRequestList.action"/>"><i class="fa fa-circle-o"></i>Indent Request RM</a></li>
                                    </s:if>
                                    <s:if test="#session.SuperAdmin==1 || #session.DepartmentHead==5">
                                    <li><a href="<s:url action="deptHeadIndentRequestList.action"/>"><i class="fa fa-circle-o"></i>Indent Request Dept Head</a></li>
                                    </s:if>
                                    <s:if test="#session.SuperAdmin==1 || #session.Procurement==6">
                                    <li><a href="<s:url action="getProcurementList.action"/>"><i class="fa fa-circle-o"></i>Indent Request Procurement</a></li>
                                    </s:if>
                            </ul>
                        </li>
                        <s:if test="#session.SuperAdmin==1 || #session.DepartmentHead==5 || #session.Admin==2">
                                <%--<s:if test="#session.empNumber!=41">--%>
                                     <li><a href="<s:url action="indentReport.action"/>"><i class="fa fa-file-excel-o"></i>Report</a></li>
                                <%--</s:if>--%>
                        </s:if>
                                     <s:if test="#session.SuperAdmin==1 || #session.role == 3 || #session.Procurement == 6 || #session.Admin==2 || #session.Supervisor==4 ">
                        <li class="treeview active">
                            <a href="<s:url action="issueNoteRequestList.action"/>">
                                <i class="fa fa-folder"></i> <span>Issue Note</span>
                                <span class="pull-right-container">
                                    <i class="fa fa-angle-left pull-right"></i>
                                </span>
                            </a>     </s:if>

                            <ul class="treeview-menu">
                                <s:if test="#session.SuperAdmin==1 || #session.role==3">
                                    <li><a href="<s:url action="issueNoteRequestList.action"/>"><i class="fa fa-circle-o"></i>Issue Note Request</a></li>
                                    </s:if>
                                    <s:if test="#session.SuperAdmin==1 || #session.Admin==2 || #session.Supervisor==4 ">
                                    <li><a href="<s:url action="issueNoteRequestRmList.action"/>"><i class="fa fa-circle-o"></i>Issue Note RM Request</a></li>
                                    </s:if>                                    
                                    <s:if test="#session.SuperAdmin==1 || #session.Procurement==6">
                                    <li><a href="<s:url action="issueNoteRequestStoresList.action"/>"><i class="fa fa-circle-o"></i>Issue Note Procurement Request</a></li>
                                    </s:if>
                            </ul>
                            
                        <li class="treeview active">
                            <s:if test="#session.SuperAdmin==1 || #session.PlantManager == 7 || #session.FloorIncharge==8 || #session.DataEntryOperator == 9 || #session.GoodsIncharge==10 || #session.GRNIncharge==11 || #session.IissueConfirm==12 || #session.ReceiptConfirm==13 || #session.QualityManager==14">
                                <a href="<s:url action="addPlantIndentRequest.action"/>">
                                <i class="fa fa-folder"></i> <span>Plant Indent Request</span>
                                <span class="pull-right-container">
                                    <i class="fa fa-angle-left pull-right"></i>
                                </span>
                                </a> </s:if>
                            <ul class="treeview-menu">
                                <s:if test="#session.SuperAdmin==1 || #session.PlantManager == 7 ">
                                    <li><a href="<s:url action="pzindentRequestList.action"/>"><i class="fa fa-circle-o"></i>Indent from Plant Manager</a></li>
                                </s:if>
                                    
                                    <s:if test="#session.SuperAdmin==1 || #session.QualityManager==14 ">
                                    <li><a href="<s:url action="pzQualityMgrRequestList.action"/>"><i class="fa fa-circle-o"></i>Quality Control</a></li>
                                    </s:if>
                                    
                                    
                                    <s:if test="#session.SuperAdmin==1 || #session.DataEntryOperator==9 ">
                                    <li><a href="<s:url action="pzDeoMgrRequestList.action"/>"><i class="fa fa-circle-o"></i>Indent For DEO</a></li>
                                    </s:if>
                                    
                            
                                <s:if test="#session.SuperAdmin==1 || #session.FloorIncharge==8 ">
                                    <li><a href="<s:url action="pzInventoryMgrRequestList.action"/>"><i class="fa fa-circle-o"></i>Indent for Inventory Issue</a></li>
                    
                                </s:if>
                                    
                                    <s:if test="#session.SuperAdmin==1 || #session.IissueConfirm==12 ">
                                    <li><a href="<s:url action="pzIssueConfirmationList.action"/>"><i class="fa fa-circle-o"></i>Inventory Issue Confirmation</a></li>
                                    </s:if>
                                    
                               
                                <s:if test="#session.SuperAdmin==1 || #session.GoodsIncharge==10 ">
                                    <li><a href="<s:url action="pzFloorInchargeRequestList.action"/>"><i class="fa fa-circle-o"></i>Indent for Inventory Receipt</a></li>
                                    </s:if>
                                    
                                    
                                    <s:if test="#session.SuperAdmin==1 || #session.ReceiptConfirm==13 ">
                                    <li><a href="<s:url action="pzReceiptConfirmationList.action"/>"><i class="fa fa-circle-o"></i>Inventory Receipt Confirmation</a></li>
                                    </s:if>
                                    
                                    
                                    <s:if test="#session.SuperAdmin==1 || #session.GRNIncharge==11 ">
                                    <li><a href="<s:url action="pzGRPRequestList.action"/>"><i class="fa fa-circle-o"></i>Indent for GRN Receipt</a></li>
                                    </s:if>
                                    
                                    
                                    
                            </ul>
                        </ul>
                    </ul>
                 
                </section>
                <!-- /.sidebar -->
            </aside>
