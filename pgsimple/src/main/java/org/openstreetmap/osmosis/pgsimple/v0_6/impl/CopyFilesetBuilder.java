// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.CompletableContainer;
import org.openstreetmap.osmosis.pgsimple.common.CopyFileWriter;
import org.openstreetmap.osmosis.pgsimple.common.NodeLocationStoreType;
import org.openstreetmap.osmosis.pgsimple.common.PointBuilder;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink for storing all data to a set of database dump files. These
 * files can be used for populating an empty database.
 * 
 * @author Brett Henderson
 */
public class CopyFilesetBuilder implements Sink, EntityProcessor {
	
	private boolean enableBboxBuilder;
	private boolean enableLinestringBuilder;
	private WayGeometryBuilder wayGeometryBuilder;
	private CompletableContainer writerContainer;
	private MemberTypeValueMapper memberTypeValueMapper;
	private CopyFileWriter userWriter;
	private CopyFileWriter nodeWriter;
	private CopyFileWriter nodeTagWriter;
	private CopyFileWriter wayWriter;
	private CopyFileWriter wayTagWriter;
	private CopyFileWriter wayNodeWriter;
	private CopyFileWriter relationWriter;
	private CopyFileWriter relationTagWriter;
	private CopyFileWriter relationMemberWriter;
	private PointBuilder pointBuilder;
	private Set<Integer> userSet;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param copyFileset
	 *            The set of COPY files to be populated.
	 * @param enableBboxBuilder
	 *            If true, the way bbox geometry is built during processing
	 *            instead of relying on the database to build them after import.
	 *            This increases processing but is faster than relying on the
	 *            database.
	 * @param enableLinestringBuilder
	 *            If true, the way linestring geometry is built during
	 *            processing instead of relying on the database to build them
	 *            after import. This increases processing but is faster than
	 *            relying on the database.
	 * @param storeType
	 *            The node location storage type used by the geometry builders.
	 */
	public CopyFilesetBuilder(
			CopyFileset copyFileset, boolean enableBboxBuilder,
			boolean enableLinestringBuilder, NodeLocationStoreType storeType) {
		this.enableBboxBuilder = enableBboxBuilder;
		this.enableLinestringBuilder = enableLinestringBuilder;
		
		writerContainer = new CompletableContainer();
		
		userWriter = writerContainer.add(new CopyFileWriter(copyFileset.getUserFile()));
		nodeWriter = writerContainer.add(new CopyFileWriter(copyFileset.getNodeFile()));
		nodeTagWriter = writerContainer.add(new CopyFileWriter(copyFileset.getNodeTagFile()));
		wayWriter = writerContainer.add(new CopyFileWriter(copyFileset.getWayFile()));
		wayTagWriter = writerContainer.add(new CopyFileWriter(copyFileset.getWayTagFile()));
		wayNodeWriter = writerContainer.add(new CopyFileWriter(copyFileset.getWayNodeFile()));
		relationWriter = writerContainer.add(new CopyFileWriter(copyFileset.getRelationFile()));
		relationTagWriter = writerContainer.add(new CopyFileWriter(copyFileset.getRelationTagFile()));
		relationMemberWriter = writerContainer.add(new CopyFileWriter(copyFileset.getRelationMemberFile()));
		
		pointBuilder = new PointBuilder();
		wayGeometryBuilder = new WayGeometryBuilder(storeType);
		memberTypeValueMapper = new MemberTypeValueMapper();
		memberTypeValueMapper = new MemberTypeValueMapper();
		
		userSet = new HashSet<Integer>();
	}
    
    
    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		OsmUser user;
		
		// Write a user entry if the user doesn't already exist.
		user = entityContainer.getEntity().getUser();
		if (!user.equals(OsmUser.NONE)) {
			if (!userSet.contains(user.getId())) {
				userWriter.writeField(user.getId());
				userWriter.writeField(user.getName());
				userWriter.endRecord();
				
				userSet.add(user.getId());
			}
		}
		
		// Process the entity itself.
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer nodeContainer) {
		Node node;
		
		node = nodeContainer.getEntity();
		
		nodeWriter.writeField(node.getId());
		nodeWriter.writeField(node.getVersion());
		nodeWriter.writeField(node.getUser().getId());
		nodeWriter.writeField(node.getTimestamp());
		nodeWriter.writeField(node.getChangesetId());
		nodeWriter.writeField(pointBuilder.createPoint(node.getLatitude(), node.getLongitude()));
		nodeWriter.endRecord();
		
		for (Tag tag : node.getTags()) {
			nodeTagWriter.writeField(node.getId());
			nodeTagWriter.writeField(tag.getKey());
			nodeTagWriter.writeField(tag.getValue());
			nodeTagWriter.endRecord();
		}
		
		if (enableBboxBuilder || enableLinestringBuilder) {
			wayGeometryBuilder.addNodeLocation(node);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		Way way;
		int sequenceId;
		
		way = wayContainer.getEntity();
		
		// Ignore ways with a single node because they can't be loaded into postgis.
		if (way.getWayNodes().size() > 1) {
			wayWriter.writeField(way.getId());
			wayWriter.writeField(way.getVersion());
			wayWriter.writeField(way.getUser().getId());
			wayWriter.writeField(way.getTimestamp());
			wayWriter.writeField(way.getChangesetId());
			if (enableBboxBuilder) {
				wayWriter.writeField(wayGeometryBuilder.createWayBbox(way));
			}
			if (enableLinestringBuilder) {
				wayWriter.writeField(wayGeometryBuilder.createWayLinestring(way));
			}
			wayWriter.endRecord();
			
			for (Tag tag : way.getTags()) {
				wayTagWriter.writeField(way.getId());
				wayTagWriter.writeField(tag.getKey());
				wayTagWriter.writeField(tag.getValue());
				wayTagWriter.endRecord();
			}
			
			sequenceId = 0;
			for (WayNode wayNode : way.getWayNodes()) {
				wayNodeWriter.writeField(way.getId());
				wayNodeWriter.writeField(wayNode.getNodeId());
				wayNodeWriter.writeField(sequenceId++);
				wayNodeWriter.endRecord();
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		Relation relation;
		int memberSequenceId;
		
		relation = relationContainer.getEntity();
		
		relationWriter.writeField(relation.getId());
		relationWriter.writeField(relation.getVersion());
		relationWriter.writeField(relation.getUser().getId());
		relationWriter.writeField(relation.getTimestamp());
		relationWriter.writeField(relation.getChangesetId());
		relationWriter.endRecord();
		
		for (Tag tag : relation.getTags()) {
			relationTagWriter.writeField(relation.getId());
			relationTagWriter.writeField(tag.getKey());
			relationTagWriter.writeField(tag.getValue());
			relationTagWriter.endRecord();
		}
		
		memberSequenceId = 0;
		for (RelationMember member : relation.getMembers()) {
			relationMemberWriter.writeField(relation.getId());
			relationMemberWriter.writeField(member.getMemberId());
			relationMemberWriter.writeField(memberTypeValueMapper.getMemberType(member.getMemberType()));
			relationMemberWriter.writeField(member.getMemberRole());
			relationMemberWriter.writeField(memberSequenceId++);
			relationMemberWriter.endRecord();
		}
	}
	
	
	/**
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		writerContainer.complete();
	}
	
	
	/**
	 * Releases all resources.
	 */
	public void release() {
		writerContainer.release();
		wayGeometryBuilder.release();
	}
}
