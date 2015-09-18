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
            String hrel = (String) menu.get("hrel");
//          String icon = (String) menu.get("icon");

            if (href.startsWith( "!" )) {
                continue;
            }
            if (href.startsWith( path + "/#" )) {
                href = href.substring(href.indexOf('#'));
                hrel = Core.BASE_HREF + "/" + hrel;
            } else {
                href = Core.BASE_HREF + "/" + href;
                hrel = "";
            }

            menus.append("<li>" )
                 .append("<a data-href=\"")
                 .append(hrel)
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
    String w = helper.getParameter("w");
    if (w == null || "".equals(w)) {
        w = "manage";
    }
    String m = helper.getParameter("m");
    if (m == null || "".equals(m)) {
        m =  w;
    }
    String u = helper.getParameter("u");
    if (u == null || "".equals(u)) {
        u =  m;
    }

    MenuSet main = MenuSet.getInstance(w);
    MenuSet curr = MenuSet.getInstance(m);
    List<Map> mainMenu = main.getMenuTranslated(1, 1);
    List<Map> currMenu = curr.getMenuTranslated(1, 1);

    String  user = (String ) helper.getSessibute("uname");
    Integer msgc = (Integer) helper.getSessibute( "msgc");
    String  msgs = msgc == null ? null : (msgc > 9 ? "9+" : Integer.toString(msgc));
%>

<div class="navbar-header">
    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#main-collapse">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
    </button>
    <a class="navbar-brand" href="#">
        <span style="color:#800; font-size: 0.90em;" class="glyphicon glyphicon-fire"></span>
        <span style="color:#b00">H</span>
        <span style="color:#b22">o</span>
        <span style="color:#b44">n</span>
        <span style="color:#b66">g</span>
        <span style="color:#b88">s</span>
        <span style="color:#faa">C</span>
        <span style="color:#fbb">O</span>
        <span style="color:#fcc">R</span>
        <span style="color:#fdd">E</span>
    </a>
</div>

<div class="collapse navbar-collapse" id="main-collapse">
    <ul class="nav navbar-nav navbar-left " id="curr-menubar">
        <%=makeMenu(currMenu, u)%>
    </ul>
    <ul class="nav navbar-nav navbar-right" id="main-menubar">
        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                <%if (user != null) {%><%=user%><%} else {%><span class="glyphicon glyphicon-gift"></span><%}%>
                <%if (msgs != null) {%><span class="badge"><%=msgs%></span><%}%>
                <span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu">
                <%=makeMenu(mainMenu, u)%>
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
                var p  =  $(this).attr("data-hreq");
                if (p) {
                    $(this).removeAttr("data-hreq");
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
                $.get(hsFixUri("manage/member/sign/delete.act"), function() {
                    location.reload();
                });
            });

        $(function() {
            // Click the first available menu item
            var a;
            if (location.hash) {
                // #def&x=1&y=2
                var h = location.hash ;
                var p = h.indexOf('&');
                p = p != -1 ? h.substring(p + 1) : "" ;
                a = $("#curr-menubar a[href='"+h+"']");
                a.attr("data-hreq", p);
            } else {
                a = $("#curr-menubar a").first();
            }
            if (a.size() == 0) {
                a = $("#main-menubar ul.dropdown-menu a").first();
            }
            a.click();
        });
    })(jQuery);
</script>
