// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.common;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * A runtime exception indicating that the requested database record doesn't exist.
 * 
 * @author Brett Henderson
 */
public class NoSuchRecordException extends OsmosisRuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	
	/**
     * Constructs a new exception.
     */
    public NoSuchRecordException() {
		super();
	}
    
	
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public NoSuchRecordException(String message) {
		super(message);
	}
    
	
    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param cause the cause.
     */
    public NoSuchRecordException(Throwable cause) {
		super(cause);
	}
    
	
    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public NoSuchRecordException(String message, Throwable cause) {
		super(message, cause);
	}
}
