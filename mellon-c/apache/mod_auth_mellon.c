
#include "auth_mellon.h"

static const char *am_set_string_slot(cmd_parms *cmd,
                                      void *struct_ptr,
                                      const char *arg)
{
    return ap_set_string_slot(cmd, ((am_srv_cfg_rec *) struct_ptr)->mc, arg);
}

static const char *am_set_int_slot(cmd_parms *cmd,
                                   void *struct_ptr,
                                   const char *arg)
{
    return ap_set_int_slot(cmd, ((am_srv_cfg_rec *) struct_ptr)->mc, arg);
}

static const char *am_set_require_slot(cmd_parms *cmd,
                                       void *struct_ptr,
                                       const char *arg)
{
    am_reqlist_t   *r;
    am_dir_cfg_rec *d = struct_ptr;
    char *attribute, *value;

    attribute = ap_getword_conf(cmd->pool, &arg);
    value     = ap_getword_conf(cmd->pool, &arg);

    if (*attribute == '\0' || *value == '\0') {
        return apr_pstrcat(cmd->pool, cmd->cmd->name,
                           " takes two arguments", NULL);
    }

    do {
        int nr;

        ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, cmd->server,
                     "%s=%s", attribute, value);

        r = apr_hash_get(d->require, attribute, APR_HASH_KEY_STRING);

        if (r == NULL) {
            r = am_reqlist_make(cmd->pool, 2);
            apr_hash_set(d->require, attribute, APR_HASH_KEY_STRING, r);
        }

        while (r->next != NULL) {
            r = r->next;
        }

        if (r->done) {
            r->next = am_reqlist_make(cmd->pool, 5);
            r = r->next;
        }

        nr = am_reqlist_push(r, value);

        ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, cmd->server,
                     "[%d] Pushed %s onto reqlist", nr, value);

    } while (*(value = ap_getword_conf(cmd->pool, &arg)) != '\0');

    r->done = 1;

    return NULL;
}
    
static const command_rec auth_mellon_commands[] =
{
    AP_INIT_TAKE1   ("MellonVariable", ap_set_string_slot,
                     (void *)APR_OFFSETOF(am_dir_cfg_rec, varname),
                     OR_AUTHCFG,
                     "The name of the GET variable put into the URL."),
    AP_INIT_TAKE1   ("MellonUrl", ap_set_string_slot,
                     (void *)APR_OFFSETOF(am_dir_cfg_rec, moria_url),
                     OR_AUTHCFG,
                     "The SOAP URL to the Moria authentication server"),
    AP_INIT_TAKE1   ("MellonServerUser", ap_set_string_slot,
                     (void *)APR_OFFSETOF(am_dir_cfg_rec, userid),
                     OR_AUTHCFG,
                     "The userid given to the Mellon authentiation server."),
    AP_INIT_TAKE1   ("MellonServerPassword", ap_set_string_slot,
                     (void *)APR_OFFSETOF(am_dir_cfg_rec, passwd),
                     OR_AUTHCFG,
                     "The password to access the Moria authentication server."),
    AP_INIT_TAKE1   ("MellonDomain", ap_set_string_slot,
                     (void *)APR_OFFSETOF(am_dir_cfg_rec, domain),
                     OR_AUTHCFG,
                     "The current domain of the authentication."),
    AP_INIT_TAKE1   ("MellonCacheSize", am_set_int_slot,
                     (void *)APR_OFFSETOF(am_mod_cfg_rec, cache_size),
                     RSRC_CONF,
                     "The number of elements allowed in hash."),
    AP_INIT_TAKE1   ("MellonCacheAge", am_set_int_slot,
                     (void *)APR_OFFSETOF(am_mod_cfg_rec, cache_age),
                     RSRC_CONF,
                     "The time in seconds before a cache entry will expire."),
    AP_INIT_TAKE1   ("MellonLockFile", am_set_string_slot,
                     (void *)APR_OFFSETOF(am_mod_cfg_rec, lock_file),
                     RSRC_CONF,
                     "The lock file for file-based mutexes."), 
    AP_INIT_TAKE1   ("MellonShmFile", am_set_string_slot,
                     (void *)APR_OFFSETOF(am_mod_cfg_rec, cache_file),
                     RSRC_CONF,
                     "File for shared memory (where needed)."),
    AP_INIT_RAW_ARGS("MellonRequire", am_set_require_slot, NULL, OR_AUTHCFG,
                     "Attribute requirements for authentication."),
    {NULL}
};

static void *dir_config(apr_pool_t *p, char *d)
{
    am_dir_cfg_rec *dir = apr_palloc(p, sizeof(*dir));
    dir->varname   = NULL;
    dir->moria_url = NULL;
    dir->userid    = NULL;
    dir->passwd    = NULL;
    dir->domain    = NULL;
    dir->require   = apr_hash_make(p);
    return dir;
}

static void *server_config(apr_pool_t *p, server_rec *s)
{
    am_srv_cfg_rec *srv;
    am_mod_cfg_rec *mod;
    const char key[] = "auth_mellon_server_config";

    srv = apr_palloc(p, sizeof(*srv));

    /* we want to keeep our global configuration of shared memory and
     * mutexes, so we try to find it in the userdata before doing anything
     * else */
    apr_pool_userdata_get((void **)&mod, key, p);
    if (mod) {
        srv->mc = mod;
        return srv;
    }

    /* the module has not been initiated at all */
    mod = apr_palloc(p, sizeof(*mod));
    mod->cache_file = NULL;
    mod->cache      = NULL;
    mod->cache_age  = 5*60; /* 5 minutes sensible default */
    mod->cache_size = 100;  /* ought to be enough for everybody */
    mod->lock       = NULL;
    mod->lock_file  = NULL;

    apr_pool_userdata_set(mod, key, apr_pool_cleanup_null, p);

    srv->mc = mod;
    return srv;
}

static apr_status_t am_global_kill(void *p)
{
    server_rec     *s = (server_rec *) p;
    am_mod_cfg_rec *m = am_get_mod_cfg(s);

    if (m->cache) {
        apr_shm_destroy(m->cache);
        m->cache = NULL;
    }
    return OK;
}


static int am_global_init(apr_pool_t *conf, apr_pool_t *log,
                          apr_pool_t *tmp, server_rec *s)
{
    am_cache_entry_t *table;
    apr_size_t        mem_size;
    am_mod_cfg_rec   *mod;
    int rv, i;
    const char userdata_key[] = "auth_mellon_init";
    char buffer[512];
    void *data;

    /* Apache tests loadable modules by loading them (as is the only way).
     * This has the effect that all modules are loaded and initialised twice,
     * and we just want to initialise shared memory and mutexes when the
     * module loads for real! */
    apr_pool_userdata_get(&data, userdata_key, s->process->pool);
    if (!data) {
        apr_pool_userdata_set((const void *)1, userdata_key,
                              apr_pool_cleanup_null, s->process->pool);
        return OK;
    } 

    mod = am_get_mod_cfg(s);

    ap_log_error(APLOG_MARK, APLOG_ERR, 0, s,
                 "mod cfg addr: %08X", mod);

    /* find out the memory size of the cache */
    mem_size = sizeof(am_cache_entry_t) * mod->cache_size;

    /* register a function to clean up the whole mess on exit */
    apr_pool_cleanup_register(conf, s, 
                              am_global_kill,
                              apr_pool_cleanup_null);

    /* create the shared memory, exit if it fails.  on success we initialise
     * the table in the memory */
    rv = apr_shm_create(&(mod->cache), mem_size, mod->cache_file, conf);

    if (rv != APR_SUCCESS) {
        ap_log_error(APLOG_MARK, APLOG_ERR, rv, s,
                     "shm_create: Error [%d] \"%s\"", rv,
                      apr_strerror(rv, buffer, sizeof(buffer)));
        return !OK;
    }

    table = apr_shm_baseaddr_get(mod->cache);
    for (i = 0; i < mod->cache_size; i++) {
        table[i].key[0] = '\0';
        table[i].access = 0;
    }

    /* now create the mutex that we need for locking the share memory, then
     * test for success.  we really do need this, so we exit on failure. */
    rv = apr_global_mutex_create(&(mod->lock),
                                 mod->lock_file,
                                 APR_LOCK_DEFAULT,
                                 conf);

    if (rv != APR_SUCCESS) {
        ap_log_error(APLOG_MARK, APLOG_ERR, rv, s,
                     "mutex_create: Error [%d] \"%s\"", rv,
                     apr_strerror(rv, buffer, sizeof(buffer)));
        return !OK;
    }

    return OK;
}

static void am_child_init(apr_pool_t *p, server_rec *s)
{
    am_mod_cfg_rec *m = am_get_mod_cfg(s);
    apr_status_t rv;

    ap_log_error(APLOG_MARK, APLOG_ERR, 0, s,
                 "Connecting to mutex: %08X", m->lock);

    rv = apr_global_mutex_child_init(&(m->lock), m->lock_file, p);
    if (rv != APR_SUCCESS) {
        ap_log_error(APLOG_MARK, APLOG_ERR, rv, s,
                     "Child process could not connect to mutex");
    }
    return;
}

static int am_auth_mellon_user(request_rec *r)
{
    am_dir_cfg_rec *dir;
    am_mod_cfg_rec *mod = am_get_mod_cfg(r->server);
    int return_code = HTTP_UNAUTHORIZED;
    char *key, *cookie;
    m_config *fc;

    dir = ap_get_module_config(r->per_dir_config, &auth_mellon_module);

    /* check if we have been configured properly in this directory, if not
     * refuse to handle request */
    if (!(dir->varname && dir->moria_url && dir->userid &&
          dir->passwd && dir->domain)) {
        return DECLINED;
    }

    fc = m_init(dir->moria_url, dir->userid, dir->passwd, 0);

    cookie = am_cookie_get(r);
    key    = am_get_key(r);

    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                 "cookie=%s, key=%s", cookie, key);

    if (cookie || key) {
        /* we have a way of accessing a key */
        int ret;
        m_attr_array *fa;
        char *cache_key;

        /* first we want to look for the key in the cache of authentications,
         * if it already exists */
        if (key) {

            ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                         "We have a key!");

            cache_key = am_cache_genkey(r, key, dir->domain);

            ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                         "Generated cache_key");

            ret = am_cache_find(r->server, cache_key);

            ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                         "Did am_cache_find.");

            if (ret == -1) {
                ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                             "key=%s not in cache", key);
                return_code = HTTP_UNAUTHORIZED;
            } else {
                ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                             "key=%s in cache with interval %d",
                             key, ret);
                return_code = OK;
            }
        }

        ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                     "Done processing key");

        /* if the key has not been cached yet, we need to fetch information
         * about it and then cache it */
        if (key && return_code != OK) {
            ret = m_get_attributes(fc, key, &fa);
            if (ret == M_OK) {
                if (am_check_permissions(fa, dir->require, r)) {
                    return_code = OK;
                } else {
                    return_code = HTTP_UNAUTHORIZED;
                    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                                 "key=%s, permissions failed", key);
                }
                m_free_attributes(fa);
            } else {
                return_code = HTTP_INTERNAL_SERVER_ERROR;
                ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                             "get_attributes: [%d] failed",
                              ret);
            }
        }

        /* now we try to handle the cookie, if it is present and still
         * relevant to authentication */
        if (cookie && return_code != OK) {
            if (am_cache_find(r->server, cookie) == -1) {
                ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                             "cookie=%s no longer valid", cookie);
                return_code = HTTP_UNAUTHORIZED;
            } else {
                ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                             "cookie=%s authorized in cache", cookie);
                return_code = OK;
            }
        }

        /* if the user is authenticated already but has no cookie, we
         * need store the authentication in the cache and then send the cache
         * key to the user as a cookie */
        if (!cookie && return_code == OK) {
            cache_key = am_cache_genkey(r, key, dir->domain);
            am_cookie_set(r, cache_key);
            am_cache_set(r->server, cache_key);
        }
    
    } else {

        int ret;
        char *url, *redirect, **attributes;

        attributes = am_build_attr_array(dir, r);
        url = am_reconstruct_url(r);

        ret = m_request_session(fc, attributes, url, "", &redirect);
        if (ret != M_OK) {
            ap_log_error(APLOG_MARK, APLOG_ERR, 0, r->server,
                         "request_session: \"%s\"", fc->error_str);
            return_code = HTTP_INTERNAL_SERVER_ERROR;
        } else {
            ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                         "request_session: requested \"%s\", directed to %s",
                         url, redirect);
            apr_table_setn(r->headers_out, "Location", redirect);
            return_code = HTTP_MOVED_TEMPORARILY;
        }
    }

    m_end(fc);

    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                 "return_code = %d", return_code);

    return return_code;
}

static void register_hooks(apr_pool_t *p)
{
    ap_hook_access_checker(am_auth_mellon_user, NULL, NULL, APR_HOOK_MIDDLE);
    ap_hook_post_config(am_global_init, NULL, NULL, APR_HOOK_MIDDLE);
    ap_hook_child_init(am_child_init, NULL, NULL, APR_HOOK_MIDDLE);
    return;
}

module AP_MODULE_DECLARE_DATA auth_mellon_module =
{
    STANDARD20_MODULE_STUFF,
    dir_config,
    NULL,
    server_config,
    NULL,
    auth_mellon_commands,
    register_hooks
};

