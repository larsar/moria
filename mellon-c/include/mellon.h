
/* mellon.h
 *
 * client authentication library for the FEIDE project.
 *
 */

#ifndef MELLON_H
#define MELLON_H

/*
 * the new set of return codes that are unified for the whole API.
 */

#define M_OK              0 /* the function returned as normal */
#define M_SERVER_ERROR    1 /* server signalled an error */
#define M_MESSAGE_ERROR   2 /* message was malformed in some way */
#define M_MEMORY_ERROR    3 /* out of memory */
#define M_TRANSPORT_ERROR 4 /* an error occured when sending a message */
#define M_ERROR           5 /* something went wrong, and we're not sure what */


/* a structure that library configuration data is put in.  earlier this was
 * in static variables inside, but they have been moved here due to thread
 * safety compliance.
 */
typedef struct m_config {
	char *userid;
	char *passwd;
	char *url;
	char *error_str;
	int   sso;
} m_config;


/* get_attributes() depends on these datastructures to build up the data
 * you need.
 */
typedef struct m_attr {
	char *name;
	char **values;
	int size;
} m_attr;

typedef struct m_attr_array {
	m_attr *attributes;
	int size;
} m_attr_array;


/* function prototypes */
m_config *m_init(char *, char *, char *, int);
char     *m_error(m_config *);
void      m_end(m_config *);
int       m_request_session(m_config *, char *[], char *, char *, char **);
int       m_get_attributes(m_config *, char *, m_attr_array **);
void      m_free_attributes(m_attr_array *);

#endif  /* MELLON_H */
