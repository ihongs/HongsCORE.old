<%@ page contentType="text/html;charset=utf-8"%>
<!--
Hong's Common User Module
用户模块
//-->
<!doctype html>
<html>
    <head>
        <title>Rich Internet Application</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="icon" href="../../favicon.ico" type="image/x-icon"/>
        <link rel="stylesheet" type="text/css" href="../css/bootstrap.min.css"/>
        <link rel="stylesheet" type="text/css" href="../css/hongscore.min.css"/>
        <script type="text/javascript" src="../jquery.min.js"></script>
        <!--[if glt IE8.0]>
        <script type="text/javascript" src="../compon/respond/respond.min.js"></script>
        <![endif]-->
        <script type="text/javascript" src="../bootstrap.min.js"></script>
        <script type="text/javascript" src="../hongscore.min.js"></script>
        <script type="text/javascript" src="../conf/default.js"></script>
        <script type="text/javascript" src="../lang/default.js"></script>
    </head>
    <body>
        <div id="notebox"></div>
        <nav id="headbox" class="navbar navbar-default navbar-fixed-top" role="navigation">
            <div class="container">
                <div class="row" data-load="./head.jsp?m=<%=request.getParameter("m")%>"></div>
            </div>
        </nav>
        <div id="bodybox">
            <div class="container tabsbox" id="main-context"></div>
        </div>
        <nav id="footbox" class="navbar navbar-default navbar-fixed-bottom">
            <div class="container">
                <div class="row" data-load="./foot.jsp?m=<%=request.getParameter("m")%>"></div>
            </div>
        </nav>
    </body>
</html>
