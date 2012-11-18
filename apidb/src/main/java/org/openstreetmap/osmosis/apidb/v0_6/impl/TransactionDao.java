// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.apidb.common.DatabaseContext2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;


/**
 * Reads active transaction ids from the database allowing up-to-current queries to be performed
 * when extracting changesets from the history tables.
 */
public class TransactionDao implements TransactionManager {
	private static final Logger LOG = Logger.getLogger(TransactionDao.class.getName());
	
	private DatabaseContext2 dbCtx;
	private JdbcTemplate jdbcTemplate;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            Used to access the database.
	 */
	public TransactionDao(DatabaseContext2 dbCtx) {
		this.dbCtx = dbCtx;
		
		jdbcTemplate = dbCtx.getJdbcTemplate();
	}
	
	
	@Override
	public TransactionSnapshot getTransactionSnapshot() {
		String snapshotString;
		TransactionSnapshot snapshot; 
		
		snapshotString = jdbcTemplate.queryForObject("SELECT txid_current_snapshot()", String.class);
		
		snapshot = new TransactionSnapshot(snapshotString);
		
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Loaded new database snapshot, xmin=" + snapshot.getXMin()
					+ ", xmax=" + snapshot.getXMax()
					+ ", xiplist=" + snapshot.getXIpList());
		}
		
		return snapshot;
	}


	@Override
	public void executeWithinTransaction(final Runnable target) {
		dbCtx.executeWithinTransaction(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				target.run();
			}
		});
	}
}
