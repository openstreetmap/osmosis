package com.bretth.osmosis;

import com.bretth.osmosis.change.ChangeApplierFactory;
import com.bretth.osmosis.change.ChangeDeriverFactory;
import com.bretth.osmosis.filter.BoundingBoxFilterFactory;
import com.bretth.osmosis.misc.NullChangeWriterFactory;
import com.bretth.osmosis.misc.NullWriterFactory;
import com.bretth.osmosis.mysql.MysqlChangeReaderFactory;
import com.bretth.osmosis.mysql.MysqlReaderFactory;
import com.bretth.osmosis.mysql.MysqlWriterFactory;
import com.bretth.osmosis.pipeline.TaskManagerFactory;
import com.bretth.osmosis.sort.ChangeForSeekableApplierComparator;
import com.bretth.osmosis.sort.ChangeForStreamableApplierComparator;
import com.bretth.osmosis.sort.ChangeSorterFactory;
import com.bretth.osmosis.sort.EntityByTypeThenIdComparator;
import com.bretth.osmosis.sort.EntitySorterFactory;
import com.bretth.osmosis.xml.XmlChangeReaderFactory;
import com.bretth.osmosis.xml.XmlChangeWriterFactory;
import com.bretth.osmosis.xml.XmlReaderFactory;
import com.bretth.osmosis.xml.XmlWriterFactory;


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
		TaskManagerFactory.register("write-xml", new XmlWriterFactory());
		TaskManagerFactory.register("write-xml-change", new XmlChangeWriterFactory());
		TaskManagerFactory.register("write-null", new NullWriterFactory());
		TaskManagerFactory.register("write-null-change", new NullChangeWriterFactory());
	}
}
