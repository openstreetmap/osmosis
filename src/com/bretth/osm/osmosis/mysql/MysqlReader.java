package com.bretth.osm.osmosis.mysql;

import com.bretth.osm.osmosis.data.Way;
import com.bretth.osm.osmosis.mysql.impl.NodeReader;
import com.bretth.osm.osmosis.mysql.impl.SegmentReader;
import com.bretth.osm.osmosis.mysql.impl.WayReader;
import com.bretth.osm.osmosis.mysql.impl.WaySegment;
import com.bretth.osm.osmosis.mysql.impl.WaySegmentReader;
import com.bretth.osm.osmosis.mysql.impl.WayTag;
import com.bretth.osm.osmosis.mysql.impl.WayTagReader;
import com.bretth.osm.osmosis.task.RunnableSource;
import com.bretth.osm.osmosis.task.Sink;


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
	 */
	public MysqlReader(String host, String database, String user, String password) {
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
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
		
		reader = new NodeReader(host, database, user, password);
		
		try {
			while (reader.hasNext()) {
				sink.processNode(reader.next());
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
		
		reader = new SegmentReader(host, database, user, password);
		
		try {
			while (reader.hasNext()) {
				sink.processSegment(reader.next());
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
		
		wayReader = new WayReader(host, database, user, password);
		waySegmentReader = new WaySegmentReader(host, database, user, password);
		wayTagReader = new WayTagReader(host, database, user, password);
		
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
				
				sink.processWay(way);
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
