package com.bretth.osm.transformer.mysql;

import com.bretth.osm.transformer.data.Way;
import com.bretth.osm.transformer.mysql.impl.NodeReader;
import com.bretth.osm.transformer.mysql.impl.SegmentReader;
import com.bretth.osm.transformer.mysql.impl.WayReader;
import com.bretth.osm.transformer.mysql.impl.WaySegment;
import com.bretth.osm.transformer.mysql.impl.WaySegmentReader;
import com.bretth.osm.transformer.mysql.impl.WayTag;
import com.bretth.osm.transformer.mysql.impl.WayTagReader;
import com.bretth.osm.transformer.pipeline.OsmSink;
import com.bretth.osm.transformer.pipeline.OsmSource;


public class DatabaseReader implements OsmSource {
	
	private OsmSink osmSink;
	
	
	public DatabaseReader() {
	}
	
	
	public DatabaseReader(OsmSink osmSink) {
		this.osmSink = osmSink;
	}
	
	
	public void setOsmSink(OsmSink osmSink) {
		this.osmSink = osmSink;
	}
	
	
	private void processNodes() {
		NodeReader reader;
		
		reader = new NodeReader();
		
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
		
		reader = new SegmentReader();
		
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
		
		wayReader = new WayReader();
		waySegmentReader = new WaySegmentReader();
		wayTagReader = new WayTagReader();
		
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
	
	
	public void process() {
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
