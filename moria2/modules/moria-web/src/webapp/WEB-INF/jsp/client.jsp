<%@ page import="java.util.Iterator,
                 java.util.Map"%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<title>Moria Test Client</title>
</head>

<body>

<%
    String username, password, attributes, urlPrefix, urlPostfix, principal;
    if (request.getParameter("attributes") != null)
        attributes = request.getParameter("attributes");
    else
        attributes = "attr1,attr2";
    if (request.getParameter("urlPrefix") != null)
        urlPrefix = request.getParameter("urlPrefix");
    else
        urlPrefix = request.getRequestURL().toString()+"?moriaID=";
    if (request.getParameter("urlPostfix") != null)
        urlPostfix = request.getParameter("urlPostfix");
    else
        urlPostfix = "";
    if (request.getParameter("principal") != null)
        principal = request.getParameter("principal");
    else
        principal = "test";
%>


<% if (request.getAttribute("ticketID") != null) {%>
<!-- Request contains Moria Ticket, show result -->

<%} else {
  if (request.getAttribute("attributes") != null) {

  }


%>
<!-- Request does not contain Moria Ticket, request authentication -->

<form action="Client" method="POST" name="startAuth">


Principal:  <input type="text" size="50" value="<%= principal %>"  name="principal"/> <br/>
Attributes: <input type="text" size="50" value="<%= attributes %>" name="attributes"/><br/>
URLPrefix:  <input type="text" size="50" value="<%= urlPrefix %>"  name="urlPrefix"/> <br/>
URLPostfix: <input type="text" size="50" value="<%= urlPostfix %>" name="urlPostfix"/><br/>
Force: <br/>
<input type="submit" value="Request authentication"/>

</form>

  <% if (request.getAttribute("error") != null) { %>
    <font color="red"><%= request.getAttribute("error") %></font>
  <%}%>
<%}%>
<table border="1">
  <% if (request.getAttribute("attributes") != null) {
      Map attrs = (Map) request.getAttribute("attributes");
      Iterator it = attrs.keySet().iterator();
      while (it.hasNext()) {
       String key = (String) it.next();
       String[] values = (String[]) attrs.get(key);%>
       <tr><td><%=key%></td><td><%for (int i = 0; i < values.length; i++) { %><%=values[i]%> <%}%></td></tr>
      <%}%>

    <%= request.getAttribute("error") %>
  <%}%>

</body>
</html>