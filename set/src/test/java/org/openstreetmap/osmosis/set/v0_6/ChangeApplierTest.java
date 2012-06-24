// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;

/**
 * Test the --apply-change task.
 * 
 * @author Igor Podolskiy
 */
public class ChangeApplierTest extends AbstractDataTest {

	
	/**
	 * Test the application of an empty change to a non-empty stream.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void emptyChange() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base.osm",
				"v0_6/empty-change.osc",
				"v0_6/apply_change/apply-change-base.osm");
	}
	
	/**
	 * Test the application of a non-empty change to an empty stream.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void emptyBase() throws Exception {
		applyChange("v0_6/empty-entity.osm",
				"v0_6/apply_change/change-delete.osc",
				"v0_6/empty-entity.osm");
	}
	
	/**
	 * Test the application of an empty change to an empty stream.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void emptyBoth() throws Exception {
		applyChange("v0_6/empty-entity.osm",
				"v0_6/empty-change.osc",
				"v0_6/empty-entity.osm");
	}

	/**
	 * Test the creation of a node.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void createNode() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base.osm",
				"v0_6/apply_change/change-create.osc",
				"v0_6/apply_change/apply-change-create.osm");
	}
	
	/**
	 * Test the modification of a node.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void modifyNode() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base.osm",
				"v0_6/apply_change/change-modify.osc",
				"v0_6/apply_change/apply-change-modify.osm");
	}
	
	/**
	 * Test the deletion of a node.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void deleteNode() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base.osm",
				"v0_6/apply_change/change-delete.osc",
				"v0_6/apply_change/apply-change-delete.osm");
	}
	
	/**
	 * Test the creation, modification and deletion of the same entity in a single stream.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public void createModifyDelete() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base.osm",
				"v0_6/apply_change/change-create-modify-delete.osc",
				"v0_6/apply_change/apply-change-base.osm");
	}

	/**
	 * Test the deletion of an entity that does not exist in the source stream.
	 *
	 * Deletion of a non-existent entity doesn't change anything.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void deleteNonExistent() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base.osm",
				"v0_6/apply_change/change-delete-nonexistent.osc",
				"v0_6/apply_change/apply-change-base.osm");
	}
	
	/**
	 * Test the modification of an entity that does not exist in the source stream.
	 * 
	 * Modification of a non-existent entity has the same effect as its creation.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void modifyNonExistent() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base.osm", 
				"v0_6/apply_change/change-modify-nonexistent.osc", 
				"v0_6/apply_change/apply-change-modify-nonexistent.osm");
	}
	
	/**
	 * Test the creation of an entity that already exists in the source stream.
	 * 
	 * Creation of an existent entity has the same effect as a modification.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void createExistent() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base.osm", 
				"v0_6/apply_change/change-create-existent.osc", 
				"v0_6/apply_change/apply-change-base.osm");
	}

	/**
	 * Test the case when the version in the change stream is lower than in the
	 * source stream.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void modifyHigherVersion() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base-high.osm", 
				"v0_6/apply_change/change-modify.osc", 
				"v0_6/apply_change/apply-change-modify-higher.osm");
	}
	
	/**
	 * Test the case when the change is longer than the source stream 
	 * and consists of creates.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void longChangeCreate() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base-node-only.osm", 
				"v0_6/apply_change/change-big-create.osc", 
				"v0_6/apply_change/apply-change-big.osm");
	}
	
	/**
	 * Test the case when the change is longer than the source 
	 * stream and consists of deletes.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void longChangeDelete() throws Exception {
		applyChange("v0_6/apply_change/apply-change-base-node-only.osm", 
				"v0_6/apply_change/change-big-delete.osc", 
				"v0_6/apply_change/apply-change-base-node-only.osm");
	}


	private void applyChange(String sourceFileName, String changeFileName, 
			String expectedOutputFileName) throws IOException {
		File sourceFile;
		File changeFile;
		File expectedOutputFile;
		File actualOutputFile;
		
		sourceFile = dataUtils.createDataFile(sourceFileName);
		changeFile = dataUtils.createDataFile(changeFileName);
		expectedOutputFile = dataUtils.createDataFile(expectedOutputFileName);
		actualOutputFile = dataUtils.newFile();

		Osmosis.run(
				new String [] {
					"-q",
					"--read-xml-change-0.6", changeFile.getPath(),
					"--read-xml-0.6", sourceFile.getPath(),
					"--apply-change-0.6",
					"--write-xml-0.6", actualOutputFile.getPath()
				}
			);

		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}
}
