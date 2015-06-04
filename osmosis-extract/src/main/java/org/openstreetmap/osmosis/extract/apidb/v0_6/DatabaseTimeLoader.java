// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.extract.apidb.v0_6;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;


/**
 * Loads the current time from the database. This avoids relying on the clock of this system which
 * may be different. If this system was to skew ahead of the database server it may be possible for
 * data to be missed during extraction operations.
 * 
 * @author Brett Henderson
 */
public class DatabaseTimeLoader {
	private DatabaseLoginCredentials loginCredentials;
	
	
	/**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     */
	public DatabaseTimeLoader(DatabaseLoginCredentials loginCredentials) {
		this.loginCredentials = loginCredentials;
	}
	
	
	private Date readTimeField(ResultSet timeSet) {
		try (ResultSet rs = timeSet) {
			Date dbTime;
			timeSet.next();
			
			dbTime = timeSet.getTimestamp("SystemTime");
			return new Date(dbTime.getTime());

		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read the time from the database server.", e);
		}
	}
	
	
	/**
	 * Gets the current system time according to the database server.
	 * 
	 * @return The current system time.
	 */
	public Date getDatabaseTime() {
		try (DatabaseContext dbCtx = new DatabaseContext(loginCredentials)) {
			ResultSet rs;
			Date result;
			
			rs = dbCtx.executeQuery("SELECT now() AS SystemTime");
			result = readTimeField(rs);
			
			return result;
			
		}
	}
}
