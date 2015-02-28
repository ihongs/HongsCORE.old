<%@page import="app.hongs.CoreConfig"%>
<%@page import="app.hongs.CoreLanguage"%>
<%@page import="app.hongs.action.ActionDriver"%>
<%@page import="app.hongs.action.FormSet"%>
<%@page import="app.hongs.action.SiteMap"%>
<%@page import="app.hongs.util.Synt"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.HashMap"%>
﻿<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page extends="app.hongs.action.Pagelet"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    int i;
    String _module, _entity;
    _module = ActionDriver.getWorkPath(request);
    i = _module.lastIndexOf('/');
    _module = _module.substring(1, i);
    i = _module.lastIndexOf('/');
    _entity = _module.substring(i+ 1);
    _module = _module.substring(0, i);

    String _action, _render;
    _action = (String)request.getAttribute("form.action");
    if (_action == null) {
        _action = "create";
        _render = "form.jsp";
    } else {
        _render = "form4"+_action+".jsp";
    }

    CoreLanguage lang = CoreLanguage.getInstance().clone(/**/);
                 lang.loadIgnrFNF/***/(_module);
    SiteMap site = SiteMap.getInstance(_module);
    FormSet form = FormSet.getInstance(_module);
    Map<String, Object> flds = form.getFormTranslated(_entity);

    Map    pagz  = site.getPage(_module+"/"+_entity+"/list.jsp");
    String title = pagz == null ? "" : (String) pagz.get("disp");
           title = lang.translate(title);

    Map<String, String[]> data = new HashMap();
    data.putAll(request.getParameterMap());
    if (data.containsKey("id") == false ) {
        data.put("id", new String[] {"0"});
    }

    Set hide = new HashSet();
    CoreConfig conf = CoreConfig.getInstance( );
    hide.add(conf.getProperty("core.table.ctime.field", "ctime"));
    hide.add(conf.getProperty("core.table.mtime.field", "mtime"));
    hide.add(conf.getProperty("core.table.etime.field", "etime"));
    hide.add(conf.getProperty("core.table.state.field", "state"));
%>
<!-- 表单 -->
<h2><%=lang.translate("fore."+_action+".title", title)%></h2>
<object class="config" name="hsInit" data="">
    <param name="width" value="600px"/>
</object>
<div id="<%=_module%>-<%=_entity%>-<%=_action%>">
    <object class="config" name="hsForm" data="">
        <param name="loadUrl" value="<%=_module%>/<%=_entity%>/retrieve.act?id=<%=data.get("id")[0]%>&jd=1"/>
        <param name="saveUrl" value="<%=_module%>/<%=_entity%>/<%=_action%>.act"/>
        <param name="_fill__pick" value="(hsFormFillPick)"/>
    </object>
    <form action="" method="POST">
        <div class="row"><div class="col-md-6">
            <%
            for(Map.Entry et : flds.entrySet()) {
                Map    info = (Map ) et.getValue();
                String name = (String) et.getKey();
                String type = (String) info.get( "widget" );
                if (null == type) {
                       type = (String) info.get("__type__");
                }
                String disp = (String) info.get("__disp__");
                String rqrd = Synt.declare(info.get("__required__"), false) ? "required=\"required\"" : "";
                String rptd = Synt.declare(info.get("__repeated__"), false) ? "multiple=\"multiple\"" : "";
                if (!"".equals(rptd)) {
                    name += ".";
                }

                if ("yes".equals(info.get("hideInForm")) || hide.contains(name)) {
                    continue ;
                }
             %>
            <%if ("hidden".equals(type) || data.get(name) != null) {%>
                <input type="hidden" name="<%=name%>"/>
            <%} else if (type.startsWith("form-col-")) {%>
            <%} else {%>
                <div class="form-group">
                    <label class="control-label"><%=disp%></label>
                    <%if ("textarea".equals(type)) {%>
                        <textarea class="form-control" name="<%=name%>" <%=rqrd%>></textarea>
                    <%} else if ("number".equals(type)) {%>
                        <input class="form-control" type="number" name="<%=name%>" value="" <%=rqrd%>/>
                    <%} else if ("range".equals(type)) {%>
                        <input class="form-control" type="range" name="<%=name%>" value="" <%=rqrd%>/>
                    <%} else if ("file".equals(type)) {%>
                        <input class="form-control" type="file" name="<%=name%>" value="" <%=rqrd%>/>
                    <%} else if ("date".equals(type)) {%>
                        <input class="form-control input-date" type="text" name="<%=name%>" value="" data-toggle="datetimepicker" <%=rqrd%>/>
                    <%} else if ("time".equals(type)) {%>
                        <input class="form-control input-time" type="text" name="<%=name%>" value="" data-toggle="datetimepicker" <%=rqrd%>/>
                    <%} else if ("datetime".equals(type)) {%>
                        <input class="form-control input-datetime" type="text" name="<%=name%>" value="" data-toggle="datetimepicker" <%=rqrd%>/>
                    <%} else if ("check".equals(type)) {%>
                        <div class="checkbox" data-fn="<%=name%>" data-ft="_check"></div>
                    <%} else if ("radio".equals(type)) {%>
                        <div class="radio"    data-fn="<%=name%>" data-ft="_radio"></div>
                    <%} else if ("enum".equals(type) || "select".equals(type)) {%>
                        <select class="form-control" name="<%=name%>" <%=rqrd%> <%=rptd%>><option value="">--<%=lang.translate("fore.select.lebel")%>--</option></select>
                    <%} else if ("form".equals(type) || "picker".equals(type)) {%>
                        <ul class="pickbox" data-ft="_pick" data-fn="<%=name%>" data-tn="<%=info.get("data-tn")%>" data-tk="<%=info.get("data-tk")%>" data-vk="<%=info.get("data-vk")%>" <%=rqrd%>></ul>
                        <button type="button" class="btn btn-default form-control" data-toggle="hsPick" data-target="@" data-href="<%=info.containsKey("data-ln")?info.get("data-tn"):_module%>/<%=info.get("data-tn")%>/list4select.html"><%=lang.translate("fore.select.lebel", (String)disp)%></button>
                    <%} else {%>
                        <input class="form-control" type="text" name="<%=name%>" value="" <%=rqrd%>/>
                    <%} // End If %>
                </div>
            <%} // Edn If %>
            <%} // End For%>
        </div></div>
        <div>
            <button type="submit" class="ensure btn btn-primary"><%=lang.translate("fore.ensure")%></button>
            <button type="button" class="cancel btn btn-link"   ><%=lang.translate("fore.cancel")%></button>
        </div>
    </form>
</div>