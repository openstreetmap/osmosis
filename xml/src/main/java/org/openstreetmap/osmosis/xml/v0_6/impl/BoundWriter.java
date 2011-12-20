// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.util.Locale;

import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.xml.common.ElementWriter;

/**
 * @author KNewman
 * @author Igor Podolskiy
 * 
 */
public class BoundWriter extends ElementWriter {

	private boolean legacyBound;


	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 * @param legacyBound
	 *            If true, write the legacy <bound> element instead of the
	 *            correct <bounds> one.
	 */
	public BoundWriter(String elementName, int indentLevel, boolean legacyBound) {
		super(elementName, indentLevel);
		this.legacyBound = legacyBound;
	}


	/**
	 * Writes the bound.
	 * 
	 * @param bound
	 *            The bound to be processed.
	 */
	public void process(Bound bound) {
		if (legacyBound) {
			processLegacy(bound);
		} else {
			processRegular(bound);
		}
	}


	private void processRegular(Bound bound) {
		String format = "%.5f";

		beginOpenElement();
		
		addAttribute(XmlConstants.ATTRIBUTE_NAME_MINLON,
				String.format(Locale.US, format, bound.getLeft()));
		addAttribute(XmlConstants.ATTRIBUTE_NAME_MINLAT,
				String.format(Locale.US, format, bound.getBottom()));
		addAttribute(XmlConstants.ATTRIBUTE_NAME_MAXLON,
				String.format(Locale.US, format, bound.getRight()));
		addAttribute(XmlConstants.ATTRIBUTE_NAME_MAXLAT,
				String.format(Locale.US, format, bound.getTop()));
		
		if (bound.getOrigin() != null) {
			addAttribute("origin", bound.getOrigin());
		}
		
		endOpenElement(true);
	}


	private void processLegacy(Bound bound) {
		// Only add the Bound if the origin string isn't empty
		if (!"".equals(bound.getOrigin())) {
			beginOpenElement();
			// Write with the US locale (to force . instead of , as the decimal
			// separator)
			// Use only 5 decimal places (~1.2 meter resolution should be
			// sufficient for Bound)
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
