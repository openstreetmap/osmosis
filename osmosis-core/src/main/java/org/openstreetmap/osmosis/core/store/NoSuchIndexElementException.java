// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * A runtime exception indicating that the requested index value does not exist.
 * 
 * @author Brett Henderson
 */
public class NoSuchIndexElementException extends OsmosisRuntimeException {

	private static final long serialVersionUID = 1L;
	
	
	/**
     * Constructs a new exception.
     */
    public NoSuchIndexElementException() {
		super();
	}
    
	
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public NoSuchIndexElementException(String message) {
		super(message);
	}
    
	
    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param cause the cause.
     */
    public NoSuchIndexElementException(Throwable cause) {
		super(cause);
	}
    
	
    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public NoSuchIndexElementException(String message, Throwable cause) {
		super(message, cause);
	}
}
