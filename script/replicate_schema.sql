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
	interval int NOT NULL,
	tstamp timestamp without time zone NOT NULL
);
ALTER TABLE ONLY queue ADD CONSTRAINT pk_queue PRIMARY KEY (id);


-- Create a table attaching items to queues.
CREATE TABLE item_queue (
	item_id bigint NOT NULL,
	queue_id int NOT NULL,
	selected boolean NOT NULL
);
ALTER TABLE ONLY item_queue ADD CONSTRAINT pk_item_queue PRIMARY KEY (item_id, queue_id);
CREATE INDEX idx_item_queue_selected ON item_queue USING btree (selected);
ALTER TABLE item_queue ADD CONSTRAINT fk_item_queue_item_id FOREIGN KEY (item_id) REFERENCES item (id);
ALTER TABLE item_queue ADD CONSTRAINT fk_item_queue_queue_id FOREIGN KEY (queue_id) REFERENCES queue (id);
