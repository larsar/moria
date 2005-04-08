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

<div class="midt">
<table cellspacing="0">
<tbody><tr valign="top">
<td class="kropp">
     <h3>
			<%=bundle.getString("general_message")%>
    </h3>

<%
  Class eclass = exception.getClass();
    
  if (eclass == AuthenticationException.class) { %>
    <p><%=bundle.getString("error_authentication")%></p><br/> <%;
  }
  else if (eclass == AuthorizationException.class) { %>
    <p><%=bundle.getString("error_authorization")%></p><br/><%;
  }
  else if (eclass == DirectoryUnavailableException.class) { %>
    <p><%=bundle.getString("error_directory")%></p><br/>;<%
  }
  else if (eclass == IllegalInputException.class) { %>
    <p><%=bundle.getString("error_illegalinput")%></p><br/><%;
  }
  else if (eclass == InoperableStateException.class) { %>
    <p><%=bundle.getString("error_inoperable")%></p><br/><%;
  }
  else if (eclass == MoriaControllerException.class) { %>
    <p><%=bundle.getString("error_moriacontroller")%></p><br/><%;
  }
  else if (eclass == UnknownTicketException.class) {%>
    <p><%=bundle.getString("error_unknownticket")%></p><br/><%;
  }
  else if (eclass == IllegalArgumentException.class) {%>
    <p><%=bundle.getString("error_illegalargument")%></p><br/><%;
  }
  else if (eclass == NullPointerException.class) {%>
    <p><%=bundle.getString("error_nullpointer")%></p><br/><%;
  }
  else if (eclass == IllegalStateException.class) {%>
    <p><%=bundle.getString("error_illegalstate")%></p><br/><%;
  }
  else if (eclass == ServletException.class) {
  ServletException se = (ServletException) exception;
  Throwable servletThrowable = se.getRootCause();
  	if (servletThrowable.getClass() == AuthenticationException.class) { %>
		<p><%=bundle.getString("error_authentication")%></p><br/><%;
  	}
  	else if (servletThrowable.getClass() == AuthorizationException.class) { %>
		<p><%=bundle.getString("error_authorization")%></p><br/><%;
	}
	else if (servletThrowable.getClass() == DirectoryUnavailableException.class) { %>
		<p><%=bundle.getString("error_directory")%></p><br/><%;
	}
	else if (servletThrowable.getClass() == IllegalInputException.class) { %>
		<p><%=bundle.getString("error_illegalinput")%></p><br/><%;
	}
	else if (servletThrowable.getClass() == InoperableStateException.class) { %>
		<p><%=bundle.getString("error_inoperable")%></p><br/><%;
	}
	else if (servletThrowable.getClass() == MoriaControllerException.class) { %>
		<p><%=bundle.getString("error_moriacontroller")%></p><br/><%;
	}
	else if (servletThrowable.getClass() == UnknownTicketException.class) { %>
		<p><%=bundle.getString("error_unknownticket")%></p><br/><%;
	}
	else if (servletThrowable.getClass() == IllegalArgumentException.class) { %>
		<p><%=bundle.getString("error_illegalargument")%></p><br/><%;
	}
	else if (servletThrowable.getClass() == NullPointerException.class) { %>
		<p><%=bundle.getString("error_nullpointer")%></p><br/><%;
	}
	else if (servletThrowable.getClass() == IllegalStateException.class) {%>
    	<p><%=bundle.getString("error_illegalstate")%></p><br/><%;
    }
	}
  else { %>
   <p><%=bundle.getString("error_rest")%></p><br/><%;
   }
   %>

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
