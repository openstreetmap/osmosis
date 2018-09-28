// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * A runtime exception indicating that the end of the store has been reached while reading.
 * 
 * @author Brett Henderson
 */
public class EndOfStoreException extends OsmosisRuntimeException {

	private static final long serialVersionUID = 1L;
	
	
	/**
     * Constructs a new exception.
     */
    public EndOfStoreException() {
		super();
	}
    
	
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public EndOfStoreException(String message) {
		super(message);
	}
    
	
    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param cause the cause.
     */
    public EndOfStoreException(Throwable cause) {
		super(cause);
	}
    
	
    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public EndOfStoreException(String message, Throwable cause) {
		super(message, cause);
	}
}
