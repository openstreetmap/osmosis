// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.sort.v0_6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.task.v0_6.SinkSource;


/**
 * A data stream filter that sorts tags on entities. This is useful for testing
 * to allow two sets of data to be compared for equality.
 * 
 * @author Brett Henderson
 */
public class TagSorter implements SinkSource, EntityProcessor {
	private Sink sink;
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		entityContainer.process(this);
	}


	/**
	 * Sorts the specified tag list.
	 * 
	 * @param tagList
	 *            The tag list to be sorted.
	 * @return A new list containing the sorted tags.
	 */
	private List<Tag> sortTagList(List<Tag> tagList) {
		List<Tag> sortedTagList;
		
		sortedTagList = new ArrayList<Tag>(tagList);
		Collections.sort(sortedTagList);
		
		return sortedTagList;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(BoundContainer boundContainer) {
		Bound oldBound;
		Bound newBound;
		
		oldBound = boundContainer.getEntity();
		
		newBound = new Bound(oldBound.getRight(), oldBound.getLeft(), oldBound.getTop(), oldBound.getBottom(), oldBound.getOrigin());
		newBound.addTags(sortTagList(oldBound.getTagList()));
		
		sink.process(new BoundContainer(newBound));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(NodeContainer nodeContainer) {
		Node oldNode;
		Node newNode;
		
		oldNode = nodeContainer.getEntity();
		
		newNode = new Node(oldNode.getId(), oldNode.getVersion(), oldNode.getTimestamp(), oldNode.getUser(), oldNode.getLatitude(), oldNode.getLongitude());
		newNode.addTags(sortTagList(oldNode.getTagList()));
		
		sink.process(new NodeContainer(newNode));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(WayContainer wayContainer) {
		Way oldWay;
		Way newWay;
		
		oldWay = wayContainer.getEntity();
		
		newWay = new Way(oldWay.getId(), oldWay.getVersion(), oldWay.getTimestamp(), oldWay.getUser());
		newWay.addTags(sortTagList(oldWay.getTagList()));
		newWay.addWayNodes(oldWay.getWayNodeList());
		
		sink.process(new WayContainer(newWay));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(RelationContainer relationContainer) {
		Relation oldRelation;
		Relation newRelation;
		
		oldRelation = relationContainer.getEntity();
		
		newRelation = new Relation(oldRelation.getId(), oldRelation.getVersion(), oldRelation.getTimestamp(), oldRelation.getUser());
		newRelation.addTags(sortTagList(oldRelation.getTagList()));
		newRelation.addMembers(oldRelation.getMemberList());
		
		sink.process(new RelationContainer(newRelation));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sink.release();
	}
}
