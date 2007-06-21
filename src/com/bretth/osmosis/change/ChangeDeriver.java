package com.bretth.osmosis.change;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.change.impl.DataPostbox;
import com.bretth.osmosis.change.impl.ElementContainer;
import com.bretth.osmosis.change.impl.NodeContainer;
import com.bretth.osmosis.change.impl.SegmentContainer;
import com.bretth.osmosis.change.impl.WayContainer;
import com.bretth.osmosis.data.Node;
import com.bretth.osmosis.data.Segment;
import com.bretth.osmosis.data.Way;
import com.bretth.osmosis.sort.ElementByTypeThenIdComparator;
import com.bretth.osmosis.task.ChangeAction;
import com.bretth.osmosis.task.ChangeSink;
import com.bretth.osmosis.task.MultiSinkRunnableChangeSource;
import com.bretth.osmosis.task.Sink;


/**
 * Compares two different data sources and produces a set of differences.
 * 
 * @author Brett Henderson
 */
public class ChangeDeriver implements MultiSinkRunnableChangeSource {

	private ChangeSink changeSink;
	private DataPostbox<ElementContainer> fromPostbox;
	private DataPostbox<ElementContainer> toPostbox;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inputBufferCapacity
	 *            The size of the buffers to use for input sources.
	 */
	public ChangeDeriver(int inputBufferCapacity) {
		fromPostbox = new DataPostbox<ElementContainer>(inputBufferCapacity);
		toPostbox = new DataPostbox<ElementContainer>(inputBufferCapacity);
	}


	/**
	 * {@inheritDoc}
	 */
	public Sink getSink(int instance) {
		final DataPostbox<ElementContainer> destinationPostbox;
		
		switch (instance) {
		case 0:
			destinationPostbox = fromPostbox;
			break;
		case 1:
			destinationPostbox = toPostbox;
			break;
		default:
			throw new OsmosisRuntimeException("Sink instance " + instance
					+ " is not valid.");
		}
		
		return new Sink() {
			private DataPostbox<ElementContainer> postbox = destinationPostbox;
			public void processNode(Node node) {
				postbox.put(new NodeContainer(node));
			}
			public void processSegment(Segment segment) {
				postbox.put(new SegmentContainer(segment));
			}
			public void processWay(Way way) {
				postbox.put(new WayContainer(way));
			}
			public void complete() {
				postbox.complete();
			}
			public void release() {
				postbox.release();
			}
		};
	}


	/**
	 * This implementation always returns 2.
	 * 
	 * @return 2
	 */
	public int getSinkCount() {
		return 2;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * Processes the input sources and sends the changes to the change sink.
	 */
	public void run() {
		boolean completed = false;
		
		try {
			ElementByTypeThenIdComparator comparator;
			ElementContainer fromElement = null;
			ElementContainer toElement = null;
			
			// Create a comparator for comparing two elements by type and identifier.
			comparator = new ElementByTypeThenIdComparator();
			
			// We continue in the comparison loop while both sources still have data.
			while ((fromElement != null || fromPostbox.hasNext()) && (toElement != null || toPostbox.hasNext())) {
				int comparisonResult;
				
				// Get the next input data where required.
				if (fromElement == null) {
					fromElement = fromPostbox.getNext();
				}
				if (toElement == null) {
					toElement = toPostbox.getNext();
				}
				
				// Compare the two sources.
				comparisonResult = comparator.compare(fromElement.getElement(), toElement.getElement());
				
				if (comparisonResult < 0) {
					// The from element doesn't exist on the to source therefore has
					// been deleted.
					fromElement.processChange(changeSink, ChangeAction.Delete);
					fromElement = null;
				} else if (comparisonResult > 0) {
					// The to element doesn't exist on the from source therefore has
					// been created.
					toElement.processChange(changeSink, ChangeAction.Create);
					toElement = null;
				} else {
					// The element exists on both sources, therefore we must
					// compare
					// the elements directly. If there is a difference, the
					// element has been modified.
					if (!fromElement.getElement().equals(toElement.getElement())) {
						toElement.processChange(changeSink, ChangeAction.Modify);
					}
					fromElement = null;
					toElement = null;
				}
			}
			
			// Any remaining "from" elements are deletes.
			while (fromElement != null || fromPostbox.hasNext()) {
				if (fromElement == null) {
					fromElement = fromPostbox.getNext();
				}
				fromElement.processChange(changeSink, ChangeAction.Delete);
				fromElement = null;
			}
			// Any remaining "to" elements are creates.
			while (toElement != null || toPostbox.hasNext()) {
				if (toElement == null) {
					toElement = toPostbox.getNext();
				}
				toElement.processChange(changeSink, ChangeAction.Create);
				toElement = null;
			}
			
			changeSink.complete();
			completed = true;
			
		} finally {
			if (!completed) {
				fromPostbox.setOutputError();
				toPostbox.setOutputError();
			}
			
			changeSink.release();
		}
	}
}
