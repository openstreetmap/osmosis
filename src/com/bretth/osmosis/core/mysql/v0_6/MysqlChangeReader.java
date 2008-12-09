// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6;

import java.util.Date;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.mysql.v0_6.impl.SchemaVersionValidator;
import com.bretth.osmosis.core.mysql.v0_6.impl.NodeChangeReader;
import com.bretth.osmosis.core.mysql.v0_6.impl.RelationChangeReader;
import com.bretth.osmosis.core.mysql.v0_6.impl.WayChangeReader;
import com.bretth.osmosis.core.task.v0_6.ChangeSink;
import com.bretth.osmosis.core.task.v0_6.RunnableChangeSource;


/**
 * A change source reading from database history tables. This aims to be
 * suitable for running at regular intervals with database overhead proportional
 * to changeset size.
 * 
 * @author Brett Henderson
 */
public class MysqlChangeReader implements RunnableChangeSource {
	private ChangeSink changeSink;
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private boolean readAllUsers;
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
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 * @param fullHistory
	 *            Specifies if full version history should be returned, or just
	 *            a single change per entity for the interval.
	 */
	public MysqlChangeReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences, boolean readAllUsers, Date intervalBegin, Date intervalEnd, boolean fullHistory) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		this.readAllUsers = readAllUsers;
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
	 * Reads all node changes and sends them to the change sink.
	 */
	private void processNodes() {
		NodeChangeReader reader = new NodeChangeReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd, fullHistory);
		
		try {
			while (reader.hasNext()) {
				changeSink.process(reader.next());
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all ways from the database and sends to the sink.
	 */
	private void processWays() {
		WayChangeReader reader = new WayChangeReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd, fullHistory);
		
		try {
			while (reader.hasNext()) {
				changeSink.process(reader.next());
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all relations from the database and sends to the sink.
	 */
	private void processRelations() {
		RelationChangeReader reader = new RelationChangeReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd, fullHistory);
		
		try {
			while (reader.hasNext()) {
				changeSink.process(reader.next());
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all data from the file and send it to the sink.
	 */
	public void run() {
		try {
			if (preferences.getValidateSchemaVersion()) {
				new SchemaVersionValidator(loginCredentials).validateVersion(MySqlVersionConstants.SCHEMA_MIGRATIONS);
			}
			processNodes();
			processWays();
			processRelations();
			
			changeSink.complete();
			
		} finally {
			changeSink.release();
		}
	}
}
