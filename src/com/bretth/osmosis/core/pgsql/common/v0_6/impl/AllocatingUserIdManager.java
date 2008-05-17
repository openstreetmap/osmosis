// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.common.v0_6.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Allocates and manages identifiers for user names.
 * 
 * @author Brett Henderson
 */
public class AllocatingUserIdManager {
	
	private long currentIdValue;
	private Map<String, Long> userIdMap;
	
	
	/**
	 * Creates a new instance.
	 */
	public AllocatingUserIdManager() {
		currentIdValue = 0;
		
		userIdMap = new HashMap<String, Long>();
	}
	
	
	/**
	 * Returns the user id associated with the user name, if one doesn't exist a
	 * new id will be allocated.
	 * 
	 * @param userName
	 *            The user name.
	 * @return The id associated with the user name.
	 */
	public long getUserId(String userName) {
		long id;
		
		if (userIdMap.containsKey(userName)) {
			id = userIdMap.get(userName).longValue();
		} else {
			id = ++currentIdValue;
			userIdMap.put(userName, Long.valueOf(id));
		}
		
		return id;
	}
	
	
	/**
	 * Returns the maximum id allocated.
	 * 
	 * @return The maximum id.
	 */
	public long getMaximumId() {
		return currentIdValue;
	}
	
	
	/**
	 * Returns the internal map between user names and their identifiers.
	 * 
	 * @return A read-only version of the user id map.
	 */
	public Map<String, Long> getUserIdMap() {
		return Collections.unmodifiableMap(userIdMap);
	}
}
