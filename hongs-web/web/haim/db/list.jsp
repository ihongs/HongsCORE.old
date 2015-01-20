<%@page import="app.hongs.CoreLanguage"%>
<%@page import="app.hongs.action.ActionWarder"%>
<%@page import="app.hongs.db.DB"%>
<%@page import="app.hongs.db.Mview"%>
<%@page import="java.util.Map"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String  _module, _entity; int i;
    _module = ActionWarder.getWorkPath(request);
    i = _module.lastIndexOf('/');
    _module = _module.substring(1, i);
    i = _module.lastIndexOf('/');
    _entity = _module.substring(i+ 1);
    _module = _module.substring(0, i);
    
    String _action = (String)request.getAttribute("list.action");

    String root = _module;
    if (_entity.length() != 0) {
        root += "/" + _entity;
    }
    if (_action == null) {
        _action = "list" ;
    }

    Mview view = new Mview(DB.getInstance(_module).getModel(_entity));
    Map<String, Map<String, String>> flds = view.getFields();
    CoreLanguage lang = CoreLanguage.getInstance( ).clone( );
    lang.loadIgnrFNF(_module);

    String idKey = view.getIdKey();
    String title = view.getTitle();
%>
<h2><%=lang.translate("fore."+_action+".title", title)%></h2>
<div id="<%=_module%>-<%=_entity%>-<%=_action%>">
    <object class="config" name="hsList" data="">
        <param name="loadUrl" value="('<%=root%>/retrieve.act?jd=2')"/>
        <param name="openUrls:0" value="['.create','<%=root%>/form.html','{TABSBOX}']"/>
        <param name="openUrls:1" value="['.update','<%=root%>/form4update.html?<%=idKey%>={ID}','{TABSBOX}']"/>
        <param name="sendUrls:0" value="['.delete','<%=root%>/delete.act','<%=lang.translate("fore.deletre.confirm", title)%>']"/>
        <param name="_fill__pick" value="(hsListFillPick)"/>
    </object>
    <div>
        <div class="toolbox col-md-9 btn-group">
            <%if ( "select".equals(_action)) {%>
            <button type="button" class="create btn btn-primary"><%=lang.translate("fore.select", title)%></button>
            <%} // End If %>
            <button type="button" class="create btn btn-default"><%=lang.translate("fore.create", title)%></button>
            <%if (!"select".equals(_action)) {%>
            <button type="button" class="update for-select btn btn-default"><%=lang.translate("fore.update", title)%></button>
            <button type="button" class="delete for-checks btn btn-danger" ><%=lang.translate("fore.delete", title)%></button>
            <%} // End If %>
        </div>
        <form class="findbox col-md-3 input-group" action="" method="POST">
            <input type="search" name="wd" class="form-control input-search"/>
            <span class="input-group-btn">
                <button type="submit" class="btn btn-default"><%=lang.translate("fore.search", title)%></button>
            </span>
        </form>
    </div>
    <div class="listbox table-responsive">
        <table class="table table-hover table-striped">
            <thead>
                <tr>
                    <th data-fn="id[]" data-ft="<%if ("select".equals(_action)) {%>_pick<%} else {%>_check<%}%>" class="_check">
                        <input type="checkbox" class="checkall" name="id[]"/>
                    </th>
                    <%for(Map.Entry et : flds.entrySet()) {
                        Map    info = (Map ) et.getValue();
                        String name = (String) et.getKey();
                        String type = (String) info.get("type");
                        String disp = (String) info.get("disp");
                        if ("1".equals(info.get("hideInList")) || "hidden".equals(type)
                        || (type != null && type.startsWith("form-"))) {
                            continue;
                        }
                     %>
                    <%if  ("number".equals(type) || "range".equals(type)) {%>
                    <th data-fn="<%=name%>" class="sortable text-right"><%=disp%></th>
                    <%} else if ("datetime".equals(type)) {%>
                    <th data-fn="<%=name%>" data-ft="_datetime" class="sortable datetime"><%=disp%></th>
                    <%} else if ("date".equals(type)) {%>
                    <th data-fn="<%=name%>" data-ft="_date" class="sortable date"><%=disp%></th>
                    <%} else if ("time".equals(type)) {%>
                    <th data-fn="<%=name%>" data-ft="_time" class="sortable time"><%=disp%></th>
                    <%} else if ("select".equals(type)) {%>
                    <th data-fn="<%=name%>_disp" class="sortable"><%=disp%></th>
                    <%} else if ("pick".equals(type)) {%>
                    <th data-fn="<%=info.get("data-tn")%>.<%=info.get("data-tk")%>" class="sortable"><%=disp%></th>
                    <%} else if (!"primary".equals(info.get("primary")) && !"foreign".equals(info.get("foreign"))) {%>
                    <th data-fn="<%=name%>" class="sortable"><%=disp%></th>
                    <%} // End If %>
                    <%} // End For%>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
    <div class="pagebox"></div>
</div>
