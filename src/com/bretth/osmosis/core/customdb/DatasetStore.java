package com.bretth.osmosis.core.customdb;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.domain.v0_5.Node;
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
public class DatasetStore implements Sink, EntityProcessor {
	
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
		
		nodeObjectStore = new RandomAccessObjectStore<Node>(new SingleClassObjectSerializationFactory(Node.class), "nos");
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
		
		wayTileIndexWriter = new WayTileAreaIndex(fileManager);
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
	public void process(RelationContainer relation) {
		// Do nothing.
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
	
	
	/*
	 * Uses a RandomAccessObjectStore for storage of all node objects.
	 * Uses a RandomAccessObjectStore for storage of all way objects.
	 * Uses a RandomAccessObjectStore for storage of all relation objects.
	 * 
	 * A two-field index for storing a long node id with a long file offset for the node object store.
	 * A two-field index for storing a long way id with a long file offset for the way object store.
	 * A two-field index for storing a long relation id with a long file offset for the relation object store.
	 * 
	 * A two-field index for storing a 32 bit (int) tile id with an associated node id.
	 * 
	 * A two-field index for storing a 32 bit (int) tile id with an associated way id.
	 * A two-field index for storing a 28 bit (int) tile id with an associated way id.
	 * A two-field index for storing a 24 bit (int) tile id with an associated way id.
	 * A two-field index for storing a 16 bit (char) tile id with an associated way id.
	 * A two-field index for storing a 8 bit (byte) tile id with an associated way id.
	 * A one-field index with all remaining way ids.
	 * 
	 * A two-field index for storing a long node id with an associated relation id.
	 * A two-field index for storing a long way id with an associated relation id.
	 * A two-field index for storing a long relation id with an associated relation id.
	 */
}
