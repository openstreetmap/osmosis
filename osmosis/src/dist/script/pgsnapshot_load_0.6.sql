-- Allow data loss (but not corruption) in the case of a power outage. This is okay because we need to re-run the script anyways.
SET synchronous_commit TO OFF;

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

-- Uncomment these out if bbox or linestring columns are needed and the COPY
-- files do not include them. If you want these columns you should use the
-- enableBboxBuilder or enableLinestringBuilder options to --write-pgsql-dump
-- as they are faster than the following SQL.

/*SELECT DropGeometryColumn('ways', 'bbox');
SELECT DropGeometryColumn('ways', 'linestring');*/

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

ALTER TABLE ONLY nodes CLUSTER ON idx_nodes_geom;
ALTER TABLE ONLY way_nodes CLUSTER ON pk_way_nodes;
ALTER TABLE ONLY relation_members CLUSTER ON pk_relation_members;

-- Uncomment these if bbox or linestring columns are needed and the COPY files do not include them.

-- Update the bbox column of the way table.
/*SELECT AddGeometryColumn('ways', 'bbox', 4326, 'GEOMETRY', 2);
UPDATE ways SET bbox = (
	SELECT ST_Envelope(ST_Collect(geom))
	FROM nodes JOIN way_nodes ON way_nodes.node_id = nodes.id
	WHERE way_nodes.way_id = ways.id
);*/

-- Update the linestring column of the way table.
/*SELECT AddGeometryColumn('ways', 'linestring', 4326, 'GEOMETRY', 2);
UPDATE ways w SET linestring = (
	SELECT ST_MakeLine(c.geom) AS way_line FROM (
		SELECT n.geom AS geom
		FROM nodes n INNER JOIN way_nodes wn ON n.id = wn.node_id
		WHERE (wn.way_id = w.id) ORDER BY wn.sequence_id
	) c
);*/

-- Index the way bounding box column. If you don't have one of these columns, comment out the index
CREATE INDEX idx_ways_bbox ON ways USING gist (bbox);
CREATE INDEX idx_ways_linestring ON ways USING gist (linestring);

ALTER TABLE ONLY ways CLUSTER ON idx_ways_bbox;
ALTER TABLE ONLY ways CLUSTER ON idx_ways_linestring;

-- Optional: CLUSTER imported tables. CLUSTER takes a significant amount of time to run and a 
-- significant amount of free disk space but speeds up some queries.

--CLUSTER nodes;
--CLUSTER ways;

-- It is not necessary to CLUSTER way_nodes or relation_members after the initial load but you might want to do so later on

-- Perform database maintenance due to large database changes.
ANALYZE;
