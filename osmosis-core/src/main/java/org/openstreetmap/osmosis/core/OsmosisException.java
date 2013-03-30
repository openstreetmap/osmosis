// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core;


/**
 * The root of the checked exception hierarchy for the application. All typed
 * exceptions subclass this exception.
 * 
 * @author Brett Henderson
 */
public abstract class OsmosisException extends Exception {
	
	private static final long serialVersionUID = 1L; 
	
	
	/**
     * Constructs a new exception with <code>null</code> as its detail message.
     */
    public OsmosisException() {
		super();
	}
	
	
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message.
     */
    public OsmosisException(String message) {
		super(message);
	}
	
    
    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * 
     * @param cause the cause.
     */
    public OsmosisException(Throwable cause) {
		super(cause);
	}
	
	
    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public OsmosisException(String message, Throwable cause) {
		super(message, cause);
	}
}
