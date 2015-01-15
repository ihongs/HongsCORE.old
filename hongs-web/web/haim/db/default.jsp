<%@page import="java.util.Map"%>
<%@page import="app.hongs.Core"%>
<%@page import="app.hongs.HongsException"%>
<%@page import="app.hongs.action.ActionCasual"%>
<%@page import="app.hongs.action.ActionHelper"%>
<%@page import="app.hongs.action.StructConfig"%>
<%@page contentType="text/html;charset=utf-8"%>
<%!
    String getTitle(String name) throws HongsException {
        StructConfig conf = StructConfig.getInstance(name);
        Map unis = conf.getForm( "__MENU__" ) != null
                 ? conf.getUnitsTranslated("__MENU__")
                 : conf.getFormsTranslated();
        String hrel, href = "", titl = "";
        for (Object o : unis.entrySet()) {
            Map.Entry e = (Map.Entry) o ;
            Map unit = (Map)e.getValue();
                hrel = unit.get("_href").toString();
            if (Core.ACTION_NAME.get().startsWith(hrel)
            &&  href.length( )  <  hrel.length( ) ) {
                titl = unit.get("_disp").toString();
                href = hrel;
            }
        }
        return titl;
    }
%>
<%
    String _module = (String)request.getAttribute(ActionCasual.MODULE);
    String  title  = getTitle( _module );
    if ( "".equals(title)) {
            title  = getTitle("default");
    }
    if (!"".equals(title)) {
            title = " - " + title;
    }
%>
<!--
Hong's Common User Module
用户模块
//-->
<!doctype html>
<html>
    <head>
        <title>HongsCORE<%=title%></title>
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
