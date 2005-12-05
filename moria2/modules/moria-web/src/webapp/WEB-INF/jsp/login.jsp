<%@ page
			language="java"
			errorPage="/Error"
			session="false"
			contentType="text/html; charset=ISO-8859-1"
			pageEncoding="ISO-8859-1"
			import="no.feide.moria.servlet.RequestUtil,
                 java.util.ResourceBundle,
                 java.util.TreeMap,
                 java.util.Iterator, 
                 java.util.Properties" %>
<%ResourceBundle bundle = (ResourceBundle) request.getAttribute("bundle");

Properties pconfig;
try {
  pconfig = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
} catch (IllegalStateException e) {
  pconfig = null;
}%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="icon" href="../resource/favicon.ico" type="image/png">
<style type="text/css">
@import url("../resource/stil.css");
</style>
<link rel="author" href="mailto:<%=pconfig.get(RequestUtil.RESOURCE_MAIL)%>">
<title><%=bundle.getString("header_title")%></title>
<script type="text/javascript" language="JavaScript">
<!--
function fokuser(){document.loginform.username.focus();}
// -->
</script>
</head>
<body onload="fokuser()">

<table summary="Layout-tabell" class="invers" border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="middle">
<td class="logo" width="76"><a href="<%=pconfig.get(RequestUtil.RESOURCE_LINK)%>"><img src="../resource/logo.gif" alt="<%=pconfig.get(RequestUtil.PROP_FAQ_OWNER)%>" border="0" height="41" width="76"></a></td>
<td width="0%"><a class="noline" href="<%=pconfig.get(RequestUtil.RESOURCE_LINK)%>"><%=bundle.getString("header_feide")%></a></td>
<td width="35%">&nbsp;<td>

<!-- Language selection -->
<%
        TreeMap languages = (TreeMap) request.getAttribute(RequestUtil.ATTR_LANGUAGES);
        Iterator it = languages.keySet().iterator();
        while(it.hasNext()) {
            String longName = (String) it.next();
            String shortName  = (String) languages.get(longName);
            if (request.getAttribute(RequestUtil.ATTR_SELECTED_LANG).equals(shortName)) {%>
                [<%=longName%>]
            <%} else {%>
                <td align="centre"><small> <a class="invers" href="<%= request.getAttribute(RequestUtil.ATTR_BASE_URL) + "&"+RequestUtil.PARAM_LANG+"=" + shortName %>">[<%=longName%>]</a> </small></td>
            <%}%>
        <%}%>
<td class="dekor1" width="100%">&nbsp;</td>
</tr></tbody></table> 
<!-- This is the end of the FEIDE header - currently a red band with logo inserted into it -->

<div class="midt">

<table cellspacing="0" border="0"><!--This is the start of the page content --> 
<tbody>
<td class="kropp">
        <tr>
           <td valign="middle">
            <table summary="" cellspacing="0" cellpadding="0" border="0"><!-- This is a table around the "Moria-description" -->
              <tr>
                <td valign="top"><font size="+3">Moria</font> -&nbsp;</td>
                <td><font size="-1"><b><%=bundle.getString("body_title")%></b><br>
				</font></td>
              </tr>
            </table> <!-- This is the end of the "Moria"-description table -->
          </td>
        </tr>
    </td>

<% if (request.getAttribute(RequestUtil.ATTR_ERROR_TYPE) != null) { %>
<!--We have an error situation-->
  <tr>
   <td colspan="2">
   <table summary="" cellpadding="0" cellspacing="0" border="0" width="100%"> <!--Table for error message and description-->
    <tr>
    <td valign="top">
        </td>
    <td valign="middle" width="99%">
    <b><%=bundle.getString("error_" + (String) request.getAttribute(RequestUtil.ATTR_ERROR_TYPE))%></b><br/>
    <i><%=bundle.getString("error_" + (String) request.getAttribute(RequestUtil.ATTR_ERROR_TYPE) + "_desc")%></i>

    <p><%=bundle.getString("error_commonString")%></p>    

    </td>
    </tr>
    </table> <!-- Error msg table end -->
    </td>
   </tr>
<%}%>

<% String errorType = (String) request.getAttribute(RequestUtil.ATTR_ERROR_TYPE);
   if (errorType == null || !(errorType.equals(RequestUtil.ERROR_UNKNOWN_TICKET) || errorType.equals(RequestUtil.ERROR_MORIA_DOWN))) { %>
   <!--No error, or an error situation where we can try again-->
  <tr valign="top">
    <td width="20%">
      <table summary="" cellpadding="7" cellspacing="0" border="0" bgcolor="#EEEEFF"> <!--This is the table for the login form -->
        <tr>
          <td>
            <form action="<%= request.getAttribute(RequestUtil.ATTR_BASE_URL)%>" method="POST" name="loginform" autocomplete="off">
              <table summary="" cellpadding="3" cellspacing="3" border="0" bgcolor="#EEEEFF"> <!--This is a table for the fields of the form-->
                <tbody>

                  <tr>
                    <td align="left" nowrap ="nowrap">
		            <%=bundle.getString("form_username")%><br/>
                    <input type="text" size="16" value="" name="<%= RequestUtil.PARAM_USERNAME %>" autocomplete="off"></td>
                  </tr>
                  <tr>
                    <td>
                      <%=bundle.getString("form_password")%><br>
                      <input type="password" size="16" value="" name="<%= RequestUtil.PARAM_PASSWORD %>" autocomplete="off">
                    </td>
                  </tr>

		<tr>
		<td><%=bundle.getString("form_org")%><br>
		<select name="org">
		<option value="null"><%=bundle.getString("form_selectOrg")%></option>
        <%
        TreeMap orgNames = (TreeMap) request.getAttribute(RequestUtil.ATTR_ORGANIZATIONS);
        it = orgNames.keySet().iterator();
        while(it.hasNext()) {
            String longName = (String) it.next();
            String shortName  = (String) orgNames.get(longName);
        %>
	    <option <%if (request.getAttribute(RequestUtil.ATTR_SELECTED_ORG) != null && request.getAttribute(RequestUtil.ATTR_SELECTED_ORG).equals(shortName)) {%>selected=="true" <%}%>value="<%=shortName%>"><%=longName%></option>
        <%}%>
		</select>
		</td>
		</tr>
                          <tr>
                            <td>
                                <input type="checkbox" <% if (((Boolean)request.getAttribute(RequestUtil.ATTR_SELECTED_DENYSSO)).booleanValue()) {%> CHECKED <%}%>" value="true" name="<%= RequestUtil.PARAM_DENYSSO %>">
                                <font size="-2"><%=bundle.getString("form_denySSO")%></font><br>
                            </td>
                          </tr>



                  <tr>
                    <td colspan="3" valign="top">
                    <input type="submit" value="<%=bundle.getString("form_login")%>">
                    </td>
                  </tr>
                </tbody>
              </table> <!--This ends the table for the fields  -->
            </form>
          </td>
        </tr>
      </table> <!-- This ends the entire login form -->

    </td>
    <td width="70%">
      <table summary="" cellspacing="3" cellpadding="3" border="0" > <!-- This is the table for the explanations -->
      <tr valign="top">
        <td>-</td>
        <td><%=RequestUtil.insertLink("CLIENT_LINK",
                                         bundle.getString("expl_ws"),
                                         (String) request.getAttribute(RequestUtil.ATTR_CLIENT_NAME),
                                         (String) request.getAttribute(RequestUtil.ATTR_CLIENT_URL))%></td>
      </tr>
      <tr valign="top">
        <td>-</td>
        <td><%=bundle.getString("expl_user")%></td>
      </tr>
      <tr valign="top">
        <td>-</td>
        <td><%=RequestUtil.insertLink("CLIENT_LINK",
                                         bundle.getString("expl_data_"+request.getAttribute(RequestUtil.ATTR_SEC_LEVEL)),
                                         (String) request.getAttribute(RequestUtil.ATTR_CLIENT_NAME),
                                         (String) request.getAttribute(RequestUtil.ATTR_CLIENT_URL))%><br/>


       </td>
      </tr>
      <tr>
            <td>-</td>
        <td><%=bundle.getString("expl_link1")%> <a href=<%=request.getAttribute("faqlink")%>><%=bundle.getString("expl_link2")%></a> <%=bundle.getString("expl_link3")%><br/>

       </td>
       <tr>
            <td>&nbsp;</td>
        <td><% String showAttributes = (String)request.getAttribute(RequestUtil.PARAM_SHOW_ATTRS);
        if (showAttributes.equals("false")) { %>
        <a href="<%=request.getAttribute(RequestUtil.ATTR_BASE_URL) + "&" + RequestUtil.PARAM_SHOW_ATTRS + "=true" %>"><%=bundle.getString("expl_link4")%></a>
        <%
        } else if (showAttributes.equals("true")) {
        %>
        <ul>
        <%
        String[] attributes = (String[]) request.getAttribute(RequestUtil.ATTR_REQUESTED_ATTRIBUTES);
        for (int i = 0; i < attributes.length; i++) {
        String attr;
        try {
                attr = bundle.getString(attributes[i]);
                } catch (Exception e) {
                attr = attributes[i];
        }%><li><%=attr%>
        <%}
        }%>
       </ul>
       </td>
      </tr>
         
      <tr>
        <td>&nbsp;</td>
        <td><hr noshade="noshade" size="1"/></td>
      </tr>
      <tr valign="top">
        <td>&nbsp;</td>
        <td>
	</td>
      </tr>
    </table> <!-- This is the end of the explanation table -->
  </td>

<%} else {%>

<td></td>
<%}%>


</tr>
</table> <!-- This is the end of the page content table -->
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