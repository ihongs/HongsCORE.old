<%@page import="app.hongs.Cnst"%>
<%@page import="app.hongs.Core"%>
<%@page import="app.hongs.CoreLocale"%>
<%@page import="java.io.PrintStream"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page extends="app.hongs.action.Pagelet"%>
<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true" trimDirectiveWhitespaces="true"%>
<%
    // 如果有内部返回, 则不要显示此页
    if (request.getAttribute(Cnst.RESP_ATTR) != null) {
        return;
    }
%>
<!doctype html>
<html>
    <head>
        <title>HongsCORE::Login</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="icon" type="image/x-icon" href="favicon.ico"/>
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
                <h1 style="font-weight: bold;">:(</h1>
                <p  style="font-weight: bold;"><%=CoreLocale.getInstance().translate("core.error.no.found")%>! <a href="<%=request.getContextPath()%>/" style="color: #ace;"><%=CoreLocale.getInstance().translate("core.error.go.index")%></a>.</p>
            </div>
        </div>
        <nav id="footbox" class="navbar">
            <div class="container">
                <blockquote><p>Copyleft &copy; 2015 黄弘. <small class="pull-right">Powered by <a href="https://github.com/ihongs/HongsCORE/" target="_blank">HongsCORE</a>, and <a href="power.html" target="_blank">others</a>.</small></p></blockquote>
            </div>
        </nav>
    </body>
</html>