/*

    auth_mellon.h: an authentication apache module
    Copyright © 2003 UNINETT (http://www.uninett.no/)

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

#ifndef MOD_AUTH_MELLON_H
#define MOD_AUTH_MELLON_H

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

#define am_get_srv_cfg(s) (am_srv_cfg_rec *)ap_get_module_config((s)->module_config, &auth_mellon_module)

#define am_get_mod_cfg(s) (am_get_srv_cfg((s)))->mc


typedef struct am_mod_cfg_rec {
    apr_shm_t          *cache;
    char               *cache_file;
    int                 cache_age;
    int                 cache_size;
    apr_global_mutex_t *lock;
    char               *lock_file;
} am_mod_cfg_rec;


typedef struct am_srv_cfg_rec {
    am_mod_cfg_rec *mc;
} am_srv_cfg_rec;


typedef struct am_dir_cfg_rec {
    char       *varname;
    char       *moria_url;
    char       *userid;
    char       *passwd;
    char       *domain;
    apr_hash_t *require;
} am_dir_cfg_rec;


#define AM_CACHE_KEYSIZE 120

typedef struct am_cache_entry_t {
    char key[AM_CACHE_KEYSIZE];
    apr_time_t access;
} am_cache_entry_t;


typedef struct am_reqlist_t {
    int     done;
    int     nelms;
    int     nalloc;
    char  **elms;
    struct am_reqlist_t *next;
    apr_pool_t *pool;
} am_reqlist_t;


am_reqlist_t *am_reqlist_make(apr_pool_t *p, int n);
int           am_reqlist_push(am_reqlist_t *rl, char *value);

char         *am_cookie_get(request_rec *r);
void          am_cookie_set(request_rec *r, const char *id);

apr_time_t    am_cache_find(server_rec *s, char *key);
void          am_cache_set(server_rec *s, const char *key);
char         *am_cache_genkey(request_rec *r, const char *id, const char *domain);

char         *am_reconstruct_url(request_rec *r);
char         *am_get_key(request_rec *r);
char        **am_build_attr_array(am_dir_cfg_rec *d, request_rec *r);
int           am_check_permisssions(m_attr_array *p, apr_hash_t *req, request_rec  *r);


module AP_MODULE_DECLARE_DATA auth_mellon_module;

#endif /* MOD_AUTH_MELLON_H */
