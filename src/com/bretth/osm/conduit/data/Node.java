package com.bretth.osm.conduit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class Node {
	private long id;
	private Date timestamp;
	private double latitude;
	private double longitude;
	private List<Tag> tagList;
	
	
	public Node(long id, Date timestamp, double latitude, double longitude) {
		this.id = id;
		this.timestamp = timestamp;
		this.latitude = latitude;
		this.longitude = longitude;
		
		tagList = new ArrayList<Tag>();
	}
	
	
	public long getId() {
		return id;
	}
	
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	
	public double getLatitude() {
		return latitude;
	}
	
	
	public double getLongitude() {
		return longitude;
	}
	
	
	public List<Tag> getTagList() {
		return Collections.unmodifiableList(tagList);
	}
	
	
	public void addTag(Tag tag) {
		tagList.add(tag);
	}
	
	
	public void addTags(Collection<Tag> tags) {
		tagList.addAll(tags);
	}
}
