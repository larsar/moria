#
# $Id$

CFLAGS=-L. -I../include -g  -Wall
SOAPDEFS=-DLINUX -DWITH_OPENSSL

.PHONY: clean

clean:
	rm -f -v *.o *.a *.log *.la
	rm -f -v test

test: test.c libfeide.a
	$(CC) $(CFLAGS) -o $@ $? -lfeide -lssl -lcrypto

.c.o:
	$(CC) $(CFLAGS) $(SOAPDEFS) -c -o $@ $<

libfeide.a: stdsoap2.o soapC.o soapClient.o feide.o
	$(AR) cr $@ $?
