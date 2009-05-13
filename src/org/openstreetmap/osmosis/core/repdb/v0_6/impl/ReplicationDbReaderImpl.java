// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
	private int queueId;
	private ItemDeserializer itemDeserializer;
	private TimestampManager systemTimestampManager;
	private TimestampManager queueTimestampManager;
	private ReleasableStatementContainer statementContainer;
	private PreparedStatement markItems;
	private PreparedStatement selectMarkedItems;
	private PreparedStatement deleteMarkedItems;
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
		systemTimestampManager = new TimestampManager(dbCtx);
		statementContainer = new ReleasableStatementContainer();
	}
	
	
	private void identifyQueueId() {
		PreparedStatement queueNameStatement = null;
		ResultSet queueNameResultSet = null;
		
		try {
			queueNameStatement = dbCtx.prepareStatementForStreaming("SELECT id FROM queue WHERE name = ?");
			queueNameStatement.setString(1, queueName);
			queueNameResultSet = queueNameStatement.executeQuery();
			
			if (!queueNameResultSet.next()) {
				throw new OsmosisRuntimeException("The queue (" + queueName + ") does not exist.");
			}
			
			queueId = queueNameResultSet.getInt("id");
			
			queueNameResultSet.close();
			queueNameResultSet = null;
			queueNameStatement.close();
			queueNameStatement = null;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to get the id for queue (" + queueName + ").");
			
		} finally {
			if (queueNameResultSet != null) {
				try {
					queueNameResultSet.close();
				} catch (SQLException e) {
					LOG.log(Level.WARNING, "Unable to close the result set.", e);
				}
			}
			if (queueNameStatement != null) {
				try {
					queueNameStatement.close();
				} catch (SQLException e) {
					LOG.log(Level.WARNING, "Unable to close the prepared statement.", e);
				}
			}
		}
	}
	
	
	private void initialize() {
		if (!initialized) {
			// Get the id of the queue.
			identifyQueueId();
			
			markItems = statementContainer.add(dbCtx
					.prepareStatement("UPDATE item_queue iq INNER JOIN item i ON iq.item_id = i.id"
							+ " SET iq.selected = TRUE WHERE iq.queue_id = ? AND tstamp <= ?"));
			
			selectMarkedItems = statementContainer.add(
					dbCtx.prepareStatementForStreaming(
							"SELECT i.payload FROM item i INNER JOIN item_queue iq ON i.id = iq.item_id"
							+ " WHERE iq.queue_id = ? AND iq.selected = TRUE"));
			
			deleteMarkedItems = statementContainer.add(dbCtx
					.prepareStatement("DELETE FROM item_queue WHERE iq.queue_id = ? AND selected = TRUE"));
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
	}
	
	
	private void consumeResultSet(ResultSet itemResultSet) {
		try {
			while (itemResultSet.next()) {
				byte[] payload;
				ChangeContainer change;
				
				payload = itemResultSet.getBytes("payload");
				
				change = itemDeserializer.deserialize(payload);
				
				changeSink.process(change);
			}
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read the item result set.", e);
		} finally {
			if (itemResultSet != null) {
				try {
					itemResultSet.close();
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
			prmIndex = 1;
			markItems.setInt(prmIndex++, queueId);
			markItems.setTimestamp(prmIndex++, new Timestamp(queueTimestamp.getTime()));
			markItems.executeUpdate();
			
			selectMarkedItems.setInt(1, queueId);
			consumeResultSet(selectMarkedItems.executeQuery());
			
			deleteMarkedItems.setInt(1, queueId);
			deleteMarkedItems.executeUpdate();
			
			queueTimestampManager.setTimestamp(queueTimestamp);
			
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
