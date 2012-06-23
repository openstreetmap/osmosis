// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.logging.Logger;

import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;


/**
 * Drops and creates indexes in support of bulk load activities.
 * 
 * @author Brett Henderson
 */
public class IndexManager {
	
	private static final Logger LOG = Logger.getLogger(IndexManager.class.getName());
	
	
	private static final String[] PRE_LOAD_SQL = {
		"ALTER TABLE users DROP CONSTRAINT pk_users",
		"ALTER TABLE nodes DROP CONSTRAINT pk_nodes",
		"ALTER TABLE ways DROP CONSTRAINT pk_ways",
		"ALTER TABLE way_nodes DROP CONSTRAINT pk_way_nodes",
		"ALTER TABLE relations DROP CONSTRAINT pk_relations",
		"ALTER TABLE relation_members DROP CONSTRAINT pk_relation_members",
		"DROP INDEX idx_nodes_geom",
		"DROP INDEX idx_way_nodes_node_id",
		"DROP INDEX idx_relation_members_member_id_and_type"
	};
	private static final String[] PRE_LOAD_SQL_WAY_BBOX = {
		"DROP INDEX idx_ways_bbox"
	};
	private static final String[] PRE_LOAD_SQL_WAY_LINESTRING = {
		"DROP INDEX idx_ways_linestring"
	};
	
	private static final String[] POST_LOAD_SQL = {
		"ALTER TABLE ONLY users ADD CONSTRAINT pk_users PRIMARY KEY (id)",
		"ALTER TABLE ONLY nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id)",
		"ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id)",
		"ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id)",
		"ALTER TABLE ONLY relations ADD CONSTRAINT pk_relations PRIMARY KEY (id)",
		"ALTER TABLE ONLY relation_members ADD CONSTRAINT pk_relation_members PRIMARY KEY (relation_id, sequence_id)",
		"CREATE INDEX idx_nodes_geom ON nodes USING gist (geom)",
		"CREATE INDEX idx_way_nodes_node_id ON way_nodes USING btree (node_id)",
		"CREATE INDEX idx_relation_members_member_id_and_type ON relation_members USING btree (member_id, member_type)"
	};
	private static final String[] POST_LOAD_SQL_WAY_BBOX = {
		"CREATE INDEX idx_ways_bbox ON ways USING gist (bbox)"
	};
	private static final String[] POST_LOAD_SQL_WAY_LINESTRING = {
		"CREATE INDEX idx_ways_linestring ON ways USING gist (linestring)"
	};
	private static final String POST_LOAD_SQL_POPULATE_WAY_BBOX =
		"UPDATE ways SET bbox = ("
		+ "SELECT ST_Envelope(ST_Collect(geom)) FROM nodes JOIN way_nodes ON way_nodes.node_id = nodes.id"
		+ " WHERE way_nodes.way_id = ways.id"
		+ ")";
	private static final String POST_LOAD_SQL_POPULATE_WAY_LINESTRING =
		"UPDATE ways w SET linestring = ("
		+ "SELECT ST_MakeLine(c.geom) AS way_line FROM ("
		+ "SELECT n.geom AS geom FROM nodes n INNER JOIN way_nodes wn ON n.id = wn.node_id"
		+ " WHERE (wn.way_id = w.id) ORDER BY wn.sequence_id"
		+ ") c"
		+ ")";
	
	
	private SimpleJdbcTemplate jdbcTemplate;
	private DatabaseCapabilityChecker capabilityChecker;
	private boolean populateBbox;
	private boolean populateLinestring;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            Provides access to the database.
	 * @param populateBbox
	 *            If true, the bbox colum on the way table will be populated
	 *            after load.
	 * @param populateLinestring
	 *            If true, the linestring column on the way table will be
	 *            populated after load.
	 */
	public IndexManager(DatabaseContext dbCtx, boolean populateBbox, boolean populateLinestring) {
		this.populateBbox = populateBbox;
		this.populateLinestring = populateLinestring;
		
		jdbcTemplate = dbCtx.getSimpleJdbcTemplate();
		capabilityChecker = new DatabaseCapabilityChecker(dbCtx);
	}
	
	
	/**
	 * Drops indexes and constraints in the database.
	 */
	public void prepareForLoad() {
		LOG.fine("Running pre-load SQL statements.");
		for (int i = 0; i < PRE_LOAD_SQL.length; i++) {
			LOG.finer("SQL: " + PRE_LOAD_SQL[i]);
			jdbcTemplate.update(PRE_LOAD_SQL[i]);
		}
		if (capabilityChecker.isWayBboxSupported()) {
			LOG.fine("Running pre-load bbox SQL statements.");
			for (int i = 0; i < PRE_LOAD_SQL_WAY_BBOX.length; i++) {
				LOG.finer("SQL: " + PRE_LOAD_SQL_WAY_BBOX[i]);
				jdbcTemplate.update(PRE_LOAD_SQL_WAY_BBOX[i]);
			}
		}
		if (capabilityChecker.isWayLinestringSupported()) {
			LOG.fine("Running pre-load linestring SQL statements.");
			for (int i = 0; i < PRE_LOAD_SQL_WAY_LINESTRING.length; i++) {
				LOG.finer("SQL: " + PRE_LOAD_SQL_WAY_LINESTRING[i]);
				jdbcTemplate.update(PRE_LOAD_SQL_WAY_LINESTRING[i]);
			}
		}
		LOG.fine("Pre-load SQL statements complete.");
	}
	
	
	/**
	 * Creates indexes in the database and populates derived columns.
	 */
	public void completeAfterLoad() {
		LOG.fine("Running post-load SQL.");
		for (int i = 0; i < POST_LOAD_SQL.length; i++) {
			LOG.finer("SQL: " + POST_LOAD_SQL[i]);
			jdbcTemplate.update(POST_LOAD_SQL[i]);
		}
		if (capabilityChecker.isWayBboxSupported()) {
			LOG.fine("Running post-load bbox SQL statements.");
			if (populateBbox) {
				LOG.finer("SQL: " + POST_LOAD_SQL_POPULATE_WAY_BBOX);
				jdbcTemplate.update(POST_LOAD_SQL_POPULATE_WAY_BBOX);
			}
			for (int i = 0; i < POST_LOAD_SQL_WAY_BBOX.length; i++) {
				LOG.finer("SQL: " + POST_LOAD_SQL_WAY_BBOX[i]);
				jdbcTemplate.update(POST_LOAD_SQL_WAY_BBOX[i]);
			}
		}
		if (capabilityChecker.isWayLinestringSupported()) {
			LOG.fine("Running post-load linestring SQL statements.");
			if (populateLinestring) {
				LOG.finer("SQL: " + POST_LOAD_SQL_POPULATE_WAY_LINESTRING);
				jdbcTemplate.update(POST_LOAD_SQL_POPULATE_WAY_LINESTRING);
			}
			for (int i = 0; i < POST_LOAD_SQL_WAY_LINESTRING.length; i++) {
				LOG.finer("SQL: " + POST_LOAD_SQL_WAY_LINESTRING[i]);
				jdbcTemplate.update(POST_LOAD_SQL_WAY_LINESTRING[i]);
			}
		}
	}
}
