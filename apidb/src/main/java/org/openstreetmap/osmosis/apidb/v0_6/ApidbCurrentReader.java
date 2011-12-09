// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6;

import java.util.Collections;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.apidb.common.DatabaseContext2;
import org.openstreetmap.osmosis.apidb.v0_6.impl.AllEntityDao;
import org.openstreetmap.osmosis.apidb.v0_6.impl.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;


/**
 * An OSM data source reading from a databases current tables. The entire contents of the database
 * are read.
 * 
 * @author Brett Henderson
 */
public class ApidbCurrentReader implements RunnableSource {

    private Sink sink;
    private DatabaseLoginCredentials loginCredentials;
    private DatabasePreferences preferences;


    /**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
    public ApidbCurrentReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
        this.loginCredentials = loginCredentials;
        this.preferences = preferences;
    }


    /**
     * {@inheritDoc}
     */
    public void setSink(Sink sink) {
        this.sink = sink;
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
    		ReleasableIterator<EntityContainer> reader;
    		
    		sink.initialize(Collections.<String, Object>emptyMap());
    		
	        new SchemaVersionValidator(loginCredentials, preferences)
	                .validateVersion(ApidbVersionConstants.SCHEMA_MIGRATIONS);
	        
	        entityDao = new AllEntityDao(dbCtx.getJdbcTemplate());
	        
	        sink.process(new BoundContainer(new Bound("Osmosis " + OsmosisConstants.VERSION)));
	        reader = entityDao.getCurrent();
	        try {
	        	while (reader.hasNext()) {
	        		sink.process(reader.next());
	        	}
	        	
	        } finally {
	        	reader.release();
	        }
	
	        sink.complete();
	        
    	} finally {
    		sink.release();
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
