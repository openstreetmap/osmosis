// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.io.Writer;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.xml.common.ElementWriter;


/**
 * Renders OSM changes as xml.
 *
 * @author Brett Henderson
 */
public class OsmChangeWriter extends ElementWriter {

    /**
     * The OsmWriter to use for created elements.
     */
    private OsmWriter osmCreateWriter;

    /**
     * The OsmWriter to use for modified elements.
     */
    private OsmWriter osmModifyWriter;

    /**
     * The OsmWriter to use for deleted elements.
     */
    private OsmWriter osmDeleteWriter;
    /**
     * @see #updateActiveOsmWriter(ChangeAction)
     */
    private OsmWriter activeOsmWriter;
    /**
     * The last action (add, modify, delete)
     * that we processed.
     */
    private ChangeAction lastAction;

    /**
     * Creates a new instance that
     * starts with an &lt;osmChange&gt; -element
     * at indent-level 0.
     */
    public OsmChangeWriter() {
       this("osmChange", 0);
    }

	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public OsmChangeWriter(final String elementName, final int indentLevel) {
		super(elementName, indentLevel);
		
		osmCreateWriter = new OsmWriter("create", indentLevel + 1, false, false);
		osmModifyWriter = new OsmWriter("modify", indentLevel + 1, false, false);
		osmDeleteWriter = new OsmWriter("delete", indentLevel + 1, false, false);
		activeOsmWriter = null;
		lastAction = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWriter(final Writer aWriter) {
		super.setWriter(aWriter);

		this.osmCreateWriter.setWriter(aWriter);
		this.osmModifyWriter.setWriter(aWriter);
		this.osmDeleteWriter.setWriter(aWriter);
	}

	/**
	 * Begins an &lt;osmchange&gt;-element.
	 */
	public void begin() {
		beginOpenElement();
		addAttribute("version", XmlConstants.OSM_VERSION);
		addAttribute("generator", "Osmosis " + OsmosisConstants.VERSION);
		endOpenElement(false);
	}

	/**
	 * Ends an &lt;osmchange&gt;-element.
	 */
	public void end() {
		if (activeOsmWriter != null) {
			activeOsmWriter.end();
			activeOsmWriter = null;
		}

		lastAction = null;
		closeElement();
	}

	/**
	 * Returns the appropriate osm writer for the particular change type.
	 * 
	 * @param action
	 *            The change action to be performed.
	 * @return The osm writer for the change type.
	 */
	private OsmWriter getWriterForAction(final ChangeAction action) {
		if (action.equals(ChangeAction.Create)) {
			return osmCreateWriter;
		} else if (action.equals(ChangeAction.Modify)) {
			return osmModifyWriter;
		} else if (action.equals(ChangeAction.Delete)) {
			return osmDeleteWriter;
		} else {
			throw new OsmosisRuntimeException("The change action " + action + " is not recognised.");
		}
	}

	/**
	 * Switch to another type of change.
	 * @param action the action to apply to the next elements.
	 */
	private void updateActiveOsmWriter(final ChangeAction action) {
		if (action != lastAction) {
			if (activeOsmWriter != null) {
				activeOsmWriter.end();
			}

			activeOsmWriter = getWriterForAction(action);

			activeOsmWriter.begin();

			lastAction = action;
		}
	}

	/**
	 * Writes the change in the container.
	 * 
	 * @param changeContainer
	 *            The container holding the change.
	 */
	public void process(final ChangeContainer changeContainer) {
		updateActiveOsmWriter(changeContainer.getAction());
		activeOsmWriter.process(changeContainer.getEntityContainer());
	}
}
