<%-- 
    Document   : login
    Created on : Sep 27, 2019, 2:00:02 PM
    Author     : ramesh.avv
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="Seeds/assets//bootstrap/dist/css/bootstrap.min.css">
        <!-- Font Awesome -->
        <link rel="stylesheet" href="Seeds/assets/font-awesome/css/font-awesome.min.css">
        <!-- Ionicons -->
        <link rel="stylesheet" href="Seeds/assets/Ionicons/css/ionicons.min.css">
        <!-- Theme style -->
        <link rel="stylesheet" href="Seeds/dist/css/seeds.min.css">
        <!-- iCheck -->
        <link rel="stylesheet" href="Seeds/plugins/iCheck/square/blue.css">
        <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,300italic,400italic,600italic">
        <style>
        body {
            background-image: url('images/guide1_1.jpg'); /* Replace with the actual path to your image */
            background-size: cover; /* Adjust to your preferred background size */
            background-repeat: no-repeat;
            background-attachment: fixed; /* Optional: Fixed background */
        }
    </style>
    </head>
    <body class="login">

        <script>
            function checkLoginForm()
            {

                var userName = document.getElementById("userName").value;
                var password = document.getElementById("password").value;
                if (userName.length == 0 || password.length == 0) {
                    return false;
                } else {
                    var form1 = document.getElementById("loginForm");
                    form1.action = "loginAction.action";
                    form1.submit();
                }
            }
        </script>
        <div class="login-box">
            <div class="login-logo">
                <img src="images/Nsllogo.png"><h1>NSLDAKSH</h1>                
            </div>            
            <div class="login-box-body">
                <p class="login-box-msg">Sign in to start your session</p>
                <form action="#" method="post" onsubmit="javascript: return checkLoginForm();" id="loginForm">
                    <div class="form-group has-feedback">
                        <s:fielderror theme="simple"/>
                    </div>
                    <div class="form-group has-feedback">
                        <s:textfield cssClass="form-control" name="userName"/>
                        <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
                    </div>
                    <div class="form-group has-feedback">
                        <s:password cssClass="form-control" name="password"/>
                        <span class="glyphicon glyphicon-lock form-control-feedback"></span>
                    </div>
                    <div class="row">
                        <div class="col-xs-8">
                            <div class="checkbox icheck">
                                <label>

                                </label>
                            </div>
                        </div>
                        <!-- /.col -->
                        <div class="col-xs-4">
                            <s:submit cssClass="btn btn-primary btn-block btn-flat" value="Sign In" />
                        </div>
                        <!-- /.col -->
                    </div>
                </form>

            </div>
            <!-- /.login-box-body -->
        </div>
        <!-- /.login-box -->
        <script src="Seeds/assets/jquery/dist/jquery.min.js"></script>
        <!-- Bootstrap 3.3.7 -->
        <script src="Seeds/assets/bootstrap/dist/js/bootstrap.min.js"></script>
        <!-- iCheck -->
        <script src="Seeds/plugins/iCheck/icheck.min.js"></script>
        <script src="Seeds/dist/js/seeds.min.js"></script>        
    </body>
</html>
