<%@page import="app.hongs.CoreLanguage"%>
<%@page import="app.hongs.db.DB"%>
<%@page import="app.hongs.db.Mview"%>
<%@page import="app.hongs.dl.HaimFilter"%>
<%@page import="java.util.Map"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String _module = (String)request.getAttribute(HaimFilter.MODULE);
    String _entity = (String)request.getAttribute(HaimFilter.ENTITY);

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
        data.put(idKey, data.get("id"));
    }
    if (opera == null) {
        opera = "create";
    }
    title = lang.translate("fore."+opera, title);
%>

<object class="config" name="hsInit" data="">
    <param name="width" value="600px"/>
</object>

<h2><%=title%></h2>

<div id="hcum_user_form">
    <object class="config" name="hsForm" data="">
        <param name="loadUrl" value="<%=root%>/retrieve.act?id=0"/>
        <param name="saveUrl" value="<%=root%>/<%=opera%>.act"/>
    </object>
    <form action="" method="POST">
        <div class="row">
            <div class="col-md-4">
                <%for (Map.Entry et : flds.entrySet()) {
                    String key = (String) et.getKey();
                    Map    map = (Map ) et.getValue();
                    String rqr = "required".equals(map.get("required")) ? "required=\"required\"" : "";
                 %>
                <%if ("hidden".equals(map.get("type")) || data.get(key) != null) {%>
                    <input type="hidden" name="<%=key%>" value="<%=data.containsKey(key) ? data.get(key)[0] : ""%>"/>
                <%} else {%>
                    <div class="form-group">
                        <label class="control-label"><%=map.get("text")%></label>
                        <%if ("pick".equals(map.get("type"))) {%>
                            <ul class="pickbox" data-ft="_pick" data-fn="<%=key%>" data-tn="<%=map.get("pick-tn")%>" data-vk="<%=map.get("pick-vk")%>" data-tk="<%=map.get("pick-tk")%>" <%=rqr%>></ul>
                            <button type="button" class="btn btn-default form-control" data-toggle="hsPick" data-target="@" data-href="<%=_module%>/<%=map.get("name")%>/pick.html"><%=lang.translate("fore.select", (String)map.get("text"))%></button>
                        <%} else if ("textarea".equals(map.get("type"))) {%>
                            <textarea class="form-control" name="<%=key%>" <%=rqr%>></textarea>
                        <%} else if ("number".equals(map.get("type"))) {%>
                            <input class="form-control" type="number" name="<%=key%>" value="" <%=rqr%>/>
                        <%} else if ("datetime".equals(map.get("type"))) {%>
                            <input class="form-control" type="datetime" name="<%=key%>" value="" <%=rqr%>/>
                        <%} else if ("date".equals(map.get("type"))) {%>
                            <input class="form-control" type="date" name="<%=key%>" value="" <%=rqr%>/>
                        <%} else if ("time".equals(map.get("type"))) {%>
                            <input class="form-control" type="date" name="<%=key%>" value="" <%=rqr%>/>
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