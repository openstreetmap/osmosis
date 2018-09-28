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
     * Constructs a new exception.
     */
    public OsmosisException() {
		super();
	}
	
	
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public OsmosisException(String message) {
		super(message);
	}
	
    
    /**
     * Constructs a new exception with the specified cause.
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
