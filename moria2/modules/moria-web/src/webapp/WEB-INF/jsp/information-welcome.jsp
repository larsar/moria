<?xml version="1.0" encoding="ISO-8859-1"?>

<%@ page
			language="java"
			errorPage="/Error"
			session="false"
			contentType="text/html; charset=ISO-8859-1"
			pageEncoding="ISO-8859-1"
			import="java.util.ResourceBundle, java.util.Properties, no.feide.moria.servlet.RequestUtil,
			java.util.TreeMap, java.util.Iterator" %>

<%
Properties pconfig;
try {
  pconfig = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
} catch (IllegalStateException e) {
  pconfig = null;
}

String langFromCookie = null;
if (pconfig != null && request.getCookies() != null) {
  langFromCookie = RequestUtil.getCookieValue((String) pconfig.get(RequestUtil.PROP_COOKIE_LANG), 
                                              request.getCookies());
}

final ResourceBundle bundle = RequestUtil.getBundle(
                RequestUtil.BUNDLE_INFOWELCOME, request.getParameter(RequestUtil.PARAM_LANG), langFromCookie, null,
                request.getHeader("Accept-Language"), (String) pconfig.get(RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE)); %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
	  "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%= bundle.getLocale() %>" >

<head>
  <title><%= bundle.getString("header_title") %></title>
</head>
<body>

<h2>
<%=bundle.getString("welcome")%>
</h2>

<a href="<%=pconfig.get(RequestUtil.PROP_INFORMATION_URL_PREFIX)%>"><%=bundle.getString("login")%></a>