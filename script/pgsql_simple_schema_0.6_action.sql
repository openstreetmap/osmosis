-- Add an action table for the purpose of capturing all actions applied to a database.
-- The table is populated during application of a changeset, then osmosisUpdate is called,
-- then the table is cleared all within a single database transaction.
-- The contents of this table can be used to update derivative tables by customising the
-- osmosisUpdate stored procedure.

-- Create a table for actions.
CREATE TABLE actions (
	data_type character(1) NOT NULL,
	action character(1) NOT NULL,
	id bigint NOT NULL
);

-- Add primary key.
ALTER TABLE ONLY actions ADD CONSTRAINT pk_actions PRIMARY KEY (data_type, id);
