<?xml version="1.0" encoding="ISO-8859-1"?>

<%@ page
			language="java"
			errorPage="/Error"
			session="false" 
			contentType="text/html; charset=ISO-8859-1" 
			pageEncoding="ISO-8859-1" %>

<% String wsdlURL = request.getContextPath() + request.getAttribute("serviceName") + "?wsdl"; %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
	  "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" >
  <head>
    <title>Moria Web Service</title>
  </head>
  <body>
    <p>
      <a href="<%= wsdlURL %>">WSDL</a>
    </p>
  </body>
</html>
