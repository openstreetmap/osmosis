// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.common;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.ReleasableStatementContainer;


/**
 * Postgresql implementation of an identity value loader.
 * 
 * @author Brett Henderson
 */
public class PostgresqlIdentityValueLoader implements IdentityValueLoader {
	private static final Logger LOG = Logger.getLogger(PostgresqlIdentityValueLoader.class.getName());
	private static final String SQL_SELECT_LAST_INSERT_ID =
		"SELECT lastval() AS lastInsertId";
	private static final String SQL_SELECT_LAST_SEQUENCE_ID =
		"SELECT currval(?) AS lastSequenceId";
	
	private DatabaseContext dbCtx;
	private ReleasableStatementContainer statementContainer;
	private PreparedStatement selectInsertIdStatement;
	private PreparedStatement selectSequenceIdStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for all database access.
	 */
	public PostgresqlIdentityValueLoader(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		
		statementContainer = new ReleasableStatementContainer();
	}
	
	
	/**
	 * Returns the id of the most recently inserted row on the current
	 * connection.
	 * 
	 * @return The newly inserted id.
	 */
	public long getLastInsertId() {
		ResultSet lastInsertQuery;
		
		if (selectInsertIdStatement == null) {
			selectInsertIdStatement =
				statementContainer.add(dbCtx.prepareStatementForStreaming(SQL_SELECT_LAST_INSERT_ID));
		}
		
		lastInsertQuery = null;
		try {
			long lastInsertId;
			
			lastInsertQuery = selectInsertIdStatement.executeQuery();
			
			lastInsertQuery.next();
			lastInsertId = lastInsertQuery.getLong("lastInsertId");
			
			lastInsertQuery.close();
			lastInsertQuery = null;
			
			return lastInsertId;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to retrieve the id of the newly inserted record.",
				e
			);
		} finally {
			if (lastInsertQuery != null) {
				try {
					lastInsertQuery.close();
				} catch (SQLException e) {
					// We are already in an error condition so log and continue.
					LOG.log(Level.WARNING, "Unable to close last insert query.", e);
				}
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLastSequenceId(String sequenceName) {
		ResultSet lastSequenceQuery;
		
		if (selectSequenceIdStatement == null) {
			selectSequenceIdStatement =
				statementContainer.add(dbCtx.prepareStatementForStreaming(SQL_SELECT_LAST_SEQUENCE_ID));
		}
		
		lastSequenceQuery = null;
		try {
			long lastSequenceId;
			
			selectSequenceIdStatement.setString(1, sequenceName);
			lastSequenceQuery = selectSequenceIdStatement.executeQuery();
			
			lastSequenceQuery.next();
			lastSequenceId = lastSequenceQuery.getLong("lastSequenceId");
			
			lastSequenceQuery.close();
			lastSequenceQuery = null;
			
			return lastSequenceId;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to retrieve the last sequence id.",
				e
			);
		} finally {
			if (lastSequenceQuery != null) {
				try {
					lastSequenceQuery.close();
				} catch (SQLException e) {
					// We are already in an error condition so log and continue.
					LOG.log(Level.WARNING, "Unable to close last sequence query.", e);
				}
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		statementContainer.release();
	}
}
