package com.bretth.osmosis.core;

import com.bretth.osmosis.core.buffer.v0_4.ChangeBufferFactory;
import com.bretth.osmosis.core.buffer.v0_4.EntityBufferFactory;
import com.bretth.osmosis.core.change.v0_4.ChangeApplierFactory;
import com.bretth.osmosis.core.change.v0_4.ChangeDeriverFactory;
import com.bretth.osmosis.core.filter.v0_4.BoundingBoxFilterFactory;
import com.bretth.osmosis.core.filter.v0_4.PolygonFilterFactory;
import com.bretth.osmosis.core.merge.v0_4.ChangeMergerFactory;
import com.bretth.osmosis.core.merge.v0_4.EntityMergerFactory;
import com.bretth.osmosis.core.misc.v0_4.NullChangeWriterFactory;
import com.bretth.osmosis.core.misc.v0_4.NullWriterFactory;
import com.bretth.osmosis.core.mysql.v0_4.MySqlCurrentReaderFactory;
import com.bretth.osmosis.core.mysql.v0_4.MysqlChangeReaderFactory;
import com.bretth.osmosis.core.mysql.v0_4.MysqlChangeWriterFactory;
import com.bretth.osmosis.core.mysql.v0_4.MysqlReaderFactory;
import com.bretth.osmosis.core.mysql.v0_4.MysqlTruncatorFactory;
import com.bretth.osmosis.core.mysql.v0_4.MysqlWriterFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.report.EntityReporterFactory;
import com.bretth.osmosis.core.sort.v0_4.ChangeForSeekableApplierComparator;
import com.bretth.osmosis.core.sort.v0_4.ChangeForStreamableApplierComparator;
import com.bretth.osmosis.core.sort.v0_4.ChangeSorterFactory;
import com.bretth.osmosis.core.sort.v0_4.EntityByTypeThenIdComparator;
import com.bretth.osmosis.core.sort.v0_4.EntitySorterFactory;
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
		TaskManagerFactory.register("read-mysql-current", new MySqlCurrentReaderFactory());
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
		TaskManagerFactory.register("bounding-polygon", new PolygonFilterFactory());
		TaskManagerFactory.register("report", new EntityReporterFactory());
		
		TaskManagerFactory.register("apply-change-0.4", new ChangeApplierFactory());
		TaskManagerFactory.register("bounding-box-0.4", new BoundingBoxFilterFactory());
		TaskManagerFactory.register("derive-change-0.4", new ChangeDeriverFactory());
		TaskManagerFactory.register("read-mysql-0.4", new MysqlReaderFactory());
		TaskManagerFactory.register("read-mysql-change-0.4", new MysqlChangeReaderFactory());
		TaskManagerFactory.register("read-mysql-current-0.4", new MySqlCurrentReaderFactory());
		TaskManagerFactory.register("read-xml-0.4", new XmlReaderFactory());
		TaskManagerFactory.register("read-xml-change-0.4", new XmlChangeReaderFactory());
		TaskManagerFactory.register("sort-0.4", entitySorterFactory);
		TaskManagerFactory.register("sort-change-0.4", changeSorterFactory);
		TaskManagerFactory.register("write-mysql-0.4", new MysqlWriterFactory());
		TaskManagerFactory.register("write-mysql-change-0.4", new MysqlChangeWriterFactory());
		TaskManagerFactory.register("truncate-mysql-0.4", new MysqlTruncatorFactory());
		TaskManagerFactory.register("write-xml-0.4", new XmlWriterFactory());
		TaskManagerFactory.register("write-xml-change-0.4", new XmlChangeWriterFactory());
		TaskManagerFactory.register("write-null-0.4", new NullWriterFactory());
		TaskManagerFactory.register("write-null-change-0.4", new NullChangeWriterFactory());
		TaskManagerFactory.register("buffer-0.4", new EntityBufferFactory());
		TaskManagerFactory.register("buffer-change-0.4", new ChangeBufferFactory());
		TaskManagerFactory.register("merge-0.4", new EntityMergerFactory());
		TaskManagerFactory.register("merge-change-0.4", new ChangeMergerFactory());
		TaskManagerFactory.register("read-api-0.4", new XmlDownloaderFactory());
		TaskManagerFactory.register("bounding-polygon-0.4", new PolygonFilterFactory());
		TaskManagerFactory.register("report-0.4", new EntityReporterFactory());
		
		TaskManagerFactory.register("apply-change-0.5", new com.bretth.osmosis.core.change.v0_5.ChangeApplierFactory());
		TaskManagerFactory.register("bounding-box-0.5", new com.bretth.osmosis.core.filter.v0_5.BoundingBoxFilterFactory());
		TaskManagerFactory.register("derive-change-0.5", new com.bretth.osmosis.core.change.v0_5.ChangeDeriverFactory());
		TaskManagerFactory.register("read-mysql-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlReaderFactory());
		TaskManagerFactory.register("read-mysql-change-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeReaderFactory());
		TaskManagerFactory.register("read-mysql-current-0.5", new com.bretth.osmosis.core.mysql.v0_5.MySqlCurrentReaderFactory());
		//TaskManagerFactory.register("read-xml-0.5", new XmlReaderFactory());
		//TaskManagerFactory.register("read-xml-change-0.5", new XmlChangeReaderFactory());
		//TaskManagerFactory.register("sort-0.5", entitySorterFactory);
		TaskManagerFactory.register("sort-change-0.5", changeSorterFactory);
		TaskManagerFactory.register("write-mysql-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlWriterFactory());
		TaskManagerFactory.register("write-mysql-change-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeWriterFactory());
		TaskManagerFactory.register("truncate-mysql-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlTruncatorFactory());
		//TaskManagerFactory.register("write-xml-0.5", new XmlWriterFactory());
		//TaskManagerFactory.register("write-xml-change-0.5", new XmlChangeWriterFactory());
		TaskManagerFactory.register("write-null-0.5", new com.bretth.osmosis.core.misc.v0_5.NullWriterFactory());
		TaskManagerFactory.register("write-null-change-0.5", new com.bretth.osmosis.core.misc.v0_5.NullChangeWriterFactory());
		TaskManagerFactory.register("buffer-0.5", new com.bretth.osmosis.core.buffer.v0_5.EntityBufferFactory());
		TaskManagerFactory.register("buffer-change-0.5", new com.bretth.osmosis.core.buffer.v0_5.ChangeBufferFactory());
		TaskManagerFactory.register("merge-0.5", new com.bretth.osmosis.core.merge.v0_5.EntityMergerFactory());
		TaskManagerFactory.register("merge-change-0.5", new com.bretth.osmosis.core.merge.v0_5.ChangeMergerFactory());
		//TaskManagerFactory.register("read-api-0.5", new XmlDownloaderFactory());
		TaskManagerFactory.register("bounding-polygon-0.5", new com.bretth.osmosis.core.filter.v0_5.PolygonFilterFactory());
		//TaskManagerFactory.register("report-0.5", new EntityReporterFactory());
	}
}
