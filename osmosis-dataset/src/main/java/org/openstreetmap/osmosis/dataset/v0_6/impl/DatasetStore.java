// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.Dataset;
import org.openstreetmap.osmosis.core.container.v0_6.DatasetContext;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.CompletableContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.sort.v0_6.SortedEntityPipeValidator;
import org.openstreetmap.osmosis.core.store.ComparableComparator;
import org.openstreetmap.osmosis.core.store.IndexStore;
import org.openstreetmap.osmosis.core.store.IndexStoreReader;
import org.openstreetmap.osmosis.core.store.IntegerLongIndexElement;
import org.openstreetmap.osmosis.core.store.LongLongIndexElement;
import org.openstreetmap.osmosis.core.store.NoSuchIndexElementException;
import org.openstreetmap.osmosis.core.store.RandomAccessObjectStore;
import org.openstreetmap.osmosis.core.store.RandomAccessObjectStoreReader;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.UnsignedIntegerComparator;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.util.TileCalculator;


/**
 * Provides a file based storage mechanism for implementing a dataset.
 * 
 * @author Brett Henderson
 */
public class DatasetStore implements Sink, EntityProcessor, Dataset {
	
	private static final Logger LOG = Logger.getLogger(DatasetStore.class.getName());
	
	
	private SortedEntityPipeValidator sortedPipeValidator;
	private TileCalculator tileCalculator;
	private UnsignedIntegerComparator uintComparator;
	
	private boolean enableWayTileIndex;
	
	private CompletableContainer storeContainer;
	private RandomAccessObjectStore<Node> nodeObjectStore;
	private IndexStore<Long, LongLongIndexElement> nodeObjectOffsetIndexWriter;
	private IndexStore<Integer, IntegerLongIndexElement> nodeTileIndexWriter;
	private RandomAccessObjectStore<Way> wayObjectStore;
	private IndexStore<Long, LongLongIndexElement> wayObjectOffsetIndexWriter;
	private WayTileAreaIndex wayTileIndexWriter;
	private IndexStore<Long, LongLongIndexElement> nodeWayIndexWriter;
	private RandomAccessObjectStore<Relation> relationObjectStore;
	private IndexStore<Long, LongLongIndexElement> relationObjectOffsetIndexWriter;
	private IndexStore<Long, LongLongIndexElement> nodeRelationIndexWriter;
	private IndexStore<Long, LongLongIndexElement> wayRelationIndexWriter;
	private IndexStore<Long, LongLongIndexElement> relationRelationIndexWriter;
	
	private RandomAccessObjectStoreReader<Node> nodeObjectReader;
	private IndexStoreReader<Long, LongLongIndexElement> nodeObjectOffsetIndexReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param fileManager
	 *            The manager providing access to store files.
	 * @param enableWayTileIndex
	 *            If true a tile index is created for ways, otherwise a node-way
	 *            index is used.
	 */
	public DatasetStore(DatasetStoreFileManager fileManager, boolean enableWayTileIndex) {
		this.enableWayTileIndex = enableWayTileIndex;
		
		storeContainer = new CompletableContainer();
		
		// Validate all input data to ensure it is sorted.
		sortedPipeValidator = new SortedEntityPipeValidator();
		sortedPipeValidator.setSink(new Sink() {
			@Override
		    public void initialize(Map<String, Object> metaData) {
				throw new UnsupportedOperationException();
			}
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
			} });
		
		tileCalculator = new TileCalculator();
		uintComparator = new UnsignedIntegerComparator();
		
		// Create node store and indexes.
		nodeObjectStore = storeContainer.add(
			new RandomAccessObjectStore<Node>(
				new SingleClassObjectSerializationFactory(Node.class),
				fileManager.getNodeObjectFile()
			)
		);
		nodeObjectOffsetIndexWriter = storeContainer.add(
			new IndexStore<Long, LongLongIndexElement>(
			LongLongIndexElement.class,
			new ComparableComparator<Long>(),
			fileManager.getNodeObjectOffsetIndexFile()
			)
		);
		nodeTileIndexWriter = storeContainer.add(
			new IndexStore<Integer, IntegerLongIndexElement>(
			IntegerLongIndexElement.class,
			uintComparator,
			fileManager.getNodeTileIndexFile()
			)
		);
		
		// Create way store and indexes.
		wayObjectStore = storeContainer.add(
			new RandomAccessObjectStore<Way>(
				new SingleClassObjectSerializationFactory(Way.class),
				fileManager.getWayObjectFile()
			)
		);
		wayObjectOffsetIndexWriter = storeContainer.add(
			new IndexStore<Long, LongLongIndexElement>(
				LongLongIndexElement.class,
				new ComparableComparator<Long>(),
				fileManager.getWayObjectOffsetIndexFile()
			)
		);
		wayTileIndexWriter = storeContainer.add(new WayTileAreaIndex(fileManager));
		nodeWayIndexWriter = storeContainer.add(
			new IndexStore<Long, LongLongIndexElement>(
				LongLongIndexElement.class,
				new ComparableComparator<Long>(),
				fileManager.getNodeWayIndexFile()
			)
		);
		
		// Create relation store and indexes.
		relationObjectStore = storeContainer.add(
			new RandomAccessObjectStore<Relation>(
				new SingleClassObjectSerializationFactory(Relation.class),
				fileManager.getRelationObjectFile()
			)
		);
		relationObjectOffsetIndexWriter = storeContainer.add(
			new IndexStore<Long, LongLongIndexElement>(
				LongLongIndexElement.class,
				new ComparableComparator<Long>(),
				fileManager.getRelationObjectOffsetIndexFile()
			)
		);
		nodeRelationIndexWriter = storeContainer.add(
			new IndexStore<Long, LongLongIndexElement>(
				LongLongIndexElement.class,
				new ComparableComparator<Long>(),
				fileManager.getNodeRelationIndexFile()
			)
		);
		wayRelationIndexWriter = storeContainer.add(
			new IndexStore<Long, LongLongIndexElement>(
				LongLongIndexElement.class,
				new ComparableComparator<Long>(),
				fileManager.getWayRelationIndexFile()
			)
		);
		relationRelationIndexWriter = storeContainer.add(
			new IndexStore<Long, LongLongIndexElement>(
				LongLongIndexElement.class,
				new ComparableComparator<Long>(),
				fileManager.getRelationRelationIndexFile()
			)
		);
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
	@Override
    public void process(BoundContainer bound) {
        // Do nothing.
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
			nodeObjectStore.complete();
			nodeObjectReader = nodeObjectStore.createReader();
		}
		if (nodeObjectOffsetIndexReader == null) {
			nodeObjectOffsetIndexWriter.complete();
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
		
		if (enableWayTileIndex) {
		// Calculate the minimum and maximum tile indexes for the way.
		tilesFound = false;
		minimumTile = 0;
		maximumTile = 0;
		for (WayNode wayNode : way.getWayNodes()) {
			long nodeId;
			Node node;
			int tile;
			
			nodeId = wayNode.getNodeId();
			
			try {
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
				
			} catch (NoSuchIndexElementException e) {
				// Ignore any referential integrity problems.
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finest(
						"Ignoring referential integrity problem where way " + wayId
						+ " refers to non-existent node " + nodeId + "."
					);
		}
			}
		}
		
		// Write the way id to an index keyed by tile but only if tiles were
		// actually found.
		if (tilesFound) {
		wayTileIndexWriter.write(wayId, minimumTile, maximumTile);
			}
			
		} else {
			for (WayNode wayNode : way.getWayNodes()) {
				long nodeId;
				
				nodeId = wayNode.getNodeId();
				
				nodeWayIndexWriter.write(new LongLongIndexElement(nodeId, wayId));
			}
	}
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
		for (RelationMember member : relation.getMembers()) {
			EntityType memberType;
			
			memberType = member.getMemberType();
			
			if (memberType.equals(EntityType.Node)) {
				nodeRelationIndexWriter.write(new LongLongIndexElement(member.getMemberId(), relationId));
			} else if (memberType.equals(EntityType.Way)) {
				wayRelationIndexWriter.write(new LongLongIndexElement(member.getMemberId(), relationId));
			} else if (memberType.equals(EntityType.Relation)) {
				relationRelationIndexWriter.write(new LongLongIndexElement(member.getMemberId(), relationId));
			} else {
				throw new OsmosisRuntimeException("Member type " + memberType + " is not recognised.");
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		// Complete all the stores to ensure their data is fully persisted.
		storeContainer.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DatasetContext createReader() {
		ReleasableContainer releasableContainer = new ReleasableContainer();
		
		try {
			DatasetContext reader;
			
			reader = new DatasetStoreReader(
					new NodeStorageContainer(
							releasableContainer.add(nodeObjectStore.createReader()),
							releasableContainer.add(nodeObjectOffsetIndexWriter.createReader()),
							releasableContainer.add(nodeTileIndexWriter.createReader()),
							releasableContainer.add(nodeWayIndexWriter.createReader()),
							releasableContainer.add(nodeRelationIndexWriter.createReader())),
					new WayStorageContainer(
							releasableContainer.add(wayObjectStore.createReader()),
							releasableContainer.add(wayObjectOffsetIndexWriter.createReader()),
							releasableContainer.add(wayTileIndexWriter.createReader()),
							releasableContainer.add(wayRelationIndexWriter.createReader())),
					new RelationStorageContainer(
							releasableContainer.add(relationObjectStore.createReader()),
							releasableContainer.add(relationObjectOffsetIndexWriter.createReader()),
							releasableContainer.add(relationRelationIndexWriter.createReader())),
					enableWayTileIndex
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
		
		storeContainer.release();
	}
}
