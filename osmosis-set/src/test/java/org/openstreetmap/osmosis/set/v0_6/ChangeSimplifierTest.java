// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.misc.v0_6.NullChangeWriter;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests the change simplifier task.
 */
public class ChangeSimplifierTest extends AbstractDataTest {
	
	/**
	 * Tests that a set of changes is simplified correctly.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void commonCase() throws IOException {
		File sourceFile;
		File expectedOutputFile;
		File actualOutputFile;
		
		// Generate files.
		sourceFile = dataUtils.createDataFile("v0_6/simplify-change-in.osc");
		expectedOutputFile = dataUtils.createDataFile("v0_6/simplify-change-out.osc");
		actualOutputFile = dataUtils.newFile();
		
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-change-0.6",
				sourceFile.getPath(),
				"--simplify-change-0.6",
				"--write-xml-change-0.6",
				actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}
	
	/**
	 * Tests that simplifying an already simple change successfully 
	 * yields the same change.
	 * 
	 * @throws Exception
	 *             if anything fails.
	 */
	@Test
	public void alreadySimple() throws Exception {
		File sourceFile;
		File expectedOutputFile;
		File actualOutputFile;
		
		sourceFile = dataUtils.createDataFile("v0_6/simplify-change-out.osc");
		expectedOutputFile = dataUtils.createDataFile("v0_6/simplify-change-out.osc");
		actualOutputFile = dataUtils.newFile();

		Osmosis.run(
				new String [] {
					"-q",
					"--read-xml-change-0.6", sourceFile.getPath(),
					"--simplify-change-0.6",
					"--write-xml-change-0.6", actualOutputFile.getPath()
				}
			);

		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}

	/**
	 * Tests that simplifying an empty change successfully 
	 * yields an empty change.
	 * 
	 * @throws Exception
	 *             if anything fails.
	 */
	@Test
	public void empty() throws Exception {
		File expectedOutputFile;
		File actualOutputFile;
		
		expectedOutputFile = dataUtils.createDataFile("v0_6/empty-change.osc");
		actualOutputFile = dataUtils.newFile();

		Osmosis.run(
				new String [] {
					"-q",
					"--read-empty-change-0.6",
					"--simplify-change-0.6",
					"--write-xml-change-0.6", actualOutputFile.getPath()
				}
			);

		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}
	
	/**
	 * Tests that badly ordered input (with respect to the version) is detected correctly.
	 * 
	 * @throws Exception
	 *             if anything fails.
	 */
	@Test
	public void badSortOrderVersion() throws Exception {
		ChangeSimplifier simplifier = new ChangeSimplifier();
		simplifier.setChangeSink(new NullChangeWriter());
		simplifier.initialize(new HashMap<String, Object>());
		Node node;

		node = new Node(new CommonEntityData(1, 2, new Date(), OsmUser.NONE, 2), 1, 1);
		simplifier.process(new ChangeContainer(new NodeContainer(node), ChangeAction.Modify));

		try {
			node = new Node(new CommonEntityData(1, 1, new Date(), OsmUser.NONE, 1), 1, 1);
			simplifier.process(new ChangeContainer(new NodeContainer(node), ChangeAction.Modify));
		} catch (OsmosisRuntimeException e) {
			if (e.getMessage().startsWith("Pipeline entities are not sorted")) {
				return;
			}
			throw e;
		}
		Assert.fail("Expected exception not thrown");
	}
	
	/**
	 * Tests that badly ordered input (with respect to the ids) is detected correctly.
	 * 
	 * @throws Exception
	 *             if anything fails.
	 */
	@Test
	public void badSortOrderId() throws Exception {
		ChangeSimplifier simplifier = new ChangeSimplifier();
		simplifier.setChangeSink(new NullChangeWriter());
		simplifier.initialize(new HashMap<String, Object>());
		Node node;

		node = new Node(new CommonEntityData(2, 2, new Date(), OsmUser.NONE, 2), 1, 1);
		simplifier.process(new ChangeContainer(new NodeContainer(node), ChangeAction.Modify));

		try {
			node = new Node(new CommonEntityData(1, 2, new Date(), OsmUser.NONE, 1), 1, 1);
			simplifier.process(new ChangeContainer(new NodeContainer(node), ChangeAction.Modify));
		} catch (OsmosisRuntimeException e) {
			if (e.getMessage().startsWith("Pipeline entities are not sorted")) {
				return;
			}
			throw e;
		}
		Assert.fail("Expected exception not thrown");
	}
	
	/**
	 * Tests that badly ordered input (with respect to the ids) is detected correctly.
	 * 
	 * @throws Exception
	 *             if anything fails.
	 */
	@Test
	public void badSortOrderType() throws Exception {
		ChangeSimplifier simplifier = new ChangeSimplifier();
		simplifier.setChangeSink(new NullChangeWriter());
		simplifier.initialize(new HashMap<String, Object>());
		Node node;
		Way way;
		
		way = new Way(new CommonEntityData(2, 2, new Date(), OsmUser.NONE, 2));
		simplifier.process(new ChangeContainer(new WayContainer(way), ChangeAction.Modify));

		try {
			node = new Node(new CommonEntityData(1, 2, new Date(), OsmUser.NONE, 1), 1, 1);
			simplifier.process(new ChangeContainer(new NodeContainer(node), ChangeAction.Modify));
		} catch (OsmosisRuntimeException e) {
			if (e.getMessage().startsWith("Pipeline entities are not sorted")) {
				return;
			}
			throw e;
		}
		Assert.fail("Expected exception not thrown");
	}
}
