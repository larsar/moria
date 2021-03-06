/*

    auth_mellon_reqlist.c: an authentication apache module
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

am_reqlist_t *am_reqlist_make(apr_pool_t *p, int n)
{
    am_reqlist_t *new;

    new = apr_palloc(p, sizeof(*new));
    new->elms = apr_palloc(p, sizeof(char *) * n);
    new->nelms = 0;
    new->nalloc = n;
    new->next = NULL;
    new->pool = p;
    new->done = 0;
    return new;
}

int am_reqlist_push(am_reqlist_t *rl, char *value)
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
