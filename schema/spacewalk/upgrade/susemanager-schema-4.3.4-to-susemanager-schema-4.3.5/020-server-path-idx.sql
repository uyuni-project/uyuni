
DROP INDEX IF EXISTS rhn_serverpath_sid_pos_uq;

ALTER TABLE rhnServerPath DROP CONSTRAINT IF EXISTS rhn_serverpath_sid_pos;
ALTER TABLE rhnServerPath ADD CONSTRAINT rhn_serverpath_sid_pos UNIQUE
    (server_id, position) DEFERRABLE INITIALLY DEFERRED;
