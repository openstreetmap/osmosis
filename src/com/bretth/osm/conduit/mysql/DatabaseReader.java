package com.bretth.osm.conduit.mysql;

import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.mysql.impl.NodeReader;
import com.bretth.osm.conduit.mysql.impl.SegmentReader;
import com.bretth.osm.conduit.mysql.impl.WayReader;
import com.bretth.osm.conduit.mysql.impl.WaySegment;
import com.bretth.osm.conduit.mysql.impl.WaySegmentReader;
import com.bretth.osm.conduit.mysql.impl.WayTag;
import com.bretth.osm.conduit.mysql.impl.WayTagReader;
import com.bretth.osm.conduit.task.OsmRunnableSource;
import com.bretth.osm.conduit.task.OsmSink;


public class DatabaseReader implements OsmRunnableSource {
	
	private OsmSink osmSink;
	private String host;
	private String database;
	private String user;
	private String password;
	
	
	public DatabaseReader(String host, String database, String user, String password) {
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
	}
	
	
	public DatabaseReader(OsmSink osmSink) {
		this.osmSink = osmSink;
	}
	
	
	public void setOsmSink(OsmSink osmSink) {
		this.osmSink = osmSink;
	}
	
	
	private void processNodes() {
		NodeReader reader;
		
		reader = new NodeReader(host, database, user, password);
		
		try {
			while (reader.hasNext()) {
				osmSink.addNode(reader.next());
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	private void processSegments() {
		SegmentReader reader;
		
		reader = new SegmentReader(host, database, user, password);
		
		try {
			while (reader.hasNext()) {
				osmSink.addSegment(reader.next());
			}
			
		} finally {
			reader.release();
		}
	}
	
	
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
				
				osmSink.addWay(way);
			}
			
		} finally {
			wayReader.release();
			waySegmentReader.release();
			wayTagReader.release();
		}
	}
	
	
	public void run() {
		try {
			processNodes();
			processSegments();
			processWays();
			osmSink.complete();
			
		} finally {
			osmSink.release();
		}
	}
}
