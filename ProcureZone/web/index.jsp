<%-- 
    Document   : index
    Created on : Nov 26, 2012, 11:38:19 AM
    Author     : Ravi Teja
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
String ua = request.getHeader( "User-Agent" );
boolean isFirefox = ( ua != null && ua.indexOf( "Firefox/" ) != -1 );
boolean isMSIE = ( ua != null && ua.indexOf( "MSIE" ) != -1 );
response.setHeader( "Vary", "User-Agent" );
%>

<% if( isMSIE ){ %>
<p align="center"> <img src="browser.jpg" align="center" height="70%" width="70%"></p>
<% } else { 

    response.sendRedirect("login.action");
 } %>
