<%@ page import="no.feide.moria.servlet.RequestUtil,
                 java.util.ResourceBundle,
                 java.util.TreeMap,
                 java.util.Iterator" %>
<%ResourceBundle bundle = (ResourceBundle) request.getAttribute("bundle");%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title><%=bundle.getString("header_title")%></title>
<style type="text/css">
  body {font-family: verdana, arial;}
  input {font-family: verdana, arial;}
</style>
<script type="text/javascript" language="JavaScript">
<!--
function fokuser(){document.loginform.username.focus();}
// -->
</script>
</head>
<body onload="fokuser()">

<table summary="" cellpadding="5" cellspacing="5" border="0" width="100%">
  <tr valign="top">
    <td width="5%" rowspan="3">&nbsp;</td>
    <td colspan="2">
      <table summary="" cellspacing="0" cellpadding="0" border="0" width="100%">
      <tr>
        <td colspan="2" style="text-align:right">
<!-- TODO: Only show language selection if the ticket is valid -->
<font size="-1">
        <%
        TreeMap languages = (TreeMap) request.getAttribute("languages");
        Iterator it = languages.keySet().iterator();
        while(it.hasNext()) {
            String longName = (String) it.next();
            String shortName  = (String) languages.get(longName);
            if (request.getAttribute("selectedLang").equals(shortName)) {%>
                [<%=longName%>]
            <%} else {%>
                <A href="<%= request.getAttribute("baseURL") + "&lang=" + shortName %>"><%=longName%></A>
            <%}%>
        <%}%>
</font>

	</td>
      </tr>

        <tr>
          <td valign="middle">&nbsp;
            <a href="http://www.feide.no/"><img src="/element/feidelogo.png" alt="FEIDE-logo" border="0"/></a>
          </td>
          <td valign="middle">
            <table summary="" cellspacing="0" cellpadding="0" border="0" >
              <tr>
                <td valign="top">-&nbsp;</td>
                <td><font size="-1"><b><%=bundle.getString("body_title")%></b><br>

</font></td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
    <td width="5%" rowspan="2">&nbsp;</td>
  </tr>

<% if (request.getAttribute("errorType") != null) { %>
  <tr>
   <td colspan="2">
   <table summary="" cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
    <td valign="top">
    <img src="/element/varseltrekant.png" alt="feilmelding"/>
    </td>
    <td valign="middle" width="99%">
    <b><%=bundle.getString("error_" + (String) request.getAttribute("errorType"))%></b><br/>
    <i><%=bundle.getString("error_" + (String) request.getAttribute("errorType") + "_desc")%></i>

    </td>
    </tr>
    </table>
    </td>
   </tr>
<%}%>

<% if (request.getAttribute("errorType") == null || !request.getAttribute("errorType").equals("unknownTicket")) { %>
  <tr valign="top">
    <td width="20%">
      <table summary="" cellpadding="7" cellspacing="0" border="0" bgcolor="#EEEEFF">
        <tr>
          <td>
            <form action="<%= request.getAttribute("baseURL")%>" method="POST" name="loginform" autocomplete="off">
              <table summary="" cellpadding="3" cellspacing="3" border="0" bgcolor="#EEEEFF">
                <tbody>

                  <tr>
                    <td align="left" nowrap ="nowrap">
		            <%=bundle.getString("form_username")%><br/>
                    <input type="text" size="16" value="" name="username"></td>
                  </tr>
                  <tr>
                    <td>
                      <%=bundle.getString("form_password")%><br>
                      <input type="password" size="16" value="" name="password">
                    </td>
                  </tr>

		<tr>
		<td><%=bundle.getString("form_org")%><br>
		<select name="org">
		<option value="null"><%=bundle.getString("form_selectOrg")%></option>
        <%
        TreeMap orgNames = (TreeMap) request.getAttribute("organizationNames");
        it = orgNames.keySet().iterator();
        while(it.hasNext()) {
            String longName = (String) it.next();
            String shortName  = (String) orgNames.get(longName);
        %>
	    <option <%if (request.getAttribute("selectedOrg").equals(shortName)) {%>selected=="true" <%}%>value="<%=shortName%>"><%=longName%></option>
        <%}%>
		</select>
		</td>
		</tr>



                  <tr>
                    <td colspan="3" valign="top">
                    <input type="submit" value="<%=bundle.getString("form_login")%>">
                    </td>
                  </tr>
                </tbody>
              </table>
            </form>
          </td>
        </tr>
      </table>

    </td>
    <td width="70%">
      <table summary="" cellspacing="3" cellpadding="3" border="0" >
      <tr valign="top">
        <td><img src="/element/listepunkt.gif" alt="-"/></td>
        <td><%=RequestUtil.insertLink("CLIENT_LINK",
                                         bundle.getString("expl_ws"),
                                         (String) request.getAttribute("clientName"),
                                         (String) request.getAttribute("clientURL"))%></td>
      </tr>
      <tr valign="top">
        <td><img src="/element/listepunkt.gif" alt="-"/></td>
        <td><%=bundle.getString("expl_user")%></td>
      </tr>
      <tr valign="top">
        <td><img src="/element/listepunkt.gif" alt="-"/></td>
        <td><%=RequestUtil.insertLink("CLIENT_LINK",
                                         bundle.getString("expl_data_"+request.getAttribute("secLevel")),
                                         (String) request.getAttribute("clientName"),
                                         (String) request.getAttribute("clientURL"))%><BR/>


       </td>
      </tr>
      <tr>
        <td>&nbsp;</td>
        <td><hr noshade="noshade" size="1"/></td>
      </tr>
      <tr valign="top">
        <td>&nbsp;</td>
        <td>
        <a href="http://www.feide.no/moria/doc/user/faq.html"><%=bundle.getString("faq")%></a><img src="/element/emblemS.png" border="0" valign="middle" ALT="Arrow" />
	</td>
      </tr>
    </table>
  </td>

<%} else {%>

<td><a href="http://www.feide.no/moria/doc/user/faq.html"><%=bundle.getString("faq")%></a><img src="/element/emblemS.png" border="0" valign="middle" ALT="Arrow" /></td>
<%}%>


</tr>
</table>
</body>
</html>