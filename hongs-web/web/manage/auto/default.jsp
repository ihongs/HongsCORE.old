<%@page import="app.hongs.CoreLocale"%>
<%@page import="app.hongs.action.ActionDriver"%>
<%@page import="app.hongs.action.NaviMap"%>
<%@page import="java.util.Map"%>
<%@page extends="app.hongs.action.Pagelet"%>
<%@page pageEncoding="UTF-8"%>
<%@page contentType="text/html"%>
<%@page trimDirectiveWhitespaces="true"%>
<%
    int i;
    String _module, _entity;
    _module = ActionDriver.getWorkPath(request);
    i = _module.lastIndexOf('/');
    _module = _module.substring(1, i);
    i = _module.lastIndexOf('/');
    _entity = _module.substring(i+ 1);
    _module = _module.substring(0, i);

    CoreLocale lang = CoreLocale.getInstance().clone();
               lang.loadIgnrFNF( _module+"/"+_entity );
    NaviMap    site = NaviMap.getInstance(_module+"/"+_entity);
    Map        menu = site.getMenu(_module +"/"+ _entity +"/");

    String nm = menu == null ? "" : (String) menu.get( "disp");
%>
<!doctype html>
<html>
    <head>
        <title>HongsCORE::<%=nm%></title>
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
        <script type="text/javascript" src="common/src/hongscore-date.js"></script>
        <script type="text/javascript" src="common/src/hongscore-file.js"></script>
        <script type="text/javascript" src="common/src/hongscore-fork.js"></script>
        <script type="text/javascript">
            $(document).on("noMenu", function() {
                $('#main-context').empty().append(
                    '<div class="alert alert-info"><p>'
                  + ':( 糟糕! 这里什么也没有, <a href="manage/">换个地方</a>瞧瞧去?'
                  + '</p></div>'
                );
                setTimeout(function() {
                    location.href = hsFixUri("manage/");
                } , 3000);
            });
        </script>
    </head>
    <body>
        <div id="notebox"></div>
        <nav id="headbox" class="navbar navbar-default navbar-fixed-top" role="navigation">
            <div class="container">
                <div class="row" data-load="manage/head.jsp?n=<%=_module%>/<%=_entity%>"></div>
            </div>
        </nav>
        <div id="bodybox">
            <div class="container" id="main-context">加载中...</div>
        </div>
        <nav id="footbox" class="navbar navbar-default navbar-fixed-bottom">
            <div class="container">
                <div class="row" data-load="manage/foot.jsp?n=<%=_module%>/<%=_entity%>"></div>
            </div>
        </nav>
    </body>
</html>
