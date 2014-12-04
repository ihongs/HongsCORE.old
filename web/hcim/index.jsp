<%@page import="app.hongs.serv.RiggerFilter"%>
<%@ page contentType="text/html;charset=utf-8"%>
<!--
Hong's Common User Module
用户模块
//-->
<!doctype html>
<html>
    <head>
        <title>脚手架</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="icon" href="<%=request.getContextPath()%>/favicon.ico" type="image/x-icon"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/common/css/bootstrap.min.css"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/common/css/hongscore.min.css"/>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/jquery.min.js"></script>
        <!--[if glt IE8.0]>
        <script type="text/javascript" src="../compon/respond/respond.min.js"></script>
        <![endif]-->
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/bootstrap.min.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/hongscore.min.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/conf/default.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/lang/default.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/auth/default.js"></script>
    </head>
    <body>
        <div id="notebox"></div>
        <nav id="headbox" class="navbar navbar-default navbar-fixed-top" role="navigation">
            <div class="container">
                <div class="row" data-load="<%=request.getContextPath()%>/common/pages/head.jsp?c=<%=request.getAttribute(RiggerFilter.CONFIG)%>"></div>
            </div>
        </nav>
        <div id="bodybox">
            <div class="container tabsbox" id="main-context"></div>
        </div>
        <nav id="footbox" class="navbar navbar-default navbar-fixed-bottom">
            <div class="container">
                <div class="row" data-load="<%=request.getContextPath()%>/common/pages/foot.jsp"></div>
            </div>
        </nav>
    </body>
</html>
