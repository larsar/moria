
#include "apr_strings.h"
#include "apr_shm.h"
#include "apr_rmm.h"
#include "apr_global_mutex.h"

#include "ap_config.h"
#include "httpd.h"
#include "http_config.h"
#include "http_core.h"
#include "http_log.h"
#include "http_protocol.h"
#include "http_request.h"

#include "feide.h"

module AP_MODULE_DECLARE_DATA auth_feide_module;

/*
 * all this is for the FeideRequire configuration command.  we want each
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
	int   authorative;
	int   cache_age;
	apr_hash_t *require;
} auth_feide_config_rec;


/*
 * initalizes a configuration structure for use
 */
static void *create_auth_dir_config(apr_pool_t *p, char *d)
{
	auth_feide_config_rec *conf = apr_palloc(p, sizeof(*conf));

	conf->varname = NULL;
	conf->moria_url = NULL;
	conf->userid = NULL;
	conf->passwd = NULL;
	conf->domain = NULL;
	conf->cache_age = 3600;  /* default to one hour */
	conf->require = apr_hash_make(p);
	return conf;
}


/*
 * a function to set different fields in the configuration structure
 */
static const char *set_feide_slot(cmd_parms *cmd, void *conf,
                                  const char *f, const char *t)
{
	if (t && strcmp(t, "feide")) {
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
	auth_feide_config_rec *cfg = conf_;
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
static const command_rec auth_feide_cmds[] =
{
	AP_INIT_TAKE12("FeideVariable", set_feide_slot,
	               (void *)APR_OFFSETOF(auth_feide_config_rec, varname),
	               OR_AUTHCFG, "Name of GET variable."),
	AP_INIT_TAKE12("FeideUrl", set_feide_slot,
	               (void *)APR_OFFSETOF(auth_feide_config_rec, moria_url),
	               OR_AUTHCFG, "URL to Moria host."),
	AP_INIT_TAKE12("FeideServerUser", set_feide_slot,
	               (void *)APR_OFFSETOF(auth_feide_config_rec, userid),
	               OR_AUTHCFG, "Server access username."),
	AP_INIT_TAKE12("FeideServerPassword", set_feide_slot,
	               (void *)APR_OFFSETOF(auth_feide_config_rec, passwd),
	               OR_AUTHCFG, "Server acces password."),
	AP_INIT_TAKE12("FeideDomain", set_feide_slot,
	               (void *)APR_OFFSETOF(auth_feide_config_rec, domain),
	               OR_AUTHCFG, "The 'domain' of the authentication."),
	AP_INIT_TAKE1("FeideCacheAge", ap_set_int_slot,
	              (void *)APR_OFFSETOF(auth_feide_config_rec, cache_age),
	              OR_AUTHCFG, "Local authentication cache age in seconds.  Defaults to 3600."),
	AP_INIT_RAW_ARGS("FeideRequire", set_require_slot, NULL,
	                 OR_AUTHCFG, "Attribute requirements."),
	{NULL}
};


/*
 * creates an array of attributes to query from the Moria server
 */
static char **mf_build_attr_array(auth_feide_config_rec *cfg, request_rec *r)
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
 * compares the result attribute array from f_get_attributes with the
 * requirement values
 */
static int mf_check_permissions(f_attr_array *p, apr_hash_t *req,
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
	auth_feide_config_rec *cfg = ap_get_module_config(r->per_dir_config,
	                                                  &auth_feide_module);
	char *value, *buffer, *end;
	char *name = apr_pstrcat(r->pool, "feide-", cfg->varname, NULL);
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
	auth_feide_config_rec *cfg = ap_get_module_config(r->per_dir_config,
	                                                  &auth_feide_module);
	char *name = apr_pstrcat(r->pool, "feide-", cfg->varname, NULL);
	char *cookie;

	cookie = apr_psprintf(r->pool, "%s=%s; Version=1; Path=/; Max-Age=%d",
	                      name, id, cfg->cache_age);
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
	auth_feide_config_rec *cfg = ap_get_module_config(r->per_dir_config,
	                                                  &auth_feide_module);
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
	auth_feide_config_rec *cfg = ap_get_module_config(r->per_dir_config,
	                                                  &auth_feide_module);
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

static int authenticate_feide_user(request_rec *r)
{
	auth_feide_config_rec *cfg = ap_get_module_config(r->per_dir_config,
	                                                  &auth_feide_module);
	int return_code = HTTP_UNAUTHORIZED;
	/* temporary value, this will need a better solution when requests
	 * become more structured */
	char *key, *cookie;
	f_config *fc;

	/* if the module hasn't been configured, then we decline to handle
	 * this request */
	if (!(cfg->varname && cfg->moria_url && cfg->userid  && cfg->passwd)) {
		return DECLINED;
	}

	fc = f_init(cfg->moria_url, cfg->userid, cfg->passwd, 0);

	cookie = mf_get_cookie(r);
	key    = mf_get_key(r);

	if (key) { /* was (cookie || key), but we don't have shared mem yet */
		/* we have a way of accessing an ID */
		int ret;
		f_attr_array *fa;

		ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
		             "cookie=%s, key=%s", cookie, key);

		/* first attempt to validate the key, look at the cookie later */
		if (key) {
			ret = f_get_attributes(fc, key, &fa);
			ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
			             "key=%s got result %d", key, ret);
			if (ret == F_OK) {
				if (mf_check_permissions(fa, cfg->require, r)) {
					return_code = OK;
				} else {
					return_code = HTTP_UNAUTHORIZED;
				}
				f_free_attributes(fa);
			} else {
				ap_log_error(APLOG_MARK, APLOG_NOTICE, 0, r->server,
				             "auth_feide: getAttributes error \"%s\".",
				             fc->error_str);
				return_code = HTTP_INTERNAL_SERVER_ERROR;
			}
		}

		/* then attempt to handle cookie, if still relevant */
		if (/*cookie && return_code != OK*/ NULL) {
			/* here we're supposed to look up the cookie in a local cache
			 * but there's no cache implemented yet.  just log that we're
			 * here and then continue as normal */
			ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
			             "cookie=%s", cookie);
		}

		/* if there's no cookie but we're still ok, then make a cookie */
		if (!cookie && return_code == OK) {
			mf_set_cookie(r, key);
		}
	} else {
		/* we don't have a way to accvess an ID, redirect to authenticate */
		int   ret;
		char *url, *redirect, **attributes;

		attributes = mf_build_attr_array(cfg, r);
		url = mf_reconstruct_url(r);
		ret = f_request_session(fc, attributes, url, "", &redirect);
		if (ret != F_OK) {
			ap_log_error(APLOG_MARK, APLOG_NOTICE, 0, r->server,
			             "auth_feide: requestSession error \"%s\".",
			             fc->error_str);
			return_code = HTTP_INTERNAL_SERVER_ERROR;
		} else {
			ap_log_error(APLOG_MARK, APLOG_INFO, 0, r->server,
			             "auth_feide: Request for \"%s\", redirecting to \"%s\".",
			             url, redirect);
			apr_table_setn(r->headers_out, "Location", redirect);
			return_code = HTTP_MOVED_TEMPORARILY;
		}
	}
	f_end(fc);
	return return_code;
}


/*
 * tell Apache when we want to mess in it's request handling business.
 *
 * we had to use access_checker hook instead of the authentication hooks
 * because we needed to react before Apache prompts the user for a password.
 */
static void register_hooks(apr_pool_t *p)
{
	ap_hook_access_checker(authenticate_feide_user,NULL,NULL,APR_HOOK_MIDDLE);
}


/*
 * module definition
 */
module AP_MODULE_DECLARE_DATA auth_feide_module =
{
	STANDARD20_MODULE_STUFF,
	create_auth_dir_config,
	NULL,
	NULL,
	NULL,
	auth_feide_cmds,
	register_hooks
};
