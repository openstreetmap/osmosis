// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bretth.osmosis.core.buffer.v0_6.ChangeBufferFactory;
import com.bretth.osmosis.core.buffer.v0_6.EntityBufferFactory;
import com.bretth.osmosis.core.change.v0_6.ChangeApplierFactory;
import com.bretth.osmosis.core.change.v0_6.ChangeDeriverFactory;
import com.bretth.osmosis.core.customdb.v0_6.DumpDatasetFactory;
import com.bretth.osmosis.core.customdb.v0_6.ReadDatasetFactory;
import com.bretth.osmosis.core.customdb.v0_6.WriteDatasetFactory;
import com.bretth.osmosis.core.filter.v0_6.BoundingBoxFilterFactory;
import com.bretth.osmosis.core.filter.v0_6.DatasetBoundingBoxFilterFactory;
import com.bretth.osmosis.core.filter.v0_6.NodeKeyFilterFactory;
import com.bretth.osmosis.core.filter.v0_6.NodeKeyValueFilterFactory;
import com.bretth.osmosis.core.filter.v0_6.PolygonFilterFactory;
import com.bretth.osmosis.core.filter.v0_6.UsedNodeFilterFactory;
import com.bretth.osmosis.core.filter.v0_6.WayKeyValueFilterFactory;
import com.bretth.osmosis.core.merge.v0_6.ChangeDownloadInitializerFactory;
import com.bretth.osmosis.core.merge.v0_6.ChangeDownloaderFactory;
import com.bretth.osmosis.core.merge.v0_6.ChangeMergerFactory;
import com.bretth.osmosis.core.merge.v0_6.EntityMergerFactory;
import com.bretth.osmosis.core.migrate.MigrateV05ToV06Factory;
import com.bretth.osmosis.core.misc.v0_6.NullChangeWriterFactory;
import com.bretth.osmosis.core.misc.v0_6.NullWriterFactory;
import com.bretth.osmosis.core.mysql.v0_6.MySqlCurrentReaderFactory;
import com.bretth.osmosis.core.mysql.v0_6.MysqlChangeReaderFactory;
import com.bretth.osmosis.core.mysql.v0_6.MysqlChangeWriterFactory;
import com.bretth.osmosis.core.mysql.v0_6.MysqlReaderFactory;
import com.bretth.osmosis.core.mysql.v0_6.MysqlTruncatorFactory;
import com.bretth.osmosis.core.mysql.v0_6.MysqlWriterFactory;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlChangeWriterFactory;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlDatasetDumpWriterFactory;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlDatasetReaderFactory;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlDatasetTruncatorFactory;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlDatasetWriterFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactoryRegister;
import com.bretth.osmosis.core.plugin.PluginLoader;
import com.bretth.osmosis.core.progress.v0_6.ChangeProgressLoggerFactory;
import com.bretth.osmosis.core.progress.v0_6.EntityProgressLoggerFactory;
import com.bretth.osmosis.core.report.v0_6.EntityReporterFactory;
import com.bretth.osmosis.core.report.v0_6.IntegrityReporterFactory;
import com.bretth.osmosis.core.sort.v0_6.ChangeForSeekableApplierComparator;
import com.bretth.osmosis.core.sort.v0_6.ChangeForStreamableApplierComparator;
import com.bretth.osmosis.core.sort.v0_6.ChangeSorterFactory;
import com.bretth.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import com.bretth.osmosis.core.sort.v0_6.EntitySorterFactory;
import com.bretth.osmosis.core.sort.v0_6.TagSorterFactory;
import com.bretth.osmosis.core.tee.v0_6.ChangeTeeFactory;
import com.bretth.osmosis.core.tee.v0_6.EntityTeeFactory;
import com.bretth.osmosis.core.xml.v0_6.XmlChangeReaderFactory;
import com.bretth.osmosis.core.xml.v0_6.XmlChangeWriterFactory;
import com.bretth.osmosis.core.xml.v0_6.XmlDownloaderFactory;
import com.bretth.osmosis.core.xml.v0_6.XmlReaderFactory;
import com.bretth.osmosis.core.xml.v0_6.XmlWriterFactory;


/**
 * Provides the initialisation logic for registering all task factories.
 * 
 * @author Brett Henderson
 */
public class TaskRegistrar {
	

	 /**
	  * The register containing all known task manager factories.
	  */
	 private TaskManagerFactoryRegister factoryRegister;
	
	
	/**
	 * Creates a new instance.
	 */
	public TaskRegistrar() {
		factoryRegister = new TaskManagerFactoryRegister();
	}
	
	
	/**
	 * Returns the configured task manager factory register configured.
	 * 
	 * @return The task manager factory register.
	 */
	public TaskManagerFactoryRegister getFactoryRegister() {
		return factoryRegister;
	}
	
	
	/**
	 * Initialises factories for all tasks. No plugins are loaded by this
	 * method.
	 */
	public void initialize() {
		initialize(new ArrayList<String>());
	}
	
	
	/**
	 * Initialises factories for all tasks. Loads additionally specified plugins
	 * as well as default tasks.
	 * 
	 * @param plugins
	 *            The class names of all plugins to be loaded.
	 */
	public void initialize(List<String> plugins) {
		com.bretth.osmosis.core.sort.v0_5.EntitySorterFactory entitySorterFactory05;
		com.bretth.osmosis.core.sort.v0_5.ChangeSorterFactory changeSorterFactory05;
		EntitySorterFactory entitySorterFactory06;
		ChangeSorterFactory changeSorterFactory06;
		
		// Configure factories that require additional information.
		entitySorterFactory05 = new com.bretth.osmosis.core.sort.v0_5.EntitySorterFactory();
		entitySorterFactory05.registerComparator("TypeThenId", new com.bretth.osmosis.core.sort.v0_5.EntityByTypeThenIdComparator(), true);
		changeSorterFactory05 = new com.bretth.osmosis.core.sort.v0_5.ChangeSorterFactory();
		changeSorterFactory05.registerComparator("streamable", new com.bretth.osmosis.core.sort.v0_5.ChangeForStreamableApplierComparator(), true);
		changeSorterFactory05.registerComparator("seekable", new com.bretth.osmosis.core.sort.v0_5.ChangeForSeekableApplierComparator(), false);
		entitySorterFactory06 = new EntitySorterFactory();
		entitySorterFactory06.registerComparator("TypeThenId", new EntityByTypeThenIdComparator(), true);
		changeSorterFactory06 = new ChangeSorterFactory();
		changeSorterFactory06.registerComparator("streamable", new ChangeForStreamableApplierComparator(), true);
		changeSorterFactory06.registerComparator("seekable", new ChangeForSeekableApplierComparator(), false);
		
		// Register factories.
		factoryRegister.register("apply-change", new com.bretth.osmosis.core.change.v0_5.ChangeApplierFactory());
		factoryRegister.register("ac", new com.bretth.osmosis.core.change.v0_5.ChangeApplierFactory());
		factoryRegister.register("bounding-box", new com.bretth.osmosis.core.filter.v0_5.BoundingBoxFilterFactory());
		factoryRegister.register("bb", new com.bretth.osmosis.core.filter.v0_5.BoundingBoxFilterFactory());
		factoryRegister.register("derive-change", new com.bretth.osmosis.core.change.v0_5.ChangeDeriverFactory());
		factoryRegister.register("dc", new com.bretth.osmosis.core.change.v0_5.ChangeDeriverFactory());
		factoryRegister.register("read-mysql", new com.bretth.osmosis.core.mysql.v0_5.MysqlReaderFactory());
		factoryRegister.register("rm", new com.bretth.osmosis.core.mysql.v0_5.MysqlReaderFactory());
		factoryRegister.register("read-mysql-change", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeReaderFactory());
		factoryRegister.register("rmc", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeReaderFactory());
		factoryRegister.register("read-mysql-current", new com.bretth.osmosis.core.mysql.v0_5.MySqlCurrentReaderFactory());
		factoryRegister.register("rmcur", new com.bretth.osmosis.core.mysql.v0_5.MySqlCurrentReaderFactory());
		factoryRegister.register("read-xml", new com.bretth.osmosis.core.xml.v0_5.XmlReaderFactory());
		factoryRegister.register("rx", new com.bretth.osmosis.core.xml.v0_5.XmlReaderFactory());
		factoryRegister.register("read-xml-change", new com.bretth.osmosis.core.xml.v0_5.XmlChangeReaderFactory());
		factoryRegister.register("rxc", new com.bretth.osmosis.core.xml.v0_5.XmlChangeReaderFactory());
		factoryRegister.register("sort", entitySorterFactory05);
		factoryRegister.register("s", entitySorterFactory05);
		factoryRegister.register("sort-change", changeSorterFactory05);
		factoryRegister.register("sc", changeSorterFactory05);
		factoryRegister.register("write-mysql", new com.bretth.osmosis.core.mysql.v0_5.MysqlWriterFactory());
		factoryRegister.register("wm", new com.bretth.osmosis.core.mysql.v0_5.MysqlWriterFactory());
		factoryRegister.register("write-mysql-change", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeWriterFactory());
		factoryRegister.register("wmc", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeWriterFactory());
		factoryRegister.register("truncate-mysql", new com.bretth.osmosis.core.mysql.v0_5.MysqlTruncatorFactory());
		factoryRegister.register("tm", new com.bretth.osmosis.core.mysql.v0_5.MysqlTruncatorFactory());
		factoryRegister.register("write-xml", new com.bretth.osmosis.core.xml.v0_5.XmlWriterFactory());
		factoryRegister.register("wx", new com.bretth.osmosis.core.xml.v0_5.XmlWriterFactory());
		factoryRegister.register("write-xml-change", new com.bretth.osmosis.core.xml.v0_5.XmlChangeWriterFactory());
		factoryRegister.register("wxc", new com.bretth.osmosis.core.xml.v0_5.XmlChangeWriterFactory());
		factoryRegister.register("write-null", new com.bretth.osmosis.core.misc.v0_5.NullWriterFactory());
		factoryRegister.register("wn", new com.bretth.osmosis.core.misc.v0_5.NullWriterFactory());
		factoryRegister.register("write-null-change", new com.bretth.osmosis.core.misc.v0_5.NullChangeWriterFactory());
		factoryRegister.register("wnc", new com.bretth.osmosis.core.misc.v0_5.NullChangeWriterFactory());
		factoryRegister.register("buffer", new com.bretth.osmosis.core.buffer.v0_5.EntityBufferFactory());
		factoryRegister.register("b", new com.bretth.osmosis.core.buffer.v0_5.EntityBufferFactory());
		factoryRegister.register("buffer-change", new com.bretth.osmosis.core.buffer.v0_5.ChangeBufferFactory());
		factoryRegister.register("bc", new com.bretth.osmosis.core.buffer.v0_5.ChangeBufferFactory());
		factoryRegister.register("merge", new com.bretth.osmosis.core.merge.v0_5.EntityMergerFactory());
		factoryRegister.register("m", new com.bretth.osmosis.core.merge.v0_5.EntityMergerFactory());
		factoryRegister.register("merge-change", new com.bretth.osmosis.core.merge.v0_5.ChangeMergerFactory());
		factoryRegister.register("mc", new com.bretth.osmosis.core.merge.v0_5.ChangeMergerFactory());
		factoryRegister.register("read-api", new com.bretth.osmosis.core.xml.v0_5.XmlDownloaderFactory());
		factoryRegister.register("ra", new com.bretth.osmosis.core.xml.v0_5.XmlDownloaderFactory());
		factoryRegister.register("bounding-polygon", new com.bretth.osmosis.core.filter.v0_5.PolygonFilterFactory());
		factoryRegister.register("bp", new com.bretth.osmosis.core.filter.v0_5.PolygonFilterFactory());
		factoryRegister.register("report-entity", new com.bretth.osmosis.core.report.v0_5.EntityReporterFactory());
		factoryRegister.register("re", new com.bretth.osmosis.core.report.v0_5.EntityReporterFactory());
		factoryRegister.register("report-integrity", new com.bretth.osmosis.core.report.v0_5.IntegrityReporterFactory());
		factoryRegister.register("ri", new com.bretth.osmosis.core.report.v0_5.IntegrityReporterFactory());
		factoryRegister.register("log-progress", new com.bretth.osmosis.core.progress.v0_5.EntityProgressLoggerFactory());
		factoryRegister.register("lp", new com.bretth.osmosis.core.progress.v0_5.EntityProgressLoggerFactory());
		factoryRegister.register("log-progress-change", new com.bretth.osmosis.core.progress.v0_5.ChangeProgressLoggerFactory());
		factoryRegister.register("lpc", new com.bretth.osmosis.core.progress.v0_5.ChangeProgressLoggerFactory());
		factoryRegister.register("tee", new com.bretth.osmosis.core.tee.v0_5.EntityTeeFactory());
		factoryRegister.register("t", new com.bretth.osmosis.core.tee.v0_5.EntityTeeFactory());
		factoryRegister.register("tee-change", new com.bretth.osmosis.core.tee.v0_5.ChangeTeeFactory());
		factoryRegister.register("tc", new com.bretth.osmosis.core.tee.v0_5.ChangeTeeFactory());
		factoryRegister.register("write-customdb", new com.bretth.osmosis.core.customdb.v0_5.WriteDatasetFactory());
		factoryRegister.register("wc", new com.bretth.osmosis.core.customdb.v0_5.WriteDatasetFactory());
		factoryRegister.register("dataset-bounding-box", new com.bretth.osmosis.core.filter.v0_5.DatasetBoundingBoxFilterFactory());
		factoryRegister.register("dbb", new com.bretth.osmosis.core.filter.v0_5.DatasetBoundingBoxFilterFactory());
		factoryRegister.register("dataset-dump", new com.bretth.osmosis.core.customdb.v0_5.DumpDatasetFactory());
		factoryRegister.register("dd", new com.bretth.osmosis.core.customdb.v0_5.DumpDatasetFactory());
		factoryRegister.register("read-customdb", new com.bretth.osmosis.core.customdb.v0_5.ReadDatasetFactory());
		factoryRegister.register("rc", new com.bretth.osmosis.core.customdb.v0_5.ReadDatasetFactory());
		factoryRegister.register("write-pgsql", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetWriterFactory());
		factoryRegister.register("wp", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetWriterFactory());
		factoryRegister.register("truncate-pgsql", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetTruncatorFactory());
		factoryRegister.register("tp", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetTruncatorFactory());
		factoryRegister.register("write-pgsql-dump", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetDumpWriterFactory());
		factoryRegister.register("wpd", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetDumpWriterFactory());
		factoryRegister.register("read-pgsql", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetReaderFactory());
		factoryRegister.register("rp", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetReaderFactory());
		factoryRegister.register("write-pgsql-change", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlChangeWriterFactory());
		factoryRegister.register("wpc", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlChangeWriterFactory());
		factoryRegister.register("used-node", new com.bretth.osmosis.core.filter.v0_5.UsedNodeFilterFactory());
		factoryRegister.register("un", new com.bretth.osmosis.core.filter.v0_5.UsedNodeFilterFactory());
		factoryRegister.register("node-key", new com.bretth.osmosis.core.filter.v0_5.NodeKeyFilterFactory());
		factoryRegister.register("nk", new com.bretth.osmosis.core.filter.v0_5.NodeKeyFilterFactory());
		factoryRegister.register("node-key-value", new com.bretth.osmosis.core.filter.v0_5.NodeKeyValueFilterFactory());
		factoryRegister.register("nkv", new com.bretth.osmosis.core.filter.v0_5.NodeKeyValueFilterFactory());
		factoryRegister.register("way-key-value", new com.bretth.osmosis.core.filter.v0_5.WayKeyValueFilterFactory());
		factoryRegister.register("wkv", new com.bretth.osmosis.core.filter.v0_5.WayKeyValueFilterFactory());
		factoryRegister.register("read-change-interval", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloaderFactory());
		factoryRegister.register("rci", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloaderFactory());
		factoryRegister.register("read-change-interval-init", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloadInitializerFactory());
		factoryRegister.register("rcii", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloadInitializerFactory());
		factoryRegister.register("migrate", new MigrateV05ToV06Factory());
		factoryRegister.register("mig", new MigrateV05ToV06Factory());
		
		factoryRegister.register("apply-change-0.5", new com.bretth.osmosis.core.change.v0_5.ChangeApplierFactory());
		factoryRegister.register("bounding-box-0.5", new com.bretth.osmosis.core.filter.v0_5.BoundingBoxFilterFactory());
		factoryRegister.register("derive-change-0.5", new com.bretth.osmosis.core.change.v0_5.ChangeDeriverFactory());
		factoryRegister.register("read-mysql-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlReaderFactory());
		factoryRegister.register("read-mysql-change-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeReaderFactory());
		factoryRegister.register("read-mysql-current-0.5", new com.bretth.osmosis.core.mysql.v0_5.MySqlCurrentReaderFactory());
		factoryRegister.register("read-xml-0.5", new com.bretth.osmosis.core.xml.v0_5.XmlReaderFactory());
		factoryRegister.register("read-xml-change-0.5", new com.bretth.osmosis.core.xml.v0_5.XmlChangeReaderFactory());
		factoryRegister.register("sort-0.5", entitySorterFactory05);
		factoryRegister.register("sort-change-0.5", changeSorterFactory05);
		factoryRegister.register("write-mysql-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlWriterFactory());
		factoryRegister.register("write-mysql-change-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeWriterFactory());
		factoryRegister.register("truncate-mysql-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlTruncatorFactory());
		factoryRegister.register("write-xml-0.5", new com.bretth.osmosis.core.xml.v0_5.XmlWriterFactory());
		factoryRegister.register("write-xml-change-0.5", new com.bretth.osmosis.core.xml.v0_5.XmlChangeWriterFactory());
		factoryRegister.register("write-null-0.5", new com.bretth.osmosis.core.misc.v0_5.NullWriterFactory());
		factoryRegister.register("write-null-change-0.5", new com.bretth.osmosis.core.misc.v0_5.NullChangeWriterFactory());
		factoryRegister.register("buffer-0.5", new com.bretth.osmosis.core.buffer.v0_5.EntityBufferFactory());
		factoryRegister.register("buffer-change-0.5", new com.bretth.osmosis.core.buffer.v0_5.ChangeBufferFactory());
		factoryRegister.register("merge-0.5", new com.bretth.osmosis.core.merge.v0_5.EntityMergerFactory());
		factoryRegister.register("merge-change-0.5", new com.bretth.osmosis.core.merge.v0_5.ChangeMergerFactory());
		factoryRegister.register("read-api-0.5", new com.bretth.osmosis.core.xml.v0_5.XmlDownloaderFactory());
		factoryRegister.register("bounding-polygon-0.5", new com.bretth.osmosis.core.filter.v0_5.PolygonFilterFactory());
		factoryRegister.register("report-entity-0.5", new com.bretth.osmosis.core.report.v0_5.EntityReporterFactory());
		factoryRegister.register("report-integrity-0.5", new com.bretth.osmosis.core.report.v0_5.IntegrityReporterFactory());
		factoryRegister.register("log-progress-0.5", new com.bretth.osmosis.core.progress.v0_5.EntityProgressLoggerFactory());
		factoryRegister.register("log-change-progress-0.5", new com.bretth.osmosis.core.progress.v0_5.ChangeProgressLoggerFactory());
		factoryRegister.register("tee-0.5", new com.bretth.osmosis.core.tee.v0_5.EntityTeeFactory());
		factoryRegister.register("tee-change-0.5", new com.bretth.osmosis.core.tee.v0_5.ChangeTeeFactory());
		factoryRegister.register("write-customdb-0.5", new com.bretth.osmosis.core.customdb.v0_5.WriteDatasetFactory());
		factoryRegister.register("dataset-bounding-box-0.5", new com.bretth.osmosis.core.filter.v0_5.DatasetBoundingBoxFilterFactory());
		factoryRegister.register("dataset-dump-0.5", new com.bretth.osmosis.core.customdb.v0_5.DumpDatasetFactory());
		factoryRegister.register("read-customdb-0.5", new com.bretth.osmosis.core.customdb.v0_5.ReadDatasetFactory());
		factoryRegister.register("write-pgsql-0.5", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetWriterFactory());
		factoryRegister.register("truncate-pgsql-0.5", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetTruncatorFactory());
		factoryRegister.register("write-pgsql-dump-0.5", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetDumpWriterFactory());
		factoryRegister.register("read-pgsql-0.5", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetReaderFactory());
		factoryRegister.register("write-pgsql-change-0.5", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlChangeWriterFactory());
		factoryRegister.register("used-node-0.5", new com.bretth.osmosis.core.filter.v0_5.UsedNodeFilterFactory());
		factoryRegister.register("node-key-0.5", new com.bretth.osmosis.core.filter.v0_5.NodeKeyFilterFactory());
		factoryRegister.register("node-key-value-0.5", new com.bretth.osmosis.core.filter.v0_5.NodeKeyValueFilterFactory());
		factoryRegister.register("way-key-value-0.5", new com.bretth.osmosis.core.filter.v0_5.WayKeyValueFilterFactory());
		factoryRegister.register("read-change-interval-0.5", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloaderFactory());
		factoryRegister.register("read-change-interval-init-0.5", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloadInitializerFactory());

		factoryRegister.register("apply-change-0.6", new ChangeApplierFactory());
		factoryRegister.register("bounding-box-0.6", new BoundingBoxFilterFactory());
		factoryRegister.register("derive-change-0.6", new ChangeDeriverFactory());
		factoryRegister.register("read-mysql-0.6", new MysqlReaderFactory());
		factoryRegister.register("read-mysql-change-0.6", new MysqlChangeReaderFactory());
		factoryRegister.register("read-mysql-current-0.6", new MySqlCurrentReaderFactory());
		factoryRegister.register("read-xml-0.6", new XmlReaderFactory());
		factoryRegister.register("read-xml-change-0.6", new XmlChangeReaderFactory());
		factoryRegister.register("sort-0.6", entitySorterFactory06);
		factoryRegister.register("sort-change-0.6", changeSorterFactory06);
		factoryRegister.register("write-mysql-0.6", new MysqlWriterFactory());
		factoryRegister.register("write-mysql-change-0.6", new MysqlChangeWriterFactory());
		factoryRegister.register("truncate-mysql-0.6", new MysqlTruncatorFactory());
		factoryRegister.register("write-xml-0.6", new XmlWriterFactory());
		factoryRegister.register("write-xml-change-0.6", new XmlChangeWriterFactory());
		factoryRegister.register("write-null-0.6", new NullWriterFactory());
		factoryRegister.register("write-null-change-0.6", new NullChangeWriterFactory());
		factoryRegister.register("buffer-0.6", new EntityBufferFactory());
		factoryRegister.register("buffer-change-0.6", new ChangeBufferFactory());
		factoryRegister.register("merge-0.6", new EntityMergerFactory());
		factoryRegister.register("merge-change-0.6", new ChangeMergerFactory());
		factoryRegister.register("read-api-0.6", new XmlDownloaderFactory());
		factoryRegister.register("bounding-polygon-0.6", new PolygonFilterFactory());
		factoryRegister.register("report-entity-0.6", new EntityReporterFactory());
		factoryRegister.register("report-integrity-0.6", new IntegrityReporterFactory());
		factoryRegister.register("log-progress-0.6", new EntityProgressLoggerFactory());
		factoryRegister.register("log-change-progress-0.6", new ChangeProgressLoggerFactory());
		factoryRegister.register("tee-0.6", new EntityTeeFactory());
		factoryRegister.register("tee-change-0.6", new ChangeTeeFactory());
		factoryRegister.register("write-customdb-0.6", new WriteDatasetFactory());
		factoryRegister.register("dataset-bounding-box-0.6", new DatasetBoundingBoxFilterFactory());
		factoryRegister.register("dataset-dump-0.6", new DumpDatasetFactory());
		factoryRegister.register("read-customdb-0.6", new ReadDatasetFactory());
		factoryRegister.register("write-pgsql-0.6", new PostgreSqlDatasetWriterFactory());
		factoryRegister.register("truncate-pgsql-0.6", new PostgreSqlDatasetTruncatorFactory());
		factoryRegister.register("write-pgsql-dump-0.6", new PostgreSqlDatasetDumpWriterFactory());
		factoryRegister.register("read-pgsql-0.6", new PostgreSqlDatasetReaderFactory());
		factoryRegister.register("write-pgsql-change-0.6", new PostgreSqlChangeWriterFactory());
		factoryRegister.register("used-node-0.6", new UsedNodeFilterFactory());
		factoryRegister.register("node-key-0.6", new NodeKeyFilterFactory());
		factoryRegister.register("node-key-value-0.6", new NodeKeyValueFilterFactory());
		factoryRegister.register("way-key-value-0.6", new WayKeyValueFilterFactory());
		factoryRegister.register("read-change-interval-0.6", new ChangeDownloaderFactory());
		factoryRegister.register("read-change-interval-init-0.6", new ChangeDownloadInitializerFactory());
		factoryRegister.register("migrate-0.6", new MigrateV05ToV06Factory());
		factoryRegister.register("mig-0.6", new MigrateV05ToV06Factory());
		factoryRegister.register("tag-sort-0.6", new TagSorterFactory());
		
		// Register the plugins.
		for (String plugin : plugins) {
			loadPlugin(plugin);
		}
	}
	
	
	/**
	 * Loads the tasks associated with a plugin.
	 * 
	 * @param plugin
	 *            The plugin loader class name.
	 */
	@SuppressWarnings("unchecked")
	private void loadPlugin(String plugin) {
		ClassLoader classLoader;
		Class<?> untypedPluginClass;
		Class<PluginLoader> pluginClass;
		PluginLoader pluginLoader;
		Map<String, TaskManagerFactory> pluginTasks;
		
		// Obtain the thread context class loader. This becomes important if run
		// within an application server environment where plugins might be
		// inaccessible to this class's classloader.
		classLoader = Thread.currentThread().getContextClassLoader();
		
		// Load the plugin class.
		try {
			untypedPluginClass = classLoader.loadClass(plugin);
		} catch (ClassNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to load plugin class (" + plugin + ").", e);
		}
		
		// Verify that the plugin implements the plugin loader interface.
		if (!PluginLoader.class.isAssignableFrom(untypedPluginClass)) {
			throw new OsmosisRuntimeException("The class (" + plugin + ") does not implement interface (" + PluginLoader.class.getName() + ").");
		}
		pluginClass = (Class<PluginLoader>) untypedPluginClass;
		
		// Instantiate the plugin loader.
		try {
			pluginLoader = pluginClass.newInstance();
		} catch (InstantiationException e) {
			throw new OsmosisRuntimeException("Unable to instantiate class (" + plugin + ")", e);
		} catch (IllegalAccessException e) {
			throw new OsmosisRuntimeException("Unable to instantiate class (" + plugin + ")", e);
		}
		
		// Obtain the plugin task factories with their names.
		pluginTasks = pluginLoader.loadTaskFactories();
		
		// Register the plugin tasks.
		for (Entry<String, TaskManagerFactory> taskEntry : pluginTasks.entrySet()) {
			factoryRegister.register(taskEntry.getKey(), taskEntry.getValue());
		}
	}
}
