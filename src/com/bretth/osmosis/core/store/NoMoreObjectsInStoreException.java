package com.bretth.osmosis.core.store;


/**
 * A runtime exception indicating that no more objects are available to be
 * retrieved from the store. This is a special case of EndOfStoreException and
 * should be the exception caught when reading entire objects off the store.
 * This will only be thrown if the previous object was read in its entirety and
 * no more data is available, EndOfStoreException will be thrown in all cases
 * where the end of store has been reached.
 * 
 * @author Brett Henderson
 */
public class NoMoreObjectsInStoreException extends EndOfStoreException {

	private static final long serialVersionUID = 1L;
	
	
	/**
     * Constructs a new exception with <code>null</code> as its detail message.
     */
    public NoMoreObjectsInStoreException() {
		super();
	}
    
	
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message.
     */
    public NoMoreObjectsInStoreException(String message) {
		super(message);
	}
    
	
    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * 
     * @param cause the cause.
     */
    public NoMoreObjectsInStoreException(Throwable cause) {
		super(cause);
	}
    
	
    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public NoMoreObjectsInStoreException(String message, Throwable cause) {
		super(message, cause);
	}
}
