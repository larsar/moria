/*
 * $Id$
 * 
 */
package no.feide.moria.store;

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class UnknownTicketException extends Exception {

    	public UnknownTicketException() {
    	    super();
    	}
    	
    	public UnknownTicketException(String message) {
    	    super(message);
    	}
}
