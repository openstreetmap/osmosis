package com.bretth.osmosis.core.mysql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.bretth.osmosis.core.container.NodeContainer;
import com.bretth.osmosis.core.container.SegmentContainer;
import com.bretth.osmosis.core.container.WayContainer;
import com.bretth.osmosis.core.data.Way;
import com.bretth.osmosis.core.mysql.impl.NodeReader;
import com.bretth.osmosis.core.mysql.impl.SegmentReader;
import com.bretth.osmosis.core.mysql.impl.WayReader;
import com.bretth.osmosis.core.mysql.impl.WaySegment;
import com.bretth.osmosis.core.mysql.impl.WaySegmentReader;
import com.bretth.osmosis.core.mysql.impl.WayTag;
import com.bretth.osmosis.core.mysql.impl.WayTagReader;
import com.bretth.osmosis.core.sort.impl.PeekableIterator;
import com.bretth.osmosis.core.sort.impl.PersistentIterator;
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
		NodeReader reader;
		
		reader = new NodeReader(host, database, user, password, snapshotInstant);
		
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
		SegmentReader reader;
		
		reader = new SegmentReader(host, database, user, password, snapshotInstant);
		
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
		PeekableIterator<Way> wayReader;
		PeekableIterator<WaySegment> waySegmentReader;
		PeekableIterator<WayTag> wayTagReader;
		
		wayReader =
			new PeekableIterator<Way>(
				new PersistentIterator<Way>(
					new WayReader(host, database, user, password, snapshotInstant),
					"way",
					true
				)
			);
		waySegmentReader =
			new PeekableIterator<WaySegment>(
				new PersistentIterator<WaySegment>(
					new WaySegmentReader(host, database, user, password, snapshotInstant),
					"wayseg",
					true
				)
			);
		wayTagReader =
			new PeekableIterator<WayTag>(
				new PersistentIterator<WayTag>(
					new WayTagReader(host, database, user, password, snapshotInstant),
					"waytag",
					true
				)
			);
		
		// Calling hasNext will cause the readers to execute their queries and
		// initialise their internal state.
		wayReader.hasNext();
		waySegmentReader.hasNext();
		wayTagReader.hasNext();
		
		try {
			while (wayReader.hasNext()) {
				Way way;
				List<WaySegment> waySegments;
				
				// Read the next way.
				way = wayReader.next();
				waySegments = new ArrayList<WaySegment>();
				
				// Read all associated way segments.
				while (
						waySegmentReader.hasNext() &&
						(waySegmentReader.peekNext().getWayId() <= way.getId())) {
					
					WaySegment waySegment;
					
					waySegment = waySegmentReader.next();
					
					if (waySegment.getWayId() == way.getId()) {
						waySegments.add(waySegment);
					}
				}
				
				// Sort the way segments by the sequence id.
				Collections.sort(
					waySegments,
					new Comparator<WaySegment>() {
						public int compare(WaySegment o1, WaySegment o2) {
							return o1.getSequenceId() - o2.getSequenceId();
						}
					}
				);
				
				// Add the way segments to the way.
				for (WaySegment waySegment : waySegments) {
					way.addSegmentReference(waySegment);
				}
				
				// Read all associated way tags.
				while (
						wayTagReader.hasNext() &&
						(wayTagReader.peekNext().getWayId() <= way.getId())) {
					
					WayTag wayTag;
					
					wayTag = wayTagReader.next();
					
					if (wayTag.getWayId() == way.getId()) {
						way.addTag(wayTag);
					}
				}
				
				// Send the complete way to the sink.
				sink.process(new WayContainer(way));
			}
			
		} finally {
			wayReader.release();
			waySegmentReader.release();
			wayTagReader.release();
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
