-- Database creation script for the simple PostgreSQL schema.

-- Drop all tables if they exist.
DROP TABLE IF EXISTS node;
DROP TABLE IF EXISTS node_tag;
DROP TABLE IF EXISTS way;
DROP TABLE IF EXISTS way_node;
DROP TABLE IF EXISTS way_tag;
DROP TABLE IF EXISTS relation;
DROP TABLE IF EXISTS relation_member;
DROP TABLE IF EXISTS relation_tag;
DROP TABLE IF EXISTS schema_info;


-- Create a table which will contain a single row defining the current schema version.
CREATE TABLE schema_info (
    version integer NOT NULL
);


-- Create a table for nodes.
CREATE TABLE node (
    id bigint NOT NULL,
    user_name text NOT NULL,
    tstamp timestamp without time zone NOT NULL
);
-- Add a postgis point column holding the location of the node.
SELECT AddGeometryColumn('node', 'coordinate', -1, 'POINT', 2);

-- Create a table for node tags.
CREATE TABLE node_tag (
    node_id bigint NOT NULL,
    name text NOT NULL,
    value text NOT NULL
);


-- Create a table for ways.
CREATE TABLE way (
    id bigint NOT NULL,
    user_name text NOT NULL,
    tstamp timestamp without time zone NOT NULL
);
-- Add a postgis bounding box column used for indexing the location of the way.
-- This will contain a bounding box surrounding the extremities of the way.
SELECT AddGeometryColumn('way', 'bbox', -1, 'GEOMETRY', 2);


-- Create a table for representing way to node relationships.
CREATE TABLE way_node (
    way_id bigint NOT NULL,
    node_id bigint NOT NULL,
    sequence_id smallint NOT NULL
);


-- Create a table for way tags.
CREATE TABLE way_tag (
    way_id bigint NOT NULL,
    name text NOT NULL,
    value text
);


-- Create a table for relations.
CREATE TABLE relation (
    id bigint NOT NULL,
    user_name text NOT NULL,
    tstamp timestamp without time zone NOT NULL
);

-- Create a table for representing relation member relationships.
CREATE TABLE relation_member (
    relation_id bigint NOT NULL,
    member_id bigint NOT NULL,
    member_role text NOT NULL,
    member_type smallint NOT NULL
);


-- Create a table for relation tags.
CREATE TABLE relation_tag (
    relation_id bigint NOT NULL,
    name text NOT NULL,
    value text NOT NULL
);


-- Configure the schema version.
INSERT INTO schema_info (version) VALUES (1);


-- Add primary keys to tables.

ALTER TABLE ONLY schema_info ADD CONSTRAINT pk_schema_info PRIMARY KEY (version);


ALTER TABLE ONLY node ADD CONSTRAINT pk_node PRIMARY KEY (id);


ALTER TABLE ONLY way ADD CONSTRAINT pk_way PRIMARY KEY (id);


ALTER TABLE ONLY way_node ADD CONSTRAINT pk_way_node PRIMARY KEY (way_id, sequence_id);


ALTER TABLE ONLY relation ADD CONSTRAINT pk_relation PRIMARY KEY (id);


-- Add indexes to tables.

CREATE INDEX idx_node_tag_node_id ON node_tag USING btree (node_id);
CREATE INDEX idx_node_location ON node USING gist (coordinate);


CREATE INDEX idx_way_tag_way_id ON way_tag USING btree (way_id);
CREATE INDEX idx_way_bbox ON way USING gist (bbox);


CREATE INDEX idx_relation_tag_relation_id ON relation_tag USING btree (relation_id);
