<%@page import="app.hongs.Core"%>
<%@page import="java.io.PrintStream"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true"%>
<!doctype html>
<html>
    <head>
        <title>HongsCORE::Login</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="icon" type="image/x-icon" href="<%=request.getContextPath()%>/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/common/css/bootstrap.min.css"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/common/css/hongscore.min.css"/>
        <!--[if glt IE8.0]>
        <script type="text/javascript" src="<%=request.getContextPath()%>/compon/respond/respond.min.js"></script>
        <![endif]-->
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/jquery.min.js"></script>
    </head>
    <body>
        <div class="jumbotron" style="margin-top: 2em; background-color: #0071AD; color: #fff;">
            <div class="container">
                <h1>:(</h1>
                <p>&nbsp;</p>
                <pre style="border: 0; color: #fff; background-color: transparent;">
<%=exception.getLocalizedMessage().replace("<", "&lt;").replace(">", "&gt;")%>
                </pre>
                <% if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG)) { %>
                <pre style="border: 0; color: #ddd; background-color: transparent;">
<%
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    exception.printStackTrace(new PrintStream( o ));
    String x = new String(o.toByteArray(), "utf-8");
    x = x.replace("<", "&lt;").replace(">", "&gt;");
    out.println(x.trim());
%>
                </pre>
                <% } // End If %>
            </div>
        </div>
        <nav id="footbox" class="navbar">
            <div class="container">
                <blockquote><p>Copyleft &copy; 2015 黄弘. <small class="pull-right">Powered by <a href="https://github.com/ihongs/HongsCORE/" target="_blank">HongsCORE</a>, and <a href="power.html" target="_blank">others</a>.</small></p></blockquote>
            </div>
        </nav>
    </body>
</html>