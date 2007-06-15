package com.bretth.osm.conduit.sort;

import java.util.Comparator;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.OsmElement;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.sort.impl.FileBasedSort;
import com.bretth.osm.conduit.sort.impl.ReleasableIterator;
import com.bretth.osm.conduit.task.ChangeAction;
import com.bretth.osm.conduit.task.ChangeSink;
import com.bretth.osm.conduit.task.ChangeSinkChangeSource;


/**
 * A change stream filter that sorts changes. The sort order is specified by
 * comparator provided during instantiation.
 * 
 * @author Brett Henderson
 */
public class ChangeSorter implements ChangeSinkChangeSource {
	private FileBasedSort<ChangeElement> fileBasedSort;
	private ChangeSink sink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparator
	 *            The comparator to use for sorting.
	 */
	public ChangeSorter(Comparator<ChangeElement> comparator) {
		fileBasedSort = new FileBasedSort<ChangeElement>(comparator, true);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processNode(Node node, ChangeAction action) {
		fileBasedSort.add(new ChangeElement(node, action));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processSegment(Segment segment, ChangeAction action) {
		fileBasedSort.add(new ChangeElement(segment, action));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processWay(Way way, ChangeAction action) {
		fileBasedSort.add(new ChangeElement(way, action));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		ReleasableIterator<ChangeElement> iterator = null;
		
		try {
			iterator = fileBasedSort.iterate();
			
			while (iterator.hasNext()) {
				ChangeElement changeElement = iterator.next();
				OsmElement element = changeElement.getElement();
				ChangeAction action = changeElement.getAction();
				
				if (element instanceof Node) {
					sink.processNode((Node) element, action);
				} else if (element instanceof Segment) {
					sink.processSegment((Segment) element, action);
				} else if (element instanceof Way) {
					sink.processWay((Way) element, action);
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
