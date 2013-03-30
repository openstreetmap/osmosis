// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.report.v0_6;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.filter.common.BitSetIdTracker;
import org.openstreetmap.osmosis.core.filter.common.IdTracker;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * A sink that verifies the referential integrity of all data passing through
 * it.
 * 
 * @author Brett Henderson
 */
public class IntegrityReporter implements Sink, EntityProcessor {
	
	private static final Logger LOG = Logger.getLogger(IntegrityReporter.class.getName());
	
	private File file;
	private boolean initialized;
	private BufferedWriter writer;
	private IdTracker nodeBitSet;
	private IdTracker wayBitSet;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 */
	public IntegrityReporter(File file) {
		this.file = file;
		
		initialized = false;
		nodeBitSet = new BitSetIdTracker();
		wayBitSet = new BitSetIdTracker();
	}
	
	
	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	private void write(String data) {
		try {
			writer.write(data);
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Writes a new line in the output file.
	 */
	private void writeNewLine() {
		try {
			writer.newLine();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Initialises the output file for writing. This must be called by
	 * sub-classes before any writing is performed. This method may be called
	 * multiple times without adverse affect allowing sub-classes to invoke it
	 * every time they perform processing.
	 */
	protected void initialize() {
		if (!initialized) {
			OutputStream outStream = null;
			
			try {
				outStream = new FileOutputStream(file);
				
				writer = new BufferedWriter(new OutputStreamWriter(outStream, "UTF-8"));
				
				outStream = null;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open file " + file + " for writing.", e);
			} finally {
				if (outStream != null) {
					try {
						outStream.close();
					} catch (Exception e) {
						LOG.log(Level.SEVERE, "Unable to close output stream for file " + file + ".", e);
					}
					outStream = null;
				}
			}
			
			initialized = true;
			
			write("Entity Type, Entity Id, Referred Type, Referred Id");
			writeNewLine();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void initialize(Map<String, Object> metaData) {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
    public void process(BoundContainer bound) {
	    // Do nothing.
    }


    /**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer node) {
		nodeBitSet.set(node.getEntity().getId());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		Way way;
		
		way = wayContainer.getEntity();
		
		wayBitSet.set(way.getId());
		
		for (WayNode wayNode : way.getWayNodes()) {
			if (!nodeBitSet.get(wayNode.getNodeId())) {
				initialize();
				
				write("Way," + way.getId() + ",Node," + wayNode.getNodeId());
				writeNewLine();
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		Relation relation;
		
		relation = relationContainer.getEntity();
		
		for (RelationMember relationMember : relation.getMembers()) {
			EntityType memberType;
			
			memberType = relationMember.getMemberType();
			
			if (EntityType.Node.equals(memberType)) {
				if (!nodeBitSet.get(relationMember.getMemberId())) {
					initialize();
					
					write("Relation," + relation.getId() + ",Node," + relationMember.getMemberId());
					writeNewLine();
				}
			} else if (EntityType.Way.equals(memberType)) {
				if (!wayBitSet.get(relationMember.getMemberId())) {
					initialize();
					
					write("Relation," + relation.getId() + ",Way," + relationMember.getMemberId());
					writeNewLine();
				}
			}
		}
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		try {
			if (writer != null) {
				writer.close();
			}
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to complete writing to the file " + file + ".", e);
		} finally {
			initialized = false;
			writer = null;
		}
	}
	
	
	/**
	 * Cleans up any open file handles.
	 */
	public void release() {
		try {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Unable to close writer.", e);
			}
		} finally {
			initialized = false;
			writer = null;
		}
	}
}
