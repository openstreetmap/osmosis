-- Database creation script for the PostgreSQL replication schema.
-- This schema supports the osmosis multicast replication functionality.

DROP TABLE IF EXISTS item_queue;
DROP TABLE IF EXISTS queue;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS system;
DROP TABLE IF EXISTS schema_info;


-- Create a table which will contain a single row defining the current schema version.
CREATE TABLE schema_info (
    version integer NOT NULL
);


-- Configure the schema version.
INSERT INTO schema_info (version) VALUES (1);


-- Create a table holding top level system data.
CREATE TABLE system (
    id int NOT NULL,
    tstamp timestamp without time zone NOT NULL
);

-- Configure the system record.
INSERT INTO system (id, tstamp) VALUES (1, timestamp 'January 1, 1970');


-- Create a table holding replication items.
CREATE TABLE item (
	id bigserial,
	tstamp timestamp without time zone NOT NULL,
	payload bytea
);
ALTER TABLE ONLY item ADD CONSTRAINT pk_item PRIMARY KEY (id);


-- Create a table defining recipient queues.
CREATE TABLE queue (
	id serial,
	name text NOT NULL,
	last_item_id bigint
);
ALTER TABLE ONLY queue ADD CONSTRAINT pk_queue PRIMARY KEY (id);
ALTER TABLE queue ADD CONSTRAINT uix_queue_name UNIQUE(name);
