// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.database.ReleasableStatementContainer;
import org.openstreetmap.osmosis.core.pgsql.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.ItemSerializer;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.ReplicationDbVersionConstants;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.SystemTimestampManager;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;


/**
 * Writes a stream of changes into a replication database.
 */
public class ReplicationDbWriter implements ChangeSink {
	
	private static final Logger LOG = Logger.getLogger(ReplicationDbWriter.class.getName());
	
	
	private DatabaseContext dbCtx;
	private ReleasableStatementContainer statementContainer;
	private PreparedStatement insertItemStatement;
	private boolean initialized;
	private ItemSerializer itemSerializer;
	private SystemTimestampManager timestampManager;
	private Date systemTimestamp;
	private SchemaVersionValidator schemaVersionValidator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param systemTimestamp
	 *            The systemTimestamp marking the end of the time period of change data.
	 */
	public ReplicationDbWriter(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
			Date systemTimestamp) {
		dbCtx = new DatabaseContext(loginCredentials);
		
		itemSerializer = new ItemSerializer();
		timestampManager = new SystemTimestampManager(dbCtx);
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials, preferences);
	}
	
	
	private void initialize() {
		if (!initialized) {
			insertItemStatement = dbCtx.prepareStatement("INSERT INTO item (tstamp, payload) VALUES (?, ?)");
			
			schemaVersionValidator.validateVersion(ReplicationDbVersionConstants.SCHEMA_VERSION);
			
			initialized = true;
		}
	}
	
	
	private void insertItem(byte[] data, Date itemTimestamp) {
		try {
			int prmIndex;
			
			prmIndex = 1;
			insertItemStatement.setTimestamp(prmIndex++, new Timestamp(itemTimestamp.getTime()));
			insertItemStatement.setBytes(prmIndex++, data);
			insertItemStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert a new item.", e);
		}
		
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "The item has been added with id " + dbCtx.getLastSequenceId("item_id_seq") + ".");
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ChangeContainer change) {
		Date timestamp;
		byte[] data;
		
		initialize();
		
		timestamp = change.getEntityContainer().getEntity().getTimestamp();
		data = itemSerializer.serialize(change);
		
		insertItem(data, timestamp);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		timestampManager.setTimestamp(systemTimestamp);
		
		dbCtx.commit();
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		initialized = false;
		
		statementContainer.release();
		
		dbCtx.release();
	}
}
