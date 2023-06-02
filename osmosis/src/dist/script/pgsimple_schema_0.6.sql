-- Database creation script for the simple PostgreSQL schema.

-- Drop all tables if they exist.
DROP TABLE IF EXISTS actions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS nodes;
DROP TABLE IF EXISTS node_tags;
DROP TABLE IF EXISTS ways;
DROP TABLE IF EXISTS way_nodes;
DROP TABLE IF EXISTS way_tags;
DROP TABLE IF EXISTS relations;
DROP TABLE IF EXISTS relation_members;
DROP TABLE IF EXISTS relation_tags;
DROP TABLE IF EXISTS schema_info;

-- Drop all stored procedures if they exist.
DROP FUNCTION IF EXISTS osmosisUpdate();


-- Create a table which will contain a single row defining the current schema version.
CREATE TABLE schema_info (
    version integer NOT NULL
);


-- Create a table for users.
CREATE TABLE users (
    id int NOT NULL,
    name text NOT NULL
);


-- Create a table for nodes.
CREATE TABLE nodes (
    id bigint NOT NULL,
    version int NOT NULL,
    user_id int NOT NULL,
    tstamp timestamp without time zone NOT NULL,
    changeset_id bigint NOT NULL
);
-- Add a postgis point column holding the location of the node.
SELECT AddGeometryColumn('nodes', 'geom', 4326, 'POINT', 2);


-- Create a table for node tags.
CREATE TABLE node_tags (
    node_id bigint NOT NULL,
    k text NOT NULL,
    v text NOT NULL
);


-- Create a table for ways.
CREATE TABLE ways (
    id bigint NOT NULL,
    version int NOT NULL,
    user_id int NOT NULL,
    tstamp timestamp without time zone NOT NULL,
    changeset_id bigint NOT NULL
);


-- Create a table for representing way to node relationships.
CREATE TABLE way_nodes (
    way_id bigint NOT NULL,
    node_id bigint NOT NULL,
    sequence_id int NOT NULL
);


-- Create a table for way tags.
CREATE TABLE way_tags (
    way_id bigint NOT NULL,
    k text NOT NULL,
    v text
);


-- Create a table for relations.
CREATE TABLE relations (
    id bigint NOT NULL,
    version int NOT NULL,
    user_id int NOT NULL,
    tstamp timestamp without time zone NOT NULL,
    changeset_id bigint NOT NULL
);

-- Create a table for representing relation member relationships.
CREATE TABLE relation_members (
    relation_id bigint NOT NULL,
    member_id bigint NOT NULL,
    member_type character(1) NOT NULL,
    member_role text NOT NULL,
    sequence_id int NOT NULL
);


-- Create a table for relation tags.
CREATE TABLE relation_tags (
    relation_id bigint NOT NULL,
    k text NOT NULL,
    v text NOT NULL
);


-- Configure the schema version.
INSERT INTO schema_info (version) VALUES (5);


-- Add primary keys to tables.
ALTER TABLE ONLY schema_info ADD CONSTRAINT pk_schema_info PRIMARY KEY (version);

ALTER TABLE ONLY users ADD CONSTRAINT pk_users PRIMARY KEY (id);

ALTER TABLE ONLY nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id);

ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id);

ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id);

ALTER TABLE ONLY relations ADD CONSTRAINT pk_relations PRIMARY KEY (id);

ALTER TABLE ONLY relation_members ADD CONSTRAINT pk_relation_members PRIMARY KEY (relation_id, sequence_id);


-- Add indexes to tables.
CREATE INDEX idx_node_tags_node_id ON node_tags USING btree (node_id);
CREATE INDEX idx_nodes_geom ON nodes USING gist (geom);

CREATE INDEX idx_way_tags_way_id ON way_tags USING btree (way_id);
CREATE INDEX idx_way_nodes_node_id ON way_nodes USING btree (node_id);

CREATE INDEX idx_relation_tags_relation_id ON relation_tags USING btree (relation_id);


-- Create stored procedures.
CREATE FUNCTION osmosisUpdate() RETURNS void AS $$
DECLARE
BEGIN
END;
$$ LANGUAGE plpgsql;
