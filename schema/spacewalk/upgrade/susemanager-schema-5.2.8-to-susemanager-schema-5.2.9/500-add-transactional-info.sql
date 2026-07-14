CREATE TABLE IF NOT EXISTS suseMinionTransactionalInfo
(
    minion_server_id NUMERIC NOT NULL
                         CONSTRAINT suse_minion_transactional_info_sid_fk
                             REFERENCES suseMinionInfo (server_id)
                             ON DELETE CASCADE,
    active_snapshot  NUMERIC,
    default_snapshot NUMERIC,
    snapshots        VARCHAR,
    snapshot_details      TEXT,
    pending_reboot_action_id NUMERIC REFERENCES rhnAction(id) ON DELETE SET NULL,
    pending_reboot_set_at TIMESTAMPTZ,

    CONSTRAINT suse_minion_transactional_info_pk PRIMARY KEY (minion_server_id)
);
