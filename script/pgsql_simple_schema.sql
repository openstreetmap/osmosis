-- Database creation script for the simple PostgreSQL schema.

-- Drop all tables if they exist.
DROP TABLE IF EXISTS nodes;
DROP TABLE IF EXISTS node_tags;
DROP TABLE IF EXISTS ways;
DROP TABLE IF EXISTS way_nodes;
DROP TABLE IF EXISTS way_tags;
DROP TABLE IF EXISTS relations;
DROP TABLE IF EXISTS relation_members;
DROP TABLE IF EXISTS relation_tags;
DROP TABLE IF EXISTS schema_info;


-- Create a table which will contain a single row defining the current schema version.
CREATE TABLE schema_info (
    version integer NOT NULL
);


-- Create a table for nodes.
CREATE TABLE nodes (
    id bigint NOT NULL,
    user_name text NOT NULL,
    tstamp timestamp without time zone NOT NULL
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
    user_name text NOT NULL,
    tstamp timestamp without time zone NOT NULL
);
-- Add a postgis bounding box column used for indexing the location of the way.
-- This will contain a bounding box surrounding the extremities of the way.
SELECT AddGeometryColumn('ways', 'bbox', 4326, 'GEOMETRY', 2);


-- Create a table for representing way to node relationships.
CREATE TABLE way_nodes (
    way_id bigint NOT NULL,
    node_id bigint NOT NULL,
    sequence_id smallint NOT NULL
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
    user_name text NOT NULL,
    tstamp timestamp without time zone NOT NULL
);

-- Create a table for representing relation member relationships.
CREATE TABLE relation_members (
    relation_id bigint NOT NULL,
    member_id bigint NOT NULL,
    member_role text NOT NULL,
    member_type smallint NOT NULL
);


-- Create a table for relation tags.
CREATE TABLE relation_tags (
    relation_id bigint NOT NULL,
    k text NOT NULL,
    v text NOT NULL
);


-- Configure the schema version.
INSERT INTO schema_info (version) VALUES (1);


-- Add primary keys to tables.

ALTER TABLE ONLY schema_info ADD CONSTRAINT pk_schema_info PRIMARY KEY (version);


ALTER TABLE ONLY nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id);


ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id);


ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id);


ALTER TABLE ONLY relations ADD CONSTRAINT pk_relations PRIMARY KEY (id);


-- Add indexes to tables.

CREATE INDEX idx_node_tags_node_id ON node_tags USING btree (node_id);
CREATE INDEX idx_nodes_geom ON nodes USING gist (geom);


CREATE INDEX idx_way_tags_way_id ON way_tags USING btree (way_id);
CREATE INDEX idx_ways_bbox ON ways USING gist (bbox);
CREATE INDEX idx_way_nodes_node_id ON way_nodes USING btree (node_id);


CREATE INDEX idx_relation_tags_relation_id ON relation_tags USING btree (relation_id);
