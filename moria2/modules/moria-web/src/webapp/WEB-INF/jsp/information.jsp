<?xml version="1.0" encoding="ISO-8859-1"?>

<%@ page
			language="java"
			errorPage="/Error"
			session="true"
			contentType="text/html; charset=ISO-8859-1"
			pageEncoding="ISO-8859-1"
			import="java.util.ResourceBundle, java.util.Vector, no.feide.moria.servlet.RequestUtil,
			java.util.TreeMap, java.util.Iterator, java.util.Properties" %>
			
<% // The Information service does not allow caching.
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>

<% ResourceBundle bundle = (ResourceBundle) request.getAttribute("bundle"); %>
<% Vector tabledata = (Vector) request.getAttribute("tabledata"); %>
<% String userorg = (String) request.getAttribute("userorg"); %>
<% String[] picarr = (String[]) request.getAttribute("picture"); %>
<% if (picarr != null) { %>
<% session.setAttribute("picture", picarr); %>
<% } 
Properties pconfig;
try {
  pconfig = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
} catch (IllegalStateException e) {
  pconfig = null;
}%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%= bundle.getLocale() %>" >
<head>
<link rel="icon" href="/favicon.ico" type="image/png">
<style type="text/css">
@import url("../resource/stil.css");
</style>
<link rel="author" href="mailto:<%=pconfig.get(RequestUtil.RESOURCE_MAIL)%>">
  <title><%= bundle.getString("header_title") %></title>
</head>
<body>
<!-- Do we have table data (attributes) to show? -->	
<%if (tabledata != null) {%>
    <% if (!(((Boolean)request.getAttribute(RequestUtil.ATTR_SELECTED_DENYSSO)).booleanValue())) { %>

<table summary="Layout-tabell" class="invers" border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="middle">
<td class="logo" width="76"><a href="<%=pconfig.get(RequestUtil.RESOURCE_LINK)%>"><img src="../resource/logo.gif" alt="<%=pconfig.get(RequestUtil.PROP_FAQ_OWNER)%>" border="0" height="41" width="76"></a></td>
<td width="0%"><a class="noline" href="<%=pconfig.get(RequestUtil.RESOURCE_LINK)%>"><%=bundle.getString("header_feide")%></a></td>
<td width="35%">&nbsp;<td>


<td class="dekor1" width="100%">&nbsp;</td>
</tr></tbody></table> 

   
   <% } 
   		else { %>
   		<table summary="Layout-tabell" class="invers" border="0" cellpadding="0" cellspacing="0" width="100%">
		<tbody><tr valign="middle">
		<td class="logo" width="76"><a href="<%=pconfig.get(RequestUtil.RESOURCE_LINK)%>"><img src="../resource/logo.gif" alt="<%=pconfig.get(RequestUtil.PROP_FAQ_OWNER)%>" border="0" height="41" width="76"></a></td>
		<td width="0%"><a class="noline" href="<%=pconfig.get(RequestUtil.RESOURCE_LINK)%>"><%=bundle.getString("header_feide")%></a></td>
		<td class="dekor1" width="100%">&nbsp;</td>
		</tr></tbody></table>
		
		<% } %> <!-- end of denySSO test -->
    
<div class="midt">
<table cellspacing="0">
<tbody><tr valign="top">
<td class="meny">
   <br><%=bundle.getString("login")%><%=request.getAttribute("username")%>
   <br>
   <!-- Logout-->
   <A href="<%= request.getAttribute(RequestUtil.ATTR_BASE_URL) + "&logout=user_logout" %>"><%=bundle.getString("user_logout")%></A>

</td>
    <%
      /* rearrange table data into some temporary vectors */
      Vector mandatoryvec = new Vector();
      Vector optionalvec = new Vector();
      Vector o_mandatoryvec = new Vector();
      Vector o_optionalvec = new Vector();
      final String piclink = (String)request.getAttribute(RequestUtil.PIC_LINK);
      int n = tabledata.size();

        for (int i = 0; i < n; i += 6) {
          String link = (String) tabledata.get(i);
          String description = (String) tabledata.get(i+1);
          String userstring = (String) tabledata.get(i+2);
          String relevance = (String) tabledata.get(i+3);
          String o_relevance = (String) tabledata.get(i+4);
          String key = (String) tabledata.get(i+5);

          if (userstring == null || userstring.equals("")) {	
		    if (relevance.equals("Mandatory")) {
                userstring = "<FONT COLOR=\"#ff0000\">" + bundle.getString("m_missing") + "</FONT>";
            }
            else {
                userstring = bundle.getString("o_missing");
            }
          }
          if (userstring.equals("p_yes")) {
            userstring = "";
            if (picarr != null) {
          	  for (int j = 0; j < picarr.length; j++) {
			    userstring += "<A HREF=\"" + piclink + "?index=" + j + "\" TARGET=\"_blank\">" + (j+1) + "</A><BR>"; 
			  }
			}
          }
          
          if (relevance.equals("Mandatory")) {
            mandatoryvec.add(link);
            mandatoryvec.add(description);
            mandatoryvec.add(userstring);
            mandatoryvec.add(key);
          } else if (relevance.equals("Optional")){
            optionalvec.add(link);
            optionalvec.add(description);
            optionalvec.add(userstring);
            optionalvec.add(key);
           }
          
          if (o_relevance.equals("Mandatory")) {
              o_mandatoryvec.add(link);
              o_mandatoryvec.add(description);
              o_mandatoryvec.add(userstring);
              o_mandatoryvec.add(key);
          } else if (o_relevance.equals("Optional")) {
              o_optionalvec.add(link);
              o_optionalvec.add(description);
              o_optionalvec.add(userstring);
              o_optionalvec.add(key);
          }
       }
%>

     <!-- mandatory table -->
     <td class="kropp">
     <br><%= bundle.getString("user_info") + userorg + "." %> 
     <br><br><%=bundle.getString("user_info2") %>
     
     <table border=1> <tr><th> <%= bundle.getString("tc_description") %> </th>
     <th> <%= bundle.getString("tc_value") + userorg%> </th></tr>
     <%
     n = mandatoryvec.size();
     for (int i = 0; i < n; i += 4) { 
       String link = (String) mandatoryvec.get(i);
       String description = (String) mandatoryvec.get(i+1);
       String userstring = (String) mandatoryvec.get(i+2);
       String key = (String) mandatoryvec.get(i+3);
       %>
       <tr>
         <td><%= description%><br><A HREF="<%= link %>" TARGET="_blank" class="attr"> <%= key %></A></td>
         <td><%= userstring %></td>
       </tr>
     <%}%>
     </table>
     <br><br><%=bundle.getString("user_table")%>
     
     <!-- optional table -->
     <table border=1> <tr><th> <%= bundle.getString("tc_description") %> </th>
     <th> <%= bundle.getString("tc_value") + userorg%> </th></tr>
     <%
     n = optionalvec.size();
     for (int i = 0; i < n; i += 4) {
       String link = (String) optionalvec.get(i);
       String description = (String) optionalvec.get(i+1);
       String userstring = (String) optionalvec.get(i+2);
       String key = (String) optionalvec.get(i+3);
       %> 
       <tr>
         <td><%= description %><br><A HREF="<%= link %>" TARGET="_blank" class="attr"> <%= key %></A></td>
         <td><%= userstring %></td>
       </tr>       
     <%}%>
     </table>
     
     <!-- Mandatory table for organizations -->
     <br><br><%= bundle.getString("org_info") + userorg + "." %>
        
     <table border=1> <tr><th> <%= bundle.getString("tc_description") %> </th>
     <th> <%= bundle.getString("tc_value") + userorg%> </th></tr>
     <%
     n = o_mandatoryvec.size();
     for (int i = 0; i < n; i += 4) { 
       String link = (String) o_mandatoryvec.get(i);
       String description = (String) o_mandatoryvec.get(i+1);
       String userstring = (String) o_mandatoryvec.get(i+2);
       String key = (String) o_mandatoryvec.get(i+3);
       %>
       <tr>
         <td><%= description %><br><A HREF="<%= link %>" TARGET="_blank" class="attr"> <%= key %></A></td>
         <td><%= userstring %></td>
       </tr>
     <%}%>
     </table>
     <br><br><%=bundle.getString("org_info2")%>
     
     <!-- Optional table for organizations -->
     <table border=1> <tr><th> <%= bundle.getString("tc_description") %> </th>
     <th> <%= bundle.getString("tc_value") + userorg%> </th></tr>
     <%
     n = o_optionalvec.size();
     for (int i = 0; i < n; i += 4) {
       String link = (String) o_optionalvec.get(i);
       String description = (String) o_optionalvec.get(i+1);
       String userstring = (String) o_optionalvec.get(i+2);
       String key = (String) o_optionalvec.get(i+3);
       %> 
       <tr>
         <td><%= description %><br><A HREF="<%= link %>" TARGET="_blank"> <%= key %></A></td>
         <td><%= userstring %></td>
       </tr>       
     <%}%>
     </table>
     
     </td>
</tr>
</tbody></table>
</div>
<%}%>    

<p>
<table summary="Layout-tabell" class="invers" border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr class="bunn" valign="middle">
<td class="invers" align="left"><small><a class="invers" href="mailto:<%=pconfig.get(RequestUtil.RESOURCE_MAIL)%>"><%=pconfig.get(RequestUtil.RESOURCE_MAIL)%></a></small></td>
<td class="invers" align="right"><small><%=pconfig.get(RequestUtil.RESOURCE_DATE)%></small></td>
</tr></tbody></table></p>
</body>
</html>
