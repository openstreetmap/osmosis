-- Database creation script for the simple PostgreSQL schema.
DROP TABLE IF EXISTS node;
DROP TABLE IF EXISTS node_tag;
DROP TABLE IF EXISTS way;
DROP TABLE IF EXISTS way_node;
DROP TABLE IF EXISTS way_tag;
DROP TABLE IF EXISTS relation;
DROP TABLE IF EXISTS relation_member;
DROP TABLE IF EXISTS relation_tag;
DROP TABLE IF EXISTS schema_info;


CREATE TABLE schema_info (
    version integer NOT NULL
);


CREATE TABLE node (
    id bigint NOT NULL,
    user_name text NOT NULL,
    tstamp timestamp without time zone NOT NULL
);


--SELECT AddGeometryColumn( 'node', 'coordinate', 4326, 'POINT', 2);
SELECT AddGeometryColumn( 'node', 'coordinate', -1, 'POINT', 2);


CREATE TABLE node_tag (
    node_id bigint NOT NULL,
    name text NOT NULL,
    value text NOT NULL
);


CREATE TABLE way (
    id bigint NOT NULL,
    user_name text NOT NULL,
    tstamp timestamp without time zone NOT NULL
);


CREATE TABLE way_node (
    way_id bigint NOT NULL,
    node_id bigint NOT NULL,
    sequence_id smallint NOT NULL
);


CREATE TABLE way_tag (
    way_id bigint NOT NULL,
    name text NOT NULL,
    value text
);


CREATE TABLE relation (
    id bigint NOT NULL,
    user_name text NOT NULL,
    tstamp timestamp without time zone NOT NULL
);


CREATE TABLE relation_member (
    relation_id bigint NOT NULL,
    member_id bigint NOT NULL,
    member_role text NOT NULL,
    member_type smallint NOT NULL
);


CREATE TABLE relation_tag (
    relation_id bigint NOT NULL,
    name text NOT NULL,
    value text NOT NULL
);


INSERT INTO schema_info (version) VALUES (1);


ALTER TABLE ONLY schema_info ADD CONSTRAINT pk_schema_info PRIMARY KEY (version);


ALTER TABLE ONLY node ADD CONSTRAINT pk_node PRIMARY KEY (id);


ALTER TABLE ONLY way ADD CONSTRAINT pk_way PRIMARY KEY (id);


ALTER TABLE ONLY way_node ADD CONSTRAINT pk_way_node PRIMARY KEY (way_id, sequence_id);


ALTER TABLE ONLY relation ADD CONSTRAINT pk_relation PRIMARY KEY (id);


CREATE INDEX idx_node_tag_node_id ON node_tag USING btree (node_id);
CREATE INDEX idx_node_location ON node USING gist (coordinate);


CREATE INDEX idx_way_tag_way_id ON way_tag USING btree (way_id);


CREATE INDEX idx_relation_tag_relation_id ON relation_tag USING btree (relation_id);
