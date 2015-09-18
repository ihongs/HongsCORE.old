<%@page import="java.util.Map"%>
<%@page import="app.hongs.CoreLocale"%>
<%@page import="app.hongs.action.ActionDriver"%>
<%@page import="app.hongs.action.MenuSet"%>
<%@page extends="app.hongs.action.Pagelet"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    int i;
    String _module, m, n;
    _module = ActionDriver.getWorkPath(request);
    try {
        i = _module.lastIndexOf('/');
        _module = _module.substring(1, i);
        i = _module.lastIndexOf('/');
        m = _module.substring(0 , i);
        n = _module.substring(1 + i);
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
        <link rel="icon" href="<%=request.getContextPath()%>/favicon.ico" type="image/x-icon"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/common/css/bootstrap.min.css"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/compon/bootstrap-datetimepicker/css/bootstrap-datetimepicker.css"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/common/src/hongscore.css"/>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/jquery.min.js"></script>
        <!--[if glt IE8.0]>
        <script type="text/javascript" src="../compon/respond/respond.min.js"></script>
        <![endif]-->
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/bootstrap.min.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/src/hongscore.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/src/hongscore-list.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/src/hongscore-form.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/src/hongscore_pick.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/conf/default.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/common/lang/default.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/compon/bootstrap-datetimepicker/bootstrap-datetimepicker.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/compon/bootstrap-datetimepicker/bootstrap-datetimetoggle.js"></script>
    </head>
    <body>
        <div id="notebox"></div>
        <nav id="headbox" class="navbar navbar-default navbar-fixed-top" role="navigation">
            <div class="container">
                <div class="row" data-load="<%=request.getContextPath()%>/manage/head.jsp?m=<%=m%>&n=<%=n%>"></div>
            </div>
        </nav>
        <div id="bodybox" class="container">
            <div id="main-context">
                <ul class="nav nav-tabs tabs">
                    <li class="active"><a href="javascript:;">加载中...</a></li>
                </ul>
                <div class="panes">
                    <div class="openbox" data-load="<%=request.getContextPath()%>/<%=_module%>/list.html"></div>
                </div>
            </div>
        </div>
        <nav id="footbox" class="navbar navbar-default navbar-fixed-bottom">
            <div class="container">
                <div class="row" data-load="<%=request.getContextPath()%>/manage/foot.jsp?m=<%=m%>&n=<%=n%>"></div>
            </div>
        </nav>
    </body>
</html>
