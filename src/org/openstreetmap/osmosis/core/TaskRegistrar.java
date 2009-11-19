// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.java.plugin.JpfException;
import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.ManifestProcessingException;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;
import org.openstreetmap.osmosis.core.apidb.v0_6.ApidbChangeReaderFactory;
import org.openstreetmap.osmosis.core.apidb.v0_6.ApidbChangeWriterFactory;
import org.openstreetmap.osmosis.core.apidb.v0_6.ApidbCurrentReaderFactory;
import org.openstreetmap.osmosis.core.apidb.v0_6.ApidbFileReplicatorFactory;
import org.openstreetmap.osmosis.core.apidb.v0_6.ApidbReaderFactory;
import org.openstreetmap.osmosis.core.apidb.v0_6.ApidbTestReaderFactory;
import org.openstreetmap.osmosis.core.apidb.v0_6.ApidbTruncatorFactory;
import org.openstreetmap.osmosis.core.apidb.v0_6.ApidbWriterFactory;
import org.openstreetmap.osmosis.core.buffer.v0_6.ChangeBufferFactory;
import org.openstreetmap.osmosis.core.buffer.v0_6.EntityBufferFactory;
import org.openstreetmap.osmosis.core.change.v0_6.ChangeApplierFactory;
import org.openstreetmap.osmosis.core.change.v0_6.ChangeDeriverFactory;
import org.openstreetmap.osmosis.core.customdb.v0_6.DumpDatasetFactory;
import org.openstreetmap.osmosis.core.customdb.v0_6.ReadDatasetFactory;
import org.openstreetmap.osmosis.core.customdb.v0_6.WriteDatasetFactory;
import org.openstreetmap.osmosis.core.filter.v0_6.BoundingBoxFilterFactory;
import org.openstreetmap.osmosis.core.filter.v0_6.DatasetBoundingBoxFilterFactory;
import org.openstreetmap.osmosis.core.filter.v0_6.NodeKeyFilterFactory;
import org.openstreetmap.osmosis.core.filter.v0_6.NodeKeyValueFilterFactory;
import org.openstreetmap.osmosis.core.filter.v0_6.PolygonFilterFactory;
import org.openstreetmap.osmosis.core.filter.v0_6.UsedNodeFilterFactory;
import org.openstreetmap.osmosis.core.filter.v0_6.WayKeyFilterFactory;
import org.openstreetmap.osmosis.core.filter.v0_6.WayKeyValueFilterFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.ChangeAppenderFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.ChangeSimplifierFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.IntervalDownloaderInitializerFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.IntervalDownloaderFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.ChangeMergerFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.EntityMergerFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.ReplicationDownloaderFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.ReplicationDownloaderInitializerFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.ReplicationFileMergerFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.ReplicationFileMergerInitializerFactory;
import org.openstreetmap.osmosis.core.migrate.MigrateChangeV05ToV06Factory;
import org.openstreetmap.osmosis.core.migrate.MigrateV05ToV06Factory;
import org.openstreetmap.osmosis.core.misc.v0_6.NullChangeWriterFactory;
import org.openstreetmap.osmosis.core.misc.v0_6.NullWriterFactory;
import org.openstreetmap.osmosis.core.pgsql.v0_6.PostgreSqlChangeWriterFactory;
import org.openstreetmap.osmosis.core.pgsql.v0_6.PostgreSqlDatasetDumpWriterFactory;
import org.openstreetmap.osmosis.core.pgsql.v0_6.PostgreSqlDatasetReaderFactory;
import org.openstreetmap.osmosis.core.pgsql.v0_6.PostgreSqlDatasetTruncatorFactory;
import org.openstreetmap.osmosis.core.pgsql.v0_6.PostgreSqlDatasetWriterFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactoryRegister;
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
import org.openstreetmap.osmosis.core.sort.v0_6.EntitySorterFactory;
import org.openstreetmap.osmosis.core.sort.v0_6.TagSorterFactory;
import org.openstreetmap.osmosis.core.tagremove.v0_6.TagRemoverFactory;
import org.openstreetmap.osmosis.core.tee.v0_6.ChangeTeeFactory;
import org.openstreetmap.osmosis.core.tee.v0_6.EntityTeeFactory;
import org.openstreetmap.osmosis.core.xml.v0_6.FastXmlReaderFactory;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeReaderFactory;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeUploaderFactory;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeWriterFactory;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlDownloaderFactory;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlReaderFactory;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlWriterFactory;


/**
 * Provides the initialisation logic for registering all task factories.
 * 
 * @author Brett Henderson
 */
public class TaskRegistrar {

    /**
     * Our logger for debug and error -output.
     */
    private static final Logger LOG = Logger.getLogger(TaskRegistrar.class.getName());

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
	 * Initialises factories for all tasks. Loads additionally specified plugins
	 * as well as default tasks.
	 * 
	 * @param plugins
	 *            The class names of all plugins to be loaded.
	 */
	public void initialize(List<String> plugins) {
		org.openstreetmap.osmosis.core.sort.v0_5.EntitySorterFactory entitySorterFactory05;
		org.openstreetmap.osmosis.core.sort.v0_5.ChangeSorterFactory changeSorterFactory05;
		EntitySorterFactory entitySorterFactory06;
		ChangeSorterFactory changeSorterFactory06;

		// Configure factories that require additional information.
		entitySorterFactory05 = new org.openstreetmap.osmosis.core.sort.v0_5.EntitySorterFactory();
		entitySorterFactory05.registerComparator("TypeThenId",
				new org.openstreetmap.osmosis.core.sort.v0_5.EntityByTypeThenIdComparator(), true);
		changeSorterFactory05 = new org.openstreetmap.osmosis.core.sort.v0_5.ChangeSorterFactory();
		changeSorterFactory05.registerComparator("streamable",
				new org.openstreetmap.osmosis.core.sort.v0_5.ChangeForStreamableApplierComparator(), true);
		changeSorterFactory05.registerComparator("seekable",
				new org.openstreetmap.osmosis.core.sort.v0_5.ChangeForSeekableApplierComparator(), false);
		entitySorterFactory06 = new EntitySorterFactory();
		entitySorterFactory06.registerComparator("TypeThenId", new EntityByTypeThenIdComparator(), true);
		changeSorterFactory06 = new ChangeSorterFactory();
		changeSorterFactory06.registerComparator("streamable", new ChangeForStreamableApplierComparator(), true);
		changeSorterFactory06.registerComparator("seekable", new ChangeForSeekableApplierComparator(), false);

		// Register factories.
		factoryRegister.register("apply-change", new ChangeApplierFactory());
		factoryRegister.register("ac", new ChangeApplierFactory());
		factoryRegister.register("bounding-box", new BoundingBoxFilterFactory());
		factoryRegister.register("bb", new BoundingBoxFilterFactory());
		factoryRegister.register("derive-change", new ChangeDeriverFactory());
		factoryRegister.register("dc", new ChangeDeriverFactory());
		factoryRegister.register("read-xml", new XmlReaderFactory());
		factoryRegister.register("rx", new XmlReaderFactory());
        factoryRegister.register("read-xml-change",  new XmlChangeReaderFactory());
        factoryRegister.register("upload-xml-change", new XmlChangeUploaderFactory());
		factoryRegister.register("rxc", new XmlChangeReaderFactory());
		factoryRegister.register("sort", entitySorterFactory05);
		factoryRegister.register("s", entitySorterFactory05);
		factoryRegister.register("sort-change", changeSorterFactory05);
		factoryRegister.register("sc", changeSorterFactory05);
		factoryRegister.register("write-xml", new XmlWriterFactory());
		factoryRegister.register("wx", new XmlWriterFactory());
		factoryRegister.register("write-xml-change", new XmlChangeWriterFactory());
		factoryRegister.register("wxc", new XmlChangeWriterFactory());
		factoryRegister.register("write-null", new NullWriterFactory());
		factoryRegister.register("wn", new NullWriterFactory());
		factoryRegister.register("write-null-change", new NullChangeWriterFactory());
		factoryRegister.register("wnc", new NullChangeWriterFactory());
		factoryRegister.register("buffer", new EntityBufferFactory());
		factoryRegister.register("b", new EntityBufferFactory());
		factoryRegister.register("buffer-change", new ChangeBufferFactory());
		factoryRegister.register("bc", new ChangeBufferFactory());
		factoryRegister.register("merge", new EntityMergerFactory());
		factoryRegister.register("m", new EntityMergerFactory());
		factoryRegister.register("merge-change", new ChangeMergerFactory());
		factoryRegister.register("mc", new ChangeMergerFactory());
		factoryRegister.register("read-api", new XmlDownloaderFactory());
		factoryRegister.register("ra", new XmlDownloaderFactory());
		factoryRegister.register("bounding-polygon", new PolygonFilterFactory());
		factoryRegister.register("bp", new PolygonFilterFactory());
		factoryRegister.register("report-entity", new EntityReporterFactory());
		factoryRegister.register("re", new EntityReporterFactory());
		factoryRegister.register("report-integrity", new IntegrityReporterFactory());
		factoryRegister.register("ri", new IntegrityReporterFactory());
		factoryRegister.register("log-progress", new EntityProgressLoggerFactory());
		factoryRegister.register("lp", new EntityProgressLoggerFactory());
		factoryRegister.register("log-progress-change", new ChangeProgressLoggerFactory());
		factoryRegister.register("lpc", new ChangeProgressLoggerFactory());
		factoryRegister.register("tee", new EntityTeeFactory());
		factoryRegister.register("t", new EntityTeeFactory());
		factoryRegister.register("tee-change", new ChangeTeeFactory());
		factoryRegister.register("tc", new ChangeTeeFactory());
		factoryRegister.register("write-customdb", new WriteDatasetFactory());
		factoryRegister.register("wc", new WriteDatasetFactory());
		factoryRegister.register("dataset-bounding-box", new DatasetBoundingBoxFilterFactory());
		factoryRegister.register("dbb", new DatasetBoundingBoxFilterFactory());
		factoryRegister.register("dataset-dump", new DumpDatasetFactory());
		factoryRegister.register("dd", new DumpDatasetFactory());
		factoryRegister.register("read-customdb", new ReadDatasetFactory());
		factoryRegister.register("rc", new ReadDatasetFactory());
		factoryRegister.register("write-pgsql", new PostgreSqlDatasetWriterFactory());
		factoryRegister.register("wp", new PostgreSqlDatasetWriterFactory());
		factoryRegister.register("truncate-pgsql", new PostgreSqlDatasetTruncatorFactory());
		factoryRegister.register("tp", new PostgreSqlDatasetTruncatorFactory());
		factoryRegister.register("write-pgsql-dump", new PostgreSqlDatasetDumpWriterFactory());
		factoryRegister.register("wpd", new PostgreSqlDatasetDumpWriterFactory());
		factoryRegister.register("read-pgsql", new PostgreSqlDatasetReaderFactory());
		factoryRegister.register("rp", new PostgreSqlDatasetReaderFactory());
		factoryRegister.register("write-pgsql-change", new PostgreSqlChangeWriterFactory());
		factoryRegister.register("wpc", new PostgreSqlChangeWriterFactory());
		factoryRegister.register("used-node", new UsedNodeFilterFactory());
		factoryRegister.register("un", new UsedNodeFilterFactory());
		factoryRegister.register("node-key", new NodeKeyFilterFactory());
		factoryRegister.register("nk", new NodeKeyFilterFactory());
		factoryRegister.register("node-key-value", new NodeKeyValueFilterFactory());
		factoryRegister.register("nkv", new NodeKeyValueFilterFactory());
		factoryRegister.register("way-key", new WayKeyFilterFactory());
		factoryRegister.register("wk", new WayKeyFilterFactory());
		factoryRegister.register("way-key-value", new WayKeyValueFilterFactory());
		factoryRegister.register("wkv", new WayKeyValueFilterFactory());
		factoryRegister.register("read-change-interval", new IntervalDownloaderFactory());
		factoryRegister.register("rci", new IntervalDownloaderFactory());
		factoryRegister.register("read-change-interval-init", new IntervalDownloaderInitializerFactory());
		factoryRegister.register("rcii", new IntervalDownloaderInitializerFactory());
		factoryRegister.register("read-replication-interval", new ReplicationDownloaderFactory());
		factoryRegister.register("rri", new ReplicationDownloaderFactory());
		factoryRegister.register("read-replication-interval-init", new ReplicationDownloaderInitializerFactory());
		factoryRegister.register("rrii", new ReplicationDownloaderInitializerFactory());
		factoryRegister.register("merge-replication-files", new ReplicationFileMergerFactory());
		factoryRegister.register("mrf", new ReplicationFileMergerFactory());
		factoryRegister.register("merge-replication-files-init", new ReplicationFileMergerInitializerFactory());
		factoryRegister.register("mrfi", new ReplicationFileMergerInitializerFactory());
		factoryRegister.register("migrate", new MigrateV05ToV06Factory());
		factoryRegister.register("mig", new MigrateV05ToV06Factory());
		factoryRegister.register("migrate-change", new MigrateChangeV05ToV06Factory());
		factoryRegister.register("migc", new MigrateChangeV05ToV06Factory());
		factoryRegister.register("read-apidb", new ApidbReaderFactory());
		factoryRegister.register("rd", new ApidbReaderFactory());
		factoryRegister.register("read-apidb-change", new ApidbChangeReaderFactory());
		factoryRegister.register("rdc", new ApidbChangeReaderFactory());
		factoryRegister.register("read-apidb-current", new ApidbCurrentReaderFactory());
		factoryRegister.register("rdcur", new ApidbCurrentReaderFactory());
		factoryRegister.register("write-apidb", new ApidbWriterFactory());
		factoryRegister.register("wd", new ApidbWriterFactory());
		factoryRegister.register("write-apidb-change", new ApidbChangeWriterFactory());
		factoryRegister.register("wdc", new ApidbChangeWriterFactory());
		factoryRegister.register("truncate-apidb", new ApidbTruncatorFactory());
		factoryRegister.register("td", new ApidbTruncatorFactory());
		factoryRegister.register("append-change", new ChangeAppenderFactory());
		factoryRegister.register("apc", new ChangeAppenderFactory());
		factoryRegister.register("replicate-apidb", new ApidbFileReplicatorFactory());
		factoryRegister.register("repa", new ApidbFileReplicatorFactory());
		factoryRegister.register("read-apidb-test", new ApidbTestReaderFactory());
		factoryRegister.register("rat", new ApidbFileReplicatorFactory());
		factoryRegister.register("simplify-change", new ChangeSimplifierFactory());
		factoryRegister.register("simc", new ChangeSimplifierFactory());
		
		factoryRegister.register("apply-change-0.5",
				new org.openstreetmap.osmosis.core.change.v0_5.ChangeApplierFactory());
		factoryRegister.register("bounding-box-0.5",
				new org.openstreetmap.osmosis.core.filter.v0_5.BoundingBoxFilterFactory());
		factoryRegister.register("derive-change-0.5",
				new org.openstreetmap.osmosis.core.change.v0_5.ChangeDeriverFactory());
		factoryRegister.register("read-mysql-0.5", new org.openstreetmap.osmosis.core.mysql.v0_5.MysqlReaderFactory());
		factoryRegister.register("read-mysql-change-0.5",
				new org.openstreetmap.osmosis.core.mysql.v0_5.MysqlChangeReaderFactory());
		factoryRegister.register("read-mysql-current-0.5",
				new org.openstreetmap.osmosis.core.mysql.v0_5.MySqlCurrentReaderFactory());
		factoryRegister.register("read-xml-0.5", new org.openstreetmap.osmosis.core.xml.v0_5.XmlReaderFactory());
		factoryRegister.register("read-xml-change-0.5",
				new org.openstreetmap.osmosis.core.xml.v0_5.XmlChangeReaderFactory());
		factoryRegister.register("sort-0.5", entitySorterFactory05);
		factoryRegister.register("sort-change-0.5", changeSorterFactory05);
		factoryRegister.register("write-mysql-0.5", new org.openstreetmap.osmosis.core.mysql.v0_5.MysqlWriterFactory());
		factoryRegister.register("write-mysql-change-0.5",
				new org.openstreetmap.osmosis.core.mysql.v0_5.MysqlChangeWriterFactory());
		factoryRegister.register("truncate-mysql-0.5",
				new org.openstreetmap.osmosis.core.mysql.v0_5.MysqlTruncatorFactory());
		factoryRegister.register("write-xml-0.5", new org.openstreetmap.osmosis.core.xml.v0_5.XmlWriterFactory());
		factoryRegister.register("write-xml-change-0.5",
				new org.openstreetmap.osmosis.core.xml.v0_5.XmlChangeWriterFactory());
		factoryRegister.register("write-null-0.5", new org.openstreetmap.osmosis.core.misc.v0_5.NullWriterFactory());
		factoryRegister.register("write-null-change-0.5",
				new org.openstreetmap.osmosis.core.misc.v0_5.NullChangeWriterFactory());
		factoryRegister.register("buffer-0.5", new org.openstreetmap.osmosis.core.buffer.v0_5.EntityBufferFactory());
		factoryRegister.register("buffer-change-0.5",
				new org.openstreetmap.osmosis.core.buffer.v0_5.ChangeBufferFactory());
		factoryRegister.register("merge-0.5", new org.openstreetmap.osmosis.core.merge.v0_5.EntityMergerFactory());
		factoryRegister.register("merge-change-0.5",
				new org.openstreetmap.osmosis.core.merge.v0_5.ChangeMergerFactory());
		factoryRegister.register("read-api-0.5", new org.openstreetmap.osmosis.core.xml.v0_5.XmlDownloaderFactory());
		factoryRegister.register("bounding-polygon-0.5",
				new org.openstreetmap.osmosis.core.filter.v0_5.PolygonFilterFactory());
		factoryRegister.register("report-entity-0.5",
				new org.openstreetmap.osmosis.core.report.v0_5.EntityReporterFactory());
		factoryRegister.register("report-integrity-0.5",
				new org.openstreetmap.osmosis.core.report.v0_5.IntegrityReporterFactory());
		factoryRegister.register("log-progress-0.5",
				new org.openstreetmap.osmosis.core.progress.v0_5.EntityProgressLoggerFactory());
		factoryRegister.register("log-change-progress-0.5",
				new org.openstreetmap.osmosis.core.progress.v0_5.ChangeProgressLoggerFactory());
		factoryRegister.register("tee-0.5", new org.openstreetmap.osmosis.core.tee.v0_5.EntityTeeFactory());
		factoryRegister.register("tee-change-0.5", new org.openstreetmap.osmosis.core.tee.v0_5.ChangeTeeFactory());
		factoryRegister.register("write-customdb-0.5",
				new org.openstreetmap.osmosis.core.customdb.v0_5.WriteDatasetFactory());
		factoryRegister.register("dataset-bounding-box-0.5",
				new org.openstreetmap.osmosis.core.filter.v0_5.DatasetBoundingBoxFilterFactory());
		factoryRegister.register("dataset-dump-0.5",
				new org.openstreetmap.osmosis.core.customdb.v0_5.DumpDatasetFactory());
		factoryRegister.register("read-customdb-0.5",
				new org.openstreetmap.osmosis.core.customdb.v0_5.ReadDatasetFactory());
		factoryRegister.register("write-pgsql-0.5",
				new org.openstreetmap.osmosis.core.pgsql.v0_5.PostgreSqlDatasetWriterFactory());
		factoryRegister.register("truncate-pgsql-0.5",
				new org.openstreetmap.osmosis.core.pgsql.v0_5.PostgreSqlDatasetTruncatorFactory());
		factoryRegister.register("write-pgsql-dump-0.5",
				new org.openstreetmap.osmosis.core.pgsql.v0_5.PostgreSqlDatasetDumpWriterFactory());
		factoryRegister.register("read-pgsql-0.5",
				new org.openstreetmap.osmosis.core.pgsql.v0_5.PostgreSqlDatasetReaderFactory());
		factoryRegister.register("write-pgsql-change-0.5",
				new org.openstreetmap.osmosis.core.pgsql.v0_5.PostgreSqlChangeWriterFactory());
		factoryRegister.register("used-node-0.5",
				new org.openstreetmap.osmosis.core.filter.v0_5.UsedNodeFilterFactory());
		factoryRegister.register("node-key-0.5", new org.openstreetmap.osmosis.core.filter.v0_5.NodeKeyFilterFactory());
		factoryRegister.register("node-key-value-0.5",
				new org.openstreetmap.osmosis.core.filter.v0_5.NodeKeyValueFilterFactory());
		factoryRegister.register("way-key-value-0.5",
				new org.openstreetmap.osmosis.core.filter.v0_5.WayKeyValueFilterFactory());
		factoryRegister.register("read-change-interval-0.5",
				new org.openstreetmap.osmosis.core.merge.v0_5.ChangeDownloaderFactory());
		factoryRegister.register("read-change-interval-init-0.5",
				new org.openstreetmap.osmosis.core.merge.v0_5.ChangeDownloadInitializerFactory());

		factoryRegister.register("apply-change-0.6", new ChangeApplierFactory());
		factoryRegister.register("bounding-box-0.6", new BoundingBoxFilterFactory());
		factoryRegister.register("derive-change-0.6", new ChangeDeriverFactory());
		factoryRegister.register("read-xml-0.6", new XmlReaderFactory());
		factoryRegister.register("fast-read-xml-0.6", new FastXmlReaderFactory());
		factoryRegister.register("read-xml-change-0.6", new XmlChangeReaderFactory());
		factoryRegister.register("sort-0.6", entitySorterFactory06);
		factoryRegister.register("sort-change-0.6", changeSorterFactory06);
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
		factoryRegister.register("way-key-0.6", new WayKeyFilterFactory());
		factoryRegister.register("way-key-value-0.6", new WayKeyValueFilterFactory());
		factoryRegister.register("read-change-interval-0.6", new IntervalDownloaderFactory());
		factoryRegister.register("read-change-interval-init-0.6", new IntervalDownloaderInitializerFactory());
		factoryRegister.register("read-replication-interval-0.6", new ReplicationDownloaderFactory());
		factoryRegister.register("read-replication-interval-init-0.6", new ReplicationDownloaderInitializerFactory());
		factoryRegister.register("merge-replication-files-0.6", new ReplicationFileMergerFactory());
		factoryRegister.register("merge-replication-files-init-0.6", new ReplicationFileMergerInitializerFactory());
		factoryRegister.register("migrate-0.6", new MigrateV05ToV06Factory());
		factoryRegister.register("mig-0.6", new MigrateV05ToV06Factory());
		factoryRegister.register("tag-sort-0.6", new TagSorterFactory());
		factoryRegister.register("tag-sort-change-0.6", new ChangeTagSorterFactory());
		factoryRegister.register("remove-tags-0.6", new TagRemoverFactory());
		factoryRegister.register("read-apidb-0.6", new ApidbReaderFactory());
		factoryRegister.register("read-apidb-change-0.6", new ApidbChangeReaderFactory());
		factoryRegister.register("read-apidb-current-0.6", new ApidbCurrentReaderFactory());
		factoryRegister.register("write-apidb-0.6", new ApidbWriterFactory());
		factoryRegister.register("write-apidb-change-0.6", new ApidbChangeWriterFactory());
		factoryRegister.register("truncate-apidb-0.6", new ApidbTruncatorFactory());
		factoryRegister.register("append-change-0.6", new ChangeAppenderFactory());
		factoryRegister.register("replicate-apidb-0.6", new ApidbFileReplicatorFactory());
		factoryRegister.register("read-apidb-test-0.6", new ApidbTestReaderFactory());
		factoryRegister.register("simplify-change-0.6", new ChangeSimplifierFactory());

		// Register the plugins.
		for (String plugin : plugins) {
			loadPlugin(plugin);
		}
		loadJPFPlugins();
	}


	/**
	 * Loads the tasks implemented as plugins.
	 * 
	 */
	private void loadJPFPlugins() {
		PluginManager pluginManager;
		
		// Create a new JPF plugin manager.
		pluginManager = ObjectFactory.newInstance().createManager();
		
		// Search known locations for plugin files.
		LOG.fine("Searching for JPF plugins.");
		List<PluginLocation> locations = gatherJpfPlugins();
		
		// Register the core plugin.
		LOG.fine("Registering the core plugin.");
		registerCorePlugin(pluginManager);
		
		// Register all located plugins.
		LOG.fine("Registering the extension plugins.");
		if (locations.size() == 0) {
			// There are no plugins available so stop processing here.
		   return;
		}
		registerJpfPlugins(pluginManager, locations);
		
		// Initialise all of the plugins that have been registered.
		LOG.fine("Activating the plugins.");
		// load plugins for the task-extension-point
		PluginDescriptor core = pluginManager.getRegistry()
				.getPluginDescriptor("org.openstreetmap.osmosis.core.plugin.Core");

		ExtensionPoint point = pluginManager.getRegistry().getExtensionPoint(core.getId(), "Task");
		for (Iterator<Extension> it = point.getConnectedExtensions().iterator(); it.hasNext();) {

			Extension ext = it.next();
			PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
			try {
				pluginManager.enablePlugin(descr, true);
				pluginManager.activatePlugin(descr.getId());
				ClassLoader classLoader = pluginManager.getPluginClassLoader(descr);
				loadPluginClass(ext.getParameter("class").valueAsString(), classLoader);
			} catch (PluginLifecycleException e) {
				throw new OsmosisRuntimeException("Cannot load JPF-plugin '" + ext.getId()
						+ "' for extensionpoint '" + ext.getExtendedPointId() + "'", e);
			}
		}
	}


	/**
	 * Register the core plugin from which other plugins will extend.
	 * 
	 * @param pluginManager
	 *            The plugin manager to register the plugin with.
	 */
	private void registerCorePlugin(PluginManager pluginManager) {
		try {
			URL core;
			PluginDescriptor coreDescriptor;
			
			// Get the plugin configuration file.
			core = getClass().getResource("/org/openstreetmap/osmosis/core/plugin/plugin.xml");
			LOG.finest("Plugin URL: " + core);
			
			// Register the core plugin in the plugin registry.
			pluginManager.getRegistry().register(new URL[] {core});
			
			// Get the plugin descriptor from the registry.
			coreDescriptor = pluginManager.getRegistry().getPluginDescriptor(
					"org.openstreetmap.osmosis.core.plugin.Core");
			
			// Enable the plugin.
			pluginManager.enablePlugin(coreDescriptor, true);
			pluginManager.activatePlugin("org.openstreetmap.osmosis.core.plugin.Core");
			
		} catch (ManifestProcessingException e) {
			throw new OsmosisRuntimeException("Unable to register core plugin.", e);
		} catch (PluginLifecycleException e) {
			throw new OsmosisRuntimeException("Unable to enable core plugin.", e);
		}
	}


	/**
	 * Register the given JPF-plugins with the {@link PluginManager}.
	 * 
	 * @param locations
	 *            the plugins found
	 */
	private void registerJpfPlugins(PluginManager pluginManager, List<PluginLocation> locations) {
		if (locations == null) {
			throw new IllegalArgumentException("null plugin-list given");
		}

		try {
			pluginManager.publishPlugins(locations.toArray(new PluginLocation[locations.size()]));
		} catch (JpfException e) {
			throw new OsmosisRuntimeException("Unable to publish plugins.", e);
		}
	}


	/**
	 * @return a list of all JPF-plugins found.
	 */
	private List<PluginLocation> gatherJpfPlugins() {
		File[] pluginsDirs = new File[] {
				new File("plugins"),
				new File(System.getProperty("user.home") + "/.openstreetmap" + File.separator + "osmosis"
						+ File.separator + "plugins"),
				new File(System.getenv("APPDATA") + File.separator + "openstreetmap" + File.separator + "osmosis"
						+ File.separator + "plugins")

		};

		FilenameFilter pluginFileNameFilter = new FilenameFilter() {

			/**
			 * @param dir
			 *            the directory of the file
			 * @param name
			 *            the unqualified name of the file
			 * @return true if this may be a plugin-file
			 */
			public boolean accept(final File dir, final String name) {
				return name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar");
			}
		};
		List<PluginLocation> locations = new LinkedList<PluginLocation>();
		for (File pluginDir : pluginsDirs) {
			LOG.finer("Loading plugins in " + pluginDir.getAbsolutePath());
			if (!pluginDir.exists()) {
				continue;
			}
			File[] plugins = pluginDir.listFiles(pluginFileNameFilter);
			try {
				for (int i = 0; i < plugins.length; i++) {
					LOG.finest("Found plugin " + plugins[i].getAbsolutePath());
					locations.add(StandardPluginLocation.create(plugins[i]));
				}
			} catch (MalformedURLException e) {
				throw new OsmosisRuntimeException("Cannot create plugin location " + pluginDir.getAbsolutePath(), e);
			}
		}
		return locations;
	}


	/**
	 * Loads the tasks associated with a plugin (old plugin-api).
	 * 
	 * @param plugin
	 *            The plugin loader class name.
	 */
	private void loadPlugin(final String plugin) {
		ClassLoader classLoader;

		// Obtain the thread context class loader. This becomes important if run
		// within an application server environment where plugins might be
		// inaccessible to this class's classloader.
		classLoader = Thread.currentThread().getContextClassLoader();

		loadPluginClass(plugin, classLoader);
	}


	/**
	 * Load the given plugin, old API or new JPF.
	 * 
	 * @param pluginClassName
	 *            the name of the class to instantiate
	 * @param classLoader
	 *            the ClassLoader to use
	 */
	@SuppressWarnings("unchecked")
	private void loadPluginClass(final String pluginClassName, final ClassLoader classLoader) {
		Class<?> untypedPluginClass;
		PluginLoader pluginLoader;
		Map<String, TaskManagerFactory> pluginTasks;
		// Load the plugin class.
		try {
			untypedPluginClass = classLoader.loadClass(pluginClassName);
		} catch (ClassNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to load plugin class (" + pluginClassName + ").", e);
		}
		// Verify that the plugin implements the plugin loader interface.
		if (!PluginLoader.class.isAssignableFrom(untypedPluginClass)) {
			throw new OsmosisRuntimeException("The class (" + pluginClassName + ") does not implement interface ("
					+ PluginLoader.class.getName() + "). Maybe it's not a plugin?");
		}
		Class<PluginLoader> pluginClass = (Class<PluginLoader>) untypedPluginClass;

		// Instantiate the plugin loader.
		try {
			pluginLoader = pluginClass.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Unable to instantiate plugin class (" + pluginClassName + ").", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Unable to instantiate plugin class (" + pluginClassName + ").", e);
		}

		// Obtain the plugin task factories with their names.
		pluginTasks = pluginLoader.loadTaskFactories();

		// Register the plugin tasks.
		for (Entry<String, TaskManagerFactory> taskEntry : pluginTasks.entrySet()) {
			factoryRegister.register(taskEntry.getKey(), taskEntry.getValue());
		}
	}
}
