package com.bretth.osmosis.core.mysql.v0_4;

import com.bretth.osmosis.core.container.v0_4.NodeContainer;
import com.bretth.osmosis.core.container.v0_4.SegmentContainer;
import com.bretth.osmosis.core.container.v0_4.WayContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.domain.v0_4.Node;
import com.bretth.osmosis.core.domain.v0_4.Segment;
import com.bretth.osmosis.core.domain.v0_4.Way;
import com.bretth.osmosis.core.mysql.v0_4.impl.CurrentNodeReader;
import com.bretth.osmosis.core.mysql.v0_4.impl.CurrentSegmentReader;
import com.bretth.osmosis.core.mysql.v0_4.impl.CurrentWayReader;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.task.v0_4.RunnableSource;
import com.bretth.osmosis.core.task.v0_4.Sink;


/**
 * An OSM data source reading from a databases current tables. The entire
 * contents of the database are read.
 * 
 * @author Brett Henderson
 */
public class MySqlCurrentReader implements RunnableSource {
	private Sink sink;
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private boolean readAllUsers;
	
	
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
	 */
	public MySqlCurrentReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences, boolean readAllUsers) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		this.readAllUsers = readAllUsers;
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
	private void processNodes() {
		ReleasableIterator<Node> reader;
		
		reader = new CurrentNodeReader(loginCredentials, readAllUsers);
		
		try {
			while (reader.hasNext()) {
				sink.process(new NodeContainer(reader.next()));
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all segments from the database and sends to the sink.
	 */
	private void processSegments() {
		ReleasableIterator<Segment> reader;
		
		reader = new CurrentSegmentReader(loginCredentials, readAllUsers);
		
		try {
			while (reader.hasNext()) {
				sink.process(new SegmentContainer(reader.next()));
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all ways from the database and sends to the sink.
	 */
	private void processWays() {
		ReleasableIterator<Way> reader;
		
		reader = new CurrentWayReader(loginCredentials, readAllUsers);
		
		try {
			while (reader.hasNext()) {
				sink.process(new WayContainer(reader.next()));
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all data from the database and send it to the sink.
	 */
	public void run() {
		try {
			if (preferences.getValidateSchemaVersion()) {
				new SchemaVersionValidator(loginCredentials).validateVersion(MySqlVersionConstants.SCHEMA_VERSION);
			}
			processNodes();
			processSegments();
			processWays();
			
			sink.complete();
			
		} finally {
			sink.release();
		}
	}
}
