<?xml version="1.0" encoding="ISO-8859-1"?>

<%@ page
			language="java"
			errorPage="/Error"
			session="false"
			contentType="text/html; charset=ISO-8859-1"
			pageEncoding="ISO-8859-1"
			import="java.util.ResourceBundle, java.util.Properties, no.feide.moria.servlet.RequestUtil" %>

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
                RequestUtil.BUNDLE_INFOABOUT, request.getParameter(RequestUtil.PARAM_LANG), langFromCookie, null,
                request.getHeader("Accept-Language"), "nb"); %>

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

   <table summary="" cellspacing="0" cellpadding="0" border="0" width="100%">
   <tr><td style="text-align:centre"><i><p><%=bundle.getString("info_legal1")%></p>
   <p><%=bundle.getString("info_legal2")%></p>
   <p><%=bundle.getString("info_legal3")%></p></i></td>
   </tr>
   </table>

   
</body>
</html>