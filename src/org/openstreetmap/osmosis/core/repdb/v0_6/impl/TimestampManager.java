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
import org.openstreetmap.osmosis.core.database.ReleasableStatementContainer;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;


/**
 * Allows the timestamp column for system and queue tables to be queried and manipulated.
 */
public class TimestampManager implements Releasable {
	
	private static final Logger LOG = Logger.getLogger(TimestampManager.class.getName());
	
	
	private DatabaseContext dbCtx;
	private String tableName;
	private int queueId;
	private ReleasableStatementContainer statementContainer;
	private boolean initialized;
	private PreparedStatement selectStatement;
	private PreparedStatement updateStatement;
	
	
	/**
	 * Creates a new instance for manipulating the system time.
	 * 
	 * @param dbCtx
	 *            Used to access the database.
	 */
	public TimestampManager(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		
		tableName = "system";
		queueId = 1;
	}
	
	
	/**
	 * Creates a new instance for manipulating the current time of a queue.
	 * 
	 * @param dbCtx
	 *            Used to access the database.
	 * @param queueId
	 *            The queue to modify.
	 */
	public TimestampManager(DatabaseContext dbCtx, int queueId) {
		this.dbCtx = dbCtx;
		
		tableName = "queue";
		this.queueId = queueId;
	}
	
	
	private void initialize() {
		if (!initialized) {
			selectStatement = statementContainer.add(
					dbCtx.prepareStatementForStreaming("SELECT tstamp FROM " + tableName + " WHERE id = " + queueId));
			updateStatement = statementContainer.add(
					dbCtx.prepareStatement("UPDATE " + tableName + " (tstamp) VALUES (?)"));
			
			initialized = true;
		}
	}
	
	
	/**
	 * Reads the current timestamp value.
	 * 
	 * @return The current timestamp.
	 */
	public Date getTimestamp() {
		ResultSet rs;
		Date result;
		
		initialize();
		
		rs = null;
		try {
			rs = selectStatement.executeQuery();
			rs.next();
			result = new Date(rs.getTimestamp("tstamp").getTime());
			
			rs.close();
			rs = null;
			
			return result;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to retrieve the current timestamp.", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOG.log(Level.WARNING, "Unable to close result set.", e);
				}
			}
		}
	}
	
	
	/**
	 * Updates the timestamp value.
	 * 
	 * @param timestamp The new timestamp.
	 */
	public void setTimestamp(Date timestamp) {
		initialize();
		
		try {
			updateStatement.setTimestamp(1, new Timestamp(timestamp.getTime()));
			updateStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to update the current timestamp.", e);
		}
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
