<%@page contentType="text/html; charset=UTF-8"%>
<%@page import="app.hongs.action.ActionDriver"%>
<%@page extends="app.hongs.action.Pagelet"%>
<%
    String  _entity; int i;
    _entity = ActionDriver.getWorkPath(request);
    i = _entity.lastIndexOf('/');
    _entity = _entity.substring(1, i);
    i = _entity.lastIndexOf('/');
    _entity = _entity.substring(i+ 1);
%>
<div id="search-list">
    <object class="config" name="hsList" data="">
        <param name="loadUrl" value="('search/<%=_entity%>/retrieve.act')"/>
    </object>
    <div class="listbox table-responsive">
        <table class="table table-hover table-striped">
            <thead>
                <tr>
                    <th data-fn="name" class="sortable">名称</th>
                    <th data-fn="note">备注</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
    <div class="pagebox"></div>
</div>
