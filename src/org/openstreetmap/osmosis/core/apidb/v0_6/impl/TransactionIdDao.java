// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;


/**
 * Reads active transaction ids from the database allowing up-to-current queries to be performed
 * when extracting changesets from the history tables.
 */
public class TransactionIdDao {
	private SimpleJdbcTemplate jdbcTemplate;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate Used to access the database.
	 */
	public TransactionIdDao(SimpleJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	
	/**
	 * Obtains the current database snapshot.
	 * 
	 * @return The transaction snapshot.
	 */
	public TransactionSnapshot getTransactionSnapshot() {
		String snapshotString;
		TransactionSnapshot snapshot; 
		
		snapshotString = jdbcTemplate.queryForObject("", String.class);
		
		snapshot = new TransactionSnapshot(snapshotString);
		
		return snapshot;
	}
}
