// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.common;


/**
 * Mysql implementation of an identity value loader.
 * 
 * @author Brett Henderson
 */
public class MysqlIdentityValueLoader2 implements IdentityValueLoader {
	private static final String SQL_SELECT_LAST_INSERT_ID =
		"SELECT LAST_INSERT_ID() AS lastInsertId FROM DUAL";
	
	private DatabaseContext2 dbCtx;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for all database access.
	 */
	public MysqlIdentityValueLoader2(DatabaseContext2 dbCtx) {
		this.dbCtx = dbCtx;
	}
	
	
	/**
	 * Returns the id of the most recently inserted row on the current
	 * connection.
	 * 
	 * @return The newly inserted id.
	 */
	public long getLastInsertId() {
		return dbCtx.getJdbcTemplate().queryForLong(SQL_SELECT_LAST_INSERT_ID);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLastSequenceId(String sequenceName) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		// Do nothing.
	}
}
