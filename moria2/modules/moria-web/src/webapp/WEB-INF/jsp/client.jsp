<%@ page import="java.util.Iterator,
                 java.util.Map"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<title>Moria Test Client</title>
</head>

<body>

<!-- Do we have an error message? -->
<%if (request.getAttribute("error") != null) {%>

	<!-- Display error message. -->
	<font color="red"><%=request.getAttribute("error")%></font>

<!-- Do we have attributes to show? -->	
<%} else if (request.getAttribute("attributes") != null) {%>
  
	<!-- Show table with attribute values. -->
	<%Map attributes = (Map) request.getAttribute("attributes");
	Iterator keys = attributes.keySet().iterator();%>
	<table><tr><td><u>Attribute name</u></td><td><u>Attribute value(s)</u></td></tr>
	<%while (keys.hasNext()) {%>
		<%String name = (String)keys.next();%>
		<tr>
		<td><%=name%></td>
		<%Object values = attributes.get(name);
		if (values == null)%>
			<td>NULL</td>
	    <%else {
	    	String value = values.toString();%>
	    	<td><%=value%></td>
	    <%}%>
		</tr>
	<%}%>
	</table>
	
<!-- The authentication page. -->
<%} else {%>

	<!-- Show authentication form. -->
	<%String username, password, attributes, urlPrefix, urlPostfix, principal;
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
        principal = "test";%>
	
	<form action="Client" method="POST" name="startAuth">
		Service principal:    <input type="text" size="50" value="<%=principal%>"  name="principal"/> <br/>
		Requested attributes: <input type="text" size="50" value="<%=attributes%>" name="attributes"/><br/>
		URL prefix:           <input type="text" size="50" value="<%=urlPrefix%>"  name="urlPrefix"/> <br/>
		URL postfix:          <input type="text" size="50" value="<%=urlPostfix%>" name="urlPostfix"/><br/>
		<input type="submit" value="Request authentication"/>
	</form>

<%}%>

</body>

</html>