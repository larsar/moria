
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
