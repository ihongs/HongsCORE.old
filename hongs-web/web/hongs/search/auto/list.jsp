<%@page import="app.hongs.CoreLanguage"%>
<%@page import="app.hongs.action.ActionDriver"%>
<%@page import="app.hongs.action.MenuSet"%>
<%@page import="java.util.Map"%>
<%@page extends="app.hongs.action.Pagelet"%>
<%@page contentType="text/html; charset=UTF-8"%>
<%
    int i;
    String  _module, _entity;
    _module = ActionDriver.getWorkPath(request);
    i = _module.lastIndexOf('/');
    _module = _module.substring(1, i);
    i = _module.lastIndexOf('/');
    _entity = _module.substring(i+ 1);
    _module = _module.substring(0, i);

    CoreLanguage lang = CoreLanguage.getInstance().clone();
                 lang.loadIgnrFNF/***/(_module);
    MenuSet site = MenuSet.getInstance(_module);

    Map    pagz  = site.getMenu(_module+"/"+_entity+"/list.jsp");
    String title = pagz == null ? "" : (String) pagz.get("disp");
           title = lang.translate(title);
%>
<div class="row">
    <form id="search-form" class="form-inline" style="text-align: center;">
        <select name="sid[]" class="form-control">
            <option value="">--全部--</option>
        </select>
        <input type="search" name="wd" class="form-control"/>
        <button type="submit" class="form-control btn btn-primary">搜索</button>
    </form>
</div>
<div class="row">
    <div id="filter-form" class="col-md-3 side-context">
        <object class="config" name="hsList" data="">
            <param name="loadUrl" value="('search/<%=_entity%>/counts/retrieve.act')"/>
        </object>
        <div class="listbox"></div>
    </div>
    <div id="search-list" class="col-md-9 main-context">
        <object class="config" name="hsList" data="">
            <param name="loadUrl" value="('search/<%=_entity%>/retrieve.act')"/>
            <param name="openUrls#0" value="['.create','<%=_module%>/<%=_entity%>/form.jsp','{TABSBOX}']"/>
            <param name="openUrls#1" value="['.update','<%=_module%>/<%=_entity%>/form.jsp?id={ID}','{TABSBOX}']"/>
            <param name="sendUrls#0" value="['.delete','<%=_module%>/<%=_entity%>/delete.act','<%=lang.translate("fore.deletre.confirm", title)%>']"/>
        </object>
        <div class="listbox"></div>
        <div class="pagebox"></div>
    </div>
</div>
<script type="text/javascript">
    $("#search-form").submit(function() {
        var form = $("#filter-form").data("HsList");
        var list = $("#search-list").data("HsList");
        var data = hsSerialObj($(this));
        form.load(null, data);
        list.load(null, data);
        return false;
    });
</script>
