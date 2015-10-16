<%@page import="java.util.Map"%>
<%@page import="app.hongs.CoreLocale"%>
<%@page import="app.hongs.action.ActionDriver"%>
<%@page import="app.hongs.action.MenuSet"%>
<%@page extends="app.hongs.action.Pagelet"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    int i;
    String _module, n, u;
    _module = ActionDriver.getWorkPath(request);
    try {
        i = _module.lastIndexOf('/');
        _module = _module.substring(1, i);
        i = _module.lastIndexOf('/');
        n = _module.substring(0 , i);
        u = _module;
    } catch (StringIndexOutOfBoundsException e) {
        throw new ServletException("URL Error");
    }

    CoreLocale  lang = CoreLocale.getInstance().clone();
                              lang.loadIgnrFNF(_module);
    MenuSet     site =     MenuSet.getInstance(_module);
    Map         cell =        site.getMenu    (_module + "/");
    String     title = lang.translate(cell.get("disp").toString());
%>
<!--
Hong's Auto Info Manage
自动信息管理
//-->
<!doctype html>
<html>
    <head>
        <title>HongsCORE::<%=title%></title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <base href="<%=request.getContextPath()%>/">
        <link rel="icon" href="favicon.ico" type="image/x-icon"/>
        <link rel="stylesheet" type="text/css" href="common/css/bootstrap.min.css"/>
        <link rel="stylesheet" type="text/css" href="compon/bootstrap-datetimepicker/css/datetimepicker.css"/>
        <link rel="stylesheet" type="text/css" href="compon/bootstrap-fileinput/css/fileinput.css"/>
        <link rel="stylesheet" type="text/css" href="common/css/hongscore.min.css"/>
        <script type="text/javascript" src="common/jquery.min.js"></script>
        <!--[if glt IE8.0]>
        <script type="text/javascript" src="compon/respond/respond.min.js"></script>
        <![endif]-->
        <script type="text/javascript" src="common/bootstrap.min.js"></script>
        <script type="text/javascript" src="compon/bootstrap-datetimepicker/datetimepicker.js"></script>
        <script type="text/javascript" src="compon/bootstrap-fileinput/fileinput.js"></script>
        <script type="text/javascript" src="common/conf/default.js"></script>
        <script type="text/javascript" src="common/lang/default.js"></script>
        <script type="text/javascript" src="common/src/hongscore.js"></script>
        <script type="text/javascript" src="common/src/hongscore-list.js"></script>
        <script type="text/javascript" src="common/src/hongscore-form.js"></script>
        <script type="text/javascript" src="common/src/hongscore-pick.js"></script>
        <script type="text/javascript" src="common/src/hongscore-date.js"></script>
        <script type="text/javascript" src="common/src/hongscore-file.js"></script>
    </head>
    <body>
        <div id="notebox"></div>
        <nav id="headbox" class="navbar navbar-default navbar-fixed-top" role="navigation">
            <div class="container">
                <div class="row" data-load="manage/head.jsp?n=<%=n%>&u=<%=u%>"></div>
            </div>
        </nav>
        <div id="bodybox" class="container">
            <div id="main-context">
                <ul class="nav nav-tabs tabs">
                    <li class="active"><a href="javascript:;">加载中...</a></li>
                </ul>
                <div class="panes">
                    <div class="openbox" data-load="<%=_module%>/list.html"></div>
                </div>
            </div>
        </div>
        <nav id="footbox" class="navbar navbar-default navbar-fixed-bottom">
            <div class="container">
                <div class="row" data-load="manage/foot.jsp?m=<%=n%>&u=<%=u%>"></div>
            </div>
        </nav>
    </body>
</html>
