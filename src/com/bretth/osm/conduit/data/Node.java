package com.bretth.osm.conduit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * A data class representing a single OSM node.
 * 
 * @author Brett Henderson
 */
public class Node {
	private long id;
	private Date timestamp;
	private double latitude;
	private double longitude;
	private List<Tag> tagList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param latitude
	 *            The geographic latitude.
	 * @param longitude
	 *            The geographic longitude.
	 */
	public Node(long id, Date timestamp, double latitude, double longitude) {
		this.id = id;
		this.timestamp = timestamp;
		this.latitude = latitude;
		this.longitude = longitude;
		
		tagList = new ArrayList<Tag>();
	}
	
	
	/**
	 * @return The id. 
	 */
	public long getId() {
		return id;
	}
	
	
	/**
	 * @return The timestamp. 
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	
	/**
	 * @return The latitude. 
	 */
	public double getLatitude() {
		return latitude;
	}
	
	
	/**
	 * @return The longitude. 
	 */
	public double getLongitude() {
		return longitude;
	}
	
	
	/**
	 * Returns the attached list of tags. The returned list is read-only.
	 * 
	 * @return The tagList.
	 */
	public List<Tag> getTagList() {
		return Collections.unmodifiableList(tagList);
	}
	
	
	/**
	 * Adds a new tag.
	 * 
	 * @param tag
	 *            The tag to add.
	 */
	public void addTag(Tag tag) {
		tagList.add(tag);
	}
	
	
	/**
	 * Adds all tags in the collection to the node.
	 * 
	 * @param tags
	 *            The collection of tags to be added.
	 */
	public void addTags(Collection<Tag> tags) {
		tagList.addAll(tags);
	}
}
