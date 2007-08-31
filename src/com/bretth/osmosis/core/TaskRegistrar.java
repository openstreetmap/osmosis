package com.bretth.osmosis.core;

import com.bretth.osmosis.core.change.ChangeApplierFactory;
import com.bretth.osmosis.core.change.ChangeDeriverFactory;
import com.bretth.osmosis.core.buffer.ChangeBufferFactory;
import com.bretth.osmosis.core.buffer.EntityBufferFactory;
import com.bretth.osmosis.core.filter.BoundingBoxFilterFactory;
import com.bretth.osmosis.core.merge.ChangeMergerFactory;
import com.bretth.osmosis.core.merge.EntityMergerFactory;
import com.bretth.osmosis.core.misc.NullChangeWriterFactory;
import com.bretth.osmosis.core.misc.NullWriterFactory;
import com.bretth.osmosis.core.mysql.MysqlChangeReaderFactory;
import com.bretth.osmosis.core.mysql.MysqlChangeWriterFactory;
import com.bretth.osmosis.core.mysql.MysqlReaderFactory;
import com.bretth.osmosis.core.mysql.MysqlTruncatorFactory;
import com.bretth.osmosis.core.mysql.MysqlWriterFactory;
import com.bretth.osmosis.core.pipeline.TaskManagerFactory;
import com.bretth.osmosis.core.sort.ChangeForSeekableApplierComparator;
import com.bretth.osmosis.core.sort.ChangeForStreamableApplierComparator;
import com.bretth.osmosis.core.sort.ChangeSorterFactory;
import com.bretth.osmosis.core.sort.EntityByTypeThenIdComparator;
import com.bretth.osmosis.core.sort.EntitySorterFactory;
import com.bretth.osmosis.core.xml.XmlChangeReaderFactory;
import com.bretth.osmosis.core.xml.XmlChangeWriterFactory;
import com.bretth.osmosis.core.xml.XmlDownloaderFactory;
import com.bretth.osmosis.core.xml.XmlReaderFactory;
import com.bretth.osmosis.core.xml.XmlWriterFactory;


/**
 * Provides the initialisation logic for registering all task factories.
 * 
 * @author Brett Henderson
 */
public class TaskRegistrar {
	/**
	 * Initialises factories for all tasks.
	 */
	public static void initialize() {
		EntitySorterFactory entitySorterFactory;
		ChangeSorterFactory changeSorterFactory;
		
		// Configure factories that require additional information.
		entitySorterFactory = new EntitySorterFactory();
		entitySorterFactory.registerComparator("TypeThenId", new EntityByTypeThenIdComparator(), true);
		changeSorterFactory = new ChangeSorterFactory();
		changeSorterFactory.registerComparator("streamable", new ChangeForStreamableApplierComparator(), true);
		changeSorterFactory.registerComparator("seekable", new ChangeForSeekableApplierComparator(), false);
		
		// Register factories.
		TaskManagerFactory.register("apply-change", new ChangeApplierFactory());
		TaskManagerFactory.register("bounding-box", new BoundingBoxFilterFactory());
		TaskManagerFactory.register("derive-change", new ChangeDeriverFactory());
		TaskManagerFactory.register("read-mysql", new MysqlReaderFactory());
		TaskManagerFactory.register("read-mysql-change", new MysqlChangeReaderFactory());
		TaskManagerFactory.register("read-xml", new XmlReaderFactory());
		TaskManagerFactory.register("read-xml-change", new XmlChangeReaderFactory());
		TaskManagerFactory.register("sort", entitySorterFactory);
		TaskManagerFactory.register("sort-change", changeSorterFactory);
		TaskManagerFactory.register("write-mysql", new MysqlWriterFactory());
		TaskManagerFactory.register("write-mysql-change", new MysqlChangeWriterFactory());
		TaskManagerFactory.register("truncate-mysql", new MysqlTruncatorFactory());
		TaskManagerFactory.register("write-xml", new XmlWriterFactory());
		TaskManagerFactory.register("write-xml-change", new XmlChangeWriterFactory());
		TaskManagerFactory.register("write-null", new NullWriterFactory());
		TaskManagerFactory.register("write-null-change", new NullChangeWriterFactory());
		TaskManagerFactory.register("buffer", new EntityBufferFactory());
		TaskManagerFactory.register("buffer-change", new ChangeBufferFactory());
		TaskManagerFactory.register("merge", new EntityMergerFactory());
		TaskManagerFactory.register("merge-change", new ChangeMergerFactory());
		TaskManagerFactory.register("read-api", new XmlDownloaderFactory());
	}
}
