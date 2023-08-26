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
    latest_timestamp TIMESTAMP without time zone NOT NULL
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
