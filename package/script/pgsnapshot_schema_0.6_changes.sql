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

DROP TABLE IF EXISTS state;

 CREATE TABLE state (
  id SERIAL,
  tstamp TIMESTAMP without time zone NOT NULL DEFAULT(NOW()),
  sequence_number BIGINT NOT NULL,
  state_timestamp TIMESTAMP WITHOUT time zone NOT NULL,
  disabled BOOLEAN NOT NULL DEFAULT(false)
);

 DROP TABLE IF EXISTS locked;

 CREATE TABLE locked (
  id SERIAL,
  started TIMESTAMP WITHOUT time zone NOT NULL DEFAULT(NOW()),
  process TEXT NOT NULL,
  source TEXT NOT NULL,
  location TEXT NOT NULL,
  write_lock BOOLEAN NOT NULL DEFAULT(false)
);

 DROP FUNCTION IF EXISTS lock_database(TEXT, TEXT, TEXT);
CREATE OR REPLACE FUNCTION lock_database(new_process TEXT, new_source TEXT, new_location TEXT, request_write_lock BOOLEAN) RETURNS INT AS $$
  DECLARE locked_id INT;
  DECLARE current_id INT;
  DECLARE current_process TEXT;
  DECLARE current_source TEXT;
  DECLARE current_location TEXT;
  DECLARE current_write_lock BOOLEAN;
BEGIN

   SELECT id, process, source, location, write_lock
    INTO current_id, current_process, current_source, current_location, current_write_lock
    FROM locked ORDER BY write_lock DESC NULLS LAST LIMIT 1;
  IF (current_process IS NULL OR CHAR_LENGTH(current_process) = 0 OR (request_write_lock = FALSE AND current_write_lock = FALSE)) THEN
    INSERT INTO locked (process, source, location, write_lock) VALUES (new_process, new_source, new_location, request_write_lock) RETURNING id INTO locked_id;
    RETURN locked_id;
  ELSE
    RAISE EXCEPTION 'Database is locked by another id {%}, process {%}, source {%}, location {%}', current_id, current_process, current_source, current_location;
  END IF;
END;
$$ LANGUAGE plpgsql VOLATILE;

 CREATE OR REPLACE FUNCTION unlock_database(locked_id INT) RETURNS BOOLEAN AS $$
  DECLARE response BOOLEAN;
  DECLARE exist_count INT;
BEGIN
  IF (locked_id = -1) THEN
    DELETE FROM locked;
    RETURN true;
  ELSE
    SELECT COUNT(*) INTO exist_count FROM locked WHERE id = locked_id;
    IF (exist_count = 1) THEN
      DELETE FROM locked WHERE id = locked_id;
      RETURN true;
    ELSE
      RETURN false;
    END IF;
  END IF;
END;
$$ LANGUAGE plpgsql VOLATILE;
