<?xml version="1.0" encoding="ISO-8859-1"?>

<%@ page
			language="java"
			isErrorPage="true"
			session="false" 
			contentType="text/html; charset=ISO-8859-1" 
			pageEncoding="ISO-8859-1" %>

<% response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE); %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
		"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" >
  <head>
		<title>Moria Web Service Error</title>
  </head>
  <body>
    <p>
			An error occured processing the request.
    </p>
    
<%if (request.getAttribute("error") != null) {%>

	<!-- Display error message. -->
	<font color="red"><%=request.getAttribute("error")%></font>

<%}%>
    
  </body>
</html>
