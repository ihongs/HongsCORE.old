<%@page import="app.hongs.CoreConfig"%>
<%@page import="app.hongs.CoreLanguage"%>
<%@page import="app.hongs.db.DB"%>
<%@page import="app.hongs.db.Mview"%>
<%@page import="app.hongs.serv.CommonFilter"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
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
    Map<String, String[ ] > data = request.getParameterMap();
    CoreLanguage lang = CoreLanguage.getInstance();
    lang.load(_module);

    String idKey = view.getIdKey();
    String title = view.getTitle();
    String opera = (String)request.getAttribute("form.action");
    if (data.containsKey("id")) {
        data.put( idKey, data.get("id" ));
    } else if (! data.containsKey(idKey)) {
        data.put("id", new String[]{"0"});
    }
    if (opera == null) {
        opera = "create";
    }
    title = lang.translate("fore."+opera, title);
    
    CoreConfig conf = CoreConfig.getInstance();
    Set hide = new HashSet();
    hide.add(conf.getProperty("core.table.ctime.field", "ctime"));
    hide.add(conf.getProperty("core.table.mtime.field", "mtime"));
    hide.add(conf.getProperty("core.table.etime.field", "etime"));
    hide.add(conf.getProperty("core.table.state.field", "state"));
%>

<object class="config" name="hsInit" data="">
    <param name="width" value="600px"/>
</object>

<h2><%=title%></h2>

<div id="<%=_module%>-<%=_entity%>-form">
    <object class="config" name="hsForm" data="">
        <param name="loadUrl" value="<%=root%>/retrieve.act?id=<%=data.get("id")[0]%>&jd=1"/>
        <param name="saveUrl" value="<%=root%>/<%=opera%>.act"/>
        <param name="_fill__pick" value="(hsFormFillPick)"/>
    </object>
    <form action="" method="POST">
        <div class="row">
            <div class="col-md-4">
                <%for(Map.Entry et : flds.entrySet()) {
                    String key = (String) et.getKey();
                    Map    map = (Map ) et.getValue();
                    String rqr = "required".equals(map.get("required")) ? "required=\"required\"" : "";
                    if (hide.contains(key)) continue ;
                 %>
                <%if ("hidden".equals(map.get("type")) || data.get(key) != null) {%>
                    <input type="hidden" name="<%=key%>"/>
                <%} else {%>
                    <div class="form-group">
                        <label class="control-label"><%=map.get("disp")%></label>
                        <%if ("pick".equals(map.get("type"))) {%>
                            <ul class="pickbox" data-ft="_pick" data-fn="<%=key%>" data-tn="<%=map.get("data-tn")%>" data-tk="<%=map.get("data-tk")%>" data-vk="<%=map.get("data-vk")%>" <%=rqr%>></ul>
                            <button type="button" class="btn btn-default form-control" data-toggle="hsPick" data-target="@" data-href="<%=_module%>/<%=map.get("data-tn")%>/list4select.html"><%=lang.translate("fore.select", (String)map.get("disp"))%></button>
                        <%} else if ("textarea".equals(map.get("type"))) {%>
                            <textarea class="form-control" name="<%=key%>" <%=rqr%>></textarea>
                        <%} else if ("number".equals(map.get("type"))) {%>
                            <input class="form-control" type="number" name="<%=key%>" value="" <%=rqr%>/>
                        <%} else if ("datetime".equals(map.get("type"))) {%>
                            <input class="form-control input-datetime" type="text" name="<%=key%>" value="" <%=rqr%> data-toggle="datetimepicker"/>
                        <%} else if ("date".equals(map.get("type"))) {%>
                            <input class="form-control input-date" type="text" name="<%=key%>" value="" <%=rqr%> data-toggle="datetimepicker"/>
                        <%} else if ("time".equals(map.get("type"))) {%>
                            <input class="form-control input-time" type="text" name="<%=key%>" value="" <%=rqr%> data-toggle="datetimepicker"/>
                        <%} else if ("enum".equals(map.get("type"))) {%>
                            <select class="form-control" name="<%=key%>"><option value="">--请选择--</option></select>
                        <%} else {%>
                            <input class="form-control" type="text" name="<%=key%>" value="" <%=rqr%>/>
                        <%} // End If %>
                    </div>
                <%} // Edn If %>
                <%} // End For%>
            </div>
            <div class="col-md-4">
            </div>
        </div>
        <div>
            <button type="submit" class="ensure btn btn-primary">提交</button>
            <button type="button" class="cancel btn btn-link">取消</button>
        </div>
    </form>
</div>