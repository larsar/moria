<?xml version="1.0" encoding="ISO-8859-1"?>

<%@ page
			language="java"
			isErrorPage="true"
			session="false" 
			contentType="text/html; charset=ISO-8859-1" 
			pageEncoding="ISO-8859-1" 
			import="java.util.ResourceBundle, java.util.Properties, no.feide.moria.servlet.RequestUtil, no.feide.moria.controller.*" %>


<% response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE); %>

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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
		"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%= bundle.getLocale() %>" >
  <head>
		<title><%=bundle.getString("header_title")%></title>
  </head>
  <body>
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
	}
  else { %>
   <p><%=bundle.getString("error_rest")%></p><br/><%;
   }
   %>

  </body>
</html>
