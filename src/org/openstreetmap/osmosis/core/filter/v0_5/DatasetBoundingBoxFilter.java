// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.v0_5;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_5.Dataset;
import org.openstreetmap.osmosis.core.container.v0_5.DatasetReader;
import org.openstreetmap.osmosis.core.container.v0_5.EntityContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.task.v0_5.DatasetSinkSource;
import org.openstreetmap.osmosis.core.task.v0_5.Sink;


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
