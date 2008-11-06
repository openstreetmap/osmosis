// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.v0_5;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.Dataset;
import com.bretth.osmosis.core.container.v0_5.DatasetReader;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.lifecycle.ReleasableIterator;
import com.bretth.osmosis.core.task.v0_5.DatasetSinkSource;
import com.bretth.osmosis.core.task.v0_5.Sink;


/**
 * Provides a filter utilising a dataset to extract all entities that lie within
 * a specific geographical box identified by latitude and longitude coordinates.
 * 
 * @author Brett Henderson
 */
public class DatasetBoundingBoxFilter implements DatasetSinkSource {
	private Sink sink;
	private double left;
	private double right;
	private double top;
	private double bottom;
	private boolean completeWays;
	private DatasetReader datasetReader;
	
	
	/**
	 * Creates a new instance with the specified geographical coordinates. When
	 * filtering, nodes right on the left and bottom edges of the box will be
	 * included, nodes on the top and right edges will be excluded.
	 * 
	 * @param left
	 *            The longitude marking the left edge of the bounding box.
	 * @param right
	 *            The longitude marking the right edge of the bounding box.
	 * @param top
	 *            The latitude marking the top edge of the bounding box.
	 * @param bottom
	 *            The latitude marking the bottom edge of the bounding box.
	 * @param completeWays
	 *            Include all nodes for ways which have some portion inside the
	 *            filtered area.
	 */
	public DatasetBoundingBoxFilter(double left, double right, double top, double bottom, boolean completeWays) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.completeWays = completeWays;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(Dataset dataset) {
		ReleasableIterator<EntityContainer> bboxData;
		
		if (datasetReader != null) {
			throw new OsmosisRuntimeException("process may only be invoked once.");
		}
		
		datasetReader = dataset.createReader();
		
		// Pass all data within the bounding box to the sink.
		bboxData = datasetReader.iterateBoundingBox(left, right, top, bottom, completeWays);
		try {
			while (bboxData.hasNext()) {
				sink.process(bboxData.next());
			}
			
			sink.complete();
			
		} finally {
			bboxData.release();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (datasetReader != null) {
			datasetReader.release();
		}
	}
}
