// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6;

import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext2;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.EntitySnapshotReader;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.NodeDao;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.RelationDao;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.WayDao;
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
 * An OSM data source reading from a database. The entire contents of the database are read.
 * 
 * @author Brett Henderson
 */
public class ApidbReader implements RunnableSource {

    private Sink sink;

    private DatabaseLoginCredentials loginCredentials;
    private DatabasePreferences preferences;
    private Date snapshotInstant;


    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param preferences Contains preferences configuring database behaviour.
     * @param snapshotInstant The state of the node table at this point in time will be dumped. This
     *        ensures a consistent snapshot.
     */
    public ApidbReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
            Date snapshotInstant) {
        this.loginCredentials = loginCredentials;
        this.preferences = preferences;
        this.snapshotInstant = snapshotInstant;
        
    }

    /**
     * {@inheritDoc}
     */
    public void setSink(Sink sink) {
        this.sink = sink;
    }

    /**
     * Reads all nodes from the database and sends to the sink.
     */
    private void processNodes(DatabaseContext2 dbCtx) {
    	NodeDao entityDao;
    	ReleasableIterator<EntityContainer> reader;
    	
    	entityDao = new NodeDao(dbCtx.getJdbcTemplate());

    	reader = new EntitySnapshotReader(entityDao.getHistory(), snapshotInstant);

        try {
            while (reader.hasNext()) {
            	sink.process(reader.next());
            }

        } finally {
            reader.release();
        }
    }

    /**
     * Reads all ways from the database and sends to the sink.
     */
    private void processWays(DatabaseContext2 dbCtx) {
    	WayDao entityDao;
    	ReleasableIterator<EntityContainer> reader;
    	
    	entityDao = new WayDao(dbCtx.getJdbcTemplate());

    	reader = new EntitySnapshotReader(entityDao.getHistory(), snapshotInstant);

        try {
            while (reader.hasNext()) {
            	sink.process(reader.next());
            }

        } finally {
            reader.release();
        }
    }

    /**
     * Reads all relations from the database and sends to the sink.
     */
    private void processRelations(DatabaseContext2 dbCtx) {
    	RelationDao entityDao;
    	ReleasableIterator<EntityContainer> reader;
    	
    	entityDao = new RelationDao(dbCtx.getJdbcTemplate());

    	reader = new EntitySnapshotReader(entityDao.getHistory(), snapshotInstant);

        try {
            while (reader.hasNext()) {
            	sink.process(reader.next());
            }

        } finally {
            reader.release();
        }
    }
    
    
    /**
	 * Runs the task implementation. This is called by the run method within a transaction.
	 * 
	 * @param dbCtx
	 *            Used to access the database.
	 */
    protected void runImpl(DatabaseContext2 dbCtx) {
    	try {
	        new SchemaVersionValidator(loginCredentials, preferences)
	                .validateVersion(ApidbVersionConstants.SCHEMA_MIGRATIONS);
	
	        sink.process(new BoundContainer(new Bound("Osmosis " + OsmosisConstants.VERSION)));
	        processNodes(dbCtx);
	        processWays(dbCtx);
	        processRelations(dbCtx);
	
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
        		DatabaseContext2 dbCtxInner = dbCtx;

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					runImpl(dbCtxInner);
				}});

        } finally {
            dbCtx.release();
        }
    }
}
