
APXS=/usr/local/www-prefork/bin/apxs
CFLAGS=-I../include -L../src -lssl -lcrypto -lmellon

mod_auth_mellon.la: mod_auth_mellon.c
	$(APXS) $(CFLAGS) -c $?

