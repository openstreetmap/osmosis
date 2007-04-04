package com.bretth.osm.conduit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class Segment {
	private long id;
	private long from;
	private long to;
	private List<Tag> tagList;
	
	
	public Segment(long id, long from, long to) {
		this.id = id;
		this.from = from;
		this.to = to;
		
		tagList = new ArrayList<Tag>();
	}
	
	
	public long getId() {
		return id;
	}
	
	
	public long getFrom() {
		return from;
	}
	
	
	public long getTo() {
		return to;
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
