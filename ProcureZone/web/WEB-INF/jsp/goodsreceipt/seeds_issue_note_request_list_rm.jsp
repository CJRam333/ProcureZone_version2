<%-- 
    Document   : seeds_indent_request_list
    Created on : Oct 3, 2019, 12:17:54 PM
    Author     : ramesh.avv
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib uri="/WEB-INF/displaytag.tld" prefix="display" %>
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
                Indent Request
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li><a href="#">Masters</a></li>
                <li class="active">Issue Note Request List</li>
            </ol>
        </section>
        <!-- Main content -->
        <section class="content">
            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <div class="box-header">
                            <h3 class="box-title">Issue Note Request List</h3>
                        </div>
                        <script>
                            function getSubmit(val)
                            {
                                document.getElementById("receipt_Id").value = document.getElementById("statusId").value;
                                var frm = document.getElementById("issueNoteStatus");
                                frm.submit();
                            }
                            function getSubmit1(val)
                            {
                                document.getElementById("receipt_Id1").value = val;
                                var frm = document.getElementById("approveIssueNote");
                                frm.submit();
                            }
                            function getSubmit2(val)
                            {
                                document.getElementById("receipt_Id2").value = val;
                                var frm = document.getElementById("finalApproveIssueNote");
                                frm.submit();
                            }                        
                        </script>
                        <s:form action="rmIssueNoteRequestList1.action" id="issueNoteStatus" method="post"> 
                            <s:hidden id="receipt_Id" name="receiptId" />
                        </s:form>
                        <s:form action="getRmIssueNoteDetails.action" id="approveIssueNote" method="post"> 
                            <s:hidden id="receipt_Id1" name="receiptId" />
                        </s:form>
                        <s:form action="getRmIssueNoteDetailsView.action" id="finalApproveIssueNote" method="post"> 
                            <s:hidden id="receipt_Id2" name="receiptId" />
                        </s:form>
                        <s:form action="getRejectedIssueNoteDetails.action" id="rejectedIssueNote" method="post"> 
                            <s:hidden id="receipt_Id3" name="receiptId" />
                        </s:form>                       
                        
                        <div class="box-body">
                            <div class="box-footer">
                                <label class="col-sm-2 headline text-yellow" style="font-size:20px ">Filter Status</label>
                                <div class="col-xs-2">
                                    <s:select onchange="getSubmit();" name="statusId" id="statusId" list="listTblIndentStatus" headerKey="0" headerValue="--- Select ---" listKey="indentStatusId" listValue="indentStatusName" cssClass="form-control select2"/>
                                </div>                                    
                                <a href="<s:url action="addIssueNoteRequest.action"/>"> <span class="box-title btn btn-success pull-right">Add Issue Note</span></a>
                            </div>
                            <table id="example1" class="table table-bordered table-striped">
                                <thead>
                                    <tr>
                                        <th>Indent No.</th>
                                        <th>Employee Name</th>
                                        <th>Company</th>
                                        <th>Material Description</th>
                                        <th>Requested Quantity</th>
                                        <th>Balance Quantity in Stores</th>
                                        <th>Modified Date</th>
                                        <th>Modified User</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <s:iterator value="listTblGoodsReceipt">
                                        <s:if test="tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==1">
                                            <tr>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="tblIndentMaster.indentNo"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="tblEmpMasterByGoodsReceiptCreatedby.empName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="tblIndentMaster.tblCompanyMaster.compName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="tblIndentDetails.tblMaterialMaster.materialName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="goodsReceiptRequestedQuantity"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="goodsReceiptBalanceQuantityStores"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="goodsReceiptLmd"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="tblEmpMasterByGoodsReceiptLmu.empName"/></s:a></td>
                                                <td><s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==1 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" >Pending</s:a></s:if>
                                                    <s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==2 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" >RM Rejected</s:a></s:if>
                                                    <s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==3 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" >RM Approved</s:a></s:if>
                                                    </td>
                                                    </tr>
                                        </s:if>
                                        <s:elseif test="tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==3">
                                            <tr>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="tblIndentMaster.indentNo"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="tblEmpMasterByGoodsReceiptCreatedby.empName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="tblIndentMaster.tblCompanyMaster.compName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="tblIndentDetails.tblMaterialMaster.materialName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="goodsReceiptRequestedQuantity"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="goodsReceiptBalanceQuantityStores"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="goodsReceiptLmd"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="tblEmpMasterByGoodsReceiptLmu.empName"/></s:a></td>
                                                <td><s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==1 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" >Pending</s:a></s:if>
                                                    <s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==2 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" >RM Rejected</s:a></s:if>
                                                    <s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==3 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" >RM Approved</s:a></s:if>
                                                    </td>
                                                    </tr>
                                        </s:elseif>
                                        <s:elseif test="tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==2">
                                            <tr>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="tblIndentMaster.indentNo"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="tblEmpMasterByGoodsReceiptCreatedby.empName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="tblIndentMaster.tblCompanyMaster.compName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="tblIndentDetails.tblMaterialMaster.materialName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="goodsReceiptRequestedQuantity"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="goodsReceiptBalanceQuantityStores"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="goodsReceiptLmd"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit2(%{goodsReceiptId})" ><s:property value="tblEmpMasterByGoodsReceiptLmu.empName"/></s:a></td>
                                                <td><s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==1 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit3(%{goodsReceiptId})" >Pending</s:a></s:if>
                                                    <s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==2 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit3(%{goodsReceiptId})" >RM Rejected</s:a></s:if>
                                                    <s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==3 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit3(%{goodsReceiptId})" >RM Approved</s:a></s:if>
                                                </td>
                                            </tr>
                                        </s:elseif>                                        
                                        <s:else>
                                            <tr>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="tblIndentMaster.indentNo"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="tblEmpMasterByGoodsReceiptCreatedby.empName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="tblIndentMaster.tblCompanyMaster.compName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="tblIndentDetails.tblMaterialMaster.materialName"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="goodsReceiptRequestedQuantity"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="goodsReceiptBalanceQuantityStores"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="goodsReceiptLmd"/></s:a></td>
                                                <td><s:a href="javascript:void(0)" onclick="getSubmit1(%{goodsReceiptId})" ><s:property value="tblEmpMasterByGoodsReceiptLmu.empName"/></s:a></td>
                                                <td><s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==1 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit4(%{goodsReceiptId})" >Pending</s:a></s:if>
                                                    <s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==2 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit4(%{goodsReceiptId})" >RM Rejected</s:a></s:if>
                                                    <s:if test="%{tblIndentStatusByGoodsReceiptApprovedStatus.indentStatusId==3 && tblIndentStatusByGoodsReceiptStoresbyStatus.indentStatusId==1}"><s:a href="javascript:void(0)" onclick="getSubmit4(%{goodsReceiptId})" >RM Approved</s:a></s:if>
                                                    </td>
                                                    </tr>
                                        </s:else>
                                    </s:iterator>            
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <!-- /.box -->
                </div>
                <!-- /.col -->
            </div>
            <!-- /.row -->
        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->
    <footer class="main-footer">
        <div class="pull-right hidden-xs">
            <b>Version</b> 1.0.0
        </div>
        <strong>Copyright &copy; 2019 <a href="http://www.nuziveeduseeds.com/">Nuziveedu Seeds</a>.</strong> All rights reserved.
    </footer>

    <!-- jQuery 3 -->
    <script src="Seeds/assets/jquery/dist/jquery.min.js"></script>    
    <script>
        $.widget.bridge('uibutton', $.ui.button);
    </script>
    <!-- Bootstrap 3.3.7 -->
    <script src="Seeds/assets/bootstrap/dist/js/bootstrap.min.js"></script>    
    <!-- Slimscroll -->
    <script src="Seeds/assets/jquery-slimscroll/jquery.slimscroll.min.js"></script>
    <!-- FastClick -->
    <script src="Seeds/assets/fastclick/lib/fastclick.js"></script>
    <!-- AdminLTE App -->
    <script src="Seeds/dist/js/seeds.min.js"></script>

    <!-- AdminLTE for demo purposes -->
    <script src="Seeds/dist/js/demo.js"></script>    <!-- iCheck 1.0.1 -->
    <script src="Seeds/plugins/iCheck/icheck.min.js"></script>
    <!-- DataTables -->
    <script src="Seeds/assets/datatables.net/js/jquery.dataTables.min.js"></script>
    <script src="Seeds/assets/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>    
    <script>
        $(function () {
            $('#example1').DataTable()
            $('#example2').DataTable({
                'background': red,
                'paging': true,
                'lengthChange': false,
                'searching': false,
                'ordering': true,
                'info': true,
                'autoWidth': false
            })
        })

    </script>
    <script>
        $(function () {
            $('input').iCheck({
                checkboxClass: 'icheckbox_square-blue',
                radioClass: 'iradio_square-blue',
                increaseArea: '20%' /* optional */
            });
        });
    </script>
</body>
</html>
