<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
	  "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" >
  <head>
    <title>Moria Web Service</title>
  </head>
  <% String wsdlURL = request.getContextPath() + request.getAttribute("serviceName") + "?wsdl"; %>
  <body>
    <p>
      <a href="<%= wsdlURL %>">WSDL</a>
    </p>
  </body>
</html>
