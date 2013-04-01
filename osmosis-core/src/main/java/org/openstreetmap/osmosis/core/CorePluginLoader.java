// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.bound.v0_6.BoundComputerFactory;
import org.openstreetmap.osmosis.core.bound.v0_6.BoundSetterFactory;
import org.openstreetmap.osmosis.core.buffer.v0_6.ChangeBufferFactory;
import org.openstreetmap.osmosis.core.buffer.v0_6.EntityBufferFactory;
import org.openstreetmap.osmosis.core.misc.v0_6.EmptyChangeReaderFactory;
import org.openstreetmap.osmosis.core.misc.v0_6.EmptyReaderFactory;
import org.openstreetmap.osmosis.core.misc.v0_6.NullChangeWriterFactory;
import org.openstreetmap.osmosis.core.misc.v0_6.NullWriterFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.core.progress.v0_6.ChangeProgressLoggerFactory;
import org.openstreetmap.osmosis.core.progress.v0_6.EntityProgressLoggerFactory;
import org.openstreetmap.osmosis.core.report.v0_6.EntityReporterFactory;
import org.openstreetmap.osmosis.core.report.v0_6.IntegrityReporterFactory;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeForSeekableApplierComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeForStreamableApplierComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeSorterFactory;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeTagSorterFactory;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityContainerComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.EntitySorterFactory;
import org.openstreetmap.osmosis.core.sort.v0_6.TagSorterFactory;
import org.openstreetmap.osmosis.core.tee.v0_6.ChangeTeeFactory;
import org.openstreetmap.osmosis.core.tee.v0_6.EntityTeeFactory;


/**
 * The plugin loader for the core tasks.
 * 
 * @author Brett Henderson
 */
public class CorePluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		EntitySorterFactory entitySorterFactory06;
		ChangeSorterFactory changeSorterFactory06;

		factoryMap = new HashMap<String, TaskManagerFactory>();

		// Configure factories that require additional information.
		entitySorterFactory06 = new EntitySorterFactory();
		entitySorterFactory06.registerComparator("TypeThenId", new EntityContainerComparator(
				new EntityByTypeThenIdComparator()), true);
		changeSorterFactory06 = new ChangeSorterFactory();
		changeSorterFactory06.registerComparator("streamable", new ChangeForStreamableApplierComparator(), true);
		changeSorterFactory06.registerComparator("seekable", new ChangeForSeekableApplierComparator(), false);

		// Register factories.
		factoryMap.put("sort", entitySorterFactory06);
		factoryMap.put("s", entitySorterFactory06);
		factoryMap.put("sort-change", changeSorterFactory06);
		factoryMap.put("sc", changeSorterFactory06);
		factoryMap.put("write-null", new NullWriterFactory());
		factoryMap.put("wn", new NullWriterFactory());
		factoryMap.put("write-null-change", new NullChangeWriterFactory());
		factoryMap.put("wnc", new NullChangeWriterFactory());
		factoryMap.put("buffer", new EntityBufferFactory());
		factoryMap.put("b", new EntityBufferFactory());
		factoryMap.put("buffer-change", new ChangeBufferFactory());
		factoryMap.put("bc", new ChangeBufferFactory());
		factoryMap.put("report-entity", new EntityReporterFactory());
		factoryMap.put("re", new EntityReporterFactory());
		factoryMap.put("report-integrity", new IntegrityReporterFactory());
		factoryMap.put("ri", new IntegrityReporterFactory());
		factoryMap.put("log-progress", new EntityProgressLoggerFactory());
		factoryMap.put("lp", new EntityProgressLoggerFactory());
		factoryMap.put("log-progress-change", new ChangeProgressLoggerFactory());
		factoryMap.put("lpc", new ChangeProgressLoggerFactory());
		factoryMap.put("tee", new EntityTeeFactory());
		factoryMap.put("t", new EntityTeeFactory());
		factoryMap.put("tee-change", new ChangeTeeFactory());
		factoryMap.put("tc", new ChangeTeeFactory());
		factoryMap.put("read-empty", new EmptyReaderFactory());
		factoryMap.put("rem", new EmptyReaderFactory());
		factoryMap.put("read-empty-change", new EmptyChangeReaderFactory());
		factoryMap.put("remc", new EmptyChangeReaderFactory());

		factoryMap.put("compute-bounding-box", new BoundComputerFactory());
		factoryMap.put("cbb", new BoundComputerFactory());
		factoryMap.put("set-bounding-box", new BoundSetterFactory());
		factoryMap.put("sbb", new BoundSetterFactory());

		factoryMap.put("sort-0.6", entitySorterFactory06);
		factoryMap.put("sort-change-0.6", changeSorterFactory06);
		factoryMap.put("write-null-0.6", new NullWriterFactory());
		factoryMap.put("write-null-change-0.6", new NullChangeWriterFactory());
		factoryMap.put("buffer-0.6", new EntityBufferFactory());
		factoryMap.put("buffer-change-0.6", new ChangeBufferFactory());
		factoryMap.put("report-entity-0.6", new EntityReporterFactory());
		factoryMap.put("report-integrity-0.6", new IntegrityReporterFactory());
		factoryMap.put("log-progress-0.6", new EntityProgressLoggerFactory());
		factoryMap.put("log-progress-change-0.6", new ChangeProgressLoggerFactory());
		factoryMap.put("tee-0.6", new EntityTeeFactory());
		factoryMap.put("tee-change-0.6", new ChangeTeeFactory());
		factoryMap.put("read-empty-0.6", new EmptyReaderFactory());
		factoryMap.put("read-empty-change-0.6", new EmptyChangeReaderFactory());
		factoryMap.put("tag-sort-0.6", new TagSorterFactory());
		factoryMap.put("tag-sort-change-0.6", new ChangeTagSorterFactory());

		factoryMap.put("compute-bounding-box-0.6", new BoundComputerFactory());
		factoryMap.put("set-bounding-box-0.6", new BoundSetterFactory());
		
		return factoryMap;
	}
}
