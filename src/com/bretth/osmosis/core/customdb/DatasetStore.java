package com.bretth.osmosis.core.customdb;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.index.IndexWriter;
import com.bretth.osmosis.core.index.IntLongElement;
import com.bretth.osmosis.core.index.IntLongElementFactory;
import com.bretth.osmosis.core.index.LongLongElement;
import com.bretth.osmosis.core.index.LongLongElementFactory;
import com.bretth.osmosis.core.store.RandomAccessObjectStore;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;
import com.bretth.osmosis.core.task.v0_5.Sink;


/**
 * Provides a file based storage mechanism for implementing a dataset.
 * 
 * @author Brett Henderson
 */
public class DatasetStore implements Sink, EntityProcessor {
	
	private TileCalculator tileCalculator;
	private RandomAccessObjectStore<Node> nodeObjectStore;
	private IndexWriter<LongLongElement> nodeObjectOffsetIndexWriter;
	private IndexWriter<IntLongElement> nodeTileIndexWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param fileManager
	 *            The manager providing access to store files.
	 */
	public DatasetStore(DatasetStoreFileManager fileManager) {
		tileCalculator = new TileCalculator();
		
		nodeObjectStore = new RandomAccessObjectStore<Node>(new SingleClassObjectSerializationFactory(Node.class), "nos");
		nodeObjectOffsetIndexWriter = new IndexWriter<LongLongElement>(
			fileManager.getNodeObjectOffsetIndexFile(),
			new LongLongElementFactory(),
			LongLongElement.class
		);
		nodeTileIndexWriter = new IndexWriter<IntLongElement>(
			fileManager.getNodeTileIndexFile(),
			new IntLongElementFactory(),
			IntLongElement.class
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
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
		
		objectOffset = nodeObjectStore.add(node);
		nodeObjectOffsetIndexWriter.write(
			new LongLongElement(nodeId, objectOffset)
		);
		nodeTileIndexWriter.write(
			new IntLongElement(tileCalculator.calculateTile(node.getLatitude(), node.getLongitude()),
			nodeId)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer way) {
		// Do nothing.
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
