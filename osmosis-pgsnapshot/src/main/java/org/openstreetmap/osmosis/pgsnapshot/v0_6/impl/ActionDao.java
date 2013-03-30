// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * Performs all action db operations.
 * 
 * @author Brett Henderson
 */
public class ActionDao {
	private static final String SQL_INSERT = "INSERT INTO actions(data_type, action, id) VALUES(?, ?, ?)";
	private static final String SQL_TRUNCATE = "TRUNCATE actions";
	
	private JdbcTemplate jdbcTemplate;
	private DatabaseCapabilityChecker capabilityChecker;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public ActionDao(DatabaseContext dbCtx) {
		jdbcTemplate = dbCtx.getJdbcTemplate();
		
		capabilityChecker = new DatabaseCapabilityChecker(dbCtx);
	}
	
	
	/**
	 * Adds the specified action to the database.
	 * 
	 * @param dataType The type of data being represented by this action. 
	 * @param action The action being performed on the data.
	 * @param id The identifier of the data. 
	 */
	public void addAction(ActionDataType dataType, ChangesetAction action, long id) {
		if (capabilityChecker.isActionSupported()) {
			jdbcTemplate.update(SQL_INSERT, dataType.getDatabaseValue(), action.getDatabaseValue(), id);
		}
	}
	
	
	/**
	 * Removes all action records.
	 */
	public void truncate() {
		if (capabilityChecker.isActionSupported()) {
			jdbcTemplate.update(SQL_TRUNCATE);
		}
	}
}
