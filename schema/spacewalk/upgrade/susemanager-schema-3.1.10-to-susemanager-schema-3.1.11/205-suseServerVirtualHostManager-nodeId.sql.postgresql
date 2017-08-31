-- oracle equivalent source sha1 ceb9a528d48141e0bff81cf87813ac7d318f1702
ALTER TABLE suseServerVirtualHostManager ALTER COLUMN
    server_id DROP NOT NULL;

ALTER TABLE suseServerVirtualHostManager ADD COLUMN
    node_id NUMERIC CONSTRAINT suse_svhm_nodeinfo_fk
                    REFERENCES suseVirtualHostManagerNodeInfo (id)
                    ON DELETE SET NULL;

ALTER TABLE suseServerVirtualHostManager DROP CONSTRAINT suse_server_vhms_sid_fk;

ALTER TABLE suseServerVirtualHostManager ADD CONSTRAINT suse_server_vhms_sid_fk
    FOREIGN KEY(server_id) REFERENCES rhnServer(id) ON DELETE SET NULL;
