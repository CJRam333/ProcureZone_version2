    <%-- 
    Document   : dashboard.jsp
    Created on : Sep 30, 2019, 2:21:24 PM
    Author     : ramesh.avv
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include  file="WEB-INF/jsp/header/header.jsp" %>   
        <!--<title>Plant Inventory</title>-->
         <link href="images/Nsllogo.png" rel="icon">
        <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                Dashboard
                <small>Control panel</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>                        
                <li class="active">Dashboard</li>
            </ol>
        </section>
        <!-- Main content -->
      <section class="content">
            <!-- Small boxes (Stat box) -->
            <div class="row">
                
                
                
                
                <s:if test="#session.PlantManager==7 || #session.FloorIncharge==8 || #session.DataEntryOperator==9 || #session.GoodsIncharge==10 || #session.GRNIncharge==11 || #session.IissueConfirm==12 || #session.ReceiptConfirm==13 || #session.QualityManager==14">
                <div class="col-lg-2 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-aqua" style="border-radius: 10px">
                        <div class="inner">
                            <s:if test="listTblpzIndentMaster != null">
                                <h3><s:property value="listTblpzIndentMaster.size"/></h3>
                            </s:if>
                            <s:else>
                                <h3>0</h3>
                            </s:else>
                            <p>Total Indents</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-information"></i>
                        </div>
                        
                        <s:if test="#session.PlantManager==7 ">
                        <a href="<s:url action="pzindentRequestList.action"/>" class="small-box-footer">More Info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:if>
                    <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                        
                    </div>
                </div>
                    
                    
                    
                    
                    <div class="col-lg-2 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-green" style="border-radius: 10px">
                        <div class="inner">
                            <s:if test="listTblpzIndentMaster != null">
                                <h3><s:property value="listTblpzIndentMaster8.size"/></h3>
                            </s:if>
                            <s:else>
                                <h3>0</h3>
                            </s:else>
                            <p>Quality Control</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-backspace"></i>
                        </div>
                        <s:if test="#session.QualityManager==14 ">
                            <a href="<s:url action="pzQualityMgrRequestList.action"/>" class="small-box-footer">More info <i class="fa fa-arrow-circle-right"></i></a>
                        <!--<a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>-->
                    </s:if>
                    <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                    </div>
                </div>
                
                <div class="col-lg-2 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-red-gradient" style="border-radius: 10px">
                        <div class="inner">
                            <s:if test="listTblpzIndentMaster != null">
                                <h3><s:property value="listTblpzIndentMasterQcrejected.size"/></h3>
                            </s:if>
                            <s:else>
                                <h3>0</h3>
                            </s:else>
                            <p>Quality Rej indents</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-alert-circled"></i>
                        </div>
                        
                        <s:if test="#session.QualityManager==14 ">
                            <a href="<s:url action="pzQualityMgrRejectedIndentList.action"/>" class="small-box-footer">More info <i class="fa fa-arrow-circle-right"></i></a>
                        <!--<a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>-->
                    </s:if>
                    <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                    </div>
                </div>
             
                
                <div class="col-lg-2 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-red" style="border-radius: 10px">
                        <div class="inner">
                            <s:if test="listTblpzIndentMaster != null">
                                <h3><s:property value="listTblpzIndentMaster0.size"/></h3>
                            </s:if>
                            <s:else>
                                <h3>0</h3>
                            </s:else>
                            <p>Rejected Indents</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-android-alert"></i>
                        </div>
                        <s:if test="#session.PlantManager==7 ">
                        <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:if>
                    <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                    </div>
                </div>
                
                
                
                <!-- ./col -->
                <div class="col-lg-2 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-blue-active" style="border-radius: 10px">
                        <div class="inner">
                            <h3><s:property value="listTblpzIndentMaster1.size"/></h3>
                            <p>Pending DEO Indents</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-android-bookmark"></i>
                        </div>
                            
                           <s:if test="#session.DataEntryOperator==9 ">
                        <a href="<s:url action="pzDeoMgrRequestList.action"/>" class="small-box-footer">More info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:if>
                        <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                    </div>
                </div>
                <!-- ./col -->
                <div class="col-lg-2 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-yellow" style="border-radius: 10px">
                        <div class="inner">
                            <h3><s:property value="listTblpzIndentMaster2.size"/></h3>
                            <p>pending Inve Issue</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-android-cart"></i>
                        </div>
                    
                    <s:if test="#session.FloorIncharge==8 ">
                        <a href="<s:url action="pzInventoryMgrRequestList.action"/>" class="small-box-footer">More info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:if>
                        <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                    </div>
                </div>
                <!-- ./col -->
                <div class="col-lg-2 col-xs-6" >
                    <!-- small box -->
                    <div class="small-box bg-blue-gradient" style="border-radius: 10px;box-shadow: 20px">
                        <div class="inner">
                            <h3><s:property value="listTblpzIndentMaster3.size"/></h3>
                            <p>Issue Confirmation Pending</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-android-attach"></i>
                        </div>
                    
                    <s:if test="#session.IissueConfirm==12 ">
                        <a href="<s:url action="pzIssueConfirmationList.action"/>" class="small-box-footer">More info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:if>
                        <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                    
                    </div>
                </div>
                    
                   
                    
                    <div class="col-lg-2 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-purple" style="border-radius: 10px">
                        <div class="inner">
                            <h3><s:property value="listTblpzIndentMaster4.size"/></h3>
                            <p>pending inventory Receipt</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-reply-all"></i>
                        </div>
                    
                    <s:if test="#session.GoodsIncharge==10 ">
                        <a href="<s:url action="pzFloorInchargeRequestList.action"/>" class="small-box-footer">More info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:if>
                        <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                    
                    </div>
                </div>
                    
                    <div class="col-lg-2 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-lime" style="border-radius: 10px">
                        <div class="inner">
                            <h3><s:property value="listTblpzIndentMaster5.size"/></h3>
                            <p>pending INV Receipt Confirmations</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-pie-graph"></i>
                        </div>
                    
                    <s:if test="#session.ReceiptConfirm==13 ">
                        <a href="<s:url action="pzReceiptConfirmationList.action"/>" class="small-box-footer">More info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:if>
                        <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                    </div>
                    
                    
                </div>
                    
                    <div class="col-lg-2 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-orange" style="border-radius: 10px">
                        <div class="inner">
                            <h3><s:property value="listTblpzIndentMaster6.size"/></h3>
                            <p>Total Pending GRN Receipt</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-printer"></i>
                        </div>
                    
                    <s:if test="#session.GRNIncharge==11 ">
                        <a href="<s:url action="pzGRPRequestList.action"/>" class="small-box-footer">More info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:if>
                        <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                    
                    </div>
                    
                    
                </div>
                            
                            
                <div class="col-lg-2 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-maroon-active" style="border-radius: 10px">
                        <div class="inner">
                            <h3><s:property value="listTblpzIndentMaster7.size"/></h3>
                            <p>Over All Completed Indents</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-thumbsup"></i>
                        </div>
                    
                    <s:if test="#session.GRNIncharge==11 ">
                        <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:if>
                        <s:else>
                    <a href="#" class="small-box-footer">No info <i class="fa fa-arrow-circle-right"></i></a>
                    </s:else>
                    
                    </div>
                </div>
         
                            
                            
                </s:if>
                <!-- ./col -->
            </div>
                            
            <!-- /.row -->
        </section>
        <!-- /.content -->
    </div>
    <%@include file="WEB-INF/jsp/header/footer.jsp" %>
</body>
</html>

