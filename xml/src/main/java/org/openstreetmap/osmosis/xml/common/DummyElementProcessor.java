// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;

import org.xml.sax.Attributes;



/**
 * Provides a no-op implementation of an element processor. This implementation
 * is provided to allow nested elements to be ignored if they are not required.
 * 
 * @author Brett Henderson
 */
public class DummyElementProcessor extends BaseElementProcessor {
	
	private int nestedElementCount;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 */
	public DummyElementProcessor(BaseElementProcessor parentProcessor) {
		super(parentProcessor, false);
	}
	
	
	/**
	 * This implementation does not do any processing.
	 * 
	 * @param attributes
	 *            The attributes of the new element.
	 */
	public void begin(Attributes attributes) {
		// Nothing to do because we're not processing this element.
	}
	
	
	/**
	 * This implementation returns itself and increments an internal counter.
	 * The corresponding getParent method decrements the counter and when it
	 * reaches zero returns the true parent of this instance.
	 * 
	 * @param uri
	 *            The element uri.
	 * @param localName
	 *            The element localName.
	 * @param qName
	 *            The element qName.
	 * @return This instance.
	 */
	@Override
	public ElementProcessor getChild(String uri, String localName, String qName) {
		nestedElementCount++;
		
		return this;
	}
	
	
	/**
	 * This implementation decrements an internal counter, if the counter
	 * reaches zero the true parent is returned, else this instance is returned.
	 * 
	 * @return The element processor for the parent of the current element.
	 */
	@Override
	public ElementProcessor getParent() {
		if (nestedElementCount > 0) {
			nestedElementCount--;
			return this;
		} else {
			return super.getParent();
		}
	}
	
	
	/**
	 * This implementation does not do any processing.
	 */
	public void end() {
		// Nothing to do because we're not processing this element.
	}
}
