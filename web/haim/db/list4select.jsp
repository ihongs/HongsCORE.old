<%@page import="app.hongs.CoreLanguage"%>
<%@page import="app.hongs.db.DB"%>
<%@page import="app.hongs.db.Mview"%>
<%@page import="app.hongs.serv.CommonFilter"%>
<%@page import="java.util.Map"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String _module = (String)request.getAttribute(CommonFilter.MODULE);
    String _entity = (String)request.getAttribute(CommonFilter.ENTITY);

    String root = _module;
    if (_entity.length() != 0) {
        root += "/" + _entity;
    }

    Mview view = new Mview(DB.getInstance(_module).getModel(_entity));
    Map<String, Map<String, String>> flds = view.getFields();
    CoreLanguage lang = CoreLanguage.getInstance();
    lang.load(_module);

    String idKey = view.getIdKey();
    String title = view.getTitle();
    String list = lang.translate("fore.list", title);
    String find = lang.translate("fore.find", title);
    String create = lang.translate("fore.create", title);
    String select = lang.translate("fore.select", title);
%>
<h2><%=list%></h2>
<div id="<%=_module%>-<%=_entity%>-list4select">
    <object class="config" name="hsList" data="">
        <param name="loadUrl" value="('<%=root%>/retrieve.act')"/>
        <param name="openUrls:0" value="['.create','<%=root%>/form.html?','{TABSBOX}']"/>
    </object>
    <div>
        <div class="toolbox col-md-9 btn-group">
            <button type="button" class="ensure btn btn-primary"><%=select%></button>
            <button type="button" class="create btn btn-default"><%=create%></button>
        </div>
        <form class="findbox col-md-3 input-group" action="" method="POST">
            <input type="search" name="wd" class="form-control input-search"/>
            <span class="input-group-btn">
                <button type="submit" class="btn btn-default"><%=find%></button>
            </span>
        </form>
    </div>
    <div class="listbox table-responsive">
        <table class="table table-hover table-striped">
            <thead>
                <tr>
                    <th data-fn="<%=idKey%>[]" data-ft="_check" class="_check">
                        <input type="checkbox" class="checkall" name="<%=idKey%>[]"/>
                    </th>
                    <%for (Map.Entry et : flds.entrySet()) {
                        String key = (String) et.getKey();
                        Map    map = (Map ) et.getValue();
                        if ("hidden".equals(map.get("type"))) {
                            continue;
                        }
                     %>
                    <%if  ("number".equals(map.get("type"))) {%>
                    <th data-fn="<%=key%>" class="sortable text-right"><%=map.get("disp")%></th>
                    <%} else if ("datetime".equals(map.get("type"))) {%>
                    <th data-fn="<%=key%>" data-ft="_datetime" class="sortable datetime"><%=map.get("disp")%></th>
                    <%} else if ("date".equals(map.get("type"))) {%>
                    <th data-fn="<%=key%>" data-ft="_date" class="sortable date"><%=map.get("disp")%></th>
                    <%} else if ("time".equals(map.get("type"))) {%>
                    <th data-fn="<%=key%>" data-ft="_time" class="sortable time"><%=map.get("disp")%></th>
                    <%} else if ("enum".equals(map.get("type"))) {%>
                    <th data-fn="<%=key%>_disp" class="sortable"><%=map.get("disp")%></th>
                    <%} else if (!"primary".equals(map.get("primary")) && !"foreign".equals(map.get("foreign"))) {%>
                    <th data-fn="<%=key%>" class="sortable"><%=map.get("text")%></th>
                    <%} // End If %>
                    <%} // End For%>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
    <div class="pagebox"></div>
</div>
