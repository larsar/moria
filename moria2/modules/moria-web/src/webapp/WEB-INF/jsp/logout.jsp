<?xml version="1.0" encoding="ISO-8859-1"?>

<%@ page
			language="java"
			errorPage="/Error"
			session="false"
			contentType="text/html; charset=ISO-8859-1"
			pageEncoding="ISO-8859-1"
			import="java.util.ResourceBundle" %>

<% ResourceBundle bundle = (ResourceBundle) request.getAttribute("bundle"); %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
	  "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%= bundle.getLocale() %>" >
  <head>
    <title><%= bundle.getString("header_title") %></title>
  </head>
  <body>
     <p>
      <h1><%= bundle.getString("body_title") %></h1>
      <%= bundle.getString("body_text") %>
    </p>
  </body>
</html>
