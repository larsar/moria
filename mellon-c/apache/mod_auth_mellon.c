/*

    mod_auth_mellon.c: an authentication apache module
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

#include "apr_time.h"
#include "apr_strings.h"
#include "apr_shm.h"
#include "apr_md5.h"

#include "ap_config.h"
#include "httpd.h"
#include "http_config.h"
#include "http_core.h"
#include "http_log.h"
#include "http_protocol.h"
#include "http_request.h"

#include "mellon.h"

module AP_MODULE_DECLARE_DATA auth_mellon_module;

#define MF_CACHE_KEYSIZE 120

/*
 * all the information required to store an authentication hash inside
 * the module.  this means we demand support for shared memory!
 */
typedef struct {
	char key[MF_CACHE_KEYSIZE];
	apr_time_t access;
} mf_cache_entry_t;


/*
 * finds the matching element in the table, if it exists.  if it does
 * it returns the difference in seconds between the authentication
 * and now.
 */
static int mf_cache_find(mf_cache_entry_t *table, int size, char *key)
{
	int i;

	for (i = 0; i < size; i++) {
		if (strcmp(table[i].key, key) == 0) {
			apr_time_t interval;
			interval = apr_time_sec(apr_time_now() - table[i].access);
			/* renew 'lease' of the key */
			table[i].access = apr_time_now();
			return interval;
		}
	}

	return -1;
}


/*
 * sets the key into the table, and makes room for it if the cache is
 * full.
 */
static void mf_cache_set(mf_cache_entry_t *table, int size, char *key)
{
	int i;

	if (table == NULL || key == NULL) {
		return;
	}

	/* first attempt to find an empty spot */
	for (i = 0; i < size; i++) {
		if (table[i].access == 0)
			break;
	}

	/* if there is no room, then kick out the oldest element */
	if (table[i].key[0] != '\0') {
		int oldest_index = 0;
		for (i = 0; i < size; i++) {
			if (table[i].access < table[oldest_index].access) {
				oldest_index = i;
			}
		}
		i = oldest_index;
	}

	/* update table element */
	strcpy(table[i].key, key);
	table[i].access = apr_time_now();

	return;
}


/* parse error here for some silly reason */
static char *mf_cache_genkey(apr_pool_t *pool, char *id, char *domain, char *ip)
{
	char hash[MF_CACHE_KEYSIZE];
	char *tmp;

	tmp = apr_pstrcat(pool, id, ip, domain, NULL);

	if (apr_md5_encode(tmp, "ab", hash, strlen(tmp)) == 0) {
		return apr_pstrdup(pool, hash);
	} else {
		return NULL;
	}
}


/*
 * all this is for the MellonRequire configuration command.  we want each
 * configuration argument on the same line to be connected with an OR and
 * those on different lines to be connected with an AND.  this requires the
 * use of 'raw' processing of the configuration line and the following data
 * structure, which mostly imitates the apr_array.
 */
typedef struct requirement_list_ {
	int done;
	int nelms;
	int nalloc;
	char **elms;
	struct requirement_list_ *next;
	apr_pool_t *pool;
} mf_reqlist_t;


/*
 * creates a new instance of mf_reqlist_t which is initialised.
 */
static mf_reqlist_t *mf_reqlist_make(apr_pool_t *pool, int nalloc)
{
	mf_reqlist_t *nrl;

	nrl = apr_palloc(pool, sizeof(mf_reqlist_t));
	nrl->elms = apr_palloc(pool, sizeof(char *) * nalloc);
	nrl->nelms = 0;
	nrl->nalloc = nalloc;
	nrl->next = NULL;
	nrl->pool = pool;
	nrl->done = 0;
	return nrl;
}


/*
 * pushes a new value onto the the reqlist.
 */
static int mf_reqlist_push(mf_reqlist_t *rl, char *value)
{
	if (rl->nelms == rl->nalloc) {
		int    new_size = (rl->nalloc <= 0) ? 1 : rl->nalloc * 2;
		char **new_data;

		new_data = apr_palloc(rl->pool, new_size);
		memcpy(new_data, rl->elms,
		       rl->nelms * sizeof(char *));
		memset(new_data + rl->nalloc * sizeof(char *), 0,
		       sizeof(char *) * (new_size - rl->nalloc));
		rl->elms = new_data;
		rl->nalloc = new_size;
	}

	rl->elms[rl->nelms] = value;
	return ++rl->nelms;
}


/* 
 * struct to hold configuration info
 */
typedef struct {
	char *varname;
	char *moria_url;
	char *userid;
	char *passwd;
	char *domain;
	apr_hash_t *require;
} auth_mellon_config_rec;


/*
 * struct to hold server configuration info.
 */
typedef struct {
	int nelms;
	int cache_age;
	apr_shm_t *cache;
	char *cache_name;
} auth_mellon_server_rec;


/*
 * initalizes a configuration structure for use
 */
static void *create_auth_dir_config(apr_pool_t *p, char *d)
{
	auth_mellon_config_rec *conf = apr_palloc(p, sizeof(*conf));

	conf->varname = NULL;
	conf->moria_url = NULL;
	conf->userid = NULL;
	conf->passwd = NULL;
	conf->domain = NULL;
	conf->require = apr_hash_make(p);
	return conf;
}


/*
 * intialize a server configuration structure
 */
static void *create_auth_server_config(apr_pool_t *p, server_rec *s)
{
	auth_mellon_server_rec *cfg;

	cfg = apr_palloc(p, sizeof(*cfg));
	cfg->nelms = 100;  /* number of authorizations to cache */
	cfg->cache = NULL; /* will be allocated later */
	cfg->cache_age = 300; /* default to five minutes */

	return cfg;
}


/*
 * a function to set different fields in the configuration structure
 */
static const char *set_mellon_slot(cmd_parms *cmd, void *conf,
                                  const char *f, const char *t)
{
	if (t && strcmp(t, "mellon")) {
		return DECLINE_CMD;
	}
	return ap_set_string_slot(cmd, conf, f);
}


/*
 * read the attribute requirements for authentication and put them in
 * the 'require' hash table.
 */
static const char *set_require_slot(cmd_parms *cmd, void *conf_,
                                    const char *args)
{
	mf_reqlist_t *reqlist;
	auth_mellon_config_rec *cfg = conf_;
	char *attribute;
	char *value;

	attribute = ap_getword_conf(cmd->pool, &args);
	value     = ap_getword_conf(cmd->pool, &args);

	if (*attribute == '\0' || value == '\0') {
		return apr_pstrcat(cmd->pool, cmd->cmd->name, " takes two arguments",
		                   NULL);
	}

	do {
		int nr;

		ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, cmd->server,
		             "%s=%s", attribute, value);

		reqlist = apr_hash_get(cfg->require, attribute, APR_HASH_KEY_STRING);

		if (reqlist == NULL) {
			reqlist = mf_reqlist_make(cmd->pool, 2);
			apr_hash_set(cfg->require, attribute, APR_HASH_KEY_STRING, reqlist);
		}

		while (reqlist->next != NULL) {
			reqlist = reqlist->next;
		}

		if (reqlist->done) {
			reqlist->next = mf_reqlist_make(cmd->pool, 5);
			reqlist = reqlist->next;
		}

		nr = mf_reqlist_push(reqlist, value);

		ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, cmd->server,
		             "(%d) Pushed %s onto reqlist.", nr, value);

	} while (*(value = ap_getword_conf(cmd->pool, &args)) != '\0');

	reqlist->done = 1;

	return NULL;
}


/*
 * declare the different configuration directives that can be put into
 * .htaccess files and httpd.conf
 */
static const command_rec auth_mellon_cmds[] =
{
	AP_INIT_TAKE12("MellonVariable", set_mellon_slot,
	               (void *)APR_OFFSETOF(auth_mellon_config_rec, varname),
	               OR_AUTHCFG, "Name of GET variable."),
	AP_INIT_TAKE12("MellonUrl", set_mellon_slot,
	               (void *)APR_OFFSETOF(auth_mellon_config_rec, moria_url),
	               OR_AUTHCFG, "URL to Moria host."),
	AP_INIT_TAKE12("MellonServerUser", set_mellon_slot,
	               (void *)APR_OFFSETOF(auth_mellon_config_rec, userid),
	               OR_AUTHCFG, "Server access username."),
	AP_INIT_TAKE12("MellonServerPassword", set_mellon_slot,
	               (void *)APR_OFFSETOF(auth_mellon_config_rec, passwd),
	               OR_AUTHCFG, "Server acces password."),
	AP_INIT_TAKE12("MellonDomain", set_mellon_slot,
	               (void *)APR_OFFSETOF(auth_mellon_config_rec, domain),
	               OR_AUTHCFG, "The 'domain' of the authentication."),
	AP_INIT_TAKE1("MellonCacheSize", ap_set_int_slot,
	              (void *)APR_OFFSETOF(auth_mellon_server_rec, nelms),
	              RSRC_CONF, "The size of the server cache."),
	AP_INIT_TAKE1("MellonAge", ap_set_int_slot,
	              (void *)APR_OFFSETOF(auth_mellon_server_rec, cache_age),
	              RSRC_CONF, "Local authentication cache age in seconds.  Defaults to 3600."),
	AP_INIT_RAW_ARGS("MellonRequire", set_require_slot, NULL,
	                 OR_AUTHCFG, "Attribute requirements."),
	{NULL}
};


/*
 * creates an array of attributes to query from the Moria server
 */
static char **mf_build_attr_array(auth_mellon_config_rec *cfg, request_rec *r)
{
	int    size, j;
	char **array;
	char  *key;
	apr_hash_index_t *i;

	size = apr_hash_count(cfg->require);

	/* the server will barf if we don't ask for something */
	if (size == 0) {
		array = apr_palloc(r->pool, 2 * sizeof(char *));
		array[0] = apr_pstrdup(r->pool, "eduPersonAffiliation");
		array[1] = NULL;
		return array;
	}

	array = apr_palloc(r->pool, (size+1) * sizeof(char *));
	i = apr_hash_first(r->pool, cfg->require);
	j = 0;
	while (i) {
		apr_hash_this(i, &key, NULL, NULL);
		array[j] = key;
		i = apr_hash_next(i);
		j++;
	}

	array[size] = NULL;

	return array;
}


/*
 * compares the result attribute array from m_get_attributes with the
 * requirement values
 */
static int mf_check_permissions(m_attr_array *p, apr_hash_t *req,
                                request_rec *r)
{
	apr_hash_index_t *idx;

	for (idx = apr_hash_first(r->pool, req); idx;
	     idx = apr_hash_next(idx)) {

		int i, j, attr_match = -1;
		char *key;
		mf_reqlist_t *reqlist;

		/* fetch key and value from the hash */
		apr_hash_this(idx, &key, NULL, &reqlist);

		/* find the key in the attribute array */
		for (i = 0; i < p->size && attr_match == -1; i++) {
			if (strcmp(key, p->attributes[i].name) == 0) {
				attr_match = i;
			}
		}

		/* the key doesn't exist in the attribute array, the user clearly
		 * doesn't have access. */
		if (attr_match == -1) {
			ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
			             "Refused because attribute could not be found.");
			return 0;
		}

		while (reqlist) {
			int reqlist_ok = 0;
			for (i = 0; i < reqlist->nelms; i++) {
				char *req = reqlist->elms[i];
				for (j = 0; j < p->attributes[attr_match].size; j++) {
					char *val = p->attributes[attr_match].values[j];
					if (strcmp(req, val) == 0) {
						reqlist_ok = 1;
					}
				}
			}
			if (!reqlist_ok) {
				ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
				             "Attribute values did not match.");
				return 0;
			}
			reqlist = reqlist->next;
		}
	}

	return 1;
}


/*
 * find the authentication cookie in the headers
 *
 * ideas stolen from mod_usertrack.
 */
static char *mf_get_cookie(request_rec *r)
{
	auth_mellon_config_rec *cfg = ap_get_module_config(r->per_dir_config,
	                                                   &auth_mellon_module);
	char *value, *buffer, *end;
	char *name = apr_pstrcat(r->pool, "mellon-", cfg->varname, NULL);
	const char *cookie;

	/* don't run for subrequests */
	if (r->main) {
		return NULL;
	}

	if ((cookie = apr_table_get(r->headers_in, "Cookie"))) {
		if ((value = ap_strstr_c(cookie, name))) {
			value += strlen(name) + 1;
			buffer = apr_pstrdup(r->pool, value);
			end    = strchr(buffer, ';');
			if (end) {
				*end = '\0';
			}
			return buffer;
		}
	}
	return NULL;
}


/*
 * set the authentication cookie
 */
static void mf_set_cookie(request_rec *r, char *id)
{
	auth_mellon_config_rec *cfg; 
	auth_mellon_server_rec *scfg;
	char *name, *cookie;

	if (id == NULL) {
		return;
	}

	cfg = ap_get_module_config(r->per_dir_config, &auth_mellon_module);
	scfg = ap_get_module_config(r->server->module_config, &auth_mellon_module);

	name = apr_pstrcat(r->pool, "mellon-", cfg->varname, NULL);

	cookie = apr_psprintf(r->pool, "%s=%s; Version=1; Path=/", name, id);
	apr_table_setn(r->headers_out, "Set-Cookie", cookie);
	return;
}


/*
 * make a decent attempt at reconstructing the url the user entered, and then
 * append the FeideVariable=
 *
 * ideas for this function was stolen from mod_rewrite.
 */
static char *mf_reconstruct_url(request_rec *r)
{
	auth_mellon_config_rec *cfg = ap_get_module_config(r->per_dir_config,
	                                                  &auth_mellon_module);
	int port;
	const char *thisserver;
	char *thisport, buf[32], *url;

	thisserver = ap_get_server_name(r);
	port       = ap_get_server_port(r);

	if (ap_is_default_port(port, r)) {
		thisport = "";
	} else {
		apr_snprintf(buf, sizeof(buf), ":%u", port);
		thisport = buf;
	}

	if (r->filename[0] == '/') {
		url = apr_psprintf(r->pool, "%s://%s%s%s", ap_http_method(r),
		                   thisserver, thisport, r->unparsed_uri);
	} else {
		url = apr_psprintf(r->pool, "%s://%s%s/%s", ap_http_method(r),
		                   thisserver, thisport, r->unparsed_uri);
	}

	if (r->parsed_uri.query == NULL) {
		url = apr_psprintf(r->pool, "%s?%s=", url, cfg->varname);
	} else {
		url = apr_psprintf(r->pool, "%s&%s=", url, cfg->varname);
	}

	return url;
}


/*
 * goes through a query string, and tries to find the 'FeideVariable' in
 * it.  if it's found the value is returned, otherwise NULL.
 */
static char *mf_get_key(request_rec *r)
{
	auth_mellon_config_rec *cfg = ap_get_module_config(r->per_dir_config,
	                                                  &auth_mellon_module);
	int length;
	char *query, *token, *state, *s;

	query  = r->parsed_uri.query;
	length = strlen(cfg->varname);

	if (query == NULL) {
		return NULL;
	}

	state = apr_pstrdup(r->pool, cfg->varname);
	token = apr_strtok(query, "&", &state);

	while (token != NULL && strncmp(token, cfg->varname, length) != 0) {
		token = apr_strtok(NULL, "&", &state);
	}

	if (token != NULL) {
		s = token;
		while (*s != '\0' && *s != '=') {
			s++;
		}
		if (s == '\0') {
			return NULL;
		}
		return apr_pstrdup(r->pool, ++s);
	} else {
		return NULL;
	}
}


/*
 * this is where we do most of the work.  look in the cache, contact Moria,
 * redirect user if necessary and throw him out if he's not wanted.
 */
static int authenticate_mellon_user(request_rec *r)
{
	auth_mellon_config_rec *cfg = ap_get_module_config(r->per_dir_config,
	                                                  &auth_mellon_module);
	auth_mellon_server_rec *scfg = ap_get_module_config(r->server->module_config,
	                                                   &auth_mellon_module);
	int return_code = HTTP_UNAUTHORIZED;
	char *key, *cookie;
	m_config *fc;

	/* if the module hasn't been configured, then we decline to handle
	 * this request */
	if (!(cfg->varname && cfg->moria_url && cfg->userid  && cfg->passwd &&
	      cfg->domain)) {
		return DECLINED;
	}

	fc = m_init(cfg->moria_url, cfg->userid, cfg->passwd, 0);

	cookie = mf_get_cookie(r);
	key    = mf_get_key(r);

	ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
	             "cookie=%s, key=%s", cookie, key);

	if (cookie || key) {
		/* we have a way of accessing an ID */
		int ret;
		m_attr_array *fa;
		mf_cache_entry_t *cache;
		char *cache_key;

		cache = apr_shm_baseaddr_get(scfg->cache);

		if (key) {
			cache_key = mf_cache_genkey(r->pool, key, cfg->domain,
			                            r->connection->remote_ip);

			if (mf_cache_find(cache, scfg->nelms, cache_key) == -1) {
				ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
				             "key=%s not in cache.");
				return_code = HTTP_UNAUTHORIZED;
			} else {
				ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
				             "key=%s in cache.");
				return_code = OK;
			}
		}

		/* first attempt to validate the key, look at the cookie later */
		if (key && return_code != OK) {
			ret = m_get_attributes(fc, key, &fa);
			ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
			             "key=%s got result %d", key, ret);
			if (ret == M_OK) {
				if (mf_check_permissions(fa, cfg->require, r)) {
					return_code = OK;
					ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
					             "return_code = OK (%d)", return_code);
				} else {
					return_code = HTTP_UNAUTHORIZED;
					ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
					             "return_code = HTTP_UNAUTHORIZED (%d)",
					             return_code);
				}
				m_free_attributes(fa);
			} else {
				ap_log_error(APLOG_MARK, APLOG_NOTICE, 0, r->server,
				             "auth_mellon: getAttributes error \"%s\".",
				             fc->error_str);
				return_code = HTTP_INTERNAL_SERVER_ERROR;
				ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
				             "return_code = HTTP_INTERNAL_SERVER_ERROR (%d)",
				             return_code);
			}
		}

		/* then attempt to handle cookie, if still relevant */
		if (cookie && return_code != OK) {
			if (mf_cache_find(cache, scfg->nelms, cookie) == -1) {
				ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
				             "%s no longer valid.", cookie);
				return_code = HTTP_UNAUTHORIZED;
			} else {
				ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
				             "%s authorised in cache.", cookie);
				return_code = OK;
			}
		}

		/* the user is authenticated, but he has no cookie set, so we make
		 * a hash from the key and the client IP address and send that to
		 * the client as a cookie.  the hash is stored in the cache together
		 * with the access time. */
		if (!cookie && return_code == OK) {
			cache_key = mf_cache_genkey(r->pool, key, cfg->domain,
			                            r->connection->remote_ip);
			mf_set_cookie(r, cache_key);
			mf_cache_set(cache, scfg->nelms, cache_key);
		}
	} else {
		/* we don't have a way to access an ID, redirect to authenticate */
		int   ret;
		char *url, *redirect, **attributes;

		attributes = mf_build_attr_array(cfg, r);
		url = mf_reconstruct_url(r);
		ret = m_request_session(fc, attributes, url, "", &redirect);
		if (ret != M_OK) {
			ap_log_error(APLOG_MARK, APLOG_NOTICE, 0, r->server,
			             "auth_mellon: requestSession error \"%s\".",
			             fc->error_str);
			return_code = HTTP_INTERNAL_SERVER_ERROR;
		} else {
			ap_log_error(APLOG_MARK, APLOG_INFO, 0, r->server,
			             "auth_mellon: Request for \"%s\", redirecting to \"%s\".",
			             url, redirect);
			apr_table_setn(r->headers_out, "Location", redirect);
			return_code = HTTP_MOVED_TEMPORARILY;
		}
	}
	m_end(fc);

	ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
	             "returning return_code = %d", return_code);

	return return_code;
}


/*
 * frees the shared memory structure, it's run when the `pconf' pool is
 * deallocated (see `mf_global_init').
 */
static apr_status_t mf_global_kill(void *p)
{
	server_rec *s = p;
	auth_mellon_server_rec *cfg;

	cfg = ap_get_module_config(s->module_config, &auth_mellon_module);

	if (cfg->cache) {
		apr_shm_destroy(cfg->cache);
		cfg->cache = NULL;
	}

	return OK;
}


/*
 * alloated the shared memory and puts a pointer to it in the shared
 * memory structure.
 */
static int mf_global_init(apr_pool_t *pconf, apr_pool_t *plog,
                          apr_pool_t *ptemp, server_rec *s)
{
	mf_cache_entry_t *table;
	apr_status_t rv;
	apr_size_t mem_size;
	auth_mellon_server_rec *cfg;
	int i;
	const char *userdata_key = "auth_mellon_init";
	void *data;

	/* we want to make sure that the contents of this function is ran once,
     * and onle once. */
	apr_pool_userdata_get(&data, userdata_key, s->process->pool);
	if (!data) {
		apr_pool_userdata_set((conse void *)1, userdata_key,
		                      apr_pool_cleanup_null, s->process->pool);
	} else {
		return OK;
	}
	
	cfg = ap_get_module_config(s->module_config,
	                           &auth_mellon_module);

	mem_size = sizeof(mf_cache_entry_t) * cfg->nelms;

	apr_pool_cleanup_register(pconf, s, mf_global_kill, apr_pool_cleanup_null);

	rv = apr_shm_create(&(cfg->cache), mem_size, NULL, pconf);
	if (rv != APR_SUCCESS) {
		ap_log_error(APLOG_MARK, APLOG_ERR, rv, s,
		             "Could not allocate %d bytes of shared memory.",
		             mem_size);
		return !OK;
	}

	table = apr_shm_baseaddr_get(cfg->cache);
	for (i = 0; i < cfg->nelms; i++) {
		table[i].key[0] = '\0';
		table[i].access = 0;
	}

	return OK;
}


/*
 * tell Apache when we want to mess in it's request handling business.
 *
 * we had to use access_checker hook instead of the authentication hooks
 * because we needed to react before Apache prompts the user for a password.
 */
static void register_hooks(apr_pool_t *p)
{
	ap_hook_access_checker(authenticate_mellon_user,NULL,NULL,APR_HOOK_MIDDLE);
	ap_hook_post_config(mf_global_init, NULL, NULL, APR_HOOK_MIDDLE);
}


/*
 * module definition
 */
module AP_MODULE_DECLARE_DATA auth_mellon_module =
{
	STANDARD20_MODULE_STUFF,
	create_auth_dir_config,
	NULL,
	create_auth_server_config,
	NULL,
	auth_mellon_cmds,
	register_hooks
};
