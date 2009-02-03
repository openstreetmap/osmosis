// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_6;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.mysql.v0_6.impl.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.mysql.v0_6.impl.CurrentNodeReader;
import org.openstreetmap.osmosis.core.mysql.v0_6.impl.CurrentRelationReader;
import org.openstreetmap.osmosis.core.mysql.v0_6.impl.CurrentWayReader;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


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
	public MySqlCurrentReader(
			DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences, boolean readAllUsers) {
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
	 * Reads all relations from the database and sends to the sink.
	 */
	private void processRelations() {
		ReleasableIterator<Relation> reader;
		
		reader = new CurrentRelationReader(loginCredentials, readAllUsers);
		
		try {
			while (reader.hasNext()) {
				sink.process(new RelationContainer(reader.next()));
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
			new SchemaVersionValidator(loginCredentials, preferences)
					.validateVersion(MySqlVersionConstants.SCHEMA_MIGRATIONS);
			
			sink.process(new BoundContainer(new Bound("Osmosis " + OsmosisConstants.VERSION)));
			processNodes();
			processWays();
			processRelations();
			
			sink.complete();
			
		} finally {
			sink.release();
		}
	}
}
