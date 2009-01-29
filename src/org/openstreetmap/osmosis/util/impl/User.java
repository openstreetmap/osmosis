// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.util.impl;


/**
 * Represents a single record from the user table.
 * 
 * @author Brett Henderson
 */
public class User {
	private long id;
	private String displayName;
	private boolean dataPublic;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The user id.
	 * @param displayName
	 *            The user display name.
	 * @param dataPublic
	 *            The user's public edit flag.
	 */
	public User(long id, String displayName, boolean dataPublic) {
		this.id = id;
		this.displayName = displayName;
		this.dataPublic = dataPublic;
	}
	
	
	/**
	 * The unique identifier of the user.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return id;
	}
	
	
	/**
	 * The publicly displayed name of the user.
	 * 
	 * @return The display name.
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	
	/**
	 * Indicates if the user's edits are public.
	 * 
	 * @return True if the edits are public.
	 */
	public boolean isDataPublic() {
		return dataPublic;
	}
}
