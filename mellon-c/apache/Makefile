
APXS=/usr/local/www-prefork/bin/apxs
CFLAGS=-I../include -L../src -lssl -lcrypto -lfeide

mod_auth_feide.la: mod_auth_feide.c
	$(APXS) $(CFLAGS) -c $?

