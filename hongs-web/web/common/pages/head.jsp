<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="app.hongs.Core"%>
<%@page import="app.hongs.CoreLanguage"%>
<%@page import="app.hongs.action.ActionHelper"%>
<%@page import="app.hongs.action.SiteMap"%>
<%!
    StringBuilder makeMenu(List<Map> list, String rootName, boolean realHref) {
        StringBuilder menus = new StringBuilder();
        for(Map menu : list) {
            String href = (String) menu.get("href");
            String disp = (String) menu.get("disp");
            String name = href.replaceAll("\\.\\w+$", "");
            
            if (name.startsWith(rootName)) {
                name = name.substring(rootName.length() + 1);
            }

            if (realHref) {
                String temp = href;
                href =  Core.BASE_HREF + "/" + href;
                name = temp;
            } else {
                String temp = href;
                href = "#" + name;
                name = temp;
            }
            
            menus.append("<li>" )
                 .append("<a data-href=\"")
                 .append(name)
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
    List<Map> mainMenu = main.getMenu (1, 1);
    List<Map> currMenu = curr.getMenu (1, 1);
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
        <%=makeMenu(currMenu, name, false)%>
    </ul>
    <ul class="nav navbar-nav navbar-right" id="main-menubar">
        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">ihongs@live.cn <span class="badge">9+</span> <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
                <%=makeMenu(mainMenu, name, true)%>
                <li class="divider"></li>
            </ul>
        </li>
    </ul>
</div><!-- /.navbar-collapse -->

<script type="text/javascript">
    (function($) {
        $("#curr-menubar>li>a")
            .filter(function() {
                return $(this).attr( "href" ).substring(0, 1) == "#";
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
