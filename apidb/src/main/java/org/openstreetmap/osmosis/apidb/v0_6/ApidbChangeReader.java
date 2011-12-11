// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6;

import java.util.Collections;
import java.util.Date;

import org.openstreetmap.osmosis.apidb.common.DatabaseContext2;
import org.openstreetmap.osmosis.apidb.v0_6.impl.AllEntityDao;
import org.openstreetmap.osmosis.apidb.v0_6.impl.DeltaToDiffReader;
import org.openstreetmap.osmosis.apidb.v0_6.impl.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;


/**
 * A change source reading from database history tables. This aims to be suitable for running at
 * regular intervals with database overhead proportional to changeset size.
 * 
 * @author Brett Henderson
 */
public class ApidbChangeReader implements RunnableChangeSource {

    private ChangeSink changeSink;
    private DatabaseLoginCredentials loginCredentials;
    private DatabasePreferences preferences;
    private Date intervalBegin;
    private Date intervalEnd;
    private boolean fullHistory;


	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 * @param fullHistory
	 *            Specifies if full version history should be returned, or just a single change per
	 *            entity for the interval.
	 */
    public ApidbChangeReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
            Date intervalBegin, Date intervalEnd, boolean fullHistory) {
        this.loginCredentials = loginCredentials;
        this.preferences = preferences;
        this.intervalBegin = intervalBegin;
        this.intervalEnd = intervalEnd;
        this.fullHistory = fullHistory;
    }

    /**
     * {@inheritDoc}
     */
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
    	try {
    		AllEntityDao entityDao;
    		ReleasableIterator<ChangeContainer> reader;
    		
    		changeSink.initialize(Collections.<String, Object>emptyMap());
    		
	        new SchemaVersionValidator(loginCredentials, preferences)
	                .validateVersion(ApidbVersionConstants.SCHEMA_MIGRATIONS);
	        
	        entityDao = new AllEntityDao(dbCtx.getJdbcTemplate());
	        
	        reader = entityDao.getHistory(intervalBegin, intervalEnd);
	        if (!fullHistory) {
	        	reader = new DeltaToDiffReader(reader);
	        }
	        try {
	        	while (reader.hasNext()) {
	        		changeSink.process(reader.next());
	        	}
	        	
	        } finally {
	        	reader.release();
	        }
	
	        changeSink.complete();
	        
    	} finally {
    		changeSink.release();
    	}
    }
    

    /**
     * Reads all data from the database and send it to the sink.
     */
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
