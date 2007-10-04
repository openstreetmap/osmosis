package com.bretth.osmosis.core.mysql.v0_4;

import java.util.Date;

import com.bretth.osmosis.core.container.v0_4.NodeContainer;
import com.bretth.osmosis.core.container.v0_4.SegmentContainer;
import com.bretth.osmosis.core.container.v0_4.WayContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.domain.v0_4.Node;
import com.bretth.osmosis.core.domain.v0_4.Segment;
import com.bretth.osmosis.core.domain.v0_4.Way;
import com.bretth.osmosis.core.mysql.common.EntityHistory;
import com.bretth.osmosis.core.mysql.v0_4.impl.EntityHistoryComparator;
import com.bretth.osmosis.core.mysql.v0_4.impl.EntitySnapshotReader;
import com.bretth.osmosis.core.mysql.v0_4.impl.NodeReader;
import com.bretth.osmosis.core.mysql.v0_4.impl.SegmentReader;
import com.bretth.osmosis.core.mysql.v0_4.impl.WayReader;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.task.v0_4.RunnableSource;
import com.bretth.osmosis.core.task.v0_4.Sink;


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
	 * Reads all segments from the database and sends to the sink.
	 */
	private void processSegments() {
		ReleasableIterator<Segment> reader;
		
		reader = new EntitySnapshotReader<Segment>(
			new PeekableIterator<EntityHistory<Segment>>(
				new SegmentReader(loginCredentials, readAllUsers)
			),
			snapshotInstant,
			new EntityHistoryComparator<Segment>()
		);
		
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
