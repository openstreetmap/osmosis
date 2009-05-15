// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.ReleasableStatementContainer;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;


/**
 * Provides the ability to manipulate queues (eg. add and remove).
 */
public class QueueManager implements Releasable {
	
	private static final Logger LOG = Logger.getLogger(QueueManager.class.getName());
	
	
	private DatabaseContext dbCtx;
	private boolean initialized;
	private ReleasableStatementContainer statementContainer;
	private PreparedStatement createQueueStatement;
	private PreparedStatement deleteQueueStatement;
	private PreparedStatement listQueuesStatement;
	private PreparedStatement getTimestampIdStatement;
	private PreparedStatement seekQueueStatement;
	private PreparedStatement getMaxItemIdStatement;
	private PreparedStatement getQueueItemIdStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx Used to access the database.
	 */
	public QueueManager(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		
		statementContainer = new ReleasableStatementContainer();
	}
	
	
	private void initialize() {
		if (!initialized) {
			createQueueStatement = statementContainer.add(
					dbCtx.prepareStatement("INSERT INTO queue (name, last_item_id) VALUES (?, ?)"));
			deleteQueueStatement = statementContainer.add(
					dbCtx.prepareStatement("DELETE FROM queue WHERE name = ?"));
			listQueuesStatement = statementContainer.add(
					dbCtx.prepareStatementForStreaming("SELECT name FROM queue"));
			getTimestampIdStatement = statementContainer.add(
					dbCtx.prepareStatementForStreaming("SELECT max(id) AS id FROM item WHERE tstamp <= ?"));
			seekQueueStatement = statementContainer.add(
					dbCtx.prepareStatementForStreaming("UPDATE queue SET last_item_id = ? WHERE name = ?"));
			getMaxItemIdStatement = statementContainer.add(
					dbCtx.prepareStatementForStreaming("SELECT max(id) AS id FROM item"));
			getQueueItemIdStatement = statementContainer.add(
					dbCtx.prepareStatementForStreaming("SELECT last_item_id AS id FROM queue WHERE name = ?"));
		}
	}
	
	
	/**
	 * Creates a new queue.
	 * 
	 * @param queueName
	 *            The name of the queue.
	 * 
	 * @return The database assigned identifier of the queue.
	 */
	public long createQueue(String queueName) {
		int prmIndex;
		
		initialize();
	
		try {
			prmIndex = 1;
			createQueueStatement.setString(prmIndex++, queueName);
			createQueueStatement.setInt(prmIndex++, 0);
			createQueueStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create queue (" + queueName + ")", e);
		}
		
		return dbCtx.getLastSequenceId("queue_id_seq");
	}
	
	
	/**
	 * Deletes an existing queue.
	 * 
	 * @param queueName
	 *            The name of the queue to be deleted.
	 */
	public void deleteQueue(String queueName) {
		initialize();
		
		try {
			deleteQueueStatement.setString(1, queueName);
			deleteQueueStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete queue (" + queueName + ")", e);
		}
	}
	
	
	private List<String> listQueues(ResultSet queueResultSet) {
		ResultSet queueResultSetLocal = queueResultSet;
		
		try {
			List<String> queues;
			
			queues = new ArrayList<String>();
			while (queueResultSetLocal.next()) {
				queues.add(queueResultSetLocal.getString("name"));
			}
			
			queueResultSetLocal.close();
			queueResultSetLocal = null;
			
			return queues;
	
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read the queue names from the result set.", e);
		} finally {
			if (queueResultSetLocal != null) {
				try {
					queueResultSetLocal.close();
				} catch (SQLException e) {
					LOG.log(Level.WARNING, "Unable to close the result set.", e);
				}
			}
		}
	}
	
	
	/**
	 * Lists the names of all queues in the database.
	 * 
	 * @return The queue names.
	 */
	public List<String> listQueues() {
		initialize();
		
		try {
			return listQueues(listQueuesStatement.executeQuery());
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to list the queues.", e);
		}
	}
	
	
	private long getItemId(ResultSet itemIdResultSet) {
		ResultSet itemIdResultSetLocal = itemIdResultSet;
		
		try {
			long itemId;
			
			if (itemIdResultSet.next()) {
				itemId = itemIdResultSet.getLong("id");
			} else {
				itemId = 0;
			}
			
			itemIdResultSetLocal.close();
			itemIdResultSetLocal = null;
			
			return itemId;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read the item id from the result set.", e);
		} finally {
			if (itemIdResultSetLocal != null) {
				try {
					itemIdResultSetLocal.close();
				} catch (SQLException e) {
					LOG.log(Level.WARNING, "Unable to close the result set.", e);
				}
			}
		}
	}


	/**
	 * Modifies the queue to seek to the specified item id.
	 * 
	 * @param queueName
	 *            The name of the queue to advance.
	 * @param itemId
	 *            The item id to skip to.  This item will be read by the next read.
	 */
	public void seekQueue(String queueName, long itemId) {
		initialize();
		
		try {
			int prmIndex;
			
			prmIndex = 1;
			seekQueueStatement.setLong(prmIndex++, itemId);
			seekQueueStatement.setString(prmIndex++, queueName);
			seekQueueStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to skip items up to item id " + itemId + ".", e);
		}
	}


	/**
	 * Determines the item id that corresponds to the specified timestamp. If a queue seeks to this
	 * position it will begin reading after this timestamp. If it reads up to this timestamp it will
	 * read all data up to and including this timestamp.
	 * 
	 * @param timestamp
	 *            The timestamp to query.
	 * @return The item id corresponding to the timestamp.
	 */
	public long getPositionForTimestamp(Date timestamp) {
		long timestampItemId;
		
		initialize();
		
		try {
			getTimestampIdStatement.setTimestamp(1, new Timestamp(timestamp.getTime()));
			timestampItemId = getItemId(getTimestampIdStatement.executeQuery());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to seek queue to timestamp " + timestamp + ".", e);
		}
		
		return timestampItemId;
	}


	/**
	 * Modifies the queue to seek to the specified timestamp. The first item past the specified
	 * timestamp will be read next.
	 * 
	 * @param queueName
	 *            The name of the queue to advance.
	 * @param timestamp
	 *            The timestamp to skip to.
	 */
	public void seekQueue(String queueName, Date timestamp) {
		seekQueue(queueName, getPositionForTimestamp(timestamp));
	}


	/**
	 * Gets the last item id read by the queue.
	 * 
	 * @param queueName
	 *            The name of the queue to query.
	 * @return The last read queue item id.
	 */
	public long getQueuePosition(String queueName) {
		long lastItemId;
		
		initialize();
		
		try {
			getQueueItemIdStatement.setString(1, queueName);
			lastItemId = getItemId(getQueueItemIdStatement.executeQuery());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to get current queue position for queue (" + queueName + ").", e);
		}
		
		return lastItemId;
	}


	/**
	 * Gets the last inserted item id.
	 * 
	 * @return The last inserted item id.
	 */
	public long getCurrentItemId() {
		long lastItemId;
		
		initialize();
		
		try {
			lastItemId = getItemId(getMaxItemIdStatement.executeQuery());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to get current item id.", e);
		}
		
		return lastItemId;
	}


	/**
	 * Modifies the queue pointer to the end of available items.  Only newly added data will be returned.
	 * 
	 * @param queueName
	 *            The name of the queue to advance.
	 */
	public void clearQueue(String queueName) {
		seekQueue(queueName, getCurrentItemId());
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
