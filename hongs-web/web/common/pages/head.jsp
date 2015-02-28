<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="app.hongs.Core"%>
<%@page import="app.hongs.CoreLanguage"%>
<%@page import="app.hongs.action.ActionHelper"%>
<%@page import="app.hongs.action.SiteMap"%>
<%@page extends="app.hongs.action.Pagelet"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%!
    StringBuilder makeMenu(List<Map> list, String path) {
        StringBuilder menus = new StringBuilder();
        for(Map menu : list) {
            if (! (Boolean) menu.get("auth")) {
                continue;
            }
            
            String disp = (String) menu.get("disp");
            String href = (String) menu.get("name");
            String data = (String) menu.get("href");

            if (!"".equals(data)) {
                if (data.startsWith(path+"/") && data.contains("#")) {
                    String hash = data.substring(data.indexOf ('#'));
                    data = Core.BASE_HREF + "/" + href;
                    href = hash;
                } else {
                    href = Core.BASE_HREF + "/" + data;
                    data =  "" ;
                }
            } else {
                    href = Core.BASE_HREF + "/" + href;
            }

            menus.append("<li>" )
                 .append("<a data-href=\"")
                 .append(data)
                 .append("\" href=\"")
                 .append(href)
                 .append("\">"  )
                 .append(disp)
                 .append("</a>" )
                 .append("</li>");
        }
        return menus;
    }
%>
<%
    ActionHelper helper = (ActionHelper) Core.getInstance(ActionHelper.class);
    String name= helper.getParameter("m");
    if (name == null || "".equals(name) ) {
        name = "default";
    }

    SiteMap main = SiteMap.getInstance();
    SiteMap curr = SiteMap.getInstance(name);
    List<Map> mainMenu = main.getMenuTranslated(1, 1);
    List<Map> currMenu = curr.getMenuTranslated(1, 1);
    
    String  user = (String ) helper.getSessvalue("name");
    Integer msgc = (Integer) helper.getSessvalue("msgc");
    String  msgs = msgc == null ? null : (msgc > 9 ? "9+" : Integer.toString(msgc));
%>

<div class="navbar-header">
    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#main-collapse">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
    </button>
    <a class="navbar-brand" href="#" style="font-weight:bold;">
        <span style="color:#f08" class="glyphicon glyphicon-fire"></span>
        <span style="color:#f00">H</span>
        <span style="color:#f22">o</span>
        <span style="color:#f44">n</span>
        <span style="color:#f66">g</span>
        <span style="color:#f88">s</span>
        <span style="color:#faa">C</span>
        <span style="color:#fbb">O</span>
        <span style="color:#fcc">R</span>
        <span style="color:#fdd">E</span>
    </a>
</div>

<div class="collapse navbar-collapse" id="main-collapse">
    <ul class="nav navbar-nav navbar-left " id="curr-menubar">
        <%=makeMenu(currMenu, name)%>
    </ul>
    <ul class="nav navbar-nav navbar-right" id="main-menubar">
        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                <%if (user != null) {%><%=user%><%} else {%><span class="glyphicon glyphicon-gift"></span><%}%>
                <%if (msgs != null) {%><span class="badge"><%=msgs%></span><%}%>
                <span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu">
                <%=makeMenu(mainMenu, name)%>
                <%if (user != null) {%>
                <li class="divider"></li>
                <li><a href="javascript:;" id="sign-out">注销</a></li>
                <%} // End If%>
            </ul>
        </li>
    </ul>
</div><!-- /.navbar-collapse -->

<script type="text/javascript">
    (function($) {
        $("#curr-menubar>li>a")
            .filter(function() {
                return !! $(this).attr("data-href");
            })
            .click(function() {
                $("#main-context").hsLoad($(this).attr("data-href"));
                $(this).closest("li").addClass("active")
                       .siblings().removeClass("active");

            });
        $("#main-menubar>li>a")
            .click(function() {
                var that = $(this);
                setTimeout(function() {
                    that.parent( ).removeClass("active");
                    that.blur  ( );
                }, 100);
            });
        $("#sign-out")
            .click(function() {
                $.get(hsFixUri("hcum/sign/out.act"), function() {
                    location.reload();
                });
            });

        $(function() {
            if ( location.hash  ) {
                $("#curr-menubar>li>a[href='"+location.hash+"']").click();
            } else {
                $("#curr-menubar>li>a").first().click();
            }
            setTimeout(function() {
                $("#main-menubar>li>a").first().click();
            }, 1000);
        });
    })(jQuery);
</script>
