// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core;


/**
 * The root of the unchecked exception hierarchy for the application. All typed
 * runtime exceptions subclass this exception.
 * 
 * @author Brett Henderson
 */
public class OsmosisRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	
	/**
     * Constructs a new exception with <code>null</code> as its detail message.
     */
    public OsmosisRuntimeException() {
		super();
	}
    
	
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message.
     */
    public OsmosisRuntimeException(String message) {
		super(message);
	}
    
	
    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * 
     * @param cause the cause.
     */
    public OsmosisRuntimeException(Throwable cause) {
		super(cause);
	}
    
	
    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public OsmosisRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
