#
# $Id$

CFLAGS=-L. -I../include -g  -Wall
SOAPDEFS=-DLINUX -DWITH_OPENSSL

.PHONY: clean

clean:
	rm -f -v *.o *.a *.log *.la
	rm -f -v test

test: test.c libmellon.a
	$(CC) $(CFLAGS) -o $@ $? -lmellon -lssl -lcrypto

.c.o:
	$(CC) $(CFLAGS) $(SOAPDEFS) -c -o $@ $<

libmellon.a: stdsoap2.o soapC.o soapClient.o mellon.o
	$(AR) cr $@ $?
