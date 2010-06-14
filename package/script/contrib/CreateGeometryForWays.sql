-------------------------------------------------------------------------------
-- The following script creates a new table for the pgsql simple schema for
-- storing full way geometries.
--
-- Author: Ralf
-------------------------------------------------------------------------------


-- drop table if it exists
DROP TABLE IF EXISTS way_geometry;

-- create table
CREATE TABLE way_geometry(
  way_id bigint NOT NULL
);
-- add PostGIS geometry column
SELECT AddGeometryColumn('', 'way_geometry', 'geom', 4326, 'GEOMETRY', 2);



-------------------------------------------------------------------------------
-- the following might go into the POST_LOAD_SQL-array in the class "PostgreSqlWriter"??
-------------------------------------------------------------------------------

-- add a linestring for every way (create a polyline)
INSERT INTO way_geometry select id, ( select ST_LineFromMultiPoint( Collect(nodes.geom) ) from nodes 
left join way_nodes on nodes.id=way_nodes.node_id where way_nodes.way_id=ways.id ) FROM ways;

-- after creating a line for every way (polyline), we want closed ways to be stored as polygones. 
-- So we need to delete the previously created polylines for these ways first.
DELETE FROM way_geometry WHERE way_id IN 
  ( SELECT ways.id FROM ways 
  WHERE ST_IsClosed( (SELECT ST_LineFromMultiPoint( Collect(n.geom) ) FROM nodes n LEFT JOIN way_nodes wn ON n.id=wn.node_id WHERE ways.id=wn.way_id) ) 
  AND ST_NumPoints( (SELECT ST_LineFromMultiPoint( Collect(n.geom) ) FROM nodes n LEFT JOIN way_nodes wn ON n.id=wn.node_id WHERE ways.id=wn.way_id) ) >= 3 
  )
;

-- now we need to add the polyline geometry for every closed way
INSERT INTO way_geometry SELECT ways.id, 
  ( SELECT ST_MakePolygon( ST_LineFromMultiPoint(Collect(nodes.geom)) ) FROM nodes 
  LEFT JOIN way_nodes ON nodes.id=way_nodes.node_id WHERE way_nodes.way_id=ways.id 
  ) 
FROM ways 
WHERE ST_IsClosed( (SELECT ST_LineFromMultiPoint( Collect(n.geom) ) FROM nodes n LEFT JOIN way_nodes wn ON n.id=wn.node_id WHERE ways.id=wn.way_id) ) 
AND ST_NumPoints( (SELECT ST_LineFromMultiPoint( Collect(n.geom) ) FROM nodes n LEFT JOIN way_nodes wn ON n.id=wn.node_id WHERE ways.id=wn.way_id) ) >= 3 
;
-------------------------------------------------------------------------------

-- create index on way_geometry
CREATE INDEX idx_way_geometry_way_id ON way_geometry USING btree (way_id);
CREATE INDEX idx_way_geometry_geom ON way_geometry USING gist (geom);
