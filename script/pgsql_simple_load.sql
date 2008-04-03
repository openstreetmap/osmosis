-- Drop all primary keys and indexes to improve load speed.
ALTER TABLE node DROP CONSTRAINT pk_node;
ALTER TABLE way DROP CONSTRAINT pk_way;
ALTER TABLE way_node DROP CONSTRAINT pk_way_node;
ALTER TABLE relation DROP CONSTRAINT pk_relation;
DROP INDEX idx_node_tag_node_id;
DROP INDEX idx_node_location;
DROP INDEX idx_way_tag_way_id;
DROP INDEX idx_way_node_node_id;
DROP INDEX idx_relation_tag_relation_id;
DROP INDEX idx_way_bbox;

-- Import the table data from the data files using the fast COPY method.
COPY node FROM E'C:\\tmp\\pgimport\\node.txt';
COPY node_tag FROM E'C:\\tmp\\pgimport\\node_tag.txt';
COPY way FROM E'C:\\tmp\\pgimport\\way.txt';
COPY way_tag FROM E'C:\\tmp\\pgimport\\way_tag.txt';
COPY way_node FROM E'C:\\tmp\\pgimport\\way_node.txt';
COPY relation FROM E'C:\\tmp\\pgimport\\relation.txt';
COPY relation_tag FROM E'C:\\tmp\\pgimport\\relation_tag.txt';
COPY relation_member FROM E'C:\\tmp\\pgimport\\relation_member.txt';

-- Add the primary keys and indexes back again (except the way bbox index).
ALTER TABLE ONLY node ADD CONSTRAINT pk_node PRIMARY KEY (id);
ALTER TABLE ONLY way ADD CONSTRAINT pk_way PRIMARY KEY (id);
ALTER TABLE ONLY way_node ADD CONSTRAINT pk_way_node PRIMARY KEY (way_id, sequence_id);
ALTER TABLE ONLY relation ADD CONSTRAINT pk_relation PRIMARY KEY (id);
CREATE INDEX idx_node_tag_node_id ON node_tag USING btree (node_id);
CREATE INDEX idx_node_location ON node USING gist (coordinate);
CREATE INDEX idx_way_tag_way_id ON way_tag USING btree (way_id);
CREATE INDEX idx_way_node_node_id ON way_node USING btree (node_id);
CREATE INDEX idx_relation_tag_relation_id ON relation_tag USING btree (relation_id);

-- Update the bbox column of the way table.
UPDATE way SET bbox = (
	SELECT Envelope(Collect(coordinate))
	FROM node JOIN way_node ON way_node.node_id = node.id
	WHERE way_node.way_id = way.id
);

-- Index the way bounding box column.
CREATE INDEX idx_way_bbox ON way USING gist (bbox);

-- Perform database maintenance due to large database changes.
VACUUM;
ANALYZE;
