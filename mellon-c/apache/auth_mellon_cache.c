/*

    auth_mellon_cache.c: an authentication apache module
    Copyright � 2003 UNINETT (http://www.uninett.no/)

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

apr_time_t am_cache_find(server_rec *s, char *key)
{
    int i;
    apr_time_t interval = -1;
    am_mod_cfg_rec   *m;
    am_cache_entry_t *t;

    m = am_get_mod_cfg(s);

    /* now lock the mutex (or wait for it to be released), so that we can
     * safely ravage memory */

    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, s,
                 "Locking mutex: %08X", m->lock);

    apr_global_mutex_lock(m->lock);

    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, s,
                 "Inside mutex.");

    t = apr_shm_baseaddr_get(m->cache);

    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, s,
                 "Got baseaddr.");

    for (i = 0; i < m->cache_size; i++) {
        if (strcmp(t[i].key, key) == 0) {
            interval = apr_time_sec(apr_time_now() - t[i].access);
            t[i].access = apr_time_now();
        }
    }

    apr_global_mutex_unlock(m->lock);

    ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, s,
                 "Left mutex: %08X", m->lock);

    return interval;
}

void am_cache_set(server_rec *s, const char *key)
{
    am_cache_entry_t *t;
    am_mod_cfg_rec   *m;
    int i;

    m = am_get_mod_cfg(s);

    apr_global_mutex_lock(m->lock);

    t = apr_shm_baseaddr_get(m->cache);

    for (i = 0; i < m->cache_size; i++) {
        if (t[i].access == 0)
            break;
    }

    if (t[i].key[0] != '\0') {
        int oldest_index = 0;
        for (i = 0; i < m->cache_size; i++) {
            if (t[i].access < t[oldest_index].access) {
                oldest_index = i;
            }
        }
        i = oldest_index;
    }

    strcpy(t[i].key, key);
    t[i].access = apr_time_now();

    apr_global_mutex_unlock(m->lock);

    return;
}

char *am_cache_genkey(request_rec *r, const char *id, const char *domain)
{
    char hash[AM_CACHE_KEYSIZE];
    char *tmp;

    tmp = apr_pstrcat(r->pool, id, r->connection->remote_ip, domain, NULL);

    if (apr_md5_encode(tmp, "ab", hash, strlen(tmp)) == 0) {
        return apr_pstrdup(r->pool, hash);
    } else {
        return NULL;
    }
}
