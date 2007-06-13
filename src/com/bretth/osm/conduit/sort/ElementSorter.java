package com.bretth.osm.conduit.sort;

import java.util.Comparator;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.OsmElement;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.sort.impl.FileBasedSort;
import com.bretth.osm.conduit.sort.impl.ReleasableIterator;
import com.bretth.osm.conduit.task.Sink;
import com.bretth.osm.conduit.task.SinkSource;

/**
 * A data stream filter that sorts elements. The sort order is specified by
 * comparator provided during instantiation.
 * 
 * @author Brett Henderson
 */
public class ElementSorter implements SinkSource {
	private FileBasedSort<OsmElement> fileBasedSort;
	private Sink sink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparator
	 *            The comparator to use for sorting.
	 */
	public ElementSorter(Comparator<OsmElement> comparator) {
		fileBasedSort = new FileBasedSort<OsmElement>(comparator, false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processNode(Node node) {
		fileBasedSort.add(node);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processSegment(Segment segment) {
		fileBasedSort.add(segment);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processWay(Way way) {
		fileBasedSort.add(way);
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
		ReleasableIterator<OsmElement> iterator = null;
		
		try {
			iterator = fileBasedSort.iterate();
			
			while (iterator.hasNext()) {
				OsmElement element;
				
				element = iterator.next();
				
				if (element instanceof Node) {
					sink.processNode((Node) element);
				} else if (element instanceof Segment) {
					sink.processSegment((Segment) element);
				} else if (element instanceof Way) {
					sink.processWay((Way) element);
				} else {
					throw new ConduitRuntimeException("Element type " + element.getClass().getName() + " is unrecognised.");
				}
			}
			
			sink.complete();
		} finally {
			if (iterator != null) {
				iterator.release();
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		fileBasedSort.release();
		sink.release();
	}
}
