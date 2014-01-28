<%@page import="app.hongs.util.action.Menu"%>
<%!
StringBuilder makeMenus(List<Map> menus, int i) {
  StringBuilder sb = new StringBuilder();
  if (menus.isEmpty()) return sb;
  
  sb.append("<ul class=\"")
    .append(i > 0 ? "dropdown-menu" : "nav navbar-nav menu")
    .append("\">");
  for (Map menu : menus) {
    List<Map> subMenus = (List)menu.get("menus");
    String claz = !subMenus.isEmpty() ? "dropdown-toggle" : "";
    String cart = !subMenus.isEmpty() ? "<b class=\"caret\"></b>" : "";
    String name = (String)menu.get("name");
    String href = (String)menu.get("url" );
    hash = href.substring(href.indexOf("/")+1 , href.lastIndexOf("."));
    sb.append("<li>")
      .append("<a class=\"")
      .append(claz)
      .append("\" href=\"#")
      .append(hash)
      .append("\" data-href=\"")
      .append(href)
      .append("\">")
      .append(name)
      .append(cart)
      .append("</a>")
      .append(makeMenu(subMenus), i+1)
      .append("</li>");
  }
  sb.append("</ul>");
  
  return sb;
}
%>
<%
  String name  = helper.getParameter("n");
  String level = helper.getParameter("l");
  String depth = helper.getParameter("d");
  List<Map> menus = Menu.getMenus(name, level, depth);
%>

<nav id="main-menubar" class="navbar navbar-default" role="navigation">
  <!-- Brand and toggle get grouped for better mobile display -->
  <div class="navbar-header">
    <a class="navbar-brand" href="#">HongsCORE</a>
  </div>
  <!-- Collect the nav links, forms, and other content for toggling -->
  <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
    <%=makeMenus(menus).toString()%>
    <ul class="nav navbar-nav menu">
      <li><a href="#module/list" data-href="hcim/module/list.html">模块</a></li>
      <li><a href="#model/list" data-href="hcim/model/list.html">模型</a></li>
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
            return  $(this).attr("hash") !== "#";
        })
        .click(function() {console.log($("#main-context"))
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
