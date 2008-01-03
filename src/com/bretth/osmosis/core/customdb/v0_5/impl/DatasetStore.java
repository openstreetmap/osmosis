package com.bretth.osmosis.core.customdb.v0_5.impl;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.Dataset;
import com.bretth.osmosis.core.container.v0_5.DatasetReader;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.domain.v0_5.EntityType;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.RelationMember;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.merge.v0_5.impl.SortedEntityPipeValidator;
import com.bretth.osmosis.core.mysql.common.TileCalculator;
import com.bretth.osmosis.core.store.ComparableComparator;
import com.bretth.osmosis.core.store.IndexStore;
import com.bretth.osmosis.core.store.IndexStoreReader;
import com.bretth.osmosis.core.store.LongLongIndexElement;
import com.bretth.osmosis.core.store.RandomAccessObjectStore;
import com.bretth.osmosis.core.store.RandomAccessObjectStoreReader;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;
import com.bretth.osmosis.core.store.IntegerLongIndexElement;
import com.bretth.osmosis.core.store.UnsignedIntegerComparator;
import com.bretth.osmosis.core.task.v0_5.Sink;


/**
 * Provides a file based storage mechanism for implementing a dataset.
 * 
 * @author Brett Henderson
 */
public class DatasetStore implements Sink, EntityProcessor, Dataset {
	
	private SortedEntityPipeValidator sortedPipeValidator;
	private TileCalculator tileCalculator;
	private RandomAccessObjectStore<Node> nodeObjectStore;
	private RandomAccessObjectStoreReader<Node> nodeObjectReader;
	private IndexStore<Long, LongLongIndexElement> nodeObjectOffsetIndexWriter;
	private IndexStoreReader<Long, LongLongIndexElement> nodeObjectOffsetIndexReader;
	private IndexStore<Integer, IntegerLongIndexElement> nodeTileIndexWriter;
	private RandomAccessObjectStore<Way> wayObjectStore;
	private IndexStore<Long, LongLongIndexElement> wayObjectOffsetIndexWriter;
	private WayTileAreaIndex wayTileIndexWriter;
	private RandomAccessObjectStore<Relation> relationObjectStore;
	private IndexStore<Long, LongLongIndexElement> relationObjectOffsetIndexWriter;
	private IndexStore<Long, LongLongIndexElement> wayRelationIndexWriter;
	private IndexStore<Long, LongLongIndexElement> nodeRelationIndexWriter;
	private UnsignedIntegerComparator uintComparator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param fileManager
	 *            The manager providing access to store files.
	 */
	public DatasetStore(DatasetStoreFileManager fileManager) {
		// Validate all input data to ensure it is sorted.
		sortedPipeValidator = new SortedEntityPipeValidator();
		sortedPipeValidator.setSink(new Sink() {
			@Override
			public void complete() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public void process(EntityContainer entityContainer) {
				processImpl(entityContainer);
			}
			
			@Override
			public void release() {
				throw new UnsupportedOperationException();
			}});
		
		tileCalculator = new TileCalculator();
		uintComparator = new UnsignedIntegerComparator();
		
		// Create node store and indexes.
		nodeObjectStore = new RandomAccessObjectStore<Node>(
			new SingleClassObjectSerializationFactory(Node.class),
			fileManager.getNodeObjectFile()
		);
		nodeObjectOffsetIndexWriter = new IndexStore<Long, LongLongIndexElement>(
			LongLongIndexElement.class,
			new ComparableComparator<Long>(),
			fileManager.getNodeObjectOffsetIndexFile()
		);
		nodeTileIndexWriter = new IndexStore<Integer, IntegerLongIndexElement>(
			IntegerLongIndexElement.class,
			uintComparator,
			fileManager.getNodeTileIndexFile()
		);
		
		// Create way store and indexes.
		wayObjectStore = new RandomAccessObjectStore<Way>(
			new SingleClassObjectSerializationFactory(Way.class),
			fileManager.getWayObjectFile()
		);
		wayObjectOffsetIndexWriter = new IndexStore<Long, LongLongIndexElement>(
			LongLongIndexElement.class,
			new ComparableComparator<Long>(),
			fileManager.getWayObjectOffsetIndexFile()
		);
		wayTileIndexWriter = new WayTileAreaIndex(fileManager);
		
		// Create relation store and indexes.
		relationObjectStore = new RandomAccessObjectStore<Relation>(
			new SingleClassObjectSerializationFactory(Relation.class),
			fileManager.getRelationObjectFile()
		);
		relationObjectOffsetIndexWriter = new IndexStore<Long, LongLongIndexElement>(
			LongLongIndexElement.class,
			new ComparableComparator<Long>(),
			fileManager.getRelationObjectOffsetIndexFile()
		);
		nodeRelationIndexWriter = new IndexStore<Long, LongLongIndexElement>(
			LongLongIndexElement.class,
			new ComparableComparator<Long>(),
			fileManager.getNodeRelationIndexFile()
		);
		wayRelationIndexWriter = new IndexStore<Long, LongLongIndexElement>(
			LongLongIndexElement.class,
			new ComparableComparator<Long>(),
			fileManager.getWayRelationIndexFile()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		sortedPipeValidator.process(entityContainer);
	}
	
	
	/**
	 * The entity processing implementation. This must not be called directly,
	 * it is called by the internal sorted pipe validator.
	 * 
	 * @param entityContainer
	 *            The entity to be processed.
	 */
	protected void processImpl(EntityContainer entityContainer) {
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer nodeContainer) {
		Node node;
		long nodeId;
		long objectOffset;
		
		node = nodeContainer.getEntity();
		nodeId = node.getId();
		
		// Write the node to the object store and save the file offset in an
		// index keyed by node id.
		objectOffset = nodeObjectStore.add(node);
		nodeObjectOffsetIndexWriter.write(
			new LongLongIndexElement(nodeId, objectOffset)
		);
		
		// Write the node id to an index keyed by tile.
		nodeTileIndexWriter.write(
			new IntegerLongIndexElement((int) tileCalculator.calculateTile(node.getLatitude(), node.getLongitude()),
			nodeId)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		Way way;
		long wayId;
		long objectOffset;
		int minimumTile;
		int maximumTile;
		boolean tilesFound;
		
		if (nodeObjectReader == null) {
			nodeObjectReader = nodeObjectStore.createReader();
		}
		if (nodeObjectOffsetIndexReader == null) {
			nodeObjectOffsetIndexReader = nodeObjectOffsetIndexWriter.createReader();
		}
		
		way = wayContainer.getEntity();
		wayId = way.getId();
		
		// Write the way to the object store and save the file offset in an
		// index keyed by way id.
		objectOffset = wayObjectStore.add(way);
		wayObjectOffsetIndexWriter.write(
			new LongLongIndexElement(wayId, objectOffset)
		);
		
		// Calculate the minimum and maximum tile indexes for the way.
		tilesFound = false;
		minimumTile = 0;
		maximumTile = 0;
		for (WayNode wayNode : way.getWayNodeList()) {
			long nodeId;
			Node node;
			int tile;
			
			nodeId = wayNode.getNodeId();
			node = nodeObjectReader.get(
				nodeObjectOffsetIndexReader.get(nodeId).getValue()
			);
			
			tile = (int) tileCalculator.calculateTile(node.getLatitude(), node.getLongitude());
			
			if (tilesFound) {
				if (uintComparator.compare(tile, minimumTile) < 0) {
					minimumTile = tile;
				}
				if (uintComparator.compare(maximumTile, tile) < 0) {
					maximumTile = tile;
				}
				
			} else {
				minimumTile = tile;
				maximumTile = tile;
				
				tilesFound = true;
			}
		}
		
		// Write the way id to an index keyed by tile.
		wayTileIndexWriter.write(wayId, minimumTile, maximumTile);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		Relation relation;
		long relationId;
		long objectOffset;
		
		relation = relationContainer.getEntity();
		relationId = relation.getId();
		
		// Write the relation to the object store and save the file offset in an
		// index keyed by relation id.
		objectOffset = relationObjectStore.add(relation);
		relationObjectOffsetIndexWriter.write(
			new LongLongIndexElement(relationId, objectOffset)
		);
		
		// Write the relation id to indexes keyed by each of the relation members.
		for (RelationMember member : relation.getMemberList()) {
			EntityType memberType;
			
			memberType = member.getMemberType();
			
			if (memberType.equals(EntityType.Node)) {
				nodeRelationIndexWriter.write(new LongLongIndexElement(member.getMemberId(), relationId));
			} else if (memberType.equals(EntityType.Way)) {
				wayRelationIndexWriter.write(new LongLongIndexElement(member.getMemberId(), relationId));
			} else if (memberType.equals(EntityType.Relation)) {
				// Do nothing.
			} else {
				throw new OsmosisRuntimeException("Member type " + memberType + " is not recognised.");
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		nodeObjectOffsetIndexWriter.complete();
		nodeTileIndexWriter.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DatasetReader createReader() {
		ReleasableContainer releasableContainer = new ReleasableContainer();
		
		try {
			DatasetReader reader;
			
			reader = new DatasetStoreReader(
				releasableContainer.add(nodeObjectStore.createReader()),
				releasableContainer.add(nodeObjectOffsetIndexWriter.createReader()),
				releasableContainer.add(wayObjectStore.createReader()),
				releasableContainer.add(wayObjectOffsetIndexWriter.createReader()),
				releasableContainer.add(relationObjectStore.createReader()),
				releasableContainer.add(relationObjectOffsetIndexWriter.createReader()),
				tileCalculator,
				uintComparator,
				releasableContainer.add(nodeTileIndexWriter.createReader()),
				releasableContainer.add(wayTileIndexWriter.createReader()),
				releasableContainer.add(nodeRelationIndexWriter.createReader()),
				releasableContainer.add(wayRelationIndexWriter.createReader())
			);
			
			// Stop the release of all created objects.
			releasableContainer.clear();
			
			return reader;
			
		} finally {
			releasableContainer.release();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (nodeObjectReader != null) {
			nodeObjectReader.release();
		}
		if (nodeObjectOffsetIndexReader != null) {
			nodeObjectOffsetIndexReader.release();
		}
		
		nodeObjectStore.release();
		nodeObjectOffsetIndexWriter.release();
		nodeTileIndexWriter.release();
	}
}
