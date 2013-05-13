-- Recreate FK constraint on rhnChannel
ALTER TABLE suseCVEServerChannel DROP CONSTRAINT suse_cvesc_cid_fk;
ALTER TABLE suseCVEServerChannel ADD CONSTRAINT suse_cvesc_cid_fk FOREIGN KEY (channel_id) REFERENCES rhnChannel(id) ON DELETE CASCADE;

-- Recreate FK constraint on rhnServer
ALTER TABLE suseCVEServerChannel DROP CONSTRAINT suse_cvesc_sid_fk;
ALTER TABLE suseCVEServerChannel ADD CONSTRAINT suse_cvesc_sid_fk FOREIGN KEY (server_id) REFERENCES rhnServer(id) ON DELETE CASCADE;

