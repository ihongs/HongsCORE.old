<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="app.hongs.Core"%>
<%@page import="app.hongs.CoreLanguage"%>
<%@page import="app.hongs.action.AuthConfig"%>
<%@page import="app.hongs.action.ActionHelper"%>
<%!
    StringBuilder makeMenu(List<Map> menus, boolean realHref) {
        app.hongs.util.Data.dumps(menus);
        StringBuilder menuz = new StringBuilder();
        if (menus.isEmpty()) {
            return menuz;
        }

        for (Map menu : menus) {
            List<Map> subMenus = (List) menu.get("list");
            String claz = !subMenus.isEmpty() ? "dropdown-toggle" : "";
            String cart = !subMenus.isEmpty() ? "<b class=\"caret\"></b>" : "";
            String name = (String) menu.get("name");
            String href = (String) menu.get("href");
            String hash = "#" + href.substring(href.indexOf("/") + 1);
            if (name.indexOf("${opr}") != -1) {
                continue; // 操作类的内页不要
            }
            if (realHref) {
                hash = Core.BASE_HREF + "/" + href; // User Menu
            } else if (hash.endsWith(".jsp" )) {
                hash = hash.substring(0, hash.length() - 4);
            } else if (hash.endsWith(".html")) {
                hash = hash.substring(0, hash.length() - 5);
            } else if (hash.startsWith("common/goto.act")) {
                hash = hash.substring(19).replaceAll("=", ".");
            }
            menuz.append("<li>")
                 .append("<a class=\"")
                 .append(claz)
                 .append("\" href=\"" )
                 .append(hash)
                 .append("\" data-href=\"")
                 .append(href)
                 .append("\">" )
                 .append(name)
                 .append(cart)
                 .append("</a>");
            StringBuilder subMenuz = makeMenu(subMenus, realHref);
            if (subMenuz.length() > 0) {
                menuz.append("<ul class=\"dropdown-menu\">")
                        .append(subMenuz)
                        .append("</ul>");
            }
            menuz.append("</li>");
        }

        return menuz;
    }
%>
<%
    ActionHelper helper = (ActionHelper) Core.getInstance(ActionHelper.class);
    String name  = helper.getParameter("c");
    String level = helper.getParameter("l");
    String depth = helper.getParameter("d");

    if (name  == null || name .length() == 0) {
        name  = "default";
    }

    int l , d;
    if (level == null || level.length() == 0) {
        l = 1;
    } else {
        l = Integer.parseInt(level);
    }
    if (depth == null || depth.length() == 0) {
        d = 1;
    } else {
        d = Integer.parseInt(depth);
    }

    AuthConfig userAuth = AuthConfig.getInstance();
    AuthConfig currAuth = AuthConfig.getInstance(name);

    List<Map> userMenu = userAuth.getMenu(1, 1);
    List<Map> currMenu = currAuth.getMenu(l, d);
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
    <ul class="nav navbar-nav navbar-left " id="main-menubar">
        <%=makeMenu(currMenu, false)%>
    </ul>
    <ul class="nav navbar-nav navbar-right" id="user-menubar">
        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">ihongs@live.cn <span class="badge">9+</span> <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
                <%=makeMenu(userMenu, true)%>
                <li class="divider"></li>
                <li><a href="#">消息中心</a></li>
                <li><a href="#">修改密码</a></li>
            </ul>
        </li>
    </ul>
</div><!-- /.navbar-collapse -->

<script type="text/javascript">
    (function($) {
        $("#main-menubar>li>a")
            .filter(function() {
                return $(this).attr( "href" ).substring(0, 1) == "#";
            })
            .click(function() {
                $("#main-context").hsLoad($(this).attr("data-href"));
                $(this).closest("li").addClass("active")
                       .siblings().removeClass("active");
            });
        $("#user-menubar>li>a")
            .click(function() {
                var that = $(this);
                setTimeout(function() {
                    that.parent( ).removeClass("active");
                    that.blur  ( );
                }, 100);
            });

        $(function() {
            if (location.hash) {
                $("#main-menubar>li>a[href='" + location.hash + "']").click(  );
            }
            else {
                $("#main-menubar>li>a").first().click();
            }
            setTimeout(function() {
                $("#user-menubar>li>a").first().click();
            }, 500);
        });
    })(jQuery);
</script>
