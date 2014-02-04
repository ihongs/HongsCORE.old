<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@page import="app.common.action.Menu"%>
<%@page import="app.hongs.Core"%>
<%@page import="app.hongs.action.ActionHelper"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%!
StringBuilder makeMenu(List<Map> menus, int i) {
  StringBuilder menuz = new StringBuilder();
  if (menus.isEmpty()) return menuz;
  
  for (Map menu : menus) {
    List<Map> subMenus = (List)menu.get("menus");app.hongs.util.JSON.print(menu);
    String claz = !subMenus.isEmpty() ? "dropdown-toggle" : "";
    String cart = !subMenus.isEmpty() ? "<b class=\"caret\"></b>" : "";
    String name =  (String)menu.get("name");
    String href = ((String)menu.get("uri" )).substring(1);
    String hash = "#"+href.substring(href.indexOf("/")+1);
    if (name.indexOf("${opt}") != -1) {
        continue; // 操作类的内页不要
    }
    if (i == -1) {
        hash = Core.BASE_HREF + "/" + href; // Logo Menu
    }
    else if (hash.endsWith(".jsp" )) {
        hash = hash.substring(0, hash.length()-4);
    }
    else if (hash.endsWith(".html")) {
        hash = hash.substring(0, hash.length()-5);
    }
    else if (hash.startsWith("hongs/util/Jump/To.act")) {
        hash = hash.substring(19).replaceAll("=",".");
    }
    menuz.append("<li>")
         .append("<a class=\"")
         .append(claz)
         .append("\" href=\"")
         .append(hash)
         .append("\" data-href=\"")
         .append(href)
         .append("\">")
         .append(name)
         .append(cart)
         .append("</a>");
    StringBuilder subMenuz = makeMenu(subMenus, i+1);
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
  ActionHelper helper = (ActionHelper)Core.getInstance(ActionHelper.class);
  String name  = helper.getParameter("c");
  String level = helper.getParameter("l");
  String depth = helper.getParameter("d");
  
  List<Map> menus = Menu.getList(name, level, depth);
  List<Map> logom = Menu.getList("default", "1","1");
%>

<nav id="main-menubar" class="navbar navbar-default" role="navigation">
  <!-- Brand and toggle get grouped for better mobile display -->
  <div class="navbar-header">
    <a class="navbar-brand dropdown-toggle" href="#" data-toggle="dropdown">HongsCORE <b class="caret"></b></a>
    <ul class="dropdown-menu">
      <%=makeMenu(logom, -1).toString()%>
    </ul>
  </div>
  <!-- Collect the nav links, forms, and other content for toggling -->
  <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
    <ul class="nav navbar-nav menu">
      <%=makeMenu(menus, 0).toString()%>
    </ul>
    <ul class="nav navbar-nav navbar-right">
      <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown">hongs@www.com <b class="caret"></b></a>
        <ul class="dropdown-menu">
          <li><a href="#">Action</a></li>
          <li><a href="#">Another action</a></li>
          <li><a href="#">Something else here</a></li>
          <li class="divider"></li>
          <li><a href="#">Separated link</a></li>
        </ul>
      </li>
    </ul>
  </div><!-- /.navbar-collapse -->
</nav>
<script type="text/javascript">
    (function($) {
        $("#main-menubar .menu>li>a")
        .filter(function() {
            return $(this).attr("href").substring(0, 1) == "#";
        })
        .click(function() {
            $("#main-context").load($(this).attr("data-href"));
            $(this).closest("li")
                   .addClass("active")
                   .siblings()
                   .removeClass("active");
        });

        $(function() {
            if (location.hash) {
                $("#main-menubar .menu a[href='"+location.hash+"']").click();
            }
            else {
                $("#main-menubar .menu a").first().click();
            }
        });
    })(jQuery);
</script>
