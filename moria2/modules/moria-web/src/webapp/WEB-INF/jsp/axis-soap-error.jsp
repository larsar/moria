<?xml version="1.0" encoding="UTF-8"?>

<%@ page
			language="java"
			errorPage="/Error"
			session="false"
			contentType="text/xml; charset=UTF-8"
			pageEncoding="UTF-8" %>

<SOAP-ENV:Envelope
	xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
	<SOAP-ENV:Body>
		<SOAP-ENV:Fault>
			<faultcode><%= request.getAttribute("faultCode") %></faultcode>
			<faultstring><%= request.getAttribute("faultString") %></faultstring>
	  </SOAP-ENV:Fault>
	</SOAP-ENV:Body>	
</SOAP-ENV:Envelope>
