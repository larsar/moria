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
                RequestUtil.BUNDLE_FAQ, request.getParameter(RequestUtil.PARAM_LANG), langFromCookie, null,
                request.getHeader("Accept-Language"), "nb"); %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
	  "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%= bundle.getLocale() %>" >
<head>
<meta name="generator" content="HTML Tidy, see www.w3.org">
  <title><%= bundle.getString("header_title") %></title>
  <link rel="stylesheet" href="html.css" type="text/css">
  <meta name="generator" content="DocBook XSL Stylesheets V1.60.1">
</head>

<body bgcolor="white" text="black" link="#0000FF" vlink="#840084"
alink="#0000FF">
<div class="article" lang="en">
<div class="titlepage">
<div>
<div>

<h1 class="title"><a name="d0e1"></a><%=bundle.getString("header_title")%></h1>
</div>
</div>

<hr>
</div>

<div class="toc">
<dl>
<dt>1. <a href="#d0e5"><%=bundle.getString("q1")%></a></dt>

<dt>2. <a href="#d0e31"><%=bundle.getString("q2")%></a></dt>

<dt>3. <a href="#d0e54"><%=bundle.getString("q3")%></a></dt>

<dt>4. <a href="#d0e59"><%=bundle.getString("q4")%></a></dt>

<dt>5. <a href="#d0e64"><%=bundle.getString("q5")%></a></dt>
</dl>
</div>

<div class="section" lang="en">
<div class="titlepage">
<div>
<div>
<h2 class="title" style="clear: both"><a name="
d0e5"></a>1.&nbsp;<%=bundle.getString("q1")%></h2>

</div>
</div>
</div>

<p><%=bundle.getString("answer1_1")%>:</p>

<div class="orderedlist">
<ol type="1">
<li>
<p><%=bundle.getString("answer1_2")%></p>
</li>

<li>
<p><%=bundle.getString("answer1_3")%><a href="
http://www.feide.no/" target="_top"><%=bundle.getString("answer1_4")%></a>.</p>
</li>

<li>
<p><%=bundle.getString("answer1_5")%></p>

</li>

<li>
<p><%=bundle.getString("answer1_6")%></p>
</li>

<li>
<p><%=bundle.getString("answer1_7")%></p>

</li>
</ol>
</div>

<p><%=bundle.getString("answer1_8")%></p>
</div>

<div class="section" lang="en">
<div class="titlepage">
<div>
<div>
<h2 class="title" style="clear: both"><a name="
d0e31"></a>2.&nbsp;<%=bundle.getString("q2")%></h2>

</div>
</div>
</div>

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
</div>

<p><%=bundle.getString("answer2_8")%></p>

</div>

<div class="section" lang="en">
<div class="titlepage">
<div>
<div>
<h2 class="title" style="clear: both"><a name="
d0e54"></a>3.&nbsp;<%=bundle.getString("q3")%></h2>
</div>
</div>
</div>

<p><%=bundle.getString("answer3_1")%></p>
</div>

<div class="section" lang="en">
<div class="titlepage">
<div>

<div>
<h2 class="title" style="clear: both"><a name="
d0e59"></a>4.&nbsp;<%=bundle.getString("q4")%></h2>
</div>
</div>
</div>

<p><%=bundle.getString("answer4_1")%></p>

</div>

<div class="section" lang="en">
<div class="titlepage">
<div>
<div>
<h2 class="title" style="clear: both"><a name="
d0e64"></a>5.&nbsp;<%=bundle.getString("q5")%></h2>
</div>
</div>
</div>

<p><%=bundle.getString("answer5_1")%></p>
</div>
</div>

</body>
</html>
