package com.bretth.osmosis.change;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.change.impl.DataPostbox;
import com.bretth.osmosis.container.ChangeContainer;
import com.bretth.osmosis.container.ElementContainer;
import com.bretth.osmosis.sort.ElementByTypeThenIdComparator;
import com.bretth.osmosis.task.ChangeAction;
import com.bretth.osmosis.task.ChangeSink;
import com.bretth.osmosis.task.MultiSinkMultiChangeSinkRunnableSource;
import com.bretth.osmosis.task.Sink;


/**
 * Applies a change set to an input source and produces an updated data set.
 * 
 * @author Brett Henderson
 */
public class ChangeApplier implements MultiSinkMultiChangeSinkRunnableSource {
	
	private Sink sink;
	private DataPostbox<ElementContainer> basePostbox;
	private DataPostbox<ChangeContainer> changePostbox;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inputBufferCapacity
	 *            The size of the buffers to use for input sources.
	 */
	public ChangeApplier(int inputBufferCapacity) {
		basePostbox = new DataPostbox<ElementContainer>(inputBufferCapacity);
		changePostbox = new DataPostbox<ChangeContainer>(inputBufferCapacity);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Sink getSink(int instance) {
		final DataPostbox<ElementContainer> destinationPostbox = basePostbox;
		
		if (instance != 0) {
			throw new OsmosisRuntimeException("Sink instance " + instance
					+ " is not valid.");
		}
		
		return new Sink() {
			private DataPostbox<ElementContainer> postbox = destinationPostbox;
			
			public void process(ElementContainer elementContainer) {
				postbox.put(elementContainer);
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
	 * This implementation always returns 1.
	 * 
	 * @return 1
	 */
	public int getSinkCount() {
		return 1;
	}


	/**
	 * {@inheritDoc}
	 */
	public ChangeSink getChangeSink(int instance) {
		final DataPostbox<ChangeContainer> destinationPostbox = changePostbox;
		
		if (instance != 0) {
			throw new OsmosisRuntimeException("Change sink instance " + instance
					+ " is not valid.");
		}
		
		return new ChangeSink() {
			private DataPostbox<ChangeContainer> postbox = destinationPostbox;

			public void process(ChangeContainer change) {
				postbox.put(change);
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
	 * This implementation always returns 1.
	 * 
	 * @return 1
	 */
	public int getChangeSinkCount() {
		return 1;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}


	/**
	 * Processes the input sources and sends the updated data stream to the
	 * sink.
	 */
	public void run() {
		boolean completed = false;
		
		try {
			ElementByTypeThenIdComparator comparator;
			ElementContainer base = null;
			ChangeContainer change = null;
			
			// Create a comparator for comparing two elements by type and identifier.
			comparator = new ElementByTypeThenIdComparator();
			
			// We continue in the comparison loop while both sources still have data.
			while ((base != null || basePostbox.hasNext()) && (change != null || changePostbox.hasNext())) {
				int comparisonResult;
				
				// Get the next input data where required.
				if (base == null) {
					base = basePostbox.getNext();
				}
				if (change == null) {
					change = changePostbox.getNext();
				}
				
				// Compare the two sources.
				comparisonResult = comparator.compare(base, change.getElement());
				
				if (comparisonResult < 0) {
					// The base element doesn't exist on the change source therefore we simply pass it through.
					sink.process(base);
					base = null;
				} else if (comparisonResult > 0) {
					// This element doesn't exist in the "base" source therefore we
					// are expecting an add.
					if (change.getAction().equals(ChangeAction.Create)) {
						sink.process(change.getElement());
						
					} else {
						throw new OsmosisRuntimeException(
							"Cannot perform action " + change.getAction() + " on node with id="
							+ change.getElement().getElement().getId()
							+ " because it doesn't exist in the base source."
						);
					}
					
					change = null;
					
				} else {
					// The same element exists in both sources therefore we are
					// expecting a modify or delete.
					if (change.getAction().equals(ChangeAction.Modify)) {
						sink.process(change.getElement());
						
					} else if (change.getAction().equals(ChangeAction.Delete)) {
						// We don't need to do anything for delete.
						
					} else {
						throw new OsmosisRuntimeException(
							"Cannot perform action " + change.getAction() + " on node with id="
							+ change.getElement().getElement().getId()
							+ " because it exists in the base source."
						);
					}
					
					base = null;
					change = null;
				}
			}
			
			// Any remaining "base" elements are unmodified.
			while (base != null || basePostbox.hasNext()) {
				if (base == null) {
					base = basePostbox.getNext();
				}
				sink.process(base);
				base = null;
			}
			// Any remaining "change" elements must be creates.
			while (change != null || changePostbox.hasNext()) {
				if (change == null) {
					change = changePostbox.getNext();
				}
				// This element doesn't exist in the "base" source therefore we
				// are expecting an add.
				if (change.getAction().equals(ChangeAction.Create)) {
					sink.process(change.getElement());
					
				} else {
					throw new OsmosisRuntimeException(
						"Cannot perform action " + change.getAction() + " on node with id="
						+ change.getElement().getElement().getId()
						+ " because it doesn't exist in the base source."
					);
				}
				
				change = null;
			}
			
			sink.complete();
			completed = true;
			
		} finally {
			if (!completed) {
				basePostbox.setOutputError();
				changePostbox.setOutputError();
			}
			
			sink.release();
		}
	}
}
