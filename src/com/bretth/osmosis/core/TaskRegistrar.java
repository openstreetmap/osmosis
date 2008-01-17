// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core;

import com.bretth.osmosis.core.bdb.v0_5.BdbReaderFactory;
import com.bretth.osmosis.core.bdb.v0_5.BdbWriterFactory;
import com.bretth.osmosis.core.buffer.v0_5.ChangeBufferFactory;
import com.bretth.osmosis.core.buffer.v0_5.EntityBufferFactory;
import com.bretth.osmosis.core.change.v0_5.ChangeApplierFactory;
import com.bretth.osmosis.core.change.v0_5.ChangeDeriverFactory;
import com.bretth.osmosis.core.customdb.v0_5.DumpDatasetFactory;
import com.bretth.osmosis.core.customdb.v0_5.WriteDatasetFactory;
import com.bretth.osmosis.core.filter.v0_5.BoundingBoxFilterFactory;
import com.bretth.osmosis.core.filter.v0_5.DatasetBoundingBoxFilterFactory;
import com.bretth.osmosis.core.filter.v0_5.PolygonFilterFactory;
import com.bretth.osmosis.core.merge.v0_5.ChangeMergerFactory;
import com.bretth.osmosis.core.merge.v0_5.EntityMergerFactory;
import com.bretth.osmosis.core.misc.v0_5.NullChangeWriterFactory;
import com.bretth.osmosis.core.misc.v0_5.NullWriterFactory;
import com.bretth.osmosis.core.mysql.v0_5.MySqlCurrentReaderFactory;
import com.bretth.osmosis.core.mysql.v0_5.MysqlChangeReaderFactory;
import com.bretth.osmosis.core.mysql.v0_5.MysqlChangeWriterFactory;
import com.bretth.osmosis.core.mysql.v0_5.MysqlReaderFactory;
import com.bretth.osmosis.core.mysql.v0_5.MysqlTruncatorFactory;
import com.bretth.osmosis.core.mysql.v0_5.MysqlWriterFactory;
import com.bretth.osmosis.core.pgsql.common.v0_5.PostgreSqlDumpWriterFactory;
import com.bretth.osmosis.core.pgsql.common.v0_5.PostgreSqlTruncatorFactory;
import com.bretth.osmosis.core.pgsql.common.v0_5.PostgreSqlWriterFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.progress.v0_5.ChangeProgressLoggerFactory;
import com.bretth.osmosis.core.progress.v0_5.EntityProgressLoggerFactory;
import com.bretth.osmosis.core.report.v0_5.EntityReporterFactory;
import com.bretth.osmosis.core.report.v0_5.IntegrityReporterFactory;
import com.bretth.osmosis.core.sort.v0_5.ChangeForSeekableApplierComparator;
import com.bretth.osmosis.core.sort.v0_5.ChangeForStreamableApplierComparator;
import com.bretth.osmosis.core.sort.v0_5.ChangeSorterFactory;
import com.bretth.osmosis.core.sort.v0_5.EntityByTypeThenIdComparator;
import com.bretth.osmosis.core.sort.v0_5.EntitySorterFactory;
import com.bretth.osmosis.core.tee.v0_5.ChangeTeeFactory;
import com.bretth.osmosis.core.tee.v0_5.EntityTeeFactory;
import com.bretth.osmosis.core.xml.v0_5.XmlChangeReaderFactory;
import com.bretth.osmosis.core.xml.v0_5.XmlChangeWriterFactory;
import com.bretth.osmosis.core.xml.v0_5.XmlDownloaderFactory;
import com.bretth.osmosis.core.xml.v0_5.XmlReaderFactory;
import com.bretth.osmosis.core.xml.v0_5.XmlWriterFactory;


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
		EntitySorterFactory entitySorterFactory05;
		ChangeSorterFactory changeSorterFactory05;
		
		// Configure factories that require additional information.
		entitySorterFactory05 = new EntitySorterFactory();
		entitySorterFactory05.registerComparator("TypeThenId", new EntityByTypeThenIdComparator(), true);
		changeSorterFactory05 = new ChangeSorterFactory();
		changeSorterFactory05.registerComparator("streamable", new ChangeForStreamableApplierComparator(), true);
		changeSorterFactory05.registerComparator("seekable", new ChangeForSeekableApplierComparator(), false);
		
		// Register factories.
		TaskManagerFactory.register("apply-change", new ChangeApplierFactory());
		TaskManagerFactory.register("ac", new ChangeApplierFactory());
		TaskManagerFactory.register("bounding-box", new BoundingBoxFilterFactory());
		TaskManagerFactory.register("bb", new BoundingBoxFilterFactory());
		TaskManagerFactory.register("derive-change", new ChangeDeriverFactory());
		TaskManagerFactory.register("dc", new ChangeDeriverFactory());
		TaskManagerFactory.register("read-mysql", new MysqlReaderFactory());
		TaskManagerFactory.register("rm", new MysqlReaderFactory());
		TaskManagerFactory.register("read-mysql-change", new MysqlChangeReaderFactory());
		TaskManagerFactory.register("rmc", new MysqlChangeReaderFactory());
		TaskManagerFactory.register("read-mysql-current", new MySqlCurrentReaderFactory());
		TaskManagerFactory.register("rmcur", new MySqlCurrentReaderFactory());
		TaskManagerFactory.register("read-xml", new XmlReaderFactory());
		TaskManagerFactory.register("rx", new XmlReaderFactory());
		TaskManagerFactory.register("read-xml-change", new XmlChangeReaderFactory());
		TaskManagerFactory.register("rxc", new XmlChangeReaderFactory());
		TaskManagerFactory.register("sort", entitySorterFactory05);
		TaskManagerFactory.register("s", entitySorterFactory05);
		TaskManagerFactory.register("sort-change", changeSorterFactory05);
		TaskManagerFactory.register("sc", changeSorterFactory05);
		TaskManagerFactory.register("write-mysql", new MysqlWriterFactory());
		TaskManagerFactory.register("wm", new MysqlWriterFactory());
		TaskManagerFactory.register("write-mysql-change", new MysqlChangeWriterFactory());
		TaskManagerFactory.register("wmc", new MysqlChangeWriterFactory());
		TaskManagerFactory.register("truncate-mysql", new MysqlTruncatorFactory());
		TaskManagerFactory.register("tm", new MysqlTruncatorFactory());
		TaskManagerFactory.register("write-xml", new XmlWriterFactory());
		TaskManagerFactory.register("wx", new XmlWriterFactory());
		TaskManagerFactory.register("write-xml-change", new XmlChangeWriterFactory());
		TaskManagerFactory.register("wxc", new XmlChangeWriterFactory());
		TaskManagerFactory.register("write-null", new NullWriterFactory());
		TaskManagerFactory.register("wn", new NullWriterFactory());
		TaskManagerFactory.register("write-null-change", new NullChangeWriterFactory());
		TaskManagerFactory.register("wnc", new NullChangeWriterFactory());
		TaskManagerFactory.register("buffer", new EntityBufferFactory());
		TaskManagerFactory.register("b", new EntityBufferFactory());
		TaskManagerFactory.register("buffer-change", new ChangeBufferFactory());
		TaskManagerFactory.register("bc", new ChangeBufferFactory());
		TaskManagerFactory.register("merge", new EntityMergerFactory());
		TaskManagerFactory.register("m", new EntityMergerFactory());
		TaskManagerFactory.register("merge-change", new ChangeMergerFactory());
		TaskManagerFactory.register("mc", new ChangeMergerFactory());
		TaskManagerFactory.register("read-api", new XmlDownloaderFactory());
		TaskManagerFactory.register("wa", new XmlDownloaderFactory());
		TaskManagerFactory.register("bounding-polygon", new PolygonFilterFactory());
		TaskManagerFactory.register("bp", new PolygonFilterFactory());
		TaskManagerFactory.register("report-entity", new EntityReporterFactory());
		TaskManagerFactory.register("re", new EntityReporterFactory());
		TaskManagerFactory.register("report-integrity", new IntegrityReporterFactory());
		TaskManagerFactory.register("ri", new IntegrityReporterFactory());
		TaskManagerFactory.register("write-pgsql", new PostgreSqlWriterFactory());
		TaskManagerFactory.register("wp", new PostgreSqlWriterFactory());
		TaskManagerFactory.register("truncate-pgsql", new PostgreSqlTruncatorFactory());
		TaskManagerFactory.register("tp", new PostgreSqlTruncatorFactory());
		TaskManagerFactory.register("log-progress", new EntityProgressLoggerFactory());
		TaskManagerFactory.register("lp", new EntityProgressLoggerFactory());
		TaskManagerFactory.register("log-progress-change", new ChangeProgressLoggerFactory());
		TaskManagerFactory.register("lpc", new ChangeProgressLoggerFactory());
		TaskManagerFactory.register("tee", new EntityTeeFactory());
		TaskManagerFactory.register("t", new EntityTeeFactory());
		TaskManagerFactory.register("tee-change", new ChangeTeeFactory());
		TaskManagerFactory.register("tc", new ChangeTeeFactory());
		TaskManagerFactory.register("write-pgsql-dump", new PostgreSqlDumpWriterFactory());
		TaskManagerFactory.register("wpd", new PostgreSqlDumpWriterFactory());
		TaskManagerFactory.register("write-customdb", new WriteDatasetFactory());
		TaskManagerFactory.register("wc", new WriteDatasetFactory());
		TaskManagerFactory.register("dataset-bounding-box", new DatasetBoundingBoxFilterFactory());
		TaskManagerFactory.register("dbb", new DatasetBoundingBoxFilterFactory());
		TaskManagerFactory.register("dataset-dump", new DumpDatasetFactory());
		TaskManagerFactory.register("dd", new DumpDatasetFactory());
		TaskManagerFactory.register("write-bdb", new BdbWriterFactory());
		TaskManagerFactory.register("wb", new BdbWriterFactory());
		TaskManagerFactory.register("read-bdb", new BdbReaderFactory());
		TaskManagerFactory.register("rb", new BdbReaderFactory());
		
		TaskManagerFactory.register("apply-change-0.5", new ChangeApplierFactory());
		TaskManagerFactory.register("bounding-box-0.5", new BoundingBoxFilterFactory());
		TaskManagerFactory.register("derive-change-0.5", new ChangeDeriverFactory());
		TaskManagerFactory.register("read-mysql-0.5", new MysqlReaderFactory());
		TaskManagerFactory.register("read-mysql-change-0.5", new MysqlChangeReaderFactory());
		TaskManagerFactory.register("read-mysql-current-0.5", new MySqlCurrentReaderFactory());
		TaskManagerFactory.register("read-xml-0.5", new XmlReaderFactory());
		TaskManagerFactory.register("read-xml-change-0.5", new XmlChangeReaderFactory());
		TaskManagerFactory.register("sort-0.5", entitySorterFactory05);
		TaskManagerFactory.register("sort-change-0.5", changeSorterFactory05);
		TaskManagerFactory.register("write-mysql-0.5", new MysqlWriterFactory());
		TaskManagerFactory.register("write-mysql-change-0.5", new MysqlChangeWriterFactory());
		TaskManagerFactory.register("truncate-mysql-0.5", new MysqlTruncatorFactory());
		TaskManagerFactory.register("write-xml-0.5", new XmlWriterFactory());
		TaskManagerFactory.register("write-xml-change-0.5", new XmlChangeWriterFactory());
		TaskManagerFactory.register("write-null-0.5", new NullWriterFactory());
		TaskManagerFactory.register("write-null-change-0.5", new NullChangeWriterFactory());
		TaskManagerFactory.register("buffer-0.5", new EntityBufferFactory());
		TaskManagerFactory.register("buffer-change-0.5", new ChangeBufferFactory());
		TaskManagerFactory.register("merge-0.5", new EntityMergerFactory());
		TaskManagerFactory.register("merge-change-0.5", new ChangeMergerFactory());
		TaskManagerFactory.register("read-api-0.5", new XmlDownloaderFactory());
		TaskManagerFactory.register("bounding-polygon-0.5", new PolygonFilterFactory());
		TaskManagerFactory.register("report-entity-0.5", new EntityReporterFactory());
		TaskManagerFactory.register("report-integrity-0.5", new IntegrityReporterFactory());
		TaskManagerFactory.register("write-pgsql-0.5", new PostgreSqlWriterFactory());
		TaskManagerFactory.register("truncate-pgsql-0.5", new PostgreSqlTruncatorFactory());
		TaskManagerFactory.register("log-progress-0.5", new EntityProgressLoggerFactory());
		TaskManagerFactory.register("log-change-progress-0.5", new ChangeProgressLoggerFactory());
		TaskManagerFactory.register("tee-0.5", new EntityTeeFactory());
		TaskManagerFactory.register("tee-change-0.5", new ChangeTeeFactory());
		TaskManagerFactory.register("write-pgsql-dump-0.5", new PostgreSqlDumpWriterFactory());
		TaskManagerFactory.register("write-customdb-0.5", new WriteDatasetFactory());
		TaskManagerFactory.register("dataset-bounding-box-0.5", new DatasetBoundingBoxFilterFactory());
		TaskManagerFactory.register("dataset-dump-0.5", new DumpDatasetFactory());
		TaskManagerFactory.register("write-bdb-0.5", new BdbWriterFactory());
		TaskManagerFactory.register("read-bdb-0.5", new BdbReaderFactory());
	}
}
