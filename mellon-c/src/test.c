
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "feide.h"

int main(void)
{
	char *foo[3];
	char *url, id[1024];
	f_attr_array *a;
	f_config     *c;
	int i, j;

	foo[0] = strdup("eduPersonAffiliation");
	foo[1] = strdup("eduPersonOrgDN");
	foo[2] = NULL;

	c = f_init("https://login.feide.no/moria/Authentication",
	           "demo", "demo", 0);

	printf("Foobar!\n");

	if (f_request_session(c, foo, "http://www.uninett.no/?id=", "", &url) != F_OK) {
		printf("Something bad happened.\n");
		printf("ERROR: %s\n", c->error_str);
		exit(1);
	}

	printf("Got URL: %s\n", url);

	printf("Enter ID: "); fflush(stdout);
	scanf("%s", id);

	f_get_attributes(c, id, &a);

	f_end(c);

	printf("Got %d attributes.\n", a->size);

	for (i = 0; i < a->size; i++) {
		printf("Attr: %s\n", a->attributes[i].name);
		for (j = 0; j < a->attributes[i].size; j++) {
			printf("  - %s\n", a->attributes[i].values[j]);
		}
	}

	f_free_attributes(a);

	exit(0);
}
