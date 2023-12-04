CREATE INDEX IF NOT EXISTS rhn_snc_seid_idx
    ON rhnServerNeededCache (server_id, errata_id);
