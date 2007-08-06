package com.bretth.osmosis.mysql;

import java.util.Date;

import com.bretth.osmosis.container.NodeContainer;
import com.bretth.osmosis.container.SegmentContainer;
import com.bretth.osmosis.container.WayContainer;
import com.bretth.osmosis.data.Way;
import com.bretth.osmosis.mysql.impl.NodeReader;
import com.bretth.osmosis.mysql.impl.SegmentReader;
import com.bretth.osmosis.mysql.impl.WayReader;
import com.bretth.osmosis.mysql.impl.WaySegment;
import com.bretth.osmosis.mysql.impl.WaySegmentReader;
import com.bretth.osmosis.mysql.impl.WayTag;
import com.bretth.osmosis.mysql.impl.WayTagReader;
import com.bretth.osmosis.task.RunnableSource;
import com.bretth.osmosis.task.Sink;


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
		WayReader wayReader;
		WaySegmentReader waySegmentReader;
		WayTagReader wayTagReader;
		
		wayReader = new WayReader(host, database, user, password, snapshotInstant);
		waySegmentReader = new WaySegmentReader(host, database, user, password, snapshotInstant);
		wayTagReader = new WayTagReader(host, database, user, password, snapshotInstant);
		
		// Calling hasNext will cause the readers to execute their queries and
		// initialise their internal state.
		wayReader.hasNext();
		waySegmentReader.hasNext();
		wayTagReader.hasNext();
		
		try {
			while (wayReader.hasNext()) {
				Way way;
				
				way = wayReader.next();
				
				while (
						waySegmentReader.hasNext() &&
						(waySegmentReader.peekNext().getWayId() <= way.getId())) {
					
					WaySegment waySegment;
					
					waySegment = waySegmentReader.next();
					
					if (waySegment.getWayId() == way.getId()) {
						way.addSegmentReference(waySegment);
					}
				}
				
				
				while (
						wayTagReader.hasNext() &&
						(wayTagReader.peekNext().getWayId() <= way.getId())) {
					
					WayTag wayTag;
					
					wayTag = wayTagReader.next();
					
					if (wayTag.getWayId() == way.getId()) {
						way.addTag(wayTag);
					}
				}
				
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
