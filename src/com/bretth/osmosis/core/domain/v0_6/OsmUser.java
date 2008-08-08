// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * A value class representing a single OSM user, comprised of user name and id.
 * 
 * This class is immutable, and the static factories are thread-safe.
 * 
 * @author Karl Newman
 * @author Brett Henderson
 */
public class OsmUser {
	private String userName;
	private int userId;
	/** User ID value to designate no id available */
	public static final int USER_ID_NONE = 0;
	
	
	/**
	 * The user instance representing no user available.
	 */
	public static final OsmUser NO_USER;
	
	/**
	 *  Canonicalization of OsmUser instances. This may not be optimal, because Osmosis' pipeline
	 *  approach helps to ensure that objects won't hang around very long, so by keeping a single
	 *  instance of each unique OsmUser, this Map may actually cause more memory consumption than
	 *  otherwise. If that turns out to be the case, it's easy enough to change the implementation
	 *  of the static factory. 
	 */
	private static ConcurrentMap<OsmUser, OsmUser> userMap;

	static {
		/* make sure the Map is initialized before attempting a getInstance */
		userMap = new ConcurrentHashMap<OsmUser, OsmUser>();
		NO_USER = OsmUser.getInstance("", USER_ID_NONE);
	}

	
	/**
	 * Creates a new instance.
	 * 
	 * @param userName
	 *            The name of the user that this object represents.
	 * @param userId
	 *            The userId associated with the user name.
	 */
	private OsmUser(String userName, int userId) {
		if (userName == null) {
			throw new NullPointerException("The user name cannot be null.");
		}
		
		this.userName = userName;
		this.userId = userId;
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
	 * Factory method to retrieve an OSM User instance from a given user name
	 * and user id.
	 * 
	 * @param userName
	 *            The name of the user.
	 * @param userId
	 *            The userId associated with the user name.
	 * @return The instance matching the request parameters.
	 */
	public static OsmUser getInstance(String userName, int userId) {
		OsmUser newUser = new OsmUser(userName, userId);
		OsmUser result = userMap.get(newUser);
		if (result == null) {
			result = userMap.putIfAbsent(newUser, newUser);
			if (result == null) {
				result = newUser;
			}
		}
		
		return result;
	}
	
	
	/**
	 * Factory method to retrieve an OSM User instance from a StoreReader and StoreClassRegister.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 * @return The instance loaded from the store.
	 */
	public static OsmUser getInstance(StoreReader sr, StoreClassRegister scr) {
		return getInstance(sr.readString(), sr.readInteger());
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
