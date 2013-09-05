<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page extends="app.hongs.action.AbstractJspPage"%>
<%@page import="app.hongs.db.DB" language="java"%>

<%
DB db = DB.getInstance();
String sql = "";
%>