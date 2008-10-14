-- Add a postgis GEOMETRY column to the way table for the purpose of storing the full linestring of the way.
SELECT AddGeometryColumn('ways', 'linestring', 4326, 'GEOMETRY', 2);

-- Add an index to the bbox column.
CREATE INDEX idx_ways_linestring ON ways USING gist (linestring);
