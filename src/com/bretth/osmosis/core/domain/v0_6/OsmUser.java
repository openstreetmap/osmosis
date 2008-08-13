// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;


/**
 * A value class representing a single OSM user, comprised of user name and id.
 * 
 * This class is immutable, and the static factories are thread-safe.
 * 
 * @author Karl Newman
 * @author Brett Henderson
 */
public class OsmUser implements Storeable {
	private String userName;
	private int userId;
	
	
	/**
	 * User ID value to designate no id available.
	 */
	private static final int USER_ID_NONE = 0;
	
	
	/**
	 * The user instance representing no user available or no user applicable.
	 */
	public static final OsmUser NONE = new OsmUser("", USER_ID_NONE);
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param userName
	 *            The name of the user that this object represents.
	 * @param userId
	 *            The userId associated with the user name.
	 */
	public OsmUser(String userName, int userId) {
		if (userName == null) {
			throw new NullPointerException("The user name cannot be null.");
		}
		
		// Disallow a user to be created with the "NONE" id.
		if (NONE != null && userId == USER_ID_NONE) {
			throw new OsmosisRuntimeException("A user id of " + USER_ID_NONE + " is not permitted.");
		}
		
		this.userName = userName;
		this.userId = userId;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public OsmUser(StoreReader sr, StoreClassRegister scr) {
		userName = sr.readString();
		userId = sr.readInteger();
	}
	
	
	/**
	 * Stores all state to the specified store writer.
	 * 
	 * @param sw
	 *            The writer that persists data to an underlying store.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeString(userName);
		sw.writeInteger(userId);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		OsmUser ou;
		
		if (!(o instanceof OsmUser)) {
			return false;
		}
		
		ou = (OsmUser) o;
		
		return userName.equals(ou.userName) && userId == ou.userId;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result;
		
		result = -17;
		result = 31 * result + userName.hashCode();
		result = 31 * result + userId;
		
		return result;
	}
	
	
	/**
	 * @return The userName.
	 */
	public String getUserName() {
		return userName;
	}
	
	
	/**
	 * @return The userId.
	 */
	public int getUserId() {
		return userId;
	}
}
