// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext2;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.AllEntityDao;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.FileReplicationDestination;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.ReplicationDestination;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.ReplicationSource;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.Replicator;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.SystemTimeLoader;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.TimeDao;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.TransactionDao;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.TransactionSnapshotLoader;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;


/**
 * Performs replication from an API database into change files.
 */
public class ApidbFileReplicator implements RunnableTask {
	
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private File workingDirectory;
	
	
	/**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param preferences Contains preferences configuring database behaviour.
     * @param workingDirectory The directory to store all output files.
     */
    public ApidbFileReplicator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
            File workingDirectory) {
    	this.loginCredentials = loginCredentials;
    	this.preferences = preferences;
    	this.workingDirectory = workingDirectory;
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
		ReplicationDestination destination;
		TransactionSnapshotLoader txnSnapshotLoader;
		SystemTimeLoader systemTimeLoader;
		
		new SchemaVersionValidator(loginCredentials, preferences)
				.validateVersion(ApidbVersionConstants.SCHEMA_MIGRATIONS);
		
		source = new AllEntityDao(dbCtx.getJdbcTemplate());
		destination = new FileReplicationDestination(workingDirectory);
		txnSnapshotLoader = new TransactionDao(dbCtx.getJdbcTemplate());
		systemTimeLoader = new TimeDao(dbCtx.getJdbcTemplate());
		
		replicator = new Replicator(source, destination, txnSnapshotLoader, systemTimeLoader);
		
		replicator.replicate();
    }


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
        final DatabaseContext2 dbCtx = new DatabaseContext2(loginCredentials);
    	
        try {
        	dbCtx.executeWithinTransaction(new TransactionCallbackWithoutResult() {
        		private DatabaseContext2 dbCtxInner = dbCtx;

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					runImpl(dbCtxInner);
				} });

        } finally {
            dbCtx.release();
        }
	}
}
