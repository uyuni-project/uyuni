-- add the new column to store the products which have no successors

ALTER TABLE rhnActionDup ADD COLUMN IF NOT EXISTS missing_successors VARCHAR(512);
