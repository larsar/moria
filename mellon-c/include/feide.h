
/* feide.h
 *
 * client authentication library for the FEIDE project.
 *
 */

#ifndef FEIDE_H
#define FEIDE_H

/*
 * the new set of return codes that are unified for the whole API.
 */

#define F_OK              0 /* the function returned as normal */
#define F_SERVER_ERROR    1 /* server signalled an error */
#define F_MESSAGE_ERROR   2 /* message was malformed in some way */
#define F_MEMORY_ERROR    3 /* out of memory */
#define F_TRANSPORT_ERROR 4 /* an error occured when sending a message */
#define F_ERROR           5 /* something went wrong, and we're not sure what */


/* a structure that library configuration data is put in.  earlier this was
 * in static variables inside, but they have been moved here due to thread
 * safety compliance.
 */
typedef struct f_config {
	char *userid;
	char *passwd;
	char *url;
	char *error_str;
	int   sso;
} f_config;


/* get_attributes() depends on these datastructures to build up the data
 * you need.
 */
typedef struct f_attr {
	char *name;
	char **values;
	int size;
} f_attr;

typedef struct f_attr_array {
	f_attr *attributes;
	int size;
} f_attr_array;


/* function prototypes */
f_config *f_init(char *, char *, char *, int);
char     *f_error(f_config *);
void      f_end(f_config *);
int       f_request_session(f_config *, char *[], char *, char *, char **);
int       f_get_attributes(f_config *, char *, f_attr_array **);
void      f_free_attributes(f_attr_array *);

#endif  /* FEIDE_H */
