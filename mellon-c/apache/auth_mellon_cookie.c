/*

    mod_auth_cookie.c: an authentication apache module
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

char *am_cookie_get(request_rec *r)
{
    am_dir_cfg_rec *d;
    char *value, *buffer, *end, *name;
    const char *cookie;

    /* don't run for subrequests */
    if (r->main) {
        return NULL;
    }

    d = ap_get_module_config(r->per_dir_config, &auth_mellon_module);
    name = apr_pstrcat(r->pool, "mellon-", d->varname, NULL);

    if (cookie = apr_table_get(r->headers_in, "Cookie")) {
        if (value = ap_strstr_c(cookie, name)) {
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

void am_cookie_set(request_rec *r, const char *id)
{
    am_dir_cfg_rec *d;
    char *name, *cookie;

    if (id == NULL)
        return;

    d = ap_get_module_config(r->per_dir_config, &auth_mellon_module);

    name   = apr_pstrcat(r->pool, "mellon-", d->varname, NULL);
    cookie = apr_psprintf(r->pool, "%s=%s; Version=1; Path=/", name, id);
    apr_table_setn(r->headers_out, "Set-Cookie", cookie);
    return;
}
