/*

    auth_mellon_util.c: an authentication apache module
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

#include "auth_mellon.h"


char *am_reconstruct_url(request_rec *r)
{
    am_dir_cfg_rec *d;
    apr_port_t  port;
    const char *t_server;
    char       *t_port, buf[32], *url;

    d = ap_get_module_config(r->per_dir_config, &auth_mellon_module);

    t_server = ap_get_server_name(r);
    port     = ap_get_server_port(r);

    if (ap_is_default_port(port, r)) {
        t_port = "";
    } else {
        apr_snprintf(buf, sizeof(buf), ":%u", port);
        t_port = buf;
    }

    if (r->filename[0] == '/') {
        url = apr_psprintf(r->pool, "%s://%s%s%s", ap_http_method(r),
                           t_server, t_port, r->unparsed_uri);
    } else {
        url = apr_psprintf(r->pool, "%s://%s%s/%s", ap_http_method(r),
                           t_server, t_port, r->unparsed_uri);
    }

    if (r->parsed_uri.query == NULL) {
        url = apr_psprintf(r->pool, "%s?%s=", url, d->varname);
    } else {
        url = apr_psprintf(r->pool, "%s&%s=", url, d->varname);
    }

    return url;
}


char *am_get_key(request_rec *r)
{
    am_dir_cfg_rec *cfg = ap_get_module_config(r->per_dir_config,
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

char **am_build_attr_array(am_dir_cfg_rec *d, request_rec *r)
{
    int size, j;
    char **array, *key;
    apr_hash_index_t *i;

    size = apr_hash_count(d->require);

    /* if the authentication asks for no attributes we need to ask for
     * something since the protocol doesn't support empty fields there */
    if (size == 0) {
        array = apr_palloc(r->pool, 2 * sizeof(char *));
        array[0] = apr_pstrdup(r->pool, "eduPersonAffiliation");
        array[1] = NULL;
        return array;
    }

    array = apr_palloc(r->pool, (size + 1) * sizeof(char *));
    i = apr_hash_first(r->pool, d->require);
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

int am_check_permissions(m_attr_array *p, apr_hash_t *req, request_rec *r)
{
    apr_hash_index_t *idx;

    for (idx = apr_hash_first(r->pool, req); idx;
         idx = apr_hash_next(idx)) {
        int i,j, attr_match = -1;
        char *key;
        am_reqlist_t *rlist;

        apr_hash_this(idx, &key, NULL, &rlist);

        for (i = 0; i < p->size && attr_match == -1; i++) {
            if (strcmp(key, p->attributes[i].name) == 0) {
                attr_match = i;
            }
        }

        if (attr_match == -1) {
            ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                         "Attribute \"%s\" not found in reqlist", key);
            return 0;
        }

        while (rlist) {
            int rlist_ok = 0;
            for (i = 0; i < rlist->elms[i]; i++) {
                char *req = rlist->elms[i];
                for (j = 0; j < p->attributes[attr_match].size; j++) {
                    char *val = p->attributes[attr_match].values[j];
                    if (strcmp(req, val) == 0) {
                        rlist_ok = 1;
                    }
                }
            }
            if (!rlist_ok) {
                ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server,
                             "Attribute \"%s\" did not match", key);
                return 0;
            }
            rlist = rlist->next;
        }
    }
    return 1;
}
