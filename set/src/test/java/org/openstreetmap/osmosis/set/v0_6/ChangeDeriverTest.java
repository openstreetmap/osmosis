// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.misc.v0_6.EmptyReader;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;
import org.openstreetmap.osmosis.testutil.v0_6.RunTaskUtilities;
import org.openstreetmap.osmosis.testutil.v0_6.SinkChangeInspector;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

/**
 * Tests for --derive-change task.
 * 
 * @author Igor Podolskiy
 */
public class ChangeDeriverTest extends AbstractDataTest {
	
	/**
	 * Empty inputs should yield empty change.
	 * 
	 * @throws Exception if something goes wrong.
	 */
	@Test
	public void emptyInputs() throws Exception {
		deriveChange("v0_6/empty-entity.osm", 
				"v0_6/empty-entity.osm", 
				"v0_6/empty-change.osc");
	}
	
	/**
	 * Same inputs should yield empty change.
	 * 
	 * @throws Exception if something goes wrong.
	 */
	@Test
	public void sameInputs() throws Exception {
		deriveChange("v0_6/derive_change/simple.osm", 
				"v0_6/derive_change/simple.osm", 
				"v0_6/empty-change.osc");
	}
	
	/**
	 * Deriving change with an empty left input should yield
	 * a change with deletes only.
	 * 
	 * @throws Exception if something goes wrong.
	 */
	@Test
	public void leftEmpty() throws Exception {
		deriveChange("v0_6/empty-entity.osm", 
				"v0_6/derive_change/simple.osm", 
				"v0_6/derive_change/full-create.osc");
	}
	
	/**
	 * Deriving change with an empty right input should yield
	 * a change with creates only.
	 * 
	 * @throws Exception if something goes wrong.
	 */
	@Test
	public void rightEmpty() throws Exception {
		// Cannot be tested with file comparison as the derived 
		// change contains deletes which have a current timestamp 
		// that cannot be reliably predicted.
		// Therefore, check all relevant attributes manually.
		
		ChangeDeriver deriver = new ChangeDeriver(1);
		RunnableSource left = new XmlReader(dataUtils.createDataFile(
				"v0_6/derive_change/simple.osm"), true, CompressionMethod.None);
		RunnableSource right = new EmptyReader();
		
		SinkChangeInspector result = RunTaskUtilities.run(deriver, left, right);
		List<ChangeContainer> changes = result.getProcessedChanges();
		
		Assert.assertEquals(3, changes.size());
		for (ChangeContainer changeContainer : changes) {
			Assert.assertEquals(ChangeAction.Delete, changeContainer.getAction());
		}
		
		Entity e;
		e = changes.get(0).getEntityContainer().getEntity();
		Assert.assertEquals(EntityType.Node, e.getType());
		Assert.assertEquals(10, e.getId());
		Assert.assertEquals(34, e.getVersion());
		
		e = changes.get(1).getEntityContainer().getEntity();
		Assert.assertEquals(EntityType.Way, e.getType());
		Assert.assertEquals(100, e.getId());
		Assert.assertEquals(56, e.getVersion());

		e = changes.get(2).getEntityContainer().getEntity();
		Assert.assertEquals(EntityType.Relation, e.getType());
		Assert.assertEquals(1000, e.getId());
		Assert.assertEquals(78, e.getVersion());
	}

	
	private void deriveChange(String leftFileName, String rightFileName, 
			String expectedOutputFileName) throws IOException {
		File leftFile;
		File rightFile;
		File expectedOutputFile;
		File actualOutputFile;
		
		leftFile = dataUtils.createDataFile(leftFileName);
		rightFile = dataUtils.createDataFile(rightFileName);
		expectedOutputFile = dataUtils.createDataFile(expectedOutputFileName);
		actualOutputFile = dataUtils.newFile();

		Osmosis.run(
				new String [] {
					"-q",
					"--read-xml-0.6", rightFile.getPath(),
					"--read-xml-0.6", leftFile.getPath(),
					"--derive-change-0.6",
					"--write-xml-change-0.6", actualOutputFile.getPath()
				}
			);

		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}
}
