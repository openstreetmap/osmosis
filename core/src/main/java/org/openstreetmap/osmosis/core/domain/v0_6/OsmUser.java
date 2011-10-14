// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A value class representing a single OSM user, comprised of user name and id.
 * 
 * This class is immutable, and the static factories are thread-safe.
 * 
 * @author Karl Newman
 * @author Brett Henderson
 */
public class OsmUser implements Storeable {
	private String name;
	private int id;
	
	/**
	 * User ID value to designate no id available. If this is set to 0, some
	 * databases (ie. MySQL) will default a non-zero value upon insert. To avoid
	 * special case code for each database with this issue, this value is made
	 * negative.
	 */
	private static final int USER_ID_NONE = -1;
	
	
	/**
	 * The user instance representing no user available or no user applicable.
	 */
	public static final OsmUser NONE = new OsmUser(USER_ID_NONE, "");
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The userId associated with the user name.
	 * @param userName
	 *            The name of the user that this object represents.
	 */
	public OsmUser(int id, String userName) {
		if (userName == null) {
			throw new NullPointerException("The user name cannot be null.");
		}
		
		// Disallow a user to be created with the "NONE" id.
		if (NONE != null && id == USER_ID_NONE) {
			throw new OsmosisRuntimeException("A user id of " + USER_ID_NONE + " is not permitted.");
		}
		
		this.name = userName;
		this.id = id;
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
		name = sr.readString();
		id = sr.readInteger();
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
		sw.writeString(name);
		sw.writeInteger(id);
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
		
		return name.equals(ou.name) && id == ou.id;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result;
		
		result = -17;
		result = 31 * result + name.hashCode();
		result = 31 * result + id;
		
		return result;
	}
	
	
	/**
	 * @return The userId.
	 */
	public int getId() {
		return id;
	}
	
	
	/**
	 * @return The name of the user.
	 */
	public String getName() {
		return name;
	}
}
