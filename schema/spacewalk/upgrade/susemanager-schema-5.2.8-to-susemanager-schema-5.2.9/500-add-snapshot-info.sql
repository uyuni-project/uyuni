CREATE TABLE IF NOT EXISTS suseMinionSnapshotInfo
(
    minion_server_id  NUMERIC NOT NULL
                          CONSTRAINT suse_minion_snapshot_info_sid_fk
                              REFERENCES suseMinionInfo (server_id)
                              ON DELETE CASCADE,
    active_snapshot   NUMERIC,
    default_snapshot  NUMERIC,
    snapshots         VARCHAR,
    snapshot_details  TEXT,

    CONSTRAINT suse_minion_snapshot_info_pk PRIMARY KEY (minion_server_id)
);

