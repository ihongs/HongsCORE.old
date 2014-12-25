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
    CoreLanguage lang = CoreLanguage.getInstance();
    lang.load(_module);

    String idKey = view.getIdKey();
    String title = view.getTitle();
    String list = lang.translate("fore.list", title);
    String find = lang.translate("fore.find", title);
    String create = lang.translate("fore.create", title);
    String modify = lang.translate("fore.modify", title);
    String delete = lang.translate("fore.delete", title);
    String deleteConfirm = lang.translate("fore.delete.confirm", title);
%>
<h2><%=list%></h2>
<div id="hcim-list">
    <object class="config" name="hsList" data="">
        <param name="loadUrl" value="('<%=root%>/retrieve.act')"/>
        <param name="openUrls:0" value="['.create','<%=root%>/form.html?','{TABSBOX}']"/>
        <param name="openUrls:1" value="['.modify','<%=root%>/form4modify.html?<%=idKey%>={ID}','{TABSBOX}']"/>
        <param name="sendUrls:0" value="['.delete','<%=root%>/delete.act','<%=deleteConfirm%>']"/>
    </object>
    <div>
        <div class="toolbox col-md-9 btn-group">
            <button type="button" class="create btn btn-default"><%=create%></button>
            <button type="button" class="modify for-select btn btn-default"><%=modify%></button>
            <button type="button" class="delete for-checks btn btn-danger" ><%=delete%></button>
        </div>
        <form class="findbox col-md-3 input-group" action="" method="POST">
            <input type="search" name="wd" class="form-control input-search"/>
            <span class="input-group-btn">
                <button type="submit" class="btn btn-default"><%=find%></button>
            </span>
        </form>
    </div>
    
</div>
