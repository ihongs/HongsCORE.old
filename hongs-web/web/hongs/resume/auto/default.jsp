<%@page contentType="text/html;charset=utf-8"%>
<%@page import="app.hongs.Core"%>
<%@page import="app.hongs.HongsException"%>
<%@page import="app.hongs.action.ActionHelper"%>
<%@page import="app.hongs.action.ActionDriver"%>
<%@page import="app.hongs.db.DB"%>
<%@page import="app.hongs.db.Mview"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%
    String  _module, _entity; int i;
    _module = ActionDriver.getWorkPath(request);
    i = _module.lastIndexOf('/');
    _module = _module.substring(1, i);
    i = _module.lastIndexOf('/');
    _entity = _module.substring(i+ 1);
    _module = _module.substring(0, i);

    String title = "";
%>
<!--
Hong's Common User Module
用户模块
//-->
<!doctype html>
<html>
    <head>
        <title>HongsCORE::<%=title%></title>
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
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/src/hongscore.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/src/hongscore-list.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/src/hongscore-form.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/src/hongscore-pick.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/conf/default.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/lang/default.js"></script>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/common/src/bootstrap-datetimepicker.css"/>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/src/bootstrap-datetimepicker.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/src/bootstrap-datetimetoggle.js"></script>
    </head>
    <body>
        <div id="notebox"></div>
        <nav id="headbox" class="navbar navbar-default navbar-fixed-top" role="navigation">
            <div class="container">
                <div class="row" data-load="<%=request.getContextPath()%>/common/pages/head.jsp?m=<%=_module%>"></div>
            </div>
        </nav>
        <div id="bodybox">
            <div class="container tabsbox" id="main-context"></div>
        </div>
        <nav id="footbox" class="navbar navbar-default navbar-fixed-bottom">
            <div class="container">
                <div class="row" data-load="<%=request.getContextPath()%>/common/pages/foot.jsp?m=<%=_module%>"></div>
            </div>
        </nav>
    </body>
</html>
