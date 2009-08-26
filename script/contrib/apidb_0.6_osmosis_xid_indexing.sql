-- This script creates a function and indexes that allow osmosis to efficiently query based on transaction ids.
CREATE OR REPLACE FUNCTION xid_to_int4(t xid)
  RETURNS integer AS
$BODY$
	DECLARE
		tl bigint;
		ti int;
        BEGIN
		tl := t;

		IF tl >= 2147483648 THEN
			tl := tl - 4294967296;
		END IF;
		
		ti := tl;
		
                RETURN ti;
        END;
$BODY$
LANGUAGE 'plpgsql' IMMUTABLE STRICT;


DROP INDEX IF EXISTS nodes_xmin_idx;
DROP INDEX IF EXISTS ways_xmin_idx;
DROP INDEX IF EXISTS relations_xmin_idx;
CREATE INDEX nodes_xmin_idx ON nodes USING btree ((xid_to_int4(xmin)));
CREATE INDEX ways_xmin_idx ON ways USING btree ((xid_to_int4(xmin)));
CREATE INDEX relations_xmin_idx ON relations USING btree ((xid_to_int4(xmin)));
