package com.bretth.osm.conduit.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class Way {
	private long id;
	private Date timestamp;
	private List<Tag> tagList;
	private List<SegmentReference> segmentReferenceList;
	
	
	public Way(long id, Date timestamp) {
		this.id = id;
		this.timestamp = timestamp;
		
		tagList = new ArrayList<Tag>();
		segmentReferenceList = new ArrayList<SegmentReference>();
	}
	
	
	public long getId() {
		return id;
	}
	
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	
	public List<Tag> getTagList() {
		return Collections.unmodifiableList(tagList);
	}
	
	
	public List<SegmentReference> getSegmentReferenceList() {
		return Collections.unmodifiableList(segmentReferenceList);
	}
	
	
	public void addTag(Tag tag) {
		tagList.add(tag);
	}


	public void addSegmentReference(SegmentReference segmentReference) {
		segmentReferenceList.add(segmentReference);
	}
}
