<?xml version="1.0" encoding="ISO-8859-1"?>

<%@ page
			language="java"
			errorPage="/Error"
			session="false"
			contentType="text/html; charset=ISO-8859-1"
			pageEncoding="ISO-8859-1"
			import="java.util.ResourceBundle, java.util.Vector, no.feide.moria.servlet.RequestUtil,
			java.util.TreeMap, java.util.Iterator" %>

<% ResourceBundle bundle = (ResourceBundle) request.getAttribute("bundle"); %>
<% Vector tabledata = (Vector) request.getAttribute("tabledata"); %>
<% String userorg = (String) request.getAttribute("userorg"); %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
	  "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%= bundle.getLocale() %>" >
<head>
  <title><%= bundle.getString("header_title") %></title>
</head>
<body>
  
<!-- Do we have table data (attributes) to show? -->	
<%if (tabledata != null) {%>
    <% if (!(((Boolean)request.getAttribute(RequestUtil.ATTR_SELECTED_DENYSSO)).booleanValue())) { %>
       <!-- Language selection -->  
 	<table summary="" cellspacing="0" cellpadding="0" border="0" width="100%">
      <tr>
        <td colspan="2" style="text-align:right">
		<font size="-1">
        <%
        TreeMap languages = (TreeMap) request.getAttribute(RequestUtil.ATTR_LANGUAGES);
        Iterator it = languages.keySet().iterator();
        while(it.hasNext()) {
            String longName = (String) it.next();
            String shortName  = (String) languages.get(longName);
            if (request.getAttribute(RequestUtil.ATTR_SELECTED_LANG).equals(shortName)) {%>
                [<%=longName%>]
            <%} else {%>
                <A href="<%= request.getAttribute(RequestUtil.ATTR_BASE_URL) + "&"+RequestUtil.PARAM_LANG+"=" + shortName %>"><%=longName%></A>
            <%}%>
        <%}%>
       </font>
	  </td>
    </tr>  
    </table>
    
    <% } %> <!-- end of denySSO test -->
   
   <!-- Logout-->
   <A href="<%= request.getAttribute(RequestUtil.ATTR_BASE_URL) + "&logout=user_logout" %>"><%=bundle.getString("user_logout")%></A>
  

    <br/>
    <br/>    
      
     <p>
     <b><center><%= bundle.getString("user_info") %> </b>
     </p>
     <!-- Show table with attributes, values and relevance -->
        <p><table border=1> <tr><th> <%= bundle.getString("tc_description") %> </th>
       	<th> <%= bundle.getString("tc_value") %> </th>
       	<th> <%= bundle.getString("tc_relevance") %> </th></tr>
		<% 
        final int n = tabledata.size();
        for (int i = 0; i < n; i += 4) {
          String link = (String) tabledata.get(i);
          String description = (String) tabledata.get(i+1);
          String userstring = (String) tabledata.get(i+2);
          String relevance = (String) tabledata.get(i+3);
          
          if (userstring == null || userstring.equals("")) {	
		    if (relevance.equals("fd_mandatory")) {
                userstring = "<FONT COLOR=\"#ff0000\">" + bundle.getString("m_missing") + " " + userorg + "</FONT>";
            }
            else {
                userstring = bundle.getString("o_missing")+ " "  + userorg;
            }
          }
          relevance = bundle.getString(relevance);
		%>
          <tr>
            <td align=left><A HREF="<%= link %>"> <%= description %></A></td>
            <td align=center><%= userstring %></td>
            <td align=center><%= relevance %></td>
          </tr>
        <% } %>        
        </table></center>
    </p>

<%} %>
    
</body>
</html>
