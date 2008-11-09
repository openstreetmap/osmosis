// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_5;

import java.util.Date;

import com.bretth.osmosis.core.OsmosisConstants;
import com.bretth.osmosis.core.container.v0_5.BoundContainer;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.domain.v0_5.Bound;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.lifecycle.ReleasableIterator;
import com.bretth.osmosis.core.mysql.v0_5.impl.EntityHistory;
import com.bretth.osmosis.core.mysql.v0_5.impl.EntityHistoryComparator;
import com.bretth.osmosis.core.mysql.v0_5.impl.EntitySnapshotReader;
import com.bretth.osmosis.core.mysql.v0_5.impl.NodeReader;
import com.bretth.osmosis.core.mysql.v0_5.impl.RelationReader;
import com.bretth.osmosis.core.mysql.v0_5.impl.SchemaVersionValidator;
import com.bretth.osmosis.core.mysql.v0_5.impl.WayReader;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.task.v0_5.RunnableSource;
import com.bretth.osmosis.core.task.v0_5.Sink;


/**
 * An OSM data source reading from a database.  The entire contents of the database are read.
 * 
 * @author Brett Henderson
 */
public class MysqlReader implements RunnableSource {
	private Sink sink;
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private Date snapshotInstant;
	private boolean readAllUsers;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param snapshotInstant
	 *            The state of the node table at this point in time will be
	 *            dumped. This ensures a consistent snapshot.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public MysqlReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences, Date snapshotInstant, boolean readAllUsers) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		this.snapshotInstant = snapshotInstant;
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
		
		reader = new EntitySnapshotReader<Node>(
			new PeekableIterator<EntityHistory<Node>>(
				new NodeReader(loginCredentials, readAllUsers)
			),
			snapshotInstant,
			new EntityHistoryComparator<Node>()
		);
		
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
		
		reader = new EntitySnapshotReader<Way>(
			new PeekableIterator<EntityHistory<Way>>(
				new WayReader(loginCredentials, readAllUsers)
			),
			snapshotInstant,
			new EntityHistoryComparator<Way>()
		);
		
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
		
		reader = new EntitySnapshotReader<Relation>(
			new PeekableIterator<EntityHistory<Relation>>(
				new RelationReader(loginCredentials, readAllUsers)
			),
			snapshotInstant,
			new EntityHistoryComparator<Relation>()
		);
		
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
			if (preferences.getValidateSchemaVersion()) {
				new SchemaVersionValidator(loginCredentials).validateVersion(MySqlVersionConstants.SCHEMA_VERSION);
			}
			
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
