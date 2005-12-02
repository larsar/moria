<?xml version="1.0" encoding="ISO-8859-1"?>

<%@ page
			language="java"
			isErrorPage="true"
			session="false" 
			contentType="text/html; charset=ISO-8859-1" 
			pageEncoding="ISO-8859-1" 
			import="java.util.ResourceBundle, java.util.Properties, no.feide.moria.servlet.*, no.feide.moria.controller.*, no.feide.moria.log.*" %>


<%
   response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

	 MessageLogger messageLogger = new MessageLogger(JSPLogIdentifier.class);
   messageLogger.logCritical("Showing error page", exception);
%>

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
                RequestUtil.BUNDLE_ERROR, request.getParameter(RequestUtil.PARAM_LANG), langFromCookie, null,
                request.getHeader("Accept-Language"), (String) pconfig.get(RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE)); %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%= bundle.getLocale() %>" >
<head>
<link rel="icon" href="../resource/favicon.ico" type="image/png">
<style type="text/css">
@import url("../resource/stil.css");
</style>
<link rel="author" href="mailto:<%=pconfig.get(RequestUtil.RESOURCE_MAIL)%>">
<title><%=bundle.getString("header_title")%></title>
</head>

  <body>
  
  <table summary="Layout-tabell" class="invers" border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="middle">
<td class="logo" width="76"><a href="<%=pconfig.get(RequestUtil.RESOURCE_LINK)%>"><img src="../resource/logo.gif" alt="<%=pconfig.get(RequestUtil.PROP_FAQ_OWNER)%>" border="0" height="41" width="76"></a></td>
<td width="0%"><a class="noline" href="<%=pconfig.get(RequestUtil.RESOURCE_LINK)%>"><%=bundle.getString("header_feide")%></a></td>
<td class="dekor1" width="100%">&nbsp;</td>
</tr></tbody></table>

<div class="midt">
<table cellspacing="0">
<tbody><tr valign="top">
<td class="kropp">
     <h3><%=bundle.getString("general_message")%></h3>

<%

  String errmsg = null;
  String errdesc = null;

  Class eclass = null;
    // It is possible to enter https://login.feide.no/Error, which will generate a "false" error and a NULL exception.
    // That case will be logged, and we'll present the "unknown error" (catch all) 
   
   if (exception == null) {
       messageLogger.logCritical("exception is null. The error page has been requested directly without an actual error occuring", exception);
   }
   else
       eclass = exception.getClass();

  Class errclass;

  //If it's a ServletException, the actual error class has to be "dug out"
  if (eclass == ServletException.class) {
      ServletException se = (ServletException) exception;
      Throwable servletThrowable = se.getRootCause();
      errclass = servletThrowable.getClass();
  }
  else
    errclass = eclass; // direct errorclass available

  if (errclass == AuthenticationException.class) { 
      errmsg  = bundle.getString("error_authentication_msg");
      errdesc = bundle.getString("error_authentication_desc");
  }
  else if (errclass == AuthorizationException.class) {
      errmsg = bundle.getString("error_authorization_msg");
      errdesc = bundle.getString("error_authorization_desc");
  }
  else if (errclass == DirectoryUnavailableException.class) { 
      errmsg = bundle.getString("error_directory_msg");
      errdesc = bundle.getString("error_directory_desc");
  }
  else if (errclass == IllegalInputException.class) { 
      errmsg = bundle.getString("error_illegalinput_msg");
      errdesc = bundle.getString("error_illegalinput_desc");
  }
  else if (errclass == UnknownTicketException.class) {
      errmsg = bundle.getString("error_unknownticket_msg");
      errdesc = bundle.getString("error_unknownticket_desc");
  }
  else if (errclass == InoperableStateException.class) { 
      errmsg = bundle.getString("error_inoperable_msg");
      errdesc = bundle.getString("error_inoperable_desc");
      // This is currently the end of normal errors. The rest are internal errors
  }
  else if (errclass == MoriaControllerException.class) { 
      errmsg = bundle.getString("error_moriacontroller");
      errdesc = bundle.getString("error_internalerror");
  }
  else if (errclass == IllegalArgumentException.class) {
      errmsg = bundle.getString("error_illegalargument_msg");
      errdesc = bundle.getString("error_internalerror");
  }
  else if (errclass == NullPointerException.class) {
      errmsg = bundle.getString("error_nullpointer");
      errdesc = bundle.getString("error_internalerror");
  }
  else if (errclass == IllegalStateException.class) {
      errmsg = bundle.getString("error_illegalstate");
      errdesc = bundle.getString("error_internalerror");
  }
  else { 
      errmsg = bundle.getString("error_rest");
      errdesc = bundle.getString("error_internalerror");
      // This is the catch-all part: "unknown error", which should not happen normally.
      // Two cases where this might occur: the resource files are wrong or the error page is requested directly
   }
   %>
      <p><b><%= errmsg %></b></p>
	  <p><%= errdesc %></p>

     <dd>
     

</tr>
</table>
</tbody>
</div>

<p>
<table summary="Layout-tabell" class="invers" border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr class="bunn" valign="middle">
<td class="invers" align="left"><small><a class="invers" href="mailto:<%=pconfig.get(RequestUtil.RESOURCE_MAIL)%>"><%=pconfig.get(RequestUtil.RESOURCE_MAIL)%></a></small></td>
<td class="invers" align="right"><small><%=pconfig.get(RequestUtil.RESOURCE_DATE)%></small></td>
</tr></tbody></table></p>

  </body>
</html>
