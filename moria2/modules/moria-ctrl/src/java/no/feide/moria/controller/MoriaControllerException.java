package no.feide.moria.controller;

/**
 * Superclass for the controller exceptions.
 *
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class MoriaControllerException extends Exception {
    /**
     * Basic constructor.
     *
     * @param message Exception message.
     */
    public MoriaControllerException(final String message) {
        super(message);
    }
}
