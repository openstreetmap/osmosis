-- Add the relation members primary key.
ALTER TABLE ONLY relation_members ADD CONSTRAINT pk_relation_members PRIMARY KEY (relation_id, sequence_id);

-- Upgrade the schema version.
UPDATE schema_info SET version = 5;