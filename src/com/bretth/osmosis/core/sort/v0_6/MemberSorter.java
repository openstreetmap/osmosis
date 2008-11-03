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
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.task.v0_6.SinkSource;


/**
 * A data stream filter that sorts members on relations. This is useful for testing
 * to allow two sets of data to be compared for equality.
 * 
 * @author Brett Henderson
 */
public class MemberSorter implements SinkSource, EntityProcessor {
	private Sink sink;
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		entityContainer.process(this);
	}


	/**
	 * Sorts the specified member list.
	 * 
	 * @param memberList
	 *            The member list to be sorted.
	 * @return A new list containing the sorted members.
	 */
	private List<RelationMember> sortMemberList(List<RelationMember> tagList) {
		List<RelationMember> sortedMemberList;
		
		sortedMemberList = new ArrayList<RelationMember>(tagList);
		Collections.sort(sortedMemberList);
		
		return sortedMemberList;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(BoundContainer boundContainer) {
		sink.process(boundContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(NodeContainer nodeContainer) {
		sink.process(nodeContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(WayContainer wayContainer) {
		sink.process(wayContainer);
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
		newRelation.addTags(oldRelation.getTagList());
		newRelation.addMembers(sortMemberList(oldRelation.getMemberList()));
		
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
