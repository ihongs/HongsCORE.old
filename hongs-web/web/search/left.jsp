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
<div id="search-left">
    <object class="config" name="hsList" data="">
        <param name="loadUrl" value="('search/<%=_entity%>/counts/retrieve.act')"/>
    </object>
    <div class="listbox table-responsive">
        <table class="table table-hover table-striped">
            <thead>
                <tr>
                    <th data-fn="id[]" data-ft="_check" class="_check">
                        <input type="checkbox" class="checkall" name="id[]"/>
                    </th>
                    <th data-fn="username" class="sortable">选择</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
    <div class="pagebox"></div>
</div>
