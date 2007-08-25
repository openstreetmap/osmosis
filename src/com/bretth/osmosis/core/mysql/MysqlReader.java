package com.bretth.osmosis.core.mysql;

import java.util.Date;

import com.bretth.osmosis.core.container.NodeContainer;
import com.bretth.osmosis.core.container.SegmentContainer;
import com.bretth.osmosis.core.container.WayContainer;
import com.bretth.osmosis.core.domain.Node;
import com.bretth.osmosis.core.domain.Segment;
import com.bretth.osmosis.core.domain.Way;
import com.bretth.osmosis.core.mysql.impl.EntityHistory;
import com.bretth.osmosis.core.mysql.impl.EntityHistoryComparator;
import com.bretth.osmosis.core.mysql.impl.EntitySnapshotReader;
import com.bretth.osmosis.core.mysql.impl.NodeReader;
import com.bretth.osmosis.core.mysql.impl.SegmentReader;
import com.bretth.osmosis.core.mysql.impl.WayReader;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.task.RunnableSource;
import com.bretth.osmosis.core.task.Sink;


/**
 * An OSM data source reading from a database.  The entire contents of the database are read.
 * 
 * @author Brett Henderson
 */
public class MysqlReader implements RunnableSource {
	private Sink sink;
	private String host;
	private String database;
	private String user;
	private String password;
	private Date snapshotInstant;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param host
	 *            The server hosting the database.
	 * @param database
	 *            The database instance.
	 * @param user
	 *            The user name for authentication.
	 * @param password
	 *            The password for authentication.
	 * @param snapshotInstant
	 *            The state of the node table at this point in time will be
	 *            dumped.  This ensures a consistent snapshot.
	 */
	public MysqlReader(String host, String database, String user, String password, Date snapshotInstant) {
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
		this.snapshotInstant = snapshotInstant;
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
				new NodeReader(host, database, user, password)
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
				new SegmentReader(host, database, user, password)
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
				new WayReader(host, database, user, password)
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
			processNodes();
			processSegments();
			processWays();
			
			sink.complete();
			
		} finally {
			sink.release();
		}
	}
}
