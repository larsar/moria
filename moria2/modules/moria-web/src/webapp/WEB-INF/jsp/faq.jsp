<?xml version="1.0" encoding="ISO-8859-1"?>

<%@ page
			language="java"
			errorPage="/Error"
			session="false"
			contentType="text/html; charset=ISO-8859-1"
			pageEncoding="ISO-8859-1"
			import="java.util.ResourceBundle, java.util.Properties, no.feide.moria.servlet.RequestUtil" %>

<%
// Get configuration properties.
Properties pconfig;
try {
    pconfig = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
} catch (IllegalStateException e) {
    pconfig = null;
}

// Get language from cookie, if available.
String langFromCookie = null;
if (pconfig != null && request.getCookies() != null) {
    langFromCookie = RequestUtil.getCookieValue((String) pconfig.get(RequestUtil.PROP_COOKIE_LANG), 
                                                request.getCookies());
}

// Get resource bundle.
final ResourceBundle bundle = RequestUtil.getBundle(RequestUtil.BUNDLE_FAQ,
                                                    request.getParameter(RequestUtil.PARAM_LANG),
                                                    langFromCookie,
                                                    null,
request.getHeader("Accept-Language"), "nb");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%= bundle.getLocale() %>" >
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="icon" href="/favicon.ico" type="image/png">
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

<div class="kropp">
<dl>
<dt>1. <a href="#d0e5"><%=bundle.getString("q1")%></a></dt>

<dt>2. <a href="#d0e31"><%=bundle.getString("q2")%></a></dt>

<dt>3. <a href="#d0e59"><%=bundle.getString("q3")%></a></dt>

<dt>4. <a href="#d0e64"><%=bundle.getString("q0")%></a></dt>
</dl>

<h2><a name="d0e5"></a>1.&nbsp;<%=bundle.getString("q1")%></h2>

<p><%=bundle.getString("answer1_1")%>:</p>

<div class="orderedlist">
<ol type="1">
<%
String owner = (String) pconfig.get(RequestUtil.PROP_FAQ_OWNER);
if ((owner == null) || owner.length() == 0)
    owner = bundle.getString("answer1_owner");
%>
<li>
<p><%=bundle.getString("answer1_2")%> <%=owner%>.</p>
</li>

<li>
<p><%=bundle.getString("answer1_3")%></p>
</li>

<li>
<p><%=bundle.getString("answer1_4")%></p>
</li>

<li>
<p><%=bundle.getString("answer1_5")%></p>
</li>

<li>
<p><%=bundle.getString("answer1_6")%></p>

</li>
</ol>

<p><%=bundle.getString("answer1_7")%></p>
</div>


<h2><a name="d0e31"></a>2.&nbsp;<%=bundle.getString("q2")%></h2>


<p><%=bundle.getString("answer2_1")%>:</p>

<div class="itemizedlist">
<ul type="disc">
<li>
<p><%=bundle.getString("answer2_2")%></p>

<p><%=bundle.getString("answer2_3")%></p>
</li>

<li>

<p><%=bundle.getString("answer2_4")%></p>

<p><%=bundle.getString("answer2_5")%></p>
</li>

<li>

<p><%=bundle.getString("answer2_6")%></p>

<p><%=bundle.getString("answer2_7")%></p>
</li>

</ul>

<p><%=bundle.getString("answer2_8")%></p>

</div>

<div>
<h2><a name="d0e59"></a>3.&nbsp;<%=bundle.getString("q3")%></h2>

<p><%=bundle.getString("answer3_1")%></p>

</div>

<div>
<h2><a name="d0e64"></a>4.&nbsp;<%=bundle.getString("q0")%></h2>

<p>
<%=bundle.getString("answer0_1")%>
<a href="<%=pconfig.get(RequestUtil.PROP_FAQ_STATUS)%>"><%=bundle.getString("answer0_2")%></a><%=bundle.getString("answer0_3")%>
</p>
<p>
<%=bundle.getString("answer0_4")%>
</p>
</tbody>
</div>
</div>

<table summary="Layout-tabell" class="invers" border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr class="bunn" valign="middle">
<td class="invers" align="left"><small><a class="invers" href="mailto:<%=pconfig.get(RequestUtil.RESOURCE_MAIL)%>"><%=pconfig.get(RequestUtil.RESOURCE_MAIL)%></a></small></td>
<td class="invers" align="right"><small><%=pconfig.get(RequestUtil.RESOURCE_DATE)%></small></td>
</tr></tbody></table>

</body>
</html>
