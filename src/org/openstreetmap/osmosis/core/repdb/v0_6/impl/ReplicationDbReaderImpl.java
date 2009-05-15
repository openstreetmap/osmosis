// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.database.ReleasableStatementContainer;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSource;


/**
 * Reads streams of changes from a replication database. This is separated from the calling task
 * implementation so that it can be executed multiple times while re-using a single database
 * connection.
 * <p>
 * Note that while this class is a ChangeSource, it only calls the process method of the change
 * sink. It does not call the complete or release methods.
 */
public class ReplicationDbReaderImpl implements ChangeSource, Releasable {
	
	private static final Logger LOG = Logger.getLogger(ReplicationDbReaderImpl.class.getName());
	
	
	private ChangeSink changeSink;
	private DatabaseContext dbCtx;
	private String queueName;
	private ItemDeserializer itemDeserializer;
	private SystemTimestampManager systemTimestampManager;
	private QueueManager queueManager;
	private ReleasableStatementContainer statementContainer;
	private PreparedStatement getItemPayloadStatement;
	private boolean initialized;


	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            Used to access the database.
	 * @param queueName
	 *            The name of the queue to be read from.
	 */
	public ReplicationDbReaderImpl(DatabaseContext dbCtx, String queueName) {
		this.dbCtx = dbCtx;
		this.queueName = queueName;
		
		itemDeserializer = new ItemDeserializer();
		systemTimestampManager = new SystemTimestampManager(dbCtx);
		queueManager = new QueueManager(dbCtx);
		statementContainer = new ReleasableStatementContainer();
	}
	
	
	private void initialize() {
		if (!initialized) {
			getItemPayloadStatement = statementContainer.add(
					dbCtx.prepareStatement("SELECT payload FROM item WHERE itemId > ? AND itemId <= ?"));
		}
	}
	
	
	/**
	 * Reads all data currently available in the queue.
	 */
	public void process() {
		processImpl(systemTimestampManager.getTimestamp());
	}
	
	
	/**
	 * Reads all data in the queue up to the specified timestamp. This timestamp must be less than
	 * or equal to the current system timestamp.
	 * 
	 * @param queueTimestamp
	 *            The queue timestamp to read up to.
	 */
	public void process(Date queueTimestamp) {
		Date systemTimestamp;
		
		// The timestamp that we launch the process with must be less than or equal to the system
		// timestamp. A queue cannot read past the time allowed by the system time.
		systemTimestamp = systemTimestampManager.getTimestamp();
		if (queueTimestamp.compareTo(systemTimestamp) < 0) {
			throw new OsmosisRuntimeException("The requested queue timestamp of " + queueTimestamp
					+ " exceeds the current system timestamp of " + systemTimestamp + ".");
		}
		
		processImpl(queueTimestamp);
	}
	
	
	private void consumeResultSet(ResultSet itemResultSet) {
		ResultSet itemResultSetLocal = itemResultSet;
		
		try {
			while (itemResultSetLocal.next()) {
				byte[] payload;
				ChangeContainer change;
				
				payload = itemResultSetLocal.getBytes("payload");
				
				change = itemDeserializer.deserialize(payload);
				
				changeSink.process(change);
			}
			
			itemResultSetLocal.close();
			itemResultSetLocal = null;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read the item result set.", e);
		} finally {
			if (itemResultSetLocal != null) {
				try {
					itemResultSetLocal.close();
				} catch (SQLException e) {
					LOG.log(Level.WARNING, "Unable to close the result set.", e);
				}
			}
		}
	}


	private void processImpl(Date queueTimestamp) {
		int prmIndex;
		
		initialize();
		
		try {
			long startItem;
			long finishItem;
			
			startItem = queueManager.getQueuePosition(queueName);
			finishItem = queueManager.getPositionForTimestamp(queueTimestamp);
			
			if (finishItem > startItem) {
				prmIndex = 1;
				getItemPayloadStatement.setLong(prmIndex++, startItem);
				getItemPayloadStatement.setLong(prmIndex++, finishItem);
				consumeResultSet(getItemPayloadStatement.executeQuery());
				
				queueManager.seekQueue(queueName, finishItem);
			}
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to retrieve items up to queue timestamp " + queueTimestamp + ".");
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		statementContainer.release();
		initialized = false;
	}
}
