/* soapClient.cpp
   Generated by gSOAP 2.2.3b from Authentication.h
   Copyright (C) 2001-2003 Genivia inc.
   All Rights Reserved.
*/
#include "soapH.h"

SOAP_SOURCE_STAMP("@(#) soapClient.cpp ver 2.2.3b 2003-06-12 15:43:33 GMT")


SOAP_FMAC1 int SOAP_FMAC2 soap_call_tns__requestSession(struct soap *soap, const char *URL, const char *action, struct ArrayOfstring *attributes, char *prefix, char *postfix, enum Enum_ denySSO, struct tns__requestSessionResponse *out)
{
	struct tns__requestSession soap_tmp_tns__requestSession;
	if (!action)
		action = "";
	soap_tmp_tns__requestSession.attributes=attributes;
	soap_tmp_tns__requestSession.prefix=prefix;
	soap_tmp_tns__requestSession.postfix=postfix;
	soap_tmp_tns__requestSession.denySSO=denySSO;
	soap_begin(soap);
	soap_serializeheader(soap);
	soap_serialize_tns__requestSession(soap, &soap_tmp_tns__requestSession);
	soap_begin_count(soap);
	if (soap->mode & SOAP_IO_LENGTH)
	{	soap_envelope_begin_out(soap);
		soap_putheader(soap);
		soap_body_begin_out(soap);
		soap_put_tns__requestSession(soap, &soap_tmp_tns__requestSession, "tns:requestSession", "");
		soap_body_end_out(soap);
		soap_envelope_end_out(soap);
	}
	if (soap_connect(soap, URL, action)
	 || soap_envelope_begin_out(soap)
	 || soap_putheader(soap)
	 || soap_body_begin_out(soap)
	 || soap_put_tns__requestSession(soap, &soap_tmp_tns__requestSession, "tns:requestSession", "")
	 || soap_body_end_out(soap)
	 || soap_envelope_end_out(soap)
	 || soap_putattachments(soap)
	 || soap_end_send(soap))
		return soap->error;
	soap_default_tns__requestSessionResponse(soap, out);
	if (soap_begin_recv(soap)
	 || soap_envelope_begin_in(soap)
	 || soap_recv_header(soap)
	 || soap_body_begin_in(soap))
		return soap->error;
	soap_get_tns__requestSessionResponse(soap, out, "tns:requestSessionResponse", "tns:requestSessionResponse");
	if (soap->error)
	{	if (soap->error == SOAP_TAG_MISMATCH && soap->level == 2)
			soap_recv_fault(soap);
		return soap->error;
	}
	if (soap_body_end_in(soap)
	 || soap_envelope_end_in(soap)
	 || soap_getattachments(soap)
	 || soap_end_recv(soap))
		return soap->error;
	soap_closesock(soap);
	return SOAP_OK;
}

SOAP_FMAC1 int SOAP_FMAC2 soap_call_tns__getAttributes(struct soap *soap, const char *URL, const char *action, char *sessionId, struct tns__getAttributesResponse *out)
{
	struct tns__getAttributes soap_tmp_tns__getAttributes;
	if (!action)
		action = "";
	soap_tmp_tns__getAttributes.sessionId=sessionId;
	soap_begin(soap);
	soap_serializeheader(soap);
	soap_serialize_tns__getAttributes(soap, &soap_tmp_tns__getAttributes);
	soap_begin_count(soap);
	if (soap->mode & SOAP_IO_LENGTH)
	{	soap_envelope_begin_out(soap);
		soap_putheader(soap);
		soap_body_begin_out(soap);
		soap_put_tns__getAttributes(soap, &soap_tmp_tns__getAttributes, "tns:getAttributes", "");
		soap_body_end_out(soap);
		soap_envelope_end_out(soap);
	}
	if (soap_connect(soap, URL, action)
	 || soap_envelope_begin_out(soap)
	 || soap_putheader(soap)
	 || soap_body_begin_out(soap)
	 || soap_put_tns__getAttributes(soap, &soap_tmp_tns__getAttributes, "tns:getAttributes", "")
	 || soap_body_end_out(soap)
	 || soap_envelope_end_out(soap)
	 || soap_putattachments(soap)
	 || soap_end_send(soap))
		return soap->error;
	soap_default_tns__getAttributesResponse(soap, out);
	if (soap_begin_recv(soap)
	 || soap_envelope_begin_in(soap)
	 || soap_recv_header(soap)
	 || soap_body_begin_in(soap))
		return soap->error;
	soap_get_tns__getAttributesResponse(soap, out, "tns:getAttributesResponse", "tns:getAttributesResponse");
	if (soap->error)
	{	if (soap->error == SOAP_TAG_MISMATCH && soap->level == 2)
			soap_recv_fault(soap);
		return soap->error;
	}
	if (soap_body_end_in(soap)
	 || soap_envelope_end_in(soap)
	 || soap_getattachments(soap)
	 || soap_end_recv(soap))
		return soap->error;
	soap_closesock(soap);
	return SOAP_OK;
}

/* end of soapClient.cpp */
