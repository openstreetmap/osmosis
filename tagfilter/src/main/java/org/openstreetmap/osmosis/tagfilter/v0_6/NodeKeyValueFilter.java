// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;
import org.openstreetmap.osmosis.tagfilter.common.KeyValueFileReader;


/**
 * A class filtering everything but allowed nodes.
 *
 * @author Aurelien Jacobs
 */
public class NodeKeyValueFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private HashSet<String> allowedKeyValues;

	/**
	 * Creates a new instance.
	 *
	 * @param keyValueList
	 *            Comma-separated list of allowed key-value combinations,
	 *            e.g. "place.city,place.town"
	 */
	public NodeKeyValueFilter(String keyValueList) {

		allowedKeyValues = new HashSet<String>();
		String[] keyValues = keyValueList.split(",");
		for (int i = 0; i < keyValues.length; i++) {
			allowedKeyValues.add(keyValues[i]);
		}

	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param keyValueListFile
	 *            File containing one key-value combination per line
	 */
	public NodeKeyValueFilter(File keyValueListFile) {
		
		String[] keyValues;
		try {
			KeyValueFileReader reader = new KeyValueFileReader(keyValueListFile);
			keyValues = reader.loadKeyValues();
		} catch (FileNotFoundException ex) {
			throw new OsmosisRuntimeException("Unable to find key.value file " + keyValueListFile.getAbsolutePath()
					+ ".", ex);
		} catch (IOException ex) {
			throw new OsmosisRuntimeException("Unable to read from key.value file "
					+ keyValueListFile.getAbsolutePath() + ".", ex);
		}

		allowedKeyValues = new HashSet<String>();
		for (int i = 0; i < keyValues.length; i++) {
			allowedKeyValues.add(keyValues[i]);
		}

	}
    
    
    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		sink.initialize(metaData);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// Ask the entity container to invoke the appropriate processing method
		// for the entity type.
		entityContainer.process(this);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {
		// By default, pass it on unchanged
		sink.process(boundContainer);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer container) {
		Node node = container.getEntity();

		boolean matchesFilter = false;
		for (Tag tag : node.getTags()) {
			String keyValue = tag.getKey() + "." + tag.getValue();
			if (allowedKeyValues.contains(keyValue)) {
				matchesFilter = true;
				break;
			}
		}

		if (matchesFilter) {
			sink.process(container);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer container) {
		// Do nothing.
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		// Do nothing.
	}


	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		sink.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sink.release();
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
