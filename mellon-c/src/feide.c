/*
    feide.c: an API for authentication to Moria servers.
	Copyright (C) 2003 UNINETT (http://www.uninett.no/)

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

#include <stdlib.h>
#include <string.h>
#include <errno.h>

#include "feide.h"
#include "soapH.h"
#include "soapAuthentication.nsmap"

/*
 * f_translate_error:
 *   translates error codes from the gSOAP library into terms of libfeide
 *   error code.
 */
static int f_translate_error(int error)
{
	int f_error;

	switch (error) {
		case SOAP_OK:
			f_error = F_OK;
			break;

		case SOAP_CLI_FAULT:
		case SOAP_SVR_FAULT:
		case SOAP_MUSTUNDERSTAND:
		case SOAP_VERSIONMISMATCH:
			f_error = F_SERVER_ERROR;
			break;

		case SOAP_TAG_MISMATCH:
		case SOAP_TYPE_MISMATCH:
		case SOAP_SYNTAX_ERROR:
		case SOAP_NO_TAG:
		case SOAP_NAMESPACE:
			f_error = F_MESSAGE_ERROR;
			break;

		case SOAP_TCP_ERROR:
		case SOAP_HTTP_ERROR:
		case SOAP_SSL_ERROR:
			f_error = F_TRANSPORT_ERROR;
			break;

		case SOAP_EOM:
		case SOAP_IOB:
			f_error = F_MEMORY_ERROR;
			break;

		default:
			f_error = F_ERROR;
	}


	return f_error;
}


/* f_init:
 *   library configuration is stored in a struct that's given as an argument
 *   to each function, this for thread-safety.
 */
f_config *f_init(char *url, char *userid, char *passwd, int sso)
{
	f_config *config;

	if ((config = malloc(sizeof(f_config))) == NULL) {
		return NULL;
	}

	config->url = url;
	config->userid = userid;
	config->passwd = passwd;
	config->error_str = NULL;
	config->sso = sso;

	return config;
}


/* f_end:
 *   deallocates the memory f_init allocated.
 */
void f_end(f_config *config)
{
	free(config);
}


/* f_request_session:
 *   request a FEIDE session on behalf of a user and get an URL to redirect the
 *   user to for authentication. */
int f_request_session(f_config *config, char *attributes[], char *uprefix, char *upostfix, char **url)
{
	int i, result;
	struct soap soap;
	struct ArrayOfstring attrs;
	struct tns__requestSessionResponse response;

	soap_init(&soap);

	if (config->userid != NULL && config->passwd != NULL) {
		soap.userid = config->userid;
		soap.passwd = config->passwd;
	}

	if (attributes != NULL) {
		i = 0;
		while (attributes[i] != NULL) {
			i++;
		}

		attrs.__ptr = malloc(sizeof(char *) * i);
		attrs.__size = i;
		attrs.__offset = 0;

		if (attrs.__ptr == NULL) {
			return F_MEMORY_ERROR;
		}

		for (i = 0; attributes[i] != NULL && i < attrs.__size; i++) {
			attrs.__ptr[i] = strdup(attributes[i]);
		}
	} else {
		attrs.__ptr = NULL;
		attrs.__size = 0;
		attrs.__offset = 0;
	}

	result = soap_call_tns__requestSession(&soap,
	                                       config->url,
	                                       NULL,
	                                       &attrs,
	                                       uprefix,
	                                       upostfix,
	                                       config->sso,
	                                       &response);

	if (result != SOAP_OK) {
		/* some sort of error occured, we copy the error string from gSOAP
		 * into our "session" structure so that it can be extracted if the
		 * API user wants to.
		 */
		if (config->error_str != NULL) {
			free(config->error_str);
		}
		if (!*soap_faultstring(&soap)) {
			config->error_str = "";
		} else {
			config->error_str = strdup(*(soap_faultstring(&soap)));
		}
		return f_translate_error(result);
	}

	*url = strdup(response._result);

	for (i = 0; i < attrs.__size; i++) {
		free(attrs.__ptr[i]);
	}
	free(attrs.__ptr);

	soap_end(&soap);

	return F_OK;
}

/* f_get_attributes:
 *   get the set set of attributes that were requested in request_session. */
int f_get_attributes(f_config *config, char *id, f_attr_array **attributes)
{
	int i, j, result;
	struct soap soap;
	struct tns__getAttributesResponse r;
	f_attr_array *a;

	soap_init(&soap);

	/* configure for HTTP Basic authentication */
	if (config->userid != NULL && config->passwd != NULL) {
		soap.userid = config->userid;
		soap.passwd = config->passwd;
	}

	result = soap_call_tns__getAttributes(&soap, config->url, NULL, id, &r);

	/*
     * do utterly basic error handling, and attempt to write the error string
     * from gSOAP into config->error_str.
     */
	if (result != SOAP_OK) {
		if (config->error_str != NULL) {
			free(config->error_str);
		}
		if (!*soap_faultstring(&soap)) {
			config->error_str = "";
		} else {
			config->error_str = strdup(*(soap_faultstring(&soap)));
		}
		*attributes = NULL;
		return f_translate_error(result);
	}

	a = malloc(sizeof(f_attr_array));
	if (a == NULL) {
		return F_MEMORY_ERROR;
	}

	/*
	 * copy the resulting attributes from the response structure of gSOAP
	 * to the f_attr_array that we return to the user.
	 */
	a->size = r._result->__size;
	a->attributes = malloc(sizeof(f_attr) * a->size);
	if (a->attributes == NULL) {
		return F_MEMORY_ERROR;
	}
	for (i = 0; i < a->size; i++) {
		a->attributes[i].name = strdup(r._result->__ptr[i].name);
		a->attributes[i].size = r._result->__ptr[i].values->__size;
		a->attributes[i].values = malloc(sizeof(char *) * a->attributes[i].size);
		if (a->attributes[i].values == NULL) {
			return F_MEMORY_ERROR;
		}
		for (j = 0; j < a->attributes[i].size; j++) {
			a->attributes[i].values[j] = strdup(r._result->__ptr[i].values->__ptr[j]);
		}
	}

	*attributes = a;

	soap_end(&soap);

	return F_OK;
}


/* frees a f_attr_array structure from memory, so the user does not really
 * have to worry about it. */
void f_free_attributes(f_attr_array *a)
{
	int i, j;

	for (i = 0; i < a->size; i++) {
		for (j = 0; j < a->attributes[i].size; j++) {
			free(a->attributes[i].values[j]);
		}
		free(a->attributes[i].name);
	}
	free(a);
}
