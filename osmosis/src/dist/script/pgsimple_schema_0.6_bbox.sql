-- Add a postgis GEOMETRY column to the way table for the purpose of indexing the location of the way.
-- This will contain a bounding box surrounding the extremities of the way.
SELECT AddGeometryColumn('ways', 'bbox', 4326, 'GEOMETRY', 2);

-- Add an index to the bbox column.
CREATE INDEX idx_ways_bbox ON ways USING gist (bbox);