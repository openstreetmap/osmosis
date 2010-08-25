-- Database script for the simple PostgreSQL schema.  This script moves all tags into hstore columns.

-- Create functions for building hstore data.
CREATE OR REPLACE FUNCTION build_node_tags() RETURNS void AS $$
DECLARE
	previousId nodes.id%TYPE;
	currentId nodes.id%TYPE;
	result hstore;
	tagRow node_tags%ROWTYPE;
BEGIN
	SET enable_seqscan = false;
	SET enable_mergejoin = false;
	SET enable_hashjoin = false;
	
	FOR tagRow IN SELECT * FROM node_tags ORDER BY node_id LOOP
		currentId := tagRow.node_id;
		
		IF currentId <> previousId THEN
			IF previousId IS NOT NULL THEN
				IF result IS NOT NULL THEN
					UPDATE nodes SET tags = result WHERE id = previousId;
					IF ((currentId / 100000) <> (previousId / 100000)) THEN
						RAISE INFO 'node id: %', previousId;
					END IF;
					result := NULL;
				END IF;
			END IF;
		END IF;
		
		IF result IS NULL THEN
			result := tagRow.k => tagRow.v;
		ELSE
			result := result || (tagRow.k => tagRow.v);
		END IF;
		
		previousId := currentId;
	END LOOP;
	
	IF previousId IS NOT NULL THEN
		IF result IS NOT NULL THEN
			UPDATE nodes SET tags = result WHERE id = previousId;
			result := NULL;
		END IF;
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION build_way_tags() RETURNS void AS $$
DECLARE
	previousId ways.id%TYPE;
	currentId ways.id%TYPE;
	result hstore;
	tagRow way_tags%ROWTYPE;
BEGIN
	SET enable_seqscan = false;
	SET enable_mergejoin = false;
	SET enable_hashjoin = false;
	
	FOR tagRow IN SELECT * FROM way_tags ORDER BY way_id LOOP
		currentId := tagRow.way_id;
		
		IF currentId <> previousId THEN
			IF previousId IS NOT NULL THEN
				IF result IS NOT NULL THEN
					UPDATE ways SET tags = result WHERE id = previousId;
					IF ((currentId / 100000) <> (previousId / 100000)) THEN
						RAISE INFO 'way id: %', previousId;
					END IF;
					result := NULL;
				END IF;
			END IF;
		END IF;
		
		IF result IS NULL THEN
			result := tagRow.k => tagRow.v;
		ELSE
			result := result || (tagRow.k => tagRow.v);
		END IF;
		
		previousId := currentId;
	END LOOP;
	
	IF previousId IS NOT NULL THEN
		IF result IS NOT NULL THEN
			UPDATE ways SET tags = result WHERE id = previousId;
			result := NULL;
		END IF;
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION build_relation_tags() RETURNS void AS $$
DECLARE
	previousId relations.id%TYPE;
	currentId relations.id%TYPE;
	result hstore;
	tagRow relation_tags%ROWTYPE;
BEGIN
	SET enable_seqscan = false;
	SET enable_mergejoin = false;
	SET enable_hashjoin = false;
	
	FOR tagRow IN SELECT * FROM relation_tags ORDER BY relation_id LOOP
		currentId := tagRow.relation_id;
		
		IF currentId <> previousId THEN
			IF previousId IS NOT NULL THEN
				IF result IS NOT NULL THEN
					UPDATE relations SET tags = result WHERE id = previousId;
					IF ((currentId / 100000) <> (previousId / 100000)) THEN
						RAISE INFO 'relation id: %', previousId;
					END IF;
					result := NULL;
				END IF;
			END IF;
		END IF;
		
		IF result IS NULL THEN
			result := tagRow.k => tagRow.v;
		ELSE
			result := result || (tagRow.k => tagRow.v);
		END IF;
		
		previousId := currentId;
	END LOOP;
	
	IF previousId IS NOT NULL THEN
		IF result IS NOT NULL THEN
			UPDATE relations SET tags = result WHERE id = previousId;
			result := NULL;
		END IF;
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION build_way_nodes() RETURNS void AS $$
DECLARE
	previousId ways.id%TYPE;
	currentId ways.id%TYPE;
	result bigint[];
	wayNodeRow way_nodes%ROWTYPE;
BEGIN
	SET enable_seqscan = false;
	SET enable_mergejoin = false;
	SET enable_hashjoin = false;
	
	FOR wayNodeRow IN SELECT * FROM way_nodes ORDER BY way_id, sequence_id LOOP
		currentId := wayNodeRow.way_id;
		
		IF currentId <> previousId THEN
			IF previousId IS NOT NULL THEN
				IF result IS NOT NULL THEN
					UPDATE ways SET nodes = result WHERE id = previousId;
					IF ((currentId / 100000) <> (previousId / 100000)) THEN
						RAISE INFO 'way id: %', previousId;
					END IF;
					result := NULL;
				END IF;
			END IF;
		END IF;
		
		IF result IS NULL THEN
			result = ARRAY[wayNodeRow.node_id];
		ELSE
			result = array_append(result, wayNodeRow.node_id);
		END IF;
		
		previousId := currentId;
	END LOOP;
	
	IF previousId IS NOT NULL THEN
		IF result IS NOT NULL THEN
			UPDATE ways SET nodes = result WHERE id = previousId;
			result := NULL;
		END IF;
	END IF;
END;
$$ LANGUAGE plpgsql;

-- Add hstore columns to entity tables.
ALTER TABLE nodes ADD COLUMN tags hstore;
ALTER TABLE ways ADD COLUMN tags hstore;
ALTER TABLE relations ADD COLUMN tags hstore;

-- Populate the hstore columns.
SELECT build_node_tags();
SELECT build_way_tags();
SELECT build_relation_tags();

-- Remove the hstore functions.
DROP FUNCTION build_node_tags();
DROP FUNCTION build_way_tags();
DROP FUNCTION build_relation_tags();

-- Drop the now redundant tag tables.
DROP TABLE node_tags;
DROP TABLE way_tags;
DROP TABLE relation_tags;

-- Add an index allowing relation_members to be queried by member id and type.
CREATE INDEX idx_relation_members_member_id_and_type ON relation_members USING btree (member_id, member_type);

-- Add the nodes column to the ways table.
ALTER TABLE ways ADD COLUMN nodes bigint[];

-- Populate the new nodes column on the ways table.
SELECT build_way_nodes();
--UPDATE ways w SET nodes = ARRAY(SELECT wn.node_id FROM way_nodes wn WHERE w.id = wn.way_id ORDER BY sequence_id);

-- Remove the way nodes function.
DROP FUNCTION build_way_nodes();

-- Organise data according to geographical location. 
CLUSTER nodes USING idx_nodes_geom;
CLUSTER ways USING idx_ways_linestring;

-- Create the function that provides "unnest" functionality while remaining compatible with 8.3.
CREATE OR REPLACE FUNCTION unnest_bbox_way_nodes() RETURNS void AS $$
DECLARE
	previousId ways.id%TYPE;
	currentId ways.id%TYPE;
	result bigint[];
	wayNodeRow way_nodes%ROWTYPE;
	wayNodes ways.nodes%TYPE;
BEGIN
	FOR wayNodes IN SELECT bw.nodes FROM bbox_ways bw LOOP
		FOR i IN 1 .. array_upper(wayNodes, 1) LOOP
			INSERT INTO bbox_way_nodes (id) VALUES (wayNodes[i]);
		END LOOP;
	END LOOP;
END;
$$ LANGUAGE plpgsql;


-- Update the schema version.
UPDATE schema_info SET version = 6;

VACUUM ANALYZE;
