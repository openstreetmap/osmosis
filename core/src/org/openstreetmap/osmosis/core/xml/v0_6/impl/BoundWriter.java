// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_6.impl;

import java.util.Locale;

import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.xml.common.ElementWriter;

/**
 * @author KNewman
 * 
 */
public class BoundWriter extends ElementWriter {

	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public BoundWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
	}


	/**
	 * Writes the bound.
	 * 
	 * @param bound
	 *            The bound to be processed.
	 */
	public void process(Bound bound) {

		// Only add the Bound if the origin string isn't empty
		if (bound.getOrigin() != "") {
			beginOpenElement();
			// Write with the US locale (to force . instead of , as the decimal separator)
			// Use only 5 decimal places (~1.2 meter resolution should be sufficient for Bound)
			addAttribute("box", String.format(
			        Locale.US,
			        "%.5f,%.5f,%.5f,%.5f",
			        bound.getBottom(),
			        bound.getLeft(),
			        bound.getTop(),
			        bound.getRight()));
			addAttribute("origin", bound.getOrigin());
			endOpenElement(true);
		}
	}
}
