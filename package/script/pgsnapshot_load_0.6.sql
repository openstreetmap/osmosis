-- Drop all primary keys and indexes to improve load speed.
ALTER TABLE nodes DROP CONSTRAINT pk_nodes;
ALTER TABLE ways DROP CONSTRAINT pk_ways;
ALTER TABLE way_nodes DROP CONSTRAINT pk_way_nodes;
ALTER TABLE relations DROP CONSTRAINT pk_relations;
ALTER TABLE relation_members DROP CONSTRAINT pk_relation_members;
DROP INDEX idx_nodes_geom;
DROP INDEX idx_way_nodes_node_id;
DROP INDEX idx_relation_members_member_id_and_type;
DROP INDEX idx_ways_bbox;
DROP INDEX idx_ways_linestring;

-- Comment these out if the COPY files include bbox or linestring column values.
SELECT DropGeometryColumn('ways', 'bbox');
SELECT DropGeometryColumn('ways', 'linestring');

-- Import the table data from the data files using the fast COPY method.
\copy users FROM 'users.txt'
\copy nodes FROM 'nodes.txt'
\copy ways FROM 'ways.txt'
\copy way_nodes FROM 'way_nodes.txt'
\copy relations FROM 'relations.txt'
\copy relation_members FROM 'relation_members.txt'

-- Add the primary keys and indexes back again (except the way bbox index).
ALTER TABLE ONLY nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id);
ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id);
ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id);
ALTER TABLE ONLY relations ADD CONSTRAINT pk_relations PRIMARY KEY (id);
ALTER TABLE ONLY relation_members ADD CONSTRAINT pk_relation_members PRIMARY KEY (relation_id, sequence_id);
CREATE INDEX idx_nodes_geom ON nodes USING gist (geom);
CREATE INDEX idx_way_nodes_node_id ON way_nodes USING btree (node_id);
CREATE INDEX idx_relation_members_member_id_and_type ON relation_members USING btree (member_id, member_type);

-- Comment these out if the COPY files include bbox or linestring column values.
SELECT AddGeometryColumn('ways', 'bbox', 4326, 'GEOMETRY', 2);
SELECT AddGeometryColumn('ways', 'linestring', 4326, 'GEOMETRY', 2);

-- Comment these out if the COPY files include bbox or linestring column values.
-- Update the bbox column of the way table.
UPDATE ways SET bbox = (
	SELECT Envelope(Collect(geom))
	FROM nodes JOIN way_nodes ON way_nodes.node_id = nodes.id
	WHERE way_nodes.way_id = ways.id
);
-- Update the linestring column of the way table.
UPDATE ways w SET linestring = (
	SELECT ST_MakeLine(c.geom) AS way_line FROM (
		SELECT n.geom AS geom
		FROM nodes n INNER JOIN way_nodes wn ON n.id = wn.node_id
		WHERE (wn.way_id = w.id) ORDER BY wn.sequence_id
	) c
);

-- Index the way bounding box column.
CREATE INDEX idx_ways_bbox ON ways USING gist (bbox);
CREATE INDEX idx_ways_linestring ON ways USING gist (linestring);

-- Update all clustered tables because it doesn't happen implicitly.
CLUSTER nodes USING idx_nodes_geom;
CLUSTER ways USING idx_ways_linestring;

-- Perform database maintenance due to large database changes.
VACUUM ANALYZE;
