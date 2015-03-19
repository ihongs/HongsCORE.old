<%@page import="app.hongs.util.Synt"%>
<%@page import="app.hongs.Core"%>
<%@page import="app.hongs.CoreLocale"%>
<%@page import="app.hongs.action.ActionHelper"%>
<%@page import="app.hongs.action.MenuSet"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page extends="app.hongs.action.Pagelet"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%!
    StringBuilder makeMenu(List<Map> list, String path) {
        StringBuilder menus = new StringBuilder();
        for(Map menu : list) {
            String disp = (String) menu.get("disp");
            String href = (String) menu.get("href");
            String page = (String) menu.get("page");
            String data = (String) menu.get("data");

            if (! "".equals(data)) {
                page += "?"+data;
            }

            if (href.startsWith(path+"/#")) {
                href = href.substring(href.indexOf('#'));
                page = Core.BASE_HREF + "/" + page;
            } else {
                href = Core.BASE_HREF + "/" + href;
                page = "";
            }

            menus.append("<li>" )
                 .append("<a data-href=\"")
                 .append(page)
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
    String path= helper.getParameter("n");
    if (path == null || "".equals(path) ) {
        path =  name ;
    }

    MenuSet main = MenuSet.getInstance();
    MenuSet curr = MenuSet.getInstance(name);
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
        <%=makeMenu(currMenu, path)%>
    </ul>
    <ul class="nav navbar-nav navbar-right" id="main-menubar">
        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                <%if (user != null) {%><%=user%><%} else {%><span class="glyphicon glyphicon-gift"></span><%}%>
                <%if (msgs != null) {%><span class="badge"><%=msgs%></span><%}%>
                <span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu">
                <%=makeMenu(mainMenu, path)%>
                <%if (user != null) {%>
                <li class="divider"></li>
                <li><a href="javascript:;" id="sign-out"><%=CoreLocale.getInstance().translate("fore.logout")%></a></li>
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
                var h  =  $(this).attr("data-href");
                var p  =  $(this).attr("data-hrel");
                if (p) {
                    $(this).removeAttr("data-hrel");
                    if (h.index('?') != -1 ) {
                        h += '?' + p;
                    } else {
                        h += '&' + p;
                    }
                }
                $("#main-context").hsLoad(h);
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
                $.get(hsFixUri("hongs/member/sign/out.act"), function() {
                    location.reload();
                });
            });

        $(function() {
            // Click the first available menu item
            var a;
            if (location.hash) {
                // #abc&x=1&y=2
                var h = location.hash ;
                var p = h.indexOf('&');
                p = p != -1 ? h.substring(p + 1) : "" ;
                a = $("#curr-menubar a[href='"+h+"']");
                a.attr("data-hrel", p);
            } else {
                a = $("#curr-menubar a").first(/****/);
            }
            if (a.size() == 0) {
                a = $("#main-menubar ul.dropdown-menu a").first( );
            }
            a.click();

            // Auto hide the main menu
            setTimeout(function() {
                $("#main-menubar>li>a").first().click();
            }, 1000);
        });
    })(jQuery);
</script>
