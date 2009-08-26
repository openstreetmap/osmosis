// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext2;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;


/**
 * A change source reading from database history tables. This aims to be suitable for running at
 * regular intervals with database overhead proportional to changeset size.
 * 
 * @author Brett Henderson
 */
public class ApidbTestReader implements RunnableTask {

	private static final Logger LOG = Logger.getLogger(ApidbTestReader.class.getName());
	
    private DatabaseLoginCredentials loginCredentials;
    private Date intervalBegin;
    private Date intervalEnd;
    private boolean enableSpring;
    private boolean enableNewQuery;


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
	 * @param enableSpring
	 *            If true use Spring, otherwise use raw JDBC.
	 * @param enableNewQuery
	 *            If true use the new query style, otherwise use the old style.
	 */
    public ApidbTestReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
            Date intervalBegin, Date intervalEnd, boolean enableSpring, boolean enableNewQuery) {
        this.loginCredentials = loginCredentials;
        this.intervalBegin = intervalBegin;
        this.intervalEnd = intervalEnd;
        this.enableSpring = enableSpring;
        this.enableNewQuery = enableNewQuery;
    }
    
    
    private String buildEntityHistoryQuery() {
    	String sql;
    	
    	if (enableNewQuery) {
			StringBuilder sqlBuilder;
			String selectedEntityStatement;
			
			selectedEntityStatement =
				"(SELECT id, version FROM "
				+ "node"
				+ "s WHERE timestamp > ? AND timestamp <= ?)";
	
			sqlBuilder = new StringBuilder();
			sqlBuilder.append("SELECT e.id, e.version, e.timestamp, e.visible, u.data_public,");
			sqlBuilder.append(" u.id AS user_id, u.display_name, e.changeset_id, e.latitude, e.longitude");
	
			sqlBuilder.append(" FROM ");
			sqlBuilder.append("node");
			sqlBuilder.append("s e");
			sqlBuilder.append(" INNER JOIN ");
			sqlBuilder.append(selectedEntityStatement);
			sqlBuilder.append(" t ON e.id = t.id AND e.version = t.version");
			sqlBuilder.append(" INNER JOIN changesets c ON e.changeset_id = c.id");
			sqlBuilder.append(" INNER JOIN users u ON c.user_id = u.id");
			
			sql = sqlBuilder.toString();
			
    	} else {
    		sql = "SELECT e.id, e.version, e.timestamp, e.visible, u.data_public,"
					+ " u.id AS user_id, u.display_name, e.changeset_id, e.latitude, e.longitude" + " FROM nodes e"
					+ " LEFT OUTER JOIN changesets c ON e.changeset_id = c.id"
					+ " LEFT OUTER JOIN users u ON c.user_id = u.id" + " WHERE e.timestamp > ? AND e.timestamp <= ?"
					+ " ORDER BY e.id, e.version";
    	}
    	
    	LOG.log(Level.FINER, "Entity history query: " + sql);
    	
    	return sql;
	}
    
    
    /**
	 * Runs the task implementation. This is called by the run method within a transaction.
	 * 
	 * @param dbCtx
	 *            Used to access the database.
	 */
    protected void runImpl(DatabaseContext2 dbCtx) {
		RowListener rowListener;
		
		rowListener = new RowListener();
		dbCtx.getJdbcTemplate().query(buildEntityHistoryQuery(), new Object[] {intervalBegin, intervalEnd },
				rowListener);
		
		if (LOG.isLoggable(Level.INFO)) {
			LOG.info("Received " + rowListener.getRowCount() + " rows.");
		}
    }
    
    
    private void iterateResultSet(ResultSet rs) {
    	try {
    		int rowCount;
    		
    		rowCount = 0;
    		while (rs.next()) {
    			rowCount++;
    		}
    		LOG.info("Received " + rowCount + " rows.");
    		
    	} catch (SQLException e) {
    		throw new OsmosisRuntimeException("Unable to read the node history.", e);
    	} finally {
    		if (rs != null) {
    			try {
					rs.close();
				} catch (SQLException e) {
					LOG.log(Level.WARNING, "Unable to close the result set.", e);
				}
    		}
    	}
    }
    
    
    private void runImpl2(DatabaseContext dbCtx) {
    	PreparedStatement statement = null;
    	
    	try {
    		int prmIndex;
    		
    		statement = dbCtx.prepareStatementForStreaming(buildEntityHistoryQuery());
    		
    		prmIndex = 1;
    		statement.setTimestamp(prmIndex++, new Timestamp(intervalBegin.getTime()));
    		statement.setTimestamp(prmIndex++, new Timestamp(intervalEnd.getTime()));
    		iterateResultSet(statement.executeQuery());
    		
    		statement.close();
    		statement = null;
    		
    	} catch (SQLException e) {
    		throw new OsmosisRuntimeException("Unable to read the node history.", e);
    	} finally {
    		if (statement != null) {
    			try {
					statement.close();
				} catch (SQLException e) {
					LOG.log(Level.WARNING, "Unable to close the prepared statement.", e);
				}
    		}
    	}
    }
    

    /**
     * Reads all data from the database and send it to the sink.
     */
    public void run() {
    	if (enableSpring) {
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
    	} else {
    		final DatabaseContext dbCtx = new DatabaseContext(loginCredentials);
    		
    		try {
    			runImpl2(dbCtx);
    			
    			dbCtx.commit();
    		} finally {
    			dbCtx.release();
    		}
    	}
    }
    
    
    private static class RowListener implements RowCallbackHandler {
    	private int rowCount;
    	
    	
    	/**
    	 * Creates a new instance.
    	 */
    	public RowListener() {
    		rowCount = 0;
    	}
    	
    	
		@Override
		public void processRow(ResultSet arg0) throws SQLException {
			rowCount++;
		}
    	
		
		/**
		 * Gets the number of processed rows.
		 * 
		 * @return The row count.
		 */
		public int getRowCount() {
			return rowCount;
		}
    }
}
