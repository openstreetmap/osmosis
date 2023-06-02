-- Add a postgis GEOMETRY column to the way table for the purpose of indexing the location of the way.
-- This will contain a bounding box surrounding the extremities of the way.
SELECT AddGeometryColumn('ways', 'bbox', 4326, 'GEOMETRY', 2);

-- Add an index to the bbox column.
CREATE INDEX idx_ways_bbox ON ways USING gist (bbox);

-- Cluster table by geographical location.
CLUSTER ways USING idx_ways_bbox;

-- Create an aggregate function that always returns the first non-NULL item.  This is required for bbox queries.
CREATE OR REPLACE FUNCTION first_agg (anyelement, anyelement)
RETURNS anyelement AS $$
        SELECT CASE WHEN $1 IS NULL THEN $2 ELSE $1 END;
$$ LANGUAGE SQL STABLE;

CREATE AGGREGATE first (
        sfunc    = first_agg,
        basetype = anyelement,
        stype    = anyelement
);
