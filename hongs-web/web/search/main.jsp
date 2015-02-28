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
    <div class="col-md-3 side-context" data-load="search/<%=_entity%>/left.jsp"></div>
    <div class="col-md-9 main-context" data-load="search/<%=_entity%>/list.jsp"></div>
</div>