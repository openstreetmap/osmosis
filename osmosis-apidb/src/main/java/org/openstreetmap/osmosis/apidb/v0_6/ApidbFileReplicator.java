// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6;

import org.openstreetmap.osmosis.apidb.common.DatabaseContext2;
import org.openstreetmap.osmosis.apidb.v0_6.impl.AllEntityDao;
import org.openstreetmap.osmosis.apidb.v0_6.impl.ReplicationSource;
import org.openstreetmap.osmosis.apidb.v0_6.impl.Replicator;
import org.openstreetmap.osmosis.apidb.v0_6.impl.SchemaVersionValidator;
import org.openstreetmap.osmosis.apidb.v0_6.impl.SystemTimeLoader;
import org.openstreetmap.osmosis.apidb.v0_6.impl.TimeDao;
import org.openstreetmap.osmosis.apidb.v0_6.impl.TransactionDao;
import org.openstreetmap.osmosis.apidb.v0_6.impl.TransactionManager;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;


/**
 * Performs replication from an API database into change files.
 */
public class ApidbFileReplicator implements RunnableChangeSource {
	
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private int iterations;
	private int minInterval;
	private int maxInterval;
	private ChangeSink changeSink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param iterations
	 *            The number of replication intervals to execute. 0 means
	 *            infinite.
	 * @param minInterval
	 *            The minimum number of milliseconds between intervals.
	 * @param maxInterval
	 *            The maximum number of milliseconds between intervals if no new
	 *            data is available. This isn't a hard limit because proces
	 */
    public ApidbFileReplicator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
            int iterations, int minInterval, int maxInterval) {
    	this.loginCredentials = loginCredentials;
    	this.preferences = preferences;
    	this.iterations = iterations;
    	this.minInterval = minInterval;
    	this.maxInterval = maxInterval;
    }


    /**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
    
    
    /**
	 * Runs the task implementation. This is called by the run method within a transaction.
	 * 
	 * @param dbCtx
	 *            Used to access the database.
	 */
    protected void runImpl(DatabaseContext2 dbCtx) {
		Replicator replicator;
		ReplicationSource source;
		TransactionManager txnSnapshotLoader;
		SystemTimeLoader systemTimeLoader;
		
		new SchemaVersionValidator(loginCredentials, preferences)
				.validateVersion(ApidbVersionConstants.SCHEMA_MIGRATIONS);
		
		source = new AllEntityDao(dbCtx.getJdbcTemplate());
		txnSnapshotLoader = new TransactionDao(dbCtx);
		systemTimeLoader = new TimeDao(dbCtx.getJdbcTemplate());
		
		replicator = new Replicator(source, changeSink, txnSnapshotLoader, systemTimeLoader, iterations, minInterval,
				maxInterval);
		
		replicator.replicate();
    }


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
        final DatabaseContext2 dbCtx = new DatabaseContext2(loginCredentials);
    	
        try {
        	runImpl(dbCtx);

        } finally {
            dbCtx.release();
        }
	}
}
