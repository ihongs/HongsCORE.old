<%@page import="app.hongs.Core"%>
<%@page import="app.hongs.CoreLanguage"%>
<%@page import="app.hongs.serv.RiggerFilter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String _config = (String)request.getAttribute(RiggerFilter.CONFIG);
    String _prefix = (String)request.getAttribute(RiggerFilter.PREFIX);
    String _entity = (String)request.getAttribute(RiggerFilter.ENTITY);
    
    String name = _config;
    String root = _prefix;
    if (_entity.length() == 0) {
        name += "." + _entity;
        root += "/" + _entity;
    }
    
    CoreLanguage lang = CoreLanguage.getInstance( );
    lang.load(_config);
    name = name.replace("/", ".");
    name = lang.containsKey(name)? lang.translate(name): "";
    String list = lang.translate("fore.list", name);
    String find = lang.translate("fore.find", name);
    String create = lang.translate("fore.create", name);
    String modify = lang.translate("fore.modify", name);
    String delete = lang.translate("fore.delete", name);
    String deleteConfirm = lang.translate("fore.delete.confirm", name);
%>
<h2><%=list%></h2>
<div id="hcim-list">
    <object class="config" name="hsList" data="">
        <param name="loadUrl" value="('<%=root%>/list.act')"/>
        <param name="openUrls:0" value="['.create','<%=root%>/create_form.html?','{TABSBOX}']"/>
        <param name="openUrls:1" value="['.modify','<%=root%>/modify_form.html?id={ID}','{TABSBOX}']"/>
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
    <div class="listbox table-responsive">
        <table class="table table-hover table-striped">
            <thead>
                <tr>
                    <th data-fn="id[]" data-ft="_check" class="_check">
                        <input type="checkbox" class="checkall" name="id[]"/>
                    </th>
                    <th data-fn="username" class="sortable">邮箱</th>
                    <th data-fn="name" class="sortable">昵称</th>
                    <th data-fn="mtime" data-ft="_htime" class="_htime sortable">修改时间</th>
                    <th data-fn="ctime" data-ft="_htime" class="_htime sortable">创建时间</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
    <div class="pagebox"></div>
</div>
