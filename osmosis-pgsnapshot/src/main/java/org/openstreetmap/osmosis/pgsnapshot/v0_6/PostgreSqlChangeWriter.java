// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsnapshot.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.ActionChangeWriter;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.ChangeWriter;


/**
 * A change sink writing to database tables. This aims to be suitable for
 * running at regular intervals with database overhead proportional to changeset
 * size.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlChangeWriter implements ChangeSink {

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private ChangeWriter changeWriter;
	private Map<ChangeAction, ActionChangeWriter> actionWriterMap;
	private DatabaseContext dbCtx;
	private SchemaVersionValidator schemaVersionValidator;
	private boolean initialized;
	private final Set<Long> appliedChangeSets;
 	private long earliestTimestamp = 9999999999999L;
	private long latestTimestamp = 0L;
	private final Map<String, Integer> modifications;
	private boolean logging = false;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param keepInvalidWays
	 *            If true, zero and single node ways are kept. Otherwise they are
	 *            silently dropped to avoid putting invalid geometries into the 
	 *            database which can cause problems with postgis functions.
	 * @param logging
	 * 			  If true, will log all sql queries to the database that was executed
	 * 			  from the change log
	 */
	public PostgreSqlChangeWriter(DatabaseLoginCredentials loginCredentials, 
			DatabasePreferences preferences, boolean keepInvalidWays, boolean logging) {
		dbCtx = new DatabaseContext(loginCredentials);
		changeWriter = new ChangeWriter(dbCtx, logging);
		actionWriterMap = new HashMap<ChangeAction, ActionChangeWriter>();
		actionWriterMap.put(ChangeAction.Create, 
				new ActionChangeWriter(changeWriter, ChangeAction.Create, keepInvalidWays));
		actionWriterMap.put(ChangeAction.Modify, 
				new ActionChangeWriter(changeWriter, ChangeAction.Modify, keepInvalidWays));
		actionWriterMap.put(ChangeAction.Delete, 
				new ActionChangeWriter(changeWriter, ChangeAction.Delete, keepInvalidWays));
		
		schemaVersionValidator = new SchemaVersionValidator(dbCtx.getJdbcTemplate(), preferences);
		appliedChangeSets = new HashSet<>();
		modifications = new HashMap<>();
		initialized = false;
		this.logging = logging;
	}
	
	
	private void initialize() {
		if (!initialized) {
			dbCtx.beginTransaction();
			
			initialized = true;
		}
	}
    
    
    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer change) {
		ChangeAction action;
		
		initialize();
		
		// Verify that the schema version is supported.
		schemaVersionValidator.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
		
		action = change.getAction();
		
		if (!actionWriterMap.containsKey(action)) {
			throw new OsmosisRuntimeException("The action " + action + " is unrecognized.");
		}

		final Entity entity = change.getEntityContainer().getEntity();
		this.appliedChangeSets.add(entity.getChangesetId());
		this.earliestTimestamp = Math.min(this.earliestTimestamp, entity.getTimestamp().getTime());
		this.latestTimestamp = Math.max(this.latestTimestamp, entity.getTimestamp().getTime());

 		final String name = entity.getType().name() + "-" + action.name();
		final int count = modifications.getOrDefault(name, 0) + 1;
		modifications.put(name, count);
		
		// Process the entity using the action writer appropriate for the change
		// action.
		change.getEntityContainer().process(actionWriterMap.get(action));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		initialize();
		
		changeWriter.complete();

		if (this.logging) {
			// on complete write to the changes table
			dbCtx.getJdbcTemplate().execute(String.format("INSERT INTO replication_changes "
				+ "(nodes_added, nodes_modified, nodes_deleted, "
				+ "ways_added, ways_modified, ways_deleted, "
				+ "relations_added, relations_modified, relations_deleted, "
				+ "changesets_applied, earliest_timestamp, latest_timestamp) "
				+ "VALUES "
				+ "(%s, %s, %s, %s, %s, %s, %s, %s, %s, ARRAY[%s]::BIGINT[], '%s', '%s')",
				modifications.getOrDefault(EntityType.Node.name() + "-" + ChangeAction.Create, 0),
				modifications.getOrDefault(EntityType.Node.name() + "-" + ChangeAction.Modify, 0),
				modifications.getOrDefault(EntityType.Node.name() + "-" + ChangeAction.Delete, 0),
				modifications.getOrDefault(EntityType.Way.name() + "-" + ChangeAction.Create, 0),
				modifications.getOrDefault(EntityType.Way.name() + "-" + ChangeAction.Modify, 0),
				modifications.getOrDefault(EntityType.Way.name() + "-" + ChangeAction.Delete, 0),
				modifications.getOrDefault(EntityType.Relation.name() + "-" + ChangeAction.Create, 0),
				modifications.getOrDefault(EntityType.Relation.name() + "-" + ChangeAction.Modify, 0),
				modifications.getOrDefault(EntityType.Relation.name() + "-" + ChangeAction.Delete, 0),
		appliedChangeSets.stream().map(id -> id + "").collect(Collectors.joining(",")),
				FORMATTER.format(new Date(earliestTimestamp)), FORMATTER.format(new Date(latestTimestamp))));
		}
		
		dbCtx.commitTransaction();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void close() {
		changeWriter.release();
		
		dbCtx.close();
	}
}
