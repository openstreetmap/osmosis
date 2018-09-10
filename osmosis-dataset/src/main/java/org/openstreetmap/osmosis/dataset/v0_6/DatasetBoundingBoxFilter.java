// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6;

import java.util.Collections;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.Dataset;
import org.openstreetmap.osmosis.core.container.v0_6.DatasetContext;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.task.v0_6.DatasetSinkSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


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
	private boolean completeRelations;
	private DatasetContext datasetReader;
	
	
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
	 * @param completeRelations
	 *            Include all ways for relations which have some portion inside the
	 *            filtered area.
	 */
	public DatasetBoundingBoxFilter(double left, double right, double top, double bottom, boolean completeWays,
					boolean completeRelations) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.completeWays = completeWays;
		this.completeRelations = completeRelations;
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
		if (datasetReader != null) {
			throw new OsmosisRuntimeException("process may only be invoked once.");
		}
		
		datasetReader = dataset.createReader();
		
		// Pass all data within the bounding box to the sink.
		try (ReleasableIterator<EntityContainer> bboxData =
				datasetReader.iterateBoundingBox(left, right, top, bottom, completeWays,
								 completeRelations)) {

			sink.initialize(Collections.<String, Object>emptyMap());
			
			while (bboxData.hasNext()) {
				sink.process(bboxData.next());
			}
			
			sink.complete();
			
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		sink.close();
		
		if (datasetReader != null) {
			datasetReader.close();
		}
	}
}
