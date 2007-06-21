package com.bretth.osmosis.sort;

import java.util.Comparator;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.data.Element;
import com.bretth.osmosis.data.Node;
import com.bretth.osmosis.data.Segment;
import com.bretth.osmosis.data.Way;
import com.bretth.osmosis.sort.impl.FileBasedSort;
import com.bretth.osmosis.sort.impl.ReleasableIterator;
import com.bretth.osmosis.task.ChangeAction;
import com.bretth.osmosis.task.ChangeSink;
import com.bretth.osmosis.task.ChangeSinkChangeSource;


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
				Element element = changeElement.getElement();
				ChangeAction action = changeElement.getAction();
				
				if (element instanceof Node) {
					sink.processNode((Node) element, action);
				} else if (element instanceof Segment) {
					sink.processSegment((Segment) element, action);
				} else if (element instanceof Way) {
					sink.processWay((Way) element, action);
				} else {
					throw new OsmosisRuntimeException("Element type " + element.getClass().getName() + " is unrecognised.");
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
