DROP TABLE IF EXISTS replication_changes;

-- Create a table for replication changes that are applied to the database.
CREATE TABLE replication_changes (
    id SERIAL,
    tstamp TIMESTAMP without time zone NOT NULL DEFAULT(NOW()),
    nodes_modified INT NOT NULL DEFAULT (0),
    nodes_added INT NOT NULL DEFAULT (0),
    nodes_deleted INT NOT NULL DEFAULT (0),
    ways_modified INT NOT NULL DEFAULT (0),
    ways_added INT NOT NULL DEFAULT (0),
    ways_deleted INT NOT NULL DEFAULT (0),
    relations_modified INT NOT NULL DEFAULT (0),
    relations_added INT NOT NULL DEFAULT (0),
    relations_deleted INT NOT NULL DEFAULT (0),
    changesets_applied BIGINT [] NOT NULL,
    earliest_timestamp TIMESTAMP without time zone NOT NULL,
    latest_timestamp TIMESTAMP without time zone NOT NULL,
    node_table_count BIGINT NOT NULL DEFAULT(0),
    ways_table_count BIGINT NOT NULL DEFAULT(0),
    relation_table_count BIGINT NOT NULL DEFAULT(0)
);

DROP TABLE IF EXISTS sql_changes;

CREATE TABLE sql_changes (
  id SERIAL,
  tstamp TIMESTAMP without time zone NOT NULL DEFAULT(NOW()),
  entity_id BIGINT NOT NULL,
  type TEXT NOT NULL,
  changeset_id BIGINT NOT NULL,
  change_time TIMESTAMP NOT NULL,
  action INT NOT NULL,
  query text NOT NULL,
  arguments text
);

DROP TABLE IF EXISTS state;

CREATE TABLE state (
  id SERIAL,
  tstamp TIMESTAMP without time zone NOT NULL DEFAULT(NOW()),
  sequence_number BIGINT NOT NULL,
  state_timestamp TIMESTAMP WITHOUT time zone NOT NULL
);

DROP TABLE IF EXISTS locked;

CREATE TABLE locked (
  started TIMESTAMP WITHOUT time zone NOT NULL DEFAULT(NOW()) PRIMARY KEY,
  process TEXT NOT NULL,
  source TEXT NOT NULL,
  location TEXT NOT NULL
);

CREATE OR REPLACE FUNCTION count_update() RETURNS trigger AS
$BODY$
BEGIN
	UPDATE replication_changes
		SET node_table_count = (SELECT COUNT(*) FROM nodes),
			ways_table_count = (SELECT COUNT(*) FROM ways),
			relation_table_count = (SELECT COUNT(*) FROM relations)
		WHERE ID = new.id;
	RETURN new;
END
$BODY$
	LANGUAGE plpgsql VOLATILE;

DROP TRIGGER IF EXISTS replication_counts_trigger ON replication_changes;
CREATE TRIGGER replication_counts_trigger AFTER INSERT ON replication_changes
FOR EACH ROW
	EXECUTE PROCEDURE count_update();
